package cc.jfire.baseutil.bytecode.structure.constantinfo;

import cc.jfire.baseutil.bytecode.util.ConstantType;

public class FieldRefInfo extends RefInfo
{
    public FieldRefInfo()
    {
        type = ConstantType.FieldRef;
    }
}
