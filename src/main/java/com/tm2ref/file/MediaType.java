package com.tm2ref.file;


public enum MediaType
{
    TEXT(1,"text" ),
    IMAGE(2,"image" ),
    AUDIO(3,"audio"),
    VIDEO(4,"video"),
    PDF(5,"pdf" ),
    SWF(6,"swf"),
    PPT(7,"ppt"),
    DOC(8,"document"),
    ZIP(9,"zip");

    private final int mediaTypeId;

    private String key;

    private MediaType( int p , String key )
    {
        this.mediaTypeId = p;

        this.key = key;

    }


    public boolean getIsVideo()
    {
        return equals( VIDEO );
    }

    public boolean getIsAudio()
    {
        return equals( AUDIO );
    }

    public boolean getIsAudioVideo()
    {
        return getIsAudio() || getIsVideo();
    }

    public int getMediaTypeId()
    {
        return this.mediaTypeId;
    }


    public String getName()
    {
    	return key;
    }

    public static MediaType getType( int typeId )
    {
        return getValue( typeId );
    }

    public static MediaType getValue( int id )
    {
        MediaType[] vals = MediaType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getMediaTypeId() == id )
                return vals[i];
        }

        return TEXT;
    }

}
