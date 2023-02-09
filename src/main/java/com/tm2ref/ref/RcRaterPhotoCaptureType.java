package com.tm2ref.ref;


/**
 * 
 * @author miker_000
 */
public enum RcRaterPhotoCaptureType
{
    NONE(0,"None", "rcprt.none"),
    OPTIONAL(1,"Optional Photo", "rcprt.optional"),
    OPTIONAL_SAMEIP(2,"Optional Photo Same IP Only", "rcprt.optional.sameip"),
    OPTIONAL_ID(3,"Optional Photo and ID", "rcprt.optional.id"),
    OPTIONAL_ID_SAMEIP(4,"Optional Photo and ID Same IP Only", "rcprt.optional.id.sameip"),
    REQUIRED(11,"Required Photo", "rcprt.required"),
    REQUIRED_SAMEIP(12,"Required Photo Same IP Only", "rcprt.required.sameip"),
    REQUIRED_ID(13,"Required Photo and ID", "rcprt.required.id"),
    REQUIRED_ID_SAMEIP(14,"Required Photo Same IP Only", "rcprt.required.id.sameip");

    private final int rcRaterPhotoCaptureTypeId;

    private final String name;
    private String key;


    private RcRaterPhotoCaptureType( int s , String n, String k )
    {
        this.rcRaterPhotoCaptureTypeId = s;

        this.name = n;
        this.key = k;
    }

    public boolean getIsRequired()
    {
        return equals(REQUIRED) || equals(REQUIRED_SAMEIP) || equals(REQUIRED_ID) || equals(REQUIRED_ID_SAMEIP);
    }

    public boolean getIsOptional()
    {
        return equals(OPTIONAL) || equals(OPTIONAL_SAMEIP) || equals(OPTIONAL_ID) || equals(OPTIONAL_ID_SAMEIP); 
    }
    
    public boolean getSameIpOnly()
    {
        return equals(OPTIONAL_SAMEIP) || equals(REQUIRED_SAMEIP) || equals(OPTIONAL_ID_SAMEIP) || equals(REQUIRED_ID_SAMEIP);
    }
    
    public boolean getCapturesAnyId()
    {
        return equals(OPTIONAL_ID_SAMEIP) || equals(REQUIRED_ID_SAMEIP) || equals(REQUIRED_ID) || equals(OPTIONAL_ID) ;
    }
    
    
    public boolean getRequiresAnyPhotoCapture()
    {
        return rcRaterPhotoCaptureTypeId>0;
    }
    
    public static RcRaterPhotoCaptureType getValue( int id )
    {
        RcRaterPhotoCaptureType[] vals = RcRaterPhotoCaptureType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterPhotoCaptureTypeId() == id )
                return vals[i];
        }

        return NONE;
    }

    

    public int getRcRaterPhotoCaptureTypeId()
    {
        return rcRaterPhotoCaptureTypeId;
    }

    public String getName()
    {
        return name;
    }
    
}
