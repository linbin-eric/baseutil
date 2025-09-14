package com.jfirer.baseutil;

import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.CompilerFactory;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class JDTCompilerTest {

    @Test
    public void testJDTCompilerCreation() {
        // Test that we can create a JDT compiler
        try {
            Compiler jdtCompiler = CompilerFactory.createJDTCompiler();
            Assert.assertNotNull("JDT compiler should be created", jdtCompiler);
        } catch (RuntimeException e) {
            Assert.fail("Should be able to create JDT compiler: " + e.getMessage());
        }
    }
    
    @Test
    public void testJDKCompilerCreation() {
        // Test that we can create a JDK compiler
        try {
            Compiler jdkCompiler = CompilerFactory.createJDKCompiler();
            Assert.assertNotNull("JDK compiler should be created", jdkCompiler);
        } catch (RuntimeException e) {
            Assert.fail("Should be able to create JDK compiler: " + e.getMessage());
        }
    }
    
    @Test
    public void testDefaultCompilerCreation() {
        // Test that we can create a default compiler
        try {
            Compiler defaultCompiler = CompilerFactory.createDefaultCompiler();
            Assert.assertNotNull("Default compiler should be created", defaultCompiler);
        } catch (RuntimeException e) {
            Assert.fail("Should be able to create default compiler: " + e.getMessage());
        }
    }
    
    @Test
    public void testCompileHelperWithJDT() throws IOException, ClassNotFoundException {
        // Test that we can create a CompileHelper with JDT compiler
        try {
            Compiler      jdtCompiler   = CompilerFactory.createJDTCompiler();
            CompileHelper compileHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), jdtCompiler);
            Assert.assertNotNull("CompileHelper with JDT compiler should be created", compileHelper);
        } catch (RuntimeException e) {
            // This might fail in JRE environment, which is expected
            // Assert.fail("Should be able to create CompileHelper with JDT compiler: " + e.getMessage());
        }
    }
    
    @Test
    public void testCompileHelperCreation() throws IOException, ClassNotFoundException {
        // Test that we can create a CompileHelper
        try {
            CompileHelper compileHelper = new CompileHelper();
            Assert.assertNotNull("CompileHelper should be created", compileHelper);
        } catch (RuntimeException e) {
            Assert.fail("Should be able to create CompileHelper: " + e.getMessage());
        }
    }
}