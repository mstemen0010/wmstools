package com.wms.util;


import java.util.*;

public class Sorter implements Comparator {
    public int compare(Object o1, Object o2) {
        String s1 = ((String)o1).toLowerCase();
        String s2 = ((String)o2).toLowerCase();
        return s1.compareTo(s2);
    }
}
