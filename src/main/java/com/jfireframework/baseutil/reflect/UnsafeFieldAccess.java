package com.jfireframework.baseutil.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import com.jfireframework.baseutil.code.CodeLocation;
import com.jfireframework.baseutil.verify.Verify;
import sun.misc.Unsafe;

public class UnsafeFieldAccess
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
}
