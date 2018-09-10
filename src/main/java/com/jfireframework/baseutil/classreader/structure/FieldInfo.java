package com.jfireframework.baseutil.classreader.structure;

import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.classreader.structure.constantinfo.Utf8Info;
import com.jfireframework.baseutil.classreader.structure.Attribute.AttributeInfo;

import java.util.Arrays;

public class FieldInfo
{
    private int access_flags;
    private int name_index;
    private String name;
    private String descriptor;
    private AttributeInfo[] attributeInfos;

    public int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        access_flags = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
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
            counter += 2 + 4 + attributeInfos[i].getLength();
        }
        return counter;
    }

    @Override
    public String toString()
    {
        return "FieldInfo{" + "name='" + name + '\'' + ", descriptor='" + descriptor + '\'' + ", attributeInfos=" + Arrays.toString(attributeInfos) + '}';
    }
}
