package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public abstract class CodeCopy<S, D> implements Copy<S, D>
{
	static final AtomicInteger	count	= new AtomicInteger(1);
	private Class<S>			source;
	private Class<D>			destination;
	private final Copy<S, D>	util;
	
	public CodeCopy(Class<S> s, Class<D> d)
	{
		source = s;
		destination = d;
		util = new CodeCopyUtilImpl(source, destination);
	}
	
	@SuppressWarnings("unchecked")
	public CodeCopy()
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		util = new CodeCopyUtilImpl(source, destination);
	}
	
	@SuppressWarnings("unchecked")
	public CodeCopy(Map<String, String> nameMap)
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		util = new CodeCopyUtilImpl(source, destination, nameMap);
	}
	
	@SuppressWarnings("unchecked")
	public CodeCopy(String[] pairs)
	{
		ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
		source = (Class<S>) tmp.getActualTypeArguments()[0];
		destination = (Class<D>) tmp.getActualTypeArguments()[1];
		Map<String, String> map = new HashMap<String, String>();
		for (String pair : pairs)
		{
			String[] kv = pair.split(":");
			map.put(kv[0], kv[1]);
		}
		util = new CodeCopyUtilImpl(source, destination, map);
	}
	
	@Override
	public D copy(S src, D desc)
	{
		return util.copy(src, desc);
	}
	
	class CodeCopyUtilImpl implements Copy<S, D>
	{
		private final Processor<S, D> processor;
		
		public CodeCopyUtilImpl(Class<S> src, Class<D> des)
		{
			this(src, des, new HashMap<String, String>());
		}
		
		@SuppressWarnings("unchecked")
		public CodeCopyUtilImpl(Class<S> src, Class<D> des, Map<String, String> mappedName)
		{
			class Entry
			{
				Method	src;
				Method	des;
			}
			List<Entry> list = new ArrayList<Entry>();
			for (Method getOrIsMethod : src.getMethods())
			{
				if (isGetOrIsMethod(getOrIsMethod) && isIgnoreMethod(getOrIsMethod) == false)
				{
					String setMethodName = findSetMethodName(mappedName, getOrIsMethod);
					try
					{
						Method setMethod = findSetMethod(des, setMethodName);
						if (setMethod == null)
						{
							continue;
						}
						if (isEnumCopy(getOrIsMethod.getReturnType(), setMethod.getParameterTypes()[0]))
						{
							Entry entry = new Entry();
							entry.src = getOrIsMethod;
							entry.des = setMethod;
							list.add(entry);
						}
						else if (getOrIsMethod.getReturnType() == setMethod.getParameterTypes()[0])
						{
							Entry entry = new Entry();
							entry.src = getOrIsMethod;
							entry.des = setMethod;
							list.add(entry);
						}
					}
					catch (Exception e)
					{
						throw new JustThrowException(e);
					}
				}
			}
			CompilerModel compilerModel = new CompilerModel("Copy$" + count.getAndIncrement(), Object.class, Processor.class);
			try
			{
				MethodModel methodModel = new MethodModel(Processor.class.getMethod("exec", Object.class, Object.class));
				String body = "{\r\n"//
				        + SmcHelper.getTypeName(src) + " src = (" + SmcHelper.getTypeName(src) + ")$0;\r\n"//
				        + SmcHelper.getTypeName(des) + " des = (" + SmcHelper.getTypeName(des) + ")$1;\r\n";
				for (Entry each : list)
				{
					if (Enum.class.isAssignableFrom(each.src.getReturnType()))
					{
						body += "if(src." + each.src.getName() + "()!=null){\r\n";
						body += "des." + each.des.getName() + "(java.lang.Enum.valueOf(" + each.des.getParameterTypes()[0].getName() + ".class,src." + each.src.getName() + "().name()));\r\n";
						body += "}\r\n";
					}
					else
					{
						body += "des." + each.des.getName() + "(src." + each.src.getName() + "());\r\n";
					}
				}
				body += "}";
				methodModel.setBody(body);
				compilerModel.putMethod(methodModel);
				JavaStringCompiler compiler = new JavaStringCompiler();
				Class<? extends Processor<?, ?>> type = (Class<? extends Processor<?, ?>>) compiler.compile(compilerModel, src.getClassLoader());
				processor = (Processor<S, D>) type.newInstance();
			}
			catch (Exception e)
			{
				throw new JustThrowException(e);
			}
			
		}
		
		protected boolean isIgnoreMethod(Method method)
		{
			if (method.isAnnotationPresent(CopyIgnore.class))
			{
				return true;
			}
			String srcName = method.getName().startsWith("get") ? method.getName().substring(3) : method.getName().substring(2);
			Field field = findField(srcName.substring(0, 1).toLowerCase() + srcName.substring(1), method.getDeclaringClass());
			if (field != null && field.isAnnotationPresent(CopyIgnore.class))
			{
				return true;
			}
			return false;
		}
		
		protected boolean isGetOrIsMethod(Method method)
		{
			String name = method.getName();
			return (name.startsWith("get") || name.startsWith("is"))//
			        && method.getReturnType() != void.class && method.getParameterTypes().length == 0;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private boolean isEnumCopy(Class<?> srcType, Class<?> desType)
		{
			if (Enum.class.isAssignableFrom(srcType) == false || Enum.class.isAssignableFrom(desType) == false)
			{
				return false;
			}
			Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) srcType);
			boolean miss = false;
			for (Entry<String, ? extends Enum<?>> entry : allEnumInstances.entrySet())
			{
				try
				{
					Enum.valueOf((Class<Enum>) desType, entry.getKey());
				}
				catch (Exception e)
				{
					miss = true;
					break;
				}
			}
			if (miss)
			{
				return false;
			}
			return true;
		}
		
		private Field findField(String name, Class<?> ckass)
		{
			do
			{
				
				if (ckass != Object.class)
				{
					try
					{
						Field field = ckass.getDeclaredField(name);
						return field;
					}
					catch (Exception e)
					{
						ckass = ckass.getSuperclass();
					}
				}
				else
				{
					break;
				}
			} while (true);
			return null;
		}
		
		private Method findSetMethod(Class<?> type, String methodName)
		{
			Method[] declaredMethods = type.getMethods();
			for (Method each : declaredMethods)
			{
				if (each.getName().equals(methodName) && each.getParameterTypes().length == 1)
				{
					return each;
				}
			}
			return null;
		}
		
		protected String findSetMethodName(Map<String, String> mappedName, Method getOrIsMethod)
		{
			String srcName = getOrIsMethod.getName().startsWith("get") ? getOrIsMethod.getName().substring(3) : getOrIsMethod.getName().substring(2);
			String tmp = srcName.substring(0, 1).toLowerCase() + srcName.substring(1);
			if (mappedName.containsKey(tmp))
			{
				String mapName = mappedName.get(tmp);
				mapName = mapName.substring(0, 1).toUpperCase() + mapName.substring(1);
				srcName = mapName;
			}
			String setMethodName = "set" + srcName;
			return setMethodName;
		}
		
		@Override
		public D copy(S src, D desc)
		{
			if (src == null)
			{
				return desc;
			}
			processor.exec(src, desc);
			return desc;
		}
		
	}
	
	public static interface Processor<S, D>
	{
		void exec(S s, D d);
	}
	
}
