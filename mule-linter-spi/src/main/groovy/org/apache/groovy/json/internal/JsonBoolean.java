package org.apache.groovy.json.internal;

public class JsonBoolean extends JsonValue {

    JsonBoolean(Boolean value, int lineNumber) {
        this.value = value;
        this.lineNumber = lineNumber;
    }

    Boolean getValue() {
        return (Boolean)value;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if(obj instanceof Boolean)
            return (obj).equals((Boolean)value);
        if (getClass() != obj.getClass())
            return false;
        JsonBoolean other = (JsonBoolean) obj;
        return other.getValue().equals(value);
    }

}
