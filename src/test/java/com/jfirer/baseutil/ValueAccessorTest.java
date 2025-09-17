package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.valueaccessor.GetInt;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.impl.LambdaAccessorImpl;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.compiler.ecj.ECJCompiler;
import com.jfirer.baseutil.smc.compiler.jdk.JDKCompiler;
import com.jfirer.baseutil.smc.compiler.ecj.JDTCompiler;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

@RunWith(Parameterized.class)
@Data
public class ValueAccessorTest
{
    int       a    = 1;
    byte      b    = 1;
    char      c    = 'c';
    double    d    = 1;
    float     f    = 1;
    boolean   bb   = false;
    short     s    = 1;
    long      l    = 1;
    Integer   a1   = 1;
    Byte      b1   = 1;
    Character c1   = 'c';
    Double    d1   = 1d;
    Float     f1   = 1f;
    Boolean   bb1  = false;
    Short     s1   = 1;
    Long      l1   = 1l;
    String    name = "sadsas";
    @Parameter
    public Function<Field, ValueAccessor> accessorFactory;

    @Parameter(1)
    public String accessorType;

    private CompileHelper compileHelper = new CompileHelper();

    @Parameters(name = "{1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
            {(Function<Field, ValueAccessor>) ValueAccessor::standard, "Standard"},
            {(Function<Field, ValueAccessor>) field -> ValueAccessor.compile(field, new CompileHelper(Thread.currentThread().getContextClassLoader(), new JDKCompiler())), "Compile-JDK"},
            {(Function<Field, ValueAccessor>) field -> ValueAccessor.compile(field, new CompileHelper(Thread.currentThread().getContextClassLoader(), new JDTCompiler())), "Compile-JDT"},
            {(Function<Field, ValueAccessor>) field -> ValueAccessor.compile(field, new CompileHelper(Thread.currentThread().getContextClassLoader(), new ECJCompiler())), "ECJCompiler"},
            {(Function<Field, ValueAccessor>) ValueAccessor::lambda, "Lambda"}
        });
    }

    @Test
    public void testread() throws NoSuchFieldException
    {
        ValueAccessorTest   target = new ValueAccessorTest();
        Field               a      = ValueAccessorTest.class.getDeclaredField("a");
        Field               b      = ValueAccessorTest.class.getDeclaredField("b");
        Field               c      = ValueAccessorTest.class.getDeclaredField("c");
        Field               d      = ValueAccessorTest.class.getDeclaredField("d");
        Field               f      = ValueAccessorTest.class.getDeclaredField("f");
        Field               bb     = ValueAccessorTest.class.getDeclaredField("bb");
        Field               s      = ValueAccessorTest.class.getDeclaredField("s");
        Field               l      = ValueAccessorTest.class.getDeclaredField("l");
        Field               a1     = ValueAccessorTest.class.getDeclaredField("a1");
        Field               b1     = ValueAccessorTest.class.getDeclaredField("b1");
        Field               c1     = ValueAccessorTest.class.getDeclaredField("c1");
        Field               d1     = ValueAccessorTest.class.getDeclaredField("d1");
        Field               f1     = ValueAccessorTest.class.getDeclaredField("f1");
        Field               bb1    = ValueAccessorTest.class.getDeclaredField("bb1");
        Field               s1     = ValueAccessorTest.class.getDeclaredField("s1");
        Field               l1     = ValueAccessorTest.class.getDeclaredField("l1");
        Field               name   = ValueAccessorTest.class.getDeclaredField("name");

        testField(target, a, accessorFactory.apply(a));
        testField(target, b, accessorFactory.apply(b));
        testField(target, c, accessorFactory.apply(c));
        testField(target, d, accessorFactory.apply(d));
        testField(target, f, accessorFactory.apply(f));
        testField(target, bb, accessorFactory.apply(bb));
        testField(target, s, accessorFactory.apply(s));
        testField(target, l, accessorFactory.apply(l));
        testField(target, a1, accessorFactory.apply(a1));
        testField(target, b1, accessorFactory.apply(b1));
        testField(target, c1, accessorFactory.apply(c1));
        testField(target, d1, accessorFactory.apply(d1));
        testField(target, f1, accessorFactory.apply(f1));
        testField(target, bb1, accessorFactory.apply(bb1));
        testField(target, s1, accessorFactory.apply(s1));
        testField(target, l1, accessorFactory.apply(l1));
        testField(target, name, accessorFactory.apply(name));
    }

    private void testField(ValueAccessorTest target, Field field, ValueAccessor accessor) {
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();

        if (fieldType == int.class) {
            Assert.assertEquals(target.a, accessor.getInt(target));
            Assert.assertEquals(target.a, accessor.get(target));
            accessor.set(target, 2);
            Assert.assertEquals(2, target.a);
            accessor.setObject(target, 3);
            Assert.assertEquals(3, target.a);
        } else if (fieldType == byte.class) {
            Assert.assertEquals(target.b, accessor.getByte(target));
            Assert.assertEquals(target.b, accessor.get(target));
            accessor.set(target, (byte) 2);
            Assert.assertEquals(2, target.b);
            accessor.setObject(target, (byte) 3);
            Assert.assertEquals(3, target.b);
        } else if (fieldType == char.class) {
            Assert.assertEquals(target.c, accessor.getChar(target));
            Assert.assertEquals(target.c, accessor.get(target));
            accessor.set(target, 'd');
            Assert.assertEquals('d', target.c);
            accessor.setObject(target, 'e');
            Assert.assertEquals('e', target.c);
        } else if (fieldType == double.class) {
            Assert.assertEquals(target.d, accessor.getDouble(target), 0.0001);
            Assert.assertEquals(target.d, accessor.get(target));
            accessor.set(target, 2.0);
            Assert.assertEquals(2.0, target.d, 0.0001);
            accessor.setObject(target, 3.0);
            Assert.assertEquals(3.0, target.d, 0.0001);
        } else if (fieldType == float.class) {
            Assert.assertEquals(target.f, accessor.getFloat(target), 0.0001);
            Assert.assertEquals(target.f, accessor.get(target));
            accessor.set(target, 2.0f);
            Assert.assertEquals(2.0f, target.f, 0.0001);
            accessor.setObject(target, 3.0f);
            Assert.assertEquals(3.0f, target.f, 0.0001);
        } else if (fieldType == boolean.class) {
            Assert.assertEquals(target.bb, accessor.getBoolean(target));
            Assert.assertEquals(target.bb, accessor.get(target));
            accessor.set(target, true);
            Assert.assertTrue(target.bb);
            accessor.setObject(target, true);
            Assert.assertTrue(target.bb);
        } else if (fieldType == short.class) {
            Assert.assertEquals(target.s, accessor.getShort(target));
            Assert.assertEquals(target.s, accessor.get(target));
            accessor.set(target, (short) 2);
            Assert.assertEquals(2, target.s);
            accessor.setObject(target, (short) 3);
            Assert.assertEquals(3, target.s);
        } else if (fieldType == long.class) {
            Assert.assertEquals(target.l, accessor.getLong(target));
            Assert.assertEquals(target.l, accessor.get(target));
            accessor.set(target, 2L);
            Assert.assertEquals(2L, target.l);
            accessor.setObject(target, 3L);
            Assert.assertEquals(3L, target.l);
        } else if (fieldType == Integer.class) {
            Assert.assertEquals(target.a1, accessor.getReference(target));
            Assert.assertEquals(target.a1, accessor.get(target));
            accessor.setObject(target, 2);
            Assert.assertEquals(Integer.valueOf(2), target.a1);
            accessor.setReference(target, 3);
            Assert.assertEquals(Integer.valueOf(3), target.a1);
        } else if (fieldType == Byte.class) {
            Assert.assertEquals(target.b1, accessor.getReference(target));
            Assert.assertEquals(target.b1, accessor.get(target));
            accessor.setObject(target, Byte.valueOf((byte) 2));
            Assert.assertEquals(Byte.valueOf((byte) 2), target.b1);
            accessor.setReference(target, Byte.valueOf((byte) 3));
            Assert.assertEquals(Byte.valueOf((byte) 3), target.b1);
        } else if (fieldType == Character.class) {
            Assert.assertEquals(target.c1, accessor.getReference(target));
            Assert.assertEquals(target.c1, accessor.get(target));
            accessor.setObject(target, Character.valueOf('d'));
            Assert.assertEquals(Character.valueOf('d'), target.c1);
            accessor.setReference(target, 'e');
            Assert.assertEquals(Character.valueOf('e'), target.c1);
        } else if (fieldType == Double.class) {
            Assert.assertEquals(target.d1, accessor.getReference(target));
            Assert.assertEquals(target.d1, accessor.get(target));
            accessor.setObject(target, 2.0);
            Assert.assertEquals(2.0, target.d1, 0.0001);
            accessor.setReference(target, 3.0);
            Assert.assertEquals(3.0, target.d1, 0.0001);
        } else if (fieldType == Float.class) {
            Assert.assertEquals(target.f1, accessor.getReference(target));
            Assert.assertEquals(target.f1, accessor.get(target));
            accessor.setObject(target, 2.0f);
            Assert.assertEquals(2.0f, target.f1, 0.0001);
            accessor.setReference(target, 3.0f);
            Assert.assertEquals(3.0f, target.f1, 0.0001);
        } else if (fieldType == Boolean.class) {
            Assert.assertEquals(target.bb1, accessor.getReference(target));
            Assert.assertEquals(target.bb1, accessor.get(target));
            accessor.setObject(target, true);
            Assert.assertTrue(target.bb1);
            accessor.setReference(target, false);
            Assert.assertFalse(target.bb1);
        } else if (fieldType == Short.class) {
            Assert.assertEquals(target.s1, accessor.getReference(target));
            Assert.assertEquals(target.s1, accessor.get(target));
            accessor.setObject(target, (short) 2);
            Assert.assertEquals(Short.valueOf((short) 2), target.s1);
            accessor.setReference(target, (short) 3);
            Assert.assertEquals(Short.valueOf((short) 3), target.s1);
        } else if (fieldType == Long.class) {
            Assert.assertEquals(target.l1, accessor.getReference(target));
            Assert.assertEquals(target.l1, accessor.get(target));
            accessor.setObject(target, 2L);
            Assert.assertEquals(Long.valueOf(2L), target.l1);
            accessor.setReference(target, 3L);
            Assert.assertEquals(Long.valueOf(3L), target.l1);
        } else if (fieldType == String.class) {
            Assert.assertEquals(target.name, accessor.getReference(target));
            Assert.assertEquals(target.name, accessor.get(target));
            accessor.setObject(target, "hello");
            Assert.assertEquals("hello", target.name);
            accessor.setReference(target, "world");
            Assert.assertEquals("world", target.name);
        }
    }

    @Test
    public void test2() throws Throwable
    {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        GetInt factoryLambda = (GetInt) LambdaMetafactory.metafactory(MethodHandles.lookup(), //固定参数
                                                                                         "get",//需要实现的函数式接口的方法名
                                                                                         MethodType.methodType(GetInt.class),//固定参数，本方法最终返回的函数式接口的类
                                                                                         MethodType.methodType(int.class, Object.class),// 函数式接口的方法签名，如果是泛型的，用 Object.class代替
                                                                                         lookup.findVirtual(ValueAccessorTest.class, "getA", MethodType.methodType(int.class)),//这个函数接口需要引用的类的方法
                                                                                         MethodType.methodType(int.class, ValueAccessorTest.class))//实际运行时候传入的参数类型，也就是泛型信息在运行的时候对应的实际的类型。
                                                                            .getTarget().invokeExact();
        factoryLambda.get(new ValueAccessorTest());
    }

    @Test
    public void test3() throws NoSuchMethodException, IllegalAccessException, NoSuchFieldException
    {
        Field a = ValueAccessorTest.class.getDeclaredField("a");
        new LambdaAccessorImpl(a);
    }
}
