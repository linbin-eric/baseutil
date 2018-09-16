package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.CompileHelper;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public class CodePropertyCopyDesciptorFactory extends AbstractPropertyCopyDescriptorFactory
{
    private static final AtomicInteger                   count    = new AtomicInteger(0);
    private static final CompileHelper compiler = new CompileHelper();
    public static final CodePropertyCopyDesciptorFactory instance = new CodePropertyCopyDesciptorFactory();
    
    @SuppressWarnings("unchecked")
    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateEnumCopyPropertyCopyDescriptor(Class<S> s, Class<D> d, Field fromProperty, Field toProperty)
    {
        ClassModel classModel = new ClassModel("CodePropertyCopyDescriptor_" + count.incrementAndGet(), Object.class, PropertyCopyDescriptor.class);
        try
        {
            Method fromPropertyMethod = PropertyCopyDescriptor.class.getMethod("fromProperty");
            MethodModel methodModel = new MethodModel(fromPropertyMethod, classModel);
            methodModel.setBody("return \"" + fromProperty.getName() + "\";\r\n");
            classModel.putMethodModel(methodModel);
            Method toPropertyMethod = PropertyCopyDescriptor.class.getMethod("toProperty");
            methodModel = new MethodModel(toPropertyMethod, classModel);
            methodModel.setBody("return \"" + toProperty.getName() + "\";\r\n");
            classModel.putMethodModel(methodModel);
            Method processMethod = PropertyCopyDescriptor.class.getMethod("process", Object.class, Object.class);
            methodModel = new MethodModel(processMethod, classModel);
            String body = "((" + SmcHelper.getReferenceName(d, classModel) + ")$1).set" + toProperty.getName().toUpperCase().substring(0, 1) + toProperty.getName().substring(1) + "(java.lang.Enum.valueOf(" + toProperty.getType().getName() + ".class,((" + SmcHelper.getReferenceName(s, classModel) + ")$0).";
            body += "get" + fromProperty.getName().toUpperCase().substring(0, 1) + fromProperty.getName().substring(1) + "().name()";
            body += ");\r\n";
            methodModel.setBody(body);
            classModel.putMethodModel(methodModel);
            Class<?> compile = compiler.compile(classModel);
            return (PropertyCopyDescriptor<S, D>) compile.newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateDefaultCopyPropertyDescriptor(Class<S> s, Class<D> d, Field fromProperty, Field toProperty)
    {
        ClassModel classModel = new ClassModel("CodePropertyCopyDescriptor_" + count.incrementAndGet(), Object.class, PropertyCopyDescriptor.class);
        try
        {
            Method fromPropertyMethod = PropertyCopyDescriptor.class.getMethod("fromProperty");
            MethodModel methodModel = new MethodModel(fromPropertyMethod, classModel);
            methodModel.setBody("return \"" + fromProperty.getName() + "\";\r\n");
            classModel.putMethodModel(methodModel);
            Method toPropertyMethod = PropertyCopyDescriptor.class.getMethod("toProperty");
            methodModel = new MethodModel(toPropertyMethod, classModel);
            methodModel.setBody("return \"" + toProperty.getName() + "\";\r\n");
            classModel.putMethodModel(methodModel);
            Method processMethod = PropertyCopyDescriptor.class.getMethod("process", Object.class, Object.class);
            methodModel = new MethodModel(processMethod, classModel);
            String body = "((" + SmcHelper.getReferenceName(d, classModel) + ")$1).set" + toProperty.getName().toUpperCase().substring(0, 1) + toProperty.getName().substring(1) + "(((" + SmcHelper.getReferenceName(s, classModel) + ")$0).";
            if (fromProperty.getType() == boolean.class)
            {
                body += "is";
            }
            else
            {
                body += "get";
            }
            body += fromProperty.getName().toUpperCase().substring(0, 1) + fromProperty.getName().substring(1) + "()";
            body += ");\r\n";
            methodModel.setBody(body);
            classModel.putMethodModel(methodModel);
            Class<?> compile = compiler.compile(classModel);
            return (PropertyCopyDescriptor<S, D>) compile.newInstance();
        }
        catch (Exception e)
        {
            ReflectUtil.throwException(e);
            return null;
        }
    }
    
}
