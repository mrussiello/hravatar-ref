/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.report.RcHistogram;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class RcItemWrapper implements Comparable<RcItemWrapper>, Serializable {
    
    int rcItemId;
    float weight;
    int raterDisplayOrder;
    int candidateDisplayOrder;
    float scoreAvgNoCandidate;
    String questionWithSubs;
    RcHistogram histogram;
        
    //RcCompetencyWrapper tempRcCompetencyWrapper;    
    
    RcItem rcItem;
    
    List<RcRating> rcRatingList;
    RcRating rcRating;
    
    
    public RcItemWrapper()
    {}
    
    //public RcItemWrapper( int displayOrder, int candidateDisplayOrder, int rcItemId, float weight)
    //{
    //    this.displayOrder = displayOrder;
    //    this.candidateDisplayOrder = candidateDisplayOrder;
    ////    this.rcItemId = rcItemId;
    //    this.weight = weight;
    //}

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.rcItemId;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RcItemWrapper other = (RcItemWrapper) obj;
        if (this.rcItemId != other.rcItemId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "RcItemWrapper{" + "rcItemId=" + rcItemId + ", weight=" + weight + ", displayOrder=" + getDisplayOrder() + '}';
    }

    public boolean getCommentsRequiredB()
    {
        if( this.rcItem==null )
            return false;
        return rcItem.getIncludeComments()==RcItemCommentsRequiredType.REQUIRED.getRcItemCommentsRequiredTypeId();
    }
    
    public boolean getHasCommentsToShow()
    {
        if( rcRatingList==null || rcRatingList.isEmpty() )
            return false;
        for( RcRating r : rcRatingList )
        {
            if( r.getText()!=null && !r.getText().isBlank())
                return true;
        }
        return false;
    }
    
    
    public boolean getHasRatingInfoToShow()
    {
        if( rcRatingList==null || rcRatingList.isEmpty() )
            return false;
        for( RcRating r : rcRatingList )
        {
            if( r.getIsComplete() || r.getIsSkipped() )
                return true;
        }
        return false;
    }
    
    public int getDisplayOrder( boolean candidate )
    {
        return candidate ? candidateDisplayOrder : getRaterDisplayOrder();
    }
    
    
    public float getScoreCandidate( long candidateRcRaterId )
    {
        if( rcRatingList==null)
            return 0;
        
        if( rcItem==null || !rcItem.getIncludeNumRatingB() )
            return 0;
        
        for( RcRating r : rcRatingList )
        {
            if( !r.getHasNumericScore() )
                continue;
            
            if( candidateRcRaterId == r.getRcRaterId() )
                return r.getFinalScore();
        }
        return 0;
    }

    public float getAverageScore(List<Long> rcRaterIdsToSkip)
    {
        if( rcRatingList==null)
            return 0;
        
        if( rcItem==null || !rcItem.getIncludeNumRatingB() )
            return 0;
        
        float t = 0;
        float c = 0;
        for( RcRating r : rcRatingList )
        {
            if( !r.getHasNumericScore() )
                continue;
            
            if( rcRaterIdsToSkip!=null && rcRaterIdsToSkip.contains( r.getRcRaterId() ) )
                continue;
            
            t+=r.getFinalScore();
            c++;
        }
        if( c<=0 )
            return 0;
        return t/c;
    }
    
    public RcRating getRcRating( long rcRaterId )
    {
        if( rcRating!=null && rcRating.getRcRaterId()==rcRaterId )
            return rcRating;
        
        if( rcRatingList==null )
            return null;
        for( RcRating r : rcRatingList )
        {
            if( r.getRcRaterId()==rcRaterId )
                return r;
       }
       return null;        
    }
    
    public void clearRatings()
    {
        if( rcRatingList==null )
            return;
        rcRatingList.clear();
    }
    
    
    
    public void addRating( RcRating rtg )
    {
        if( rcRatingList==null )
            rcRatingList = new ArrayList<>();
        
        rcRatingList.add( rtg );
    }
    
    public boolean getIsCompleteOrHigher()
    {
        return rcRating!=null && rcRating.getIsCompleteOrHigher();
    }
    public boolean getIsComplete()
    {
        return rcRating!=null && rcRating.getIsComplete();
    }
    public boolean getIsSkipped()
    {
        return rcRating!=null && rcRating.getIsSkipped();
    }
    
    
    

    @Override
    public int compareTo(RcItemWrapper o) 
    {        
        return ((Integer)getRaterDisplayOrder()).compareTo( o.getRaterDisplayOrder() );           
    }

    public int getRcItemId() {
        return rcItemId;
    }

    public void setRcItemId(int rcItemId) {
        this.rcItemId = rcItemId;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public int getDisplayOrder() {
        return rcItem==null ? 0 : rcItem.getDisplayOrder();
    }

    //public void setDisplayOrder(int displayOrder) {
    //    this.displayOrder = displayOrder;
    //}

    public RcItem getRcItem() {
        return rcItem;
    }

    public void setRcItem(RcItem rcItem) {
        this.rcItem = rcItem;
    }

    public RcRating getRcRating() {
        return rcRating;
    }

    public void setRcRating(RcRating rcRating) {
        this.rcRating = rcRating;
    }

    public List<RcRating> getRcRatingList() {
        return rcRatingList;
    }

    public void setRcRatingList(List<RcRating> rcRatingList) {
        this.rcRatingList = rcRatingList;
    }

    public int getCandidateDisplayOrder() {
        return candidateDisplayOrder;
    }

    public void setCandidateDisplayOrder(int candidateDisplayOrder) {
        this.candidateDisplayOrder = candidateDisplayOrder;
    }

    public float getScoreAvgNoCandidate() {
        return scoreAvgNoCandidate;
    }

    public void setScoreAvgNoCandidate(float scoreAvgNoCandidate) {
        this.scoreAvgNoCandidate = scoreAvgNoCandidate;
    }

    public RcHistogram getHistogram() {
        return histogram;
    }

    public void setHistogram(RcHistogram histogram) {
        this.histogram = histogram;
    }

    public int getRaterDisplayOrder() {
        return raterDisplayOrder;
    }

    public void setRaterDisplayOrder(int raterDisplayOrder) {
        this.raterDisplayOrder = raterDisplayOrder;
    }

    public String getQuestionWithSubs() 
    {
        if( questionWithSubs==null || questionWithSubs.isBlank() && rcItem!=null )
            return rcItem.getQuestion();
        
        return questionWithSubs;
    }

    public void setQuestionWithSubs(String questionWithSubs) {
        this.questionWithSubs = questionWithSubs;
    }

    
    
    
}
