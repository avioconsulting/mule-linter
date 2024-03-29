package org.apache.groovy.json.internal;

import java.util.HashMap;

public class JsonMap extends HashMap {

    protected int lineNumber;

    public JsonMap(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    @Override
    public Object get(Object o) {
        if(o instanceof JsonString) {
            return super.get(((JsonString) o).getValue());
        } else {
            return super.get(o);
        }
    }

    Integer getLineNumber() {
        return lineNumber;
    }

    @Override
    public Object put(Object o, Object o2) {
        if(o instanceof JsonString) {
            return super.put(((JsonString) o).getValue(), o2);
        } else {
            return super.put(o, o2);
        }
    }
}
