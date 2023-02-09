package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcAvType
{
    NONE(0,"None", "rcavt.none" ),
    AUDIO(1,"Audio", "rcavt.audio" ),
    AUDIO_OR_VIDEO(2,"Audio or Video", "rcavt.videoandaudio" );  

    private final int rcAvTypeId;

    private final String name;
    private String key;


    private RcAvType( int s , String n, String k )
    {
        this.rcAvTypeId = s;

        this.name = n;
        this.key = k;
    }
    
    public boolean getAnyMedia()
    {
        return equals(AUDIO) || equals(AUDIO_OR_VIDEO);
    }
    
    public boolean getAudio()
    {
        return equals(AUDIO);
    }
    
    public boolean getAudioOrVideo()
    {
        return equals(AUDIO_OR_VIDEO);
    }
    
            
    public static RcAvType getValue( int id )
    {
        RcAvType[] vals = RcAvType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcAvTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getRcAvTypeId()
    {
        return rcAvTypeId;
    }

    public String getName()
    {
        return name;
    }

}
