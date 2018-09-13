package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

import java.nio.charset.Charset;

public class Utf8Info extends ConstantInfo
{
    private int length;
    private String value;

    public Utf8Info()
    {
        type = ConstantType.Utf8;
    }

    public int getLength()
    {
        return length;
    }

    public String getValue()
    {
        return value;
    }

    @Override
    public String toString()
    {
        return "Utf8Info{" + value + '}';
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        length = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        byte[] content = new byte[length];
        System.arraycopy(bytes, counter, content, 0, length);
        counter += length;
        value = new String(content, CHARSET);
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }

    private static final Charset CHARSET = Charset.forName("utf8");
}
