package de.tuberlin.tfdacmacs.crypto.benchmark.utils;

import java.util.Set;

public class SetUtils {

    public static <T> T first(Set<T> set) {
        return set.stream().findFirst().get();
    }
}
