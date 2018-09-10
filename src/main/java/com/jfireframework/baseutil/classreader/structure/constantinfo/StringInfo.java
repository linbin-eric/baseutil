package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class StringInfo extends ConstantInfo
{
    private int stringIndex;
    private String value;

    public StringInfo()
    {
        type = ConstantType.String;
    }

    public int getStringIndex()
    {
        return stringIndex;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        stringIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
        value = ((Utf8Info) constant_pool[stringIndex - 1]).getValue();
    }
}
