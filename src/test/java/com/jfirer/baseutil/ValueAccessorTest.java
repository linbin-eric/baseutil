package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ValueAccessor;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.time.NanoTimeWatch;
import org.junit.Assert;
import org.junit.Test;

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
        ValueAccessorTest test = new ValueAccessorTest();
        ValueAccessor     valueAccessor;
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("a"));
        Assert.assertEquals(test.a, valueAccessor.getInt(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("b"));
        Assert.assertEquals(test.b, valueAccessor.getByte(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("c"));
        Assert.assertEquals(test.c, valueAccessor.getChar(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("d"));
        Assert.assertEquals(test.d, valueAccessor.getDouble(test), 0.000001d);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("f"));
        Assert.assertEquals(test.f, valueAccessor.getFloat(test), 0x000001f);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("bb"));
        Assert.assertEquals(test.bb, valueAccessor.getBoolean(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("s"));
        Assert.assertEquals(test.s, valueAccessor.getShort(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("l"));
        Assert.assertEquals(test.l, valueAccessor.getLong(test));
        ////
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("a1"));
        Assert.assertEquals(test.a1, valueAccessor.getIntObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("b1"));
        Assert.assertEquals(test.b1, valueAccessor.getByteObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("c1"));
        Assert.assertEquals(test.c1, valueAccessor.getCharObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("d1"));
        Assert.assertEquals(test.d1, valueAccessor.getDoubleObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("f1"));
        Assert.assertEquals(test.f1, valueAccessor.getFloatObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("bb1"));
        Assert.assertEquals(test.bb1, valueAccessor.getBooleanObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("s1"));
        Assert.assertEquals(test.s1, valueAccessor.getShortObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("l1"));
        Assert.assertEquals(test.l1, valueAccessor.getLongObject(test));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("name"));
        Assert.assertEquals(test.name, valueAccessor.get(test));
    }

    @Test
    public void testWrite() throws NoSuchFieldException
    {
        ValueAccessorTest test = new ValueAccessorTest();
        ValueAccessor     valueAccessor;
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("a"));
        valueAccessor.set(test, 2);
        Assert.assertEquals(test.a, 2);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("b"));
        valueAccessor.set(test, (byte) 2);
        Assert.assertEquals(test.b, 2);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("c"));
        valueAccessor.set(test, 'c');
        Assert.assertEquals(test.c, 'c');
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("d"));
        valueAccessor.set(test, 2d);
        Assert.assertEquals(test.d, 2d, 0.000001d);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("f"));
        valueAccessor.set(test, 2f);
        Assert.assertEquals(test.f, 2f, 0x000001f);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("bb"));
        valueAccessor.set(test, true);
        Assert.assertEquals(test.bb, true);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("s"));
        valueAccessor.set(test, (short) 2);
        Assert.assertEquals(test.s, 2);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("l"));
        valueAccessor.set(test, 2l);
        Assert.assertEquals(test.l, 2l);
        ////
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("a1"));
        valueAccessor.set(test, 2);
        Assert.assertEquals(test.a1, Integer.valueOf(2));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("b1"));
        valueAccessor.set(test, (byte) 2);
        Assert.assertEquals(test.b1, Byte.valueOf((byte) 2));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("c1"));
        valueAccessor.set(test, 'c');
        Assert.assertEquals(test.c1, Character.valueOf('c'));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("d1"));
        valueAccessor.set(test, 2d);
        Assert.assertEquals(test.d1, 2d, 0.000001d);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("f1"));
        valueAccessor.set(test, 2f);
        Assert.assertEquals(test.f1, 2f, 0x000001f);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("bb1"));
        valueAccessor.set(test, true);
        Assert.assertEquals(test.bb1, true);
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("s1"));
        valueAccessor.set(test, (short) 2);
        Assert.assertEquals(test.s1, Short.valueOf((short) 2));
        valueAccessor = new ValueAccessor(ValueAccessorTest.class.getDeclaredField("l1"));
        valueAccessor.set(test, 2l);
        Assert.assertEquals(test.l1, Long.valueOf(2));
    }

    @Test
    public void testread1() throws NoSuchFieldException
    {
        ValueAccessorTest test = new ValueAccessorTest();
        ValueAccessor     valueAccessor;
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("a"), compileHelper);
        Assert.assertEquals(test.a, valueAccessor.getInt(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("b"), compileHelper);
        Assert.assertEquals(test.b, valueAccessor.getByte(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("c"), compileHelper);
        Assert.assertEquals(test.c, valueAccessor.getChar(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("d"), compileHelper);
        Assert.assertEquals(test.d, valueAccessor.getDouble(test), 0.000001d);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("f"), compileHelper);
        Assert.assertEquals(test.f, valueAccessor.getFloat(test), 0x000001f);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("bb"), compileHelper);
        Assert.assertEquals(test.bb, valueAccessor.getBoolean(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("s"), compileHelper);
        Assert.assertEquals(test.s, valueAccessor.getShort(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("l"), compileHelper);
        Assert.assertEquals(test.l, valueAccessor.getLong(test));
        ////
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("a1"), compileHelper);
        Assert.assertEquals(test.a1, valueAccessor.getIntObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("b1"), compileHelper);
        Assert.assertEquals(test.b1, valueAccessor.getByteObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("c1"), compileHelper);
        Assert.assertEquals(test.c1, valueAccessor.getCharObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("d1"), compileHelper);
        Assert.assertEquals(test.d1, valueAccessor.getDoubleObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("f1"), compileHelper);
        Assert.assertEquals(test.f1, valueAccessor.getFloatObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("bb1"), compileHelper);
        Assert.assertEquals(test.bb1, valueAccessor.getBooleanObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("s1"), compileHelper);
        Assert.assertEquals(test.s1, valueAccessor.getShortObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("l1"), compileHelper);
        Assert.assertEquals(test.l1, valueAccessor.getLongObject(test));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("name"), compileHelper);
        Assert.assertEquals(test.name, valueAccessor.get(test));
    }

    @Test
    public void testWrite1() throws NoSuchFieldException
    {
        ValueAccessorTest test = new ValueAccessorTest();
        ValueAccessor     valueAccessor;
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("a"), compileHelper);
        valueAccessor.set(test, 2);
        Assert.assertEquals(test.a, 2);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("b"), compileHelper);
        valueAccessor.set(test, (byte) 2);
        Assert.assertEquals(test.b, 2);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("c"), compileHelper);
        valueAccessor.set(test, 'c');
        Assert.assertEquals(test.c, 'c');
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("d"), compileHelper);
        valueAccessor.set(test, 2d);
        Assert.assertEquals(test.d, 2d, 0.000001d);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("f"), compileHelper);
        valueAccessor.set(test, 2f);
        Assert.assertEquals(test.f, 2f, 0x000001f);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("bb"), compileHelper);
        valueAccessor.set(test, true);
        Assert.assertEquals(test.bb, true);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("s"), compileHelper);
        valueAccessor.set(test, (short) 2);
        Assert.assertEquals(test.s, 2);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("l"), compileHelper);
        valueAccessor.set(test, 2l);
        Assert.assertEquals(test.l, 2l);
        ////
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("a1"), compileHelper);
        valueAccessor.set(test, 2);
        Assert.assertEquals(test.a1, Integer.valueOf(2));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("b1"), compileHelper);
        valueAccessor.set(test, (byte) 2);
        Assert.assertEquals(test.b1, Byte.valueOf((byte) 2));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("c1"), compileHelper);
        valueAccessor.set(test, 'c');
        Assert.assertEquals(test.c1, Character.valueOf('c'));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("d1"), compileHelper);
        valueAccessor.set(test, 2d);
        Assert.assertEquals(test.d1, 2d, 0.000001d);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("f1"), compileHelper);
        valueAccessor.set(test, 2f);
        Assert.assertEquals(test.f1, 2f, 0x000001f);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("bb1"), compileHelper);
        valueAccessor.set(test, true);
        Assert.assertEquals(test.bb1, true);
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("s1"), compileHelper);
        valueAccessor.set(test, (short) 2);
        Assert.assertEquals(test.s1, Short.valueOf((short) 2));
        valueAccessor = ValueAccessor.create(ValueAccessorTest.class.getDeclaredField("l1"), compileHelper);
        valueAccessor.set(test, 2l);
        Assert.assertEquals(test.l1, Long.valueOf(2));
    }

    public int getA()
    {
        return a;
    }

    public void setA(int a)
    {
        this.a = a;
    }

    public byte getB()
    {
        return b;
    }

    public void setB(byte b)
    {
        this.b = b;
    }

    public char getC()
    {
        return c;
    }

    public void setC(char c)
    {
        this.c = c;
    }

    public double getD()
    {
        return d;
    }

    public void setD(double d)
    {
        this.d = d;
    }

    public float getF()
    {
        return f;
    }

    public void setF(float f)
    {
        this.f = f;
    }

    public boolean isBb()
    {
        return bb;
    }

    public void setBb(boolean bb)
    {
        this.bb = bb;
    }

    public short getS()
    {
        return s;
    }

    public void setS(short s)
    {
        this.s = s;
    }

    public long getL()
    {
        return l;
    }

    public void setL(long l)
    {
        this.l = l;
    }

    public Integer getA1()
    {
        return a1;
    }

    public void setA1(Integer a1)
    {
        this.a1 = a1;
    }

    public Byte getB1()
    {
        return b1;
    }

    public void setB1(Byte b1)
    {
        this.b1 = b1;
    }

    public Character getC1()
    {
        return c1;
    }

    public void setC1(Character c1)
    {
        this.c1 = c1;
    }

    public Double getD1()
    {
        return d1;
    }

    public void setD1(Double d1)
    {
        this.d1 = d1;
    }

    public Float getF1()
    {
        return f1;
    }

    public void setF1(Float f1)
    {
        this.f1 = f1;
    }

    public Boolean getBb1()
    {
        return bb1;
    }

    public void setBb1(Boolean bb1)
    {
        this.bb1 = bb1;
    }

    public Short getS1()
    {
        return s1;
    }

    public void setS1(Short s1)
    {
        this.s1 = s1;
    }

    public Long getL1()
    {
        return l1;
    }

    public void setL1(Long l1)
    {
        this.l1 = l1;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
