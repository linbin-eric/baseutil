package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.ReflectUtil;
import cc.jfire.baseutil.reflect.valueaccessor.ValueAccessor;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;

public class MapToObj
{
    /**
     * componentType 是数组元素的类型
     *
     * @param componentType
     * @param list
     * @return
     */
    private static Object toArray(Type componentType, List list) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Class<?> rawClass = ReflectUtil.getRawClass(componentType);
        return switch (ReflectUtil.getClassId(rawClass))
        {
            case ReflectUtil.PRIMITIVE_BYTE ->
            {
                byte[] array = new byte[list.size()];
                int    i     = 0;
                for (Object o : list)
                {
                    array[i++] = Byte.parseByte((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_INT ->
            {
                int[] array = new int[list.size()];
                int   i     = 0;
                for (Object o : list)
                {
                    array[i++] = Integer.parseInt((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_SHORT ->
            {
                short[] array = new short[list.size()];
                int     i     = 0;
                for (Object o : list)
                {
                    array[i++] = Short.parseShort((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_LONG ->
            {
                long[] array = new long[list.size()];
                int    i     = 0;
                for (Object o : list)
                {
                    array[i++] = Long.parseLong((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_FLOAT ->
            {
                float[] array = new float[list.size()];
                int     i     = 0;
                for (Object o : list)
                {
                    array[i++] = Float.parseFloat((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_DOUBLE ->
            {
                double[] array = new double[list.size()];
                int      i     = 0;
                for (Object o : list)
                {
                    array[i++] = Double.parseDouble((String) o);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_CHAR ->
            {
                char[] array = new char[list.size()];
                int    i     = 0;
                for (Object o : list)
                {
                    array[i++] = ((String) o).charAt(0);
                }
                yield array;
            }
            case ReflectUtil.PRIMITIVE_BOOL ->
            {
                boolean[] array = new boolean[list.size()];
                int       i     = 0;
                for (Object o : list)
                {
                    array[i++] = Boolean.parseBoolean((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_BYTE ->
            {
                Byte[] array = new Byte[list.size()];
                int    i     = 0;
                for (Object o : list)
                {
                    array[i++] = Byte.parseByte((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_INT ->
            {
                Integer[] array = new Integer[list.size()];
                int       i     = 0;
                for (Object o : list)
                {
                    array[i++] = Integer.parseInt((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_SHORT ->
            {
                Short[] array = new Short[list.size()];
                int     i     = 0;
                for (Object o : list)
                {
                    array[i++] = Short.parseShort((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_LONG ->
            {
                Long[] array = new Long[list.size()];
                int    i     = 0;
                for (Object o : list)
                {
                    array[i++] = Long.parseLong((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_FLOAT ->
            {
                Float[] array = new Float[list.size()];
                int     i     = 0;
                for (Object o : list)
                {
                    array[i++] = Float.parseFloat((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_DOUBLE ->
            {
                Double[] array = new Double[list.size()];
                int      i     = 0;
                for (Object o : list)
                {
                    array[i++] = Double.parseDouble((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_CHAR ->
            {
                Character[] array = new Character[list.size()];
                int         i     = 0;
                for (Object o : list)
                {
                    array[i++] = ((String) o).charAt(0);
                }
                yield array;
            }
            case ReflectUtil.CLASS_BOOL ->
            {
                Boolean[] array = new Boolean[list.size()];
                int       i     = 0;
                for (Object o : list)
                {
                    array[i++] = Boolean.parseBoolean((String) o);
                }
                yield array;
            }
            case ReflectUtil.CLASS_STRING ->
            {
                String[] array = new String[list.size()];
                int      i     = 0;
                for (Object o : list)
                {
                    array[i++] = (String) o;
                }
                yield array;
            }
            case ReflectUtil.CLASS_ENUM ->
            {
                Enum<?>[] array = new Enum[list.size()];
                int       i     = 0;
                for (Object o : list)
                {
                    array[i++] = Enum.valueOf((Class<? extends Enum>) componentType, (String) o);
                }
                yield array;
            }
            default ->
            {
                int    length = list.size();
                Object arr    = Array.newInstance(rawClass, length);
                if (componentType instanceof Class<?>)
                {
                    if (Collection.class.isAssignableFrom(rawClass))
                    {
                        for (int i = 0; i < length; i++)
                        {
                            Array.set(arr, i, list.get(i));
                        }
                    }
                    else if (Map.class.isAssignableFrom(rawClass))
                    {
                        for (int i = 0; i < length; i++)
                        {
                            Array.set(arr, i, list.get(i));
                        }
                    }
                    else if (rawClass.isArray())
                    {
                        for (int i = 0; i < length; i++)
                        {
                            Array.set(arr, i, toArray(rawClass.getComponentType(), (List) list.get(i)));
                        }
                    }
                    else
                    {
                        for (int i = 0; i < length; i++)
                        {
                            Array.set(arr, i, toObj(componentType, (Map<String, Object>) list.get(i)));
                        }
                    }
                }
                yield arr;
            }
        };
    }

    public static Object toObj(Type type, Map<String, Object> map) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException
    {
        Class                              rawClass       = null;
        Type[]                             typeArguments  = null;
        TypeVariable<? extends Class<?>>[] typeParameters = null;
        if (type instanceof ParameterizedType parameterizedType)
        {
            // 获取原始类型（Person 类）
            rawClass = (Class<?>) parameterizedType.getRawType();
            // 获取泛型参数（T 的具体类型）
            typeArguments  = parameterizedType.getActualTypeArguments();
            typeParameters = rawClass.getTypeParameters();
        }
        else if (type instanceof Class<?> clazz)
        {
            // 非泛型类型，直接使用
            rawClass = clazz;
        }
        Object  instance = rawClass.getConstructor().newInstance();
        Field[] fields   = ReflectUtil.findPojoBeanSetFields(rawClass);
        for (Field each : fields)
        {
            Type          fieldGenericType = each.getGenericType();
            ValueAccessor valueAccessor    = ValueAccessor.standard(each);
            if (fieldGenericType instanceof TypeVariable<?> typeVariable)
            {
                fieldGenericType = parseFieldArgumentType(typeVariable, typeParameters, typeArguments);
            }
            if (fieldGenericType instanceof Class<?> ckass)
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
                    case ReflectUtil.PRIMITIVE_BOOLEAN_ARRAY -> valueAccessor.setReference(instance, toArray(boolean.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_INT_ARRAY -> valueAccessor.setReference(instance, toArray(int.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_SHORT_ARRAY -> valueAccessor.setReference(instance, toArray(short.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_LONG_ARRAY -> valueAccessor.setReference(instance, toArray(long.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_FLOAT_ARRAY -> valueAccessor.setReference(instance, toArray(float.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_DOUBLE_ARRAY -> valueAccessor.setReference(instance, toArray(double.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_CHAR_ARRAY -> valueAccessor.setReference(instance, toArray(char.class, (List) map.get(each.getName())));
                    case ReflectUtil.PRIMITIVE_BYTE_ARRAY -> valueAccessor.setReference(instance, toArray(byte.class, (List) map.get(each.getName())));
                    default ->
                    {
                        if (ckass.isArray())
                        {
                            valueAccessor.setReference(instance, toArray(ckass.getComponentType(), (List) map.get(each.getName())));
                        }
                        else
                        {
                            if (List.class.isAssignableFrom(ckass))
                            {
                                List list = (List) map.get(each.getName());
                                valueAccessor.setReference(instance, list);
                            }
                            else if (Set.class.isAssignableFrom(ckass))
                            {
                                List list = (List) map.get(each.getName());
                                valueAccessor.setReference(instance, new HashSet(list));
                            }
                            else if (Map.class.isAssignableFrom(ckass))
                            {
                                Map o = (Map) map.get(each.getName());
                                valueAccessor.setReference(instance, o);
                            }
                            else
                            {
                                valueAccessor.setReference(instance, toObj(fieldGenericType, (Map<String, Object>) map.get(each.getName())));
                            }
                        }
                    }
                }
            }
            else if (fieldGenericType instanceof ParameterizedType parameterizedType)
            {
                Type rawType = parameterizedType.getRawType();
                if (Collection.class.isAssignableFrom((Class<?>) rawType))
                {
                    valueAccessor.setReference(instance, fromCollection(parameterizedType.getActualTypeArguments()[0], (List) map.get(each.getName()), Set.class.isAssignableFrom((Class<?>) rawType), typeParameters, typeArguments));
                }
                else if (Map.class.isAssignableFrom((Class<?>) rawType))
                {
                    valueAccessor.setReference(instance, toMap(parameterizedType.getActualTypeArguments()[1], (Map<String, Object>) map.get(each.getName()), typeParameters, typeArguments));
                }
                else
                {
                    valueAccessor.setReference(instance, toObj(fieldGenericType, (Map<String, Object>) map.get(each.getName())));
                }
            }
            else if (fieldGenericType instanceof GenericArrayType genericArrayType)
            {
                valueAccessor.setReference(instance, toArray(fieldGenericType, (List) map.get(each.getName())));
            }
        }
        return instance;
    }

    /**
     * type 是 List 中的元素类型
     *
     * @param type
     * @param o
     * @param typeParameters
     * @param typeArguments
     * @return
     */
    private static Collection fromCollection(Type type, List o, boolean isSet, TypeVariable<? extends Class<?>>[] typeParameters, Type[] typeArguments) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        if (type instanceof Class<?> clazz)
        {
            List list = new LinkedList();
            switch (ReflectUtil.getClassId(clazz))
            {
                case ReflectUtil.CLASS_INT ->
                {
                    for (Object element : o)
                    {
                        list.add(Integer.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_SHORT ->
                {
                    for (Object element : o)
                    {
                        list.add(Short.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_LONG ->
                {
                    for (Object element : o)
                    {
                        list.add(Long.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_FLOAT ->
                {
                    for (Object element : o)
                    {
                        list.add(Float.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_DOUBLE ->
                {
                    for (Object element : o)
                    {
                        list.add(Double.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_CHAR ->
                {
                    for (Object element : o)
                    {
                        list.add(Character.valueOf(((String) element).charAt(0)));
                    }
                }
                case ReflectUtil.CLASS_BOOL ->
                {
                    for (Object element : o)
                    {
                        list.add(Boolean.valueOf(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_STRING ->
                {
                    for (Object element : o)
                    {
                        list.add(element);
                    }
                }
                case ReflectUtil.CLASS_BIGDECIMAL ->
                {
                    for (Object element : o)
                    {
                        list.add(new BigDecimal(((String) element)));
                    }
                }
                case ReflectUtil.CLASS_ENUM ->
                {
                    for (Object element : o)
                    {
                        list.add(Enum.valueOf((Class<? extends Enum>) clazz, (String) element));
                    }
                }
                default ->
                {
                    for (Object element : o)
                    {
                        list.add(toObj(type, (Map<String, Object>) element));
                    }
                }
            }
            return isSet ? new HashSet(list) : list;
        }
        else if (type instanceof WildcardType)
        {
            return isSet ? new HashSet(o) : o;
        }
        else if (type instanceof TypeVariable<?>)
        {
            return fromCollection(parseFieldArgumentType((TypeVariable<?>) type, typeParameters, typeArguments), o, isSet, typeParameters, typeArguments);
        }
        else if (type instanceof ParameterizedType parameterizedType)
        {
            Type   rawType             = parameterizedType.getRawType();
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            if (Collection.class.isAssignableFrom((Class<?>) rawType))
            {
                boolean isInnerSet  = Set.class.isAssignableFrom((Class<?>) rawType);
                Type    elementType = null;
                if (actualTypeArguments[0] instanceof Class<?> clazz)
                {
                    elementType = actualTypeArguments[0];
                }
                else if (actualTypeArguments[0] instanceof TypeVariable<?> typeVariable)
                {
                    elementType = parseFieldArgumentType(typeVariable, typeParameters, typeArguments);
                }
                else if (actualTypeArguments[0] instanceof WildcardType)
                {
                    elementType = actualTypeArguments[0];
                }
                else if (actualTypeArguments[0] instanceof ParameterizedType)
                {
                    elementType = actualTypeArguments[0];
                }
                else if (actualTypeArguments[0] instanceof GenericArrayType)
                {
                    elementType = actualTypeArguments[0];
                }
                List list = new LinkedList();
                for (Object element : o)
                {
                    list.add(fromCollection(elementType, (List) element, isInnerSet, typeParameters, typeArguments));
                }
                return isSet ? new HashSet(list) : list;
            }
            else if (Map.class.isAssignableFrom((Class<?>) rawType))
            {
                List list = new LinkedList();
                for (Object element : o)
                {
                    list.add(toMap(actualTypeArguments[1], (Map) element, typeParameters, typeArguments));
                }
                return isSet ? new HashSet(list) : list;
            }
            else
            {
                List list = new LinkedList();
                for (Object element : o)
                {
                    list.add(toObj(parameterizedType, (Map) element));
                }
                return isSet ? new HashSet(list) : list;
            }
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    private static Map<?, ?> toMap(Type valueType, Map obj, TypeVariable<? extends Class<?>>[] typeParameters, Type[] typeArguments) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException
    {
        Map<Object, Object> map = null;
        if (valueType instanceof Class<?> clazz)
        {
            map = new HashMap<>();
            switch (ReflectUtil.getClassId(clazz))
            {
                case ReflectUtil.CLASS_INT ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Integer.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_BYTE ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Byte.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_SHORT ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Short.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_LONG ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Long.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_FLOAT ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Float.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_DOUBLE ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Double.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_CHAR ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), ((String) ((Map.Entry<Object, Object>) entry).getValue()).charAt(0));
                    }
                }
                case ReflectUtil.CLASS_BOOL ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Boolean.valueOf(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_STRING ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), ((Map.Entry<Object, Object>) entry).getValue());
                    }
                }
                case ReflectUtil.CLASS_BIGDECIMAL ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), new BigDecimal(((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                case ReflectUtil.CLASS_ENUM ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), Enum.valueOf((Class<? extends Enum>) clazz, ((String) ((Map.Entry<Object, Object>) entry).getValue())));
                    }
                }
                default ->
                {
                    for (Object entry : obj.entrySet())
                    {
                        map.put(((Map.Entry<Object, Object>) entry).getKey(), toObj(clazz, (Map) ((Map.Entry<Object, Object>) entry).getValue()));
                    }
                }
            }
        }
        else if (valueType instanceof WildcardType)
        {
            map = obj;
        }
        else if (valueType instanceof TypeVariable<?>)
        {
            map = (Map<Object, Object>) toMap(parseFieldArgumentType((TypeVariable<?>) valueType, typeParameters, typeArguments), obj, typeParameters, typeArguments);
        }
        else if (valueType instanceof ParameterizedType parameterizedType)
        {
            Type rawType = parameterizedType.getRawType();
            map = new HashMap<>();
            if (Collection.class.isAssignableFrom((Class<?>) rawType))
            {
                Type    elementType = parameterizedType.getActualTypeArguments()[0];
                boolean isSet       = Set.class.isAssignableFrom((Class<?>) rawType);
                for (Object entry : obj.entrySet())
                {
                    List element = (List) ((Map.Entry<Object, Object>) entry).getValue();
                    map.put(((Map.Entry<Object, Object>) entry).getKey(), fromCollection(elementType, element, isSet, typeParameters, typeArguments));
                }
            }
            else if (Map.class.isAssignableFrom((Class<?>) rawType))
            {
                Type elementType = parameterizedType.getActualTypeArguments()[1];
                for (Object entry : obj.entrySet())
                {
                    Map element = (Map) ((Map.Entry) entry).getValue();
                    map.put(((Map.Entry) entry).getKey(), toMap(elementType, element, typeParameters, typeArguments));
                }
            }
            else
            {
                for (Object entry : obj.entrySet())
                {
                    Map element = (Map) ((Map.Entry) entry).getValue();
                    map.put(((Map.Entry) entry).getKey(), toObj(parameterizedType, element));
                }
            }
        }
        return map;
    }

    private static Type parseFieldArgumentType(TypeVariable<?> typeVariable, TypeVariable[] typeParameters, Type[] typeArguments)
    {
        Type   argumentType;
        String name = typeVariable.getName();
        for (int i = 0; i < typeParameters.length; i++)
        {
            if (typeParameters[i].getName().equals(name))
            {
                argumentType = typeArguments[i];
                return argumentType;
            }
        }
        return null;
    }
}
