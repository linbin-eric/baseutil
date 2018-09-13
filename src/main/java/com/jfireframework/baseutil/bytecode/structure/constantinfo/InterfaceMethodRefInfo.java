package com.jfireframework.baseutil.bytecode.structure.constantinfo;

import com.jfireframework.baseutil.bytecode.util.ConstantType;

public class InterfaceMethodRefInfo extends RefInfo
{
    public InterfaceMethodRefInfo()
    {
        type = ConstantType.InterfaceMethodref;
    }
}
