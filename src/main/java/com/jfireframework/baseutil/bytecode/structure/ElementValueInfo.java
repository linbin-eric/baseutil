package com.jfireframework.baseutil.bytecode.structure;

import com.jfireframework.baseutil.bytecode.structure.constantinfo.*;
import com.jfireframework.baseutil.reflect.ReflectUtil;

import java.util.Arrays;

public class ElementValueInfo
{
    private char tag;
    private ElementValueType elementValueType;
    private ConstantValue constantValue;
    private EnumConstant enumConstant;
    private String classname;
    private AnnotationInfo annotationInfo;
    private int num_values;
    private ElementValueInfo[] elementValueInfos;

    public int resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        tag = (char) bytes[counter];
        counter++;
        elementValueType = resolveType(tag);
        if ( isPrimitive(elementValueType) || elementValueType == ElementValueType.STRING )
        {
            int const_value_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            ConstantInfo constantInfo = constantInfos[const_value_index - 1];
            if ( constantInfo instanceof IntegerInfo )
            {
                constantValue = new ConstantValue(elementValueType, ((IntegerInfo) constantInfo).getValue());
            }
            else if ( constantInfo instanceof FloatInfo )
            {
                constantValue = new ConstantValue(elementValueType, ((FloatInfo) constantInfo).getValue());
            }
            else if ( constantInfo instanceof LongInfo )
            {
                constantValue = new ConstantValue(elementValueType, ((LongInfo) constantInfo).getValue());
            }
            else if ( constantInfo instanceof DoubleInfo )
            {
                constantValue = new ConstantValue(elementValueType, ((DoubleInfo) constantInfo).getValue());
            }
            else if ( elementValueType == ElementValueType.STRING )
            {
                constantValue = new ConstantValue(elementValueType, ((Utf8Info) constantInfo).getValue());
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
        else if ( elementValueType == ElementValueType.ENUM )
        {
            int type_name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            int const_name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            String typeName = ((Utf8Info) constantInfos[type_name_index - 1]).getValue();
            String enumName = ((Utf8Info) constantInfos[const_name_index - 1]).getValue();
            enumConstant = new EnumConstant(typeName, enumName);
        }
        else if ( elementValueType == ElementValueType.CLASS )
        {
            int class_info_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            classname = ((Utf8Info) constantInfos[class_info_index - 1]).getValue();
        }
        else if ( elementValueType == ElementValueType.ANNOTATION )
        {
            annotationInfo = new AnnotationInfo();
            counter = annotationInfo.resolve(bytes, counter, constantInfos);
        }
        else if ( elementValueType == ElementValueType.ARRAY )
        {
            num_values = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            elementValueInfos = new ElementValueInfo[num_values];
            for (int i = 0; i < num_values; i++)
            {
                elementValueInfos[i] = new ElementValueInfo();
                counter = elementValueInfos[i].resolve(bytes, counter, constantInfos);
            }
        }
        return counter;
    }

    public boolean isPrimitive(ElementValueType type)
    {
        if ( type == elementValueType.BYTE //
                || type == elementValueType.CHAR//
                || type == elementValueType.DOUBLE//
                || type == elementValueType.FLOAT//
                || type == elementValueType.INT//
                || type == elementValueType.LONG//
                || type == elementValueType.SHORT//
                || type == elementValueType.BOOLEAN//
        )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    ElementValueType resolveType(char c)
    {
        switch (c)
        {
            case 'B':
                return ElementValueType.BYTE;
            case 'C':
                return ElementValueType.CHAR;
            case 'D':
                return ElementValueType.DOUBLE;
            case 'F':
                return ElementValueType.FLOAT;
            case 'I':
                return ElementValueType.INT;
            case 'J':
                return ElementValueType.LONG;
            case 'S':
                return ElementValueType.SHORT;
            case 'Z':
                return ElementValueType.BOOLEAN;
            case 's':
                return ElementValueType.STRING;
            case 'e':
                return ElementValueType.ENUM;
            case 'c':
                return ElementValueType.CLASS;
            case '@':
                return ElementValueType.ANNOTATION;
            case '[':
                return ElementValueType.ARRAY;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString()
    {
        return "ElementValueInfo{" + "tag=" + tag + ", elementValueType=" + elementValueType + ", constantValue=" + constantValue + ", enumConstant=" + enumConstant + ", classname='" + classname + '\'' + ", annotationInfo=" + annotationInfo + ", num_values=" + num_values + ", elementValueInfos=" + Arrays.toString(elementValueInfos) + '}';
    }

    public ElementValueType getElementValueType()
    {
        return elementValueType;
    }

    public ConstantValue getConstantValue()
    {
        return constantValue;
    }

    public EnumConstant getEnumConstant()
    {
        return enumConstant;
    }

    public String getClassname()
    {
        return classname;
    }

    public AnnotationInfo getAnnotationInfo()
    {
        return annotationInfo;
    }

    public int getNum_values()
    {
        return num_values;
    }

    public ElementValueInfo[] getElementValueInfos()
    {
        return elementValueInfos;
    }

    public Object getValue(ClassLoader classLoader)
    {
        switch (elementValueType)
        {
            case BYTE:
                byte b = (byte) constantValue.getIntValue();
                return b;
            case CHAR:
                char c = (char) constantValue.getIntValue();
                return c;
            case DOUBLE:
                double doubleValue = constantValue.getDoubleValue();
                return doubleValue;
            case FLOAT:
                float floatValue = constantValue.getFloatValue();
                return floatValue;
            case INT:
                int intValue = constantValue.getIntValue();
                return intValue;
            case LONG:
                long longValue = constantValue.getLongValue();
                return longValue;
            case SHORT:
                short shortValue = (short) constantValue.getIntValue();
                return shortValue;
            case BOOLEAN:
                boolean booleanValue = constantValue.getIntValue() > 0;
                return booleanValue;
            case STRING:
                String stringValue = constantValue.getStringValue();
                return stringValue;
            case ENUM:
                String typeName = enumConstant.getTypeName();
                String enumName = enumConstant.getEnumName();
                return typeName.replace('/', '.')+":"+enumName;
            case CLASS:
                return classname.substring(1,classname.length()-1).replace('/', '.');
            case ANNOTATION:
                return annotationInfo.getAnnotationAttributes(classLoader).getAttributes();
            case ARRAY:
                Object[] array = new Object[elementValueInfos.length];
                for (int i = 0; i < array.length; i++)
                {
                    array[i] = elementValueInfos[i].getValue(classLoader);
                }
                return array;
            default:
                throw new IllegalArgumentException();
        }
    }
}
