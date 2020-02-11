package com.jfirer.baseutil.data;

import com.jfirer.baseutil.reflect.SimpleHotswapClassLoader;

public class Home
{
    private Person   host;
    private int      length;
    private Person[] liveins = new Person[2];
    private int      width;

    public Home()
    {
        host = new Person();
        System.out.println(host.getClass().getClassLoader());
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        SimpleHotswapClassLoader loader = new SimpleHotswapClassLoader("E:\\jfire\\baseutil\\target\\test-classes");
        loader.setReloadPackages("com.jfireframework.baseutil.data");
        System.out.println(Thread.currentThread().getContextClassLoader());
        Class<?> loadClass = loader.loadClass("com.jfirer.baseutil.data.Home");
        loadClass.newInstance();
    }

    public Person getHost()
    {
        return host;
    }

    public void setHost(Person host)
    {
        this.host = host;
    }

    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    public Person[] getLiveins()
    {
        return liveins;
    }

    public void setLiveins(Person[] liveins)
    {
        this.liveins = liveins;
    }

    public int getWidth()
    {
        return width;
    }

    public void setWidth(int width)
    {
        this.width = width;
    }
}
