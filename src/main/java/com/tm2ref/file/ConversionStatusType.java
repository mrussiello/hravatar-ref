package com.tm2ref.file;

public enum ConversionStatusType
{
    NA(0,"conversionstatustype.na"),
    NOT_STARTED(1,"conversionstatustype.notstarted"),
    MARKED_FOR_CANCELLATION(9,"conversionstatustype.markedcanceled"),
    FAILED(10,"conversionstatustype.failed"),
    CANCELED(11,"conversionstatustype.canceled"),
    STARTED(100,"conversionstatustype.started"),
    SENT_TO_AMAZON(110,"conversionstatustype.senttoamazon"),
    RETURNED_FROM_AMAZON(120,"conversionstatustype.returnedfromamazon"),
    ERROR_FROM_AMAZON(130,"conversionstatustype.errorfromamazon"),
    PHASE1COMPLETE(200,"conversionstatustype.phase1complete"),
    PHASE2COMPLETE(300,"conversionstatustype.phase2complete"),
    PHASE3COMPLETE(400,"conversionstatustype.phase3complete"),
    PHASE4COMPLETE(500,"conversionstatustype.phase4complete"),
    NEEDSMEDIAOBJECTUPDATE(600,"conversionstatustype.needsmediaobjectupdate"),
    COMPLETE(1000,"conversionstatustype.complete"),
    POSTFAIL(1001,"conversionstatustype.failed_updated"),
    POSTCANCEL(1002,"conversionstatustype.canceled_updated"),
    COMPLETE_ORIENTATIONSET(1003,"conversionstatustype.completeoritentationset"),
    POST_PROC_STARTED(1010,"conversionstatustype.postprocessingstarted" ),
    POST_PROC_COMPLETED(1020,"conversionstatustype.postprocessingcompleted" ),
    POST_PROC_FAILED(1030,"conversionstatustype.postprocessingfailed" );


    private final int conversionStatusTypeId;

    private String key;


    private ConversionStatusType( int p , String key )
    {
        this.conversionStatusTypeId = p;

        this.key = key;
    }

    public boolean getReadyForViewing()
    {
        return conversionStatusTypeId == 0 || conversionStatusTypeId == 1000 || conversionStatusTypeId == 1003 || equals(POST_PROC_STARTED) || equals(POST_PROC_COMPLETED) || equals(POST_PROC_FAILED);
    }
    
    public boolean getIsInAValidState()
    {

        return getIsActive() ||
            conversionStatusTypeId == 1000;
    }

    
    public boolean getIsComplete()
    {
        return equals( COMPLETE ) || equals( COMPLETE_ORIENTATIONSET );
    }

    public boolean getIsActiveOrNeedsUpdate()
    {
        return getIsActive() || conversionStatusTypeId == 600;
    }


    public boolean getIsActive()
    {
        if( conversionStatusTypeId == 1 ||
            conversionStatusTypeId == 100 ||
            conversionStatusTypeId == 200 ||
            conversionStatusTypeId == 300 ||
            conversionStatusTypeId == 400 ||
            conversionStatusTypeId == 500  )
            return true;

        return false;
    }


    public static boolean conversionIncomplete( int statusTypeId )
    {
        switch( statusTypeId )
        {
            case 0:
                return false;

            case 1000:
                return false;
        }

        return true;

    }

    public int getConversionStatusTypeId()
    {
        return this.conversionStatusTypeId;
    }






    public static ConversionStatusType getType( int typeId )
    {
        return getValue( typeId );
    }


    public String getKey()
    {
        return key;
    }



    public static ConversionStatusType getValue( int id )
    {
        ConversionStatusType[] vals = ConversionStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getConversionStatusTypeId() == id )
                return vals[i];
        }

        return NOT_STARTED;
    }

}
