package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcTopBottomSrcType
{
    OTHERS(0,"Others Ratings Only", "rctbst.others" ),
    SELF(1,"Self Ratings Only", "rctbst.self" ),  // has comments and ratings. This is the standard question type.
    ALL(10,"All Ratings", "rctbst.all" );  
    
    private final int rcTopBottomSrcTypeId;

    private final String name;
    private String key;


    private RcTopBottomSrcType( int s , String n, String k )
    {
        this.rcTopBottomSrcTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsOthers()
    {
        return equals(OTHERS);
    }
    public boolean getIsSelf()
    {
        return equals(SELF);
    }
    public boolean getIsAll()
    {
        return equals(ALL);
    }
    

    public static RcTopBottomSrcType getValue( int id )
    {
        RcTopBottomSrcType[] vals = RcTopBottomSrcType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcTopBottomSrcTypeId() == id )
                return vals[i];
        }

        return OTHERS;
    }

    
    
    public int getRcTopBottomSrcTypeId()
    {
        return rcTopBottomSrcTypeId;
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
