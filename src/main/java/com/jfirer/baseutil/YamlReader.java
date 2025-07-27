package com.jfirer.baseutil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.util.*;

public class YamlReader
{
    private final List<YmlElement> elements = new ArrayList<>();

    public YamlReader(String content)
    {
        read(content);
    }

    public Map<String, YmlElement> getElementsWithFullPath()
    {
        Map<String, YmlElement> map = new HashMap<>();
        for (YmlElement element : elements)
        {
            map.put(parsePath(element), element);
        }
        return map;
    }

    private static String parsePath(YmlElement element)
    {
        StringBuilder path = new StringBuilder();
        YmlElement    node = element;
        while (node != null)
        {
            if (path.isEmpty())
            {
                path.append(node.getName());
            }
            else
            {
                path.insert(0, node.getName() + ".");
            }
            node = node.getParent();
        }
        return path.toString();
    }

    public Map<String, Object> getMapWithFullPath()
    {
        Map<String, Object> map = new HashMap<>();
        for (YmlElement element : elements)
        {
            if (element instanceof StringYmlElement stringYmlElement)
            {
                if (stringYmlElement.getValue() != null)
                {
                    map.put(parsePath(element), stringYmlElement.getValue());
                }
            }
            else if (element instanceof ListYmlElement listYmlElement)
            {
                map.put(parsePath(element), listYmlElement.getValue());
            }
            else if (element instanceof MapYmlElement mapYmlElement)
            {
                map.put(parsePath(element), mapYmlElement.toOrdinaryMap());
            }
        }
        return map;
    }

    public Map<String, Object> getMapWithIndentStructure()
    {
        Map<String, Object> map = new HashMap<>();
        for (YmlElement element : elements)
        {
            if (element instanceof StringYmlElement stringYmlElement)
            {
                if (stringYmlElement.getValue() != null && element.getParent() == null)
                {
                    map.put(element.getName(), stringYmlElement.getValue());
                }
            }
            else if (element instanceof ListYmlElement listYmlElement)
            {
                if (element.getParent() == null)
                {
                    map.put(element.getName(), listYmlElement.getValue());
                }
            }
            else if (element instanceof MapYmlElement mapYmlElement)
            {
                if (element.getParent() == null)
                {
                    map.put(element.getName(), mapYmlElement.toOrdinaryMap());
                }
            }
        }
        return map;
    }

    @Data
    @Accessors(chain = true)
    public abstract static class YmlElement
    {
        protected final int        index;
        protected final String     name;
        protected final int        level;
        /**
         * 1、字符串
         * 2. {@code Map<String, YmlElement>}
         * 3. {@code List<String>}
         */
        protected       int        type;
        protected       YmlElement parent;
    }

    public static class PlaceHolder extends YmlElement
    {
        public PlaceHolder(int index, String name, int level)
        {
            super(index, name, level);
        }

        public ListYmlElement toListYmlElement()
        {
            return (ListYmlElement) new ListYmlElement(index, name, level).setParent(parent);
        }

        public MapYmlElement toMapYmlElement()
        {
            return (MapYmlElement) new MapYmlElement(index, name, level).setParent(parent);
        }

        public StringYmlElement toEmptyStringYmlElement()
        {
            return (StringYmlElement) new StringYmlElement(index, name, level, null).setParent(parent);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    @Getter
    public static class StringYmlElement extends YmlElement
    {
        protected final String value;

        public StringYmlElement(int index, String name, int level, String value)
        {
            super(index, name, level);
            this.value = value;
            this.type  = 1;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class ListYmlElement extends YmlElement
    {
        protected List<String> value = new ArrayList<>();

        public ListYmlElement(int index, String name, int level)
        {
            super(index, name, level);
            this.type = 3;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    @Getter
    public static class MapYmlElement extends YmlElement
    {
        protected Map<String, YmlElement> value = new HashMap<>();
        private   Map<String, Object>     ordinary;

        public MapYmlElement(int index, String name, int level)
        {
            super(index, name, level);
            this.type = 2;
        }

        public Map<String, Object> toOrdinaryMap()
        {
            if (ordinary != null)
            {
                return ordinary;
            }
            ordinary = new HashMap<>();
            for (Map.Entry<String, YmlElement> each : value.entrySet())
            {
                if (each.getValue() instanceof StringYmlElement ymlElement)
                {
                    if (ymlElement.getValue() != null)
                    {
                        ordinary.put(each.getKey(), ymlElement.getValue());
                    }
                }
                else if (each.getValue() instanceof ListYmlElement ymlElement)
                {
                    ordinary.put(each.getKey(), ymlElement.getValue());
                }
                else
                {
                    ordinary.put(each.getKey(), ((MapYmlElement) each.getValue()).toOrdinaryMap());
                }
            }
            return ordinary;
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
            if (line.trim().startsWith("#") || line.trim().isEmpty())
            {
                continue;
            }
            int level = 0;
            while (line.charAt(level) == ' ')
            {
                level++;
            }
            YmlElement parent = getParent(level);
            line = line.contains("#") ? line.substring(0, line.indexOf("#")).trim() : line.trim();
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
                        elements.set(parent.getIndex(), parent);
                    }
                    ((ListYmlElement) parent).getValue().add(line);
                }
                else
                {
                    throw new IllegalStateException("节点内容:" + line + "是字符串列表节点，上级节点的类型不是 List，不吻合");
                }
            }
            else if (line.contains(":"))
            {
                YmlElement element = parseElement(line, level);
                elements.add(element);
                if (parent == null)
                {
                    continue;
                }
                if (parent instanceof MapYmlElement mapYmlElement)
                {
                    element.setParent(parent);
                }
                else if (parent instanceof PlaceHolder placeHolder)
                {
                    parent = placeHolder.toMapYmlElement();
                    elements.set(parent.getIndex(), parent);
                    element.setParent(parent);
                }
                else
                {
                    throw new IllegalStateException("节点内容:" + line + "的上节节点的类型不是 Map，不吻合");
                }
            }
            else
            {
                throw new IllegalArgumentException("无法识别的格式:" + line);
            }
        }
        elements.forEach(element -> {
            if (element instanceof PlaceHolder placeHolder)
            {
                elements.set(placeHolder.getIndex(), placeHolder.toEmptyStringYmlElement());
            }
        });
        elements.forEach(element -> {
            if (element.getParent() instanceof MapYmlElement ymlElement)
            {
                ymlElement.getValue().put(element.getName(), element);
            }
        });
    }

    private YmlElement getParent(int level)
    {
        for (int i = elements.size() - 1; i > -1; i--)
        {
            if (elements.get(i).getLevel() < level)
            {
                return elements.get(i);
            }
        }
        return null;
    }

    private YmlElement parseElement(String line, int level)
    {
        int        i     = line.indexOf(":");
        String     name  = line.substring(0, i);
        String     value = line.substring(i + 1).trim();
        YmlElement element;
        if (StringUtil.isBlank(value))
        {
            element = new PlaceHolder(elements.size(), name, level);
        }
        else
        {
            element = new StringYmlElement(elements.size(), name, level, value);
        }
        return element;
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
