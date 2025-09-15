package com.jfirer.baseutil;

import com.jfirer.baseutil.smc.compiler.*;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;

/**
 * 编译器性能对比测试
 * 比较 SpringBootCompiler 和 SpringBootCompiler2 的性能差异
 *
 * @author Lin Bin
 */
@RunWith(Parameterized.class)
public class CompilerComparisonTest
{
    private final Compiler compiler;
    private final String compilerName;

    public CompilerComparisonTest(Compiler compiler, String compilerName)
    {
        this.compiler = compiler;
        this.compilerName = compilerName;
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> data()
    {
        return Arrays.asList(new Object[][]{
            {new SpringBootCompiler(), "SpringBootCompiler (Original)"},
            {new SpringBootCompiler2(), "SpringBootCompiler2 (New)"}
        });
    }

    @Test
    public void testPerformanceComparison() throws Exception
    {
        System.out.println("\n=== " + compilerName + " 性能测试 ===");
        
        // 创建测试类
        ClassModel classModel = createTestClass();
        
        // 预热 - 编译 5 次以消除 JIT 编译影响
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        for (int i = 0; i < 5; i++)
        {
            compilerHelper.compile(classModel);
        }
        
        // 正式测试 - 编译 20 次
        long startTime = System.nanoTime();
        int iterations = 20;
        
        for (int i = 0; i < iterations; i++)
        {
            Class<?> compiledClass = compilerHelper.compile(classModel);
            
            // 验证编译结果
            Object instance = compiledClass.newInstance();
            java.lang.reflect.Method method = compiledClass.getMethod("processData", String.class);
            String result = (String) method.invoke(instance, "test");
            
            if (!"Processed: test".equals(result))
            {
                throw new RuntimeException("编译结果验证失败");
            }
        }
        
        long endTime = System.nanoTime();
        long totalTimeNanos = endTime - startTime;
        double totalTimeMs = totalTimeNanos / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        System.out.println("编译器: " + compilerName);
        System.out.println("迭代次数: " + iterations);
        System.out.println("总耗时: " + String.format("%.2f", totalTimeMs) + "ms");
        System.out.println("平均每次编译: " + String.format("%.2f", avgTimeMs) + "ms");
        System.out.println("编译速度: " + String.format("%.1f", iterations / (totalTimeMs / 1000)) + " 次/秒");
        
        // 内存使用估算
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // 建议垃圾回收
        Thread.sleep(100); // 给 GC 一点时间
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("内存使用: " + (memoryUsed / 1024 / 1024) + " MB");
        System.out.println("=" + "=".repeat(50));
    }

    @Test
    public void testComplexClassPerformance() throws Exception
    {
        System.out.println("\n=== " + compilerName + " 复杂类性能测试 ===");
        
        // 创建复杂测试类
        ClassModel classModel = createComplexTestClass();
        
        // 预热
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        for (int i = 0; i < 3; i++)
        {
            compilerHelper.compile(classModel);
        }
        
        // 正式测试 - 编译 10 次
        long startTime = System.nanoTime();
        int iterations = 10;
        
        for (int i = 0; i < iterations; i++)
        {
            Class<?> compiledClass = compilerHelper.compile(classModel);
            
            // 验证编译结果
            Object instance = compiledClass.newInstance();
            
            // 测试接口方法
            DataProcessor processor = (DataProcessor) instance;
            String result = processor.process("test data");
            
            if (!"Processed: test data".equals(result))
            {
                throw new RuntimeException("复杂类编译结果验证失败");
            }
        }
        
        long endTime = System.nanoTime();
        long totalTimeNanos = endTime - startTime;
        double totalTimeMs = totalTimeNanos / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        System.out.println("编译器: " + compilerName);
        System.out.println("复杂类迭代次数: " + iterations);
        System.out.println("总耗时: " + String.format("%.2f", totalTimeMs) + "ms");
        System.out.println("平均每次编译: " + String.format("%.2f", avgTimeMs) + "ms");
        System.out.println("=" + "=".repeat(50));
    }

    @Test
    public void testMemoryUsage() throws Exception
    {
        System.out.println("\n=== " + compilerName + " 内存使用测试 ===");
        
        // 强制垃圾回收
        System.gc();
        Thread.sleep(200);
        
        Runtime runtime = Runtime.getRuntime();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        
        // 创建编译器并编译多个类
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        
        for (int i = 0; i < 10; i++)
        {
            ClassModel classModel = createTestClass("MemoryTest" + i);
            compilerHelper.compile(classModel);
        }
        
        // 再次强制垃圾回收
        System.gc();
        Thread.sleep(200);
        
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = memoryAfter - memoryBefore;
        
        System.out.println("编译器: " + compilerName);
        System.out.println("编译类数量: 10");
        System.out.println("内存使用: " + (memoryUsed / 1024) + " KB");
        System.out.println("=" + "=".repeat(50));
    }

    private ClassModel createTestClass()
    {
        return createTestClass("PerformanceTest");
    }

    private ClassModel createTestClass(String className)
    {
        ClassModel classModel = new ClassModel(className);
        
        // 添加简单的方法
        MethodModel method = new MethodModel(classModel);
        method.setMethodName("processData");
        method.setReturnType(String.class);
        method.setParamterTypes(String.class);
        method.setParamterNames("input");
        method.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        method.setBody("return \"Processed: \" + input;");
        classModel.putMethodModel(method);
        
        return classModel;
    }

    private ClassModel createComplexTestClass()
    {
        ClassModel classModel = new ClassModel("ComplexPerformanceTest");
        classModel.addInterface(DataProcessor.class);
        
        // 添加多个字段
        com.jfirer.baseutil.smc.model.FieldModel field1 = 
            new com.jfirer.baseutil.smc.model.FieldModel("data", String.class, classModel);
        classModel.addField(field1);
        
        com.jfirer.baseutil.smc.model.FieldModel field2 = 
            new com.jfirer.baseutil.smc.model.FieldModel("counter", int.class, "0", classModel);
        classModel.addField(field2);
        
        // 添加构造器
        com.jfirer.baseutil.smc.model.ConstructorModel constructor = 
            new com.jfirer.baseutil.smc.model.ConstructorModel(classModel);
        constructor.setParamTypes(String.class);
        constructor.setParamNames("initialData");
        constructor.setBody("this.data = initialData;");
        classModel.addConstructor(constructor);
        
        // 实现接口方法
        try {
            MethodModel interfaceMethod = new MethodModel(
                DataProcessor.class.getMethod("process", String.class),
                classModel
            );
            interfaceMethod.setParamterNames(new String[]{"input"});
            interfaceMethod.setBody("counter++; return \"Processed: \" + input;");
            classModel.putMethodModel(interfaceMethod);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("找不到接口方法", e);
        }
        
        // 添加额外方法
        MethodModel extraMethod = new MethodModel(classModel);
        extraMethod.setMethodName("getCounter");
        extraMethod.setReturnType(int.class);
        extraMethod.setParamterTypes();
        extraMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        extraMethod.setBody("return counter;");
        classModel.putMethodModel(extraMethod);
        
        return classModel;
    }

    // 测试接口
    public interface DataProcessor
    {
        String process(String input);
    }
}