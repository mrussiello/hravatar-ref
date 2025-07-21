package com.tm2ref.essay;



public enum UnscoredEssayType
{
    ESSAY(0,"Essay"),
    AV_TRANSCRIPT(10,"AV Transcript"),
    UPLOADED_FILE(20,"Uploaded File"),
    RC_UPLOADED_FILE(30,"RC Uploaded File"),
    RC_COMMENT(40,"RC Candidate Comment");


    private final int unscoredEssayTypeId;

    private final String name;


    private UnscoredEssayType( int s , String n )
    {
        this.unscoredEssayTypeId = s;

        this.name = n;
    }



    public static UnscoredEssayType getValue( int id )
    {
        UnscoredEssayType[] vals = UnscoredEssayType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getUnscoredEssayTypeId() == id )
                return vals[i];
        }

        return ESSAY;
    }
    
    public boolean getIsEssay()
    {
        return equals( ESSAY); 
    }

    public boolean getIsAvTranscript()
    {
        return equals( AV_TRANSCRIPT); 
    }

    public boolean getIsUploadedFile()
    {
        return equals( UPLOADED_FILE); 
    }

    public boolean getLocalPlagCheckOk()
    {
        return getIsEssay() || getIsUploadedFile();
    }
    public boolean getWebPlagCheckOk()
    {
        return getIsEssay();
    }
    
    public boolean getStopScoringOnPlagarism()
    {
        return getIsEssay();
    }
    
    public boolean getSupportsSpellingGrammarAnalysis()
    {
        return equals( ESSAY); 
    }

    public int getUnscoredEssayTypeId()
    {
        return unscoredEssayTypeId;
    }

    public String getName()
    {
        return name;
    }

}
