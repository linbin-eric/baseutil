package com.jfirer.baseutil;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class YamlTest
{
    @Test
    public void test() throws IOException
    {
        InputStream         resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.yml");
        byte[]              bytes            = IoUtil.readAllBytes(resourceAsStream);
        YamlReader reader = new YamlReader(new String(bytes, StandardCharsets.UTF_8));
        Map<String, YamlReader.YmlElement> elementsWithFullPath = reader.getElementsWithFullPath();
        for (Map.Entry<String, YamlReader.YmlElement> each : elementsWithFullPath.entrySet())
        {
            System.out.println(each.getKey());
        }
        YamlReader.YmlElement ymlElement = elementsWithFullPath.get("spring.jpa.hibernate");
        Assert.assertEquals("false", ((YamlReader.StringYmlElement) ((YamlReader.MapYmlElement) ymlElement).getValue().get("update")).getValue());
        Assert.assertEquals("false", ((YamlReader.StringYmlElement) elementsWithFullPath.get("spring.jpa.hibernate.update")).getValue());
//        Map<String, Object> map = SimpleYamlReader.read(new FileInputStream(new File("/Users/linbin/代码空间/baseutil/src/test/resources/test.yml")));
//        map.forEach((name, value) -> {
//            System.out.println(name + ":" + value);
//        });
//        Object o = map.get("spring.jpa.hibernate");
//        if (o instanceof Map map1)
//        {
//            System.out.println(((Map<String, String>) map1).get("update"));
//        }
    }
}
