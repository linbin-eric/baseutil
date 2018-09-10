package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class DoubleInfo extends ConstantInfo
{
    private double value;

    public DoubleInfo()
    {
        type = ConstantType.Double;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        long longBits = ((bytes[counter] & 0xff) << 56)//
                | ((bytes[counter + 1] & 0xff) << 48) //
                | ((bytes[counter + 2] & 0xff) << 40) //
                | ((bytes[counter + 3] & 0xff) << 32) //
                | ((bytes[counter + 4] & 0xff) << 24) //
                | ((bytes[counter + 5] & 0xff) << 16) //
                | ((bytes[counter + 6] & 0xff) << 8) //
                | ((bytes[counter + 7] & 0xff));//
        value = Double.longBitsToDouble(longBits);
        counter += 8;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }

    public double getValue()
    {
        return value;
    }
}
