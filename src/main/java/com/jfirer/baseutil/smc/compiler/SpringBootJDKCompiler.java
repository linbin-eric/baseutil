package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;

import javax.tools.*;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Spring Boot环境下的JDK编译器实现。
 * 解决了Spring Boot fat jar中类加载器问题导致的编译失败。
 *
 * @author Lin Bin
 */
public class SpringBootJDKCompiler implements Compiler
{
    private final JavaCompiler compiler;
    private final ClassLoader    classLoader;
    private final File           tempDir;
    private final List<File>     tempFiles = new ArrayList<>();

    public SpringBootJDKCompiler()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SpringBootJDKCompiler(ClassLoader classLoader)
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null)
        {
            throw new IllegalStateException("当前处于JRE环境无法获得JavaCompiler实例。如果是在windows，可以将JDK/lib目录下的tools.jar拷贝到jre/lib目录。如果是linux，将JAVA_HOME设置为jdk的");
        }
        this.classLoader = classLoader;
        // 创建临时目录
        this.tempDir = createTempDirectory();
        // 添加JVM退出时删除临时文件的钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            cleanupTempFiles();
        }));
    }

    private File createTempDirectory()
    {
        try
        {
            Path tempPath = Files.createTempDirectory("springboot-javac-");
            return tempPath.toFile();
        }
        catch (IOException e)
        {
            throw new RuntimeException("无法创建临时目录", e);
        }
    }

    private void cleanupTempFiles()
    {
        for (File file : tempFiles)
        {
            try
            {
                if (file.exists())
                {
                    if (file.isDirectory())
                    {
                        deleteDirectory(file);
                    }
                    else
                    {
                        file.delete();
                    }
                }
            }
            catch (Exception e)
            {
                // 忽略删除错误
            }
        }
        if (tempDir.exists())
        {
            deleteDirectory(tempDir);
        }
    }

    private void deleteDirectory(File directory)
    {
        File[] files = directory.listFiles();
        if (files != null)
        {
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    deleteDirectory(file);
                }
                else
                {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
        try
        {
            // 创建支持Spring Boot类加载器的FileManager
            StandardJavaFileManager standardManager = compiler.getStandardFileManager(null, null, null);
            SpringBootAwareFileManager springBootManager = new SpringBootAwareFileManager(standardManager, classLoader, tempDir);

            String         source         = classModel.toStringWithLineNo();
            JavaFileObject javaFileObject = manager.makeStringSource(classModel.fileName(), source);
            StringWriter   writer         = new StringWriter();

            // 配置编译选项，包括类路径
            List<String> options = new ArrayList<>();
            options.add("-classpath");
            String classPath = buildClassPath(classLoader);
            // 调试输出：打印类路径
            options.add(classPath);

            JavaCompiler.CompilationTask task   = compiler.getTask(writer, springBootManager, null, options, null, Arrays.asList(javaFileObject));
            Boolean                      result = task.call();

            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed. The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
            }

            // 从SpringBootAwareFileManager获取编译结果
            return springBootManager.getClassBytes();
        }
        finally
        {
            manager.clear();
        }
    }

    /**
     * 构建类路径字符串
     */
    private String buildClassPath(ClassLoader classLoader)
    {
        StringBuilder classPath = new StringBuilder();

        // 方法1：获取 fat jar 文件路径
        String fatJarPath = findFatJarPath();

        if (fatJarPath != null)
        {
            // 解压并处理 BOOT-INF/classes 和 BOOT-INF/lib
            extractBootInfResources(fatJarPath, classPath);
        }

        // 如果类路径仍然为空，尝试其他方法
        if (classPath.length() == 0)
        {

            // 方法2：从系统属性获取
            String systemClassPath = System.getProperty("java.class.path");
            if (systemClassPath != null && !systemClassPath.isEmpty())
            {
                classPath.append(systemClassPath);
            }

            // 方法3：尝试通过反射获取 Spring Boot 的类路径
            if (classPath.length() == 0)
            {
                tryAddSpringBootClassPath(classPath);
            }
        }

        // 如果仍然没有找到任何类路径，添加当前目录
        if (classPath.length() == 0)
        {
            classPath.append(".");
        }

        return classPath.toString();
    }

    private String findFatJarPath()
    {
        try
        {
            // 从系统属性中查找主 jar 文件
            String sunJavaCommand = System.getProperty("sun.java.command");
            if (sunJavaCommand != null && sunJavaCommand.endsWith(".jar"))
            {
                return sunJavaCommand;
            }

            // 从类加载器中查找
            if (classLoader instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader) classLoader).getURLs();
                for (URL url : urls)
                {
                    String path = url.getPath();
                    if (path.endsWith(".jar") && path.contains("drg-web"))
                    {
                        return path;
                    }
                }
            }

            // 尝试从 LaunchedURLClassLoader 中获取
            if (classLoader.getClass().getName().contains("LaunchedURLClassLoader"))
            {
                try
                {
                    Field ucpField = classLoader.getClass().getDeclaredField("ucp");
                    ucpField.setAccessible(true);
                    Object ucp = ucpField.get(classLoader);

                    Method getURLs = ucp.getClass().getDeclaredMethod("getURLs");
                    getURLs.setAccessible(true);
                    URL[] urls = (URL[]) getURLs.invoke(ucp);

                    for (URL url : urls)
                    {
                        String path = url.getPath();
                        if (path.endsWith(".jar") && path.contains("drg-web"))
                        {
                            return path;
                        }
                    }
                }
                catch (Exception e)
                {
                    // 忽略异常
                }
            }
        }
        catch (Exception e)
        {
            // 忽略异常
        }

        return null;
    }

    private void extractBootInfResources(String fatJarPath, StringBuilder classPath)
    {
        try
        {

            File fatJarFile = new File(fatJarPath.startsWith("file:") ?
                fatJarPath.substring(5) : fatJarPath);

            if (!fatJarFile.exists())
            {
                classPath.append(buildStandardClassPath(classLoader));
                return;
            }

            // 强制解压 BOOT-INF/classes 和需要的 jar 文件
            File extractedClassesDir = extractAllBootInfClasses(fatJarFile);
            if (extractedClassesDir != null)
            {
                if (classPath.length() > 0)
                {
                    classPath.append(File.pathSeparator);
                }
                classPath.append(extractedClassesDir.getAbsolutePath());
            }

            // 尝试解压需要的依赖 jar
            File extractedLibsDir = extractNeededLibraries(fatJarFile);
            if (extractedLibsDir != null)
            {
                // 添加所有解压的 jar 文件
                File[] jarFiles = extractedLibsDir.listFiles((dir, name) -> name.endsWith(".jar"));
                if (jarFiles != null)
                {
                    for (File jarFile : jarFiles)
                    {
                        if (classPath.length() > 0)
                        {
                            classPath.append(File.pathSeparator);
                        }
                        classPath.append(jarFile.getAbsolutePath());
                    }
                }
            }

            // 如果仍然没有添加任何内容，添加原始的 fat jar
            if (classPath.length() == 0)
            {
                classPath.append(fatJarFile.getAbsolutePath());
            }
        }
        catch (Exception e)
        {
            // 如果处理失败，使用标准类路径
            classPath.append(buildStandardClassPath(classLoader));
        }
    }

    private boolean buildClassPathFromClassLoader(StringBuilder classPath)
    {
        try
        {
            // 尝试从当前类加载器获取所有 URL
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();

            if (currentLoader instanceof URLClassLoader)
            {
                URL[] urls = ((URLClassLoader) currentLoader).getURLs();
                for (URL url : urls)
                {
                    String protocol = url.getProtocol();
                    if ("file".equals(protocol))
                    {
                        String path = url.getPath();
                        if (path.endsWith(".jar") || new File(path).isDirectory())
                        {
                            if (classPath.length() > 0)
                            {
                                classPath.append(File.pathSeparator);
                            }
                            classPath.append(path);
                        }
                    }
                }
                return true;
            }
            else if (currentLoader.getClass().getName().contains("LaunchedURLClassLoader"))
            {
                // 对于 Spring Boot 的 LaunchedURLClassLoader，尝试其他方法
                // 使用反射获取类路径
                try
                {
                    Method getURLsMethod = currentLoader.getClass().getMethod("getURLs");
                    URL[] urls = (URL[]) getURLsMethod.invoke(currentLoader);

                    for (URL url : urls)
                    {
                        String path = url.getPath();
                        if (path.contains(".jar"))
                        {
                            // 提取实际的 jar 文件路径
                            if (path.startsWith("file:"))
                            {
                                path = path.substring(5);
                            }
                            if (path.contains("!"))
                            {
                                path = path.substring(0, path.indexOf("!"));
                            }

                            if (classPath.length() > 0)
                            {
                                classPath.append(File.pathSeparator);
                            }
                            classPath.append(path);
                        }
                    }
                    return true;
                }
                catch (Exception e)
                {
                    // 反射失败，继续其他方法
                }
            }
        }
        catch (Exception e)
        {
            // 忽略错误
        }
        return false;
    }

    private File extractAllBootInfClasses(File fatJarFile)
    {
        try
        {
            // 创建临时目录
            File classesDir = new File(tempDir, "BOOT-INF-classes");
            classesDir.mkdirs();
            tempFiles.add(classesDir);


            // 使用 JarFile 读取
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(fatJarFile);
            Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

            int extractedCount = 0;

            while (entries.hasMoreElements())
            {
                java.util.jar.JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 提取 BOOT-INF/classes 下的所有文件
                if (entryName.startsWith("BOOT-INF/classes/") && !entryName.endsWith("/"))
                {
                    // 转换路径 - 去掉 BOOT-INF/classes/ 前缀
                    String relativePath = entryName.substring("BOOT-INF/classes/".length());
                    File targetFile = new File(classesDir, relativePath);
                    targetFile.getParentFile().mkdirs();

                    try (InputStream is = jarFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(targetFile))
                    {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1)
                        {
                            fos.write(buffer, 0, bytesRead);
                        }
                        extractedCount++;
                    }
                }
            }

            jarFile.close();

            if (extractedCount > 0)
            {
                return classesDir;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private File extractNeededLibraries(File fatJarFile)
    {
        try
        {
            // 创建临时目录
            File libDir = new File(tempDir, "BOOT-INF-lib");
            libDir.mkdirs();
            tempFiles.add(libDir);


            // 使用 JarFile 读取
            java.util.jar.JarFile jarFile = new java.util.jar.JarFile(fatJarFile);
            Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();

            // 需要提取的库文件名模式
            String[] neededLibs = {
                "baseutil-",          // 我们自己的工具库
                "jsql-",              // JSql 相关
                "jfireEL-",           // EL 表达式
                "drg-common-",        // 项目公共模块
                "drg-route-",         // 项目路由模块
                "spring-",            // Spring 框架
                "mybatis-",           // MyBatis
                "mysql-connector-",   // MySQL 驱动
                "logback-",           // 日志框架
                "slf4j-",             // SLF4J
                "jakarta.persistence-", // JPA
                "hibernate-",         // Hibernate
                "jackson-",           // Jackson JSON
                "fastjson"            // FastJSON
            };

            int extractedCount = 0;

            while (entries.hasMoreElements())
            {
                java.util.jar.JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 检查是否是 BOOT-INF/lib 下的 jar 文件
                if (entryName.startsWith("BOOT-INF/lib/") && entryName.endsWith(".jar"))
                {
                    String jarName = entryName.substring("BOOT-INF/lib/".length());

                    // 检查是否在需要的库列表中
                    boolean needed = false;
                    for (String libPattern : neededLibs)
                    {
                        if (jarName.contains(libPattern))
                        {
                            needed = true;
                            break;
                        }
                    }

                    if (needed)
                    {
                        File targetJar = new File(libDir, jarName);
                        targetJar.getParentFile().mkdirs();

                        try (InputStream is = jarFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(targetJar))
                        {
                            byte[] buffer = new byte[4096];
                            int bytesRead;
                            while ((bytesRead = is.read(buffer)) != -1)
                            {
                                fos.write(buffer, 0, bytesRead);
                            }
                            extractedCount++;
                        }
                    }
                }
            }

            jarFile.close();

            if (extractedCount > 0)
            {
                return libDir;
            }
            else
            {
                return null;
            }
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private void tryAddSpringBootClassPath(StringBuilder classPath)
    {
        try
        {

            // 获取当前应用的 Archive
            Class<?> launcherClass = Class.forName("org.springframework.boot.loader.Launcher");
            Class<?> archiveClass = Class.forName("org.springframework.boot.loader.archive.Archive");

            // 获取主类的类加载器
            ClassLoader mainClassLoader = this.getClass().getClassLoader();

            // 尝试获取 Archive 的入口
            Method createArchiveMethod = archiveClass.getMethod("create", File.class);
            Object archive = createArchiveMethod.invoke(null, new File(findFatJarPath()));

            // 获取所有的 URL
            Method getUrlMethod = archiveClass.getMethod("getUrl");
            URL archiveUrl = (URL) getUrlMethod.invoke(archive);


            // 将 Archive 添加到类路径
            if (classPath.length() > 0)
            {
                classPath.append(File.pathSeparator);
            }
            classPath.append(archiveUrl.getPath());

            // 尝试获取 nested archives
            Method getNestedArchivesMethod = archiveClass.getMethod("getNestedArchives", archiveClass);
            Object nestedArchives = getNestedArchivesMethod.invoke(archive, null);

            // 如果是集合，遍历处理
            if (nestedArchives instanceof Collection)
            {
                for (Object nestedArchive : (Collection<?>) nestedArchives)
                {
                    URL nestedUrl = (URL) getUrlMethod.invoke(nestedArchive);
                    String path = nestedUrl.getPath();

                    // 只处理 lib 目录中的 jar
                    if (path.contains("BOOT-INF/lib/"))
                    {
                        if (classPath.length() > 0)
                        {
                            classPath.append(File.pathSeparator);
                        }
                        classPath.append(path);
                    }
                }
            }
        }
        catch (Exception e)
        {

            // 尝试另一种方法 - 直接从类路径中找到相关的 jar
            tryAddClassPathFromRunningJars(classPath);
        }
    }

    private void tryAddClassPathFromRunningJars(StringBuilder classPath)
    {
        try
        {

            // 获取所有已加载的 URL
            ClassLoader loader = Thread.currentThread().getContextClassLoader();

            // 尝试不同的方式获取 URLs
            URL[] urls = new URL[0];

            // 方法1：如果是 URLClassLoader
            if (loader instanceof URLClassLoader)
            {
                urls = ((URLClassLoader) loader).getURLs();
            }
            else
            {
                // 方法2：通过反射获取
                try
                {
                    Class<?> classLoaderClass = loader.getClass();
                    while (classLoaderClass != null)
                    {
                        try
                        {
                            Field ucpField = classLoaderClass.getDeclaredField("ucp");
                            ucpField.setAccessible(true);
                            Object ucp = ucpField.get(loader);

                            Method getURLsMethod = ucp.getClass().getDeclaredMethod("getURLs");
                            urls = (URL[]) getURLsMethod.invoke(ucp);
                            break;
                        }
                        catch (NoSuchFieldException e)
                        {
                            classLoaderClass = classLoaderClass.getSuperclass();
                        }
                    }
                }
                catch (Exception e)
                {
                }
            }

            // 处理找到的 URLs
            Set<String> addedPaths = new HashSet<>();
            for (URL url : urls)
            {
                String path = url.getPath();

                // 处理不同的协议
                if ("file".equals(url.getProtocol()))
                {
                    // 如果是文件路径，直接添加
                    if (!addedPaths.contains(path))
                    {
                        if (classPath.length() > 0)
                        {
                            classPath.append(File.pathSeparator);
                        }
                        classPath.append(path);
                        addedPaths.add(path);
                    }
                }
                else if ("jar".equals(url.getProtocol()))
                {
                    // 处理 jar URL
                    if (path.startsWith("file:") && path.contains("!"))
                    {
                        String jarPath = path.substring(5, path.indexOf("!"));
                        if (!addedPaths.contains(jarPath))
                        {
                            if (classPath.length() > 0)
                            {
                                classPath.append(File.pathSeparator);
                            }
                            classPath.append(jarPath);
                            addedPaths.add(jarPath);
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    private String buildStandardClassPath(ClassLoader classLoader)
    {
        StringBuilder classPath = new StringBuilder();

        // 获取系统类路径
        String systemClassPath = System.getProperty("java.class.path");
        if (systemClassPath != null && !systemClassPath.isEmpty())
        {
            classPath.append(systemClassPath);
        }

        return classPath.toString();
    }

    /**
     * 支持Spring Boot类加载器的FileManager
     */
    private class SpringBootAwareFileManager extends ForwardingJavaFileManager<JavaFileManager>
    {
        private final Map<String, byte[]> classBytes = new HashMap<>();
        private final ClassLoader          mainClassLoader;
        private final ClassLoader          compilerClassLoader;
        private final File                 tempDir;

        protected SpringBootAwareFileManager(JavaFileManager fileManager, ClassLoader classLoader, File tempDir)
        {
            super(fileManager);
            this.mainClassLoader = classLoader;
            this.tempDir = tempDir;

            // 创建一个专门的编译器类加载器
            this.compilerClassLoader = createCompilerClassLoader();
        }

        private ClassLoader createCompilerClassLoader()
        {
            try
            {
                // 收集所有需要添加到类路径的 URL
                List<URL> urls = new ArrayList<>();

                // 1. 添加解压后的 classes 目录
                File classesDir = new File(tempDir, "BOOT-INF-classes");
                if (classesDir.exists())
                {
                    urls.add(classesDir.toURI().toURL());
                }

                // 2. 添加解压后的 lib 目录中的所有 jar
                File libDir = new File(tempDir, "BOOT-INF-lib");
                if (libDir.exists())
                {
                    File[] jarFiles = libDir.listFiles((dir, name) -> name.endsWith(".jar"));
                    if (jarFiles != null)
                    {
                        for (File jarFile : jarFiles)
                        {
                            urls.add(jarFile.toURI().toURL());
                        }
                    }
                }

                // 3. 添加原始的 fat jar
                String fatJarPath = findFatJarPath();
                if (fatJarPath != null)
                {
                    File fatJarFile = new File(fatJarPath.startsWith("file:") ?
                        fatJarPath.substring(5) : fatJarPath);
                    if (fatJarFile.exists())
                    {
                        urls.add(fatJarFile.toURI().toURL());
                    }
                }


                // 创建新的 URLClassLoader
                return new URLClassLoader(urls.toArray(new URL[0]), mainClassLoader.getParent());
            }
            catch (Exception e)
            {
                return mainClassLoader;
            }
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling)
        {
            return new MemoryOutputJavaFileObject(className, kind);
        }

        @Override
        public ClassLoader getClassLoader(Location location)
        {
            // 返回专门的编译器类加载器
            return compilerClassLoader;
        }

        public Map<String, byte[]> getClassBytes()
        {
            return Collections.unmodifiableMap(classBytes);
        }

        /**
         * 内存输出Java文件对象
         */
        private class MemoryOutputJavaFileObject extends SimpleJavaFileObject
        {
            private final String className;

            protected MemoryOutputJavaFileObject(String className, Kind kind)
            {
                super(URI.create("string:///" + className), kind);
                this.className = className;
            }

            @Override
            public OutputStream openOutputStream()
            {
                return new ByteArrayOutputStream()
                {
                    @Override
                    public void close() throws IOException
                    {
                        super.close();
                        classBytes.put(className, toByteArray());
                    }
                };
            }
        }
    }
}