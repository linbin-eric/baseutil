package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.ClassModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public class CodePropertyCopyDesciptorFactory extends AbstractPropertyCopyDescriptorFactory
{
    private static final AtomicInteger                   count    = new AtomicInteger(0);
    private static final JavaStringCompiler              compiler = new JavaStringCompiler();
    public static final CodePropertyCopyDesciptorFactory instance = new CodePropertyCopyDesciptorFactory();
    
    @SuppressWarnings("unchecked")
    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateEnumCopyPropertyCopyDescriptor(Class<S> s, Class<D> d, Field fromProperty, Field toProperty)
    {
        ClassModel compilerModel = new ClassModel("CodePropertyCopyDescriptor_" + count.incrementAndGet(), Object.class, PropertyCopyDescriptor.class);
        try
        {
            Method fromPropertyMethod = PropertyCopyDescriptor.class.getMethod("fromProperty");
            MethodModel methodModel = new MethodModel(fromPropertyMethod);
            methodModel.setBody("return \"" + fromProperty.getName() + "\";\r\n");
            compilerModel.putMethodModel(methodModel);
            Method toPropertyMethod = PropertyCopyDescriptor.class.getMethod("toProperty");
            methodModel = new MethodModel(toPropertyMethod);
            methodModel.setBody("return \"" + toProperty.getName() + "\";\r\n");
            compilerModel.putMethodModel(methodModel);
            Method processMethod = PropertyCopyDescriptor.class.getMethod("process", Object.class, Object.class);
            methodModel = new MethodModel(processMethod);
            String body = "((" + SmcHelper.getTypeName(d) + ")$1).set" + toProperty.getName().toUpperCase().substring(0, 1) + toProperty.getName().substring(1) + "(java.lang.Enum.valueOf(" + toProperty.getType().getName() + ".class,((" + SmcHelper.getTypeName(s) + ")$0).";
            body += "get" + fromProperty.getName().toUpperCase().substring(0, 1) + fromProperty.getName().substring(1) + "().name()";
            body += ");\r\n";
            methodModel.setBody(body);
            compilerModel.putMethodModel(methodModel);
            Class<?> compile = compiler.compile(compilerModel);
            return (PropertyCopyDescriptor<S, D>) compile.newInstance();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
        
    }
    
    @SuppressWarnings("unchecked")
    @Override
    protected <S, D> PropertyCopyDescriptor<S, D> generateDefaultCopyPropertyDescriptor(Class<S> s, Class<D> d, Field fromProperty, Field toProperty)
    {
        ClassModel compilerModel = new ClassModel("CodePropertyCopyDescriptor_" + count.incrementAndGet(), Object.class, PropertyCopyDescriptor.class);
        try
        {
            Method fromPropertyMethod = PropertyCopyDescriptor.class.getMethod("fromProperty");
            MethodModel methodModel = new MethodModel(fromPropertyMethod);
            methodModel.setBody("return \"" + fromProperty.getName() + "\";\r\n");
            compilerModel.putMethodModel(methodModel);
            Method toPropertyMethod = PropertyCopyDescriptor.class.getMethod("toProperty");
            methodModel = new MethodModel(toPropertyMethod);
            methodModel.setBody("return \"" + toProperty.getName() + "\";\r\n");
            compilerModel.putMethodModel(methodModel);
            Method processMethod = PropertyCopyDescriptor.class.getMethod("process", Object.class, Object.class);
            methodModel = new MethodModel(processMethod);
            String body = "((" + SmcHelper.getTypeName(d) + ")$1).set" + toProperty.getName().toUpperCase().substring(0, 1) + toProperty.getName().substring(1) + "(((" + SmcHelper.getTypeName(s) + ")$0).";
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
            compilerModel.putMethodModel(methodModel);
            Class<?> compile = compiler.compile(compilerModel);
            return (PropertyCopyDescriptor<S, D>) compile.newInstance();
        }
        catch (Exception e)
        {
            throw new JustThrowException(e);
        }
    }
    
}
