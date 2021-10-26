package org.apache.groovy.json.internal;

public class JsonString extends JsonValue {

    JsonString(String value, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
    }

    String getValue() {
        return (String)value;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if(obj instanceof String)
            return (obj).equals((String)value);
        if (getClass() != obj.getClass())
            return false;
        JsonString other = (JsonString) obj;
            return other.getValue().equals(value);
    }
}
