package com.jfirer.baseutil.smc.compiler.ecj;

import com.jfirer.baseutil.smc.model.ClassModel;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.IOException;
import java.util.*;

/**
 * Implementation of Compiler using Eclipse JDT Core.
 * This compiler can work in JRE environment, unlike the JDK compiler.
 *
 * @author Lin Bin
 */
public class JDTCompiler implements com.jfirer.baseutil.smc.compiler.Compiler
{
    @Override
    public Map<String, byte[]> compile(ClassModel classModel) throws IOException, ClassNotFoundException
    {
        // Convert ClassModel to source code
        String sourceCode = classModel.toStringWithLineNo();
        String fileName   = classModel.fileName();
        String className  = classModel.getPackageName() + "." + classModel.className();
        // Set up compiler options
        Map<String, String> options = new HashMap<>();
        options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_17);
        options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_17);
        options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_17);
        options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
        options.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.IGNORE);
        options.put(CompilerOptions.OPTION_ReportDeadCode, CompilerOptions.IGNORE);
        // Create a classpath entries for the file system
        // Try to get the class path from the system property
        String javaHome  = System.getProperty("java.home");
        String classpath = System.getProperty("java.class.path");
        // For Java 9+, the runtime classes are in jrt:/modules
        // For older Java versions or when running in JRE, we need to add rt.jar or classes.jar
        String[] classpathEntries;
        if (classpath != null)
        {
            classpathEntries = classpath.split(System.getProperty("path.separator"));
        }
        else
        {
            classpathEntries = new String[0];
        }
        // For Java 9+, we need to add the jrt file system to access the base module
        String[] defaultLibs = new String[0];
        if (javaHome != null)
        {
            // Check if we're in Java 9+ (modular JDK)
            java.io.File jrtFsJar = new java.io.File(javaHome, "lib/jrt-fs.jar");
            if (jrtFsJar.exists())
            {
                // Java 9+ modular JDK - jrt-fs.jar should be in the boot classpath
                defaultLibs = new String[]{jrtFsJar.getAbsolutePath()};
            }
            else
            {
                // Older JDK or JRE - try to find rt.jar
                java.io.File rtJar = new java.io.File(javaHome, "lib/rt.jar");
                if (!rtJar.exists())
                {
                    rtJar = new java.io.File(javaHome, "../lib/rt.jar");
                }
                if (rtJar.exists())
                {
                    defaultLibs = new String[]{rtJar.getAbsolutePath()};
                }
            }
        }
        // Combine classpath entries with default libraries
        String[] combinedClasspath = new String[classpathEntries.length + defaultLibs.length];
        System.arraycopy(classpathEntries, 0, combinedClasspath, 0, classpathEntries.length);
        System.arraycopy(defaultLibs, 0, combinedClasspath, classpathEntries.length, defaultLibs.length);
        // Create file system with both classpath and default libraries
        FileSystem fileSystem = new FileSystem(combinedClasspath, new String[0], null);
        // Create compiler requestor to collect results
        JDTCompilerRequestor requestor = new JDTCompilerRequestor();
        // Create compiler
        Compiler compiler = new Compiler(fileSystem, DefaultErrorHandlingPolicies.proceedWithAllProblems(), options, requestor, new DefaultProblemFactory(Locale.getDefault()));
        // Create compilation unit
        ICompilationUnit compilationUnit = new CompilationUnit(sourceCode.toCharArray(), fileName, "UTF-8");
        // Compile
        compiler.compile(new ICompilationUnit[]{compilationUnit});
        // Get the compilation results
        CompilationResult[] results = requestor.getResults();
        // Check for compilation errors
        if (results == null || results.length == 0)
        {
            throw new RuntimeException("Compilation failed: no results");
        }
        // Check for compilation errors
        for (CompilationResult result : results)
        {
            if (result.hasErrors())
            {
                StringBuilder errorMsg = new StringBuilder("Compilation failed:\n");
                for (int i = 0; i < result.problemCount; i++)
                {
                    errorMsg.append(result.problems[i].toString()).append("\n");
                }
                throw new RuntimeException(errorMsg.toString());
            }
        }
        // Process results and create class bytes
        Map<String, byte[]> classBytes = new HashMap<>();
        for (CompilationResult result : results)
        {
            ClassFile[] classFiles = result.getClassFiles();
            for (ClassFile classFile : classFiles)
            {
                String classNameResult = new String(classFile.fileName());
                classBytes.put(classNameResult.replace('/', '.'), classFile.getBytes());
            }
        }
        return classBytes;
    }

    /**
     * Custom implementation of ICompilerRequestor for JDT compiler.
     */
    private static class JDTCompilerRequestor implements ICompilerRequestor
    {
        private final List<CompilationResult> results = new ArrayList<>();

        @Override
        public void acceptResult(CompilationResult result)
        {
            results.add(result);
        }

        public CompilationResult[] getResults()
        {
            return results.toArray(new CompilationResult[0]);
        }
    }
}