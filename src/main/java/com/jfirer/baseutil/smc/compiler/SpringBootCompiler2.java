package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

/**
 * Spring Boot环境下的编译器实现2 - 使用LaunchedClassLoader直接加载fat JAR。
 * 无需解压文件到临时目录，直接使用Spring Boot的LaunchedClassLoader加载类。
 *
 * @author Lin Bin
 */
@Slf4j
public class SpringBootCompiler2 implements Compiler
{
    private final JavaCompiler compiler;
    private final ClassLoader  classLoader;

    public SpringBootCompiler2()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SpringBootCompiler2(ClassLoader classLoader)
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null)
        {
            throw new IllegalStateException("当前处于JRE环境无法获得JavaCompiler实例。如果是在windows，可以将JDK/lib目录下的tools.jar拷贝到jre/lib目录。如果是linux，将JAVA_HOME设置为jdk的");
        }
        this.classLoader = classLoader;
    }

    /**
     * 检查当前是否在Spring Boot环境中
     */
    public static boolean isSpringBootEnvironment()
    {
        try
        {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
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

            log.info("[SpringBootCompiler2] 环境检测详情:");
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
            log.info("[SpringBootCompiler2] 检测异常: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        // 创建支持LaunchedClassLoader的文件管理器
        SpringBootJavaFileManager manager = new SpringBootJavaFileManager(
            compiler.getStandardFileManager(null, null, null), classLoader);
        
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
            
            // 添加调试信息
            options.add("-g");
            
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
            manager.close();
        }
    }

    /**
     * 构建类路径字符串 - 直接使用LaunchedClassLoader的URLs
     */
    private String buildClassPath()
    {
        StringBuilder classPath = new StringBuilder();
        boolean isSpringBoot = isSpringBootEnvironment();
        
        log.info("[SpringBootCompiler2] 构建类路径:");
        log.info("  - 检测到Spring Boot环境: {}", isSpringBoot);
        
        if (isSpringBoot)
        {
            log.info("  - 使用LaunchedClassLoader直接构建类路径");
            buildSpringBootClassPath(classPath);
        }
        else
        {
            log.info("  - 使用标准类路径构建逻辑");
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
        
        return result;
    }

    /**
     * 构建Spring Boot环境的类路径 - 直接从LaunchedClassLoader获取URLs
     */
    private void buildSpringBootClassPath(StringBuilder classPath)
    {
        log.info("[SpringBootCompiler2] Spring Boot类路径构建开始:");
        try
        {
            // 方法1：尝试从当前类加载器获取URLs
            log.info("  - 尝试方法1：从类加载器获取URLs");
            boolean classLoaderSuccess = buildClassPathFromClassLoader(classPath);
            log.info("  - 方法1结果: {}", classLoaderSuccess);

            // 如果方法1失败，尝试方法2：从系统属性获取
            if (!classLoaderSuccess)
            {
                log.info("  - 尝试方法2：从系统属性获取类路径");
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
     * 从类加载器构建类路径
     */
    private boolean buildClassPathFromClassLoader(StringBuilder classPath)
    {
        try
        {
            ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
            String loaderClassName = currentLoader.getClass().getName();
            log.info("    - 当前类加载器类型: {}", loaderClassName);
            
            if (loaderClassName.contains("LaunchedClassLoader"))
            {
                // Spring Boot 3.x的LaunchedClassLoader处理（优先处理）
                log.info("    - 检测到LaunchedClassLoader");
                return handleLaunchedClassLoader(classPath, currentLoader);
            }
            else if (loaderClassName.contains("LaunchedURLClassLoader"))
            {
                // Spring Boot 2.x的LaunchedURLClassLoader处理
                log.info("    - 检测到LaunchedURLClassLoader");
                return handleLaunchedURLClassLoader(classPath, currentLoader);
            }
            else if (currentLoader instanceof URLClassLoader)
            {
                // 标准URLClassLoader处理
                log.info("    - 检测到标准URLClassLoader");
                URL[] urls = ((URLClassLoader) currentLoader).getURLs();
                log.info("    - URL数量: {}", (urls != null ? urls.length : 0));
                return addUrlsToClassPath(classPath, urls);
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
     * 将URLs添加到类路径 - Spring Boot 3.x优化版本
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

        // Spring Boot 3.x的LaunchedClassLoader特性：所有URLs都指向同一个fat JAR
        // 我们只需要提取fat JAR路径一次即可
        for (int i = 0; i < urls.length; i++)
        {
            URL url = urls[i];
            try
            {
                String path = url.getPath();
                String protocol = url.getProtocol();
                log.info("        - URL[{}]: {}://{}", i, protocol, path);

                if ("jar".equals(protocol) && path.startsWith("nested:"))
                {
                    // Spring Boot 3.x的复合协议: jar://nested:/path/to/fat.jar/!BOOT-INF/...
                    log.info("          - 发现Spring Boot 3.x nested协议");

                    // 提取fat JAR路径 - 格式: nested:/path/to/fat.jar/!...
                    String nestedPath = path.substring(7); // 移除"nested:"前缀
                    int separatorIndex = nestedPath.indexOf("!/");
                    if (separatorIndex > 0)
                    {
                        String fatJarPath = nestedPath.substring(0, separatorIndex);
                        File jarFile = new File(fatJarPath);

                        if (jarFile.exists() && jarFile.getName().endsWith(".jar"))
                        {
                            if (!addedPaths.contains(fatJarPath))
                            {
                                if (classPath.length() > 0 && hasValidPath)
                                {
                                    classPath.append(File.pathSeparator);
                                }
                                classPath.append(fatJarPath);
                                addedPaths.add(fatJarPath);
                                hasValidPath = true;
                                validCount++;
                                log.info("          ✓ 已添加Spring Boot fat JAR: {}", fatJarPath);
                            }
                            else
                            {
                                duplicateCount++;
                                log.info("          ✗ 重复fat JAR路径，跳过: {}", fatJarPath);
                            }
                        }
                        else
                        {
                            log.info("          ✗ fat JAR文件不存在: {}", fatJarPath);
                        }
                    }
                    else
                    {
                        log.info("          ✗ 格式错误的nested路径: {}", nestedPath);
                    }
                }
                else if ("file".equals(protocol))
                {
                    // 标准文件协议
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

        // 如果没有找到任何路径，使用系统类路径作为降级方案
        if (!hasValidPath)
        {
            log.info("        - 未找到有效路径，使用系统类路径作为降级方案");
            String systemClassPath = System.getProperty("java.class.path");
            if (systemClassPath != null && !systemClassPath.isEmpty())
            {
                if (classPath.length() > 0)
                {
                    classPath.append(File.pathSeparator);
                }
                classPath.append(systemClassPath);
                hasValidPath = true;
                log.info("          ✓ 已添加系统类路径");
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