package com.jfirer.baseutil;

import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.Charset;

public class IniReaderTest
{
    // 测试基本的读取能力
    @Test
    public void test()
    {
        InputStream       inputStream = IniReaderTest.class.getClassLoader().getResourceAsStream("testIni.ini");
        IniReader.IniFile iniFile     = IniReader.read(inputStream, Charset.forName("utf8"));
        Assert.assertNotNull(iniFile.getSection("user"));
        Assert.assertEquals("linbin", iniFile.getValue("name1"));
        Assert.assertEquals("林斌", iniFile.getValue("name2"));
        IniReader.Section section = iniFile.getSection("user");
        Assert.assertEquals("linbin", section.getValue("name1"));
        Assert.assertEquals("林斌", section.getValue("name2"));
    }
}
