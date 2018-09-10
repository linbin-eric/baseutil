package com.jfireframework.baseutil.classreader.structure.Attribute;

import com.jfireframework.baseutil.classreader.structure.ExceptionHandler;
import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;

public class CodeAttriInfo extends AttributeInfo
{
    //2字节长度
    private int max_stack;
    //2字节长度
    private int max_locals;
    private int code_length;
    private byte[] code;
    private int exception_table_length;
    private ExceptionHandler[] exceptionHandlers;
    private int attributes_count;
    private AttributeInfo[] attributeInfos;

    public CodeAttriInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
    }
}
