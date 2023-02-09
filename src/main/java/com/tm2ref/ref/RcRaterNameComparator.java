/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcRater;
import java.util.Comparator;

/**
 *
 * @author miker_000
 */
public class RcRaterNameComparator implements Comparator<RcRater> {

    @Override
    public int compare(RcRater o1, RcRater o2) {
        
        if( o1.getUser()==null || o2.getUser()==null)
            return 0;
        
        if( o1.getAnonymousName()!=null && !o1.getAnonymousName().isBlank() && o2.getAnonymousName()!=null && o2.getAnonymousName()!=null )
            return o1.getAnonymousName().compareTo( o2.getAnonymousName() );
        
        return o1.getUser().getLastName().compareTo( o2.getUser().getLastName() );
    }
    
    
}
