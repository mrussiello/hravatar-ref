package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcImportanceType
{
    NONE(0,"None", "rcompimpt.none", 0 ),
    VERYLOW(1,"Low", "rcompimpt.verylow", 1f ),
    LOW(2,"Low", "rcompimpt.low", 2f ),
    MEDIUM(3,"Medium", "rcompimpt.medium",3f ),
    HIGH(4,"High", "rcompimpt.high",4f ),
    VERYHIGH(5,"Very High", "rcompimpt.veryhigh",5f ),
    USE_ONET(99,"Use Onet Value", "rcompimpt.useonet",99f );

    private final int rcImportanceTypeId;

    private final String name;
    private String key;
    private final float importance;


    private RcImportanceType( int s , String n, String k, float importance )
    {
        this.rcImportanceTypeId = s;
        this.name = n;
        this.key = k;
        this.importance=importance;
    }

    public float getImportance() {
        return importance;
    }
    
    
    
            
    
    public static RcImportanceType getValue( int id )
    {
        RcImportanceType[] vals = RcImportanceType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcImportanceTypeId() == id )
                return vals[i];
        }

        return NONE;
    }


    public int getRcImportanceTypeId()
    {
        return rcImportanceTypeId;
    }

    public String getName()
    {
        return name;
    }

}
