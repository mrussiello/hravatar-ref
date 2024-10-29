package com.tm2ref.ref;


/**
 * 
 * @author miker_000
 */
public enum RcItemStatusType
{
    ACTIVE(0,"Active"),
    ARCHIVED(100,"Archived");

    private final int rcItemStatusTypeId;

    private final String name;


    private RcItemStatusType( int s , String n )
    {
        this.rcItemStatusTypeId = s;
        this.name = n;
    }
    
    
    public boolean getIsActive()
    {
        return rcItemStatusTypeId==ACTIVE.rcItemStatusTypeId;        
    }
    

    public int getRcItemStatusTypeId()
    {
        return rcItemStatusTypeId;
    }

    public String getName()
    {
        return name;
    }
    
    public static RcItemStatusType getValue( int id )
    {
        RcItemStatusType[] vals = RcItemStatusType.values();

        for (RcItemStatusType val : vals) {
            if (val.getRcItemStatusTypeId() == id) {
                return val;
            }
        }
        return ACTIVE;
    }
    

}
