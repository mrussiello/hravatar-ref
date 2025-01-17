/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.previousresult;

import java.util.Comparator;

/**
 *
 * @author miker
 */
public class PreviousResultDateComparator implements Comparator<PreviousResult> 
{

    @Override
    public int compare(PreviousResult o1, PreviousResult o2) 
    {
        if(o1.getPreviousResultDate()!=null && o2.getPreviousResultDate()!=null )
            return o1.getPreviousResultDate().compareTo( o2.getPreviousResultDate() );
        
        return 0;
    }
    
}
