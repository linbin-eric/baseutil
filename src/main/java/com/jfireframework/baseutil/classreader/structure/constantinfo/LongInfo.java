package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class LongInfo extends ConstantInfo
{

    private long value;

    public LongInfo()
    {
        type = ConstantType.Long;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        value = ((bytes[counter] & 0xff) << 56)//
                | ((bytes[counter + 1] & 0xff) << 48) //
                | ((bytes[counter + 2] & 0xff) << 40) //
                | ((bytes[counter + 3] & 0xff) << 32) //
                | ((bytes[counter + 4] & 0xff) << 24) //
                | ((bytes[counter + 5] & 0xff) << 16) //
                | ((bytes[counter + 6] & 0xff) << 8) //
                | ((bytes[counter + 7] & 0xff));//
        counter += 8;
        return counter;
    }

    public long getValue()
    {
        return value;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
