package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

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
        long longBits = (((long) bytes[counter] & 0xffL) << 56)//
                | (((long) bytes[counter + 1] & 0xffL) << 48) //
                | (((long) bytes[counter + 2] & 0xffL) << 40) //
                | (((long) bytes[counter + 3] & 0xffL) << 32) //
                | (((long) bytes[counter + 4] & 0xffL) << 24) //
                | (((long) bytes[counter + 5] & 0xffL) << 16) //
                | (((long) bytes[counter + 6] & 0xffL) << 8) //
                | (((long) bytes[counter + 7] & 0xffL));//
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
