package com.tm2ref.file;

/*
     * type = 1 = file upload 
     *        2 = file upload thumb
     *        11 = rc upload (audio/video)
     *        12 = rc upload thumb
     *        21 = proctoring  (audio/video)
     *        22 = proctoring thumb
     *        31 = lvavscore  (audio/video)
     *        32 - lvcall thumb
     *        41 = ct5 (audio/video)

*/
public enum MediaTempUrlSourceType
{
    UNKNOWN(0,"Unknown" ),
    FILE_UPLOAD(1,"Uploaded File (usually audio or video)" ),
    FILE_UPLOAD_THUMB(2,"Uploaded Thumb" ),
    REF_AV(11,"RC Ref Check Audio Video" ),
    REF_THUMB(12,"Ref Check Thumb"),
    PROCTOR_AV(21,"Proctor Audio/Video"),
    PROCTOR_THUMB(22,"Proctor Thumb" ),
    LV_AV(31,"Live Video LV Audio Video"),
    LV_THUMB(32,"LV Call Recipient Thumb"),
    CT5_AV(41,"CT5 Audio Video");

    private final int mediaTempUrlSourceTypeId;

    private String key;

    private MediaTempUrlSourceType( int p , String key )
    {
        this.mediaTempUrlSourceTypeId = p;
        this.key = key;

    }


    public String getName()
    {
    	return key;
    }

    public static MediaTempUrlSourceType getType( int typeId )
    {
        return getValue( typeId );
    }

    public static MediaTempUrlSourceType getValue( int id )
    {
        MediaTempUrlSourceType[] vals = MediaTempUrlSourceType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMediaTempUrlSourceTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }

    public int getMediaTempUrlSourceTypeId() {
        return mediaTempUrlSourceTypeId;
    }

    
    
}
