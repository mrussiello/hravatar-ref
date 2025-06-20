package com.tm2ref.ai;


public enum AiCallSourceType
{
    UNKNOWN(0,"Unknown"),
    BUILDER(1,"Sim Builder"),
    ADMIN(2,"HRA Admin"),
    SCORE(3,"HRA Score"),
    TEST(4,"HRA Test"),
    ALT(5,"HRA Alt Test"),
    REF(6,"HRA Ref");

    private final int aiCallSourceTypeId;
    private final String name;

    private AiCallSourceType( int typeId , String nm )
    {
        this.aiCallSourceTypeId = typeId;
        this.name = nm;
    }

    public String getName()
    {
        return name;
    }
        
    public int getAiCallSourceTypeId()
    {
        return this.aiCallSourceTypeId;
    }

    public static AiCallSourceType getValue( int id )
    {
        for (AiCallSourceType val : AiCallSourceType.values()) 
        {
            if (val.getAiCallSourceTypeId()==id) 
            {
                return val;
            }
        }
        return UNKNOWN;
    }

}
