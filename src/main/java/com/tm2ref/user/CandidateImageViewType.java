package com.tm2ref.user;



public enum CandidateImageViewType
{
    ALL(0,"Show to all","civt.all" ),                
    BASIC_AND_ABOVE(10,"Basic Users and Above Only","civt.basic" ),                
    ADMINS(20,"Admins Only","civt.admin" ),                
    NONE(30,"None","civt.none" );      


    private final int candidateImageViewTypeId;

    private final String name;
    private final String key;
    


    private CandidateImageViewType( int s , String n, String k )
    {
        this.candidateImageViewTypeId = s;
        this.name = n;
        this.key=k;
    }
    
    public static CandidateImageViewType getValue( int id )
    {
        CandidateImageViewType[] vals = CandidateImageViewType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCandidateImageViewTypeId() == id )
                return vals[i];
        }

        return ALL;
    }

    public boolean getShowPhotos() 
    {
        return !equals(NONE);
    }
    
    public boolean getIsNoAccess()
    {
        return equals(NONE);
    }

    
    public int getCandidateImageViewTypeId()
    {
        return candidateImageViewTypeId;
    }

    public String getName()
    {
        return name;
    }
    

}
