package com.jfirer.baseutil;

import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.SpringBootCompiler2;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * SpringBootCompiler2测试类
 * 测试在Spring Boot环境中使用LaunchedClassLoader直接加载fat JAR的功能
 *
 * @author Lin Bin
 */
@RunWith(Parameterized.class)
public class SpringBootCompiler2Test
{
    private final Compiler compiler;
    private final String compilerName;

    public SpringBootCompiler2Test(Compiler compiler, String compilerName)
    {
        this.compiler = compiler;
        this.compilerName = compilerName;
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
            {new SpringBootCompiler2(), "SpringBootCompiler2"}
        });
    }

    @Test
    public void testBasicCompilation() throws Exception
    {
        System.out.println("Testing basic compilation with " + compilerName);
        
        // 创建简单的测试类
        ClassModel classModel = new ClassModel("TestClass");
        
        // 添加简单的方法
        MethodModel methodModel = new MethodModel(classModel);
        methodModel.setMethodName("sayHello");
        methodModel.setReturnType(String.class);
        methodModel.setParamterTypes();
        methodModel.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        methodModel.setBody("return \"Hello from SpringBootCompiler2!\";");
        classModel.putMethodModel(methodModel);
        
        // 编译类
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        Class<?> compiledClass = compilerHelper.compile(classModel);
        
        // 创建实例并测试
        Object instance = compiledClass.newInstance();
        java.lang.reflect.Method method = compiledClass.getMethod("sayHello");
        String result = (String) method.invoke(instance);
        
        Assert.assertEquals("Hello from SpringBootCompiler2!", result);
        System.out.println("✓ 基本编译测试通过: " + result);
    }

    @Test
    public void testInterfaceImplementation() throws Exception
    {
        System.out.println("Testing interface implementation with " + compilerName);
        
        // 创建实现Ptest接口的类
        ClassModel classModel = new ClassModel("TestImpl");
        classModel.addInterface(Ptest.class);
        
        // 实现接口方法
        MethodModel methodModel = new MethodModel(Ptest.class.getMethod("sayHello", String.class), classModel);
        methodModel.setParamterNames(new String[]{"name"});
        methodModel.setBody("return \"Hello, \" + name + \" from SpringBootCompiler2!\";");
        classModel.putMethodModel(methodModel);
        
        // 编译类
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        Class<?> compiledClass = compilerHelper.compile(classModel);
        
        // 创建实例并测试
        Ptest instance = (Ptest) compiledClass.newInstance();
        String result = instance.sayHello("World");
        
        Assert.assertEquals("Hello, World from SpringBootCompiler2!", result);
        System.out.println("✓ 接口实现测试通过: " + result);
    }

    @Test
    public void testComplexClass() throws Exception
    {
        System.out.println("Testing complex class with " + compilerName);
        
        // 创建复杂类，使用现有的测试接口
        ClassModel classModel = new ClassModel("ComplexTestClass");
        classModel.addInterface(CompilerTest.InterfaceA.class);
        classModel.addInterface(CompilerTest.InterfaceB.class);
        
        // 添加字段
        com.jfirer.baseutil.smc.model.FieldModel fieldModel = 
            new com.jfirer.baseutil.smc.model.FieldModel("message", String.class, classModel);
        classModel.addField(fieldModel);
        
        // 添加构造器
        com.jfirer.baseutil.smc.model.ConstructorModel constructorModel = 
            new com.jfirer.baseutil.smc.model.ConstructorModel(classModel);
        constructorModel.setParamTypes(String.class);
        constructorModel.setParamNames("msg");
        constructorModel.setBody("this.message = msg;");
        classModel.addConstructor(constructorModel);
        
        // 实现接口A的方法
        MethodModel methodA = new MethodModel(
            CompilerTest.InterfaceA.class.getMethod("methodA"),
            classModel
        );
        methodA.setBody("return \"A: \" + message;");
        classModel.putMethodModel(methodA);
        
        // 实现接口B的方法
        MethodModel methodB = new MethodModel(
            CompilerTest.InterfaceB.class.getMethod("methodB"),
            classModel
        );
        methodB.setBody("return \"B: \" + message;");
        classModel.putMethodModel(methodB);
        
        // 添加额外方法
        MethodModel extraMethod = new MethodModel(classModel);
        extraMethod.setMethodName("getCombinedMessage");
        extraMethod.setReturnType(String.class);
        extraMethod.setParamterTypes();
        extraMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        extraMethod.setBody("return methodA() + \" | \" + methodB();");
        classModel.putMethodModel(extraMethod);
        
        // 编译类
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        Class<?> compiledClass = compilerHelper.compile(classModel);
        
        // 创建实例并测试
        java.lang.reflect.Constructor<?> constructor = compiledClass.getConstructor(String.class);
        Object instance = constructor.newInstance("Test Message");
        
        // 测试接口A
        CompilerTest.InterfaceA a = (CompilerTest.InterfaceA) instance;
        Assert.assertEquals("A: Test Message", a.methodA());
        
        // 测试接口B
        CompilerTest.InterfaceB b = (CompilerTest.InterfaceB) instance;
        Assert.assertEquals("B: Test Message", b.methodB());
        
        // 测试组合方法
        java.lang.reflect.Method combinedMethod = compiledClass.getMethod("getCombinedMessage");
        String combinedResult = (String) combinedMethod.invoke(instance);
        Assert.assertEquals("A: Test Message | B: Test Message", combinedResult);
        
        System.out.println("✓ 复杂类测试通过: " + combinedResult);
    }

    @Test
    public void testEnvironmentDetection() throws Exception
    {
        System.out.println("Testing Spring Boot environment detection with " + compilerName);
        
        // 测试环境检测
        boolean isSpringBoot = SpringBootCompiler2.isSpringBootEnvironment();
        
        System.out.println("✓ 环境检测结果: " + isSpringBoot);
        
        // 如果当前是Spring Boot环境，应该能检测到
        // 如果不是Spring Boot环境，也应该是正常的
        System.out.println("当前运行环境: " + (isSpringBoot ? "Spring Boot" : "标准Java"));
    }

    @Test
    public void testPerformanceComparison() throws Exception
    {
        System.out.println("Testing performance with " + compilerName);
        
        // 创建测试类
        ClassModel classModel = new ClassModel("PerformanceTest");
        MethodModel methodModel = new MethodModel(classModel);
        methodModel.setMethodName("testMethod");
        methodModel.setReturnType(int.class);
        methodModel.setParamterTypes();
        methodModel.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        methodModel.setBody("return 42;");
        classModel.putMethodModel(methodModel);
        
        // 预热
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        for (int i = 0; i < 5; i++)
        {
            compilerHelper.compile(classModel);
        }
        
        // 性能测试
        long startTime = System.currentTimeMillis();
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++)
        {
            Class<?> compiledClass = compilerHelper.compile(classModel);
            Object instance = compiledClass.newInstance();
            java.lang.reflect.Method method = compiledClass.getMethod("testMethod");
            Integer result = (Integer) method.invoke(instance);
            Assert.assertEquals(Integer.valueOf(42), result);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = (double) totalTime / iterations;
        
        System.out.println("✓ 性能测试完成:");
        System.out.println("  - 迭代次数: " + iterations);
        System.out.println("  - 总耗时: " + totalTime + "ms");
        System.out.println("  - 平均每次编译: " + String.format("%.2f", avgTime) + "ms");
    }

    // 简单的测试接口
    public interface Ptest
    {
        String sayHello(String arg);
    }
}