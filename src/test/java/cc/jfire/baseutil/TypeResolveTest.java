package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.type.TypeResolver;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;
import java.util.Map;

public class TypeResolveTest
{
    @Test
    public void shouldResolveSuperclassTypeVariableAcrossMultipleLevels() throws Exception
    {
        TypeResolver resolver = new TypeResolver(StringChild.class);

        Assert.assertEquals(String.class, resolveField(resolver, GenericAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, GenericAncestor.class, "values"));
    }

    @Test
    public void shouldResolveMiddleClassGenericFieldsAcrossMultipleLevels() throws Exception
    {
        TypeResolver resolver = new TypeResolver(MiddleGenericLeaf.class);

        Assert.assertEquals(Integer.class, resolveField(resolver, MiddleGenericAncestor.class, "rootValue"));
        Assert.assertEquals(Integer.class, resolveField(resolver, MiddleGenericParent.class, "middleValue"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "longList"), resolveField(resolver, MiddleGenericParent.class, "leftValues"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "longIntegerMap"), resolveField(resolver, MiddleGenericParent.class, "middleMap"));
    }

    @Test
    public void shouldResolveReorderedTypeVariables() throws Exception
    {
        TypeResolver resolver = new TypeResolver(ReorderedLeaf.class);

        Assert.assertEquals(Integer.class, resolveField(resolver, ReorderedAncestor.class, "first"));
        Assert.assertEquals(String.class, resolveField(resolver, ReorderedAncestor.class, "second"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "integerStringMap"), resolveField(resolver, ReorderedAncestor.class, "map"));
    }

    @Test
    public void shouldResolveNestedParameterizedTypeVariables() throws Exception
    {
        TypeResolver resolver = new TypeResolver(NestedLeaf.class);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, NestedAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "listOfIntegerList"), resolveField(resolver, NestedAncestor.class, "values"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringToListOfIntegerList"), resolveField(resolver, NestedAncestor.class, "nestedMap"));
    }

    @Test
    public void shouldResolveGenericArrayTypeVariables() throws Exception
    {
        TypeResolver resolver = new TypeResolver(ArrayLeaf.class);

        Assert.assertEquals(String[].class, resolveField(resolver, ArrayAncestor.class, "array"));

        Type resolvedListArray = resolveField(resolver, ArrayAncestor.class, "listArray");
        Assert.assertTrue(resolvedListArray instanceof GenericArrayType);
        GenericArrayType arrayType = (GenericArrayType) resolvedListArray;
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), arrayType.getGenericComponentType());
    }

    @Test
    public void shouldResolveWildcardBounds() throws Exception
    {
        TypeResolver resolver = new TypeResolver(WildcardLeaf.class);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, WildcardAncestor.class, "upperBounds"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "objectList"), resolveField(resolver, WildcardAncestor.class, "lowerBounds"));
    }

    @Test
    public void shouldKeepUnresolvedTypeVariableWhenLeafIsStillGeneric() throws Exception
    {
        TypeResolver resolver = new TypeResolver(OpenChild.class);

        Type resolved = resolveField(resolver, GenericAncestor.class, "value");
        Assert.assertTrue(resolved instanceof TypeVariable<?>);
        TypeVariable<?> typeVariable = (TypeVariable<?>) resolved;
        Assert.assertEquals("X", typeVariable.getName());
        Assert.assertEquals(OpenChild.class, typeVariable.getGenericDeclaration());
    }

    @Test
    public void shouldResolveFromParameterizedTypeSource() throws Exception
    {
        TypeResolver resolver = new TypeResolver(fieldType(SourceTypeHolder.class, "stringAncestor"));

        Assert.assertEquals(String.class, resolveField(resolver, GenericAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, GenericAncestor.class, "values"));
    }

    @Test
    public void shouldResolveParameterizedFieldClassTypeVariables() throws Exception
    {
        TypeResolver resolver = new TypeResolver(FieldHolderLeaf.class);

        Assert.assertEquals(Double.class, resolveField(resolver, Box.class, "element"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "doubleList"), resolveField(resolver, Box.class, "elements"));
    }

    private static Type resolveField(TypeResolver resolver, Class<?> clazz, String fieldName) throws NoSuchFieldException
    {
        return resolver.resolveType(fieldType(clazz, fieldName));
    }

    private static Type fieldType(Class<?> clazz, String fieldName) throws NoSuchFieldException
    {
        return clazz.getDeclaredField(fieldName).getGenericType();
    }

    private static void assertTypeEquals(Type expected, Type actual)
    {
        Assert.assertEquals(expected.getTypeName(), actual.getTypeName());
        Assert.assertEquals(expected, actual);
    }

    private static class GenericAncestor<T>
    {
        T       value;
        List<T> values;
    }

    private static class GenericParent<E> extends GenericAncestor<E>
    {
    }

    private static class StringChild extends GenericParent<String>
    {
    }

    private static class MiddleGenericAncestor<V>
    {
        V rootValue;
    }

    private static class MiddleGenericParent<L, R> extends MiddleGenericAncestor<R>
    {
        R         middleValue;
        List<L>   leftValues;
        Map<L, R> middleMap;
    }

    private static class MiddleGenericChild<T> extends MiddleGenericParent<Long, T>
    {
    }

    private static class MiddleGenericLeaf extends MiddleGenericChild<Integer>
    {
    }

    private static class ReorderedAncestor<A, B>
    {
        A         first;
        B         second;
        Map<A, B> map;
    }

    private static class ReorderedParent<X, Y> extends ReorderedAncestor<Y, X>
    {
    }

    private static class ReorderedLeaf extends ReorderedParent<String, Integer>
    {
    }

    private static class NestedAncestor<T>
    {
        T                    value;
        List<T>              values;
        Map<String, List<T>> nestedMap;
    }

    private static class NestedParent<E> extends NestedAncestor<List<E>>
    {
    }

    private static class NestedLeaf extends NestedParent<Integer>
    {
    }

    private static class ArrayAncestor<T>
    {
        T[]       array;
        List<T>[] listArray;
    }

    private static class ArrayLeaf extends ArrayAncestor<String>
    {
    }

    private static class WildcardAncestor<T extends Number>
    {
        List<? extends T> upperBounds;
        List<? super T>   lowerBounds;
    }

    private static class WildcardLeaf extends WildcardAncestor<Integer>
    {
    }

    private static class OpenChild<X> extends GenericAncestor<X>
    {
    }

    private static class SourceTypeHolder
    {
        GenericAncestor<String> stringAncestor;
    }

    private static class FieldHolder<T>
    {
        Box<T> box;
    }

    private static class FieldHolderLeaf extends FieldHolder<Double>
    {
    }

    private static class Box<T>
    {
        T       element;
        List<T> elements;
    }

    private static class ExpectedTypes
    {
        List<String>                    stringList;
        List<Long>                      longList;
        List<Integer>                   integerList;
        List<Double>                    doubleList;
        List<Object>                    objectList;
        List<List<Integer>>             listOfIntegerList;
        Map<Long, Integer>              longIntegerMap;
        Map<Integer, String>            integerStringMap;
        Map<String, List<List<Integer>>> stringToListOfIntegerList;
    }

}
