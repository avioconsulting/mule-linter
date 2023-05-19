package org.apache.groovy.json.internal;

public class JsonNull extends JsonValue {

    public JsonNull(int lineNumber) {
        super(null, lineNumber);
    }

    public boolean equals(Object obj) {
        if(obj == null) {
            return true;
        }

        if(obj instanceof JsonNull) {
            return true;
        }

        return false;
    }

    public String toString() {
        return "null";
    }
}
