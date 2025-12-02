package xin.nb1.baseutil.bytecode.structure.Attribute;

import xin.nb1.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import xin.nb1.baseutil.bytecode.util.BinaryData;

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