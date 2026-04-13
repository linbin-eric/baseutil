package cc.jfire.baseutil;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
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
        Assert.assertEquals("false", ((YamlReader.OnelineNameNode) ((YamlReader.MapYmlElement) ymlElement).getMap().get("update").getValue()).getLocalValue());
        Assert.assertEquals("false", ((YamlReader.OnelineNameNode) elementsWithFullPath.get("spring.jpa.hibernate.update")).getLocalValue());
        Assert.assertEquals("#儿童 #动画", ((YamlReader.OnelineNameNode) elementsWithFullPath.get("spring.jpa.hibernate.date")).getLocalValue());
        Assert.assertEquals("root", ((YamlReader.OnelineNameNode) elementsWithFullPath.get("spring.datasource.hikari.username")).getLocalValue());
        Assert.assertNull(((YamlReader.OnelineNameNode) elementsWithFullPath.get("spring.datasource.hikari.password")).getLocalValue());
        Map<String, Object> mapWithFullPath = reader.getMapWithFullPath();
        Map<String, Object> map             = (Map<String, Object>) mapWithFullPath.get("spring.jpa.hibernate");
        Assert.assertEquals("false", map.get("update"));
        Assert.assertEquals("""
                                    这是一个行
                                    "包含这一行""",mapWithFullPath.get("spring.chunk"));
        Assert.assertEquals("false", mapWithFullPath.get("spring.jpa.hibernate.update"));
        Assert.assertEquals("root", mapWithFullPath.get("spring.datasource.hikari.username"));
        Assert.assertNull(mapWithFullPath.get("spring.datasource.hikari.password"));
        Map<String,Object> o = (Map<String, Object>) mapWithFullPath.get("spring.datasource");
        Map<String,Object>             o1 = (Map<String, Object>) o.get("hikari");
        Assert.assertEquals("root", o1.get("username"));
        List<Map<String,Object>> o2 = (List<Map<String, Object>>) mapWithFullPath.get("spring.list");
        Assert.assertEquals("a", o2.get(0).get("name"));
        Assert.assertEquals("2", o2.get(1).get("age"));
    }
}
