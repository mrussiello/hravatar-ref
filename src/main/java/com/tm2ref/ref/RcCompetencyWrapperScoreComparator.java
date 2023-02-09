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
public class RcCompetencyWrapperScoreComparator implements Comparator<RcCompetencyWrapper> {

    boolean highTop = false;
    RcTopBottomSrcType srcTyp;
    long candRcRaterId;
    
    public RcCompetencyWrapperScoreComparator( boolean highTop, RcTopBottomSrcType srcTyp, long candRcRaterId )
    {
        if( srcTyp==null )
            srcTyp = RcTopBottomSrcType.OTHERS;
        
        this.highTop=highTop;
        this.srcTyp=srcTyp;
        this.candRcRaterId=candRcRaterId;
        
    }
    
    @Override
    public int compare(RcCompetencyWrapper o1, RcCompetencyWrapper o2) {
        
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
            scoreToUse1 = o1.getAvgScoreCandidate( candRcRaterId );
            scoreToUse2 = o2.getAvgScoreCandidate( candRcRaterId );
        }
        
        
        if( highTop )   
            return ((Float)scoreToUse2).compareTo( scoreToUse1 );
        else
            return ((Float)scoreToUse1).compareTo( scoreToUse2 );
    }
    
}
