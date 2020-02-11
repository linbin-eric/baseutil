package com.jfirer.baseutil.bytecode.structure.Attribute;

import com.jfirer.baseutil.bytecode.structure.ElementValueInfo;
import com.jfirer.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfirer.baseutil.bytecode.util.BinaryData;

public class AnnotationDefaultAttriInfo extends AttributeInfo
{
    private ElementValueInfo elementValueInfo;

    public AnnotationDefaultAttriInfo(String name, int length)
    {
        super(name, length);
    }

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
