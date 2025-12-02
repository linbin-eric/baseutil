package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.ConstantType;

public class FieldRefInfo extends RefInfo
{
    public FieldRefInfo()
    {
        type = ConstantType.FieldRef;
    }
}
