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
        Assert.assertEquals("root", ((YamlReader.StringYmlElement) elementsWithFullPath.get("spring.datasource.hikari.username")).getValue());
        Assert.assertNull(((YamlReader.StringYmlElement) elementsWithFullPath.get("spring.datasource.hikari.password")).getValue());
        Map<String, Object> mapWithFullPath = reader.getMapWithFullPath();
        Map<String, Object> map             = (Map<String, Object>) mapWithFullPath.get("spring.jpa.hibernate");
        Assert.assertEquals("false", map.get("update"));
        Assert.assertEquals("false", mapWithFullPath.get("spring.jpa.hibernate.update"));
        Assert.assertEquals("root", mapWithFullPath.get("spring.datasource.hikari.username"));
        Assert.assertNull(mapWithFullPath.get("spring.datasource.hikari.password"));
        Map<String,Object> o = (Map<String, Object>) mapWithFullPath.get("spring.datasource");
        Map<String,Object>             o1 = (Map<String, Object>) o.get("hikari");
        Assert.assertEquals("root", o1.get("username"));
    }
}
