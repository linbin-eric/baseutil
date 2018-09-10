package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public class ClassInfo extends ConstantInfo
{
    private int nameIndex;
    private String name;

    public ClassInfo()
    {
        type = ConstantType.Class;
    }

    public String getName()
    {
        return name;
    }

    public void setNameIndex(int nameIndex)
    {
        this.nameIndex = nameIndex;
    }

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        nameIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public String toString()
    {
        return "ClassInfo{" + "nameIndex=" + nameIndex + ", name='" + name + '\'' + '}';
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
        name = ((Utf8Info) constant_pool[nameIndex - 1]).getValue();
    }
}
