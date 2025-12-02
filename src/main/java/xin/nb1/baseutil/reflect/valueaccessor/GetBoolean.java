package xin.nb1.baseutil.reflect.valueaccessor;

@FunctionalInterface
public interface GetBoolean<T>
{
    boolean get(T obj);
}
