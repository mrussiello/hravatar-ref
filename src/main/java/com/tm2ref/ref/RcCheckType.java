package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcCheckType
{
    PREHIRE(0,"Pre-Hire", "rcct.prehire" ),
    EMPLOYEE_FBK(1,"Employee Feedback", "rcct.employeefbk" );  // Archived 30 days after expiration, cancelled, started, scored, or completed

    private final int rcCheckTypeId;

    private final String name;
    private String key;


    private RcCheckType( int s , String n, String k )
    {
        this.rcCheckTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsPrehire()
    {
        return equals( PREHIRE );
    }
    
    public boolean getIsEmployeeFeedback()
    {
        return equals( EMPLOYEE_FBK );
    }
    


    public static RcCheckType getValue( int id )
    {
        RcCheckType[] vals = RcCheckType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCheckTypeId() == id )
                return vals[i];
        }

        return PREHIRE;
    }


    public int getRcCheckTypeId()
    {
        return rcCheckTypeId;
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
