package com.jfirer.baseutil.reflect;

import io.github.karlatemp.unsafeaccessor.Unsafe;

import java.lang.reflect.Field;

public class ValueAccessor
{
    protected Field                 field;
    private   long                  offset;
    protected boolean               primitive;
    protected ReflectUtil.Primitive primitiveType;
    private   Unsafe                unsafe = Unsafe.getUnsafe();

    public ValueAccessor()
    {
    }

    public ValueAccessor(Field field)
    {
        this.field    = field;
        primitive     = field.getType().isPrimitive();
        offset        = unsafe.objectFieldOffset(field);
        primitiveType = ReflectUtil.ofPrimitive(field.getType());
    }

    public void set(Object entity, int value)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
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
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        if (primitive)
        {
            switch (primitiveType)
            {
                case INT -> unsafe.putInt(entity, offset, ((Number) value).intValue());
                case SHORT -> unsafe.putShort(entity, offset, ((Number) value).shortValue());
                case LONG -> unsafe.putLong(entity, offset, ((Number) value).longValue());
                case FLOAT -> unsafe.putFloat(entity, offset, ((Number) value).floatValue());
                case DOUBLE -> unsafe.putDouble(entity, offset, ((Number) value).doubleValue());
                case BOOL -> unsafe.putBoolean(entity, offset, ((Boolean) value).booleanValue());
                case BYTE -> unsafe.putByte(entity, offset, ((Number) value).byteValue());
                case CHAR -> unsafe.putChar(entity, offset, ((Character) value).charValue());
                default -> throw new UnsupportedOperationException();
            }
        }
        else
        {
            unsafe.putReference(entity, offset, value);
        }
    }

    public int getInt(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getInt(entity, offset) : (Integer) unsafe.getReference(entity, offset);
    }

    public Integer getIntObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Integer.valueOf(unsafe.getInt(entity, offset)) : (Integer) unsafe.getReference(entity, offset);
    }

    public short getShort(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getShort(entity, offset) : (Short) unsafe.getReference(entity, offset);
    }

    public Short getShortObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Short.valueOf(unsafe.getShort(entity, offset)) : (Short) unsafe.getReference(entity, offset);
    }

    public boolean getBoolean(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getBoolean(entity, offset) : (Boolean) unsafe.getReference(entity, offset);
    }

    public Boolean getBooleanObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Boolean.valueOf(unsafe.getBoolean(entity, offset)) : (Boolean) unsafe.getReference(entity, offset);
    }

    public long getLong(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getLong(entity, offset) : (Long) unsafe.getReference(entity, offset);
    }

    public Long getLongObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Long.valueOf(unsafe.getLong(entity, offset)) : (Long) unsafe.getReference(entity, offset);
    }

    public byte getByte(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getByte(entity, offset) : (Byte) unsafe.getReference(entity, offset);
    }

    public Byte getByteObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Byte.valueOf(unsafe.getByte(entity, offset)) : (Byte) unsafe.getReference(entity, offset);
    }

    public char getChar(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getChar(entity, offset) : (Character) unsafe.getReference(entity, offset);
    }

    public Character getCharObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Character.valueOf(unsafe.getChar(entity, offset)) : (Character) unsafe.getReference(entity, offset);
    }

    public float getFloat(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getFloat(entity, offset) : (Float) unsafe.getReference(entity, offset);
    }

    public Float getFloatObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Float.valueOf(unsafe.getFloat(entity, offset)) : (Float) unsafe.getReference(entity, offset);
    }

    public double getDouble(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? unsafe.getDouble(entity, offset) : (Double) unsafe.getReference(entity, offset);
    }

    public Double getDoubleObject(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        return primitive ? Double.valueOf(unsafe.getDouble(entity, offset)) : (Double) unsafe.getReference(entity, offset);
    }

    public Object get(Object entity)
    {
        if (entity == null)
        {
            throw new NullPointerException("ValueAccessor get Property from entity fail,entity is null");
        }
        if (primitive)
        {
            return switch (primitiveType)
            {
                case INT -> unsafe.getInt(entity, offset);
                case SHORT -> unsafe.getShort(entity, offset);
                case LONG -> unsafe.getLong(entity, offset);
                case FLOAT -> unsafe.getFloat(entity, offset);
                case DOUBLE -> unsafe.getDouble(entity, offset);
                case BOOL -> unsafe.getBoolean(entity, offset);
                case BYTE -> unsafe.getByte(entity, offset);
                case CHAR -> unsafe.getChar(entity, offset);
                default -> throw new UnsupportedOperationException();
            };
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
