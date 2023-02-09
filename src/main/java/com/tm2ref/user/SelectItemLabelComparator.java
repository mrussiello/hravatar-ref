/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.user;

import java.util.Comparator;
import jakarta.faces.model.SelectItem;

/**
 *
 * @author miker_000
 */
public class SelectItemLabelComparator implements Comparator<SelectItem> {

    @Override
    public int compare(SelectItem o1, SelectItem o2) {
        
        if( o1.getLabel()!=null && o2.getLabel()!=null )
            return o1.getLabel().compareTo( o2.getLabel() );
        
        return 0;
    }
    
}
