package com.tm2ref.ref;



/**
 * 
 * @author miker_000
 */
public enum RcCandidatePhotoCaptureType
{
    NONE(0,"None", "rcpct.none"),
    OPTIONAL(1,"Optional Photo", "rcpct.optional"),
    OPTIONAL_ID(2,"Optional Photo and ID", "rcpct.optional.id"),
    REQUIRED(11,"Required Photo", "rcpct.required"),
    REQUIRED_ID(12,"Required Photo and ID", "rcpct.required.id");

    private final int rcCandidatePhotoCaptureTypeId;

    private final String name;
    private String key;


    private RcCandidatePhotoCaptureType( int s , String n, String k )
    {
        this.rcCandidatePhotoCaptureTypeId = s;

        this.name = n;
        this.key = k;
    }
            
    public boolean getRequiresAnyPhotoCapture()
    {
        return rcCandidatePhotoCaptureTypeId>0;
    }

    public boolean getRequiresAnyIdCapture()
    {
        return equals(OPTIONAL_ID) || equals(REQUIRED_ID);
    }
    
    public boolean getIsRequired()
    {
        return equals(REQUIRED) || equals(REQUIRED_ID);        
    }
    
    public boolean getIsOptional()
    {
        return equals(OPTIONAL) || equals(OPTIONAL_ID); 
    }
    
    public static RcCandidatePhotoCaptureType getValue( int id )
    {
        RcCandidatePhotoCaptureType[] vals = RcCandidatePhotoCaptureType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCandidatePhotoCaptureTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getRcCandidatePhotoCaptureTypeId()
    {
        return rcCandidatePhotoCaptureTypeId;
    }

    public String getName()
    {
        return name;
    }

    
}
