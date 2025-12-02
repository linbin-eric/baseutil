package xin.nb1.baseutil.smc.compiler;

import xin.nb1.baseutil.smc.compiler.jdk.MemoryJavaFileManager;
import xin.nb1.baseutil.smc.model.ClassModel;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Interface for compiling Java source code using different compilers.
 * This allows using different compilers like JDK's compiler or Eclipse JDT.
 *
 * @author Lin Bin
 */
public interface Compiler
{
    /**
     * Compile a ClassModel into a Class object.
     *
     * @param classModel the ClassModel to compile
     * @return the compiled Class object
     * @throws IOException            if an I/O error occurs during compilation
     * @throws ClassNotFoundException if a class cannot be found during compilation
     */
    Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException;

    default Map<String, byte[]> compileWithTempFiles(ClassModel classModel, Path tempDir, JavaCompiler compiler, List<String> options) throws IOException
    {
        synchronized (compiler)
        {
            String source = classModel.toStringWithLineNo();
            // 创建源文件目录结构
            String packagePath = classModel.getPackageName().replace('.', '/');
            Path   packageDir  = tempDir.resolve("src").resolve(packagePath);
            Files.createDirectories(packageDir);
            // 写入源文件
            String fileName   = classModel.className() + ".java";
            Path   sourceFile = packageDir.resolve(fileName);
            Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));
            StandardJavaFileManager            fileManager      = compiler.getStandardFileManager(null, null, StandardCharsets.UTF_8);
            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile.toFile()));
            //为了让编译后的产物可以在内存里方便获取，采用MemoryOutputJavaFileObject。
            MemoryJavaFileManager        memoryJavaFileManager = new MemoryJavaFileManager(fileManager);
            StringWriter                 writer                = new StringWriter();
            JavaCompiler.CompilationTask task                  = compiler.getTask(writer, memoryJavaFileManager, null, options, null, compilationUnits);
            fileManager.close();
            Boolean result = task.call();
            if (result == null || !result.booleanValue())
            {
                throw new RuntimeException("Compilation failed.The error is \r\n" + writer.toString() + "\r\nThe source is \r\n" + source);
            }
            Map<String, byte[]> classBytes = memoryJavaFileManager.getClassBytes();
            Map<String, byte[]> collect    = classBytes.entrySet().stream().collect(Collectors.toMap(entry -> entry.getKey().replace('/', '.'), Map.Entry::getValue));
            memoryJavaFileManager.close();
            return collect;
        }
    }
}