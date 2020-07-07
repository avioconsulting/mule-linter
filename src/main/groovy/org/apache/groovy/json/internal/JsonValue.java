package org.apache.groovy.json.internal;

public class JsonValue {

    protected Object value;
    protected int lineNumber;

    JsonValue() {
        value = null;
        lineNumber = 0;
    }

    JsonValue(Object value, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
    }

    Object getValue() {
        return value;
    }

    Integer getLineNumber() {
        return lineNumber;
    }

    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        if(value == null) {
            return "";
        } else {
            return value.toString();
        }
    }
}
