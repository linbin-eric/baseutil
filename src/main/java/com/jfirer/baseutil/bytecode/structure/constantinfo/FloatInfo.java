package com.jfirer.baseutil.bytecode.structure.constantinfo;

import com.jfirer.baseutil.bytecode.util.BinaryData;
import com.jfirer.baseutil.bytecode.util.ConstantType;

public class FloatInfo extends ConstantInfo
{
    private float value;

    public FloatInfo()
    {
        type = ConstantType.Float;
    }

    public float getValue()
    {
        return value;
    }

    @Override
    public void resolve(BinaryData binaryData)
    {
        int intBits = binaryData.readInt();
        value = Float.intBitsToFloat(intBits);
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
