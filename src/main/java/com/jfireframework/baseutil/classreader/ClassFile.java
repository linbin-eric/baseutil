package com.jfireframework.baseutil.classreader;

import com.jfireframework.baseutil.classreader.structure.AnnotationInfo;
import com.jfireframework.baseutil.classreader.structure.Attribute.AttributeInfo;
import com.jfireframework.baseutil.classreader.structure.Attribute.RuntimeVisibleAnnotationsAttriInfo;
import com.jfireframework.baseutil.classreader.structure.FieldInfo;
import com.jfireframework.baseutil.classreader.structure.MethodInfo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ClassFile
{
    private int minor_version;
    private int major_version;
    private int access_flags;
    private String this_class_name;
    private String super_class_name;
    private String[] interfaces;
    private FieldInfo[] fieldInfos;
    private MethodInfo[] methodInfos;
    private AttributeInfo[] attributeInfos;
    private Map<String, Map<String, Object>> annotations;

    void setFieldInfos(FieldInfo[] fieldInfos)
    {
        this.fieldInfos = fieldInfos;
    }

    void setMethodInfos(MethodInfo[] methodInfos)
    {
        this.methodInfos = methodInfos;
    }

    void setAttributeInfos(AttributeInfo[] attributeInfos)
    {
        this.attributeInfos = attributeInfos;
    }

    void setInterfaces(String[] interfaces)
    {
        this.interfaces = new String[interfaces.length];
        for (int i = 0; i < this.interfaces.length; i++)
        {
            String value = interfaces[i];
            if ( value.indexOf('/') != -1 )
            {
                value = value.replace('/', '.');
            }
            this.interfaces[i] = value;
        }
    }

    void setThis_class_name(String this_class_name)
    {
        if ( this_class_name.indexOf('/') != -1 )
        {
            this_class_name = this_class_name.replace('/', '.');
        }
        this.this_class_name = this_class_name;
    }

    void setSuper_class_name(String super_class_name)
    {
        if ( super_class_name.indexOf('/') != -1 )
        {
            super_class_name = super_class_name.replace('/', '.');
        }
        this.super_class_name = super_class_name;
    }

    public int getMinor_version()
    {
        return minor_version;
    }

    void setMinor_version(int minor_version)
    {
        this.minor_version = minor_version;
    }

    public int getMajor_version()
    {
        return major_version;
    }

    void setMajor_version(int major_version)
    {
        this.major_version = major_version;
    }

    public void setAccess_flags(int access_flags)
    {
        this.access_flags = access_flags;
    }

    public Map<String, Map<String, Object>> getAnnotations(ClassLoader classLoader)
    {
        if ( annotations != null )
        {
            return annotations;
        }
        RuntimeVisibleAnnotationsAttriInfo runtimeVisibleAnnotationsAttriInfo = null;
        for (AttributeInfo attributeInfo : attributeInfos)
        {
            if ( attributeInfo instanceof RuntimeVisibleAnnotationsAttriInfo )
            {
                runtimeVisibleAnnotationsAttriInfo = (RuntimeVisibleAnnotationsAttriInfo) attributeInfo;
                break;
            }
        }
        if ( runtimeVisibleAnnotationsAttriInfo == null || runtimeVisibleAnnotationsAttriInfo.getAnnotations().length == 0 )
        {
            annotations = Collections.emptyMap();
            return annotations;
        }
        annotations = new HashMap<String, Map<String, Object>>();
        for (AnnotationInfo info : runtimeVisibleAnnotationsAttriInfo.getAnnotations())
        {
            String type = info.getType();
            Map<String, Object> annotationAttributes = info.getAnnotationAttributes(classLoader);
            annotations.put(type, annotationAttributes);
        }
        return annotations;
    }

    public MethodInfo[] getMethodInfos()
    {
        return methodInfos;
    }
}
