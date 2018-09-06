package com.jfireframework.baseutil.reflect;

import java.lang.reflect.Field;

public class ValueAccessor
{
    private static final int _UNSAFE = 0;
    private static final int _FIELD = 1;
    private static final int INT = 1;
    private static final int BYTE = 2;
    private static final int CHAR = 3;
    private static final int BOOLEAN = 4;
    private static final int SHORT = 5;
    private static final int LONG = 6;
    private static final int FLOAT = 7;
    private static final int DOUBLE = 8;
    ////
    private final int accessType = UNSAFE.isAvailable() ? _UNSAFE : _FIELD;
    private Field field;
    private long offset;
    private boolean primitive;
    private int primitiveType = 0;

    public ValueAccessor(Field field)
    {
        this.field = field;
        primitive = field.getType().isPrimitive();
        if ( accessType == _UNSAFE )
        {
            offset = UNSAFE.objectFieldOffset(field);
        }
        else
        {
            field.setAccessible(true);
        }
        if ( primitive )
        {
            Class<?> type = field.getType();
            if ( type == int.class )
            {
                primitiveType = INT;
            }
            else if ( type == short.class )
            {
                primitiveType = SHORT;
            }
            else if ( type == long.class )
            {
                primitiveType = LONG;
            }
            else if ( type == float.class )
            {
                primitiveType = FLOAT;
            }
            else if ( type == double.class )
            {
                primitiveType = DOUBLE;
            }
            else if ( type == boolean.class )
            {
                primitiveType = BOOLEAN;
            }
            else if ( type == byte.class )
            {
                primitiveType = BYTE;
            }
            else if ( type == char.class )
            {
                primitiveType = CHAR;
            }
            else
            {
                throw new IllegalArgumentException();
            }
        }
    }

