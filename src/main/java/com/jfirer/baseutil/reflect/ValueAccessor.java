package com.jfirer.baseutil.reflect;

import com.jfirer.baseutil.smc.SmcHelper;
import com.jfirer.baseutil.smc.compiler.CompileHelper;
import com.jfirer.baseutil.smc.model.ClassModel;
import com.jfirer.baseutil.smc.model.MethodModel;
import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ValueAccessor
{
    private static final int           _UNSAFE       = 0;
    private static final int           _FIELD        = 1;
    protected static final int           INT           = 1;
    protected static final int           BYTE          = 2;
    protected static final int           CHAR          = 3;
    protected static final int           BOOLEAN       = 4;
    protected static final int           SHORT         = 5;
    protected static final int           LONG          = 6;
    protected static final int           FLOAT         = 7;
    protected static final int           DOUBLE        = 8;
    protected static final AtomicInteger count         = new AtomicInteger();
    protected            Field         field;
    private              long          offset;
    protected            boolean       primitive;
    protected            int           primitiveType = 0;
    private              Unsafe        unsafe        = Unsafe.getUnsafe();

    public ValueAccessor()
    {
    }

    public ValueAccessor(Field field)
    {
        this.field = field;
        primitive = field.getType().isPrimitive();
        offset = unsafe.objectFieldOffset(field);
        if (primitive)
        {
            Class<?> type = field.getType();
            if (type == int.class)
            {
                primitiveType = INT;
            }
            else if (type == short.class)
            {
                primitiveType = SHORT;
            }
            else if (type == long.class)
            {
                primitiveType = LONG;
            }
            else if (type == float.class)
            {
                primitiveType = FLOAT;
            }
            else if (type == double.class)
            {
                primitiveType = DOUBLE;
            }
            else if (type == boolean.class)
            {
                primitiveType = BOOLEAN;
            }
            else if (type == byte.class)
            {
                primitiveType = BYTE;
            }
            else if (type == char.class)
            {
                primitiveType = CHAR;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    }

    static String toMethodName(Field field)
    {
        return field.getName().substring(0, 1).toUpperCase() + field.getName().substring(1);
    }

    public static ValueAccessor constructor(Class ckass, CompileHelper compileHelper)
    {
        ClassModel classModel = new ClassModel("ValueAccessor_constructor_" + ckass.getSimpleName() + "_" + count.getAndIncrement(), ValueAccessor.class);
        try
        {
            boolean               hasZeroParamConstructor = false;
            Map<Integer, Class[]> map                     = new HashMap<Integer, Class[]>();
            for (Constructor constructor : ckass.getConstructors())
            {
                Class[] parameterTypes = constructor.getParameterTypes();
                if (parameterTypes.length == 0)
                {
                    hasZeroParamConstructor = true;
                    continue;
                }
                else
                {
                    if (map.containsKey(parameterTypes.length))
                    {
                        throw new IllegalArgumentException("类：" + ckass.getName() + "存在相同入参个数的重载构造方法，当前只支持入参个数不同的重载构造方法");
                    }
                    map.put(parameterTypes.length, parameterTypes);
                }
            }
            if (hasZeroParamConstructor)
            {
                Method      method      = ValueAccessor.class.getDeclaredMethod("newInstace");
                MethodModel methodModel = new MethodModel(method, classModel);
                methodModel.setBody("return new " + SmcHelper.getReferenceName(ckass, classModel) + "();\r\n");
                classModel.putMethodModel(methodModel);
            }
            if (map.isEmpty() == false)
            {
                StringBuilder body = new StringBuilder();
                for (Map.Entry<Integer, Class[]> each : map.entrySet())
                {
                    body.append("if(params.length==" + each.getKey() + "){\r\n");
                    body.append("\treturn new " + SmcHelper.getReferenceName(ckass, classModel) + "(");
                    Class[] value = each.getValue();
                    for (int i = 0; i < value.length; i++)
                    {
                        body.append("(").append(SmcHelper.getReferenceName(value[i], classModel)).append(")params[").append(i).append("],");
                    }
                    body.setLength(body.length() - 1);
                    body.append(");\r\n");
                    body.append("}\r\n");
                }
                Method      method      = ValueAccessor.class.getDeclaredMethod("newInstance", Object[].class);
                MethodModel methodModel = new MethodModel(method, classModel);
                methodModel.setParamterNames("params");
                methodModel.setBody(body.toString());
                classModel.putMethodModel(methodModel);
            }
            return (ValueAccessor) compileHelper.compile(classModel).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }

    public static ValueAccessor create(Field field, CompileHelper compileHelper)
    {
        ClassModel classModel = new ClassModel("ValueAccessor_" + field.getName() + "_" + count.getAndIncrement(), ValueAccessor.class);
        Class<?>   type       = field.getType();
        if (type == int.class || type == Integer.class)
        {
            return build(field, compileHelper, classModel, "getInt", int.class, Integer.class);
        }
        else if (type == short.class || type == Short.class)
        {
            return build(field, compileHelper, classModel, "getShort", short.class, Short.class);
        }
        else if (type == long.class || type == Long.class)
        {
            return build(field, compileHelper, classModel, "getLong", long.class, Long.class);
        }
        else if (type == float.class || type == Float.class)
        {
            return build(field, compileHelper, classModel, "getFloat", float.class, Float.class);
        }
        else if (type == double.class || type == Double.class)
        {
            return build(field, compileHelper, classModel, "getDouble", double.class, Double.class);
        }
        else if (type == boolean.class || type == Boolean.class)
        {
            return build(field, compileHelper, classModel, "getBoolean", boolean.class, Boolean.class);
        }
        else if (type == byte.class || type == Byte.class)
        {
            return build(field, compileHelper, classModel, "getByte", byte.class, Byte.class);
        }
        else if (type == char.class || type == Character.class)
        {
            return build(field, compileHelper, classModel, "getChar", char.class, Character.class);
        }
        else
        {
            try
            {
                Method      method      = ValueAccessor.class.getDeclaredMethod("get", Object.class);
                MethodModel methodModel = new MethodModel(method, classModel);
                methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
                classModel.putMethodModel(methodModel);
                method = ValueAccessor.class.getDeclaredMethod("setObject", Object.class, Object.class);
                methodModel = new MethodModel(method, classModel);
                methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")$1);");
                classModel.putMethodModel(methodModel);
                return (ValueAccessor) compileHelper.compile(classModel).getDeclaredConstructor().newInstance();
            }
            catch (Exception e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
    }

    private static ValueAccessor build(Field field, CompileHelper compileHelper, ClassModel classModel, String getMethodName, Class<?> C1, Class<?> C2)
    {
        try
        {
            overrideGetMethod(field, classModel, getMethodName);
            overrideGetMethod(field, classModel, getMethodName + "Object");
            overrideGetMethod(field, classModel, "get");
            overrideSetMethod(field, classModel, "set", C1);
            overrideSetMethod(field, classModel, "set", C2);
            overrideSetMethod(field, classModel, "setObject", Object.class);
            return (ValueAccessor) compileHelper.compile(classModel).getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
        }
        return null;
    }

    private static void overrideSetMethod(Field field, ClassModel classModel, String setMethodName, Class paramType) throws NoSuchMethodException
    {
        Method      method      = ValueAccessor.class.getDeclaredMethod(setMethodName, Object.class, paramType);
        MethodModel methodModel = new MethodModel(method, classModel);
        if (paramType == Object.class)
        {
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "((" + SmcHelper.getReferenceName(field.getType(), classModel) + ")$1);");
        }
        else
        {
            methodModel.setBody("((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).set" + toMethodName(field) + "($1);");
        }
        classModel.putMethodModel(methodModel);
    }

    private static void overrideGetMethod(Field field, ClassModel classModel, String getMethodName) throws NoSuchMethodException
    {
        Method      method      = ValueAccessor.class.getDeclaredMethod(getMethodName, Object.class);
        MethodModel methodModel = new MethodModel(method, classModel);
        if (field.getType() != boolean.class)
        {
            methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).get" + toMethodName(field) + "();");
        }
        else
        {
            methodModel.setBody("return ((" + SmcHelper.getReferenceName(field.getDeclaringClass(), classModel) + ")$0).is" + toMethodName(field) + "();");
        }
        classModel.putMethodModel(methodModel);
    }

    public void set(Object entity, int value)
    {
        if (primitive)
        {
            unsafe.putInt(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Integer.valueOf(value));
        }
    }

    public void set(Object entity, Integer value)
    {
        if (primitive)
        {
            unsafe.putInt(entity, offset, value.intValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, short value)
    {
        if (primitive)
        {
            unsafe.putShort(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Short.valueOf(value));
        }
    }

    public void set(Object entity, Short value)
    {
        if (primitive)
        {
            unsafe.putShort(entity, offset, value.shortValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, long value)
    {
        if (primitive)
        {
            unsafe.putLong(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Long.valueOf(value));
        }
    }

    public void set(Object entity, Long value)
    {
        if (primitive)
        {
            unsafe.putLong(entity, offset, value.longValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, char value)
    {
        if (primitive)
        {
            unsafe.putChar(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Character.valueOf(value));
        }
    }

    public void set(Object entity, Character value)
    {
        if (primitive)
        {
            unsafe.putChar(entity, offset, value.charValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, byte value)
    {
        if (primitive)
        {
            unsafe.putByte(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Byte.valueOf(value));
        }
    }

    public void set(Object entity, Byte value)
    {
        if (primitive)
        {
            unsafe.putByte(entity, offset, value.byteValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, boolean value)
    {
        if (primitive)
        {
            unsafe.putBoolean(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Boolean.valueOf(value));
        }
    }

    public void set(Object entity, Boolean value)
    {
        if (primitive)
        {
            unsafe.putBoolean(entity, offset, value.booleanValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, float value)
    {
        if (primitive)
        {
            unsafe.putFloat(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Float.valueOf(value));
        }
    }

    public void set(Object entity, Float value)
    {
        if (primitive)
        {
            unsafe.putFloat(entity, offset, value.floatValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void set(Object entity, double value)
    {
        if (primitive)
        {
            unsafe.putDouble(entity, offset, value);
        }
        else
        {
            unsafe.putReference(entity, offset, Double.valueOf(value));
        }
    }

    public void set(Object entity, Double value)
    {
        if (primitive)
        {
            unsafe.putDouble(entity, offset, value.doubleValue());
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public void setObject(Object entity, Object value)
    {
        if (primitive)
        {
            switch (primitiveType)
            {
                case INT:
                    unsafe.putInt(entity, offset, ((Number) value).intValue());
                    break;
                case SHORT:
                    unsafe.putShort(entity, offset, ((Number) value).shortValue());
                    break;
                case LONG:
                    unsafe.putLong(entity, offset, ((Number) value).longValue());
                    break;
                case FLOAT:
                    unsafe.putFloat(entity, offset, ((Number) value).floatValue());
                    break;
                case DOUBLE:
                    unsafe.putDouble(entity, offset, ((Number) value).doubleValue());
                    break;
                case BOOLEAN:
                    unsafe.putBoolean(entity, offset, ((Boolean) value).booleanValue());
                    break;
                case BYTE:
                    unsafe.putByte(entity, offset, ((Number) value).byteValue());
                    break;
                case CHAR:
                    unsafe.putChar(entity, offset, ((Character) value).charValue());
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public int getInt(Object entity)
    {
        try
        {
            return primitive ? unsafe.getInt(entity, offset) : (Integer) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Integer getIntObject(Object entity)
    {
        try
        {
            return primitive ? Integer.valueOf(unsafe.getInt(entity, offset)) : (Integer) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public short getShort(Object entity)
    {
        try
        {
            return primitive ? unsafe.getShort(entity, offset) : (Short) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Short getShortObject(Object entity)
    {
        try
        {
            return primitive ? Short.valueOf(unsafe.getShort(entity, offset)) : (Short) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public boolean getBoolean(Object entity)
    {
        try
        {
            return primitive ? unsafe.getBoolean(entity, offset) : (Boolean) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public Boolean getBooleanObject(Object entity)
    {
        try
        {
            return primitive ? Boolean.valueOf(unsafe.getBoolean(entity, offset)) : (Boolean) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public long getLong(Object entity)
    {
        try
        {
            return primitive ? unsafe.getLong(entity, offset) : (Long) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Long getLongObject(Object entity)
    {
        try
        {
            return primitive ? Long.valueOf(unsafe.getLong(entity, offset)) : (Long) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0L;
        }
    }

    public byte getByte(Object entity)
    {
        try
        {
            return primitive ? unsafe.getByte(entity, offset) : (Byte) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Byte getByteObject(Object entity)
    {
        try
        {
            return primitive ? Byte.valueOf(unsafe.getByte(entity, offset)) : (Byte) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public char getChar(Object entity)
    {
        try
        {
            return primitive ? unsafe.getChar(entity, offset) : (Character) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Character getCharObject(Object entity)
    {
        try
        {
            return primitive ? Character.valueOf(unsafe.getChar(entity, offset)) : (Character) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public float getFloat(Object entity)
    {
        try
        {
            return primitive ? unsafe.getFloat(entity, offset) : (Float) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Float getFloatObject(Object entity)
    {
        try
        {
            return primitive ? Float.valueOf(unsafe.getFloat(entity, offset)) : (Float) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0F;
        }
    }

    public double getDouble(Object entity)
    {
        try
        {
            return primitive ? unsafe.getDouble(entity, offset) : (Double) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Double getDoubleObject(Object entity)
    {
        try
        {
            return primitive ? Double.valueOf(unsafe.getDouble(entity, offset)) : (Double) unsafe.getReference(entity, offset);
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0D;
        }
    }

    public Object get(Object entity)
    {
        if (primitive)
        {
            switch (primitiveType)
            {
                case INT:
                    return Integer.valueOf(unsafe.getInt(entity, offset));
                case SHORT:
                    return Short.valueOf(unsafe.getShort(entity, offset));
                case LONG:
                    return Long.valueOf(unsafe.getLong(entity, offset));
                case FLOAT:
                    return Float.valueOf(unsafe.getFloat(entity, offset));
                case DOUBLE:
                    return Double.valueOf(unsafe.getDouble(entity, offset));
                case BOOLEAN:
                    return Boolean.valueOf(unsafe.getBoolean(entity, offset));
                case BYTE:
                    return Byte.valueOf(unsafe.getByte(entity, offset));
                case CHAR:
                    return Character.valueOf(unsafe.getChar(entity, offset));
                default:
                    throw new UnsupportedOperationException();
            }
        }
        else
        {
            return unsafe.getReference(entity, offset);
        }
    }

    public Field getField()
    {
        return field;
    }

    public Object newInstace()
    {
        throw new IllegalStateException("还未创建ValueAccessor构造器实例");
    }

    public Object newInstance(Object... params)
    {
        throw new IllegalStateException("还未创建ValueAccessor构造器实例");
    }
}
