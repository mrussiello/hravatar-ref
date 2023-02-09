package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RefUserType
{
    CANDIDATE(0,"Candidate", "rcut.candidate" ),
    RATER(1,"Rater", "rcut.rater" );  

    private final int refUserTypeId;

    private final String name;
    private String key;


    private RefUserType( int s , String n, String k )
    {
        this.refUserTypeId = s;

        this.name = n;
        this.key = k;
    }
        

    
    public static RefUserType getValue( int id )
    {
        RefUserType[] vals = RefUserType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRefUserTypeId() == id )
                return vals[i];
        }

        return null;
    }

    public boolean getIsCandidate()
    {
        return equals(CANDIDATE);
    }
    
    public boolean getIsRater()
    {
        return equals(RATER);
    }

    public int getRefUserTypeId()
    {
        return refUserTypeId;
    }

    public String getName()
    {
        return name;
    }

}
