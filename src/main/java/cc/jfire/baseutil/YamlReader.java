package cc.jfire.baseutil;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
            if (element instanceof SequenceNode sequenceNode)
            {
                ;
            }
            else
            {
                map.put(parsePath(element), element);
            }
        }
        return map;
    }

    private static String parsePath(YmlElement element)
    {
        StringBuilder path = new StringBuilder();
        YmlElement    node = element;
        while (node != null)
        {
            if (node instanceof PlaceHolder placeHolder)
            {
                node = placeHolder.getValue();
            }
            if (StringUtil.isNotBlank(node.getName()))
            {
                if (path.isEmpty())
                {
                    path.append(node.getName());
                }
                else
                {
                    path.insert(0, node.getName() + ".");
                }
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
            if (element instanceof PlaceHolder placeHolder)
            {
                element = placeHolder.getValue();
            }
            if (element instanceof SequenceNode)
            {
                ;
            }
            else
            {
                map.put(parsePath(element), element.getOrdinary());
            }
        }
        return map;
    }

    public Map<String, Object> getMapWithIndentStructure()
    {
        Map<String, Object> map = new HashMap<>();
        for (YmlElement element : elements)
        {
            if (element.getParent() != null)
            {
                continue;
            }
            if (element instanceof PlaceHolder placeHolder)
            {
                element = placeHolder.getValue();
            }
            if (element instanceof OnelineNameNode || element instanceof ListYmlElement || element instanceof MapYmlElement)
            {
                map.put(element.getName(), element.getOrdinary());
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
    @Accessors(chain = true)
    public abstract static class YmlElement
    {
        @Getter
        protected final int        index;
        @Getter
        protected final int        level;
        @Getter
        @Setter
        protected       String     name;
        @Getter
        protected       YmlElement value;
        /**
         * 1、字符串
         * 2. {@code Map<String, YmlElement>}
         * 3. {@code List<String>}
         */
        protected       int        type;
        @Getter
        @Setter
        protected       YmlElement parent;
        protected       Object     ordinary;

        public YmlElement(int index, int level)
        {
            this.index = index;
            this.level = level;
        }

        public abstract Object getOrdinary();
    }

    public static class NoNameStringNode extends YmlElement
    {
        @Getter
        private final String localValue;

        public NoNameStringNode(int index, int level, String value)
        {
            super(index, level);
            localValue = value;
            this.value = this;
        }

        @Override
        public String getName()
        {
            return null;
        }

        @Override
        public Object getOrdinary()
        {
            return localValue;
        }
    }

    public static class OnelineNameNode extends YmlElement
    {
        @Getter
        private final String localValue;

        public OnelineNameNode(int index, int level, String name, String value)
        {
            super(index, level);
            this.name  = name;
            localValue = value;
            this.value = this;
        }

        @Override
        public Object getOrdinary()
        {
            return localValue;
        }
    }

    public static class SequenceNode extends YmlElement
    {
        public SequenceNode(int index, int level, int sequence)
        {
            super(index, level);
            this.name = "[" + sequence + "]";
        }

        public MapYmlElement getMapValue()
        {
            if (value == null)
            {
                value = new MapYmlElement(index, level, name);
            }
            return (MapYmlElement) value;
        }

        public ListYmlElement getListValue()
        {
            if (value == null)
            {
                value = new ListYmlElement(index, level, name);
            }
            return (ListYmlElement) value;
        }

        public YmlElement setStringValue(NoNameStringNode noNameStringNode)
        {
            this.value = noNameStringNode;
            return this.value;
        }

        public NoNameStringNode getStringValue()
        {
            return (NoNameStringNode) value;
        }

        @Override
        public Object getOrdinary()
        {
            return value.getOrdinary();
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    public static class ListYmlElement extends YmlElement
    {
        private ArrayList<SequenceNode> list = new ArrayList<>();

        public ListYmlElement(int index, int level, String name)
        {
            super(index, level);
            this.type = 3;
            this.name = name;
            value     = this;
        }

        public void addNewOne(SequenceNode node)
        {
            list.add(node);
        }

        @Override
        public Object getOrdinary()
        {
            if (ordinary == null)
            {
                List<Object> tmp = new ArrayList<>();
                ordinary = tmp;
                for (SequenceNode sequenceNode : list)
                {
                    YmlElement value;
                    if (sequenceNode.value instanceof PlaceHolder placeHolder)
                    {
                        value = placeHolder.getValue();
                    }
                    else
                    {
                        value = sequenceNode.value;
                    }
                    tmp.add(value.getOrdinary());
                }
            }
            return ordinary;
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Accessors(chain = true)
    @Getter
    public static class MapYmlElement extends YmlElement
    {
        protected Map<String, YmlElement> map = new HashMap<>();

        public MapYmlElement(int index, int level, String name)
        {
            super(index, level);
            this.type = 2;
            this.name = name;
            value     = this;
        }

        public void put(String name, YmlElement element)
        {
            map.put(name, element);
        }

        @Override
        public Object getOrdinary()
        {
            if (ordinary == null)
            {
                Map<String, Object> tmp = new HashMap<>();
                ordinary = tmp;
                for (Map.Entry<String, YmlElement> entry : map.entrySet())
                {
                    YmlElement value;
                    if (entry.getValue() instanceof PlaceHolder placeHolder)
                    {
                        value = placeHolder.getValue();
                    }
                    else
                    {
                        value = entry.getValue();
                    }
                    tmp.put(entry.getKey(), value.getOrdinary());
                }
            }
            return ordinary;
        }
    }

    public static class ChunkNameNode extends YmlElement
    {
        private StringBuilder chunkedValue = new StringBuilder();

        public ChunkNameNode(int index, int level, String name)
        {
            super(index, level);
            this.name = name;
        }

        public void appendLine(String line)
        {
            if (chunkedValue.length() == 0)
            {
                chunkedValue.append(line);
            }
            else
            {
                chunkedValue.append("\n").append(line);
            }
        }

        @Override
        public Object getOrdinary()
        {
            return chunkedValue.toString();
        }
    }

    public static class PlaceHolder extends YmlElement
    {
        private YmlElement resolved;

        public PlaceHolder(int index, String name, int level)
        {
            super(index, level);
            this.name = name;
        }

        public ListYmlElement resolveToList()
        {
            if (resolved == null)
            {
                resolved = new ListYmlElement(index, level, name).setParent(parent);
            }
            return (ListYmlElement) resolved;
        }

        public MapYmlElement resolveToMap()
        {
            if (resolved == null)
            {
                resolved = new MapYmlElement(index, level, name).setParent(parent);
            }
            return (MapYmlElement) resolved;
        }

        @Override
        public YmlElement getValue()
        {
            return resolved;
        }

        public YmlElement toEmptyStringYmlElement()
        {
            resolved = new OnelineNameNode(index, level, name, null).setParent(parent);
            return resolved;
        }

        @Override
        public Object getOrdinary()
        {
            return resolved.getOrdinary();
        }
    }

    public void read(String content)
    {
        List<String> lines     = lines(content);
        int          lineNo    = 0;
        boolean      chunkMode = false;
        for (String line : lines)
        {
            lineNo++;
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
            if (chunkMode)
            {
                if (parent instanceof ChunkNameNode)
                {
                    ;
                }
                else
                {
                    chunkMode = false;
                }
            }
            if (chunkMode)
            {
                ((ChunkNameNode) parent).appendLine(line.substring(level));
                continue;
            }
            else
            {
                line = pureLine(line).trim();
            }
            while (true)
            {
                if (line.startsWith("-"))
                {
                    if (parent == null)
                    {
                        throw new IllegalStateException("节点内容:" + line + "是列表节点，需要有上级节点，当前格式不吻合");
                    }
                    if (parent instanceof PlaceHolder || parent instanceof ListYmlElement || parent instanceof SequenceNode)
                    {
                        ListYmlElement listYmlElement;
                        switch (parent)
                        {
                            case ListYmlElement list ->
                            {
                                listYmlElement = list;
                            }
                            case PlaceHolder placeHolder ->
                            {
                                listYmlElement = placeHolder.resolveToList();
                            }
                            case SequenceNode sequenceNode ->
                            {
                                listYmlElement = sequenceNode.getListValue();
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + parent);
                        }
                        SequenceNode sequenceNode = new SequenceNode(elements.size(), level, listYmlElement.getList().size());
                        sequenceNode.setParent(parent);
                        listYmlElement.addNewOne(sequenceNode);
                        elements.add(sequenceNode);
                    }
                    else
                    {
                        throw new IllegalStateException("节点内容:" + line + "是字符串列表节点，上级节点的类型不是 List，不吻合");
                    }
                    //去掉'- '的内容，同时级别+1
                    line   = line.substring(2);
                    level += 2;
                    parent = getParent(level);
                }
                else
                {
                    break;
                }
            }
            YmlElement element = parseElement(line, level);
            elements.add(element);
            if (element instanceof ChunkNameNode)
            {
                chunkMode = true;
            }
            if (parent == null)
            {
                continue;
            }
            element.setParent(parent);
            switch (parent)
            {
                case MapYmlElement mapYmlElement -> mapYmlElement.put(element.getName(), element);
                case PlaceHolder placeHolder ->
                {
                    MapYmlElement mapYmlElement = placeHolder.resolveToMap();
                    mapYmlElement.put(element.getName(), element);
                }
                case SequenceNode sequenceNode ->
                {
                    switch (element)
                    {
                        case NoNameStringNode noNameStringNode -> sequenceNode.setStringValue(noNameStringNode);
                        case PlaceHolder placeHolder -> sequenceNode.getMapValue().put(placeHolder.getName(), placeHolder);
                        case OnelineNameNode nameStringNode -> sequenceNode.getMapValue().put(nameStringNode.getName(), nameStringNode);
                        default -> throw new IllegalStateException("Unexpected value: " + element);
                    }
                }
                default -> throw new IllegalStateException("lineNo:" + lineNo + "\nline:" + line + "\nUnexpected value: " + parent);
            }
        }
        elements.forEach(element -> {
            if (element instanceof PlaceHolder placeHolder && placeHolder.getValue() == null)
            {
                placeHolder.toEmptyStringYmlElement();
            }
        });
        for (int i = 0; i < elements.size(); i++)
        {
            YmlElement ymlElement = elements.get(i);
            if (ymlElement instanceof PlaceHolder placeHolder)
            {
                elements.set(i, placeHolder.getValue());
            }
        }
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
        int i = line.indexOf(":");
        if (i == -1)
        {
            return new NoNameStringNode(elements.size(), level, getLineValue(line));
        }
        String     name  = line.substring(0, i);
        String     value = line.substring(i + 1).trim();
        YmlElement element;
        if (StringUtil.isBlank(value))
        {
            element = new PlaceHolder(elements.size(), name, level);
        }
        else
        {
            value = getLineValue(value);
            if (value.trim().equals("|"))
            {
                element = new ChunkNameNode(elements.size(), level, name);
            }
            else
            {
                element = new OnelineNameNode(elements.size(), level, name, value);
            }
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
