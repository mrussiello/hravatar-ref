package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcItemCommentsRequiredType
{
    NO_COMMENTS(0,"No comments", "rcicrt.nocomments" ),
    OPTIONAL(1,"Optional", "rcicrt.optional" ),
    REQUIRED(2,"Required", "rcicrt.required" ),
    REQUIRED_CANDS(4,"Required for Candidates Only", "rcicrt.requiredcands" ),
    REQUIRED_RATERS(5,"Required for Raters Only", "rcicrt.requiredraters" );  
    
    private final int rcItemCommentsRequiredTypeId;

    private final String name;
    private String key;


    private RcItemCommentsRequiredType( int s , String n, String k )
    {
        this.rcItemCommentsRequiredTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    
    public boolean getAreCommentsRequired( RefUserType refUserType )
    {
        if( equals( NO_COMMENTS) || equals(OPTIONAL) )
            return false;
        
        if( equals( REQUIRED)  )
            return true;
        
        if( refUserType.getIsCandidate() && equals(REQUIRED_CANDS) )
            return true;

        if( refUserType.getIsRater()&& equals(REQUIRED_RATERS) )
            return true;
        
        return false;
    }
    
    public boolean getIsOptional()
    {
        return equals(OPTIONAL);
    }
    public boolean getIsRequired()
    {
        return equals(REQUIRED);
    }

    public boolean getIsRequiredCandidates()
    {
        return equals(REQUIRED_CANDS);
    }
    public boolean getIsRequiredRaters()
    {
        return equals(REQUIRED_RATERS);
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
