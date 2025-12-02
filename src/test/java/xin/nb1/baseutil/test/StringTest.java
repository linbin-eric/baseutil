package xin.nb1.baseutil.test;

import xin.nb1.baseutil.StringUtil;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class StringTest
{
    @Test
    public void test()
    {
        String pattern = "这是一个很大的问题，问题是{},zenm{}";
        String result  = StringUtil.format(pattern, "嘿嘿", 1);
        Assert.assertEquals("这是一个很大的问题，问题是嘿嘿,zenm1", result);
        pattern = "这是一个很大的问题，问题是{},zenm{},21asda{}";
        result = StringUtil.format(pattern, "嘿嘿", 1);
        Assert.assertEquals("这是一个很大的问题，问题是嘿嘿,zenm1,21asda{}", result);
        pattern = "这是一个很大的问题，问题是{question},zenm {name}";
        Map<String, String> values = new HashMap<String, String>();
        values.put("question", "1");
        values.put("name", "lb");
        result = StringUtil.format(pattern, values);
        Assert.assertEquals("这是一个很大的问题，问题是1,zenm lb", result);
    }
}
