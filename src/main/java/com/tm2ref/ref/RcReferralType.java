package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcReferralType
{
    SELF(0,"Rater", "rcrft.raterself" ),
    EXTRA(1,"Extra", "rcrft.raterextra" ); 

    private final int rcReferralTypeId;

    private final String name;
    private String key;


    private RcReferralType( int s , String n, String k )
    {
        this.rcReferralTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsSelf()
    {
        return equals( SELF );
    }
    
    public boolean getIsExtra()
    {
        return equals( EXTRA );
    }
    


    public static RcReferralType getValue( int id )
    {
        RcReferralType[] vals = RcReferralType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcReferralTypeId() == id )
                return vals[i];
        }

        return SELF;
    }


    public int getRcReferralTypeId()
    {
        return rcReferralTypeId;
    }

    public String getName()
    {
        return name;
    }
    public String getName( Locale l )
    {
        if( l==null )
            l=Locale.US;        
        return MessageFactory.getStringMessage(l, this.key );
    }

}
