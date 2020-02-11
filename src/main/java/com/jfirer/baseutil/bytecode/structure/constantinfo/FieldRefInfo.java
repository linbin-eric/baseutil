package com.jfirer.baseutil.bytecode.structure.constantinfo;

import com.jfirer.baseutil.bytecode.util.ConstantType;

public class FieldRefInfo extends RefInfo
{
    public FieldRefInfo()
    {
        type = ConstantType.FieldRef;
    }
}
