package com.jfireframework.baseutil.classreader.structure.Attribute;

import com.jfireframework.baseutil.classreader.structure.AnnotationInfo;
import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;

import java.util.Arrays;

public class RuntimeVisibleAnnotationsAttriInfo extends AttributeInfo
{
    private int num_annotations;
    private AnnotationInfo[] annotations;

    public RuntimeVisibleAnnotationsAttriInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    public String toString()
    {
        return "RuntimeVisibleAnnotationsAttriInfo{" + "num_annotations=" + num_annotations + ", annotations=" + Arrays.toString(annotations) + '}';
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        num_annotations = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        annotations = new AnnotationInfo[num_annotations];
        for (int i = 0; i < num_annotations; i++)
        {
            annotations[i] = new AnnotationInfo();
            counter = annotations[i].resolve(bytes, counter, constantInfos);
        }
    }

    public AnnotationInfo[] getAnnotations()
    {
        return annotations;
    }
}
