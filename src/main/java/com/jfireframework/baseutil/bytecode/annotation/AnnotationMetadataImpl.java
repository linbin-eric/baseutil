package com.jfireframework.baseutil.bytecode.annotation;

import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;

import java.util.List;
import java.util.Map;

public class AnnotationMetadataImpl implements AnnotationMetadata
{
    private String                   typeName;
    private Map<String, Object>      attributes;
    private List<AnnotationMetadata> presentAnnotations;

    public AnnotationMetadataImpl(String typeName, Map<String, Object> attributes, ClassLoader loader)
    {
        this.typeName = typeName;
        this.attributes = attributes;
    }

    @Override
    public boolean isValid()
    {
        return true;
    }

    @Override
    public Map<String, Object> getAttributes()
    {
        return attributes;
    }

    @Override
    public boolean isAnnotation(String name)
    {
        return name == null ? false : name.equals(typeName);
    }

    @Override
    public String type()
    {
        return typeName;
    }

    @Override
    public List<AnnotationMetadata> getPresentAnnotations()
    {
        if (presentAnnotations == null)
        {
           presentAnnotations = BytecodeUtil.findAnnotationsOnClass(typeName,this.getClass().getClassLoader());
        }
        return presentAnnotations;
    }

    @Override
    public String toString()
    {
        return "AnnotationMetadataImpl{" + "typeName='" + typeName + '\'' + ", attributes=" + attributes + '}';
    }
}
