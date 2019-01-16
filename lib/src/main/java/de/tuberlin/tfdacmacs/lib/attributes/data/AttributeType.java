package de.tuberlin.tfdacmacs.lib.attributes.data;

import lombok.Getter;

public enum AttributeType {
    NUMBER(Integer.class), STRING(String.class), BOOLEAN(Boolean.class);

    @Getter
    final Class<?> clazz;

    AttributeType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean matchesType(Object value) {
        return value.getClass().isAssignableFrom(getClazz());
    }

    public Object cast(String value) {
        switch (this) {
            case NUMBER:
                return Integer.valueOf(value);
            case STRING:
                return value;
            case BOOLEAN:
                return Boolean.valueOf(value);
        }
        return null;
    }
}
