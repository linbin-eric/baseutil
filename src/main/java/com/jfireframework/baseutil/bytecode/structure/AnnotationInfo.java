package com.jfireframework.baseutil.bytecode.structure;

import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadata;
import com.jfireframework.baseutil.bytecode.annotation.AnnotationMetadataImpl;
import com.jfireframework.baseutil.bytecode.annotation.UnValidAnnotationMetadata;
import com.jfireframework.baseutil.bytecode.structure.Attribute.AnnotationDefaultAttriInfo;
import com.jfireframework.baseutil.bytecode.structure.Attribute.AttributeInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.Utf8Info;
import com.jfireframework.baseutil.bytecode.util.BytecodeUtil;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnnotationInfo
{
    private String type;
    private element_value_pairs[] pairs;

    public class element_value_pairs
    {
        private String elementName;
        private ElementValueInfo value;

        public int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
        {
            int element_name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            elementName = ((Utf8Info) constantInfos[element_name_index - 1]).getValue();
            value = new ElementValueInfo();
            counter = value.resolve(bytes, counter, constantInfos);
            return counter;
        }

        public String getElementName()
        {
            return elementName;
        }

        public ElementValueInfo getValue()
        {
            return value;
        }

        @Override
        public String toString()
        {
            return "element_value_pairs{" + "elementName='" + elementName + '\'' + ", value=" + value + '}';
        }
    }

    public int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        int type_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        type = ((Utf8Info) constantInfos[type_index - 1]).getValue();
        if (type.startsWith("L"))
        {
            type = type.substring(1, type.length() - 1);
        }
        int num_element_value_pairs = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        pairs = new element_value_pairs[num_element_value_pairs];
        for (int i = 0; i < num_element_value_pairs; i++)
        {
            pairs[i] = new element_value_pairs();
            counter = pairs[i].resolve(bytes, counter, constantInfos);
        }
        return counter;
    }

    @Override
    public String toString()
    {
        return "AnnotationInfo{" + "type='" + type + '\'' + ", pairs=" + Arrays.toString(pairs) + '}';
    }

    public String getType()
    {
        return type;
    }

    public element_value_pairs[] getPairs()
    {
        return pairs;
    }

    public AnnotationMetadata getAnnotationAttributes(ClassLoader classLoader)
    {
        Map<String, Object> elementValues = new HashMap<String, Object>();
        byte[] bytes = BytecodeUtil.loadBytecode(classLoader, type);
        if (bytes == null)
        {
            return new UnValidAnnotationMetadata(type);
        }
        ClassFile annotationClassFile = new ClassFileParser(bytes).parse();
        for (MethodInfo methodInfo : annotationClassFile.getMethodInfos())
        {
            for (AttributeInfo attributeInfo : methodInfo.getAttributeInfos())
            {
                if (attributeInfo instanceof AnnotationDefaultAttriInfo)
                {
                    elementValues.put(methodInfo.getName(), ((AnnotationDefaultAttriInfo) attributeInfo).getElementValueInfo().getValue(classLoader));
                    break;
                }
            }
        }
        for (element_value_pairs pair : pairs)
        {
            String name = pair.getElementName();
            ElementValueInfo value = pair.getValue();
            elementValues.put(name, value.getValue(classLoader));
        }
        return new AnnotationMetadataImpl(type, elementValues, classLoader);
    }

}
