package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.Locale;



/**
 * @author miker_000
 */
public enum RcCompetencyStatusType
{
    ACTIVE(0,"Active", "rcpst.active" ),
    ARCHIVED(10,"Archived", "rcpst.archived" );  // Archived 30 days after expiration, cancelled, started, scored, or completed

    private final int rcCompetencyStatusTypeId;

    private final String name;
    private String key;


    private RcCompetencyStatusType( int s , String n, String k )
    {
        this.rcCompetencyStatusTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }
    
    public static RcCompetencyStatusType getValue( int id )
    {
        RcCompetencyStatusType[] vals = RcCompetencyStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCompetencyStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

    public int getRcCompetencyStatusTypeId()
    {
        return rcCompetencyStatusTypeId;
    }

    public String getName()
    {
        return name;
    }
    

}
