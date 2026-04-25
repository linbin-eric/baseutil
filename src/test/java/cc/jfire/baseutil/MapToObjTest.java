package cc.jfire.baseutil;

import lombok.Data;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MapToObjTest
{
    @Test
    public void testScalarAndArrayConversionFromYaml() throws Exception
    {
        ScalarArrayConfig config = readObj("scalarConfig", ScalarArrayConfig.class);

        Assert.assertEquals(7, config.getPrimitiveByte());
        Assert.assertEquals(42, config.getPrimitiveInt());
        Assert.assertEquals(12, config.getPrimitiveShort());
        Assert.assertEquals(1234567890123L, config.getPrimitiveLong());
        Assert.assertEquals(3.5f, config.getPrimitiveFloat(), 0.0001f);
        Assert.assertEquals(7.25d, config.getPrimitiveDouble(), 0.0001d);
        Assert.assertEquals('Z', config.getPrimitiveChar());
        Assert.assertTrue(config.isPrimitiveBool());
        Assert.assertEquals(Byte.valueOf((byte) 8), config.getBoxedByte());
        Assert.assertEquals(Integer.valueOf(43), config.getBoxedInt());
        Assert.assertEquals(Short.valueOf((short) 13), config.getBoxedShort());
        Assert.assertEquals(Long.valueOf(1234567890124L), config.getBoxedLong());
        Assert.assertEquals(Float.valueOf(4.5f), config.getBoxedFloat());
        Assert.assertEquals(Double.valueOf(8.25d), config.getBoxedDouble());
        Assert.assertEquals(Character.valueOf('Y'), config.getBoxedChar());
        Assert.assertEquals(Boolean.FALSE, config.getBoxedBool());
        Assert.assertEquals("demo", config.getName());
        Assert.assertEquals(new BigDecimal("12345.67"), config.getAmount());
        Assert.assertEquals(Status.ENABLED, config.getStatus());
        Assert.assertEquals("c-1", config.getChild().getCode());
        Assert.assertEquals(5, config.getChild().getLevel());

        Assert.assertArrayEquals(new byte[] { 1, 2, 3 }, config.getPrimitiveBytes());
        Assert.assertArrayEquals(new int[] { 10, 20, 30 }, config.getPrimitiveInts());
        Assert.assertArrayEquals(new short[] { 4, 5 }, config.getPrimitiveShorts());
        Assert.assertArrayEquals(new long[] { 100L, 200L }, config.getPrimitiveLongs());
        Assert.assertArrayEquals(new float[] { 1.5f, 2.5f }, config.getPrimitiveFloats(), 0.0001f);
        Assert.assertArrayEquals(new double[] { 6.5d, 7.5d }, config.getPrimitiveDoubles(), 0.0001d);
        Assert.assertArrayEquals(new char[] { 'a', 'b', 'c' }, config.getPrimitiveChars());
        Assert.assertArrayEquals(new boolean[] { true, false, true }, config.getPrimitiveBools());
        Assert.assertArrayEquals(new String[] { "alpha", "beta" }, config.getNames());
        Assert.assertArrayEquals(new Status[] { Status.ENABLED, Status.DISABLED }, config.getStatuses());
        Assert.assertEquals(2, config.getChildren().length);
        Assert.assertEquals("c-2", config.getChildren()[0].getCode());
        Assert.assertEquals(6, config.getChildren()[0].getLevel());
        Assert.assertEquals("c-3", config.getChildren()[1].getCode());
        Assert.assertEquals(7, config.getChildren()[1].getLevel());
    }

    @Test
    public void testCollectionAndMapConversionFromYaml() throws Exception
    {
        CollectionMapConfig config = readObj("collectionConfig", CollectionMapConfig.class);

        Assert.assertEquals(Arrays.asList(11, 12, 13), config.getNumbers());
        Assert.assertEquals(new HashSet<>(Arrays.asList("north", "south")), config.getLabels());
        Assert.assertEquals(Arrays.asList(new BigDecimal("1.50"), new BigDecimal("2.75")), config.getAmounts());
        Assert.assertEquals(Arrays.asList(Status.ENABLED, Status.DISABLED), config.getStatuses());
        Assert.assertEquals(2, config.getChildren().size());
        Assert.assertEquals("cc-1", config.getChildren().get(0).getCode());
        Assert.assertEquals(11, config.getChildren().get(0).getLevel());
        Assert.assertEquals("cc-2", config.getChildren().get(1).getCode());
        Assert.assertEquals(12, config.getChildren().get(1).getLevel());

        Assert.assertEquals("wrap-1", config.getWrappedChild().getValue().getCode());
        Assert.assertEquals(21, config.getWrappedChild().getValue().getLevel());
        Assert.assertEquals(2, config.getWrappedChild().getValues().size());
        Assert.assertEquals("wrap-2", config.getWrappedChild().getValues().get(0).getCode());
        Assert.assertEquals("wrap-5", config.getWrappedChild().getNamed().get("second").getCode());

        Assert.assertEquals(Integer.valueOf(95), config.getScoreMap().get("math"));
        Assert.assertEquals(Integer.valueOf(88), config.getScoreMap().get("english"));
        Assert.assertEquals("map-1", config.getChildMap().get("left").getCode());
        Assert.assertEquals(32, config.getChildMap().get("right").getLevel());
        Assert.assertEquals(Arrays.asList(1, 3, 5), config.getGroupedNumbers().get("odd"));
        Assert.assertEquals(Arrays.asList(2, 4), config.getGroupedNumbers().get("even"));
        Assert.assertEquals(Integer.valueOf(10), config.getNestedScoreMap().get("first").get("a"));
        Assert.assertEquals(Integer.valueOf(20), config.getNestedScoreMap().get("first").get("b"));
        Assert.assertEquals(Integer.valueOf(30), config.getNestedScoreMap().get("second").get("c"));
        Assert.assertEquals(2, config.getNumberEntries().size());
        Assert.assertEquals(Integer.valueOf(1), config.getNumberEntries().get(0).get("first"));
        Assert.assertEquals(Integer.valueOf(2), config.getNumberEntries().get(0).get("second"));
        Assert.assertEquals(Integer.valueOf(3), config.getNumberEntries().get(1).get("third"));
    }

    @Test
    public void testTopLevelParameterizedTypeConversionFromYaml() throws Exception
    {
        Type type = GenericTypeHolder.class.getDeclaredField("genericContainer").getGenericType();
        GenericContainer<Child> container = (GenericContainer<Child>) readObj("genericContainer", type);

        Assert.assertEquals("generic-1", container.getValue().getCode());
        Assert.assertEquals(41, container.getValue().getLevel());
        Assert.assertEquals(2, container.getValues().size());
        Assert.assertEquals("generic-2", container.getValues().get(0).getCode());
        Assert.assertEquals("generic-3", container.getValues().get(1).getCode());
        Assert.assertEquals(2, container.getNamed().size());
        Assert.assertEquals(44, container.getNamed().get("first").getLevel());
        Assert.assertEquals("generic-5", container.getNamed().get("second").getCode());
    }

    @SuppressWarnings("unchecked")
    private static <T> T readObj(String root, Type type) throws Exception
    {
        Map<String, Object> yamlMap = loadYamlMap();
        return (T) MapToObj.toObj(type, (Map<String, Object>) yamlMap.get(root));
    }

    private static Map<String, Object> loadYamlMap() throws IOException
    {
        try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("map_to_obj.yml"))
        {
            Assert.assertNotNull("map_to_obj.yml 不存在", inputStream);
            byte[] bytes = IoUtil.readAllBytes(inputStream);
            return new YamlReader(new String(bytes, StandardCharsets.UTF_8)).getMapWithIndentStructure();
        }
    }

    private enum Status
    {
        ENABLED,
        DISABLED
    }

    @Data
    public static class Child
    {
        private String code;
        private int    level;
    }

    @Data
    public static class ScalarArrayConfig
    {
        private byte        primitiveByte;
        private int         primitiveInt;
        private short       primitiveShort;
        private long        primitiveLong;
        private float       primitiveFloat;
        private double      primitiveDouble;
        private char        primitiveChar;
        private boolean     primitiveBool;
        private Byte        boxedByte;
        private Integer     boxedInt;
        private Short       boxedShort;
        private Long        boxedLong;
        private Float       boxedFloat;
        private Double      boxedDouble;
        private Character   boxedChar;
        private Boolean     boxedBool;
        private String      name;
        private BigDecimal  amount;
        private Status      status;
        private Child       child;
        private byte[]      primitiveBytes;
        private int[]       primitiveInts;
        private short[]     primitiveShorts;
        private long[]      primitiveLongs;
        private float[]     primitiveFloats;
        private double[]    primitiveDoubles;
        private char[]      primitiveChars;
        private boolean[]   primitiveBools;
        private String[]    names;
        private Status[]    statuses;
        private Child[]     children;
    }

    @Data
    public static class CollectionMapConfig
    {
        private List<Integer>              numbers;
        private Set<String>                labels;
        private List<BigDecimal>           amounts;
        private List<Status>               statuses;
        private List<Child>                children;
        private GenericContainer<Child>    wrappedChild;
        private Map<String, Integer>       scoreMap;
        private Map<String, Child>         childMap;
        private Map<String, List<Integer>> groupedNumbers;
        private Map<String, Map<String, Integer>> nestedScoreMap;
        private List<Map<String, Integer>> numberEntries;
    }

    @Data
    public static class GenericContainer<T>
    {
        private T              value;
        private List<T>        values;
        private Map<String, T> named;
    }

    public static class GenericTypeHolder
    {
        private GenericContainer<Child> genericContainer;
    }
}
