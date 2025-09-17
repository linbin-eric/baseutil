package com.jfirer.baseutil.smc.compiler.ecj;

import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ECJ编译器实现，使用临时文件支持纯内存编译
 * ECJ编译器通过javax.tools API时需要实际文件系统支持
 */
@Slf4j
public class ECJCompiler implements Compiler
{
    private JavaCompiler compiler = new EclipseCompiler();

    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("ecj-compile");
        try
        {
            return compileWithTempFiles(classModel, tempDir);
        }
        finally
        {
            // 清理临时文件
            cleanupTempDir(tempDir);
        }
    }

    private Map<String, byte[]> compileWithTempFiles(ClassModel classModel, Path tempDir) throws IOException
    {
        String source = classModel.toStringWithLineNo();

        // 创建源文件目录结构
        String packagePath = classModel.getPackageName().replace('.', '/');
        Path packageDir = tempDir.resolve("src").resolve(packagePath);
        Files.createDirectories(packageDir);

        // 创建输出目录
        Path outputDir = tempDir.resolve("classes");
        Files.createDirectories(outputDir);

        // 写入源文件
        String fileName = classModel.className() + ".java";
        Path sourceFile = packageDir.resolve(fileName);
        Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));

        // 设置编译选项
        String classpath = System.getProperty("java.class.path");
        log.info("ecj编译的时候，classpath是:{}", classpath);
        List<String> options = Arrays.asList(
            "-cp", classpath,
            "-d", outputDir.toString(),    // 输出目录
            "-source", "17",
            "-target", "17",
            "-nowarn",
            "-g"
        );

        // 编译
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile.toFile()));

        StringWriter writer = new StringWriter();
        JavaCompiler.CompilationTask task = compiler.getTask(
            writer,
            fileManager,
            null,
            options,
            null,
            compilationUnits
        );

        Boolean result = task.call();
        fileManager.close();

        if (result == null || !result.booleanValue())
        {
            throw new RuntimeException("Compilation failed.The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
        }

        // 读取编译结果
        return readCompiledClasses(outputDir, classModel.getPackageName());
    }

    private Map<String, byte[]> readCompiledClasses(Path outputDir, String packageName) throws IOException
    {
        Map<String, byte[]> classBytes = new HashMap<>();
        String packagePath = packageName.replace('.', '/');
        Path packageDir = outputDir.resolve(packagePath);

        if (Files.exists(packageDir))
        {
            Files.walk(packageDir)
                .filter(path -> path.toString().endsWith(".class"))
                .forEach(classFile -> {
                    try
                    {
                        String relativePath = outputDir.relativize(classFile).toString();
                        String className = relativePath.replace('/', '.').replace(".class", "");
                        byte[] bytes = Files.readAllBytes(classFile);
                        classBytes.put(className, bytes);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException("Failed to read compiled class file: " + classFile, e);
                    }
                });
        }

        return classBytes;
    }

    private void cleanupTempDir(Path tempDir)
    {
        try
        {
            Files.walk(tempDir)
                .sorted((a, b) -> b.compareTo(a)) // 反向排序，先删除文件再删除目录
                .forEach(path -> {
                    try
                    {
                        Files.delete(path);
                    }
                    catch (IOException e)
                    {
                        // 忽略删除失败
                    }
                });
        }
        catch (IOException e)
        {
            // 忽略清理失败
        }
    }
}
