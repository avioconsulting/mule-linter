package org.apache.groovy.json.internal;

public class JsonNumber extends JsonValue {

    JsonNumber(Number value, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
    }

    Number getValue() {
        return (Number)value;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if(obj instanceof Number)
            return (obj).equals((Number)value);
        if (getClass() != obj.getClass())
            return false;
        JsonNumber other = (JsonNumber) obj;
        return other.getValue().equals(value);
    }
}
