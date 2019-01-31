package com.jfireframework.baseutil.bytecode.annotation;

import java.util.List;
import java.util.Map;

public class ClassNotExistAnnotationMetadata implements AnnotationMetadata
{
    private String type;

    public ClassNotExistAnnotationMetadata(String type)
    {
        this.type = type;
    }

    @Override
    public boolean isValid()
    {
        return false;
    }

    @Override
    public Map<String, ValuePair> getAttributes()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAnnotation(String name)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String type()
    {
        return type;
    }

    @Override
    public List<AnnotationMetadata> getPresentAnnotations()
    {
        throw new UnsupportedOperationException();
    }
}
