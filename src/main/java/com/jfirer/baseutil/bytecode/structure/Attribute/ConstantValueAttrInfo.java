package com.jfirer.baseutil.bytecode.structure.Attribute;

import com.jfirer.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfirer.baseutil.bytecode.util.BinaryData;

public class ConstantValueAttrInfo extends AttributeInfo
{
    //定长2个字节描述的常量池索引
    private int index;

    public ConstantValueAttrInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(BinaryData binaryData, ConstantInfo[] constantInfos)
    {
        binaryData.addIndex(length);
    }
}
