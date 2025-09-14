package com.jfirer.baseutil;

import com.jfirer.baseutil.smc.compiler.Compiler;
import com.jfirer.baseutil.smc.compiler.JDKCompiler;
import com.jfirer.baseutil.smc.compiler.JDTCompiler;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.ConstructorModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class CompilerTest
{
    private final Compiler compiler;
    private final String compilerName;

    public CompilerTest(Compiler compiler, String compilerName) {
        this.compiler = compiler;
        this.compilerName = compilerName;
    }

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { new JDTCompiler(), "JDTCompiler" },
            { new JDKCompiler(), "JDKCompiler" }
        });
    }

    @Test
    public void test() throws NoSuchMethodException, IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        System.out.println("Testing with " + compilerName);
        ClassModel classModel = new ClassModel("PtestImpl");
        classModel.addInterface(Ptest.class);
        MethodModel methodModel = new MethodModel(Ptest.class.getMethod("sayHello", String.class), classModel);
        methodModel.setParamterNames(new String[]{"arg"});
        methodModel.setBody("return \"hi\";");
        classModel.putMethodModel(methodModel);
        CompileHelper compilerHelper = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler);
        Class<?>      compile  = compilerHelper.compile(classModel);
        Ptest         instance = (Ptest) compile.newInstance();
        Assert.assertEquals("hi", instance.sayHello(""));
    }

    @Test
    public void testConstruct() throws IOException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException
    {
        System.out.println("Testing constructor with " + compilerName);
        ClassModel classModel = new ClassModel("parent_ss");
        classModel.setParentClass(Parent.class);
        ConstructorModel constructModel = new ConstructorModel(classModel);
        constructModel.setParamTypes(String.class);
        constructModel.setParamNames("name");
        constructModel.setBody("this.name =name;");
        classModel.addConstructor(constructModel);
        Class<?>       compile     = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler).compile(classModel);
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
