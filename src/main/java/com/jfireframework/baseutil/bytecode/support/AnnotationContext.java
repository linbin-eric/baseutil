package com.jfireframework.baseutil.bytecode.support;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationContext
{
    boolean isAnnotationPresent(Class<? extends Annotation> ckass);

    <E extends Annotation> E getAnnotation(Class<E> ckass);

    <E extends Annotation> List<E> getAnnotations(Class<E> ckass);

    AnnotationMetadata getAnnotationMetadata(Class<? extends Annotation> ckass);

    List<AnnotationMetadata> getAnnotationMetadatas(Class<? extends Annotation> ckass);
}
