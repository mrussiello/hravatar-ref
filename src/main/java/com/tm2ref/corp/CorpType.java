package com.tm2ref.corp;



public enum CorpType
{
    PIN( 0, "Pin Test and Video Interview" ),
    LOGON( 1, "Logon Portal Test Only" ),
    REFERENCE_CHECK( 2, "Reference Checks Only" );

    private final int corpTypeId;

    private String key;

    private CorpType( int p , String key )
    {
        this.corpTypeId = p;

        this.key = key;
    }

    public boolean getIsReferenceCheck()
    {
        return equals( REFERENCE_CHECK );
    }
    


    public int getCorpTypeId()
    {
        return this.corpTypeId;
    }




    public String getName()
    {
        return key;
    }




    public static CorpType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static CorpType getValue( int id )
    {
        CorpType[] vals = CorpType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getCorpTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
