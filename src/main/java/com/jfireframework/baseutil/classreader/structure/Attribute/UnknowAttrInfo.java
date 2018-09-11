package com.jfireframework.baseutil.classreader.structure.Attribute;

import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;

public class UnknowAttrInfo extends AttributeInfo
{
    public UnknowAttrInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
    }
}
