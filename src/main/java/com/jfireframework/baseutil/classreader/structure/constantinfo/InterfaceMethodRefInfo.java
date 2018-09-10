package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class InterfaceMethodRefInfo extends RefInfo
{
    public InterfaceMethodRefInfo()
    {
        type = ConstantType.InterfaceMethodref;
    }
}
