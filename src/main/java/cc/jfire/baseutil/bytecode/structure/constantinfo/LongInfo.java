package cc.jfire.baseutil.bytecode.structure.constantinfo;

import cc.jfire.baseutil.bytecode.util.BinaryData;
import cc.jfire.baseutil.bytecode.util.ConstantType;

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
