package com.tm2ref.file;

public enum UploadedUserFileStatusType
{
    AVAILABLE(0,"Available"),
    PSEUDONYMIZED(10,"Pseudonymized");


    private final int uploadedUserFileStatusTypeId;

    private String name;


    private UploadedUserFileStatusType( int p , String key )
    {
        this.uploadedUserFileStatusTypeId = p;

        this.name = key;
    }

    
    public boolean getAvailable()
    {
        return equals( AVAILABLE );
    }
    
    public boolean getPseudo()
    {
        return equals( PSEUDONYMIZED );
    }
    


    public String getName()
    {
        return name;
    }



    public static UploadedUserFileStatusType getValue( int id )
    {
        UploadedUserFileStatusType[] vals = UploadedUserFileStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUploadedUserFileStatusTypeId() == id )
                return vals[i];
        }

        return AVAILABLE;
    }

    public int getUploadedUserFileStatusTypeId() {
        return uploadedUserFileStatusTypeId;
    }
    

}
