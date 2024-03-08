package com.jfirer.baseutil;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.*;

public class YamlReader
{
    private List<YmlElement> elements = new ArrayList<>();

    public YamlReader(String content)
    {
        read(content);
    }

    public Map<String, YmlElement> getElementsWithFullPath()
    {
        Map<String, YmlElement> map = new HashMap<>();
        for (YmlElement element : elements)
        {
            String     path = null;
            YmlElement node = element;
            while (node != null)
            {
                if (path == null)
                {
                    path = node.getName();
                }
                else
                {
                    path = node.getName() + "." + path;
                }
                node = node.getParent();
            }
            map.put(path, element);
        }
        return map;
    }

    @Data
    @Accessors(chain = true)
    abstract class YmlElement
    {
        protected String     name;
        /**
         * 1、字符串
         * 2、Map<String,YmlElement>
         * 3、List<String>
         */
        protected int        type;
        protected int        level;
        protected YmlElement parent;
    }

    class PlaceHolder extends YmlElement
    {
        public PlaceHolder(int level, String name)
        {
            this.level = level;
            this.name  = name;
        }

        public ListYmlElement toListYmlElement()
        {
            ListYmlElement element = new ListYmlElement();
            element.setName(name).setLevel(level);
            return element;
        }

        public MapYmlElement toMapYmlElement()
        {
            MapYmlElement element = new MapYmlElement();
            element.setName(name).setLevel(level);
            return element;
        }

        public StringYmlElement toStringYmlElement()
        {
            StringYmlElement element = new StringYmlElement(name, null, level);
            return element;
        }
    }

    @Data
    @Accessors(chain = true)
    class StringYmlElement extends YmlElement
    {
        protected final String value;

        public StringYmlElement(String name, String value, int level)
        {
            this.name  = name;
            this.value = value;
            this.level = level;
            this.type  = 1;
        }
    }

    @Data
    @Accessors(chain = true)
    class ListYmlElement extends YmlElement
    {
        protected List<String> value = new ArrayList<>();

        public ListYmlElement()
        {
            this.type = 3;
        }
    }

    @Data
    @Accessors(chain = true)
    class MapYmlElement extends YmlElement
    {
        protected Map<String, YmlElement> value = new HashMap<>();

        public MapYmlElement()
        {
            this.type = 2;
        }
    }

    /**
     * 1、使用#开头的内容，后续一整行全部为注释
     * 2、只有K: 的情况，后续可以有两种情况。一种是缩进，那就带着是K这个对象的属性；一种是更少的缩进，那就代表这个K:是一个空字符串值
     * 3、如果是- 的情况，则不允许下级嵌套。即，自身的父级必须不是该类型。
     */
    public void read(String content)
    {
        List<String> lines = lines(content);
        for (String line : lines)
        {
            if (line.trim().startsWith("#") || line.trim().equals(""))
            {
                continue;
            }
            int level = 0;
            while (line.charAt(level) == ' ')
            {
                level++;
            }
            YmlElement parent      = null;
            int        parentIndex = -1;
            for (int i = elements.size() - 1; i > -1; i--)
            {
                if (elements.get(i).getLevel() < level)
                {
                    parent      = elements.get(i);
                    parentIndex = i;
                    break;
                }
            }
            line = line.trim();
            if (line.startsWith("-"))
            {
                line = line.substring(1).trim();
                if (parent == null)
                {
                    throw new IllegalStateException("节点内容:" + line + "是字符串列表节点，需要有上级节点，当前格式不吻合");
                }
                if (parent instanceof PlaceHolder || parent instanceof ListYmlElement)
                {
                    if (parent instanceof PlaceHolder placeHolder)
                    {
                        parent = placeHolder.toListYmlElement();
                        elements.set(parentIndex, parent);
                    }
                    ((ListYmlElement) parent).getValue().add(line);
                }
                else
                {
                    throw new IllegalStateException("节点内容:" + line + "是字符串列表节点，上节节点的类型不是 List，不吻合");
                }
            }
            else if (line.indexOf(":") != -1)
            {
                int    i     = line.indexOf(":");
                String name  = line.substring(0, i);
                String value = line.substring(i + 1).trim();
                if (value.indexOf("#") != -1)
                {
                    value = value.substring(0, value.indexOf("#"));
                }
                YmlElement element;
                if (StringUtil.isBlank(value))
                {
                    element = new PlaceHolder(level, name);
                }
                else
                {
                    element = new StringYmlElement(name, value, level);
                }
                elements.add(element);
                if (parent == null)
                {
                    continue;
                }
                if (parent instanceof MapYmlElement)
                {
                    ;
                }
                else if (parent instanceof PlaceHolder placeHolder)
                {
                    parent = placeHolder.toMapYmlElement();
                    elements.set(parentIndex, parent);
                }
                else
                {
                    throw new IllegalStateException("节点内容:" + line + "是字符串节点，上节节点的类型不是 Map，不吻合");
                }
            }
            else
            {
                throw new IllegalArgumentException("无法识别的格式:" + line);
            }
        }
        for (int i = 0; i < elements.size(); i++)
        {
            YmlElement element = elements.get(i);
            if (element instanceof PlaceHolder placeHolder)
            {
                StringYmlElement stringYmlElement = placeHolder.toStringYmlElement();
                elements.set(i, stringYmlElement);
            }
        }
        for (int i = elements.size() - 1; i > -1; i--)
        {
            YmlElement current = elements.get(i);
            YmlElement parent  = null;
            for (int j = i - 1; j > -1; j--)
            {
                if (elements.get(j).getLevel() < current.getLevel())
                {
                    parent = elements.get(j);
                    break;
                }
            }
            if (parent != null)
            {
                if (parent instanceof MapYmlElement)
                {
                    ((MapYmlElement) parent).getValue().put(current.getName(), current);
                    current.setParent(parent);
                }
                else
                {
                    throw new IllegalArgumentException("无法识别的上下文结构，parent 是:" + parent.getName() + ",错误节点是:" + current.getName());
                }
            }
        }
    }

    private List<String> lines(String content)
    {
        List<String> list   = new LinkedList<>();
        int          length = content.length();
        int          index  = 0;
        int          pre    = 0;
        while (index < length)
        {
            char c = content.charAt(index);
            if (c == '\r')
            {
                if (index + 1 < length && content.charAt(index + 1) == '\n')
                {
                    list.add(content.substring(pre, index));
                    index = pre = index + 2;
                }
                else
                {
                    list.add(content.substring(pre, index));
                    index = pre = index + 1;
                }
            }
            else if (c == '\n')
            {
                list.add(content.substring(pre, index));
                index = pre = index + 1;
            }
            else
            {
                index++;
            }
        }
        if (pre != index)
        {
            list.add(content.substring(pre, index));
        }
        return list;
    }
}
