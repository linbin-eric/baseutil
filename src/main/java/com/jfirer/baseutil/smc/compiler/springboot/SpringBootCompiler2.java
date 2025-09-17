package com.jfirer.baseutil.smc.compiler.springboot;

import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.jdk.JDKCompiler;
import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
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
public class SpringBootCompiler2 extends JDKCompiler implements Compiler
{
    private SpringBootJavaFileManager manager;

    public SpringBootCompiler2()
    {
        this(Thread.currentThread().getContextClassLoader());
    }

    public SpringBootCompiler2(ClassLoader classLoader)
    {
        super();
        // 创建支持LaunchedClassLoader的文件管理器
        manager = new SpringBootJavaFileManager(compiler.getStandardFileManager(null, null, null), classLoader);
    }

    @Override
    public synchronized Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
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