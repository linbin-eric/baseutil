package com.jfireframework.baseutil.bytecode;

import com.jfireframework.baseutil.bytecode.structure.Attribute.AttributeInfo;
import com.jfireframework.baseutil.bytecode.structure.FieldInfo;
import com.jfireframework.baseutil.bytecode.structure.MethodInfo;
import com.jfireframework.baseutil.bytecode.structure.constantinfo.*;
import com.jfireframework.baseutil.bytecode.util.ConstantType;

import java.util.Arrays;

public class ClassFileParser
{
    private int magic;
    private int minor_version;
    private int major_version;
    private int constant_pool_count;
    private ConstantInfo[] constant_pool;
    private int access_flags;
    private String this_class_name;
    private String super_class_name;
    private String[] interfaces;
    private FieldInfo[] fieldInfos;
    private MethodInfo[] methodInfos;
    private AttributeInfo[] attributeInfos;
    /////
    private int counter = 0;
    private byte[] bytes;

    public ClassFileParser(byte[] bytes)
    {
        this.bytes = bytes;
        readMagic();
        readminorVersion();
        readmajorVersion();
        readConstantPoolCount();
        readConstantInfo();
        readAccessFlags();
        readThisClass();
        readSuperClass();
        readInterfaces();
        readFieldInfos();
        readMethodInfos();
        readAttributeInfos();
    }

    public ClassFile parse()
    {
        ClassFile classFile = new ClassFile();
        classFile.setAccess_flags(access_flags);
        classFile.setMinor_version(minor_version);
        classFile.setMajor_version(major_version);
        classFile.setInterfaces(interfaces);
        classFile.setSuper_class_name(super_class_name);
        classFile.setThis_class_name(this_class_name);
        classFile.setAttributeInfos(attributeInfos);
        classFile.setFieldInfos(fieldInfos);
        classFile.setMethodInfos(methodInfos);
        return classFile;
    }

    private void readAttributeInfos()
    {
        int attribute_count = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        attributeInfos = new AttributeInfo[attribute_count];
        for (int i = 0; i < attributeInfos.length; i++)
        {
            attributeInfos[i] = AttributeInfo.parse(bytes, counter, constant_pool);
            counter += 2 + 4 + attributeInfos[i].getLength();
        }
    }

    private void readMethodInfos()
    {
        int method_count = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        methodInfos = new MethodInfo[method_count];
        for (int i = 0; i < method_count; i++)
        {
            methodInfos[i] = new MethodInfo();
            counter = methodInfos[i].resolve(bytes, counter, constant_pool);
        }
    }

    private void readFieldInfos()
    {
        int fields_cout = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        fieldInfos = new FieldInfo[fields_cout];
        for (int i = 0; i < fields_cout; i++)
        {
            fieldInfos[i] = new FieldInfo();
            counter = fieldInfos[i].resolve(bytes, counter, constant_pool);
        }
    }

    private void readInterfaces()
    {
        int interfaces_cout = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
        interfaces = new String[interfaces_cout];
        for (int i = 0; i < interfaces_cout; i++)
        {
            int interfaceIndex = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
            counter += 2;
            interfaces[i] = ((ClassInfo) constant_pool[interfaceIndex - 1]).getName();
        }
    }

    private void readSuperClass()
    {
        int super_class = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        super_class_name = ((ClassInfo) constant_pool[super_class - 1]).getName();
        counter += 2;
    }

    private void readThisClass()
    {
        int this_class = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        this_class_name = ((ClassInfo) constant_pool[this_class - 1]).getName();
        counter += 2;
    }

    private void readAccessFlags()
    {
        access_flags = ((bytes[counter] & 0xff) << 8) | (bytes[counter + 1] & 0xff);
        counter += 2;
    }

    private void readConstantInfo()
    {
        constant_pool = new ConstantInfo[constant_pool_count - 1];
        for (int i = 0; i < constant_pool.length; i++)
        {
            ConstantType constantType = readTag();
            ConstantInfo constantInfo;
            switch (constantType)
            {
                case Utf8:
                    constantInfo = new Utf8Info();
                    break;
                case Integer:
                    constantInfo = new IntegerInfo();
                    break;
                case Float:
                    constantInfo = new FloatInfo();
                    break;
                case Long:
                    constantInfo = new LongInfo();
                    break;
                case Double:
                    constantInfo = new DoubleInfo();
                    break;
                case Class:
                    constantInfo = new ClassInfo();
                    break;
                case String:
                    constantInfo = new StringInfo();
                    break;
                case FieldRef:
                    constantInfo = new FieldRefInfo();
                    break;
                case MethodRef:
                    constantInfo = new MethodRefInfo();
                    break;
                case InterfaceMethodref:
                    constantInfo = new InterfaceMethodRefInfo();
                    break;
                case NameAndType:
                    constantInfo = new NameAndTypeInfo();
                    break;
                case MethodHandle:
                    constantInfo = new MethodHandleInfo();
                    break;
                case MethodType:
                    constantInfo = new MethodTypeInfo();
                    break;
                case InvokeDynamic:
                    constantInfo = new InvokeDynamicInfo();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            counter = constantInfo.resolve(bytes, counter);
            constant_pool[i] = constantInfo;
            if ( constantInfo instanceof LongInfo || constantInfo instanceof DoubleInfo )
            {
                //JVM规范规定了如果遇到这两个常量类型，则编号多递增1
                i++;
            }
        }
        for (ConstantInfo constantInfo : constant_pool)
        {
            if ( constantInfo != null )
            {
                constantInfo.resolve(constant_pool);
            }
        }
    }

    private ConstantType readTag()
    {
        int tag = bytes[counter];
        counter++;
        return ConstantType.byteValue(tag);
    }

    private void readConstantPoolCount()
    {
        constant_pool_count = ((0xff & bytes[counter]) << 8) | (0xff & bytes[counter + 1]);
        counter += 2;
    }

    private void readmajorVersion()
    {
        major_version = ((0xff & bytes[counter]) << 8) | (0xff & bytes[counter + 1]);
        counter += 2;
    }

    private void readminorVersion()
    {
        minor_version = ((0xff & bytes[counter]) << 8) | (0xff & bytes[counter + 1]);
        counter += 2;
    }

    private void readMagic()
    {
        if ( (bytes[counter] & 0xff) == 0xca//
                && (bytes[counter + 1] & 0xff) == 0xfe//
                && (bytes[counter + 2] & 0xff) == 0xba//
                && (bytes[counter + 3] & 0xff) == 0xbe )
        {
            magic = 0xcafebabe;
            counter += 4;
        }
        else
        {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public String toString()
    {
        return "ClassFileParser{" + "minor_version=" + minor_version + ", major_version=" + major_version + ", constant_pool_count=" + constant_pool_count + ", constant_pool=" + Arrays.toString(constant_pool) + ", access_flags=" + access_flags + ", this_class_name='" + this_class_name + '\'' + ", super_class_name='" + super_class_name + '\'' + ", interfaces=" + Arrays.toString(interfaces) + ", fieldInfos=" + Arrays.toString(fieldInfos) + ", methodInfos=" + Arrays.toString(methodInfos) + ", attributeInfos=" + Arrays.toString(attributeInfos) + '}';
    }
}
