package com.jfireframework.baseutil.classreader.annotation;

import com.jfireframework.baseutil.classreader.ClassFile;
import com.jfireframework.baseutil.classreader.ClassFileParser;
import com.jfireframework.baseutil.classreader.util.BytecodeUtil;

import java.util.List;
import java.util.Map;

public class AnnotationMetadataImpl implements AnnotationMetadata
{
    private String typeName;
    private Map<String, Object> attributes;
    private List<AnnotationMetadata> presentAnnotations;

    public AnnotationMetadataImpl(String typeName, Map<String, Object> attributes, ClassLoader loader)
    {
        this.typeName = typeName;
        this.attributes = attributes;
        byte[] bytes = BytecodeUtil.loadBytecode(loader, typeName);
        ClassFile classFile = new ClassFileParser(bytes).parse();
        presentAnnotations = classFile.getAnnotations(loader);
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
        return presentAnnotations;
    }

    @Override
    public String toString()
    {
        return "AnnotationMetadataImpl{" + "typeName='" + typeName + '\'' + ", attributes=" + attributes + '}';
    }
}