    public void set(Object entity, int value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putInt(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Integer.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setInt(entity, value);
                }
                else
                {
                    field.set(entity, Integer.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Integer value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putInt(entity, offset, value.intValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setInt(entity, value.intValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, short value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putShort(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Short.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setShort(entity, value);
                }
                else
                {
                    field.set(entity, Short.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Short value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putShort(entity, offset, value.shortValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setShort(entity, value.shortValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, long value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putLong(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Long.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setLong(entity, value);
                }
                else
                {
                    field.set(entity, Long.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Long value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putLong(entity, offset, value.longValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setLong(entity, value.longValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, char value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putChar(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Character.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setChar(entity, value);
                }
                else
                {
                    field.set(entity, Character.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Character value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putChar(entity, offset, value.charValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setChar(entity, value.charValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, byte value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putByte(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Byte.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setByte(entity, value);
                }
                else
                {
                    field.set(entity, Byte.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Byte value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putByte(entity, offset, value.byteValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setByte(entity, value.byteValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, boolean value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putBoolean(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Boolean.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setBoolean(entity, value);
                }
                else
                {
                    field.set(entity, Boolean.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Boolean value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putBoolean(entity, offset, value.booleanValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setBoolean(entity, value.booleanValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, float value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putFloat(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Float.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setFloat(entity, value);
                }
                else
                {
                    field.set(entity, Float.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Float value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putFloat(entity, offset, value.floatValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setFloat(entity, value.floatValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, double value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putDouble(entity, offset, value);
            }
            else
            {
                UNSAFE.putObject(entity, offset, Double.valueOf(value));
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setDouble(entity, value);
                }
                else
                {
                    field.set(entity, Double.valueOf(value));
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void set(Object entity, Double value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                UNSAFE.putDouble(entity, offset, value.doubleValue());
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                if ( primitive )
                {
                    field.setDouble(entity, value.doubleValue());
                }
                else
                {
                    field.set(entity, value);
                }
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public void setObject(Object entity, Object value)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                switch (primitiveType)
                {
                    case INT:
                        UNSAFE.putInt(entity, offset, ((Integer) value).intValue());
                        break;
                    case SHORT:
                        UNSAFE.putShort(entity, offset, ((Short) value).shortValue());
                        break;
                    case LONG:
                        UNSAFE.putLong(entity, offset, ((Long) value).longValue());
                        break;
                    case FLOAT:
                        UNSAFE.putFloat(entity, offset, ((Float) value).floatValue());
                        break;
                    case DOUBLE:
                        UNSAFE.putDouble(entity, offset, ((Double) value).doubleValue());
                        break;
                    case BOOLEAN:
                        UNSAFE.putBoolean(entity, offset, ((Boolean) value).booleanValue());
                        break;
                    case BYTE:
                        UNSAFE.putByte(entity, offset, ((Byte) value).byteValue());
                        break;
                    case CHAR:
                        UNSAFE.putChar(entity, offset, ((Character) value).charValue());
                        break;
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            else
            {
                UNSAFE.putObject(entity, offset, value);
            }
        }
        else
        {
            try
            {
                field.set(entity, value);
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
            }
        }
    }

    public int getInt(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getInt(entity, offset) : (Integer) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getInt(entity) : (Integer) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Integer getIntObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Integer.valueOf(UNSAFE.getInt(entity, offset)) : (Integer) UNSAFE.getObject(entity, offset) : //
                    primitive ? Integer.valueOf(field.getInt(entity)) : (Integer) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public short getShort(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getShort(entity, offset) : (Short) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getShort(entity) : (Short) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Short getShortObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Short.valueOf(UNSAFE.getShort(entity, offset)) : (Short) UNSAFE.getObject(entity, offset) : //
                    primitive ? Short.valueOf(field.getShort(entity)) : (Short) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public boolean getBoolean(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getBoolean(entity, offset) : (Boolean) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getBoolean(entity) : (Boolean) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public Boolean getBooleanObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Boolean.valueOf(UNSAFE.getBoolean(entity, offset)) : (Boolean) UNSAFE.getObject(entity, offset) : //
                    primitive ? Boolean.valueOf(field.getBoolean(entity)) : (Boolean) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return false;
        }
    }

    public long getLong(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getLong(entity, offset) : (Long) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getLong(entity) : (Long) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Long getLongObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Long.valueOf(UNSAFE.getLong(entity, offset)) : (Long) UNSAFE.getObject(entity, offset) : //
                    primitive ? Long.valueOf(field.getLong(entity)) : (Long) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0L;
        }
    }

    public byte getByte(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getByte(entity, offset) : (Byte) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getByte(entity) : (Byte) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Byte getByteObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Byte.valueOf(UNSAFE.getByte(entity, offset)) : (Byte) UNSAFE.getObject(entity, offset) : //
                    primitive ? Byte.valueOf(field.getByte(entity)) : (Byte) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public char getChar(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getChar(entity, offset) : (Character) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getChar(entity) : (Character) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Character getCharObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Character.valueOf(UNSAFE.getChar(entity, offset)) : (Character) UNSAFE.getObject(entity, offset) : //
                    primitive ? Character.valueOf(field.getChar(entity)) : (Character) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public float getFloat(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getFloat(entity, offset) : (Float) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getFloat(entity) : (Float) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Float getFloatObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Float.valueOf(UNSAFE.getFloat(entity, offset)) : (Float) UNSAFE.getObject(entity, offset) : //
                    primitive ? Float.valueOf(field.getFloat(entity)) : (Float) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0F;
        }
    }

    public double getDouble(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? UNSAFE.getDouble(entity, offset) : (Double) UNSAFE.getObject(entity, offset) : //
                    primitive ? field.getDouble(entity) : (Double) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0;
        }
    }

    public Double getDoubleObject(Object entity)
    {
        try
        {
            return accessType == _UNSAFE ? //
                    primitive ? Double.valueOf(UNSAFE.getDouble(entity, offset)) : (Double) UNSAFE.getObject(entity, offset) : //
                    primitive ? Double.valueOf(field.getDouble(entity)) : (Double) field.get(entity);
        } catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return 0D;
        }
    }

    public Object get(Object entity)
    {
        if ( accessType == _UNSAFE )
        {
            if ( primitive )
            {
                switch (primitiveType)
                {
                    case INT:
                        return Integer.valueOf(UNSAFE.getInt(entity, offset));
                    case SHORT:
                        return Short.valueOf(UNSAFE.getShort(entity, offset));
                    case LONG:
                        return Long.valueOf(UNSAFE.getLong(entity, offset));
                    case FLOAT:
                        return Float.valueOf(UNSAFE.getFloat(entity, offset));
                    case DOUBLE:
                        return Double.valueOf(UNSAFE.getDouble(entity, offset));
                    case BOOLEAN:
                        return Boolean.valueOf(UNSAFE.getBoolean(entity, offset));
                    case BYTE:
                        return Byte.valueOf(UNSAFE.getByte(entity, offset));
                    case CHAR:
                        return Character.valueOf(UNSAFE.getChar(entity, offset));
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            else
            {
                return UNSAFE.getObject(entity, offset);
            }
        }
        else
        {
            try
            {
                return field.get(entity);
            } catch (Exception e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
    }

    public Field getField()
    {
        return field;
    }
}
