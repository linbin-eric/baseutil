package cc.jfire.baseutil.reflect.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

class GenericArrayTypeImpl implements GenericArrayType
{
    private final Type genericComponentType;

    GenericArrayTypeImpl(Type genericComponentType)
    {
        this.genericComponentType = genericComponentType;
    }

    @Override
    public Type getGenericComponentType()
    {
        return genericComponentType;
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof GenericArrayType that && genericComponentType.equals(that.getGenericComponentType());
    }

    @Override
    public int hashCode()
    {
        return genericComponentType.hashCode();
    }

    @Override
    public String toString()
    {
        return genericComponentType.getTypeName() + "[]";
    }
}
