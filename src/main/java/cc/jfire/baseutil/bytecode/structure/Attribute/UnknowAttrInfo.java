package cc.jfire.baseutil.bytecode.structure.Attribute;

import cc.jfire.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import cc.jfire.baseutil.bytecode.util.BinaryData;

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