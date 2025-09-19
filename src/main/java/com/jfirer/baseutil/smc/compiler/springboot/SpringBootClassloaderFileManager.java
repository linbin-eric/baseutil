package com.jfirer.baseutil.smc.compiler.springboot;

import com.jfirer.baseutil.smc.compiler.MemoryInputJavaFileObject;
import com.jfirer.baseutil.smc.compiler.jdk.MemoryJavaFileManager;
import com.jfirer.baseutil.smc.compiler.MemoryOutputJavaFileObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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
 * 通过SpringBoot的loader来载入需要的依赖类，采用Javac编译器
 *
 * @author Lin Bin
 */
@Slf4j
public class SpringBootClassloaderFileManager extends MemoryJavaFileManager
{
    private final ClassLoader                                     classLoader;
    private final Map<String, byte[]>                             classBytes = new HashMap<>();
    private       ConcurrentHashMap<String, List<JavaFileObject>> cached     = new ConcurrentHashMap<>();

    public SpringBootClassloaderFileManager(JavaFileManager fileManager, ClassLoader classLoader)
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
            return new MemoryOutputJavaFileObject(className, classBytes);
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
        else if (file instanceof NestedClassJavaFileObject)
        {
            return ((NestedClassJavaFileObject) file).getClassName();
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
            if (recurse)
            {
                throw new UnsupportedOperationException();
            }
            return cached.computeIfAbsent(packageName, k -> findClassesFromLaunchedClassLoader(k));
        }
        return super.list(location, packageName, kinds, recurse);
    }

    /**
     * 从LaunchedClassLoader中查找类
     */
    private List<JavaFileObject> findClassesFromLaunchedClassLoader(String packageName)
    {
        // 尝试从类加载器获取资源
        //判断包存在的情况，就需要全面的检查。
        if (classLoader.getResource(packageName.replace('.', '/')) != null)
        {
            /**
             *  对于package，在SpringBoot的ClassLoader中，存在两种情况：
             *  1、包在BOOT-INF/classes下面
             *  2、包在BOOT-INF/lib下面的某一个lib中
             *  通过classLoader.getResources会得到多个资源，会同时包含上面的两种情况。
             *  比较好的是，每一个URL，通过((JarURLConnection) url.openConnection()).getJarFile()的调用，能得到这个资源归属的Jar。
             *  此时的jar可能是最外围的jar（搜索的资源是在BOOT-INF/classes），也可能是嵌入的lib（BOOT-INF/lib下面的某一个lib）。
             *  在通过获取jar的所有entry，就可以得到在包下面的应该有的类有多少。
             */
            return findClassesInPackage(packageName);
        }
        else
        {
            return List.of();
        }
    }

    @SneakyThrows
    private List<JavaFileObject> findClassesInPackage(String packageName)
    {
        String               resourcePath = packageName.replace('.', '/');
        Enumeration<URL>     resources    = classLoader.getResources(resourcePath);
        List<JavaFileObject> result       = new ArrayList<>();
        while (resources.hasMoreElements())
        {
            URL url = resources.nextElement();
            log.trace("[SpringBootClassloaderFileManager] 找到资源: {} -> {}", resourcePath, url);
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
                        log.trace("在包:{}下找到类:{}", packageName, substring);
                        URI            uri        = URI.create("");
                        JavaFileObject fileObject = new NestedClassJavaFileObject(entryName.substring(0, entryName.length() - 6).replace('/', '.'), entryName);
                        result.add(fileObject);
                    }
                }
            }
        }
        return result;
    }

    /**
     * 嵌套JAR文件中的Java文件对象（Spring Boot 3.x nested协议）
     */
    public class NestedClassJavaFileObject extends SimpleJavaFileObject
    {
        private final String className;
        private final String entryName;

        public NestedClassJavaFileObject(String className, String entryName)
        {
            super(URI.create(""), Kind.CLASS);
            this.className = className;
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
            return "NestedClassJavaFileObject[" + className + "]";
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
}