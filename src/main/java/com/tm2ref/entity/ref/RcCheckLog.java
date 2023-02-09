package com.tm2ref.entity.ref;

import com.tm2ref.ref.RcCheckLogLevelType;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;



@Entity
@Table( name = "rcchecklog" )
@NamedQueries( {
    @NamedQuery( name = "RcCheckLog.findByRcCheckId", query = "SELECT o FROM RcCheckLog AS o WHERE o.rcCheckId=:rcCheckId" )
} )
public class RcCheckLog implements Serializable
{

    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "rcchecklogid" )
    private long rcCheckLogId;

    @Column( name = "rcCheckId" )
    private long rcCheckId;

    @Column( name = "rcRaterId" )
    private long rcRaterId;


    /**
     * 2 - info
        1 - warning
        0 - error
     */
    @Column( name = "level" )
    private int level;

    @Column( name = "log" )
    private String log;

    @Column( name = "ipaddress" )
    private String ipAddress;

    @Column( name = "useragent" )
    private String userAgent;

    @Column( name = "intparam1" )
    private int intParam1;

    @Column( name = "intparam2" )
    private int intParam2;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="logdate")
    private Date logDate;


    @Override
    public String toString() {
        return "RcCheckLog{" + "rcCheckLogId=" + rcCheckLogId + ", rcRaterId=" + rcRaterId + ", log=" + log + '}';
    }


    public void appendLogEntry( String entry, int lvl )
    {
        if( entry == null || entry.trim().isEmpty() )
            return;

        if( log == null || rcCheckLogId==0 )
        {
            log = "";
            level = lvl;

        }
        else
        {
            log += " \n\n";

            // only store the most severe level
            if( lvl < level )
                level = lvl;

        }

        log += new Date().toString() + ", LEVEL " + lvl + ", " + entry.trim();
    }

    public RcCheckLogLevelType getRcCheckLogLevelType()
    {
        return RcCheckLogLevelType.getValue(level);
    }

    
    
    public long getRcCheckLogId() {
        return rcCheckLogId;
    }

    public void setRcCheckLogId(long rcCheckLogId) {
        this.rcCheckLogId = rcCheckLogId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }


    public String getLog() {
        return log;
    }

    public void setLog(String l) {

        if( l != null )
            l = l.trim();

        if( l!= null && l.isEmpty() )
            l=null;

        this.log = l;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Date getLogDate() {
        return logDate;
    }

    public void setLogDate(Date logDate) {
        this.logDate = logDate;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public long getRcRaterId() {
        return rcRaterId;
    }

    public void setRcRaterId(long rcRaterId) {
        this.rcRaterId = rcRaterId;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }




}
