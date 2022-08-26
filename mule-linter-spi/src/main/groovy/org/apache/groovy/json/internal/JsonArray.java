package org.apache.groovy.json.internal;

import java.util.ArrayList;

public class JsonArray<T> extends ArrayList {

    private Integer lineNumber;

    JsonArray(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    @Override
    public int indexOf(Object o) {
        if(o instanceof String) {
            return super.indexOf(new JsonString((String)o, 0));
        } else if(o instanceof Boolean) {
            return super.indexOf(new JsonBoolean((Boolean)o, 0));
        } else if(o == null) {
            return super.indexOf(new JsonNull(0));
        } else if(o instanceof Number) {
            return super.indexOf(new JsonNumber((Number)o, 0));
        }

        return super.indexOf(o);
    }
}
