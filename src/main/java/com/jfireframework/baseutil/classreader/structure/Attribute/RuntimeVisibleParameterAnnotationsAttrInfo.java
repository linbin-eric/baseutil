package com.jfireframework.baseutil.classreader.structure.Attribute;

import com.jfireframework.baseutil.classreader.structure.AnnotationInfo;
import com.jfireframework.baseutil.classreader.structure.constantinfo.ConstantInfo;

import java.util.Arrays;

public class RuntimeVisibleParameterAnnotationsAttrInfo extends AttributeInfo
{
    private int num_parameters;
    private ParameterAnnotation[] parameterAnnotations;

    public RuntimeVisibleParameterAnnotationsAttrInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        num_parameters = bytes[counter];
        counter++;
        parameterAnnotations = new ParameterAnnotation[num_parameters];
        for (int i = 0; i < parameterAnnotations.length; i++)
        {
            parameterAnnotations[i] = new ParameterAnnotation();
            counter = parameterAnnotations[i].resolve(bytes, counter, constantInfos);
        }
    }

    class ParameterAnnotation
    {
        private int num_annotations;
        private AnnotationInfo[] annotationInfos;

        int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
        {
            num_annotations = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            annotationInfos = new AnnotationInfo[num_annotations];
            for (int i = 0; i < annotationInfos.length; i++)
            {
                annotationInfos[i] = new AnnotationInfo();
                counter = annotationInfos[i].resolve(bytes, counter, constantInfos);
            }
            return counter;
        }

        @Override
        public String toString()
        {
            return "ParameterAnnotation{" + "annotationInfos=" + Arrays.toString(annotationInfos) + '}';
        }
    }

    @Override
    public String toString()
    {
        return "RuntimeVisibleParameterAnnotationsAttrInfo{" + "parameterAnnotations=" + Arrays.toString(parameterAnnotations) + '}';
    }
}
