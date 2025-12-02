package xin.nb1.baseutil.poi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ExcelProperty
{
    String[] value();

    Class<? extends CellReader> reader() default CellReader.class;

    Class<? extends CellWriter> writer() default CellWriter.class;
}
