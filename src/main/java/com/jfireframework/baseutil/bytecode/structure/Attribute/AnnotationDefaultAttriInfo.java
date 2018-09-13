package com.jfireframework.baseutil.bytecode.structure.Attribute;

import com.jfireframework.baseutil.bytecode.structure.ElementValueInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;

public class AnnotationDefaultAttriInfo extends AttributeInfo
{
    public AnnotationDefaultAttriInfo(String name, int length)
    {
        super(name, length);
    }

    private ElementValueInfo elementValueInfo;

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        elementValueInfo = new ElementValueInfo();
        elementValueInfo.resolve(bytes, counter, constantInfos);
    }

    @Override
    public String toString()
    {
        return "AnnotationDefaultAttriInfo{" + "elementValueInfo=" + elementValueInfo + '}';
    }

    public ElementValueInfo getElementValueInfo()
    {
        return elementValueInfo;
    }
}
