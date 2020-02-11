package com.jfirer.baseutil.bytecode.structure.constantinfo;

import com.jfirer.baseutil.bytecode.util.ConstantType;

public class InterfaceMethodRefInfo extends RefInfo
{
    public InterfaceMethodRefInfo()
    {
        type = ConstantType.InterfaceMethodref;
    }
}
