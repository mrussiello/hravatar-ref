package com.tm2ref.ai;

public enum AiCallType
{
    NONE(0,"None", "none", false),
    TEST_ASYNC(1,"Test Async Call", "testasync", true),
    TEST_SYNC(2,"Test Synchronous Call", "testsync", false),
    TEST_CONNECT(3,"Test System Availability", "sysavailability", false ),
    JOBDESCRIP_SUMMARY(10,"Job Descrip Summary", "jobdescripsummary", false),
    JOBDESCRIP_PARSE(11,"Job Descrip Parse", "jobdescripparse", false),
    RESUME_SUMMARY(20,"Resume Summary", "resumesummary", false),
    RESUME_EDUCATION(21,"Resume Education", "resumeeducaiton", false),
    RESUME_EXPERIENCE(22,"Resume Experience", "resumeexperience", false),
    RESUME_PARSE(23,"Resume Parse", "resumeparse", false),
    ORGTRAITS_SUMMARY(30,"OrgTraits Summary", "orgtraitssummary", false),
    ORGTRAITS_PARSE_FULL(31,"OrgTraits Parse Full", "orgtraitsparse", false),
    ORGTRAITS_PARSE_NOCOMPS(32,"OrgTraits Parse No Competencies", "orgtraitsparsenocomps", false),
    ORGTRAITS_PARSE_COMPSONLY(33,"OrgTraits Parse Competencies Only", "orgtraitsparsecompsonly", false),
    EVALPLAN_SCORE(100,"EvalPlan Score", "evalplanscore", false ),
    ESSAY_SCORE(200,"Essay Score", "essayscore", false );

    private final int aiCallTypeId;
    private final String name;
    private final String tran;
    private final boolean async;

    private AiCallType( int typeId , String nm, String trn, boolean async )
    {
        this.aiCallTypeId = typeId;
        this.name = nm;
        this.tran = trn;
        this.async=async;
    }

    public boolean isJobSummary()
    {
        return equals( JOBDESCRIP_SUMMARY );
    }
    
    public boolean isOrgTraitsSummary()
    {
        return equals( ORGTRAITS_SUMMARY );
    }
    
    public boolean isResumeSummary()
    {
        return equals( RESUME_SUMMARY );
    }
    
    
    public boolean isAsync() {
        return async;
    }

    
    public String getName()
    {
        return name;
    }
    
    public String getTran()
    {
        return tran;
    }
    
    
    public int getAiCallTypeId()
    {
        return this.aiCallTypeId;
    }

    public static AiCallType getValue( int id )
    {
        AiCallType[] vals = AiCallType.values();
        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getAiCallTypeId()==id )
                return vals[i];
        }
        return NONE;
    }


}
