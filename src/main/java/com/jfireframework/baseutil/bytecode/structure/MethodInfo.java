package com.jfireframework.baseutil.bytecode.structure;

import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.Utf8Info;
import com.jfireframework.baseutil.bytecode.structure.Attribute.AttributeInfo;

import java.util.Arrays;

public class MethodInfo
{
    private int access_flags;
    private String name;
    private String descriptor;
    private AttributeInfo[] attributeInfos;

    public int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        access_flags = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        int name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        name = ((Utf8Info) constantInfos[name_index - 1]).getValue();
        int descriptor_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        descriptor = ((Utf8Info) constantInfos[descriptor_index - 1]).getValue();
        int attributes_count = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        attributeInfos = new AttributeInfo[attributes_count];
        for (int i = 0; i < attributes_count; i++)
        {
            attributeInfos[i] = AttributeInfo.parse(bytes, counter, constantInfos);
            counter += attributeInfos[i].getLength() + 2 + 4;
        }
        return counter;
    }

    @Override
    public String toString()
    {
        return "MethodInfo{" + "access_flags=" + access_flags + ", name='" + name + '\'' + ", descriptor='" + descriptor + '\'' + ", attributeInfos=" + Arrays.toString(attributeInfos) + '}';
    }

    public AttributeInfo[] getAttributeInfos()
    {
        return attributeInfos;
    }

    public String getName()
    {
        return name;
    }

    public String getDescriptor()
    {
        return descriptor;
    }
}
