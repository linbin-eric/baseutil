package cc.jfire.baseutil;

import cc.jfire.baseutil.reflect.ReflectUtil;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class CsvTest
{
    public static <T> List<T> readCsv(String path, Class<T> type, Supplier<T> supplier)
    {
        try (BufferedReader reader = IoUtil.getReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8))
        {
            return CsvUtil.read(reader, type, supplier);
        }
        catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException |
               IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
        return new ArrayList<>();
    }

    public static <T> List<T> readCsv(String path, Class<T> type, Supplier<T> supplier, Function<String, String> headerName)
    {
        try (BufferedReader reader = IoUtil.getReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(path), StandardCharsets.UTF_8))
        {
            return CsvUtil.read(reader, type, supplier, headerName);
        }
        catch (IOException | NoSuchMethodException | InvocationTargetException | InstantiationException |
               IllegalAccessException e)
        {
            ReflectUtil.throwException(e);
        }
        return new ArrayList<>();
    }

    @Test
    public void test(){
        Function<String, String> headerName = str -> {
            if (str == null || str.isEmpty())
            {
                return str;
            }
            StringBuilder snakeCase = new StringBuilder();
            for (int i = 0; i < str.length(); i++)
            {
                char c = str.charAt(i);
                if (Character.isUpperCase(c))
                {
                    snakeCase.append("_");
                }
                snakeCase.append(Character.toLowerCase(c));
            }
            return snakeCase.toString();
        };
        List<CheckInsuranceRule>       checkInsuranceRules       = readCsv("csv/check_insurance_rule.csv", CheckInsuranceRule.class, CheckInsuranceRule::new, headerName);
        List<CheckInsuranceRuleDetail> checkInsuranceRuleDetails =readCsv("csv/check_insurance_rule_detail.csv", CheckInsuranceRuleDetail.class, CheckInsuranceRuleDetail::new, headerName);
        List<CheckMedicalRule>         checkMedicalRules         = readCsv("csv/check_medical_rule.csv", CheckMedicalRule.class, CheckMedicalRule::new, headerName);
        List<CheckMedicalRuleDetail>   checkMedicalRuleDetails   = readCsv("csv/check_medical_rule_detail.csv", CheckMedicalRuleDetail.class, CheckMedicalRuleDetail::new, headerName);
    }

    class CheckInsuranceRule
    {
        private Integer id;
        private String  name;
        private String  ruleBaseCode;
        private String  markWordA;
        private String  markWordB;
        private String  markWordC;
        private int     sn;
    }

    class CheckInsuranceRuleDetail
    {
        private Integer id;
        private Integer ruleId;
        private String  detailName;
        private String  detailDesc;
        private String  detailField;
        private String  detailSubField;
        private int     sn;
        private String  markWordA;
        private String  markWordB;
        private String  impl;
        private int     level;
    }

    class CheckMedicalRule
    {
        private Integer id;
        private String  name;
        private String  ruleBaseCode;
        private String  markWordA;
        private String  markWordB;
        private String  markWordC;
        private int     sn;
    }
    class CheckMedicalRuleDetail
    {
        private Integer id;
        private Integer ruleId;
        private String  detailName;
        private String  detailDesc;
        private String  detailField;
        private int     sn;
        private String  markWordA;
        private String  markWordB;
        private String  impl;
        private int     level;
    }

}
