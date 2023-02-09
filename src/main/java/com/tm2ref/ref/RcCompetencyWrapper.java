/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCompetency;
import com.tm2ref.entity.ref.RcRating;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class RcCompetencyWrapper implements Comparable<RcCompetencyWrapper>, Serializable {
    
    String onetElementId;
    int rcCompetencyId;
    int displayOrder; 
    float onetImportance;
    int userImportanceTypeId;
    float idealScore;
    
    List<RcItemWrapper> rcItemWrapperList;
    
    RcCompetency rcCompetency;
    Locale locale;
    
    public RcCompetencyWrapper()
    {
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.rcCompetencyId;
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
        final RcCompetencyWrapper other = (RcCompetencyWrapper) obj;
        if (this.rcCompetencyId != other.rcCompetencyId) {
            return false;
        }
        return true;
    }
    
    
    public RcImportanceType getUserRcImportanceType()
    {
        return RcImportanceType.getValue( userImportanceTypeId );
    }
        
    @Override
    public String toString() {
        return "RcCompetencyWrapper{" + "elementId=" + onetElementId + '}';
    }

    @Override
    public int compareTo(RcCompetencyWrapper o) 
    {        
        return ((Integer)displayOrder).compareTo( o.getDisplayOrder() );           
    }
        
    
    public RcItemWrapper getRcItemWrapper( int rcItemId )
    {
        if( rcItemWrapperList==null )
            return null;
        
        for( RcItemWrapper w : rcItemWrapperList )
        {
            if( w.getRcItemId()==rcItemId )
                return w;
        }
        return null;
    }
    
    
    public synchronized void addItemWrapper( RcItemWrapper iw )
    {
        if( rcItemWrapperList==null )
            rcItemWrapperList=new ArrayList<>();
        
        if( rcItemWrapperList.contains(iw) )
            return;
        
        rcItemWrapperList.add(iw);
    }
    
    public boolean getHasRatingInfoToShow()
    {
        if( rcItemWrapperList==null )
            return false;
        for( RcItemWrapper rciw : rcItemWrapperList )
        {
            if( rciw.getHasRatingInfoToShow() )
                return true;
        }
        return false;
    }
    
    public float getAverageScore(List<Long> rcRaterIdsToSkip)
    {
        if( rcItemWrapperList==null )
            return 0;
        float ct = 0;
        float t = 0;
        float s;
        for( RcItemWrapper rciw : rcItemWrapperList )
        {
            s = rciw.getAverageScore(rcRaterIdsToSkip);
            if( s<=0 )
                continue;
            ct++;
            t+=s;
        }
        return ct<=0 ? 0 : t/ct;
    }
    
    
    

    public RcCompetency getRcCompetency() {
        return rcCompetency;
    }

    public void setRcCompetency(RcCompetency competency) {
        this.rcCompetency = competency;
    }

    public String getOnetElementId() {
        return onetElementId;
    }

    public void setOnetElementId(String onetElementId) {
        this.onetElementId = onetElementId;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public int getRcCompetencyId() {
        return rcCompetencyId;
    }

    public void setRcCompetencyId(int rcCompetencyId) {
        this.rcCompetencyId = rcCompetencyId;
    }

    public List<RcItemWrapper> getRcItemWrapperList() {        
        if( rcItemWrapperList==null )
            rcItemWrapperList=new ArrayList<>();
        
        return rcItemWrapperList;
    }

    public void setRcItemWrapperList(List<RcItemWrapper> rcItemWrapperList) {
        this.rcItemWrapperList = rcItemWrapperList;
    }

    public float getOnetImportance() {
        return onetImportance;
    }

    public void setOnetImportance(float onetImportance) {
        this.onetImportance = onetImportance;
    }

    public int getUserImportanceTypeId() {
        return userImportanceTypeId;
    }

    public void setUserImportanceTypeId(int userImportanceTypeId) {
        this.userImportanceTypeId = userImportanceTypeId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean getHasAnyScoredItems() 
    {
        if( rcItemWrapperList==null )
            return false;
        for( RcItemWrapper rciw : rcItemWrapperList)
        {
            if( rciw.getRcItem().getIsItemScored() )
                return true;
        }
        return false;
    }
    
    
    public boolean getHasNumericScore() 
    {
        if( rcItemWrapperList==null )
            return false;
        for( RcItemWrapper rciw : rcItemWrapperList)
        {
            if( rciw.getRcRatingList()==null )
                continue;
            
            for( RcRating rating : rciw.getRcRatingList() )
            {
                if( rating.getHasNumericScore() )
                    return true;
            }
        }
        return false;
    }
    
    
    public float getAvgScoreCandidate( long candidateRaterId ) 
    {
        if( rcItemWrapperList==null || candidateRaterId<=0 )
            return 0;

        float ct = 0;
        float tot = 0;
        
        for( RcItemWrapper rciw : rcItemWrapperList)
        {
            if( rciw.getRcRatingList()==null )
                continue;
            
            for( RcRating rating : rciw.getRcRatingList() )
            {
                if( rating.getRcRaterId()==candidateRaterId )
                {
                    if( rating.getFinalScore()<=0 )
                        continue;
                    
                    ct++;
                    tot += rating.getFinalScore();
                    // return rating.getFinalScore();
                }
            }
        }
        return ct>0 ? tot/ct : 0;
        // return 0;
    }
    
    
    public float getScoreAvgNoCandidate() {
        if( rcItemWrapperList==null )
            return 0;
        float ct = 0;
        float tot = 0;
        for( RcItemWrapper rciw : rcItemWrapperList)
        {
            if( rciw.getScoreAvgNoCandidate()<=0 )
                continue;
            ct++;
            tot += rciw.getScoreAvgNoCandidate();
        }
        return ct>0 ? tot/ct : 0;
    }

    public float getIdealScore() {
        return idealScore;
    }

    public void setIdealScore(float idealScore) {
        this.idealScore = idealScore;
    }

    
    
}
