package com.avioconsulting.mule.maven.formatter;


import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public enum FormatOptionsEnum {
    CONSOLE("console"),
    JSON("json");


    private static final Map<String, FormatOptionsEnum> mapValues = Collections.unmodifiableMap(initializeMapping());
    private String value;

    private FormatOptionsEnum(String code){
        this.value= code;
    }

    private static Map<String, FormatOptionsEnum> initializeMapping() {
        Map map = new HashMap<>();
        for (FormatOptionsEnum fo : FormatOptionsEnum.values()) {
            map.put(fo.value.toLowerCase(), fo);
        }
        return map;
    }

    public static Optional<FormatOptionsEnum> fromString(String formatString){
        return Optional.ofNullable(mapValues.get(formatString.toLowerCase()));

    }

    public Object getValue() {
        return this.value;
    }
}
