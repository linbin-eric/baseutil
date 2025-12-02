package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.BinaryData;
import xin.nb1.baseutil.bytecode.util.ConstantType;

public class StringInfo extends ConstantInfo
{
    private int    stringIndex;
    private String value;

    public StringInfo()
    {
        type = ConstantType.String;
    }

    public int getStringIndex()
    {
        return stringIndex;
    }

    @Override
    public void resolve(BinaryData binaryData)
    {
        stringIndex = binaryData.readShort();
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
        value = ((Utf8Info) constant_pool[stringIndex - 1]).getValue();
    }
}
