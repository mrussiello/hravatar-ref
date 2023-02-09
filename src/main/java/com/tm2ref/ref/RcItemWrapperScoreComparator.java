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
public class RcItemWrapperScoreComparator implements Comparator<RcItemWrapper> {

    boolean highTop = false;
    RcTopBottomSrcType srcTyp;
    long candRcRaterId;
    
    public RcItemWrapperScoreComparator( boolean highTop, RcTopBottomSrcType srcTyp, long candRcRaterId )
    {
        if( srcTyp==null )
            srcTyp = RcTopBottomSrcType.OTHERS;
        
        this.highTop=highTop;
        this.srcTyp=srcTyp;
        this.candRcRaterId=candRcRaterId;
        
    }
    
    @Override
    public int compare(RcItemWrapper o1, RcItemWrapper o2) {
        
        float scoreToUse1;
        float scoreToUse2;
        
        if( srcTyp.getIsAll() )
        {
            scoreToUse1 = o1.getAverageScore(null);
            scoreToUse2 = o2.getAverageScore(null);
        }

        else if( srcTyp.getIsOthers() )
        {
            scoreToUse1 = o1.getScoreAvgNoCandidate();
            scoreToUse2 = o2.getScoreAvgNoCandidate();
        }

        else
        {
            scoreToUse1 = o1.getScoreCandidate( candRcRaterId );
            scoreToUse2 = o2.getScoreCandidate( candRcRaterId );
        }
        
        
        if( highTop )   
            return ((Float)scoreToUse2).compareTo( scoreToUse1 );
        else
            return ((Float)scoreToUse1).compareTo( scoreToUse2 );
    }
    
}
