package com.jfirer.baseutil.smc.compiler;

/**
 * Factory for creating different compiler implementations.
 *
 * @author Lin Bin
 */
public class CompilerFactory {
    
    /**
     * Create a JDT compiler implementation.
     * This compiler works in both JDK and JRE environments.
     * 
     * @return JDT compiler implementation
     * @throws RuntimeException if JDT compiler cannot be created
     */
    public static Compiler createJDTCompiler() {
        try {
            return new JDTCompiler();
        } catch (Throwable t) {
            throw new RuntimeException("无法创建JDT编译器，请确保添加了Eclipse JDT依赖。", t);
        }
    }
    
    /**
     * Create a JDK compiler implementation.
     * This compiler requires a JDK environment to work.
     * 
     * @return JDK compiler implementation
     * @throws RuntimeException if JDK compiler cannot be created
     */
    public static Compiler createJDKCompiler() {
        return new JDKCompiler();
    }
    
    /**
     * Create a default compiler.
     * This method tries to create a JDT compiler first, and falls back to JDK compiler if JDT is not available.
     * 
     * @return a compiler implementation
     */
    public static Compiler createDefaultCompiler() {
        try {
            return createJDTCompiler();
        } catch (Throwable t) {
            return createJDKCompiler();
        }
    }
}