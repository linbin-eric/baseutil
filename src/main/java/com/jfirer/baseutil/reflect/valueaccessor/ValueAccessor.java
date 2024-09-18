package com.jfirer.baseutil.reflect.valueaccessor;

public interface ValueAccessor
{
    default void set(Object entity, int value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, short value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, long value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, char value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, byte value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, boolean value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, float value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void set(Object entity, double value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void setReference(Object entity, Object value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default void setObject(Object entity, Object value)
    {
        throw new IllegalStateException("not impl method!");
    }

    default int getInt(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default short getShort(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default boolean getBoolean(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default long getLong(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default byte getByte(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default char getChar(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default float getFloat(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default double getDouble(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default Object get(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }

    default Object getReference(Object entity)
    {
        throw new IllegalStateException("not impl method!");
    }
}
