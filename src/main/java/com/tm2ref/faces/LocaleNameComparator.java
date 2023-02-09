/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.faces;

import java.util.Comparator;
import java.util.Locale;

/**
 *
 * @author Mike
 */
public class LocaleNameComparator implements Comparator<Locale> {

    @Override
    public int compare(Locale l1, Locale l2) {

        String s1 = l1.getDisplayLanguage() + l1.getDisplayCountry();
        String s2 = l2.getDisplayLanguage() + l2.getDisplayCountry();

        return s1.compareTo(s2);
    }



}
