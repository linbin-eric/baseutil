package cc.jfire.baseutil;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;

import javax.swing.text.AsyncBoxView;
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



    /**
     * yml 元素应该有如下的可能：
     * 1 k:  v   类型，此时有名称k，有值。这是一个 kv类型的节点，该节点是上级 map 类型节点的一个 kv 值。
     * 2 k:      类型，此时有名称k，解析到这一行的是不能确定类型，可能是一个空值字符串，也可以可能是一个列表，也可以可能一个map
     * 3 - v     类型，有值。该节点是一个字符串节点，同时，是父节点的列表元素值之一。该节点的名称是在列表元素中的顺序。
     * 4 - k: v  类型.该类型较为特殊，代表该节点是一个 map 类型的节点，kv 是该节点的属性对。同时该节点是上级列表节点的一个元素。该节点的名称是在列表元素中的顺序。
     */
    @Data
    @Accessors(chain = true)
    public abstract static class YmlElement
    {
        protected final int        index;
        protected final int        level;
        protected       String     name;
        /**
         * 1、字符串
         * 2. {@code Map<String, YmlElement>}
         * 3. {@code List<String>}
         */
        protected       int        type;
        protected       YmlElement parent;

        public abstract YmlValue getValue();
    }

    public static class PlaceHolder extends YmlElement
    {
        public PlaceHolder(int index, String name, int level)
        {
            super(index, level);
            this.name = name;
        }

        public ListYmlElement toListYmlElement()
        {
            return (ListYmlElement) new ListYmlElement(index, level).setParent(parent).setName(name);
        }

        public MapYmlElement toMapYmlElement()
        {
            return (MapYmlElement) new MapYmlElement(index, name, level).setParent(parent);
        }

        public NamedStringYmlElement toEmptyStringYmlElement()
        {
            return (NamedStringYmlElement) new NamedStringYmlElement(index, level, name).setParent(parent);
        }

        @Override
        public YmlValue getValue()
        {
            throw new UnsupportedOperationException();
        }
    }

    @Accessors(chain = true)
    public static class StringYmlElement extends YmlElement
    {
        private final String value;

        public StringYmlElement(int index, int level, String value)
        {
            super(index, level);
            this.value = value;
        }

        @Override
        public YmlValue getValue()
        {
            return new StringYmlValue(value);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class ListYmlElement extends YmlElement
    {
        protected List<YmlElement> value = new ArrayList<>();

        public ListYmlElement(int index, int level)
        {
            super(index, level);
            this.type = 3;
        }

        public void add(YmlElement element)
        {
            element.setParent(this);
            element.setName("[" + value.size() + "]");
            value.add(element);
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    @Getter
    public static class MapYmlElement extends YmlElement
    {
        protected Map<String, YmlElement> value = new HashMap<>();
        private   Map<String, Object>     ordinary;

        public MapYmlElement(int index, int level)
        {
            super(index, level);
            this.type = 2;
        }

        public void put(String name, YmlElement element)
        {
            value.put(name, element);
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
            line = pureLine(line).trim();
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
                    YmlElement element;
                    if (line.contains(":"))
                    {
                        element = new MapYmlElement(elements.size(), level);
                        YmlElement childElement = parseElement(line, level + 1);
                        ((MapYmlElement)element).put(childElement.getName(), childElement);
                    }
                    else
                    {
                        element = new StringYmlElement(elements.size(), level, getLineValue(line));
                        elements.add(element);
                    }
                    ((ListYmlElement) parent).add(element);
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
        value = getLineValue(value);
        if (StringUtil.isBlank(value))
        {
            element = new PlaceHolder(elements.size(), name, level);
        }
        else
        {
            element = new StringYmlElement(elements.size(), level, value).setName(name);
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

    /**
     * 当前行的有效内容。也就是第一个不被''或者""包围的#，该符号后面的都是注释
     *
     * @param line
     * @return
     */
    private String pureLine(String line)
    {
        int singleQuotes = 1;
        int doubleQuotes = 2;
        int state        = 0;
        for (int i = 0; i < line.length(); i++)
        {
            char c = line.charAt(i);
            if (c == '"')
            {
                if (state == 0)
                {
                    state = doubleQuotes;
                    continue;
                }
                else if (state == doubleQuotes)
                {
                    state = 0;
                    continue;
                }
                else
                {
                    throw new IllegalArgumentException("有错误的 yml，当前错误内容：" + line);
                }
            }
            else if (c == '\'')
            {
                if (state == 0)
                {
                    state = singleQuotes;
                    continue;
                }
                else if (state == singleQuotes)
                {
                    state = 0;
                    continue;
                }
                else
                {
                    throw new IllegalArgumentException("有错误的 yml，当前错误内容：" + line);
                }
            }
            else if (c == '#' && state == 0)
            {
                return line.substring(0, i);
            }
        }
        return line;
    }

    private String getLineValue(String line)
    {
        if (line.startsWith("\"") || line.startsWith("'"))
        {
            return line.substring(1, line.length() - 1);
        }
        return line;
    }
}
