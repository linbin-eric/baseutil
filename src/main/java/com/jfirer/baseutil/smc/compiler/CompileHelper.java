package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.compiler.ecj.JDTCompiler;
import com.jfirer.baseutil.smc.compiler.jdk.JDKCompiler;
import com.jfirer.baseutil.smc.compiler.springboot.SpringBootCompiler2;
import com.jfirer.baseutil.smc.model.ClassModel;

import javax.tools.ToolProvider;
import java.io.IOException;
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
            if (isSpringBootEnvironment())
            {
                System.out.println("[CompileHelper] 检测到Spring Boot环境，使用SpringBootCompiler");
                this.compiler = new SpringBootCompiler2(classLoader);
            }
            else if (ToolProvider.getSystemJavaCompiler() != null)
            {
                System.out.println("[CompileHelper] 检测到标准JDK环境，使用JDKCompiler");
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
                this.compiler = new JDTCompiler();
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

    public static  boolean isSpringBootEnvironment()
    {
        return Thread.currentThread().getContextClassLoader().getClass().getName().contains("springframework");
    }
}