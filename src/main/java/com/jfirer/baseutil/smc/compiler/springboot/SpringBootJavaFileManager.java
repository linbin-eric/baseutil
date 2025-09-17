package com.jfirer.baseutil.smc.compiler.springboot;

import com.jfirer.baseutil.smc.compiler.MemoryInputJavaFileObject;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import javax.tools.JavaFileObject.Kind;
import java.io.*;
import java.net.JarURLConnection;
import java.net.URI;
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
//            Enumeration<URL>     resources       = Thread.currentThread().getContextClassLoader().getResources(SpringBootJavaFileManager.class.getName().replace('.','/')+".class");
//            URL                  url             = resources.nextElement();
//            JarFile              jarFile         = ((JarURLConnection) url.openConnection()).getJarFile();
//            log.info("准备开始找东西:{},url是:{}",jarFile,url);
//            Enumeration<JarEntry> entries        = jarFile.entries();
//            while (entries.hasMoreElements())
//            {
//                JarEntry entry     = entries.nextElement();
//                String entryName = entry.getName();
//                if (entryName.endsWith(".class"))
//                {
//                    log.info("发现业务class文件:{}",entry);
//                }
//                else if(entryName.startsWith("BOOT-INF/lib/") && entryName.endsWith(".jar")){
//                    // 处理BOOT-INF/lib下的jar依赖
//                    log.info("发现BOOT-INF/lib下的jar：{}", entryName);
//                    try {
//                        URL resource = Thread.currentThread().getContextClassLoader().getResource(entryName);
//                        log.info("发现一个jar，准备拆开:{}",resource);
//                        if (resource != null) {
//                            JarFile nestedJar = ((JarURLConnection) resource.openConnection()).getJarFile();
//                            Enumeration<JarEntry> entries1 = nestedJar.entries();
//                            while (entries1.hasMoreElements())
//                            {
//                                JarEntry entry1 = entries1.nextElement();
//                                if (entry1.getName().endsWith(".class"))
//                                {
//                                    log.info("发现嵌套lib:{}的类:{}",resource,entry1);
//                                }
//                            }
//                        } else {
//                            log.warn("无法获取嵌套jar资源: {}", entryName);
//                        }
//                    } catch (Exception e) {
//                        log.warn("处理嵌套jar时出错: {} - {}", entryName, e.getMessage());
//                    }
//                }
//                else{
//                    log.info("发现不识别的:{}",entry.getRealName());
//                }
//            }
//            throw new RuntimeException();
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
        List<JavaFileObject> result       = new ArrayList<>();
        String               resourcePath = packageName.replace('.', '/');
        // 尝试从类加载器获取资源
        URL packageUrl = classLoader.getResource(resourcePath);
        //判断包存在的情况，就需要全面的检查。
        if (packageUrl != null)
        {
            log.debug("[SpringBootJavaFileManager] 找到包资源: {} -> {}", packageName, packageUrl);
            /**
             *  对于package，在SpringBoot的ClassLoader中，存在两种情况：
             *  1、包在BOOT-INF/classes下面
             *  2、包在BOOT-INF/lib下面的某一个lib中
             *  通过classLoader.getResources会得到多个资源，会同时包含上面的两种情况。
             *  比较好的是，每一个URL，通过((JarURLConnection) url.openConnection()).getJarFile()的调用，能得到这个资源归属的Jar。
             *  此时的jar可能是最外围的jar（搜索的资源是在BOOT-INF/classes），也可能是嵌入的lib（BOOT-INF/lib下面的某一个lib）。
             *  在通过获取jar的所有entry，就可以得到在包下面的应该有的类有多少。
             */
            result.addAll(findClassesInJar(packageName));
        }
        // 如果递归查找，处理子包
        if (recurse)
        {
            throw new UnsupportedOperationException("不支持递归查询");
        }
        return result;
    }

    private ConcurrentHashMap<String, List<JavaFileObject>> cached = new ConcurrentHashMap<>();

    /**
     * 在JAR文件中查找类
     */
    private List<JavaFileObject> findClassesInJar(String packageName) throws IOException
    {
        List<JavaFileObject> result = new ArrayList<>();
//        try
//        {
        result = cached.computeIfAbsent(packageName, k -> findClasses2(k));
        return result;
//        }
//        catch (Exception e)
//        {
//            log.warn("[SpringBootJavaFileManager] 处理JAR文件失败: {}", e.getMessage());
//            log.warn("[SpringBootJavaFileManager] 异常类型: {}", e.getClass().getName());
//            log.warn("[SpringBootJavaFileManager] 异常堆栈:", e);
//        }
//        log.info("[SpringBootJavaFileManager] 包:{}查找完成，找到 {} 个类", packageName, result.size());
//        return result;
    }

    @SneakyThrows
    private List<JavaFileObject> findClasses2(String packageName)
    {
        String               resourcePath = packageName.replace('.', '/');
        Enumeration<URL>     resources    = classLoader.getResources(resourcePath);
        List<JavaFileObject> result       = new ArrayList<>();
        while (resources.hasMoreElements())
        {
            URL url = resources.nextElement();
            log.trace("[SpringBootJavaFileManager] 找到资源: {} -> {}", resourcePath, url);
            JarFile jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
            log.trace("发现jar：{}", jarFile.toString());
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements())
            {
                JarEntry entry     = entries.nextElement();
                String   entryName = entry.getName();
                if (entryName.startsWith(resourcePath + "/") == false)
                {
                    continue;
                }
                String substring = entryName.substring(resourcePath.length() + 1);
                if (substring.length() > 6 && entryName.endsWith(".class"))
                {
                    if (substring.contains("/") == false)
                    {
                        log.debug("在包:{}下找到类:{}", packageName, substring);
                        URI            uri        = URI.create("");
                        JavaFileObject fileObject = new NestedJarJavaFileObject(entryName.substring(0, entryName.length() - 6).replace('/', '.'), uri, entryName);
                        result.add(fileObject);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 内存输出Java文件对象
     */
    class MemoryOutputJavaFileObject extends SimpleJavaFileObject
    {
        private final String name;

        MemoryOutputJavaFileObject(String name)
        {
            super(URI.create("string:///" + name), Kind.CLASS);
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
        private final String className;
        private final URL    nestedUrl;
        private final String entryName;

        NestedJarJavaFileObject(String className, URI uri, String entryName)
        {
            super(uri, Kind.CLASS);
            this.className = className;
            this.nestedUrl = null; // 不再需要URL，直接使用类加载器
            this.entryName = entryName;
        }

        NestedJarJavaFileObject(String className, URL nestedUrl, String entryName)
        {
            this(className, URI.create(""), entryName);
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
    private static final Logger log = LoggerFactory.getLogger(SpringBootJavaFileManager.class);
}