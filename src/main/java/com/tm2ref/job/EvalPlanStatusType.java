package com.tm2ref.job;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;


public enum EvalPlanStatusType
{
    INACTIVE(0,"Inactive"),
    ACTIVE(1,"Active"),
    ARCHIVED(99,"Archived");

    private final int evalPlanStatusTypeId;

    private String key;


    private EvalPlanStatusType( int p , String key )
    {
        this.evalPlanStatusTypeId = p;

        this.key = key;
    }




    public int getEvalPlanStatusTypeId()
    {
        return this.evalPlanStatusTypeId;
    }



    public static Map<String,Integer> getMap( Locale locale )
    {
        Map<String,Integer> outMap = new TreeMap<>();

        EvalPlanStatusType[] vals = EvalPlanStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
            outMap.put(vals[i].getKey() , vals[i].getEvalPlanStatusTypeId());

        return outMap;
    }





    public String getName()
    {
        return key;
    }



    public static EvalPlanStatusType getType( int typeId )
    {
        return getValue( typeId );
    }

    public String getKey()
    {
        return key;
    }



    public static EvalPlanStatusType getValue( int id )
    {
        EvalPlanStatusType[] vals = EvalPlanStatusType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getEvalPlanStatusTypeId() == id )
                return vals[i];
        }

        return INACTIVE;
    }

}
