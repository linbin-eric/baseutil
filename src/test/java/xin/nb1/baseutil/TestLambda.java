package xin.nb1.baseutil;

import xin.nb1.baseutil.reflect.ReflectUtil;
import xin.nb1.baseutil.reflect.valueaccessor.*;

import java.lang.reflect.Field;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static xin.nb1.baseutil.reflect.ReflectUtil.*;

public class TestLambda implements ValueAccessor
{
    private       Field                      field;
    private GetBoolean               getBoolean;
    private SetBoolean               setBoolean;
    private GetByte                  getByte;
    private       SetByte                    setByte;
    private       GetChar                    getChar;
    private       SetChar                    setChar;
    private       GetShort                   getShort;
    private       SetShort                   setShort;
    private       GetInt                     getInt;
    private SetInt                   setInt;
    private GetLong                  getLong;
    private SetLong                  setLong;
    private GetFloat                 getFloat;
    private SetFloat                 setFloat;
    private GetDouble                getDouble;
    private SetDouble                setDouble;
    private Function<Object, Object> getObj;
    private       BiConsumer<Object, Object> setObj;
    private final int                        classId;

    public TestLambda(Field field)
    {
        this.field = field;
        classId    = ReflectUtil.getClassId(field.getType());
        try
        {
            switch (classId)
            {
                case PRIMITIVE_INT ->
                {
                    getInt = ValueAccessor.buildGetInt(field);
                    setInt = ValueAccessor.buildSetInt(field);
                }
                case PRIMITIVE_BYTE ->
                {
                    getByte = ValueAccessor.buildGetByte(field);
                    setByte = ValueAccessor.buildSetByte(field);
                }
                case PRIMITIVE_SHORT ->
                {
                    getShort = ValueAccessor.buildGetShort(field);
                    setShort = ValueAccessor.buildSetShort(field);
                }
                case PRIMITIVE_CHAR ->
                {
                    getChar = ValueAccessor.buildGetChar(field);
                    setChar = ValueAccessor.buildSetChar(field);
                }
                case PRIMITIVE_FLOAT ->
                {
                    getFloat = ValueAccessor.buildGetFloat(field);
                    setFloat = ValueAccessor.buildSetFloat(field);
                }
                case PRIMITIVE_DOUBLE ->
                {
                    getDouble = ValueAccessor.buildGetDouble(field);
                    setDouble = ValueAccessor.buildSetDouble(field);
                }
                case PRIMITIVE_LONG ->
                {
                    getLong = ValueAccessor.buildGetLong(field);
                    setLong = ValueAccessor.buildSetLong(field);
                }
                case PRIMITIVE_BOOL ->
                {
                    getBoolean = ValueAccessor.buildGetBoolean(field);
                    setBoolean = ValueAccessor.buildSetBoolean(field);
                }
                default ->
                {
                    getObj = ValueAccessor.buildGetReference(field);
                    setObj = ValueAccessor.buildSetReference(field);
                }
            }
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getInt(Object entity)
    {
        return getInt.get(entity);
    }

    @Override
    public long getLong(Object entity)
    {
        return getLong.get(entity);
    }

    @Override
    public byte getByte(Object entity)
    {
        return getByte.get(entity);
    }

    @Override
    public short getShort(Object entity)
    {
        return getShort.get(entity);
    }

    @Override
    public float getFloat(Object entity)
    {
        return getFloat.get(entity);
    }

    @Override
    public double getDouble(Object entity)
    {
        return getDouble.get(entity);
    }

    @Override
    public boolean getBoolean(Object entity)
    {
        return getBoolean.get(entity);
    }

    @Override
    public char getChar(Object entity)
    {
        return getChar.get(entity);
    }

    @Override
    public void set(Object entity, int value)
    {
        setInt.set(entity, value);
    }

    @Override
    public void set(Object entity, byte value)
    {
        setByte.set(entity, value);
    }

    @Override
    public void set(Object entity, char value)
    {
        setChar.set(entity, value);
    }

    @Override
    public void set(Object entity, long value)
    {
        setLong.set(entity, value);
    }

    @Override
    public void set(Object entity, float value)
    {
        setFloat.set(entity, value);
    }

    @Override
    public void set(Object entity, short value)
    {
        setShort.set(entity, value);
    }

    @Override
    public void set(Object entity, double value)
    {
        setDouble.set(entity, value);
    }

    @Override
    public void set(Object entity, boolean value)
    {
        setBoolean.set(entity, value);
    }

    @Override
    public void setReference(Object entity, Object value)
    {
        setObj.accept(entity, value);
    }

    @Override
    public Object getReference(Object entity)
    {
        return getObj.apply(entity);
    }

    @Override
    public Object get(Object entity)
    {
        return switch (classId)
        {
            case PRIMITIVE_INT -> getInt.get(entity);
            case ReflectUtil.PRIMITIVE_SHORT -> getShort.get(entity);
            case ReflectUtil.PRIMITIVE_LONG -> getLong.get(entity);
            case ReflectUtil.PRIMITIVE_FLOAT -> getFloat.get(entity);
            case ReflectUtil.PRIMITIVE_DOUBLE -> getDouble.get(entity);
            case ReflectUtil.PRIMITIVE_BOOL -> getBoolean.get(entity);
            case ReflectUtil.PRIMITIVE_BYTE -> getByte.get(entity);
            case ReflectUtil.PRIMITIVE_CHAR -> getChar.get(entity);
            default -> getObj.apply(entity);
        };
    }

    @Override
    public void setObject(Object entity, Object value)
    {
        switch (classId)
        {
            case ReflectUtil.PRIMITIVE_BOOL -> setBoolean.set(entity, (Boolean) value);
            case ReflectUtil.PRIMITIVE_BYTE -> setByte.set(entity, (Byte) value);
            case ReflectUtil.PRIMITIVE_CHAR -> setChar.set(entity, (Character) value);
            case ReflectUtil.PRIMITIVE_SHORT -> setShort.set(entity, (Short) value);
            case PRIMITIVE_INT -> setInt.set(entity, (Integer) value);
            case ReflectUtil.PRIMITIVE_LONG -> setLong.set(entity, (Long) value);
            case ReflectUtil.PRIMITIVE_FLOAT -> setFloat.set(entity, (Float) value);
            case ReflectUtil.PRIMITIVE_DOUBLE -> setDouble.set(entity, (Double) value);
            default -> setObj.accept(entity, value);
        }
    }
}
