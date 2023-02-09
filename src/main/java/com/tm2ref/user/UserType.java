package com.tm2ref.user;



public enum UserType
{
    NAMED(0,"usertype.named"),  // THIS MEANS the user has provided name and email and should be included in reports since this is useful information.
    ANONYMOUS(1,"usertype.anonymous"),   // This means no name/email info provided. However, race and demographic and other info may have been provided.
    USERID(3,"usertype.userid"), // User is identified by a userid in email field only.
    USERNAME(4,"usertype.usernamepwd"),  // User is identified by a username in email field only. 
    PSEUDONYMIZED(10,"usertype.pseudonymized");

    private int userTypeId;

    private String key;

    private UserType( int typeId , String key )
    {
        this.userTypeId = typeId;

        this.key = key;
    }

    public boolean getNamed()
    {
        return equals( NAMED );
    }

    public boolean getPseudo()
    {
        return equals( PSEUDONYMIZED );
    }

    public boolean getAnonymous()
    {
        return equals( ANONYMOUS );
    }

    public boolean getNamedAnonPseudo()
    {
        return equals( NAMED ) || equals( ANONYMOUS ) || equals( PSEUDONYMIZED );
    }
    
    public boolean getNamedUserIdUsername()
    {
        return equals( NAMED ) || equals( USERID ) || equals( USERNAME );        
    }

    public boolean getUserId()
    {
        return equals( USERID );
    }

    public boolean getUsername()
    {
        return equals( USERNAME );
    }
    
    
    public int getUserTypeId()
    {
        return userTypeId;
    }
    
    public static UserType getValue( int id )
    {
        UserType[] vals = UserType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUserTypeId() == id )
                return vals[i];
        }

        return NAMED;
    }

    public String getKey()
    {
        return key;
    }
}
