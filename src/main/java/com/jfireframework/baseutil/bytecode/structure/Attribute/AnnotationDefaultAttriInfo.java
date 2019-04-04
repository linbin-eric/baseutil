package com.jfireframework.baseutil.bytecode.structure.Attribute;

import com.jfireframework.baseutil.bytecode.structure.ElementValueInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.bytecode.util.BinaryData;

public class AnnotationDefaultAttriInfo extends AttributeInfo
{
    public AnnotationDefaultAttriInfo(String name, int length)
    {
        super(name, length);
    }

    private ElementValueInfo elementValueInfo;

    @Override
    protected void resolve(BinaryData binaryData, ConstantInfo[] constantInfos)
    {
        elementValueInfo = new ElementValueInfo();
        elementValueInfo.resolve(binaryData, constantInfos);
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
