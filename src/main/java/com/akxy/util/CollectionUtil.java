package com.akxy.util;

import java.util.Collection;

public class CollectionUtil {

    public static <E> int size(Collection<E> collection) {
        return collection == null ? 0 : collection.size();
    }
}
