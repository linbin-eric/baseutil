package com.jfirer.baseutil.smc.compiler.ecj;

import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.jdk.MemoryJavaFileManager;
import com.jfirer.baseutil.smc.model.ClassModel;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;

public class ECJCompiler implements Compiler
{
    private JavaCompiler          compiler = new EclipseCompiler();
    private MemoryJavaFileManager manager  = new MemoryJavaFileManager(compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8));

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        try
        {
            String                       source         = classModel.toStringWithLineNo();
            JavaFileObject               javaFileObject = manager.makeStringSource(classModel.fileName(), source);
            StringWriter                 writer         = new StringWriter();
            JavaCompiler.CompilationTask task           = compiler.getTask(writer, manager, null, null, null, Arrays.asList(javaFileObject));
            Boolean                      result         = task.call();
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
