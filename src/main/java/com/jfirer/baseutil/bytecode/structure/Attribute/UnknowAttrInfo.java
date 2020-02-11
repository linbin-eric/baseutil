package com.jfirer.baseutil.bytecode.structure.Attribute;

import com.jfirer.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfirer.baseutil.bytecode.util.BinaryData;

public class UnknowAttrInfo extends AttributeInfo
{
    public UnknowAttrInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(BinaryData binaryData, ConstantInfo[] constantInfos)
    {
        ignoreParse(binaryData);
    }
}