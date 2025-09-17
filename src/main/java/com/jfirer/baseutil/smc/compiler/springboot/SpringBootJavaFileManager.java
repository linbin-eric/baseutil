package com.jfirer.baseutil.smc.compiler.springboot;

import com.jfirer.baseutil.smc.compiler.MemoryInputJavaFileObject;
import lombok.SneakyThrows;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
            List<JavaFileObject> result = new ArrayList<>();
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
                log.debug("[SpringBootJavaFileManager] 找到包资源: {} -> {}", packageName, packageUrl);
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
                throw new UnsupportedOperationException("不支持递归查询");
            }
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 从LaunchedClassLoader查找类失败: {}", e.getMessage());
        }
        return result;
    }
private ConcurrentHashMap<String,List<JavaFileObject>> cached = new ConcurrentHashMap<>();
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
            // 检查是否是nested协议（包括jar:nested:格式）
            String protocol = jarUrl.getProtocol();
            String urlStr   = jarUrl.toString();
            log.info("[SpringBootJavaFileManager] 检查协议: protocol={}, url={}", protocol, urlStr);
            result= cached.computeIfAbsent(packageName, k->findClasses2(k));
        }
        catch (Exception e)
        {
            log.warn("[SpringBootJavaFileManager] 处理JAR文件失败: {}", e.getMessage());
            log.warn("[SpringBootJavaFileManager] 异常类型: {}", e.getClass().getName());
            log.warn("[SpringBootJavaFileManager] 异常堆栈:", e);
        }
        log.info("[SpringBootJavaFileManager] 包:{}查找完成，找到 {} 个类",packageName, result.size());
        return result;
    }

    @SneakyThrows
    private List<JavaFileObject> findClasses2(String packageName)
    {
        String           resourcePath = packageName.replace('.', '/');
        Enumeration<URL> resources    = classLoader.getResources(resourcePath);
        List<JavaFileObject>                result    = new ArrayList<>();
        while (resources.hasMoreElements())
        {
            URL url = resources.nextElement();
            log.debug("[SpringBootJavaFileManager] 找到资源: {} -> {}", resourcePath, url);
            if (url.getProtocol().equals("jar"))
            {
                JarFile               jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    JarEntry entry     = entries.nextElement();
                    String   entryName = entry.getName();
                    log.debug("发现entry:{}",entryName);
                    if (entryName.startsWith(resourcePath+"/")==false)
                    {
                        continue;
                    }
                    String   substring = entryName.substring(resourcePath.length()+1);
                    if (substring.length() > 6 && entryName.endsWith(".class"))
                    {
                        if (substring.contains("/") == false)
                        {
                            log.debug("在包:{}下找到类:{}", packageName, substring);
                            java.net.URI   uri        = java.net.URI.create("");
                            JavaFileObject fileObject = new NestedJarJavaFileObject(entryName.substring(0, entryName.length()-6).replace('/','.'), uri, entryName);
                            result.add(fileObject);
                        }
                    }
                }
//                ClassScanner.findClassesInJar(url, packageName);
            }
        }
        return result;
//
//        List<String> classesInPackage = ClassScanner.getClassesInPackage(packageName);
//        log.info("在包:{}下面发现类:{}",packageName,classesInPackage.size());
//        for (String s : classesInPackage)
//        {
//            String         simpleUri  = "nested:///BOOT-INF/lib/" + s;
//            java.net.URI   uri        = java.net.URI.create(simpleUri);
//            JavaFileObject fileObject = new NestedJarJavaFileObject(s, uri, s.replace('.','/')+".class");
//            result.add(fileObject);
//        }
//        return result;
    }




    public static class ClassScanner
    {
        public static List<String> getClassesInPackage(String packageName) throws IOException, ClassNotFoundException
        {
            List<String> classes     = new ArrayList<>();
            ClassLoader    classLoader = Thread.currentThread().getContextClassLoader();
            String         path        = packageName.replace('.', '/');
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
            List<String>        classes       = new ArrayList<>();
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

        NestedJarJavaFileObject(String className, java.net.URL nestedUrl, String entryName)
        {
            this(className, java.net.URI.create(""), entryName);
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