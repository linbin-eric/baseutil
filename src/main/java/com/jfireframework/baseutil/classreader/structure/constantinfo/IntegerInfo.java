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
                | ((bytes[counter + 1] << 16) & 0xff)//
                | ((bytes[counter + 2] << 8) & 0xff)//
                | ((bytes[counter + 3] << 0) & 0xff);//
        counter += 4;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
