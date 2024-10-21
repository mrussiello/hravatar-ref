package com.tm2ref.api;



public enum ApiType
{
    DEFAULT( 0, "Default - HR Avatar" ),
    ICIMS( 1, "ICIMS" ),
    ADP_WFN( 2, "ADP WorkforceNow" ),
    GREENHOUSE( 3, "GreenHouse" ),
    JAZZHR( 4, "JazzHR" ),
    WORKABLE( 5, "Workable" ),
    TEAMTAILOR( 6, "TeamTailor" );

    private final int apiTypeId;

    private String key;

    private ApiType( int p , String key )
    {
        this.apiTypeId = p;

        this.key = key;
    }



    public boolean requiresReportNotification()
    {
        //if( equals( ICIMS ) )
        //    return true;
        
        return false;
    }
    
    public int getApiTypeId()
    {
        return this.apiTypeId;
    }


    public String getName()
    {
        return key;
    }




    public static ApiType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static ApiType getValue( int id )
    {
        ApiType[] vals = ApiType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getApiTypeId() == id )
                return vals[i];
        }

        return null;
    }

}
