package com.jfireframework.baseutil.smc.compiler;

import com.jfireframework.baseutil.smc.model.ClassModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * In-memory compile Java source code as String.
 *
 * @author michael
 */
public class CompileHelper
{
    private static final Logger                logger = LoggerFactory.getLogger(CompileHelper.class);
    private final        MemoryClassLoader     memoryClassLoader;
    private final        JavaCompiler          compiler;
    private final        MemoryJavaFileManager manager;

    public CompileHelper()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public CompileHelper(ClassLoader classLoader)
    {
        this(classLoader, null);
    }

    public CompileHelper(ClassLoader classLoader, JavaCompiler compiler)
    {
        if (compiler == null)
        {
            compiler = ToolProvider.getSystemJavaCompiler();
            if (compiler == null)
            {
                ServiceLoader<JavaCompiler> load     = ServiceLoader.load(JavaCompiler.class, classLoader);
                Iterator<JavaCompiler>      iterator = load.iterator();
                if (iterator.hasNext())
                {
                    compiler = iterator.next();
                }
                else
                {
                    throw new NullPointerException("当前处于JRE环境并且无法以SPI的形式获得JavaCompiler实例");
                }
            }
        }
        this.compiler = compiler;
        memoryClassLoader = new MemoryClassLoader(classLoader);
        manager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
    }

    public Class<?> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        try
        {
            String          source         = classModel.toStringWithLineNo();
            JavaFileObject  javaFileObject = manager.makeStringSource(classModel.fileName(), source);
            StringWriter    writer         = new StringWriter();
            CompilationTask task           = compiler.getTask(writer, manager, null, null, null, Arrays.asList(javaFileObject));
            Boolean         result         = task.call();
            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed.The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
            }
            logger.debug("编译的源代码是:\r\n{}\r\n", source);
            memoryClassLoader.addClassBytes(manager.getClassBytes());
            return memoryClassLoader.loadClass("com.jfireframe.smc.output." + classModel.className());
        }
        finally
        {
            manager.clear();
        }
    }
}
