package com.tm2ref.ref;



/**
 * @author miker_000
 */
public enum RcCompetencyType
{
    ESSENTIAL(0,"Essential", "rcompt.essential" ),
    DEFAULT_WORK_COMPETENCY(1,"Default Work Competency", "rcompt.competency" ),
    GENERAL(2,"General", "rcompt.general" ),
    MANAGER(3,"Manager", "rcompt.manager" ),
    INDIVIDUAL(4,"Individual", "rcompt.individual" ),
    SALES(5,"Sales", "rcompt.sales" ),
    LOWLEVEL(6,"Low Level", "rcompt.lowlevel" ),
    CUSTOM(10,"Custom", "rcompt.custom" );  

    private final int rcCompetencyTypeId;

    private final String name;
    private String key;


    private RcCompetencyType( int s , String n, String k )
    {
        this.rcCompetencyTypeId = s;
        this.name = n;
        this.key = k;
    }
    

    public static RcCompetencyType getValue( int id )
    {
        RcCompetencyType[] vals = RcCompetencyType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcCompetencyTypeId() == id )
                return vals[i];
        }

        return ESSENTIAL;
    }


    public int getRcCompetencyTypeId()
    {
        return rcCompetencyTypeId;
    }

    public String getName()
    {
        return name;
    }

}
