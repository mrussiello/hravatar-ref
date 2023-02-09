package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcCompetencySubType
{
    NONE(0,"None", "rcompst.none" ),
    PRE(1,"Pre", "rcompst.pre" ),
    POST(2,"Post", "rcompst.post" ),
    DEFAULT(4,"Default", "rcompst.default" );  

    private final int rcCompetencySubTypeId;

    private final String name;
    private String key;


    private RcCompetencySubType( int s , String n, String k )
    {
        this.rcCompetencySubTypeId = s;
        this.name = n;
        this.key = k;
    }
    

    public static RcCompetencySubType getValue( int id )
    {
        RcCompetencySubType[] vals = RcCompetencySubType.values();
        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCompetencySubTypeId() == id )
                return vals[i];
        }
        return NONE;
    }


    public int getRcCompetencySubTypeId()
    {
        return rcCompetencySubTypeId;
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
