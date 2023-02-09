/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.itextpdf.text.BaseColor;
import com.tm2ref.ref.RcRatingScaleType;
import java.io.Serializable;

/**
 *
 * @author miker_000
 */
public class RcHistogramRow implements Comparable<RcHistogramRow>, Serializable
{
    /*
     Orange #FFA500 	rgb(255,165,0)
     Green #32CD32 	rgb(50,205,50)
     Light Blue #87CEFA 	rgb(135,206,250)
     Blue  deepskyblue 	#00BFFF 	rgb(0,191,255)
     Purple #8A2BE2 	rgb(138,43,226)
     Dark Blue  #0000CD 	rgb(0,0,205)
     Gray #C0C0C0 	rgb(192,192,192)
    
    */
    public static String[] colors = new String[] { };
    /**
     * 1 = superior
     * 5 = peer
     * 10 = subordinates
     * 15 = Other or Unknown, or custom other 1
     * 16 = custom other 2
     * 17 = custom other 3
     * 
     * 19 = All Others
     * 20 = self
     * 40 = gap
     */
    int histogramRoleTypeId = 0;
    
    RcRatingScaleType rcRatingScaleType=null;
    
    int count = 0;
    float totalScore = 0;
    
    float avgScore = 0;

    boolean negativeVal = false;
        
    String[] otherNames;

    public RcHistogramRow( String[] otherNames, RcRatingScaleType rcRatingScaleType)
    {
        this.otherNames = otherNames;
        this.rcRatingScaleType = rcRatingScaleType;
        if( this.rcRatingScaleType==null )
            this.rcRatingScaleType = RcRatingScaleType.DEFAULT;
    }
    
    
    @Override
    public String toString() {
        return "RcHistogramRow{" + "histogramRoleTypeId=" + histogramRoleTypeId + ", count=" + count + ", avgScore=" + avgScore + '}';
    }
    

    @Override
    public int compareTo(RcHistogramRow o) {
        return ((Integer)histogramRoleTypeId).compareTo( o.getHistogramRoleTypeId() );
    }

    
    public void addRating( float score )
    {
        if( score<=0 )
            return;
        count++;
        totalScore += score;
        avgScore = totalScore/count;
    }

    public int getHistogramRoleTypeId() {
        return histogramRoleTypeId;
    }

    public void setHistogramRoleTypeId(int histogramRoleTypeId) {
        this.histogramRoleTypeId = histogramRoleTypeId;
    }
    
    

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public float getAvgScore() {
        return avgScore;
    }

    public void setAvgScore(float avgScore) {
        this.avgScore = avgScore;
    }

    public String[] getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String[] otherNames) {
        this.otherNames = otherNames;
    }

    
    public String getRgbColor() 
    {
        switch (histogramRoleTypeId) 
        {
            case 1:
                return "#FFA500";
            case 5:
                return "#32CD32";
            case 10:
                return "#87CEFA";
            case 15:
            case 16:
            case 17:
                return "#00BFFF";
            case 19:
                return "#8A2BE2";
            case 20:
                return "#0000CD";
            case 40:
                return negativeVal ? "#D0FFD0" : "#FFE6E6";
            default:
                return "#C0C0C0";
        }
    }
    
    public String getOtherNameIfPresent()
    {
        if( this.otherNames==null )
            return "";
        
        switch (histogramRoleTypeId) 
        {
            case 15:
                return otherNames[0]!=null ? otherNames[0] : "";
            case 16:
                return otherNames[1]!=null ? otherNames[1] : "";
            case 17:
                return otherNames[2]!=null ? otherNames[2] : "";
            default:
                return "";
        }        
    }
    

    public String getLangKey() 
    {
        switch (histogramRoleTypeId) 
        {
            case 1:
                return "g.RH.superior";
            case 5:
                return "g.RH.peer";
            case 10:
                return "g.RH.sub";
            case 15:
                if( otherNames!=null && otherNames[0]!=null )
                    return "g.RH.otherx";
                return "g.RH.other";
            case 16:
            case 17:
                return "g.RH.otherx";                
            case 19:
                return "g.RH.allothers";
            case 20:
                return "g.RH.self";
            default:
                return "g.RH.gap";
        }
    }

    public BaseColor getBaseColor() {
        switch (histogramRoleTypeId) 
        {
            case 1:
                return new BaseColor(255,165,0);
            case 5:
                return new BaseColor(50,205,50);
            case 10:
                return new BaseColor(135,206,250);
            case 15:
            case 16:
            case 17:
                return new BaseColor(0,191,255);
            case 19:
                return new BaseColor(138,43,226);
            case 20:
                return new BaseColor(0,0,205);
            case 40:
                return negativeVal ?  new BaseColor(208,255,208) : new BaseColor(255,230,230);
            default:
                return new BaseColor(192,192,192);
        }
    }

    public RcRatingScaleType getRcRatingScaleType() {
        return rcRatingScaleType;
    }

    public void setNegativeVal(boolean negativeVal) {
        this.negativeVal = negativeVal;
    }

    
    
    
    
    
}
