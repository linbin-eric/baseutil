package com.jfireframework.baseutil.smc.model;

import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MethodModel
{
    public static enum AccessLevel
    {
        PUBLIC, PRIVATE, PROTECTED
    }
    
    private AccessLevel accessLevel;
    private Class<?>    returnType;
    private Class<?>[]  paramterTypes;
    // 该数组为空时意味着全部属性都不需要用final修饰
    private boolean[]   paramterFinals;
    private Class<?>[]  throwables;
    private String      methodName;
    private String      body;
    private ClassModel  classModel;
    
    public MethodModel(ClassModel classModel)
    {
        this.classModel = classModel;
    }
    
    public MethodModel(Method method, ClassModel classModel)
    {
        this.classModel = classModel;
        int modifiers = method.getModifiers();
        if (Modifier.isPublic(modifiers))
        {
            accessLevel = AccessLevel.PUBLIC;
        }
        else if (Modifier.isPrivate(modifiers))
        {
            accessLevel = AccessLevel.PRIVATE;
        }
        else if (Modifier.isProtected(modifiers))
        {
            accessLevel = AccessLevel.PROTECTED;
        }
        methodName = method.getName();
        returnType = method.getReturnType();
        paramterTypes = method.getParameterTypes();
        throwables = method.getExceptionTypes();
    }
    
    public MethodModel(MethodModel methodModel)
    {
        methodName = methodModel.methodName;
        accessLevel = methodModel.accessLevel;
        paramterTypes = methodModel.paramterTypes;
        throwables = methodModel.throwables;
        returnType = methodModel.returnType;
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        switch (accessLevel)
        {
            case PUBLIC:
                cache.append("public ");
                break;
            case PRIVATE:
                cache.append("private ");
                break;
            case PROTECTED:
                cache.append("protected ");
                break;
            default:
                cache.append("public ");
                break;
        }
        cache.append(SmcHelper.getReferenceName(returnType, classModel)).append(' ')//
                .append(methodName).append('(');
        if (paramterTypes != null && paramterTypes.length > 0)
        {
            if (paramterFinals == null || paramterFinals.length == 0)
            {
                for (int i = 0; i < paramterTypes.length; i++)
                {
                    Class<?> each = paramterTypes[i];
                    cache.append(SmcHelper.getReferenceName(each,classModel)).append(" $").append(i).appendComma();
                }
            }
            else
            {
                for (int i = 0; i < paramterTypes.length; i++)
                {
                    Class<?> each = paramterTypes[i];
                    if (paramterFinals[i])
                    {
                        cache.append("final ");
                    }
                    cache.append(SmcHelper.getReferenceName(each,classModel)).append(" $").append(i).appendComma();
                }
            }
            if (cache.isCommaLast())
            {
                cache.deleteLast();
            }
        }
        cache.append(')');
        if (throwables != null && throwables.length != 0)
        {
            cache.append(" throws ");
            for (Class<?> each : throwables)
            {
                cache.append(SmcHelper.getReferenceName(each,classModel)).appendComma();
            }
            cache.deleteLast();
        }
        cache.append(" \r\n\t{\r\n");//
        String[] tmp = body.split("\r\n");
        for (String each : tmp)
        {
            cache.append("\t\t").append(each).append("\r\n");
        }
        cache.append("\t}\r\n");
        return cache.toString();
    }
    
    @Override
    public int hashCode()
    {
        return methodName.hashCode();
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof MethodModel == false)
        {
            return false;
        }
        MethodModel target = (MethodModel) o;
        if (accessLevel != target.accessLevel)
        {
            return false;
        }
        if (methodName.equals(target.methodName) == false)
        {
            return false;
        }
        if (paramterTypes.length != target.paramterTypes.length)
        {
            return false;
        }
        for (int i = 0; i < paramterTypes.length; i++)
        {
            Class<?> class1 = paramterTypes[i];
            Class<?> class2 = target.paramterTypes[i];
            if (class1 != class2)
            {
                return false;
            }
        }
        return true;
    }
    
    public MethodModelKey generateKey()
    {
        MethodModelKey key = new MethodModelKey();
        key.accessLevel = accessLevel;
        key.methodName = methodName;
        key.paramterTypes = paramterTypes;
        return key;
    }
    
    /**
     * 生成方法的调用字符串，类似xxx($0,$1,$2...)。xxx是方法名
     * 
     * @return
     */
    public String generateInvoke()
    {
        StringCache cache = new StringCache();
        cache.append(methodName).append('(');
        for (int i = 0; i < paramterTypes.length; i++)
        {
            cache.append("$").append(i).appendComma();
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        cache.append(')');
        return cache.toString();
    }
    
    public static class MethodModelKey
    {
        private AccessLevel accessLevel;
        private Class<?>[]  paramterTypes;
        private String      methodName;
        
        public MethodModelKey()
        {
        }
        
        public MethodModelKey(Method method)
        {
            int modifiers = method.getModifiers();
            if (Modifier.isPublic(modifiers))
            {
                accessLevel = AccessLevel.PUBLIC;
            }
            else if (Modifier.isPrivate(modifiers))
            {
                accessLevel = AccessLevel.PRIVATE;
            }
            else if (Modifier.isProtected(modifiers))
            {
                accessLevel = AccessLevel.PROTECTED;
            }
            methodName = method.getName();
            paramterTypes = method.getParameterTypes();
        }
        
        @Override
        public int hashCode()
        {
            return methodName.hashCode();
        }
        
        @Override
        public boolean equals(Object o)
        {
            if (o instanceof MethodModelKey == false)
            {
                return false;
            }
            MethodModelKey target = (MethodModelKey) o;
            if (accessLevel != target.accessLevel)
            {
                return false;
            }
            if (methodName.equals(target.methodName) == false)
            {
                return false;
            }
            if (paramterTypes.length != target.paramterTypes.length)
            {
                return false;
            }
            for (int i = 0; i < paramterTypes.length; i++)
            {
                Class<?> class1 = paramterTypes[i];
                Class<?> class2 = target.paramterTypes[i];
                if (class1 != class2)
                {
                    return false;
                }
            }
            return true;
        }
        
        public void setAccessLevel(AccessLevel accessLevel)
        {
            this.accessLevel = accessLevel;
        }
        
        public void setParamterTypes(Class<?>[] paramterTypes)
        {
            this.paramterTypes = paramterTypes;
        }
        
        public void setMethodName(String methodName)
        {
            this.methodName = methodName;
        }
        
    }
    
    public AccessLevel getAccessLevel()
    {
        return accessLevel;
    }
    
    public void setAccessLevel(AccessLevel accessLevel)
    {
        this.accessLevel = accessLevel;
    }
    
    public Class<?> getReturnType()
    {
        return returnType;
    }
    
    public void setReturnType(Class<?> returnType)
    {
        this.returnType = returnType;
    }
    
    public Class<?>[] getParamterTypes()
    {
        return paramterTypes;
    }
    
    public void setParamterTypes(Class<?>... paramterTypes)
    {
        this.paramterTypes = paramterTypes;
    }
    
    public Class<?>[] getThrowables()
    {
        return throwables;
    }
    
    public void setThrowables(Class<?>... throwables)
    {
        this.throwables = throwables;
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public String getBody()
    {
        return body;
    }
    
    public void setBody(String body)
    {
        this.body = body;
    }
    
    public void setParamterFinals(boolean... paramterFinals)
    {
        this.paramterFinals = paramterFinals;
    }
}
