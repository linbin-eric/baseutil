package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class MethodRefInfo extends RefInfo
{
    public MethodRefInfo()
    {
        type = ConstantType.MethodRef;
    }
}
