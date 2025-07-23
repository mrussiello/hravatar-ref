package com.tm2ref.ai;

public enum AiMetaScoreStatusType {
    NOT_READY(0, "Not Ready"),
    READY(100, "Ready for Scoring"),
    SCORING_STARTED(105, "Scoring Started"),
    PENDING_ASYNC_SCORES(106, "Scoring - Pending External/Async Scores"),
    SCORED(110, "Scored"),
    REPORT_COMPLETE(120, "Report_Revisions Complete"),
    DEACTIVATED(202, "Deactivated"),
    SCORE_ERROR(203, "Scoring Error");

    private final int aiMetaScoreStatusTypeId;

    private String key;

    private AiMetaScoreStatusType(int p, String key)
    {
        this.aiMetaScoreStatusTypeId = p;

        this.key = key;
    }

    public int getAiMetaScoreStatusTypeId()
    {
        return this.aiMetaScoreStatusTypeId;
    }

    public boolean getHasResultsToShow()
    {
        return aiMetaScoreStatusTypeId >= SCORED.getAiMetaScoreStatusTypeId() && aiMetaScoreStatusTypeId <= REPORT_COMPLETE.getAiMetaScoreStatusTypeId();
    }

    public static AiMetaScoreStatusType getType(int typeId)
    {
        return getValue(typeId);
    }

    public String getKey()
    {
        return key;
    }

    public static AiMetaScoreStatusType getValue(int id)
    {
        AiMetaScoreStatusType[] vals = AiMetaScoreStatusType.values();

        for (int i = 0; i < vals.length; i++)
        {
            if (vals[i].getAiMetaScoreStatusTypeId() == id)
                return vals[i];
        }
        return NOT_READY;
    }

}
