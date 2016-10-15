package com.example.konyash.fuzzer;

public class Value_Generator {
    public static Object generateValue(String type) {
        switch (type){
            case "int":                 return generateInteger();
            case "java.lang.String":    return generateString();
            default:                    return new Object();
        }
    }

    private static Object generateInteger()
    {
        return new Integer(0);
    }

    private static Object generateString()
    {
        return new String("");
    }
}
