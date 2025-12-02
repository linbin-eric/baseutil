package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.BinaryData;
import xin.nb1.baseutil.bytecode.util.ConstantType;

public class DoubleInfo extends ConstantInfo
{
    private double value;

    public DoubleInfo()
    {
        type = ConstantType.Double;
    }

    @Override
    public void resolve(BinaryData binaryData)
    {
        long longBits = binaryData.readLong();
        this.value = Double.longBitsToDouble(longBits);
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }

    public double getValue()
    {
        return value;
    }
}
