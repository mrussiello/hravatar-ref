package com.tm2ref.user;


public enum OrgStatusType
{
    ACTIVE(0,"ostp.Active"),  // THIS MEANS
    PENDING_ACTIVATION(10,"ostp.PendingActivation"),
    SILENT(90,"ostp.Silent"),
    DISABLED(100,"ostp.Disabled"),
    CLOSED(110,"ostp.Closed");

    private int orgStatusTypeId;

    private String key;

    private OrgStatusType( int typeId , String key )
    {
        this.orgStatusTypeId = typeId;

        this.key = key;
    }

    public boolean getIsActive()
    {
        return equals( ACTIVE );
    }
    
    public boolean getIsDisabledOrClosed()
    {
        return equals( DISABLED ) || equals( CLOSED );
    }

    public boolean getDenyTesting()
    {
        return equals( PENDING_ACTIVATION ) || getIsDisabledOrClosed();
    }

    public int getOrgStatusTypeId()
    {
        return orgStatusTypeId;
    }

    public static OrgStatusType getValue( int id )
    {
        OrgStatusType[] vals = OrgStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getOrgStatusTypeId() == id )
                return vals[i];
        }

        return ACTIVE;
    }

    public String getKey()
    {
        return key;
    }
}
