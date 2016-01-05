package com.wms.util;

import java.util.*;


public class ListSorter {
    public void sort(java.awt.List l) {
        Sorter srt = new Sorter();
        String[] sa = l.getItems();
        // sort the array
        Arrays.sort(sa, srt);
        if (sa != null) {
            l.clear();
            for (int i = 0; i < sa.length; i++) {
                if (sa[i] != null) 
                    l.add(sa[i]);
            }
        }             
    }
    
    
}