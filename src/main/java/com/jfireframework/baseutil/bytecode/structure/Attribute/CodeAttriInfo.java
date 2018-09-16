package com.jfireframework.baseutil.bytecode.structure.Attribute;

import com.jfireframework.baseutil.bytecode.structure.ExceptionHandler;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;

import java.util.Arrays;

public class CodeAttriInfo extends AttributeInfo
{
    //2字节长度
    private int max_stack;
    //2字节长度
    private int max_locals;
    private int code_length;
    private ExceptionHandler[] exceptionHandlers;
    private AttributeInfo[] attributeInfos;

    public CodeAttriInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        max_stack = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        max_locals = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        code_length = ((bytes[counter] & 0xff) << 24)//
                | ((bytes[counter + 1] & 0xff) << 16)//
                | ((bytes[counter + 2] & 0xff) << 8)//
                | ((bytes[counter + 3] & 0xff));
        counter += 4;
        //由于目前不做解析，因此忽略掉这部分code数据
        counter += code_length;
        int exception_table_length = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        //目前不解析异常表信息，忽略这部分数据
        counter += exception_table_length * 8;
        int attributes_count = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        attributeInfos = new AttributeInfo[attributes_count];
        for (int i = 0; i < attributeInfos.length; i++)
        {
            attributeInfos[i] = AttributeInfo.parse(bytes, counter, constantInfos);
            counter += 2 + 4 + attributeInfos[i].getLength();
        }
    }

    public AttributeInfo[] getAttributeInfos()
    {
        return attributeInfos;
    }

    @Override
    public String toString()
    {
        return "CodeAttriInfo{" + "max_stack=" + max_stack + ", max_locals=" + max_locals + ", code_length=" + code_length + ", exceptionHandlers=" + Arrays.toString(exceptionHandlers) + ", attributeInfos=" + Arrays.toString(attributeInfos) + '}';
    }
}
