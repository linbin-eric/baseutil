package com.jfirer.baseutil.benchmarkforhandle;

import java.lang.invoke.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class Demo2
{
    public static void main(String[] args) throws Throwable
    {
        //下面通过代码的方式，构造一个与function1一样的函数式接口引用。
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Supplier<Data> supplier = (Supplier<Data>) LambdaMetafactory.metafactory(//
                                                                                 lookup,//固定参数
                                                                                 "get",//需要实现的函数式接口的方法名
                                                                                 MethodType.methodType(Supplier.class),//固定写法，中间参数是需要实现的函数接口类
                                                                                 MethodType.methodType(Object.class),//前面的apple的方法签名
                                                                                 lookup.findConstructor(Data.class, MethodType.methodType(void.class)),//这个函数接口需要引用的类的实例方法
                                                                                 MethodType.methodType(Data.class)//这个函数式接口实际实现的时候，方法签名。对比前前一个，这个方法签名是将泛型的信息提供出来了，前面那个泛型的信息都被抹掉了
                                                                                ).getTarget().invoke();
        Data data = supplier.get();
        System.out.println(data.getAge());
    }
}
