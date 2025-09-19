package com.jfirer.baseutil.smc.compiler.jdk;

import com.jfirer.baseutil.IoUtil;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.springboot.SpringBootClassloaderFileManager;
import com.jfirer.baseutil.smc.model.ClassModel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class FatJarDecompressCompiler implements Compiler
{
    protected final JavaCompiler          compiler;
    protected       MemoryJavaFileManager manager;

    public FatJarDecompressCompiler()
    {
        this.compiler = ToolProvider.getSystemJavaCompiler();
        if (this.compiler == null)
        {
            throw new IllegalStateException("当前处于JRE环境无法获得JavaCompiler实例。如果是在windows，可以将JDK/lib目录下的tools.jar拷贝到jre/lib目录。如果是linux，将JAVA_HOME设置为jdk的");
        }
    }

    @SneakyThrows
    @Override
    public synchronized Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {

        log.info("当前的jar是:{}",IoUtil.findJarPath());
        File jarFile = new File(IoUtil.findJarPath());
        // 创建临时目录
        Path tempDir = Files.createTempDirectory("jdk-compile");
        try
        {
            extractFatJar(jarFile.getAbsolutePath(), tempDir);
            StringBuilder classPath = new StringBuilder();
            addClasspath(classPath, tempDir);
            List<String> options = List.of(//
                                           "-cp",//
                                           classPath.toString()//
            );
            return compileWithTempFiles(classModel, tempDir, compiler, options);
        }
        finally
        {
            // 清理临时文件
            IoUtil.deleteDir(tempDir.toFile().getAbsolutePath());
        }
    }

    private void extractFatJar(String jarPath, Path tempDir) throws IOException
    {
        log.debug("[SpringBootCompiler] 开始解压FatJar: {}", jarPath);
        try (JarFile jarFile = new JarFile(jarPath))
        {
            Enumeration<JarEntry> entries        = jarFile.entries();
            int                   extractedCount = 0;
            int                   skippedCount   = 0;
            while (entries.hasMoreElements())
            {
                JarEntry entry     = entries.nextElement();
                String   entryName = entry.getName();
                // 跳过META-INF目录中的某些文件
                if (entryName.startsWith("META-INF/") && (entryName.endsWith(".SF") || entryName.endsWith(".DSA") || entryName.endsWith(".RSA")))
                {
                    skippedCount++;
                    continue;
                }
                // 创建目标文件路径
                Path destPath = tempDir.resolve(entryName);
                // 如果是目录，创建目录
                if (entry.isDirectory())
                {
                    Files.createDirectories(destPath);
                    continue;
                }
                // 确保父目录存在
                Files.createDirectories(destPath.getParent());
                // 解压文件
                try (InputStream is = jarFile.getInputStream(entry); OutputStream os = Files.newOutputStream(destPath))
                {
                    byte[] buffer = new byte[8192];
                    int    bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1)
                    {
                        os.write(buffer, 0, bytesRead);
                    }
                }
                extractedCount++;
            }
            log.trace("[SpringBootCompiler] FatJar解压完成: {}, 解压文件数: {}, 跳过文件数: {}", jarPath, extractedCount, skippedCount);
        }
        catch (Exception e)
        {
            log.error("[SpringBootCompiler] 解压FatJar失败: {}", jarPath, e);
            throw e;
        }
    }

    private void addClasspath(StringBuilder classPath, Path tempDir)
    {
        // 将临时目录添加到类路径
        if (classPath.length() > 0)
        {
            classPath.append(File.pathSeparator);
        }
        classPath.append(tempDir.toString());
        // 检查BOOT-INF/classes目录是否存在，如果存在也添加到类路径
        Path bootInfClasses = tempDir.resolve("BOOT-INF").resolve("classes");
        if (Files.exists(bootInfClasses))
        {
            classPath.append(File.pathSeparator);
            classPath.append(bootInfClasses.toString());
            log.trace("成功添加BOOT-INF/classes目录到类路径: {}", bootInfClasses);
        }
        // 检查BOOT-INF/lib目录是否存在，如果存在则添加其中的所有jar文件
        Path bootInfLib = tempDir.resolve("BOOT-INF").resolve("lib");
        if (Files.exists(bootInfLib) && Files.isDirectory(bootInfLib))
        {
            log.debug("发现BOOT-INF/lib目录，开始处理依赖库");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(bootInfLib, "*.jar"))
            {
                int libCount = 0;
                for (Path libJarPath : stream)
                {
                    if (Files.isRegularFile(libJarPath) && libJarPath.toString().endsWith(".jar"))
                    {
                        classPath.append(File.pathSeparator);
                        classPath.append(libJarPath.toString());
                        libCount++;
                        log.trace("成功添加依赖库: {}", libJarPath.getFileName());
                    }
                }
                log.trace("共添加 {} 个依赖库", libCount);
            }
            catch (Exception e)
            {
                log.warn("处理BOOT-INF/lib目录时出现异常: {}", e.getMessage());
            }
        }
    }
}
