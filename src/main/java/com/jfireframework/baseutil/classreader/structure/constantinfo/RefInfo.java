package com.jfireframework.baseutil.classreader.structure.constantinfo;

public abstract class RefInfo extends ConstantInfo
{
    protected int classInfoIndex;
    protected int nameAndTypeInfoIndex;
    protected ClassInfo classInfo;

    @Override
    public int resolve(byte[] bytes, int counter)
    {
        classInfoIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        nameAndTypeInfoIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        return counter;
    }

    @Override
    public void resolve(ConstantInfo[] constant_pool)
    {
        classInfo = (ClassInfo) constant_pool[classInfoIndex-1];
    }

}
