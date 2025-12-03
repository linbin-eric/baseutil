package cc.jfire.baseutil.bytecode.structure.constantinfo;

import cc.jfire.baseutil.bytecode.util.ConstantType;

public class MethodRefInfo extends RefInfo
{
    public MethodRefInfo()
    {
        type = ConstantType.MethodRef;
    }

    @Override
    public String toString()
    {
        return "MethodRefInfo{" + "classInfoIndex=" + classInfoIndex + ", nameAndTypeInfoIndex=" + nameAndTypeInfoIndex + '}';
    }
}
