package com.tm2ref.ref;

import com.tm2ref.file.FileContentType;
import com.tm2ref.util.MessageFactory;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public enum RcCandidateUploadType {
    
    NONE(0,"None", "rccanupldtype.none"),
    ANY_GEN(1,"Any General File", "rccanupldtype.gen"),
    EXCEL(2,"Excel CSV", "rccanupldtype.excel"),
    POWERPOINT(3,"PowerPoint", "rccanupldtype.powerpoint"),
    WORD(4,"Word", "rccanupldtype.word"),
    PDF(6,"PDF", "rccanupldtype.pdf"),
    ZIP(5,"ZIP", "rccanupldtype.zip"),
    TEXT(7,"Text", "rccanupldtype.txt"),
    ANYTEXT(10,"Any Parsable Text (Word, PDF, txt)", "rccanupldtype.anytext"),
    AUDIO(20,"Audio", "rccanupldtype.audio"),
    VIDEO(21,"Video", "rccanupldtype.video"),
    AUDIO_VIDEO(22,"Audio or Video", "rccanupldtype.audiovideo");

    private final int rcCandidateUploadTypeId;

    private final String name;
    private String key;

    private RcCandidateUploadType(int s, String n, String k)
    {
        this.rcCandidateUploadTypeId = s;

        this.name = n;
        this.key = k;
    }

    public boolean getIsUploadedFileContentTypeValid(FileContentType fct)
    {
        if (equals(ANY_GEN))
            return fct.isValidForCandidateUploadedFile();

        if (equals(EXCEL))
            return fct.isExcel();

        if (equals(POWERPOINT))
            return fct.isPowerPoint();

        if (equals(WORD) )
            return fct.isWord();

        if (equals(PDF))
            return fct.isPdf();

        if (equals(TEXT))
            return fct.isTxt();

        if (equals(ZIP))
            return fct.isZip();

        if (equals(AUDIO))
            return fct.isAudio();

        if (equals(VIDEO))
            return fct.isVideo();

        if (equals(AUDIO_VIDEO))
            return fct.isAudio() || fct.isVideo();

        if( equals(ANYTEXT) )
            return fct.isWord() || fct.isPdf() || fct.isTxt();
        
        return false;
    }

    public boolean getActive()
    {
        return !equals(NONE);
    }

    public boolean getAnyAudioVideo()
    {
        return equals(AUDIO) || equals(VIDEO) || equals(AUDIO_VIDEO);
    }

    public boolean getAudio()
    {
        return equals(AUDIO);
    }

    public boolean getVideo()
    {
        return equals(VIDEO);
    }

    public boolean getAudioVideo()
    {
        return equals(AUDIO_VIDEO);
    }

    public static RcCandidateUploadType getValue(int id)
    {
        RcCandidateUploadType[] vals = RcCandidateUploadType.values();

        for (int i = 0; i < vals.length; i++)
        {
            if (vals[i].getRcCandidateUploadTypeId() == id)
                return vals[i];
        }

        return NONE;
    }

    public int getRcCandidateUploadTypeId()
    {
        return rcCandidateUploadTypeId;
    }

    public String getName()
    {
        return name;
    }

    public String getName(Locale l)
    {
        if (l == null)
            l = Locale.US;

        return MessageFactory.getStringMessage(l, key);
    }

}
