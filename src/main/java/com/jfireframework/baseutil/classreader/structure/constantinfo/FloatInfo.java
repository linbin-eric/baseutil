package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class FloatInfo extends ConstantInfo
{
    private float value;

    public FloatInfo()
    {
        type = ConstantType.Float;
    }

    public float getValue()
    {
        return value;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        int intBits = ((bytes[counter] & 0xff) << 24)//
                | ((bytes[counter + 1] << 16) & 0xff)//
                | ((bytes[counter + 2] << 8) & 0xff)//
                | ((bytes[counter + 3] << 0) & 0xff);//
        value = Float.intBitsToFloat(intBits);
        counter += 4;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
