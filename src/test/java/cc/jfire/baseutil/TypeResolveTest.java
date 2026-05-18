package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.type.ParameterizedTypeResolver;
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
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(StringChild.class);

        Assert.assertEquals(String.class, resolveField(resolver, GenericAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, GenericAncestor.class, "values"));
    }

    @Test
    public void shouldResolveMiddleClassGenericFieldsAcrossMultipleLevels() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(MiddleGenericLeaf.class);

        Assert.assertEquals(Integer.class, resolveField(resolver, MiddleGenericAncestor.class, "rootValue"));
        Assert.assertEquals(Integer.class, resolveField(resolver, MiddleGenericParent.class, "middleValue"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "longList"), resolveField(resolver, MiddleGenericParent.class, "leftValues"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "longIntegerMap"), resolveField(resolver, MiddleGenericParent.class, "middleMap"));
    }

    @Test
    public void shouldResolveReorderedTypeVariables() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(ReorderedLeaf.class);

        Assert.assertEquals(Integer.class, resolveField(resolver, ReorderedAncestor.class, "first"));
        Assert.assertEquals(String.class, resolveField(resolver, ReorderedAncestor.class, "second"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "integerStringMap"), resolveField(resolver, ReorderedAncestor.class, "map"));
    }

    @Test
    public void shouldResolveNestedParameterizedTypeVariables() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(NestedLeaf.class);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, NestedAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "listOfIntegerList"), resolveField(resolver, NestedAncestor.class, "values"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringToListOfIntegerList"), resolveField(resolver, NestedAncestor.class, "nestedMap"));
    }

    @Test
    public void shouldResolveGenericArrayTypeVariables() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(ArrayLeaf.class);

        Assert.assertEquals(String[].class, resolveField(resolver, ArrayAncestor.class, "array"));

        Type resolvedListArray = resolveField(resolver, ArrayAncestor.class, "listArray");
        Assert.assertTrue(resolvedListArray instanceof GenericArrayType);
        GenericArrayType arrayType = (GenericArrayType) resolvedListArray;
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), arrayType.getGenericComponentType());
    }

    @Test
    public void shouldResolveWildcardBounds() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(WildcardLeaf.class);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, WildcardAncestor.class, "upperBounds"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "objectList"), resolveField(resolver, WildcardAncestor.class, "lowerBounds"));
    }

    @Test
    public void shouldKeepUnresolvedTypeVariableWhenLeafIsStillGeneric() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(OpenChild.class);

        Type resolved = resolveField(resolver, GenericAncestor.class, "value");
        Assert.assertTrue(resolved instanceof TypeVariable<?>);
        TypeVariable<?> typeVariable = (TypeVariable<?>) resolved;
        Assert.assertEquals("X", typeVariable.getName());
        Assert.assertEquals(OpenChild.class, typeVariable.getGenericDeclaration());
    }

    @Test
    public void shouldResolveFromParameterizedTypeSource() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(fieldType(SourceTypeHolder.class, "stringAncestor"));

        Assert.assertEquals(String.class, resolveField(resolver, GenericAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, GenericAncestor.class, "values"));
    }

    @Test
    public void shouldResolveParameterizedFieldClassTypeVariables() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(FieldHolderLeaf.class);

        Assert.assertEquals(Double.class, resolveField(resolver, Box.class, "element"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "doubleList"), resolveField(resolver, Box.class, "elements"));
    }

    @Test
    public void shouldResolveParameterizedFieldClassTypeVariablesFromGenericArrayField() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(GenericArrayFieldHolderLeaf.class);

        Assert.assertEquals(String.class, resolveField(resolver, Box.class, "element"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, Box.class, "elements"));
    }

    @Test
    public void shouldResolveFromGenericArrayTypeSource() throws Exception
    {
        Type source = fieldType(GenericArraySourceHolder.class, "stringBoxes");
        Assert.assertTrue(source instanceof GenericArrayType);

        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(source);

        Assert.assertEquals(String.class, resolveField(resolver, Box.class, "element"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringList"), resolveField(resolver, Box.class, "elements"));
    }


    @Test
    public void shouldResolveNestedParameterizedTypeVariablesFromGenericArrayTypeSource() throws Exception
    {
        Type source = fieldType(GenericArraySourceHolder.class, "nestedAncestors");
        Assert.assertTrue(source instanceof GenericArrayType);

        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(source);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, NestedAncestor.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "listOfIntegerList"), resolveField(resolver, NestedAncestor.class, "values"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringToListOfIntegerList"), resolveField(resolver, NestedAncestor.class, "nestedMap"));
    }

    @Test
    public void shouldResolveWildcardBoundsInGenericArrayFields() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(WildcardArrayLeaf.class);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerListArray"), resolveField(resolver, WildcardArrayAncestor.class, "upperBoundArray"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "objectListArray"), resolveField(resolver, WildcardArrayAncestor.class, "lowerBoundArray"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "stringToIntegerMapArray"), resolveField(resolver, WildcardArrayAncestor.class, "upperBoundMapArray"));
    }

    @Test
    public void shouldResolveExtendsWildcardFromGenericArrayTypeSource() throws Exception
    {
        Type source = fieldType(WildcardGenericArraySourceHolder.class, "upperBoxes");
        Assert.assertTrue(source instanceof GenericArrayType);

        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(source);

        Assert.assertEquals(Integer.class, resolveField(resolver, WildcardBox.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, WildcardBox.class, "values"));
    }

    @Test
    public void shouldResolveSuperWildcardFromGenericArrayTypeSource() throws Exception
    {
        Type source = fieldType(WildcardGenericArraySourceHolder.class, "lowerBoxes");
        Assert.assertTrue(source instanceof GenericArrayType);

        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(source);

        Assert.assertEquals(Object.class, resolveField(resolver, WildcardBox.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "objectList"), resolveField(resolver, WildcardBox.class, "values"));
    }

    @Test
    public void shouldResolveUnboundedWildcardFromParameterizedTypeSource() throws Exception
    {
        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(fieldType(WildcardGenericArraySourceHolder.class, "plainBox"));

        Assert.assertEquals(Object.class, resolveField(resolver, WildcardBox.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "objectList"), resolveField(resolver, WildcardBox.class, "values"));
    }

    @Test
    public void shouldResolveNestedExtendsWildcardFromGenericArrayTypeSource() throws Exception
    {
        Type source = fieldType(WildcardGenericArraySourceHolder.class, "nestedUpperBoxes");
        Assert.assertTrue(source instanceof GenericArrayType);

        ParameterizedTypeResolver resolver = new ParameterizedTypeResolver(source);

        assertTypeEquals(fieldType(ExpectedTypes.class, "integerList"), resolveField(resolver, WildcardBox.class, "value"));
        assertTypeEquals(fieldType(ExpectedTypes.class, "listOfIntegerList"), resolveField(resolver, WildcardBox.class, "values"));
    }

    private static Type resolveField(ParameterizedTypeResolver resolver, Class<?> clazz, String fieldName) throws NoSuchFieldException
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

    private static class WildcardArrayAncestor<T extends Number>
    {
        List<? extends T>[]        upperBoundArray;
        List<? super T>[]          lowerBoundArray;
        Map<String, ? extends T>[] upperBoundMapArray;
    }



    private static class WildcardArrayLeaf extends WildcardArrayAncestor<Integer>
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

    private static class GenericArrayFieldHolder<T>
    {
        Box<T>[] boxes;
    }

    private static class GenericArrayFieldHolderLeaf extends GenericArrayFieldHolder<String>
    {
    }

    private static class GenericArraySourceHolder
    {
        Box<String>[]                    stringBoxes;
        NestedAncestor<List<Integer>>[] nestedAncestors;
    }

    private static class WildcardGenericArraySourceHolder
    {
        WildcardBox<?>                         plainBox;
        WildcardBox<? extends Integer>[]       upperBoxes;
        WildcardBox<? super Integer>[]         lowerBoxes;
        WildcardBox<? extends List<Integer>>[] nestedUpperBoxes;
    }

    private static class Box<T>
    {
        T       element;
        List<T> elements;
    }

    private static class WildcardBox<T>
    {
        T       value;
        List<T> values;
    }

    private static class ExpectedTypes
    {
        List<String>                    stringList;
        List<Long>                      longList;
        List<Integer>                   integerList;
        List<Double>                    doubleList;
        List<Object>                    objectList;
        List<Integer>[]                 integerListArray;
        List<Object>[]                  objectListArray;
        List<List<Integer>>             listOfIntegerList;
        Map<Long, Integer>              longIntegerMap;
        Map<Integer, String>            integerStringMap;
        Map<String, Integer>[]          stringToIntegerMapArray;
        Map<String, List<List<Integer>>> stringToListOfIntegerList;
    }

}
