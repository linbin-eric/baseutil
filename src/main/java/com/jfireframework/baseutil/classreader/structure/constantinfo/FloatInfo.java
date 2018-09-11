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
                | ((bytes[counter + 1] & 0xff) << 16)//
                | ((bytes[counter + 2] & 0xff) << 8)//
                | ((bytes[counter + 3] & 0xff) << 0);
        value = Float.intBitsToFloat(intBits);
        counter += 4;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
