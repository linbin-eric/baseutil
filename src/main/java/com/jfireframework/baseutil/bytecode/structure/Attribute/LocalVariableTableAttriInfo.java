package com.jfireframework.baseutil.bytecode.structure.Attribute;

import com.jfireframework.baseutil.bytecode.structure.constantinfo.ConstantInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.Utf8Info;

import java.util.Arrays;
import java.util.Comparator;

public class LocalVariableTableAttriInfo extends AttributeInfo
{
    private int local_variable_table_length;
    private LocalVariableTableEntry[] entries;

    public LocalVariableTableAttriInfo(String name, int length)
    {
        super(name, length);
    }

    @Override
    public String toString()
    {
        return "LocalVariableTableAttriInfo{" + "entries=" + Arrays.toString(entries) + '}';
    }

    @Override
    protected void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
    {
        local_variable_table_length = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter+=2;
        entries = new LocalVariableTableEntry[local_variable_table_length];
        for (int i = 0; i < entries.length; i++)
        {
            entries[i] = new LocalVariableTableEntry();
            entries[i].resolve(bytes, counter, constantInfos);
            counter += 10;
        }
        Arrays.sort(entries, new Comparator<LocalVariableTableEntry>()
        {
            @Override
            public int compare(LocalVariableTableEntry o1, LocalVariableTableEntry o2)
            {
                return o1.getIndex() - o2.getIndex();
            }
        });
    }

    public class LocalVariableTableEntry
    {
        private int start_pc;
        private int length;
        private int name_index;
        private int descriptor_index;
        private int index;
        private String name;

        void resolve(byte[] bytes, int counter, ConstantInfo[] constantInfos)
        {
            //忽略start_pc
            counter += 2;
            //忽略length
            counter += 2;
            name_index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            name = ((Utf8Info) constantInfos[name_index - 1]).getValue();
            //忽略descriptor_index
            counter += 2;
            index = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        }

        public int getIndex()
        {
            return index;
        }

        public String getName()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return "LocalVariableTableEntry{" + "index=" + index + ", name='" + name + '\'' + '}';
        }
    }

    public LocalVariableTableEntry[] getEntries()
    {
        return entries;
    }
}
