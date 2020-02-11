package com.jfirer.baseutil.bytecode.annotation;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

public interface AnnotationMetadata
{

    String RetentionName  = Retention.class.getName().replace('.', '/');
    String DocumentedName = Documented.class.getName().replace('.', '/');
    String TargetName     = Target.class.getName().replace('.', '/');

    /**
     * 内置的几个元注解需要被忽略，否则会无限循环。
     *
     * @return
     */
    boolean shouldIgnore();

    /**
     * 返回该注解实例的指定属性
     *
     * @return
     */
    ValuePair getAttribyte(String name);

    /**
     * 返回该注解的Class对象
     *
     * @return
     */
    Class<?> annotationType();

    /**
     * 返回该实例是否为某一个注解的实例
     *
     * @param name 格式为aa/bb/cc
     * @return
     */
    boolean isAnnotation(String name);

    /**
     * 返回该注解类型的资源名，格式为aa/bb/cc
     *
     * @return
     */
    String type();

    /**
     * 返回注解在该注解上的注解实例
     *
     * @return
     */
    List<AnnotationMetadata> getPresentAnnotations();

    /**
     * 通过JDK动态代理技术，生成一个该注解的实例。
     * 注意:如果该注解是一个非法注解，那么在生成实例的时候会抛出异常
     * 非法注解的情况包括：
     * * 1）该注解实例的类不在classpath中；
     * * 2）该注解某一个属性是枚举类型或枚举数组类型，且该枚举类型的类不在classpath中
     * * 3）该注解某一个属性是Class类型且定义了默认值，而该默认值的类不在classpath中
     * * 4）该注解的某个属性是class类型的数组且定义了默认值，而默认值中的某个元素的类不在classpath中
     *
     * @return
     */
    Annotation annotation();
}
