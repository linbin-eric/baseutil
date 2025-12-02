package xin.nb1.baseutil.bytecode.structure.Attribute;

import xin.nb1.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import xin.nb1.baseutil.bytecode.util.BinaryData;

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
