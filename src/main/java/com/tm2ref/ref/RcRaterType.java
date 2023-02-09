package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;




/**
 * @author miker_000
 */
public enum RcRaterType
{
    RATER(0,"Rater", "rcrt.rater" ),
    SELF(1,"Self", "rcrt.self" );  

    private final int rcRaterTypeId;

    private final String name;
    private String key;


    private RcRaterType( int s , String n, String k )
    {
        this.rcRaterTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    
    public boolean getIsCandidateOrEmployee()
    {
        return equals( SELF );
    }
    
    public boolean getIsRater()
    {
        return equals( RATER );
    }
    
           
    public static RcRaterType getValue( int id )
    {
        RcRaterType[] vals = RcRaterType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterTypeId() == id )
                return vals[i];
        }

        return RATER;
    }


    public int getRcRaterTypeId()
    {
        return rcRaterTypeId;
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
