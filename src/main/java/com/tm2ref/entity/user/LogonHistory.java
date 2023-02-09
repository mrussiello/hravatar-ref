/*
 * Created on Dec 28, 2006
 *
 */
package com.tm2ref.entity.user;

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
@Table( name="logonhistory" )
@NamedQueries({
})
public class LogonHistory implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="logonhistoryid")
    private long logonHistoryId;

    @Column(name="userid")
    private long userId;

    @Column(name="orgid")
    private long orgId;

    @Column(name="suborgid")
    private long suborgId;

    @Column(name="logontypeid")
    private int logonTypeId = 0;

    @Column(name="logofftypeid")
    private int logoffTypeId = 0;

    @Column(name="systemid")
    private int systemId;

    @Column(name="useragent")
    private String userAgent;

    @Column(name="ipaddress")
    private String ipAddress;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="logondate")
    private Date logonDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="logoffdate")
    private Date logoffDate;


    @Override
    public boolean equals( Object o )
    {
        if( o instanceof LogonHistory )
            return ((LogonHistory)o).getLogonHistoryId() == logonHistoryId;

        return false;
    }

    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (int) (this.logonHistoryId ^ (this.logonHistoryId >>> 32));
        return hash;
    }


    /**
     * @return the logoffDate
     */
    // @Transient
    public Date getLogoffDate()
    {
        return logoffDate;
    }

    /**
     * @param logoffDate the logoffDate to set
     */
    public void setLogoffDate(Date logoffDate)
    {
        this.logoffDate = logoffDate;
    }

    /**
     * @return the logonDate
     */
    public Date getLogonDate()
    {
        return logonDate;
    }

    /**
     * @param logonDate the logonDate to set
     */
    public void setLogonDate(Date logonDate)
    {
        this.logonDate = logonDate;
    }

    /**
     * @return the userId
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    /**
     * @return the logonHistoryId
     */
    public long getLogonHistoryId()
    {
        return logonHistoryId;
    }

    /**
     * @param logonHistoryId the logonHistoryId to set
     */
    public void setLogonHistoryId(long logonHistoryId)
    {
        this.logonHistoryId = logonHistoryId;
    }

    public int getLogonTypeId()
    {
        return logonTypeId;
    }

    public void setLogonTypeId( int logonTypeId )
    {
        this.logonTypeId = logonTypeId;
    }

    public int getLogoffTypeId()
    {
        return logoffTypeId;
    }

    public void setLogoffTypeId( int logoffTypeId )
    {
        this.logoffTypeId = logoffTypeId;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public long getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(long suborgId) {
        this.suborgId = suborgId;
    }

    public int getSystemId() {
        return systemId;
    }

    public void setSystemId(int systemId) {
        this.systemId = systemId;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }


}
