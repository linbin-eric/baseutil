package com.jfireframework.baseutil.smc;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.model.ClassModel;

public class SmcHelper
{
	public static String getReferenceName(Class<?> type, ClassModel classModel)
	{
		if (type.isArray() == false)
		{
			if (classModel.addImport(type))
			{
				return type.getSimpleName().replace('$', '.');
			}
			else
			{
				return type.getName().replace('$', '.');
			}
		}
		else
		{
			StringCache cache = new StringCache();
			while (type.isArray())
			{
				cache.append("[]");
				type = type.getComponentType();
			}
			if (classModel.addImport(type))
			{
				return type.getSimpleName().replace('$', '.') + cache.toString();
			}
			else
			{
				return type.getName().replace('$', '.') + cache.toString();
			}
		}
	}
	
	/**
	 * 获得类型的全限定名表达。其中对数组可以转化为源码的标准形式，以及内部类的源码形式
	 * 
	 * @param type
	 * @return
	 */
	public static String getTypeName(Class<?> type)
	{
		if (type.isArray() == false)
		{
			return type.getName().replace('$', '.');
		}
		else
		{
			StringCache cache = new StringCache();
			while (type.isArray())
			{
				cache.append("[]");
				type = type.getComponentType();
			}
			return type.getName().replace('$', '.') + cache.toString();
		}
	}
	
}
