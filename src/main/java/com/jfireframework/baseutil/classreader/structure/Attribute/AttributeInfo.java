package com.jfireframework.baseutil.classreader.structure.Attribute;

import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.classreader.structure.constantinfo.Utf8Info;

public abstract class AttributeInfo
{
    //2个字节的索引
    protected String name;
    //4个字节的bytes长度
    protected int length;

    public AttributeInfo(String name, int length)
    {
        this.name = name;
        this.length = length;
    }

    public int getLength()
    {
        return length;
    }

    protected abstract void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos);

    public static AttributeInfo parse(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        int nameIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        int length = ((bytes[counter] & 0xff) << 24)//
                | ((bytes[counter + 1] & 0xff) << 16)//
                | ((bytes[counter + 2] & 0xff) << 8)//
                | ((bytes[counter + 3] & 0xff) << 0);
        counter += 4;
        String name = ((Utf8Info) constantInfos[nameIndex - 1]).getValue();
        AttributeInfo info;
        if ( "RuntimeVisibleAnnotations".equals(name) )
        {
            info = new RuntimeVisibleAnnotationsAttriInfo(name, length);
        }
        else if ( "AnnotationDefault".equals(name) )
        {
            info = new AnnotationDefaultAttriInfo(name, length);
        }
        else if ( "Code".equals(name) )
        {
            info = new CodeAttriInfo(name, length);
        }
        else if ( "LocalVariableTable".equals(name) )
        {
            info = new LocalVariableTableAttriInfo(name, length);
        }
        else
        {
            info = new UnknowAttriInfo(name, length);
        }
        info.resolve(bytes, counter, constantInfos);
        return info;
    }

    @Override
    public String toString()
    {
        return "AttributeInfo{" + "name='" + name + '\'' + ", length=" + length + '}';
    }
}
