package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.io.Serializable;
import java.util.Locale;



/**
 * 
 * @author miker_000
 */
public enum RcRatingScaleType implements Serializable
{
    DEFAULT(0,"1-10 (default)", "rcrstp.1To10",4.825f,6.175f, 1.1f, 9.9f, 4.5f, 7.5f, 10f,1f),
    ONE_FIVE(10,"1 - 5", "rcrstp.1To5", 2.7f, 3.3f, 1.1f, 4.9f, 2.75f, 4.255f,5f,1f);

    private final int rcRatingScaleTypeId;

    private final String name;
    private String key;
    private final float denyMiddleLow;
    private final float denyMiddleHigh;
    private final float commentThresholdLow;
    private final float commentThresholdHigh;
    private final float maxLowRatedCompScore;
    private final float minHighRatedCompScore;
    private final float maxLowRatedCompScore360;
    private final float minHighRatedCompScore360;
    


    private RcRatingScaleType( int s , String n, String k, float denyMiddleLow, float denyMiddleHigh, float commentThresholdLow, float commentThresholdHigh, float maxLowRatedCompScore, float minHighRatedCompScore, float maxLowRatedCompScore360, float minHighRatedCompScore360 )
    {
        this.rcRatingScaleTypeId = s;

        this.name = n;
        this.key = k;
        this.denyMiddleLow=denyMiddleLow;
        this.denyMiddleHigh=denyMiddleHigh;
        this.commentThresholdLow=commentThresholdLow;
        this.commentThresholdHigh=commentThresholdHigh;
        this.maxLowRatedCompScore=maxLowRatedCompScore;
        this.minHighRatedCompScore=minHighRatedCompScore;
        this.maxLowRatedCompScore360=maxLowRatedCompScore360;
        this.minHighRatedCompScore360=minHighRatedCompScore360;
    }

    public float getMaxLowRatedItemScore()
    {
        if( equals(DEFAULT) )
            return 5.4999f;
        return 2.9999f;
    }

    public float getMinHighRatedItemScore()
    {
        if( equals(DEFAULT) )
            return 5.5f;
        return 3.0f;
    }

    public float getPercentOfRange( float score )
    {
        return 100f*(score-getMinScore())/(getMaxScore()-getMinScore());
    }
    
    
    public float getMaxLowRatedCompScore() {
        return maxLowRatedCompScore;
    }

    public float getMinHighRatedCompScore() {
        return minHighRatedCompScore;
    }

    public float getMaxLowRatedCompScore360() {
        return maxLowRatedCompScore360;
    }

    public float getMinHighRatedCompScore360() {
        return minHighRatedCompScore360;
    }

    
    
    public float getCommentThresholdLow() {
        return commentThresholdLow;
    }

    public float getCommentThresholdHigh() {
        return commentThresholdHigh;
    }
    
    
    
    public float getDenyMiddleLow()
    {
        return denyMiddleLow;
    }

    public float getDenyMiddleHigh()
    {
        return denyMiddleHigh;
    }
    
    public float getMinScore()
    {
        return 1f;
    }

    public float getMaxScore()
    {
        if( getIsOneToFive() )
            return 5f;
        
        return 10f;
    }
    
    public float[] getDiscreteValuesMsie()
    {
        if( getIsOneToFive() )
            return new float[] {1f,2f,3f,4f,5f}; // {1f,1.5f,2f,2.5f,3f,3.5f,4f,4.5f,5f};

        return new float[] {1f,2f,3f,4f,5f,6f,7f,8f,9f,10f};
    }
    
    
    public float[] getDiscreteValues()
    {
        if( getIsOneToFive() )
            return new float[] {1f,2.125f,3.25f,4.375f,5f};

        return new float[] {1f,3.25f,5.5f,7.75f,10f};
    }

    
    public boolean getIsDefault()
    {
        return equals( DEFAULT );
    }
    
    public boolean getIsOneToFive()
    {
        return equals( ONE_FIVE );
    }

    public static RcRatingScaleType getValue( int id )
    {
        RcRatingScaleType[] vals = RcRatingScaleType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRatingScaleTypeId() == id )
                return vals[i];
        }

        return DEFAULT;
    }


    public int getRcRatingScaleTypeId()
    {
        return rcRatingScaleTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName( Locale l )
    {
        if( l==null )
            l = Locale.US;
        
        return MessageFactory.getStringMessage(l, key );
    }
    
}
