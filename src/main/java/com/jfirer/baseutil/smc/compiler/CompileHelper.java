package com.jfirer.baseutil.smc.compiler;

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
}