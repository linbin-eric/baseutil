package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;
import org.apache.poi.ss.formula.functions.T;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class MapToObj
{
    public static T from(Map<String, Object> map, Class<T> type) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Field[] pojoBeanSetFields = ReflectUtil.findPojoBeanSetFields(type);
        T       instance          = type.getConstructor().newInstance();
        for (Field each : pojoBeanSetFields)
        {
            boolean       b             = map.containsKey(each.getName());
            ValueAccessor valueAccessor = ValueAccessor.standard(each);
            Type          genericType   = each.getGenericType();
            if (genericType instanceof Class<?> ckass)
            {
                switch (ReflectUtil.getClassId(ckass))
                {
                    case ReflectUtil.PRIMITIVE_BYTE -> valueAccessor.set(instance, Byte.parseByte(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_INT -> valueAccessor.set(instance, Integer.parseInt(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_SHORT -> valueAccessor.set(instance, Short.parseShort(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_LONG -> valueAccessor.set(instance, Long.parseLong(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_FLOAT -> valueAccessor.set(instance, Float.parseFloat(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_DOUBLE -> valueAccessor.set(instance, Double.parseDouble(((String) map.get(each.getName()))));
                    case ReflectUtil.PRIMITIVE_CHAR -> valueAccessor.set(instance, ((String) map.get(each.getName())).charAt(0));
                    case ReflectUtil.PRIMITIVE_BOOL -> valueAccessor.set(instance, Boolean.parseBoolean(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_INT -> valueAccessor.setReference(instance, Integer.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_SHORT -> valueAccessor.setReference(instance, Short.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_LONG -> valueAccessor.setReference(instance, Long.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_FLOAT -> valueAccessor.setReference(instance, Float.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_DOUBLE -> valueAccessor.setReference(instance, Double.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_BYTE -> valueAccessor.setReference(instance, Byte.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_CHAR -> valueAccessor.setReference(instance, ((String) map.get(each.getName())).charAt(0));
                    case ReflectUtil.CLASS_BOOL -> valueAccessor.setReference(instance, Boolean.valueOf(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_STRING -> valueAccessor.setReference(instance, (String) map.get(each.getName()));
                    case ReflectUtil.CLASS_BIGDECIMAL -> valueAccessor.setReference(instance, new BigDecimal(((String) map.get(each.getName()))));
                    case ReflectUtil.CLASS_ENUM -> valueAccessor.setReference(instance, Enum.valueOf((Class<? extends Enum>) ckass, (String) map.get(each.getName())));
                    default -> {}
                }
            }
        }
    }

    record PathInfo(String path, Type type, ValueAccessor valueAccessor, int classId)
    {
    }

    private static void parse(Object instance, PathInfo pathInfo, Object ymlValue)
    {
        ValueAccessor valueAccessor = pathInfo.valueAccessor;
        if (pathInfo.type instanceof Class<?> ckass)
        {
            switch (pathInfo.classId)
            {
                case ReflectUtil.PRIMITIVE_BYTE -> valueAccessor.set(instance, Byte.parseByte(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_INT -> valueAccessor.set(instance, Integer.parseInt(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_SHORT -> valueAccessor.set(instance, Short.parseShort(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_LONG -> valueAccessor.set(instance, Long.parseLong(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_FLOAT -> valueAccessor.set(instance, Float.parseFloat(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_DOUBLE -> valueAccessor.set(instance, Double.parseDouble(((String) ymlValue)));
                case ReflectUtil.PRIMITIVE_CHAR -> valueAccessor.set(instance, ((String) ymlValue).charAt(0));
                case ReflectUtil.PRIMITIVE_BOOL -> valueAccessor.set(instance, Boolean.parseBoolean(((String) ymlValue)));
                case ReflectUtil.CLASS_INT -> valueAccessor.setReference(instance, Integer.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_SHORT -> valueAccessor.setReference(instance, Short.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_LONG -> valueAccessor.setReference(instance, Long.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_FLOAT -> valueAccessor.setReference(instance, Float.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_DOUBLE -> valueAccessor.setReference(instance, Double.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_BYTE -> valueAccessor.setReference(instance, Byte.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_CHAR -> valueAccessor.setReference(instance, ((String) ymlValue).charAt(0));
                case ReflectUtil.CLASS_BOOL -> valueAccessor.setReference(instance, Boolean.valueOf(((String) ymlValue)));
                case ReflectUtil.CLASS_STRING -> valueAccessor.setReference(instance, (String) ymlValue);
                case ReflectUtil.CLASS_BIGDECIMAL -> valueAccessor.setReference(instance, new BigDecimal(((String) ymlValue)));
                case ReflectUtil.CLASS_ENUM -> valueAccessor.setReference(instance, Enum.valueOf((Class<? extends Enum>) pathInfo.type, (String) ymlValue));
                //数组类型，自定义的数据对象
                default ->
                {
                    if (ckass.isArray())
                    {
                        if (ymlValue instanceof List<?> == false)
                        {
                            throw new IllegalArgumentException(STR.format("字段:{}的类型和 yml 中的数据不对应", pathInfo.path));
                        }
                        Class<?> componentType = ckass.getComponentType();
                        int      innerClassId  = ReflectUtil.getClassId(componentType);
                        Object   array         = Array.newInstance(componentType, ((List<?>) ymlValue).size());
                        PathInfo innerPathInfo = new PathInfo(pathInfo.path + "[]", componentType, null, innerClassId);
                        for (int i = 0; i < ((List<?>) ymlValue).size(); i++)
                        {
                            Object each = ((List<?>) ymlValue).get(i);
                            parseArray(array, i, innerPathInfo, each);
                        }
                        valueAccessor.setReference(instance, array);
                    }
                    else
                    {
                    }
                }
            }
        }
    }

    private static void parseArray(Object array, int index, PathInfo info, Object ymlValue)
    {
        switch (info.classId)
        {
            case ReflectUtil.PRIMITIVE_BYTE -> ((byte[]) array)[index] = Byte.parseByte(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_INT -> ((int[]) array)[index]=Integer.parseInt(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_SHORT -> ((short[]) array)[index] = Short.parseShort(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_LONG -> ((long[]) array)[index] = Long.parseLong(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_FLOAT -> ((float[]) array)[index] = Float.parseFloat(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_DOUBLE -> ((double[]) array)[index] = Double.parseDouble(((String) ymlValue));
            case ReflectUtil.PRIMITIVE_CHAR -> ((char[]) array)[index] = ((String) ymlValue).charAt(0);
            case ReflectUtil.PRIMITIVE_BOOL -> ((boolean[]) array)[index] = Boolean.parseBoolean(((String) ymlValue));
            case ReflectUtil.CLASS_INT -> ((Integer[]) array)[index] = Integer.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_SHORT -> ((Short[]) array)[index] = Short.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_LONG -> ((Long[]) array)[index] = Long.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_FLOAT -> ((Float[]) array)[index] = Float.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_DOUBLE -> ((Double[]) array)[index] = Double.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_BYTE -> ((Byte[]) array)[index] = Byte.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_CHAR -> ((Character[]) array)[index] = ((String) ymlValue).charAt(0);
            case ReflectUtil.CLASS_BOOL -> ((Boolean[]) array)[index] = Boolean.valueOf(((String) ymlValue));
            case ReflectUtil.CLASS_STRING -> ((String[]) array)[index] = (String) ymlValue;

        }
    }
}
