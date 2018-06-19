package com.jfireframework.baseutil.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import sun.misc.Unsafe;

public final class ReflectUtil
{
	private final static Unsafe unsafe;
	
	static
	{
		try
		{
			// 由反编译Unsafe类获得的信息
			Field field = Unsafe.class.getDeclaredField("theUnsafe");
			field.setAccessible(true);
			// 获取静态属性,Unsafe在启动JVM时随rt.jar装载
			unsafe = (Unsafe) field.get(null);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	public static Unsafe getUnsafe()
	{
		return unsafe;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Map<String, ? extends Enum<?>> getAllEnumInstances(Class<? extends Enum<?>> type)
	{
		try
		{
			Method method = Class.class.getDeclaredMethod("enumConstantDirectory");
			method.setAccessible(true);
			return new HashMap((Map<String, ?>) method.invoke(type));
		}
		catch (Exception e)
		{
			throwException(e);
			return null;
		}
	}
	
	public static Class<?> wrapPrimitive(Class<?> type)
	{
		if (type.isPrimitive() == false)
		{
			throw new IllegalArgumentException();
		}
		if (type == int.class)
		{
			return Integer.class;
		}
		else if (type == short.class)
		{
			return Short.class;
		}
		else if (type == long.class)
		{
			return Long.class;
		}
		else if (type == float.class)
		{
			return Float.class;
		}
		else if (type == Double.class)
		{
			return Double.class;
		}
		else if (type == boolean.class)
		{
			return Boolean.class;
		}
		else if (type == byte.class)
		{
			return Byte.class;
		}
		else if (type == char.class)
		{
			return Character.class;
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
	
	static boolean hasUnsafe()
	{
		return unsafe != null;
	}
	
	public static void throwException(Throwable t)
	{
		if (hasUnsafe())
		{
			if (t == null)
			{
				throw new NullPointerException("传入的参数为null");
			}
			else
			{
				unsafe.throwException(t);
			}
		}
		else
		{
			ReflectUtil.<RuntimeException> throwException0(t);
		}
	}
	
	@SuppressWarnings("unchecked")
	static <E extends Throwable> void throwException0(Throwable t) throws E
	{
		throw (E) t;
	}
}
