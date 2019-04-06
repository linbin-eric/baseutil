package com.jfireframework.baseutil.bytecode.support;

import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.List;

public interface AnnotationContext
{
    boolean isAnnotationPresent(Class<? extends Annotation> ckass);

    <E extends Annotation> E getAnnotation(Class<E> ckass);

    List<Annotation> getAnnotations(Class<? extends Annotation> ckass);

    AnnotationMetadata getAnnotationMetadata(Class<? extends Annotation> ckass);
}
