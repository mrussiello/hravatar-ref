package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcItemCommentsRequiredType
{
    NO_COMMENTS(0,"No comments", "rcicrt.nocomments" ),
    OPTIONAL(1,"Optional", "rcicrt.optional" ),
    REQUIRED(2,"Required", "rcicrt.required" );  
    
    private final int rcItemCommentsRequiredTypeId;

    private final String name;
    private String key;


    private RcItemCommentsRequiredType( int s , String n, String k )
    {
        this.rcItemCommentsRequiredTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getIsOptional()
    {
        return equals(OPTIONAL);
    }
    public boolean getIsRequired()
    {
        return equals(REQUIRED);
    }
        
            

    public static RcItemCommentsRequiredType getValue( int id )
    {
        RcItemCommentsRequiredType[] vals = RcItemCommentsRequiredType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcItemCommentsRequiredTypeId() == id )
                return vals[i];
        }

        return NO_COMMENTS;
    }

    
    
    public int getRcItemCommentsRequiredTypeId()
    {
        return rcItemCommentsRequiredTypeId;
    }

    public String getName()
    {
        return name;
    }

}
