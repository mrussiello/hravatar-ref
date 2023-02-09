/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Mike
 */
public class SessionInfo implements Serializable, Cloneable, Comparable<SessionInfo>
{
    public String user;
    public String corp;
    public String rcCheck;
    public String rcRater;
    public Integer orgId;
    public String status;
    public String type;
    public Date last;


    @Override
    public int compareTo(SessionInfo o)
    {
       if( last != null && o.last != null )
           return o.last.compareTo(last);

       return 0;
    }

    
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
    
    public int getSecs()
    {
        if( last == null )
            return 0;

        return (int) (new Date().getTime() - last.getTime())/1000;
    }

    public String getCorp() {
        return corp;
    }

    public String getStatus() {
        return status;
    }

    public String getRcCheck() {
        return rcCheck;
    }

    public Integer getOrgId() {
        return orgId;
    }


    public String getUser() {
        return user;
    }

    public String getRcRater() {
        return rcRater;
    }

    public String getType() {
        return type;
    }



}
