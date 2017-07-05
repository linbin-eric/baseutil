package com.jfireframework.baseutil;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.exception.JustThrowException;

public class IniReader
{
    public interface IniFile
    {
        Section getSection(String name);
        
        String getValue(String property);
        
        Set<String> keySet();
    }
    
    public interface Section
    {
        String name();
        
        String getValue(String property);
        
        Set<String> keySet();
    }
    
    static class IniFileImpl implements IniFile
    {
        Map<String, Section> sections   = new HashMap<String, IniReader.Section>();
        Map<String, String>  properties = new HashMap<String, String>();
        
        @Override
        public Section getSection(String name)
        {
            return sections.get(name);
        }
        
        @Override
        public String getValue(String property)
        {
            return properties.get(property);
        }
        
        void putProperty(String property, String value)
        {
            properties.put(property, value);
        }
        
        void addSection(Section section)
        {
            sections.put(section.name(), section);
        }
        
        @Override
        public Set<String> keySet()
        {
            return properties.keySet();
        }
    }
    
    static class SectionImpl implements Section
    {
        final String                name;
        private Map<String, String> properties = new HashMap<String, String>();
        
        public SectionImpl(String name)
        {
            this.name = name;
        }
        
        @Override
        public String name()
        {
            return name;
        }
        
        @Override
        public String getValue(String property)
        {
            return properties.get(property);
        }
        
        void putProperty(String property, String value)
        {
            properties.put(property, value);
        }
        
        @Override
        public Set<String> keySet()
        {
            return properties.keySet();
        }
    }
    
    public static IniFile read(InputStream inputStream, Charset charset)
    {
        class Helper
        {
            /**
             * 从index位置开始（包含）,找寻/n的坐标。并且返回
             * 
             * @param src
             * @param index
             * @return
             */
            int currentLine(byte[] src, int index)
            {
                for (int i = index; i < src.length; i++)
                {
                    if (src[i] == '\n')
                    {
                        if (i > index && src[i - 1] == '\r')
                        {
                            return i - 1;
                        }
                        else
                        {
                            return i;
                        }
                    }
                }
                return src.length - 1;
            }
        }
        Helper helper = new Helper();
        SectionImpl preSection = null;
        IniFileImpl iniFileImpl = new IniFileImpl();
        try
        {
            byte[] src = new byte[inputStream.available()];
            inputStream.read(src);
            int index = 0;
            while (true)
            {
                int end = helper.currentLine(src, index);
                int skip = -1;
                if (src[end] == '\r')
                {
                    skip = 2;
                }
                else if (src[end] == '\n')
                {
                    skip = 1;
                }
                else
                {
                    skip = 0;
                }
                String value = skip > 0 ? new String(src, index, end - index, charset) : new String(src, index, end - index + 1, charset);
                value = value.trim();
                char c = value.charAt(0);
                // 忽略注释
                if (c == ';' || c == '#')
                {
                }
                // 发现是一个新的节点
                else if (c == '[' && value.charAt(value.length() - 1) == ']')
                {
                    String sectionName = value.substring(1, value.length() - 1);
                    preSection = new SectionImpl(sectionName);
                    iniFileImpl.addSection(preSection);
                }
                else
                {
                    int splitIndex = value.indexOf('=');
                    if (splitIndex > 0 && splitIndex < src.length)
                    {
                        // 属性节点
                        String k = value.substring(0, splitIndex).trim();
                        String v = value.substring(splitIndex + 1).trim();
                        iniFileImpl.putProperty(k, v);
                        if (preSection != null)
                        {
                            preSection.putProperty(k, v);
                        }
                    }
                }
                index = end + skip;
                if (skip == 0)
                {
                    break;
                }
            }
            return iniFileImpl;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new JustThrowException(e);
        }
    }
}
