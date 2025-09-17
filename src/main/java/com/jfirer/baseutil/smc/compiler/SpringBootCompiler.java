package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.compiler.jdk.MemoryJavaFileManager;
import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Spring Boot环境下的编译器实现。
 * 通过检查Spring Boot的loader类来判断当前是否在Spring Boot环境中，
 * 并在编译时自动设置正确的classpath。
 *
 * @author Lin Bin
 */
@Slf4j
public class SpringBootCompiler implements Compiler
{
    private final JavaCompiler compiler;
    private final ClassLoader  classLoader;
    private final Path tempDir;
    private final Set<String> extractedJars = new HashSet<>();

    public SpringBootCompiler()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SpringBootCompiler(ClassLoader classLoader)
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
//        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanupTempDirectory));
    }

    /**
     * 检查当前是否在Spring Boot环境中
     */
    public static boolean isSpringBootEnvironment()
    {
        try
        {
            // 检查Spring Boot的LaunchedURLClassLoader是否存在
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            log.info("当前的loader:{}",classLoader.getClass().getName());
            String loaderClassName = classLoader.getClass().getName();

            // Spring Boot 3.x 使用 LaunchedClassLoader
            // Spring Boot 2.x 使用 LaunchedURLClassLoader
            boolean containsLaunched = loaderClassName.contains("LaunchedURLClassLoader") ||
                                      loaderClassName.contains("LaunchedClassLoader");

            boolean canLoadLegacyLauncher = false;
            boolean canLoadNewLauncher = false;

            try
            {
                canLoadLegacyLauncher = Class.forName("org.springframework.boot.loader.LaunchedURLClassLoader") != null;
            }
            catch (ClassNotFoundException e1)
            {
                // Spring Boot 3.x 中这个类不存在，忽略异常
            }

            try
            {
                canLoadNewLauncher = Class.forName("org.springframework.boot.loader.launch.LaunchedClassLoader") != null;
            }
            catch (ClassNotFoundException e2)
            {
                // Spring Boot 2.x 中这个类不存在，忽略异常
            }

            log.info("[SpringBootCompiler] 环境检测详情:");
            log.info("  - 当前类加载器: {}", loaderClassName);
            log.info("  - 包含LaunchedURLClassLoader: {}", loaderClassName.contains("LaunchedURLClassLoader"));
            log.info("  - 包含LaunchedClassLoader: {}", loaderClassName.contains("LaunchedClassLoader"));
            log.info("  - 可以加载LaunchedURLClassLoader: {}", canLoadLegacyLauncher);
            log.info("  - 可以加载LaunchedClassLoader: {}", canLoadNewLauncher);
            log.info("  - 最终结果: {}", containsLaunched || canLoadLegacyLauncher || canLoadNewLauncher);

            return containsLaunched || canLoadLegacyLauncher || canLoadNewLauncher;
        }
        catch (Exception e)
        {
            log.info("[SpringBootCompiler] 检测异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
        try
        {
            String         source         = classModel.toStringWithLineNo();
            JavaFileObject javaFileObject = manager.makeStringSource(classModel.fileName(), source);
            StringWriter   writer         = new StringWriter();
            // 配置编译选项，包括类路径
            List<String> options = new ArrayList<>();
            options.add("-classpath");
            String classPath = buildClassPath();
            options.add(classPath);
            JavaCompiler.CompilationTask task   = compiler.getTask(writer, manager, null, options, null, Arrays.asList(javaFileObject));
            Boolean                      result = task.call();
            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed. The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
            }
            return manager.getClassBytes();
        }
        finally
        {
            manager.clear();
        }
    }

    /**
     * 创建临时目录
     */
    private Path createTempDirectory()
    {
        try
        {
            Path tempDir = Files.createTempDirectory("springboot-compiler-");
            log.info("[SpringBootCompiler] 创建临时目录: {}", tempDir);
            return tempDir;
        }
        catch (IOException e)
        {
            log.error("[SpringBootCompiler] 创建临时目录失败", e);
            throw new RuntimeException("创建临时目录失败", e);
        }
    }

    /**
     * 清理临时目录
     */
    private void cleanupTempDirectory()
    {
        try
        {
//            if (tempDir != null && Files.exists(tempDir))
//            {
//                log.info("[SpringBootCompiler] 清理临时目录: {}", tempDir);
////                deleteDirectory(tempDir.toFile());
//            }
        }
        catch (Exception e)
        {
            log.warn("[SpringBootCompiler] 清理临时目录失败", e);
        }
    }

    /**
     * 递归删除目录
     */
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

    /**
     * 解压FatJar到临时目录
     */
    private void extractFatJar(String jarPath) throws IOException
    {
        if (extractedJars.contains(jarPath))
        {
            log.info("[SpringBootCompiler] JAR已解压，跳过: {}", jarPath);
            return;
        }

        log.info("[SpringBootCompiler] 开始解压FatJar: {}", jarPath);

        try (JarFile jarFile = new JarFile(jarPath))
        {
            Enumeration<JarEntry> entries = jarFile.entries();
            int extractedCount = 0;
            int skippedCount = 0;

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // 跳过META-INF目录中的某些文件
                if (entryName.startsWith("META-INF/") &&
                    (entryName.endsWith(".SF") || entryName.endsWith(".DSA") || entryName.endsWith(".RSA")))
                {
                    skippedCount++;
                    continue;
                }

                // 创建目标文件路径
                Path destPath = tempDir.resolve(entryName);

                // 如果是目录，创建目录
                if (entry.isDirectory())
                {
                    Files.createDirectories(destPath);
                    continue;
                }

                // 确保父目录存在
                Files.createDirectories(destPath.getParent());

                // 解压文件
                try (InputStream is = jarFile.getInputStream(entry);
                     OutputStream os = Files.newOutputStream(destPath))
                {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1)
                    {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                extractedCount++;
            }

            extractedJars.add(jarPath);
            log.info("[SpringBootCompiler] FatJar解压完成: {}, 解压文件数: {}, 跳过文件数: {}",
                    jarPath, extractedCount, skippedCount);
        }
        catch (Exception e)
        {
            log.error("[SpringBootCompiler] 解压FatJar失败: {}", jarPath, e);
            throw e;
        }
    }

    /**
     * 构建类路径字符串
     */
    private String buildClassPath()
    {
        StringBuilder classPath = new StringBuilder();
        boolean isSpringBoot = isSpringBootEnvironment();
        
        log.info("[SpringBootCompiler] 构建类路径:");
        log.info("  - 检测到Spring Boot环境: {}", isSpringBoot);
        
        // 如果在Spring Boot环境中，使用Spring Boot的类路径构建逻辑
        if (isSpringBoot)
        {
            log.info("  - 使用Spring Boot类路径构建逻辑");
            buildSpringBootClassPath(classPath);
        }
        else
        {
            log.info("  - 使用标准类路径构建逻辑");
            // 非Spring Boot环境，使用标准类路径
            buildStandardClassPath(classPath);
        }
        
        // 如果类路径为空，添加当前目录
        if (classPath.length() == 0)
        {
            classPath.append(".");
            log.info("  - 类路径为空，添加当前目录");
        }
        
        String result = classPath.toString();
        log.info("  - 最终类路径长度: {}", result.length());
        log.info("  - 最终类路径: {}", result);
        
        return result;
    }

    /**
     * 构建Spring Boot环境的类路径
     */
    private void buildSpringBootClassPath(StringBuilder classPath)
    {
        log.info("[SpringBootCompiler] Spring Boot类路径构建开始:");
        try
        {
            // 方法1：尝试FatJar解压方式
            log.info("  - 尝试方法1：FatJar解压方式");
            boolean fatJarSuccess = buildClassPathFromFatJarExtraction(classPath);
            log.info("  - 方法1结果: {}", fatJarSuccess);

            // 如果FatJar解压成功，直接返回
            if (fatJarSuccess)
            {
                log.info("  - FatJar解压方式成功，使用解压后的类路径");
                return;
            }

            // 方法2：从类加载器获取URLs
            log.info("  - 尝试方法2：从类加载器获取URLs");
            boolean classLoaderSuccess = buildClassPathFromClassLoader(classPath);
            log.info("  - 方法2结果: {}", classLoaderSuccess);

            // 如果方法2也失败，尝试方法3：从系统属性获取
            if (!classLoaderSuccess)
            {
                log.info("  - 尝试方法3：从系统属性获取类路径");
                String systemClassPath = System.getProperty("java.class.path");
                log.info("  - 系统类路径长度: {}", (systemClassPath != null ? systemClassPath.length() : 0));

                if (systemClassPath != null && !systemClassPath.isEmpty())
                {
                    if (classPath.length() > 0)
                    {
                        classPath.append(File.pathSeparator);
                    }
                    classPath.append(systemClassPath);
                    log.info("  - 已添加系统类路径");
                }
                else
                {
                    log.info("  - 系统类路径为空");
                }
            }
        }
        catch (Exception e)
        {
            log.info("  - Spring Boot类路径构建异常: {}", e.getMessage());
            // 发生异常时使用标准类路径
            buildStandardClassPath(classPath);
        }
    }

    /**
     * 通过FatJar解压方式构建类路径
     */
    private boolean buildClassPathFromFatJarExtraction(StringBuilder classPath)
    {
        try
        {
            log.info("    - FatJar解压方式开始:");

            // 获取当前运行的JAR文件路径
            String jarPath = getCurrentRunningJarPath();
            if (jarPath == null)
            {
                log.info("    - 无法获取当前运行的JAR路径");
                return false;
            }

            log.info("    - 当前运行JAR路径: {}", jarPath);

            // 检查是否是FatJar（通过大小或名称判断）
            File jarFile = new File(jarPath);
            if (!jarFile.exists() || !jarFile.getName().endsWith(".jar"))
            {
                log.info("    - 文件不存在或不是JAR文件");
                return false;
            }

            // 解压FatJar到临时目录
            extractFatJar(jarPath);

            // 将临时目录添加到类路径
            if (classPath.length() > 0)
            {
                classPath.append(File.pathSeparator);
            }
            classPath.append(tempDir.toString());

            // 检查BOOT-INF/classes目录是否存在，如果存在也添加到类路径
            Path bootInfClasses = tempDir.resolve("BOOT-INF").resolve("classes");
            if (Files.exists(bootInfClasses))
            {
                classPath.append(File.pathSeparator);
                classPath.append(bootInfClasses.toString());
                log.info("    - 成功添加BOOT-INF/classes目录到类路径: {}", bootInfClasses);
            }

            // 检查BOOT-INF/lib目录是否存在，如果存在则添加其中的所有jar文件
            Path bootInfLib = tempDir.resolve("BOOT-INF").resolve("lib");
            if (Files.exists(bootInfLib) && Files.isDirectory(bootInfLib))
            {
                log.info("    - 发现BOOT-INF/lib目录，开始处理依赖库");
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(bootInfLib, "*.jar"))
                {
                    int libCount = 0;
                    for (Path libJarPath : stream)
                    {
                        if (Files.isRegularFile(libJarPath) && libJarPath.toString().endsWith(".jar"))
                        {
                            classPath.append(File.pathSeparator);
                            classPath.append(libJarPath.toString());
                            libCount++;
                            log.info("    - 成功添加依赖库: {}", libJarPath.getFileName());
                        }
                    }
                    log.info("    - 共添加 {} 个依赖库", libCount);
                }
                catch (Exception e)
                {
                    log.warn("    - 处理BOOT-INF/lib目录时出现异常: {}", e.getMessage());
                }
            }

            log.info("    - 成功添加临时目录到类路径: {}", tempDir);
            return true;
        }
        catch (Exception e)
        {
            log.info("    - FatJar解压方式失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前运行的JAR文件路径
     */
    private String getCurrentRunningJarPath()
    {
        try
        {
            // 方法1：通过ProtectionDomain获取
            String path = SpringBootCompiler.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (path != null && path.endsWith(".jar"))
            {
                log.info("      - 方法1成功: {}", path);
                return path;
            }

            // 方法2：通过系统属性获取
            String sunCommand = System.getProperty("sun.java.command");
            if (sunCommand != null && sunCommand.endsWith(".jar"))
            {
                // 尝试找到JAR文件路径
                String[] parts = sunCommand.split(" ");
                for (String part : parts)
                {
                    if (part.endsWith(".jar"))
                    {
                        File jarFile = new File(part);
                        if (jarFile.exists())
                        {
                            log.info("      - 方法2成功: {}", jarFile.getAbsolutePath());
                            return jarFile.getAbsolutePath();
                        }
                    }
                }
            }

            log.info("      - 无法获取JAR路径");
            return null;
        }
        catch (Exception e)
        {
            log.info("      - 获取JAR路径异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从类加载器构建类路径
     */
    private boolean buildClassPathFromClassLoader(StringBuilder classPath)
    {
        try
        {
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            String loaderClassName = currentLoader.getClass().getName();
            log.info("    - 当前类加载器类型: {}", loaderClassName);
            
            if (currentLoader instanceof URLClassLoader)
            {
                // 标准URLClassLoader处理
                log.info("    - 检测到标准URLClassLoader");
                URL[] urls = ((URLClassLoader) currentLoader).getURLs();
                log.info("    - URL数量: {}", (urls != null ? urls.length : 0));
                return addUrlsToClassPath(classPath, urls);
            }
            else if (loaderClassName.contains("LaunchedURLClassLoader"))
            {
                // Spring Boot的LaunchedURLClassLoader处理
                log.info("    - 检测到LaunchedURLClassLoader");
                return handleLaunchedURLClassLoader(classPath, currentLoader);
            }
            else if (loaderClassName.contains("LaunchedClassLoader"))
            {
                // Spring Boot 3.x的LaunchedClassLoader处理
                log.info("    - 检测到LaunchedClassLoader");
                return handleLaunchedClassLoader(classPath, currentLoader);
            }
            else
            {
                log.info("    - 未识别的类加载器类型");
            }
        }
        catch (Exception e)
        {
            log.info("    - 类加载器处理异常: {}", e.getMessage());
            // 忽略异常，返回失败
        }
        return false;
    }

    /**
     * 处理LaunchedURLClassLoader
     */
    private boolean handleLaunchedURLClassLoader(StringBuilder classPath, ClassLoader classLoader)
    {
        log.info("      - 开始处理LaunchedURLClassLoader");
        try
        {
            // 通过反射获取URLs
            log.info("      - 尝试通过反射获取URLs");
            URL[] urls = getURLsFromLaunchedURLClassLoader(classLoader);
            log.info("      - 获取到URL数量: {}", (urls != null ? urls.length : 0));

            if (urls != null)
            {
                boolean result = addUrlsToClassPath(classPath, urls);
                log.info("      - URL添加结果: {}", result);
                return result;
            }
        }
        catch (Exception e)
        {
            log.info("      - LaunchedURLClassLoader处理异常: {}", e.getMessage());
            // 忽略异常
        }
        log.info("      - LaunchedURLClassLoader处理失败");
        return false;
    }

    /**
     * 处理Spring Boot 3.x的LaunchedClassLoader
     */
    private boolean handleLaunchedClassLoader(StringBuilder classPath, ClassLoader classLoader)
    {
        log.info("      - 开始处理LaunchedClassLoader");
        try
        {
            // 通过反射获取URLs
            log.info("      - 尝试通过反射获取URLs");
            URL[] urls = getURLsFromLaunchedClassLoader(classLoader);
            log.info("      - 获取到URL数量: {}", (urls != null ? urls.length : 0));

            if (urls != null)
            {
                boolean result = addUrlsToClassPath(classPath, urls);
                log.info("      - URL添加结果: {}", result);
                return result;
            }
        }
        catch (Exception e)
        {
            log.info("      - LaunchedClassLoader处理异常: {}", e.getMessage());
            // 忽略异常
        }
        log.info("      - LaunchedClassLoader处理失败");
        return false;
    }

    /**
     * 通过反射从LaunchedURLClassLoader获取URLs
     */
    private URL[] getURLsFromLaunchedURLClassLoader(ClassLoader classLoader) throws Exception
    {
        log.info("        - 尝试通过反射获取URLs:");
        try
        {
            // 尝试直接调用getURLs方法
            log.info("        - 尝试方法1：直接调用getURLs方法");
            Method getURLsMethod = classLoader.getClass().getMethod("getURLs");
            URL[] urls = (URL[]) getURLsMethod.invoke(classLoader);
            log.info("        - 方法1成功，获取到URL数量: {}", (urls != null ? urls.length : 0));
            return urls;
        }
        catch (NoSuchMethodException e)
        {
            log.info("        - 方法1失败：getURLs方法不存在");
            // 如果直接方法不存在，尝试通过ucp字段获取
            try
            {
                log.info("        - 尝试方法2：通过ucp字段获取");
                Field ucpField = classLoader.getClass().getDeclaredField("ucp");
                ucpField.setAccessible(true);
                Object ucp = ucpField.get(classLoader);
                Method getURLsMethod = ucp.getClass().getDeclaredMethod("getURLs");
                getURLsMethod.setAccessible(true);
                URL[] urls = (URL[]) getURLsMethod.invoke(ucp);
                log.info("        - 方法2成功，获取到URL数量: {}", (urls != null ? urls.length : 0));
                return urls;
            }
            catch (Exception ex)
            {
                log.info("        - 方法2失败：{}", ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * 通过反射从LaunchedClassLoader获取URLs
     */
    private URL[] getURLsFromLaunchedClassLoader(ClassLoader classLoader) throws Exception
    {
        log.info("        - 尝试通过反射获取URLs:");
        try
        {
            // 尝试直接调用getURLs方法
            log.info("        - 尝试方法1：直接调用getURLs方法");
            Method getURLsMethod = classLoader.getClass().getMethod("getURLs");
            URL[] urls = (URL[]) getURLsMethod.invoke(classLoader);
            log.info("        - 方法1成功，获取到URL数量: {}", (urls != null ? urls.length : 0));
            return urls;
        }
        catch (NoSuchMethodException e)
        {
            log.info("        - 方法1失败：getURLs方法不存在");
            // 如果直接方法不存在，尝试通过ucp字段获取
            try
            {
                log.info("        - 尝试方法2：通过ucp字段获取");
                Field ucpField = classLoader.getClass().getDeclaredField("ucp");
                ucpField.setAccessible(true);
                Object ucp = ucpField.get(classLoader);
                Method getURLsMethod = ucp.getClass().getDeclaredMethod("getURLs");
                getURLsMethod.setAccessible(true);
                URL[] urls = (URL[]) getURLsMethod.invoke(ucp);
                log.info("        - 方法2成功，获取到URL数量: {}", (urls != null ? urls.length : 0));
                return urls;
            }
            catch (Exception ex)
            {
                log.info("        - 方法2失败：{}", ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * 将URLs添加到类路径
     */
    private boolean addUrlsToClassPath(StringBuilder classPath, URL[] urls)
    {
        if (urls == null || urls.length == 0)
        {
            log.info("        - URL数组为空");
            return false;
        }
        
        log.info("        - 开始处理 {} 个URLs", urls.length);
        Set<String> addedPaths = new HashSet<>();
        boolean hasValidPath = false;
        int validCount = 0;
        int duplicateCount = 0;
        
        for (int i = 0; i < urls.length; i++)
        {
            URL url = urls[i];
            try
            {
                String path = url.getPath();
                String protocol = url.getProtocol();
                log.info("        - URL[{}]: {}://{}", i, protocol, path);
                
                // 处理不同的协议
                if ("file".equals(protocol))
                {
                    // 文件路径直接添加
                    if (path.endsWith(".jar") || new File(path).isDirectory())
                    {
                        if (!addedPaths.contains(path))
                        {
                            if (classPath.length() > 0 && hasValidPath)
                            {
                                classPath.append(File.pathSeparator);
                            }
                            classPath.append(path);
                            addedPaths.add(path);
                            hasValidPath = true;
                            validCount++;
                            log.info("          ✓ 已添加文件路径: {}", path);
                        }
                        else
                        {
                            duplicateCount++;
                            log.info("          ✗ 重复路径，跳过: {}", path);
                        }
                    }
                    else
                    {
                        log.info("          ✗ 跳过非jar非目录文件: {}", path);
                    }
                }
                else if ("jar".equals(protocol))
                {
                    // 处理jar:file://协议
                    if (path.startsWith("file:") && path.contains("!"))
                    {
                        String jarPath = path.substring(5, path.indexOf("!"));
                        if (!addedPaths.contains(jarPath))
                        {
                            if (classPath.length() > 0 && hasValidPath)
                            {
                                classPath.append(File.pathSeparator);
                            }
                            classPath.append(jarPath);
                            addedPaths.add(jarPath);
                            hasValidPath = true;
                            validCount++;
                            log.info("          ✓ 已添加JAR路径: {}", jarPath);
                        }
                        else
                        {
                            duplicateCount++;
                            log.info("          ✗ 重复JAR路径，跳过: {}", jarPath);
                        }
                    }
                    else
                    {
                        log.info("          ✗ 跳过格式错误的jar URL: {}", path);
                    }
                }
                else
                {
                    log.info("          ✗ 跳过不支持的协议: {}", protocol);
                }
            }
            catch (Exception e)
            {
                log.info("        - URL[{}]处理异常: {}", i, e.getMessage());
                // 忽略单个URL的处理异常
            }
        }
        
        log.info("        - URL处理完成: 有效={}, 重复={}, 总计={}", validCount, duplicateCount, urls.length);
        return hasValidPath;
    }

    /**
     * 构建标准类路径
     */
    private void buildStandardClassPath(StringBuilder classPath)
    {
        log.info("  - 构建标准类路径:");
        // 获取系统类路径
        String systemClassPath = System.getProperty("java.class.path");
        log.info("    - 系统类路径长度: {}", (systemClassPath != null ? systemClassPath.length() : 0));
        
        if (systemClassPath != null && !systemClassPath.isEmpty())
        {
            classPath.append(systemClassPath);
            log.info("    - 已添加系统类路径");
        }
        else
        {
            log.info("    - 系统类路径为空");
        }
    }
}