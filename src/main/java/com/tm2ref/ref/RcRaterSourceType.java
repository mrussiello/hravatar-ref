package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.util.MessageFactory;
import java.util.Locale;




/**
 * @author miker_000
 */
public enum RcRaterSourceType
{
    UNKNOWN(0,"Unknown", "rcrtst.unknown" ),
    CANDIDATE(1,"Candidate or Employee", "rcrtst.candidate" ),
    ACCT_USER(2,"Account User", "rcrtst.accountuser" );  

    private final int rcRaterSourceTypeId;

    private final String name;
    private String key;


    private RcRaterSourceType( int s , String n, String k )
    {
        this.rcRaterSourceTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public static RcRaterSourceType getForRcRater( RcCheck rc, RcRater rcRater )
    {
        if( rcRater.getSourceUserId()==rc.getUserId() )
            return CANDIDATE;
        return ACCT_USER;
    }
    
    public boolean getIsAccountUserOrUnknown()
    {
        return equals( ACCT_USER ) || equals( UNKNOWN );
    }
    
    public boolean getIsCandidateOrEmployee()
    {
        return equals( CANDIDATE );
    }
    
    public boolean getIsAccountUser()
    {
        return equals( ACCT_USER );
    }
    
           
    public static RcRaterSourceType getValue( int id )
    {
        RcRaterSourceType[] vals = RcRaterSourceType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterSourceTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getRcRaterSourceTypeId()
    {
        return rcRaterSourceTypeId;
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
