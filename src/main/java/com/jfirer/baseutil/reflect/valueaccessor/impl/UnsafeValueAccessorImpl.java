package com.jfirer.baseutil.reflect.valueaccessor.impl;

import com.jfirer.baseutil.reflect.ReflectUtil;
import com.jfirer.baseutil.reflect.valueaccessor.ValueAccessor;
import io.github.karlatemp.unsafeaccessor.Unsafe;
import lombok.Getter;

import java.lang.reflect.Field;

public class UnsafeValueAccessorImpl implements ValueAccessor
{
    @Getter
    protected final Field  field;
    private final   long   offset;
    protected final int    classId;
    private static  Unsafe unsafe = Unsafe.getUnsafe();

    public UnsafeValueAccessorImpl(Field field)
    {
        this.field = field;
        offset     = unsafe.objectFieldOffset(field);
        classId    = ReflectUtil.getClassId(field.getType());
    }

    @Override
    public void set(Object entity, int value)
    {
        unsafe.putInt(entity, offset, value);
    }

    @Override
    public void set(Object entity, short value)
    {
        unsafe.putShort(entity, offset, value);
    }

    @Override
    public void set(Object entity, long value)
    {
        unsafe.putLong(entity, offset, value);
    }

    @Override
    public void set(Object entity, char value)
    {
        unsafe.putChar(entity, offset, value);
    }

    @Override
    public void set(Object entity, byte value)
    {
        unsafe.putByte(entity, offset, value);
    }

    @Override
    public void set(Object entity, boolean value)
    {
        unsafe.putBoolean(entity, offset, value);
    }

    @Override
    public void set(Object entity, float value)
    {
        unsafe.putFloat(entity, offset, value);
    }

    @Override
    public void set(Object entity, double value)
    {
        unsafe.putDouble(entity, offset, value);
    }

    @Override
    public void setReference(Object entity, Object value)
    {
        unsafe.putObject(entity, offset, value);
    }

    @Override
    public void setObject(Object entity, Object value)
    {
        if (entity == null)
        {
            throw new NullPointerException("UnsafeValueAccessorImpl get Property from entity fail,entity is null");
        }
        switch (classId)
        {
            case ReflectUtil.PRIMITIVE_BOOL -> unsafe.putBoolean(entity, offset, ((Boolean) value).booleanValue());
            case ReflectUtil.PRIMITIVE_BYTE -> unsafe.putByte(entity, offset, ((Number) value).byteValue());
            case ReflectUtil.PRIMITIVE_CHAR -> unsafe.putChar(entity, offset, ((Character) value).charValue());
            case ReflectUtil.PRIMITIVE_SHORT -> unsafe.putShort(entity, offset, ((Number) value).shortValue());
            case ReflectUtil.PRIMITIVE_INT -> unsafe.putInt(entity, offset, ((Number) value).intValue());
            case ReflectUtil.PRIMITIVE_LONG -> unsafe.putLong(entity, offset, ((Number) value).longValue());
            case ReflectUtil.PRIMITIVE_FLOAT -> unsafe.putFloat(entity, offset, ((Number) value).floatValue());
            case ReflectUtil.PRIMITIVE_DOUBLE -> unsafe.putDouble(entity, offset, ((Number) value).doubleValue());
            default -> unsafe.putReference(entity, offset, value);
        }
    }

    @Override
    public int getInt(Object entity)
    {
        return unsafe.getInt(entity, offset);
    }

    @Override
    public short getShort(Object entity)
    {
        return unsafe.getShort(entity, offset);
    }

    @Override
    public boolean getBoolean(Object entity)
    {
        return unsafe.getBoolean(entity, offset);
    }

    @Override
    public long getLong(Object entity)
    {
        return unsafe.getLong(entity, offset);
    }

    @Override
    public byte getByte(Object entity)
    {
        return unsafe.getByte(entity, offset);
    }

    @Override
    public char getChar(Object entity)
    {
        return unsafe.getChar(entity, offset);
    }

    @Override
    public float getFloat(Object entity)
    {
        return unsafe.getFloat(entity, offset);
    }

    @Override
    public double getDouble(Object entity)
    {
        return unsafe.getDouble(entity, offset);
    }

    @Override
    public Object get(Object entity)
    {
        return switch (classId)
        {
            case ReflectUtil.PRIMITIVE_INT -> unsafe.getInt(entity, offset);
            case ReflectUtil.PRIMITIVE_SHORT -> unsafe.getShort(entity, offset);
            case ReflectUtil.PRIMITIVE_LONG -> unsafe.getLong(entity, offset);
            case ReflectUtil.PRIMITIVE_FLOAT -> unsafe.getFloat(entity, offset);
            case ReflectUtil.PRIMITIVE_DOUBLE -> unsafe.getDouble(entity, offset);
            case ReflectUtil.PRIMITIVE_BOOL -> unsafe.getBoolean(entity, offset);
            case ReflectUtil.PRIMITIVE_BYTE -> unsafe.getByte(entity, offset);
            case ReflectUtil.PRIMITIVE_CHAR -> unsafe.getChar(entity, offset);
            default -> unsafe.getReference(entity, offset);
        };
    }

    @Override
    public Object getReference(Object entity)
    {
        return unsafe.getReference(entity, offset);
    }
}
