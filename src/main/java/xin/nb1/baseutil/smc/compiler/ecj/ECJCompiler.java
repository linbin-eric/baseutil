package xin.nb1.baseutil.smc.compiler.ecj;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;
import xin.nb1.baseutil.IoUtil;
import xin.nb1.baseutil.RuntimeJVM;
import xin.nb1.baseutil.smc.compiler.Compiler;
import xin.nb1.baseutil.smc.model.ClassModel;

import javax.tools.JavaCompiler;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * ECJ 编译器，用来对磁盘上的源文件进行编译，需要classpath也是在磁盘上，并且只支持标准的class文件和jar文件。不支持SpringBoot的FatJar。
 * 需要配合自带的StandardJavaFileManager。
 */
@Slf4j
public class ECJCompiler implements Compiler
{
    private JavaCompiler compiler = new EclipseCompiler();
    List<String> options = Arrays.asList("-cp", System.getProperty("java.class.path"),//
//                                             "-d", outputDir.toString(),    // 输出目录
                                         "-source", "17", "-target", "17", "-nowarn", "-g");

    @Override
    public synchronized Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("ecj-compile-" + RuntimeJVM.selfPid());
        try
        {
            return compileWithTempFiles(classModel, tempDir, compiler, options);
        }
        finally
        {
            // 清理临时文件
            IoUtil.deleteDir(tempDir.toFile().getAbsolutePath());
        }
    }
}
