package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.BinaryData;
import xin.nb1.baseutil.bytecode.util.ConstantType;

public class IntegerInfo extends ConstantInfo
{
    private int value;

    public IntegerInfo()
    {
        type = ConstantType.Integer;
    }

    public int getValue()
    {
        return value;
    }

    @Override
    public void resolve(BinaryData binaryData)
    {
        value = binaryData.readInt();
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
