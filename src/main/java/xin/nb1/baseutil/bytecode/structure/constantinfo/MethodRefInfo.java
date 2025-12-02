package xin.nb1.baseutil.bytecode.structure.constantinfo;

import xin.nb1.baseutil.bytecode.util.ConstantType;

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
