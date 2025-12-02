package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.BinaryData;
import xin.nb1.baseutil.bytecode.util.ConstantType;

public class LongInfo extends ConstantInfo
{
    private long value;

    public LongInfo()
    {
        type = ConstantType.Long;
    }

    @Override
    public void resolve(BinaryData binaryData)
    {
        value = binaryData.readLong();
    }

    public long getValue()
    {
        return value;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
