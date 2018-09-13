package com.jfireframework.baseutil.bytecode.structure.Attribute;

import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;

public class ConstantValueAttrInfo extends AttributeInfo
{
    //定长2个字节描述的常量池索引
    private int index;

    public ConstantValueAttrInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
    }
}
