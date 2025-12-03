package cc.jfire.baseutil.bytecode.structure.constantinfo;

import cc.jfire.baseutil.bytecode.util.BinaryData;
import cc.jfire.baseutil.bytecode.util.ConstantType;

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
