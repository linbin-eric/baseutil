package com.jfireframework.baseutil.smc.el;

import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;

public class SmcEl
{
    public static String createValue(String el, Class<?>[] types)
    {
        return el;
    }
    
    public static String createIf(String el, Class<?>[] types)
    {
        String[] paramNames = new String[types.length];
        for (int i = 0; i < paramNames.length; i++)
        {
            paramNames[i] = String.valueOf(i);
        }
        return createIf(el, paramNames, types);
    }
    
    public static String createIf(String el, String[] paramNames, Class<?>[] types)
    {
        StringCache cache = new StringCache();
        int index = 0;
        int end = 0;
        while (index < el.length())
        {
            char c = el.charAt(index);
            if (c == '$')
            {
                end = getEndFlag(el, index);
                String content = el.substring(index + 1, end);
                cache.append(SmcHelper.buildInvoke(content, paramNames, types));
                Class<?> type = SmcHelper.getType(content, paramNames, types);
                if (type == String.class)
                {
                    index = end;
                    while ((c = el.charAt(index)) == ' ')
                    {
                        index += 1;
                    }
                    if (c == '=' && el.charAt(index + 1) == '=')
                    {
                        index += 2;
                        do
                        {
                            end = getEndFlag(el, index + 1);
                            content = el.substring(index, end);
                            index = end;
                            content = content.trim();
                        } while ("".equals(content));
                        if (content.equals("null"))
                        {
                            cache.append("==null");
                        }
                        else
                        {
                            if (content.charAt(0) != '"' || content.endsWith("\"") == false)
                            {
                                throw new UnsupportedOperationException(StringUtil.format("解析条件语句存在问题，其if判断中存在对String的比较，但是却没有使用'\"'将字符串包围。请检查:{}", el));
                            }
                            cache.append(".equals(").append(content).append(")");
                        }
                    }
                    else if (c == '!' && el.charAt(index + 1) == '=')
                    {
                        index += 2;
                        do
                        {
                            end = getEndFlag(el, index + 1);
                            content = el.substring(index, end);
                            index = end;
                            content = content.trim();
                        } while ("".equals(content));
                        if (content.equals("null"))
                        {
                            cache.append("!=null");
                        }
                        else
                        {
                            if (content.charAt(0) != '"' || content.endsWith("\"") == false)
                            {
                                throw new UnsupportedOperationException(StringUtil.format("解析条件语句存在问题，其if判断中存在对String的比较，但是却没有使用'\"'将字符串包围。请检查:{}", el));
                            }
                            cache.append(".equals(").append(content).append(")==false");
                        }
                        index = end;
                    }
                    else
                    {
                        throw new UnsupportedOperationException();
                    }
                }
            }
            else if (c == ' ')
            {
                cache.append(' ');
                index += 1;
                continue;
            }
            else
            {
                end = getEndFlag(el, index + 1);
                String content = el.substring(index, end);
                cache.append(content);
            }
            index = end;
        }
        return cache.toString();
    }
    
    private static int getEndFlag(String sql, int start)
    {
        while (start < sql.length())
        {
            char c = sql.charAt(start);
            if (c == '>' || c == '<' || c == '!' || c == '=' || c == ' ' || c == ',' //
                    || c == '#' || c == '+' || c == '-' || c == '(' || c == ')' || c == ']' || c == '[')
            {
                break;
            }
            start++;
        }
        return start;
    }
    
}
