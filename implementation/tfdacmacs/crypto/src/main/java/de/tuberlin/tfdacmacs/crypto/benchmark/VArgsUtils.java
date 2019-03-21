package de.tuberlin.tfdacmacs.crypto.benchmark;

public class VArgsUtils {

    public static <T> T extract(Object[] objects, int pos) {
        return (T) objects[pos];
    }

    public static <T> T extract(Object[] objects, int pos, Class<T> clazz) {
        return (T) objects[pos];
    }
}
