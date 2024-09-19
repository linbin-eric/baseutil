package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import com.jfirer.baseutil.reflect.valueaccessor.GetInt;
import com.jfirer.baseutil.reflect.valueaccessor.impl.LambdaAccessorImpl;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

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
    private CompileHelper compileHelper = new CompileHelper();

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
        List<ValueAccessor> list   = new ArrayList<>();
        list.add(ValueAccessor.standard(a));
        list.add(ValueAccessor.compile(a));
        list.add(ValueAccessor.lambda(a));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.a, each.getInt(target));
            Assert.assertEquals(target.a, each.get(target));
            each.set(target, 2);
            Assert.assertEquals(2, target.a);
            each.setObject(target, 3);
            Assert.assertEquals(3, target.a);
        }
        list.clear();
        list.add(ValueAccessor.standard(b));
        list.add(ValueAccessor.compile(b));
        list.add(ValueAccessor.lambda(b));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.b, each.getByte(target));
            Assert.assertEquals(target.b, each.get(target));
            each.set(target, (byte) 2);
            Assert.assertEquals(2, target.b);
            each.setObject(target, (byte) 3);
            Assert.assertEquals(3, target.b);
        }
        list.clear();
        list.add(ValueAccessor.standard(c));
        list.add(ValueAccessor.compile(c));
        list.add(ValueAccessor.lambda(c));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.c, each.getChar(target));
            Assert.assertEquals(target.c, each.get(target));
            each.set(target, 'd');
            Assert.assertEquals('d', target.c);
            each.setObject(target, 'e');
            Assert.assertEquals('e', target.c);
        }
        list.clear();
        list.add(ValueAccessor.standard(d));
        list.add(ValueAccessor.compile(d));
        list.add(ValueAccessor.lambda(d));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.d, each.getDouble(target), 0.0001);
            Assert.assertEquals(target.d, each.get(target));
            each.set(target, 2.0);
            Assert.assertEquals(2.0, target.d, 0.0001);
            each.setObject(target, 3.0);
            Assert.assertEquals(3.0, target.d, 0.0001);
        }
        list.clear();
        list.add(ValueAccessor.standard(f));
        list.add(ValueAccessor.compile(f));
        list.add(ValueAccessor.lambda(f));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.f, each.getFloat(target), 0.0001);
            Assert.assertEquals(target.f, each.get(target));
            each.set(target, 2.0f);
            Assert.assertEquals(2.0f, target.f, 0.0001);
            each.setObject(target, 3.0f);
            Assert.assertEquals(3.0f, target.f, 0.0001);
        }
        list.clear();
        list.add(ValueAccessor.standard(bb));
        list.add(ValueAccessor.compile(bb));
        list.add(ValueAccessor.lambda(bb));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.bb, each.getBoolean(target));
            Assert.assertEquals(target.bb, each.get(target));
            each.set(target, true);
            Assert.assertTrue(target.bb);
            each.setObject(target, true);
            Assert.assertTrue(target.bb);
        }
        list.clear();
        list.add(ValueAccessor.standard(s));
        list.add(ValueAccessor.compile(s));
        list.add(ValueAccessor.lambda(s));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.s, each.getShort(target));
            Assert.assertEquals(target.s, each.get(target));
            each.set(target, (short) 2);
            Assert.assertEquals(2, target.s);
            each.setObject(target, (short) 3);
            Assert.assertEquals(3, target.s);
        }
        list.clear();
        list.add(ValueAccessor.standard(l));
        list.add(ValueAccessor.compile(l));
        list.add(ValueAccessor.lambda(l));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.l, each.getLong(target));
            Assert.assertEquals(target.l, each.get(target));
            each.set(target, 2L);
            Assert.assertEquals(2L, target.l);
            each.setObject(target, 3L);
            Assert.assertEquals(3L, target.l);
        }
        list.clear();
        list.add(ValueAccessor.standard(a1));
        list.add(ValueAccessor.compile(a1));
        list.add(ValueAccessor.lambda(a1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.a1, each.getReference(target));
            Assert.assertEquals(target.a1, each.get(target));
            each.setObject(target, 2);
            Assert.assertEquals(Integer.valueOf(2), target.a1);
            each.setReference(target, 3);
            Assert.assertEquals(Integer.valueOf(3), target.a1);
        }
        list.clear();
        list.add(ValueAccessor.standard(b1));
        list.add(ValueAccessor.compile(b1));
        list.add(ValueAccessor.lambda(b1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.b1, each.getReference(target));
            Assert.assertEquals(target.b1, each.get(target));
            each.setObject(target, Byte.valueOf((byte) 2));
            Assert.assertEquals(Byte.valueOf((byte) 2), target.b1);
            each.setReference(target, Byte.valueOf((byte) 3));
            Assert.assertEquals(Byte.valueOf((byte) 3), target.b1);
        }
        list.clear();
        list.add(ValueAccessor.standard(c1));
        list.add(ValueAccessor.compile(c1));
        list.add(ValueAccessor.lambda(c1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.c1, each.getReference(target));
            Assert.assertEquals(target.c1, each.get(target));
            each.setObject(target, Character.valueOf('d'));
            Assert.assertEquals(Character.valueOf('d'), target.c1);
            each.setReference(target, 'e');
            Assert.assertEquals(Character.valueOf('e'), target.c1);
        }
        list.clear();
        list.add(ValueAccessor.standard(d1));
        list.add(ValueAccessor.compile(d1));
        list.add(ValueAccessor.lambda(d1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.d1, each.getReference(target));
            Assert.assertEquals(target.d1, each.get(target));
            each.setObject(target, 2.0);
            Assert.assertEquals(2.0, target.d1, 0.0001);
            each.setReference(target, 3.0);
            Assert.assertEquals(3.0, target.d1, 0.0001);
        }
        list.clear();
        list.add(ValueAccessor.standard(f1));
        list.add(ValueAccessor.compile(f1));
        list.add(ValueAccessor.lambda(f1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.f1, each.getReference(target));
            Assert.assertEquals(target.f1, each.get(target));
            each.setObject(target, 2.0f);
            Assert.assertEquals(2.0f, target.f1, 0.0001);
            each.setReference(target, 3.0f);
            Assert.assertEquals(3.0f, target.f1, 0.0001);
        }
        list.clear();
        list.add(ValueAccessor.standard(bb1));
        list.add(ValueAccessor.compile(bb1));
        list.add(ValueAccessor.lambda(bb1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.bb1, each.getReference(target));
            Assert.assertEquals(target.bb1, each.get(target));
            each.setObject(target, true);
            Assert.assertTrue(target.bb1);
            each.setReference(target, false);
            Assert.assertFalse(target.bb1);
        }
        list.clear();
        list.add(ValueAccessor.standard(s1));
        list.add(ValueAccessor.compile(s1));
        list.add(ValueAccessor.lambda(s1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.s1, each.getReference(target));
            Assert.assertEquals(target.s1, each.get(target));
            each.setObject(target, (short) 2);
            Assert.assertEquals(Short.valueOf((short) 2), target.s1);
            each.setReference(target, (short) 3);
            Assert.assertEquals(Short.valueOf((short) 3), target.s1);
        }
        list.clear();
        list.add(ValueAccessor.standard(l1));
        list.add(ValueAccessor.compile(l1));
        list.add(ValueAccessor.lambda(l1));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.l1, each.getReference(target));
            Assert.assertEquals(target.l1, each.get(target));
            each.setObject(target, 2L);
            Assert.assertEquals(Long.valueOf(2L), target.l1);
            each.setReference(target, 3L);
            Assert.assertEquals(Long.valueOf(3L), target.l1);
        }
        list.clear();
        list.add(ValueAccessor.standard(name));
        list.add(ValueAccessor.compile(name));
        list.add(ValueAccessor.lambda(name));
        for (ValueAccessor each : list)
        {
            Assert.assertEquals(target.name, each.getReference(target));
            Assert.assertEquals(target.name, each.get(target));
            each.setObject(target, "hello");
            Assert.assertEquals("hello", target.name);
            each.setReference(target, "world");
            Assert.assertEquals("world", target.name);
        }
        list.clear();
    }

    @Test
    public void test2() throws Throwable
    {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        GetInt<ValueAccessorTest> factoryLambda = (GetInt) LambdaMetafactory.metafactory(MethodHandles.lookup(), //固定参数
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
