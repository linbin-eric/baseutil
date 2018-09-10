package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class InvokeDynamicInfo extends ConstantInfo
{
    /**
     * class文件中attributes属性的索引
     */
    private int bootstrap_method_attr_index;
    private int name_and_type_index;

    public InvokeDynamicInfo()
    {
        type = ConstantType.InvokeDynamic;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        bootstrap_method_attr_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        name_and_type_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public String toString()
    {
        return "InvokeDynamicInfo{" + "bootstrap_method_attr_index=" + bootstrap_method_attr_index + ", name_and_type_index=" + name_and_type_index + '}';
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
    }
}
