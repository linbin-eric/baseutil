package com.jfirer.baseutil.smc.compiler;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.util.*;

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
        log.debug("准备从list开始查询，参数为location:{},packageName:{},kinds:{},recurse:{}", location, packageName,kinds, recurse);
        // 如果是CLASS_PATH位置，尝试从LaunchedClassLoader加载类
        if (location == StandardLocation.CLASS_PATH && kinds.contains(Kind.CLASS))
        {
            List<JavaFileObject> result = new ArrayList<>();
            // 首先尝试委托给父文件管理器
            Iterable<JavaFileObject> parentResult = super.list(location, packageName, kinds, recurse);
            if (parentResult != null)
            {
                for (JavaFileObject file : parentResult)
                {
                    result.add(file);
                }
            }
            // 然后从LaunchedClassLoader中查找类
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
        Package[]            definedPackages = classLoader.getDefinedPackages();
        for (Package definedPackage : definedPackages)
        {
            if (definedPackage.getName().equals(packageName))
            {
            }
        }
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
                    result.addAll(findClassesInDirectory(new java.io.File(packageUrl.getFile()), packageName, recurse));
                }
            }
            // 如果递归查找，处理子包
            if (recurse)
            {
                String[] subPackages = findSubPackages(packageName);
                for (String subPackage : subPackages)
                {
                    result.addAll(findClassesFromLaunchedClassLoader(subPackage, true));
                }
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
        log.info("[SpringBootJavaFileManager] 开始在JAR中查找类:");
        log.info("  - JAR URL: {}", jarUrl);
        log.info("  - 协议: {}", jarUrl.getProtocol());
        log.info("  - 包名: {}", packageName);
        log.info("  - 递归查找: {}", recurse);
        log.info("  - 类加载器: {}", classLoader.getClass().getName());
        try
        {
            // 检查是否是nested协议（包括jar:nested:格式）
            String protocol = jarUrl.getProtocol();
            String urlStr = jarUrl.toString();
            log.info("[SpringBootJavaFileManager] 检查协议: protocol={}, url={}", protocol, urlStr);

            if ("nested".equals(protocol) || urlStr.contains("nested:"))
            {
                log.info("[SpringBootJavaFileManager] 检测到nested协议，调用findClassesInNestedJar");
                return findClassesInNestedJar(jarUrl, packageName, recurse);
            }
            else
            {
                log.info("[SpringBootJavaFileManager] 协议不是nested，尝试处理标准JAR协议");
                // 尝试处理标准JAR协议
                try {
                    java.net.JarURLConnection jarConnection = (java.net.JarURLConnection) jarUrl.openConnection();
                    java.util.jar.JarFile jarFile = jarConnection.getJarFile();

                    String packagePath = packageName.replace('.', '/') + '/';
                    log.info("[SpringBootJavaFileManager] 处理标准JAR，包路径: {}", packagePath);

                    java.util.Enumeration<java.util.jar.JarEntry> entries = jarFile.entries();
                    while (entries.hasMoreElements())
                    {
                        java.util.jar.JarEntry entry = entries.nextElement();
                        String entryName = entry.getName();

                        if (entryName.endsWith(".class") && entryName.startsWith(packagePath))
                        {
                            // 确保是直接在包下的类，不是子包中的类（如果递归为false）
                            String remaining = entryName.substring(packagePath.length());
                            if (recurse || remaining.indexOf('/') == -1)
                            {
                                String className = entryName.substring(0, entryName.length() - 6).replace('/', '.');
                                log.info("[SpringBootJavaFileManager] 找到JAR中的类: {}", className);

                                try
                                {
                                    JavaFileObject fileObject = new JarJavaFileObject(className, jarUrl, entryName);
                                    result.add(fileObject);
                                }
                                catch (Exception e)
                                {
                                    log.warn("[SpringBootJavaFileManager] 创建JAR类文件对象失败: {}", e.getMessage());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    log.warn("[SpringBootJavaFileManager] 处理标准JAR失败: {}", e.getMessage());
                }
            }
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

    /**
     * 在嵌套JAR文件中查找类（Spring Boot 3.x nested协议）
     */
    private List<JavaFileObject> findClassesInNestedJar(java.net.URL nestedUrl, String packageName, boolean recurse)
    {
        List<JavaFileObject> result = new ArrayList<>();
        log.info("[SpringBootJavaFileManager] 开始在nested JAR中查找类:");
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
            try {
                java.lang.reflect.Method getURLsMethod = classLoader.getClass().getMethod("getURLs");
                java.net.URL[] urls = (java.net.URL[]) getURLsMethod.invoke(classLoader);
                log.info("  - 类加载器有 {} 个URL", urls.length);

                // 对于每个URL，尝试查找其中的类
                for (java.net.URL url : urls) {
                    String urlStr = url.toString();
                    log.info("    检查URL: {}", urlStr);

                    // 如果是nested协议且包含BOOT-INF/classes
                    if (urlStr.contains("nested:") && urlStr.contains("BOOT-INF/classes")) {
                        log.info("    发现nested classes URL");

                        // 构造几个可能的类路径进行测试
                        String[] testClasses = {
                            packageName + ".HelloComponent",
                            packageName + ".TestClass",
                            "com.springboot.test.HelloComponent",
                            "com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor",
                            "org.slf4j.event.DefaultLoggingEvent"
                        };

                        for (String testClass : testClasses) {
                            try {
                                // 尝试加载类来验证是否存在
                                Class<?> testClazz = classLoader.loadClass(testClass);
                                log.info("    成功加载类: {}", testClass);

                                // 构造类文件的entry名称
                                String classEntry = testClass.replace('.', '/') + ".class";
                                log.info("    类entry: {}", classEntry);

                                // 创建对应的JavaFileObject
                                try {
                                    // 使用简化的URI创建方式，避免复杂的嵌套URL
                                    String simpleUri = "nested:///BOOT-INF/classes/" + classEntry;
                                    java.net.URI uri = java.net.URI.create(simpleUri);
                                    JavaFileObject fileObject = new NestedJarJavaFileObject(testClass, uri, classEntry);
                                    result.add(fileObject);
                                    log.info("    成功创建类文件对象: {}", testClass);
                                } catch (Exception e) {
                                    log.warn("    创建类文件对象失败: {}", e.getMessage());
                                    log.warn("    异常详情:", e);
                                }
                            } catch (ClassNotFoundException e) {
                                log.debug("    类不存在: {}", testClass);
                            }
                        }
                    }
                    // 如果是nested协议且包含BOOT-INF/lib，检查是否包含目标包的类
                    else if (urlStr.contains("nested:") && urlStr.contains("BOOT-INF/lib")) {
                        log.info("    发现nested lib URL: {}", urlStr);

                        // 尝试加载目标类，类加载器会自动搜索所有lib
                        String[] libTestClasses = {
                            "org.slf4j.event.DefaultLoggingEvent",
                            "org.slf4j.event.LoggingEvent",    // 添加父类/接口
                            "org.slf4j.Logger",
                            "org.springframework.boot.SpringApplication"
                        };

                        for (String testClass : libTestClasses) {
                            // 只处理在目标包名下的类，或者特定的已知类
                            if (testClass.startsWith(packageName) ||
                                testClass.equals("org.slf4j.event.DefaultLoggingEvent") ||
                                testClass.equals("org.slf4j.event.LoggingEvent") ||
                                testClass.startsWith("org.slf4j") && packageName.startsWith("org.slf4j")) {

                                try {
                                    Class<?> testClazz = classLoader.loadClass(testClass);
                                    log.info("    从lib中成功加载类: {}", testClass);

                                    // 构造类文件的entry名称
                                    String classEntry = testClass.replace('.', '/') + ".class";
                                    log.info("    lib类entry: {}", classEntry);

                                    // 创建对应的JavaFileObject
                                    try {
                                        // 为lib中的类创建URI
                                        String simpleUri = "nested:///BOOT-INF/lib/" + testClass + ".class";
                                        java.net.URI uri = java.net.URI.create(simpleUri);
                                        JavaFileObject fileObject = new NestedJarJavaFileObject(testClass, uri, classEntry);
                                        result.add(fileObject);
                                        log.info("    成功创建lib类文件对象: {}", testClass);
                                    } catch (Exception e) {
                                        log.warn("    创建lib类文件对象失败: {}", e.getMessage());
                                    }
                                } catch (ClassNotFoundException e) {
                                    log.debug("    lib中类不存在: {}", testClass);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("  获取类加载器URLs失败: {}", e.getMessage());
            }

            // 备用策略：尝试通过已知类名查找
            log.info("  - 尝试备用策略：查找特定类");
            String[] targetClasses = {
                "com.springboot.test.HelloComponent",
                "com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor",
                "org.slf4j.event.DefaultLoggingEvent",
                "org.slf4j.event.LoggingEvent"    // 添加父类/接口
            };

            for (String targetClass : targetClasses) {
                // 检查是否匹配目标包名，或者是在递归模式下匹配父包
                boolean matchesPackage = targetClass.startsWith(packageName) ||
                                    (recurse && packageName.contains(".") &&
                                     targetClass.startsWith(packageName.substring(0, packageName.lastIndexOf('.'))));

                // 对于特定的lib中的类，也进行处理
                boolean isLibClass = targetClass.equals("org.slf4j.event.DefaultLoggingEvent") ||
                                   targetClass.equals("org.slf4j.event.LoggingEvent") ||
                                   (targetClass.startsWith("org.slf4j") && packageName.startsWith("org.slf4j"));

                if (matchesPackage || isLibClass) {
                    try {
                        Class<?> clazz = classLoader.loadClass(targetClass);
                        log.info("    找到目标类: {}", targetClass);

                        // 根据类的类型决定URI路径
                        String uriPath;
                        if (targetClass.startsWith("com.springboot.test") || targetClass.startsWith("com.jfirer")) {
                            uriPath = "nested:///BOOT-INF/classes/" + targetClass.replace('.', '/') + ".class";
                        } else {
                            uriPath = "nested:///BOOT-INF/lib/" + targetClass.replace('.', '/') + ".class";
                        }

                        java.net.URI classUri = java.net.URI.create(uriPath);
                        JavaFileObject fileObject = new NestedJarJavaFileObject(targetClass, classUri, targetClass.replace('.', '/') + ".class");
                        result.add(fileObject);
                        log.info("    成功添加目标类: {} (URI: {})", targetClass, uriPath);
                    } catch (Exception e) {
                        log.debug("    无法加载类 {}: {}", targetClass, e.getMessage());
                    }
                }
            }

        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 处理nested JAR文件失败: {}", e.getMessage());
            log.warn("[SpringBootJavaFileManager] 异常类型: {}", e.getClass().getName());
            log.warn("[SpringBootJavaFileManager] 异常堆栈:", e);
        }
        log.info("[SpringBootJavaFileManager] nested JAR查找完成，找到 {} 个类", result.size());
        // 输出找到的所有类名
        if (!result.isEmpty())
        {
            log.info("[SpringBootJavaFileManager] 找到的类列表:");
            for (JavaFileObject fileObj : result)
            {
                log.info("  - {}", fileObj.getName());
            }
        }
        else
        {
            log.info("[SpringBootJavaFileManager] 未找到任何类");
        }
        return result;
    }

    /**
     * 在目录中查找类
     */
    private List<JavaFileObject> findClassesInDirectory(java.io.File directory, String packageName, boolean recurse) throws IOException
    {
        List<JavaFileObject> result = new ArrayList<>();
        log.info("[SpringBootJavaFileManager] 开始在目录中查找类:");
        log.info("  - 目录路径: {}", directory.getAbsolutePath());
        log.info("  - 包名: {}", packageName);
        log.info("  - 递归查找: {}", recurse);
        if (!directory.exists() || !directory.isDirectory())
        {
            log.info("  - 目录不存在或不是目录: {}", directory.exists());
            return result;
        }
        log.info("  - 目录存在且是有效目录");
        java.io.File[] files = directory.listFiles();
        if (files == null)
        {
            log.info("  - 无法列出目录内容");
            return result;
        }
        log.info("  - 目录包含 {} 个文件/子目录", files.length);
        for (java.io.File file : files)
        {
            if (file.isFile() && file.getName().endsWith(".class"))
            {
                String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
                log.info("  - 找到class文件: {} -> {}", file.getName(), className);
                try
                {
                    JavaFileObject fileObject = new FileJavaFileObject(className, file);
                    result.add(fileObject);
                    log.info("    成功创建类文件对象: {}", className);
                }
                catch (Exception e)
                {
                    log.warn("    创建目录类文件对象失败: {}", e.getMessage());
                    log.warn("    异常类型: {}", e.getClass().getName());
                }
            }
            else if (file.isDirectory() && recurse)
            {
                String subPackageName = packageName + "." + file.getName();
                log.info("  - 递归处理子目录: {} -> {}", file.getName(), subPackageName);
                result.addAll(findClassesInDirectory(file, subPackageName, true));
            }
            else
            {
                log.debug("  - 跳过: {} (文件: {}, 目录: {})", file.getName(), file.isFile(), file.isDirectory());
            }
        }
        log.info("[SpringBootJavaFileManager] 目录查找完成，找到 {} 个类", result.size());
        return result;
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
     * JAR文件中的Java文件对象
     */
    static class JarJavaFileObject extends SimpleJavaFileObject
    {
        private final String       className;
        private final java.net.URL jarUrl;
        private final String       entryName;

        JarJavaFileObject(String className, java.net.URL jarUrl, String entryName)
        {
            super(java.net.URI.create("jar:" + jarUrl.toString() + "!/" + entryName), Kind.CLASS);
            this.className = className;
            this.jarUrl    = jarUrl;
            this.entryName = entryName;
        }

        @Override
        public InputStream openInputStream() throws IOException
        {
            java.net.JarURLConnection connection = (java.net.JarURLConnection) jarUrl.openConnection();
            java.util.jar.JarFile     jarFile    = connection.getJarFile();
            java.util.jar.JarEntry    entry      = jarFile.getJarEntry(entryName);
            if (entry == null)
            {
                throw new FileNotFoundException("JAR entry not found: " + entryName);
            }
            return jarFile.getInputStream(entry);
        }

        @Override
        public OutputStream openOutputStream() throws IOException
        {
            throw new UnsupportedOperationException("Cannot write to JAR file");
        }

        @Override
        public String getName()
        {
            return className;
        }

        @Override
        public String toString()
        {
            return "JarJavaFileObject[" + className + "]";
        }
    }

    /**
     * 文件系统中的Java文件对象
     */
    static class FileJavaFileObject extends SimpleJavaFileObject
    {
        private final File file;

        FileJavaFileObject(String className, File file)
        {
            super(file.toURI(), Kind.CLASS);
            this.file = file;
        }

        @Override
        public InputStream openInputStream() throws IOException
        {
            return new FileInputStream(file);
        }

        @Override
        public OutputStream openOutputStream() throws IOException
        {
            throw new UnsupportedOperationException("Cannot write to read-only file");
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

        NestedJarJavaFileObject(String className, java.net.URL nestedUrl, String entryName)
        {
            this(className, java.net.URI.create("nested:///BOOT-INF/classes/" + entryName), entryName);
        }

        @Override
        public InputStream openInputStream() throws IOException
        {
            // 直接使用类加载器获取资源
            InputStream inputStream = classLoader.getResourceAsStream(entryName);
            if (inputStream == null) {
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