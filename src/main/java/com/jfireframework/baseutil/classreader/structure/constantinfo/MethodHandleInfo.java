package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class MethodHandleInfo extends ConstantInfo
{
    private int referenceKind;
    /**
     * reference_index为常量池项的索引，根据reference_kind值不同，指向不同类型的常量池项。
     * 当reference_kind为1、2、3、4时，为CONSTANT_Fieldref的索引值；
     * 当reference_kind为5、6、7、8时，为CONSTANT_Methodref的索引值；
     * 当reference_kind为9时，为CONSTANT_InterfaceMethodref的索引值。
     */
    private int referenceIndex;

    public MethodHandleInfo()
    {
        type = ConstantType.MethodHandle;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        referenceKind = bytes[counter];
        counter++;
        referenceIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
