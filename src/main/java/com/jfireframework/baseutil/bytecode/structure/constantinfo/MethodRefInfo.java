package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

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
