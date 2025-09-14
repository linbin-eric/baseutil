package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;

import java.io.IOException;
import java.util.Map;

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
}