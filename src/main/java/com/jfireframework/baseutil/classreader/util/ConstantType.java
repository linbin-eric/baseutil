package com.jfireframework.baseutil.classreader.util;

public enum ConstantType
{
    Utf8(1),//
    Integer(3),//
    Float(4),//
    Long(5),//
    Double(6),//
    Class(7),//
    String(8),//
    FieldRef(9),//
    MethodRef(10),//
    InterfaceMethodref(11), NameAndType(12),//
    MethodHandle(15),//
    MethodType(16),//
    InvokeDynamic(17),
    //
    ;
    private final int value;

    ConstantType(int value)
    {
        this.value = value;
    }

    public int value()
    {
        return value;
    }

    public static ConstantType byteValue(int value)
    {
        switch (value)
        {
            case 1:
                return Utf8;
            case 3:
                return Integer;
            case 4:
                return Float;
            case 5:
                return Long;
            case 6:
                return Double;
            case 7:
                return Class;
            case 8:
                return String;
            case 9:
                return FieldRef;
            case 10:
                return MethodRef;
            case 11:
                return InterfaceMethodref;
            case 12:
                return NameAndType;
            case 15:
                return MethodHandle;
            case 16:
                return MethodType;
            case 17:
                return InvokeDynamic;
            default:
                throw new IllegalArgumentException();
        }
    }
}
