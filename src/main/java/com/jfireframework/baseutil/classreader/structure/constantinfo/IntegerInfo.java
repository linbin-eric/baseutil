package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class IntegerInfo extends ConstantInfo
{
    private int value;

    public int getValue()
    {
        return value;
    }

    public IntegerInfo()
    {
        type = ConstantType.Integer;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        value = ((bytes[counter] & 0xff) << 24)//
                | ((bytes[counter + 1] & 0xff) << 16)//
                | ((bytes[counter + 2] & 0xff) << 8)//
                | ((bytes[counter + 3] & 0xff) << 0);
        counter += 4;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
