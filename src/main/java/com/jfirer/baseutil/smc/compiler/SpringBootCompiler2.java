package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            ClassLoader classLoader     = Thread.currentThread().getContextClassLoader();
            String      loaderClassName = classLoader.getClass().getName();
            // Spring Boot 3.x 使用 LaunchedClassLoader
            // Spring Boot 2.x 使用 LaunchedURLClassLoader
            boolean containsLaunched      = loaderClassName.contains("LaunchedURLClassLoader") || loaderClassName.contains("LaunchedClassLoader");
            boolean canLoadLegacyLauncher = false;
            boolean canLoadNewLauncher    = false;
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
        SpringBootJavaFileManager manager = new SpringBootJavaFileManager(compiler.getStandardFileManager(null, null, null), classLoader);
        try
        {
            String         source         = classModel.toStringWithLineNo();
            JavaFileObject javaFileObject = manager.makeStringSource(classModel.fileName(), source);
            StringWriter   writer         = new StringWriter();
            List<String>   options        = new ArrayList<>();
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
}