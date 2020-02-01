package com.jfireframework.baseutil;

import com.jfireframework.baseutil.encrypt.Base64Tool;
import com.jfireframework.baseutil.time.Timewatch;
import org.junit.Assert;
import org.junit.Test;
import sun.misc.BASE64Encoder;

import java.nio.charset.Charset;

public class Base64Test
{
    @Test
    public void test2()
    {
        String name = "1234";
        System.out.println(Base64Tool.encode(name.getBytes(Charset.forName("utf8"))));
        System.out.println(new String(Base64Tool.decode("MTIzNA"), Charset.forName("utf8")));
        name = "12345";
        System.out.println(Base64Tool.encode(name.getBytes(Charset.forName("utf8"))));
        System.out.println(new String(Base64Tool.decode("MTIzNDU"), Charset.forName("utf8")));
    }

    @Test
    public void test()
    {
        String name = "我的名字";
        Assert.assertEquals(name, new String(Base64Tool.decode(Base64Tool.encode(name.getBytes()))));
    }

    @Test
    public void speed()
    {
        int           count         = 1000000;
        BASE64Encoder base64Encoder = new BASE64Encoder();
        Timewatch     timewatch     = new Timewatch();
        byte[]        name          = "我的测试".getBytes();
        for (int i = 0; i < 20; i++)
        {
            base64Encoder.encode(name);
            Base64Tool.encode(name);
        }
        timewatch.start();
        for (int i = 0; i < count; i++)
        {
            Base64Tool.encode(name);
        }
        timewatch.end();
        System.out.println("Base64Util:" + timewatch.getTotal());
        timewatch.start();
        for (int i = 0; i < count; i++)
        {
            base64Encoder.encode(name);
        }
        timewatch.end();
        System.out.println("BASE64Encoder:" + timewatch.getTotal());
    }
}
