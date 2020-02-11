package com.jfireframework.baseutil;

import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.ConstructorModel;
import com.jfireframework.baseutil.smc.model.MethodModel;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class CompilerTest
{
    @Test
    public void test() throws NoSuchMethodException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        ClassModel classModel = new ClassModel("PtestImpl");
        classModel.addInterface(Ptest.class);
        MethodModel methodModel = new MethodModel(Ptest.class.getMethod("sayHello", String.class), classModel);
        methodModel.setParamterNames(new String[]{"arg"});
        methodModel.setBody("return \"hi\";");
        classModel.putMethodModel(methodModel);
        CompileHelper compiler = new CompileHelper(Thread.currentThread().getContextClassLoader());
        Class<?>      compile  = compiler.compile(classModel);
        Ptest         instance = (Ptest) compile.newInstance();
        Assert.assertEquals("hi", instance.sayHello(""));
    }

    @Test
    public void testConstruct() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        ClassModel classModel = new ClassModel("parent_ss");
        classModel.setParentClass(Parent.class);
        ConstructorModel constructModel = new ConstructorModel(classModel);
        constructModel.setParamTypes(String.class);
        constructModel.setParamNames("name");
        constructModel.setBody("this.name =name;");
        classModel.addConstructor(constructModel);
        Class<?>       compile     = new CompileHelper().compile(classModel);
        Constructor<?> constructor = compile.getConstructor(String.class);
        Parent         hello       = (Parent) constructor.newInstance("hello");
        Assert.assertEquals("hello", hello.getName());
    }

    public interface Ptest
    {
        String sayHello(String arg);
    }

    public static abstract class Parent
    {
        protected String name;

        public String getName()
        {
            return name;
        }
    }
}
