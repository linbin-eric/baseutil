package com.jfireframework.baseutil.bytecode.annotation;

import java.util.List;
import java.util.Map;

public interface AnnotationMetadata
{
    /**
     * 是否是合法的注解。非法注解的情况就是该注解实例的类不在classpath中
     * @return
     */
    boolean isValid();
    /**
     * 返回该注解实例的所有值，以Map的形式。
     * object的实际类型可能为基本类型的包装类，String，Class，Enum，AnnotationMetadata，以及以上元素的数组
     *
     * @return
     */
    Map<String, Object> getAttributes();

    /**
     * 返回该实例是否为某一个注解的实例
     *
     * @param name 注解类的全限定名
     * @return
     */
    boolean isAnnotation(String name);

    /**
     * 返回该注解类型的全限定名
     *
     * @return
     */
    String type();

    /**
     * 返回注解在该注解上的注解实例
     * @return
     */
    List<AnnotationMetadata> getPresentAnnotations();
}
