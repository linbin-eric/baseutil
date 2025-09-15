package com.jfirer.baseutil;

import com.jfirer.baseutil.smc.compiler.*;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;

/**
 * 简单的性能对比测试
 *
 * @author Lin Bin
 */
public class SimplePerformanceTest
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("=== SpringBootCompiler 性能对比测试 ===\n");
        
        // 测试原始编译器
        testCompiler(new SpringBootCompiler(), "SpringBootCompiler (原始)");
        
        // 测试新编译器
        testCompiler(new SpringBootCompiler2(), "SpringBootCompiler2 (新)");
        
        System.out.println("\n=== 测试完成 ===");
    }
    
    private static void testCompiler(Compiler compiler, String name) throws Exception
    {
        System.out.println("\n--- " + name + " ---");
        
        // 创建测试类
        ClassModel classModel = createTestClass();
        
        // 预热 - 编译 3 次
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
            java.lang.reflect.Method method = compiledClass.getMethod("sayHello", String.class);
            String result = (String) method.invoke(instance, "World");
            
            if (!"Hello, World!".equals(result))
            {
                throw new RuntimeException("编译结果验证失败");
            }
        }
        
        long endTime = System.nanoTime();
        long totalTimeNanos = endTime - startTime;
        double totalTimeMs = totalTimeNanos / 1_000_000.0;
        double avgTimeMs = totalTimeMs / iterations;
        
        System.out.println("迭代次数: " + iterations);
        System.out.println("总耗时: " + String.format("%.2f", totalTimeMs) + "ms");
        System.out.println("平均每次编译: " + String.format("%.2f", avgTimeMs) + "ms");
        System.out.println("编译速度: " + String.format("%.1f", iterations / (totalTimeMs / 1000)) + " 次/秒");
        
        // 内存使用估算
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        Thread.sleep(100);
        long memoryUsed = runtime.totalMemory() - runtime.freeMemory();
        System.out.println("内存使用: " + (memoryUsed / 1024 / 1024) + " MB");
    }
    
    private static ClassModel createTestClass()
    {
        ClassModel classModel = new ClassModel("PerformanceTest");
        
        MethodModel method = new MethodModel(classModel);
        method.setMethodName("sayHello");
        method.setReturnType(String.class);
        method.setParamterTypes(String.class);
        method.setParamterNames("name");
        method.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        method.setBody("return \"Hello, \" + name + \"!\";");
        classModel.putMethodModel(method);
        
        return classModel;
    }
}