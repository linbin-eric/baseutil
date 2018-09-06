package com.jfireframework.baseutil;

import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class CompilerTest
{
    public static interface Ptest
    {
        String sayHello();
    }

    @Test
    public void test() throws NoSuchMethodException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        ClassModel classModel = new ClassModel("PtestImpl");
        classModel.addInterface(Ptest.class);
        MethodModel methodModel = new MethodModel(Ptest.class.getMethod("sayHello"), classModel);
        methodModel.setBody("return \"hi\";");
        classModel.putMethodModel(methodModel);
        JavaStringCompiler compiler = new JavaStringCompiler(Thread.currentThread().getContextClassLoader());
        Class<?> compile = compiler.compile(classModel);
        Ptest instance = (Ptest) compile.newInstance();
        Assert.assertEquals("hi", instance.sayHello());
    }
}
