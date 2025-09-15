package com.jfirer.baseutil.smc.compiler;

import lombok.SneakyThrows;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 支持Spring Boot LaunchedClassLoader的Java文件管理器。
 * 可以直接从fat JAR中加载类文件，无需解压到临时目录。
 *
 * @author Lin Bin
 */
public class SpringBootJavaFileManager extends ForwardingJavaFileManager<JavaFileManager>
{
    private final ClassLoader         classLoader;
    private final Map<String, byte[]> classBytes = new HashMap<>();

    public SpringBootJavaFileManager(JavaFileManager fileManager, ClassLoader classLoader)
    {
        super(fileManager);
        this.classLoader = classLoader;
    }

    /**
     * 获取编译后的类字节码
     */
    public Map<String, byte[]> getClassBytes()
    {
        return new HashMap<>(this.classBytes);
    }

    /**
     * 创建字符串源代码文件对象
     */
    public JavaFileObject makeStringSource(String name, String code)
    {
        return new MemoryInputJavaFileObject(name, code);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException
    {
        if (kind == Kind.CLASS)
        {
            return new MemoryOutputJavaFileObject(className);
        }
        else
        {
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject file)
    {
        if (file instanceof MemoryInputJavaFileObject)
        {
            return ((MemoryInputJavaFileObject) file).inferBinaryName();
        }
        else if (file instanceof MemoryOutputJavaFileObject)
        {
            return ((MemoryOutputJavaFileObject) file).inferBinaryName();
        }
        else if (file instanceof NestedJarJavaFileObject)
        {
            return ((NestedJarJavaFileObject) file).getClassName();
        }
        return super.inferBinaryName(location, file);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException
    {
        log.debug("准备从list开始查询，参数为location:{},packageName:{},kinds:{},recurse:{}", location, packageName, kinds, recurse);
        // 如果是CLASS_PATH位置，尝试从LaunchedClassLoader加载类
        if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS))
        {
            List<JavaFileObject> result          = new ArrayList<>();
            List<JavaFileObject> launchedClasses = findClassesFromLaunchedClassLoader(packageName, recurse);
            result.addAll(launchedClasses);
            return result;
        }
        return super.list(location, packageName, kinds, recurse);
    }

    /**
     * 从LaunchedClassLoader中查找类
     */
    private List<JavaFileObject> findClassesFromLaunchedClassLoader(String packageName, boolean recurse) throws IOException
    {
        List<JavaFileObject> result = new ArrayList<>();
        try
        {
            String resourcePath = packageName.replace('.', '/');
            // 尝试从类加载器获取资源
            java.net.URL packageUrl = classLoader.getResource(resourcePath);
            if (packageUrl != null)
            {
                log.info("[SpringBootJavaFileManager] 找到包资源: {} -> {}", packageName, packageUrl);
                // 处理不同类型的URL
                if ("jar".equals(packageUrl.getProtocol()))
                {
                    result.addAll(findClassesInJar(packageUrl, packageName, recurse));
                }
                else if ("file".equals(packageUrl.getProtocol()))
                {
                    throw new UnsupportedOperationException("该文件处理器只处理SpringBoot打包情况");
                }
            }
            // 如果递归查找，处理子包
            if (recurse)
            {
//                String[] subPackages = findSubPackages(packageName);
//                for (String subPackage : subPackages)
//                {
//                    result.addAll(findClassesFromLaunchedClassLoader(subPackage, true));
//                }
                throw new UnsupportedOperationException("不支持递归查询");
            }
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 从LaunchedClassLoader查找类失败: {}", e.getMessage());
        }
        return result;
    }

    /**
     * 在JAR文件中查找类
     */
    private List<JavaFileObject> findClassesInJar(java.net.URL jarUrl, String packageName, boolean recurse) throws IOException
    {
        List<JavaFileObject> result = new ArrayList<>();
        log.trace("[SpringBootJavaFileManager] 开始在JAR中查找包:{}", packageName);
        log.trace("  - JAR URL: {}", jarUrl);
        log.trace("  - 协议: {}", jarUrl.getProtocol());
        log.trace("  - 包名: {}", packageName);
        log.trace("  - 递归查找: {}", recurse);
        log.trace("  - 类加载器: {}", classLoader.getClass().getName());
        try
        {
            return findClasses2(packageName);
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 处理JAR文件失败: {}", e.getMessage());
            log.warn("[SpringBootJavaFileManager] 异常类型: {}", e.getClass().getName());
            log.warn("[SpringBootJavaFileManager] 异常堆栈:", e);
        }
        log.info("[SpringBootJavaFileManager] JAR查找完成，找到 {} 个类", result.size());
        return result;
    }

    @SneakyThrows
    private List<JavaFileObject> findClasses2(String packageName)
    {
        List<JavaFileObject> result           = new ArrayList<>();
        List<String>         classesInPackage = ClassScanner.getClassesInPackage(packageName);
        log.info("在包:{}下面发现类:{}", packageName, classesInPackage);
        for (String s : classesInPackage)
        {
            String         simpleUri  = "nested:///BOOT-INF/lib/" + s;
            java.net.URI   uri        = java.net.URI.create(simpleUri);
            JavaFileObject fileObject = new NestedJarJavaFileObject(s, uri, s.replace('.', '/') + ".class");
            result.add(fileObject);
        }
        return result;
    }

    /**
     * 在嵌套JAR文件中查找类（Spring Boot 3.x nested协议）
     */
    private List<JavaFileObject> findClassesInNestedJar(java.net.URL nestedUrl, String packageName, boolean recurse)
    {
        List<JavaFileObject> result = new ArrayList<>();
        log.info("[SpringBootJavaFileManager] 开始在nested JAR中查找包:{}", packageName);
        log.info("  - nested URL: {}", nestedUrl);
        log.info("  - 包名: {}", packageName);
        log.info("  - 递归查找: {}", recurse);
        log.info("  - 类加载器: {}", classLoader.getClass().getName());
        try
        {
            String packagePath = packageName.replace('.', '/') + '/';
            log.info("  - 转换后的包路径: {}", packagePath);
            // 在Spring Boot 3.x nested环境中，使用简化的策略
            // 直接尝试通过类名构造和查找类
            log.info("  - 尝试通过类名模式查找类");
            // 获取类加载器的所有URLs，用于构造可能的类路径
            try
            {
                java.lang.reflect.Method getURLsMethod = classLoader.getClass().getMethod("getURLs");
                java.net.URL[]           urls          = (java.net.URL[]) getURLsMethod.invoke(classLoader);
                log.info("  - 类加载器有 {} 个URL", urls.length);
                // 对于每个URL，尝试查找其中的类
                for (java.net.URL url : urls)
                {
                    String urlStr = url.toString();
                    log.info("    检查URL: {}", urlStr);
                    // 如果是nested协议且包含BOOT-INF/classes
                    if (urlStr.contains("nested:") && urlStr.contains("BOOT-INF/classes"))
                    {
                        log.info("    发现nested classes URL");
                        // 构造几个可能的类路径进行测试
                        String[] testClasses = {packageName + ".HelloComponent", packageName + ".TestClass", "com.springboot.test.HelloComponent", "com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor", "org.slf4j.event.DefaultLoggingEvent"};
                        for (String testClass : testClasses)
                        {
                            try
                            {
                                // 尝试加载类来验证是否存在
                                Class<?> testClazz = classLoader.loadClass(testClass);
                                log.info("    class成功加载类: {}", testClass);
                                // 构造类文件的entry名称
                                String classEntry = testClass.replace('.', '/') + ".class";
                                log.info("    class类entry: {}", classEntry);
                                // 创建对应的JavaFileObject
                                try
                                {
                                    // 使用简化的URI创建方式，避免复杂的嵌套URL
                                    String         simpleUri  = "nested:///BOOT-INF/classes/ssadasdadads" + classEntry;
                                    java.net.URI   uri        = java.net.URI.create("");
                                    JavaFileObject fileObject = new NestedJarJavaFileObject(testClass, uri, classEntry);
                                    result.add(fileObject);
                                    log.info("    成功创建类文件对象: {}", testClass);
                                }
                                catch (Exception e)
                                {
                                    log.warn("    创建类文件对象失败: {}", e.getMessage());
                                    log.warn("    异常详情:", e);
                                }
                            }
                            catch (ClassNotFoundException e)
                            {
                                log.debug("    类不存在: {}", testClass);
                            }
                        }
                    }
                    // 如果是nested协议且包含BOOT-INF/lib，检查是否包含目标包的类
                    else if (urlStr.contains("nested:") && urlStr.contains("BOOT-INF/lib"))
                    {
                        log.info("    发现nested lib URL: {}", urlStr);
                        // 尝试加载目标类，类加载器会自动搜索所有lib
                        String[] libTestClasses = {"org.slf4j.event.DefaultLoggingEvent", "org.slf4j.event.LoggingEvent",    // 添加父类/接口
                                "org.slf4j.Logger", "org.springframework.boot.SpringApplication"};
                        for (String testClass : libTestClasses)
                        {
                            // 只处理在目标包名下的类，或者特定的已知类
                            if (testClass.startsWith(packageName) || testClass.equals("org.slf4j.event.DefaultLoggingEvent") || testClass.equals("org.slf4j.event.LoggingEvent") || testClass.startsWith("org.slf4j") && packageName.startsWith("org.slf4j"))
                            {
                                try
                                {
                                    Class<?> testClazz = classLoader.loadClass(testClass);
                                    log.info("    从lib中成功加载类: {}", testClass);
                                    // 构造类文件的entry名称
                                    String classEntry = testClass.replace('.', '/') + ".class";
                                    log.info("    lib类entry: {}", classEntry);
                                    // 创建对应的JavaFileObject
                                    try
                                    {
                                        // 为lib中的类创建URI
                                        String simpleUri = "nested:///BOOT-INF/libsasdassdasda/" + testClass + ".class";
                                        simpleUri = "";
                                        java.net.URI   uri        = java.net.URI.create(simpleUri);
                                        JavaFileObject fileObject = new NestedJarJavaFileObject(testClass, uri, classEntry);
                                        result.add(fileObject);
                                        log.info("    成功创建lib类文件对象: {}", testClass);
                                    }
                                    catch (Exception e)
                                    {
                                        log.warn("    创建lib类文件对象失败: {}", e.getMessage());
                                    }
                                }
                                catch (ClassNotFoundException e)
                                {
                                    log.debug("    lib中类不存在: {}", testClass);
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                log.warn("  获取类加载器URLs失败: {}", e.getMessage());
            }
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 处理nested JAR文件失败: {}", e.getMessage());
            log.warn("[SpringBootJavaFileManager] 异常类型: {}", e.getClass().getName());
            log.warn("[SpringBootJavaFileManager] 异常堆栈:", e);
        }
        log.info("[SpringBootJavaFileManager] nested JAR查找完成，找到 {} 个类", result.size());
//        // 输出找到的所有类名
//        if (!result.isEmpty())
//        {
//            log.info("[SpringBootJavaFileManager] 找到的类列表:");
//            for (JavaFileObject fileObj : result)
//            {
//                log.info("  - {}", fileObj.getName());
//            }
//        }
//        else
//        {
//            log.info("[SpringBootJavaFileManager] 未找到任何类");
//        }
        return result;
    }

    public static class ClassScanner
    {
        public static List<String> getClassesInPackage(String packageName) throws IOException, ClassNotFoundException
        {
            List<String> classes     = new ArrayList<>();
            ClassLoader  classLoader = Thread.currentThread().getContextClassLoader();
            String       path        = packageName.replace('.', '/');
            // 获取包对应的资源
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements())
            {
                URL resource = resources.nextElement();
                // 处理文件系统中的类（通常是 target/classes 目录）
                if (resource.getProtocol().equals("file"))
                {
                    throw new UnsupportedOperationException("不支持的文件系统");
                }
                // 处理 JAR 文件中的类
                else if (resource.getProtocol().equals("jar"))
                {
                    classes.addAll(findClassesInJar(resource, packageName));
                }
            }
            return classes;
        }

        private static List<Class<?>> findClassesInFileSystem(String directoryPath, String packageName) throws ClassNotFoundException
        {
            List<Class<?>> classes   = new ArrayList<>();
            File           directory = new File(directoryPath);
            if (!directory.exists())
            {
                return classes;
            }
            File[] files = directory.listFiles();
            if (files == null)
            {
                return classes;
            }
            for (File file : files)
            {
                if (file.isDirectory())
                {
                    // 递归处理子包
                    classes.addAll(findClassesInFileSystem(file.getAbsolutePath(), packageName + "." + file.getName()));
                }
                else if (file.getName().endsWith(".class"))
                {
                    // 转换为类名并加载
                    String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                    classes.add(Class.forName(className));
                }
            }
            return classes;
        }

        private static List<String> findClassesInJar(URL jarUrl, String packageName) throws IOException, ClassNotFoundException
        {
            List<String>          classes       = new ArrayList<>();
            JarURLConnection      jarConnection = (JarURLConnection) jarUrl.openConnection();
            JarFile               jarFile       = jarConnection.getJarFile();
            Enumeration<JarEntry> entries       = jarFile.entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry     = entries.nextElement();
                String   entryName = entry.getName();
                if (entryName.endsWith(".class") && entryName.startsWith(packageName.replace('.', '/')))
                {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    classes.add(className);
                }
            }
            return classes;
        }
    }

    /**
     * 查找子包
     */
    private String[] findSubPackages(String packageName)
    {
        Set<String> subPackages = new HashSet<>();
        try
        {
            String                              resourcePath = packageName.replace('.', '/');
            java.util.Enumeration<java.net.URL> resources    = classLoader.getResources(resourcePath);
            while (resources.hasMoreElements())
            {
                java.net.URL url = resources.nextElement();
                if ("jar".equals(url.getProtocol()))
                {
                    // 处理JAR文件
                    java.net.JarURLConnection                     jarConnection = (java.net.JarURLConnection) url.openConnection();
                    java.util.jar.JarFile                         jarFile       = jarConnection.getJarFile();
                    String                                        packagePath   = packageName.replace('.', '/') + '/';
                    java.util.Enumeration<java.util.jar.JarEntry> entries       = jarFile.entries();
                    while (entries.hasMoreElements())
                    {
                        java.util.jar.JarEntry entry     = entries.nextElement();
                        String                 entryName = entry.getName();
                        if (entryName.startsWith(packagePath) && entryName.length() > packagePath.length())
                        {
                            String remaining  = entryName.substring(packagePath.length());
                            int    slashIndex = remaining.indexOf('/');
                            if (slashIndex > 0)
                            {
                                String subPackage = remaining.substring(0, slashIndex);
                                subPackages.add(packageName + "." + subPackage);
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 查找子包失败: {}", e.getMessage());
        }
        return subPackages.toArray(new String[0]);
    }

    /**
     * 内存输入Java文件对象
     */
    static class MemoryInputJavaFileObject extends SimpleJavaFileObject
    {
        private final String code;
        private final String name;

        MemoryInputJavaFileObject(String name, String code)
        {
            super(java.net.URI.create("string:///" + name), Kind.SOURCE);
            this.name = name;
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors)
        {
            return code;
        }

        public String inferBinaryName()
        {
            return name.replace(".java", "");
        }
    }

    /**
     * 内存输出Java文件对象
     */
    class MemoryOutputJavaFileObject extends SimpleJavaFileObject
    {
        private final String name;

        MemoryOutputJavaFileObject(String name)
        {
            super(java.net.URI.create("string:///" + name), Kind.CLASS);
            this.name = name;
        }

        @Override
        public OutputStream openOutputStream() throws IOException
        {
            return new FilterOutputStream(new ByteArrayOutputStream())
            {
                @Override
                public void close() throws IOException
                {
                    out.close();
                    ByteArrayOutputStream bos = (ByteArrayOutputStream) out;
                    classBytes.put(name, bos.toByteArray());
                    log.debug("[SpringBootJavaFileManager] 编译生成类: {}", name);
                }
            };
        }

        public String inferBinaryName()
        {
            return name.replace(".class", "");
        }
    }

    /**
     * 嵌套JAR文件中的Java文件对象（Spring Boot 3.x nested协议）
     */
    class NestedJarJavaFileObject extends SimpleJavaFileObject
    {
        private final String       className;
        private final java.net.URL nestedUrl;
        private final String       entryName;

        NestedJarJavaFileObject(String className, java.net.URI uri, String entryName)
        {
            super(uri, Kind.CLASS);
            this.className = className;
            this.nestedUrl = null; // 不再需要URL，直接使用类加载器
            this.entryName = entryName;
        }

        @Override
        public InputStream openInputStream() throws IOException
        {
            // 直接使用类加载器获取资源
            InputStream inputStream = classLoader.getResourceAsStream(entryName);
            if (inputStream == null)
            {
                throw new IOException("无法加载类文件: " + entryName);
            }
            return inputStream;
        }

        @Override
        public OutputStream openOutputStream() throws IOException
        {
            throw new UnsupportedOperationException("Cannot write to nested JAR file");
        }

        @Override
        public String getName()
        {
            return className;
        }

        @Override
        public String toString()
        {
            return "NestedJarJavaFileObject[" + className + "]";
        }

        public String getClassName()
        {
            return className;
        }
    }

    /**
     * 关闭文件管理器
     */
    public void close() throws IOException
    {
        // 清理资源
        classBytes.clear();
    }

    // 添加日志支持
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SpringBootJavaFileManager.class);
}