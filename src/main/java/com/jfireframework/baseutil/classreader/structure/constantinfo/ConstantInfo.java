package com.jfireframework.baseutil.classreader.structure.constantinfo;

import com.jfireframework.baseutil.classreader.util.ConstantType;

public abstract class ConstantInfo
{
    protected ConstantType type;

    /**
     * 该常量类型解析除了tag字节以外的内容，并且返回解析完成后计数器的值
     *
     * @param bytes
     * @param counter
     * @return
     */
    public abstract int resolve(byte[] bytes, int counter);

    /**
     * 使用常量池中的数据解析一些描述字符串链接等
     *
     * @param constant_pool
     */
    public abstract void resolve(ConstantInfo[] constant_pool);
}
