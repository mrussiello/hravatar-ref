package com.tm2ref.entity.event;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;



@Entity
@Table( name = "testeventlog" )
@NamedQueries( {
} )
public class TestEventLog implements Serializable
{

    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testEventlogid" )
    private long testEventLogId;

    @Column( name = "testKeyId" )
    private long testKeyId;

    
    
    @Column( name = "testEventId" )
    private long testEventId;

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

    @Column( name = "imostatustypeid" )
    private int imoStatusTypeId;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="logdate")
    private Date logDate;


    @Override
    public String toString() {
        return "TestEventLog{" + "testEventLogId=" + testEventLogId + ", testEventId=" + testEventId + ", level=" + level + ", log=" + log + '}';
    }


    public void appendLogEntry( String entry, int lvl )
    {
        if( entry == null || entry.trim().isEmpty() )
            return;

        if( log == null || testEventLogId==0 )
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

    public long getTestEventLogId() {
        return testEventLogId;
    }

    public void setTestEventLogId(long testEventLogId) {
        this.testEventLogId = testEventLogId;
    }

    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
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

    public int getImoStatusTypeId() {
        return imoStatusTypeId;
    }

    public void setImoStatusTypeId(int imoStatusTypeId) {
        this.imoStatusTypeId = imoStatusTypeId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }




}
