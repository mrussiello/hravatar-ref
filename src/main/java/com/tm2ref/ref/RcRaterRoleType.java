package com.tm2ref.ref;

import com.tm2ref.util.MessageFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jakarta.faces.model.SelectItem;



/**
 * 
 * @author miker_000
 */
public enum RcRaterRoleType
{
    UNKNOWN(0,"Unknown", "rcrrt.unknown"),
    MANAGER(5,"Manager", "rcrrt.manager"),
    SUPERVISOR(10,"Supervisor", "rcrrt.supervisor"),
    PEER(15,"Peer", "rcrrt.peer"),
    SUBORDINATE(20,"Subordinate", "rcrrt.subordinate"),
    OTHER(30,"Other", "rcrrt.other"),
    OTHER2(31,"Other 2", "rcrrt.other2"),
    OTHER3(32,"Other 3", "rcrrt.other3");

    private final int rcRaterRoleTypeId;

    private final String name;
    private String key;


    private RcRaterRoleType( int s , String n, String k )
    {
        this.rcRaterRoleTypeId = s;

        this.name = n;
        this.key = k;
    }

    //public static List<SelectItem> getSelectItemList( Locale l, boolean inclUnknown )
    //{
    //    return getSelectItemList( l, inclUnknown, null );
    //}
    
    
    public static List<SelectItem> getSelectItemList( Locale l, boolean inclUnknown, String[] otherNames )
    {
        if( l==null )
            l = Locale.US;
        
        List<SelectItem> out = new ArrayList<>();

        for( RcRaterRoleType v : RcRaterRoleType.values())
        {
            if( v.equals( UNKNOWN ) && !inclUnknown )
                continue;
            
            if( v.equals(OTHER2) && (otherNames==null || otherNames[1]==null) )
                continue;
            
            if( v.equals(OTHER3) && (otherNames==null || otherNames[2]==null) )
                continue;
                        
            out.add( new SelectItem( v.getRcRaterRoleTypeId(), v.getName(l, otherNames ) ) );
        }

        return out;
    }
    
    
    public boolean getIsSupervisorOrManager()
    {
        return equals(MANAGER) || equals(SUPERVISOR);
    }
    public boolean getIsPeer()
    {
        return equals(PEER);
    }
    public boolean getIsSubordinate()
    {
        return equals(SUBORDINATE);
    }
    public boolean getIsOther()
    {
        return equals(OTHER);
    }
        
    

    public static RcRaterRoleType getValue( int id )
    {
        RcRaterRoleType[] vals = RcRaterRoleType.values();

        for( int i=0 ; i<vals.length ; i++ )
        {
            if( vals[i].getRcRaterRoleTypeId() == id )
                return vals[i];
        }

        return UNKNOWN;
    }


    public int getRcRaterRoleTypeId()
    {
        return rcRaterRoleTypeId;
    }

    public String getName()
    {
        return getName(Locale.US, null );
    }
    

    public String getName( Locale l, String[] otherNames)
    {
        if( otherNames!=null && otherNames.length==3 )
        {
            if( equals( OTHER ) && otherNames[0]!=null )
                return otherNames[0];
                
            if( equals( OTHER2 ) && otherNames[1]!=null )
                return otherNames[1];
            
            if( equals( OTHER3 ) && otherNames[2]!=null )
                return otherNames[2];
        }
        
        if( l==null )
            l = Locale.US;
        
        return MessageFactory.getStringMessage(l, key );
    }
    
}
