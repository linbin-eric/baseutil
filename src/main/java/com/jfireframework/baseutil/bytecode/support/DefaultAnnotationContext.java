package com.jfireframework.baseutil.bytecode.support;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.List;

public class DefaultAnnotationContext implements AnnotationContext
{
    private List<AnnotationMetadata> metadataList;

    public DefaultAnnotationContext(List<AnnotationMetadata> metadataList)
    {
        this.metadataList = metadataList;
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> ckass)
    {
        String resourceName = ckass.getName().replace('.', '/');
        for (AnnotationMetadata annotationMetadata : metadataList)
        {
            if (find(annotationMetadata, resourceName) != null)
            {
                return true;
            }
        }
        return false;
    }

    private AnnotationMetadata find(AnnotationMetadata metadata, String resourceName)
    {
        if (metadata.isAnnotation(resourceName))
        {
            return metadata;
        }
        for (AnnotationMetadata presentAnnotation : metadata.getPresentAnnotations())
        {
            AnnotationMetadata result = find(presentAnnotation, resourceName);
            if (result != null)
            {
                return result;
            }
        }
        return null;
    }

    @Override
    public Annotation getAnnotation(Class<? extends Annotation> ckass)
    {
        return getAnnotationMetadata(ckass).annotation();
    }

    @Override
    public List<Annotation> getAnnotations(Class<? extends Annotation> ckass)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public AnnotationMetadata getAnnotationMetadata(Class<? extends Annotation> ckass)
    {
        String resourceName = ckass.getName().replace('.', '/');
        for (AnnotationMetadata each : metadataList)
        {
            AnnotationMetadata metadata = find(each, resourceName);
            if (metadata != null)
            {
                return metadata;
            }
        }
        return null;
    }
}
