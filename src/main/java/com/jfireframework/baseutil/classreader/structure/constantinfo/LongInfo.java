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
       value = (((long)bytes[counter] & 0xffL) << 56)//
                | (((long)bytes[counter + 1] & 0xffL) << 48) //
                | (((long)bytes[counter + 2] & 0xffL) << 40) //
                | (((long)bytes[counter + 3] & 0xffL) << 32) //
                | (((long)bytes[counter + 4] & 0xffL) << 24) //
                | (((long)bytes[counter + 5] & 0xffL) << 16) //
                | (((long)bytes[counter + 6] & 0xffL) << 8) //
                | (((long)bytes[counter + 7] & 0xffL));//
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
