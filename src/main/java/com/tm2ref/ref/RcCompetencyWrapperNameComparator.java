/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import java.util.Comparator;

/**
 *
 * @author miker_000
 */
public class RcCompetencyWrapperNameComparator implements Comparator<RcCompetencyWrapper> {

    
    @Override
    public int compare(RcCompetencyWrapper o1, RcCompetencyWrapper o2) 
    {        
        if( o1.getRcCompetency()!=null && o2.getRcCompetency()!=null )
            return o1.getRcCompetency().getName().compareTo(o2.getRcCompetency().getName());
        return o1.compareTo(o2);
    }
    
    
}
