package com.jfirer.baseutil.smc.compiler;

import com.jfirer.baseutil.smc.model.ClassModel;

import java.io.IOException;

/**
 * Interface for compiling Java source code using different compilers.
 * This allows using different compilers like JDK's compiler or Eclipse JDT.
 *
 * @author Lin Bin
 */
public interface Compiler {
    /**
     * Compile a ClassModel into a Class object.
     *
     * @param classModel the ClassModel to compile
     * @param classLoader the ClassLoader to use for loading dependencies
     * @return the compiled Class object
     * @throws IOException if an I/O error occurs during compilation
     * @throws ClassNotFoundException if a class cannot be found during compilation
     */
    Class<?> compile(ClassModel classModel, ClassLoader classLoader) throws IOException, ClassNotFoundException;
}