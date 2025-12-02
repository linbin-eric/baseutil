package xin.nb1.baseutil;

import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class STR
{
    static ConcurrentMap<String, Template> map = new ConcurrentHashMap<>();

    public static String format(String pattern, Object... params)
    {
        StringBuilder builder = new StringBuilder();
        map.computeIfAbsent(pattern, v -> {
            if (params.length != 0 && params[0] instanceof Map map)
            {
                List<Segment> segments = new LinkedList<>();
                int           start    = 0;
                int           pre      = 0;
                while (pre < v.length())
                {
                    start = v.indexOf("${", pre);
                    if (start == -1)
                    {
                        segments.add(new StringSegment(v.substring(pre, v.length())));
                        break;
                    }
                    else
                    {
                        int end = v.indexOf("}", start);
                        if (end == -1)
                        {
                            segments.add(new StringSegment(v.substring(pre, v.length())));
                            break;
                        }
                        else
                        {
                            segments.add(new StringSegment(v.substring(pre, start)));
                            segments.add(new NameParamSegment(v.substring(start + 2, end)));
                            pre = end + 1;
                        }
                    }
                }
                return new Template(segments.toArray(Segment[]::new));
            }
            else
            {
                List<Segment> segments   = new LinkedList<>();
                char[]        value      = v.toCharArray();
                int           start      = 0;
                int           pre        = 0;
                int           paramIndex = 0;
                while (pre < value.length)
                {
                    start = indexOfBrace(value, pre);
                    if (start == -1)
                    {
                        segments.add(new StringSegment(String.valueOf(value, pre, value.length - pre)));
                        break;
                    }
                    else
                    {
                        segments.add(new StringSegment(String.valueOf(value, pre, start - pre)));
                        segments.add(new ParamSegment(paramIndex));
                        paramIndex++;
                        pre = start + 2;
                    }
                }
                return new Template(segments.toArray(Segment[]::new));
            }
        }).output(builder, params);
        if (params.length != 0 && params[params.length - 1] instanceof Throwable e)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            PrintStream           printStream           = new PrintStream(byteArrayOutputStream, false, StandardCharsets.UTF_8);
            e.printStackTrace(printStream);
            printStream.flush();
            String content = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
            builder.append("\r\n").append(content);
        }
        return builder.toString();
    }

    static class Template
    {
        Segment[] segments;

        public Template(Segment[] segments)
        {
            this.segments = segments;
        }

        void output(StringBuilder builder, Object... params)
        {
            for (Segment segment : segments)
            {
                segment.output(builder, params);
            }
        }
    }

    static abstract class Segment
    {
        abstract void output(StringBuilder builder, Object... params);
    }

    static class StringSegment extends Segment
    {
        final String value;

        StringSegment(String value) {this.value = value;}

        @Override
        void output(StringBuilder builder, Object... params)
        {
            builder.append(value);
        }
    }

    static class ParamSegment extends Segment
    {
        final int index;

        ParamSegment(int index) {this.index = index;}

        @Override
        void output(StringBuilder builder, Object... params)
        {
            builder.append(params[index]);
        }
    }

    @Data
    static class NameParamSegment extends Segment
    {
        final String name;

        @Override
        void output(StringBuilder builder, Object... params)
        {
            builder.append(((Map<String, Object>) params[0]).get(name));
        }
    }

    /**
     * 从char数组中确定大括号的位置，如果不存在返回-1
     *
     * @param array
     * @param off
     * @return
     */
    private static int indexOfBrace(char[] array, int off)
    {
        int length = array.length - 1;
        for (int i = off; i < length; i++)
        {
            if (array[i] == '{' && array[i + 1] == '}')
            {
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream           printStream           = new PrintStream(byteArrayOutputStream, false, StandardCharsets.UTF_8);
        new Throwable().printStackTrace(printStream);
        printStream.flush();
        String content = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        System.out.println(content);
    }
}
