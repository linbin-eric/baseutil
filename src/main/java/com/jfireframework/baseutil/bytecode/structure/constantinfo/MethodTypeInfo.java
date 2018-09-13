package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

public class MethodTypeInfo extends ConstantInfo
{
    /**
     * descriptor_index为CONSTANT_Utf8类型常量项的索引，里面存储了方法描述符的字符串
     */
    private int descriptorIndex;

    public MethodTypeInfo()
    {
        type = ConstantType.MethodType;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        descriptorIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
