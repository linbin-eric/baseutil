package com.jfireframework.baseutil.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.jfireframework.baseutil.code.CodeLocation;
import com.jfireframework.baseutil.verify.Verify;
import sun.misc.Unsafe;

public class UNSAFE
{
	private static final Unsafe unsafe = ReflectUtil.getUnsafe();
	
	/**
	 * 获取字段的偏移量
	 * 
	 * @param fieldName
	 * @param type
	 * @return
	 */
	public static long getFieldOffset(String fieldName, Class<?> type)
	{
		try
		{
			Field field = type.getDeclaredField(fieldName);
			field.setAccessible(true);
			Verify.False(Modifier.isStatic(field.getModifiers()), "属性{}.{}是静态属性,不应该使用该方法,请检查{}", field.getDeclaringClass(), field.getName(), CodeLocation.getCodeLocation(2));
			return unsafe.objectFieldOffset(field);
		}
		catch (Exception e)
		{
			ReflectUtil.throwException(e);
			return 0;
		}
	}
	
	public static boolean compareAndSwapInt(Object src, long offset, int except, int newValue)
	{
		return unsafe.compareAndSwapInt(src, offset, except, newValue);
	}
	
	public static boolean compareAndSwapLong(Object src, long offset, long except, long newValue)
	{
		return unsafe.compareAndSwapLong(src, offset, except, newValue);
	}
	
	public static boolean compareAndSwapObject(Object src, long offset, Object except, Object newValue)
	{
		return unsafe.compareAndSwapObject(src, offset, except, newValue);
	}
	
	public static void putOrderedLong(Object src, long offset, long value)
	{
		unsafe.putOrderedLong(src, offset, value);
	}
	
	public static void putOrderedInt(Object src, long offset, int value)
	{
		unsafe.putOrderedInt(src, offset, value);
	}
	
	public static void putOrderedObject(Object src, long offset, Object value)
	{
		unsafe.putOrderedObject(src, offset, value);
	}
	
	public static int arrayBaseOffset(Class<?> ckass)
	{
		return unsafe.arrayBaseOffset(ckass);
	}
	
	public static int arrayIndexScale(Class<?> ckass)
	{
		return unsafe.arrayIndexScale(ckass);
	}
	
	public static int getIntVolatile(Object src, long offset)
	{
		return unsafe.getIntVolatile(src, offset);
	}
	
	public static long getLongVolatile(Object src, long offset)
	{
		return unsafe.getLongVolatile(src, offset);
	}
	
	public static Object getObjectVolatile(Object src, long offset)
	{
		return unsafe.getObjectVolatile(src, offset);
	}
	
	public static Object getObject(Object src, long offset)
	{
		return unsafe.getObject(src, offset);
	}
	
	public static void putObject(Object src, long offset, Object value)
	{
		unsafe.putObject(src, offset, value);
	}
}
