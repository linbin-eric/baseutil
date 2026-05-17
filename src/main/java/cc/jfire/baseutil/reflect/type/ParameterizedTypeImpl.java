package cc.jfire.baseutil.reflect.type;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

class ParameterizedTypeImpl implements ParameterizedType
{
    private final Type   ownerType;
    private final Type   rawType;
    private final Type[] actualTypeArguments;

    ParameterizedTypeImpl(Type ownerType, Type rawType, Type[] actualTypeArguments)
    {
        this.ownerType           = ownerType;
        this.rawType             = rawType;
        this.actualTypeArguments = actualTypeArguments.clone();
    }

    @Override
    public Type[] getActualTypeArguments()
    {
        return actualTypeArguments.clone();
    }

    @Override
    public Type getRawType()
    {
        return rawType;
    }

    @Override
    public Type getOwnerType()
    {
        return ownerType;
    }

    @Override
    public boolean equals(Object other)
    {
        return other instanceof ParameterizedType that && Objects.equals(ownerType, that.getOwnerType()) && Objects.equals(rawType, that.getRawType()) && Arrays.equals(actualTypeArguments, that.getActualTypeArguments());
    }

    @Override
    public int hashCode()
    {
        return Arrays.hashCode(actualTypeArguments) ^ Objects.hashCode(ownerType) ^ Objects.hashCode(rawType);
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder(rawType.getTypeName());
        if (actualTypeArguments.length != 0)
        {
            builder.append("<");
            for (int i = 0; i < actualTypeArguments.length; i++)
            {
                if (i != 0)
                {
                    builder.append(", ");
                }
                builder.append(actualTypeArguments[i].getTypeName());
            }
            builder.append(">");
        }
        return builder.toString();
    }
}
