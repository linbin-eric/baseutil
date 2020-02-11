package com.jfirer.baseutil.bytecode.structure.Attribute;

import com.jfirer.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfirer.baseutil.bytecode.util.BinaryData;

public class ExceptionsAttriInfo extends AttributeInfo
{
    private int   number_of_exceptions;
    /**
     * exception_index_table列表每项为CONSTANT_Class常量项的索引，表示具体的异常类
     */
    private int[] exception_index_table;

    public ExceptionsAttriInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(BinaryData binaryData, ConstantInfo[] constantInfos)
    {
        ignoreParse(binaryData);
    }
}
