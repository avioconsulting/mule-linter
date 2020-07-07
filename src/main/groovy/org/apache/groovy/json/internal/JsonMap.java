package org.apache.groovy.json.internal;

import java.util.HashMap;

public class JsonMap extends HashMap {

    @Override
    public Object get(Object o) {
        if(o instanceof JsonString) {
            Object r = super.get(((JsonString) o).getValue());
            return r;
        } else {
            return super.get(o);
        }
    }

    @Override
    public Object put(Object o, Object o2) {
        if(o instanceof JsonString) {
            Object r = super.put(((JsonString) o).getValue(), o2);
            return r;
        } else {
            return super.put(o, o2);
        }
    }
}
