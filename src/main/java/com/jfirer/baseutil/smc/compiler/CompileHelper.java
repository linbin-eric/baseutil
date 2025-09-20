package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.compiler.ecj.ECJCompiler;
import com.jfirer.baseutil.smc.compiler.jdk.FatJarDecompressCompiler;
import com.jfirer.baseutil.smc.compiler.jdk.JDKCompiler;
import com.jfirer.baseutil.smc.model.ClassModel;

import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * In-memory compile Java source code as String.
 *
 * @author michael
 */
public class CompileHelper
{
    private final MemoryClassLoader memoryClassLoader;
    private final Compiler          compiler;

    public CompileHelper()
    {
        this(Thread.currentThread().getContextClassLoader(), null);
    }

    public CompileHelper(ClassLoader classLoader, Compiler compiler)
    {
        this.memoryClassLoader = new MemoryClassLoader(classLoader);
        if (compiler == null)
        {
            if (ToolProvider.getSystemJavaCompiler() != null)
            {
                this.compiler = new JDKCompiler();
            }
            else
            {
                try
                {
                    Class.forName("org.eclipse.jdt.internal.compiler.Compiler");
                }
                catch (ClassNotFoundException e)
                {
                    throw new RuntimeException("当前不是JDK环境，需要启用ECJ，检查POM是否进行了引入", e);
                }
                this.compiler = new ECJCompiler();
            }
        }
        else
        {
            this.compiler = compiler;
        }
    }

    public synchronized Class<?> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        Map<String, byte[]> compiled = compiler.compile(classModel);
        memoryClassLoader.addClassBytes(compiled);
        return memoryClassLoader.loadClass(classModel.getPackageName() + "." + classModel.className());
    }

    public static boolean isSpringBootEnvironment()
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        // 检查类加载器是否为Spring Boot的LaunchedURLClassLoader
        boolean isSpringBootClassLoader = classLoader.getClass().getName().contains("springframework");
        // 检查是否以JAR形式运行
        boolean isJarExecution = isRunningFromJar();
        return isSpringBootClassLoader && isJarExecution;
    }

    private static boolean isRunningFromJar()
    {
        try
        {
            URL resource = CompileHelper.class.getClassLoader().getResource("META-INF/MANIFEST.MF");
            return resource != null;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}