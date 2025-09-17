package com.jfirer.baseutil.smc.compiler.jdk;

import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;

/**
 * Implementation of Compiler using JDK's compiler.
 * This compiler requires a JDK environment to work.
 *
 * @author Lin Bin
 */
@Slf4j
public class JDKCompiler implements Compiler
{
    private final JavaCompiler compiler;

    public JDKCompiler()
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null)
        {
            throw new IllegalStateException("当前处于JRE环境无法获得JavaCompiler实例。如果是在windows，可以将JDK/lib目录下的tools.jar拷贝到jre/lib目录。如果是linux，将JAVA_HOME设置为jdk的");
        }
    }

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        MemoryJavaFileManager manager = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, null));
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
            return manager.getClassBytes();
        }
        finally
        {
            manager.clear();
        }
    }
}