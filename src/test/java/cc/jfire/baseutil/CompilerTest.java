package cc.jfire.baseutil;

import cc.jfire.baseutil.smc.compiler.CompileHelper;
import cc.jfire.baseutil.smc.compiler.Compiler;
import cc.jfire.baseutil.smc.compiler.ecj.ECJCompiler;
import cc.jfire.baseutil.smc.compiler.jdk.JDKCompiler;
import cc.jfire.baseutil.smc.model.ClassModel;
import cc.jfire.baseutil.smc.model.ConstructorModel;
import cc.jfire.baseutil.smc.model.FieldModel;
import cc.jfire.baseutil.smc.model.MethodModel;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.io.IOException;
import java.lang.annotation.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
            { new ECJCompiler(), "ECJCompiler" },
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

    // 测试子类继承复杂情况
    @Test
    public void testInheritanceComplex() throws Exception
    {
        System.out.println("Testing complex inheritance with " + compilerName);
        ClassModel childModel = new ClassModel("ComplexChild");
        childModel.setParentClass(GrandParent.class);
        childModel.addInterface(ChildInterface.class);

        // 添加字段
        FieldModel fieldModel = new FieldModel("childField", String.class, childModel);
        childModel.addField(fieldModel);

        // 添加构造器
        ConstructorModel constructorModel = new ConstructorModel(childModel);
        constructorModel.setParamTypes(String.class, int.class);
        constructorModel.setParamNames("fieldValue", "ageValue");
        constructorModel.setBody("this.childField = fieldValue; this.age = ageValue;");
        childModel.addConstructor(constructorModel);

        // 添加方法
        MethodModel methodModel = new MethodModel(childModel);
        methodModel.setMethodName("childMethod");
        methodModel.setReturnType(String.class);
        methodModel.setParamterTypes(String.class);
        methodModel.setParamterNames("arg");
        methodModel.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        methodModel.setBody("return \"Child: \" + arg + \" (age: \" + age + \")\";");
        childModel.putMethodModel(methodModel);

        // 重写父类方法
        MethodModel overrideMethod = new MethodModel(
            GrandParent.class.getMethod("grandMethod"),
            childModel
        );
        overrideMethod.setBody("return \"Overridden from: \" + childField;");
        childModel.putMethodModel(overrideMethod);

        Class<?> compiled = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler)
            .compile(childModel);

        Constructor<?> constructor = compiled.getConstructor(String.class, int.class);
        ChildInterface instance = (ChildInterface) constructor.newInstance("test", 25);

        Assert.assertEquals("Child: hello (age: 25)", instance.childMethod("hello"));
        Assert.assertEquals("Overridden from: test", instance.grandMethod());
    }


    // 测试泛型支持
    @Test
    public void testGenerics() throws Exception
    {
        System.out.println("Testing generics with " + compilerName);
        ClassModel genericModel = new ClassModel("GenericContainer");
        genericModel.addInterface(GenericInterface.class);

        // 添加泛型字段
        genericModel.addField(new FieldModel("data", Object.class, genericModel));

        // 添加构造器
        ConstructorModel constructor = new ConstructorModel(genericModel);
        constructor.setParamTypes(Object.class);
        constructor.setParamNames("data");
        constructor.setBody("this.data = data;");
        genericModel.addConstructor(constructor);

        // 实现泛型接口方法（由于框架限制，返回Object而不是T）
        MethodModel methodModel = new MethodModel(genericModel);
        methodModel.setMethodName("getData");
        methodModel.setReturnType(Object.class);
        methodModel.setParamterTypes();
        methodModel.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        methodModel.setBody("return data;");
        genericModel.putMethodModel(methodModel);

        Class<?> compiled = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler)
            .compile(genericModel);

        Constructor<?> constructorInstance = compiled.getConstructor(Object.class);
        Object instance = constructorInstance.newInstance("Generic Data");

        // 通过反射调用 getData 方法
        Method getDataMethod = compiled.getMethod("getData");
        Object result = getDataMethod.invoke(instance);
        Assert.assertEquals("Generic Data", result);
    }



    // 测试多重继承和接口实现
    @Test
    public void testMultipleInterfaces() throws Exception
    {
        System.out.println("Testing multiple interfaces with " + compilerName);
        ClassModel multiModel = new ClassModel("MultiImpl");
        multiModel.addInterface(InterfaceA.class);
        multiModel.addInterface(InterfaceB.class);

        // 实现接口A的方法
        MethodModel methodA = new MethodModel(
            InterfaceA.class.getMethod("methodA"),
            multiModel
        );
        methodA.setBody("return \"A implemented\";");
        multiModel.putMethodModel(methodA);

        // 实现接口B的方法
        MethodModel methodB = new MethodModel(
            InterfaceB.class.getMethod("methodB"),
            multiModel
        );
        methodB.setBody("return \"B implemented\";");
        multiModel.putMethodModel(methodB);

        // 添加额外方法
        MethodModel combinedMethod = new MethodModel(multiModel);
        combinedMethod.setMethodName("combined");
        combinedMethod.setReturnType(String.class);
        combinedMethod.setParamterTypes();
        combinedMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        combinedMethod.setBody("return methodA() + \" & \" + methodB();");
        multiModel.putMethodModel(combinedMethod);

        Class<?> compiled = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler)
            .compile(multiModel);

        Object instance = compiled.newInstance();

        // 测试接口A
        InterfaceA a = (InterfaceA) instance;
        Assert.assertEquals("A implemented", a.methodA());

        // 测试接口B
        InterfaceB b = (InterfaceB) instance;
        Assert.assertEquals("B implemented", b.methodB());

        // 测试组合方法
        Method combined = compiled.getMethod("combined");
        Assert.assertEquals("A implemented & B implemented", combined.invoke(instance));
    }

    // 测试静态方法和字段（简化版）
    @Test
    public void testStaticMembers() throws Exception
    {
        System.out.println("Testing static members with " + compilerName);
        ClassModel staticModel = new ClassModel("StaticClass");

        // 添加静态字段（初始化值）
        staticModel.addField(new FieldModel("STATIC_VALUE", String.class, "\"INITIAL\"", staticModel));
        staticModel.addField(new FieldModel("counter", int.class, "0", staticModel));

        // 添加实例方法来模拟静态访问
        MethodModel getMethod = new MethodModel(staticModel);
        getMethod.setMethodName("getStaticValue");
        getMethod.setReturnType(String.class);
        getMethod.setParamterTypes();
        getMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        getMethod.setBody("return STATIC_VALUE;");
        staticModel.putMethodModel(getMethod);

        MethodModel setMethod = new MethodModel(staticModel);
        setMethod.setMethodName("setStaticValue");
        setMethod.setReturnType(void.class);
        setMethod.setParamterTypes(String.class);
        setMethod.setParamterNames("value");
        setMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        setMethod.setBody("STATIC_VALUE = value;");
        staticModel.putMethodModel(setMethod);

        // 添加实例方法访问静态字段
        MethodModel incrementMethod = new MethodModel(staticModel);
        incrementMethod.setMethodName("incrementCounter");
        incrementMethod.setReturnType(int.class);
        incrementMethod.setParamterTypes();
        incrementMethod.setAccessLevel(MethodModel.AccessLevel.PUBLIC);
        incrementMethod.setBody("counter++; return counter;");
        staticModel.putMethodModel(incrementMethod);

        Class<?> compiled = new CompileHelper(Thread.currentThread().getContextClassLoader(), compiler)
            .compile(staticModel);

        // 创建实例测试（模拟静态行为）
        Object instance1 = compiled.newInstance();
        Object instance2 = compiled.newInstance();

        // 测试 getter 和 setter
        Method getter = compiled.getMethod("getStaticValue");
        Method setter = compiled.getMethod("setStaticValue", String.class);

        Assert.assertEquals("INITIAL", getter.invoke(instance1));
        setter.invoke(instance1, "UPDATED");
        // 注意：由于框架限制，每个实例有自己的字段副本
        Assert.assertEquals("UPDATED", getter.invoke(instance1));
        Assert.assertEquals("INITIAL", getter.invoke(instance2)); // 实例2保持初始值

        // 测试实例方法访问字段
        Method increment = compiled.getMethod("incrementCounter");
        Assert.assertEquals(1, increment.invoke(instance1));
        Assert.assertEquals(1, increment.invoke(instance2)); // 每个实例有独立的计数器
        Assert.assertEquals(2, increment.invoke(instance1));

        // 验证字段值（每个实例有自己的字段）
        Field counterField = compiled.getDeclaredField("counter");
        counterField.setAccessible(true);
        Assert.assertEquals(2, counterField.get(instance1));
        Assert.assertEquals(1, counterField.get(instance2));
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

    // 测试用的接口和类定义
    public interface ChildInterface
    {
        String childMethod(String arg);
        String grandMethod();
    }

    public static class GrandParent
    {
        protected int age;

        public String grandMethod()
        {
            return "Grand parent method";
        }
    }

    public interface GenericInterface<T>
    {
        T getData();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface TestAnnotation
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface FieldAnnotation
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface MethodAnnotation
    {
    }

    public interface InterfaceA
    {
        String methodA();
    }

    public interface InterfaceB
    {
        String methodB();
    }
}
