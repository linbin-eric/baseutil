package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory compile Java source code as String.
 *
 * @author michael
 */
public class CompileHelper
{
    private final       MemoryClassLoader     memoryClassLoader;
    private final       Compiler              compiler;
    private       MemoryJavaFileManager manager;
    public static final AtomicInteger         COMPILE_COUNTER        = new AtomicInteger();
    public static final CompileHelper         DEFAULT_COMPILE_HELPER = new CompileHelper();

    public CompileHelper()
    {
        this(Thread.currentThread().getContextClassLoader(),new JDTCompilerImpl());
    }

    public CompileHelper(ClassLoader classLoader)
    {
        this(classLoader, new JDKCompilerImpl());
    }

    public CompileHelper(ClassLoader classLoader, Compiler compiler)
    {
        this.memoryClassLoader = new MemoryClassLoader(classLoader);
        Compiler tmpCompiler;
        if (compiler == null)
        {
            // Try to use JDT compiler first as it works in JRE environment
            try {
                tmpCompiler = new JDTCompilerImpl();
            } catch (Throwable t) {
                // Fallback to JDK compiler
                tmpCompiler = new JDKCompilerImpl();
                // Initialize manager for JDK compiler
                JavaCompiler jdkCompiler = ToolProvider.getSystemJavaCompiler();
                if (jdkCompiler == null) {
                    throw new NullPointerException("当前处于JRE环境无法获得JavaCompiler实例。如果是在windows，可以将JDK/lib目录下的tools.jar拷贝到jre/lib目录。如果是linux，将JAVA_HOME设置为jdk的");
                }
                this.manager = new MemoryJavaFileManager(jdkCompiler.getStandardFileManager(null, null, null));
            }
        }
        else
        {
            tmpCompiler = compiler;
            // If using JDK compiler, initialize manager
            if (compiler instanceof JDKCompilerImpl) {
                JavaCompiler jdkCompiler = ToolProvider.getSystemJavaCompiler();
                if (jdkCompiler != null) {
                    this.manager = new MemoryJavaFileManager(jdkCompiler.getStandardFileManager(null, null, null));
                }
            }
        }
        this.compiler = tmpCompiler;
    }

    public synchronized Class<?> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        // If using JDK compiler with manager
        if (compiler instanceof JDKCompilerImpl && manager != null) {
            try
            {
                String          source         = classModel.toStringWithLineNo();
                JavaFileObject  javaFileObject = manager.makeStringSource(classModel.fileName(), source);
                StringWriter    writer         = new StringWriter();
                JavaCompiler    jdkCompiler    = ToolProvider.getSystemJavaCompiler();
                CompilationTask task           = jdkCompiler.getTask(writer, manager, null, null, null, Arrays.asList(javaFileObject));
                Boolean         result         = task.call();
                if (result == null || !result.booleanValue())
                {
                    throw new RuntimeException("Compilation failed.The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
                }
                memoryClassLoader.addClassBytes(manager.getClassBytes());
                return memoryClassLoader.loadClass(classModel.getPackageName() + "." + classModel.className());
            }
            finally
            {
                manager.clear();
            }
        } else {
            // Use the new compiler interface
            return compiler.compile(classModel, memoryClassLoader.getParent());
        }
    }
    
    /**
     * Create a CompileHelper that explicitly uses the JDK compiler.
     * 
     * @param classLoader the classloader to use
     * @param compiler the JDK compiler to use
     * @return a CompileHelper instance
     */
    public static CompileHelper createWithJDKCompiler(ClassLoader classLoader, JavaCompiler compiler)
    {
        // Create a wrapper that uses the JDK compiler
        return new CompileHelper(classLoader, new JDKCompilerImpl());
    }
}