/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.ref.RcCompetencyWrapper;
import com.tm2ref.ref.RcItemWrapper;
import com.tm2ref.ref.RcRatingScaleType;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class RcHistogram implements Serializable
{
    int rcCompetencyId;
    int rcItemId;
    String[] otherNames;
    RcRatingScaleType rcRatingScaleType = null;
        
    List<RcHistogramRow> rowList;

    public RcHistogram( String[] otherNames, RcRatingScaleType rcRatingScaleType)
    {
        this.otherNames = otherNames;
        this.rcRatingScaleType = rcRatingScaleType;
        if( this.rcRatingScaleType==null )
            this.rcRatingScaleType=RcRatingScaleType.DEFAULT;
    }
    
    public boolean getHasData()
    {
        return rowList!=null && !rowList.isEmpty();
    }
    
    public void init( RcCheck rc, RcCompetencyWrapper rcw, RcItemWrapper rciw, int scoreDigits)
    {
        // Can only have one or the other. 
        if( rcw!=null && rciw!=null )
            rciw=null;
        
        if( rc==null || (rciw==null && rcw==null) ) // || !rciw.getHasRatingInfoToShow() || rciw.getAverageScore(null)<=0 )
            return;
        
        if( rcw!=null )
        {
            if( !rcw.getHasRatingInfoToShow() || rcw.getAverageScore(null)<=0 )
                return;
        }
        else
        {
            if( !rciw.getHasRatingInfoToShow() || rciw.getAverageScore(null)<=0 )
                return;
        }
        
        // RcRatingScaleType ratingScale = rc.getRcScript().getRcRatingScaleType();
        
        RcRater rater;
        rcCompetencyId = rcw==null ? 0 : rcw.getRcCompetencyId();
        rcItemId = rciw==null ? 0 : rciw.getRcItemId();
        boolean hasSelf = false;
        boolean hasOther = false;
        rowList=null;
              
        List<RcRating> rcrl;        
        if( rcw!=null )
        {
            RcRating rtg;
            for( RcRater rtr : rc.getRcRaterList() )
            {
                rcrl = new ArrayList<>();
                for( RcItemWrapper w : rcw.getRcItemWrapperList() )
                {
                    rtg = w.getRcRating( rtr.getRcRaterId() );
                    if( rtg==null || !rtg.getHasNumericScore() )
                            continue;
                    if( rtr.getUserId()==rc.getUserId() )
                        hasSelf = true;
                    else
                        hasOther=true;
                    rcrl.add(rtg);
                }
                if( !rcrl.isEmpty() )
                    addRatingsForRater( rtr, rcrl );
            }
        }
        
        else
        {
            for( RcRating rating : rciw.getRcRatingList() )
            {
                if( !rating.getHasNumericScore() )
                    continue;

                rater = rc.getRcRaterForRcRaterId( rating.getRcRaterId() );    

                if( rater.getUserId()==rc.getUserId() )
                    hasSelf = true;
                else
                    hasOther=true;

                rcrl = new ArrayList<>();
                rcrl.add(rating);
                addRatingsForRater( rater, rcrl );
            }            
        }
        
        // 0 = others, 1=self, 2=gap
        float[] averages = computeAverageValues(scoreDigits );        
        if( hasSelf && hasOther )
            addGap( averages[2] );
        
        if( rowList!=null )
        {
            int otherCount = 0;
            for( RcHistogramRow row : rowList)
            {
                if( row.getHistogramRoleTypeId()>=1 && row.getHistogramRoleTypeId()<=17 )
                    otherCount++;
            }
            if( otherCount>1 )
            {
                addAllOthers( averages[1] );
            }
            Collections.sort(rowList);
        }        
    }
    
    
    
    public float[] computeAverageValues(int scoreDigits)
    {
        if( rowList==null )
            rowList = new ArrayList<>();
        
        float countOther=0;
        float countSelf=0;
        float totalOther=0;
        float totalSelf=0;
        
        for( RcHistogramRow row : this.rowList )
        {
            // skip gap row
            if( row.getHistogramRoleTypeId()==40 )
                continue;
            // self
            else if( row.getHistogramRoleTypeId()==20 )
            {
                countSelf++;
                totalSelf += roundToDigits(row.getAvgScore(), scoreDigits);
            }
            // skip all others
            else if( row.getHistogramRoleTypeId()==19 )
            {}
            // others
            else if( row.getHistogramRoleTypeId()<=17 )
            {
                countOther++;
                totalOther += roundToDigits(row.getAvgScore(), scoreDigits);
            }
        }
        
        float[] out = new float[3];
        //if( countSelf<=0 )
        //    return out;
        if( countSelf>0 )
            out[0] = roundToDigits(totalSelf/countSelf, scoreDigits);
        if( countOther>0 )
            out[1] = roundToDigits(totalOther/countOther, scoreDigits);
        
        out[2] = out[0] - out[1];
        return out;
    }
    
    private float roundToDigits( float v, int scoreDigits)
    {        
        return Math.round(v*((float)Math.pow(10, scoreDigits)))/ ((float)Math.pow(10, scoreDigits));
    }
        
    
    public int getHistogramRoleTypeId( RcRater rater )
    {
        if( rater==null )
            return 40;
        if( rater.getIsCandidateOrEmployee() )
            return 20;
                
        switch( rater.getRcRaterRoleTypeId() )
        {
            case 0:
            case 30:
                return 15;
            case 31:
                return 16;
            case 32:
                return 17;
            case 20:
                return 10;
            case 15:
                return 5;
            default:
                return 1;
        }
    }

    private synchronized void addAllOthers( float avg )
    {
        RcHistogramRow row = getRcHistogramRow( 19 );
        if( row==null )
        {
            row = new RcHistogramRow( otherNames, rcRatingScaleType );
            row.setHistogramRoleTypeId(19);
            rowList.add( row );
        }
        row.setAvgScore(avg);
        row.setCount(1);        
    }
    

    
    private synchronized void addGap( float gap )
    {
        RcHistogramRow row = getRcHistogramRow( 40 );
        if( row==null )
        {
            row = new RcHistogramRow( otherNames, rcRatingScaleType );
            row.setHistogramRoleTypeId(40);
            rowList.add( row );
        }
        row.setAvgScore( gap );
        row.setCount(1);        
        row.setNegativeVal( gap<0 );
    }
    
    
    private synchronized void addRatingsForRater( RcRater rater, List<RcRating> ratingList )
    {
        if( rowList==null )
            rowList = new ArrayList<>();
        
        float score = getAvgRatingScore(ratingList);
                
        //if( !rating.getHasNumericScore() )
        //    return;
        
        int histogramRoleTypeId = getHistogramRoleTypeId( rater );
        
        RcHistogramRow row = getRcHistogramRow( histogramRoleTypeId );
        if( row==null )
        {
            row = new RcHistogramRow( otherNames, rcRatingScaleType );
            row.setHistogramRoleTypeId(histogramRoleTypeId);
            rowList.add( row );
        }
        row.addRating( score );        
    }
    
    public float getAvgRatingScore( List<RcRating> ratingList)
    {
        float ct = 0;
        float t = 0;
        for( RcRating r : ratingList )
        {
            if( !r.getHasNumericScore() )
                continue;
            ct++;
            t += r.getFinalScore();
        }
        return ct<=0 ? 0 : t/ct;
    }
    
    public RcHistogramRow getRcHistogramRow( int histogramRoleTypeId )
    {
        if( rowList==null )
            return null;
        
        for( RcHistogramRow row : rowList )
        {
            if( row.getHistogramRoleTypeId()==histogramRoleTypeId )
                return row;
        }
        return null;
    }
    
    
    public List<RcHistogramRow> getRowList() {
        return rowList;
    }

    public void setRowList(List<RcHistogramRow> rowList) {
        this.rowList = rowList;
    }
    
    
    
    
    
}
