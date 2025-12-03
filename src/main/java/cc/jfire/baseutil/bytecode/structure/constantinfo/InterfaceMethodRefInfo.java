package cc.jfire.baseutil.bytecode.structure.constantinfo;

import cc.jfire.baseutil.bytecode.util.ConstantType;

public class InterfaceMethodRefInfo extends RefInfo
{
    public InterfaceMethodRefInfo()
    {
        type = ConstantType.InterfaceMethodref;
    }
}
