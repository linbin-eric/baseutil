package com.jfirer.baseutil.bytecode.annotation;

import com.jfirer.baseutil.bytecode.ClassFile;
import com.jfirer.baseutil.bytecode.ClassFileParser;
import com.jfirer.baseutil.bytecode.structure.AnnotationInfo;
import com.jfirer.baseutil.bytecode.structure.Attribute.AttributeInfo;
import com.jfirer.baseutil.bytecode.structure.Attribute.RuntimeVisibleAnnotationsAttriInfo;
import com.jfirer.baseutil.bytecode.structure.MethodInfo;
import com.jfirer.baseutil.bytecode.support.OverridesAttribute;
import com.jfirer.baseutil.bytecode.util.BytecodeUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SupportOverrideAttributeAnnotationMetadata extends AbstractAnnotationMetadata
{
    static  String                          name = OverridesAttribute.class.getName().replace('.', '/');
    private Map<String, List<OverrideItem>> map  = new HashMap<String, List<OverrideItem>>();

    private SupportOverrideAttributeAnnotationMetadata(String resourceName, Map<String, ValuePair> attributes, ClassLoader loader)
    {
        super(resourceName, attributes, loader);
        byte[]    bytecode  = BytecodeUtil.loadBytecode(loader, resourceName);
        ClassFile classFile = new ClassFileParser(bytecode).parse();
        for (MethodInfo methodInfo : classFile.getMethodInfos())
        {
            for (AttributeInfo attributeInfo : methodInfo.getAttributeInfos())
            {
                if (attributeInfo instanceof RuntimeVisibleAnnotationsAttriInfo)
                {
                    for (AnnotationInfo annotation : ((RuntimeVisibleAnnotationsAttriInfo) attributeInfo).getAnnotations())
                    {
                        if (annotation.getType().equals(name))
                        {
                            AnnotationMetadata overrideAttribute      = annotation.getAnnotation(loader);
                            ValuePair          valuePair              = overrideAttribute.getAttribyte("annotation");
                            String             annotationResourceName = valuePair.getClassName().replace('.', '/');
                            String             name                   = overrideAttribute.getAttribyte("name").getStringValue();
                            OverrideItem       overrideItem           = new OverrideItem();
                            overrideItem.overrideAnnotationName = annotationResourceName;
                            overrideItem.overrideAttribute = name;
                            overrideItem.attribute = methodInfo.getName();
                            overrideItem.valuePair = attributes.get(methodInfo.getName());
                            List<OverrideItem> overrideItems = map.get(annotationResourceName);
                            if (overrideItems == null)
                            {
                                overrideItems = new LinkedList<OverrideItem>();
                                map.put(annotationResourceName, overrideItems);
                            }
                            overrideItems.add(overrideItem);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    public static SupportOverrideAttributeAnnotationMetadata castFrom(DefaultAnnotationMetadata defaultAnnotationMetadata)
    {
        return new SupportOverrideAttributeAnnotationMetadata(defaultAnnotationMetadata.getResourceName(), defaultAnnotationMetadata.getAttributes(), defaultAnnotationMetadata.getLoader());
    }

    @Override
    public List<AnnotationMetadata> getPresentAnnotations()
    {
        if (presentAnnotations == null)
        {
            List<AnnotationMetadata> tmp = BytecodeUtil.findAnnotationsOnClass(type(), this.getClass().getClassLoader());
            presentAnnotations = new LinkedList<AnnotationMetadata>();
            for (AnnotationMetadata annotationMetadata : tmp)
            {
                if (annotationMetadata instanceof DefaultAnnotationMetadata == false)
                {
                    throw new UnsupportedOperationException();
                }
                SupportOverrideAttributeAnnotationMetadata castFrom = castFrom((DefaultAnnotationMetadata) annotationMetadata);
                if (map.containsKey(castFrom.getResourceName()))
                {
                    List<OverrideItem> overrideItems = map.get(castFrom.getResourceName());
                    for (OverrideItem each : overrideItems)
                    {
                        castFrom.attributes.put(each.overrideAttribute, each.valuePair);
                    }
                }
                presentAnnotations.add(castFrom);
            }
        }
        return presentAnnotations;
    }

    class OverrideItem
    {
        String    overrideAnnotationName;
        String    overrideAttribute;
        String    attribute;
        ValuePair valuePair;
    }
}
