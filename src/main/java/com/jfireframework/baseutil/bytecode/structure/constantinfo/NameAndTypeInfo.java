package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

public class NameAndTypeInfo extends ConstantInfo
{
    private int nameIndex;
    private int descriptorIndex;
    private String name;
    private String descriptor;

    public NameAndTypeInfo()
    {
        type = ConstantType.NameAndType;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        nameIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        descriptorIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
        name = ((Utf8Info) constant_pool[nameIndex - 1]).getValue();
        descriptor = ((Utf8Info) constant_pool[descriptorIndex - 1]).getValue();
    }
}
