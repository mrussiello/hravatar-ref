package com.tm2ref.entity.cscase;


import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.Cacheable;

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


@Cacheable
@Entity
@Table( name = "cscase" )
@NamedQueries( {
} )
public class CSCase implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "cscaseid" )
    private long csCaseId;

    @Column( name = "cscasetypeid" )
    private int csCaseTypeId = 1;

    @Column( name = "cscasestatustypeid" )
    private int csCaseStatusTypeId = 1;

    @Column( name = "userid" )
    private long userId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "rccheckid" )
    private long rcCheckId;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "createdate" )
    private Date createDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastupdate" )
    private Date lastUpdate;

    @Column( name = "localestr" )
    private String localeStr;

    @Transient
    private Locale locale;

    @Transient
    private List<CSCaseEntry> csCaseEntryList;

        


    public int getEntryCount()
    {
        if(csCaseEntryList == null)
            return 0;

        return csCaseEntryList.size();
    }



    @Override
    public String toString()
    {
        return "CSCaseId=" + csCaseId + ", userId=" + userId + ", "
                + (createDate == null ? "null date" : createDate.toString())
                + ", status=" + csCaseStatusTypeId;
    }

    /**
     * @return the createDate
     */
    public Date getCreateDate()
    {
        return createDate;
    }

    /**
     * @param createDate
     *            the createDate to set
     */
    public void setCreateDate( Date createDate )
    {
        this.createDate = createDate;
    }

    /**
     * @return the csCaseId
     */
    public long getCsCaseId()
    {
        return csCaseId;
    }

    /**
     * @param csCaseId
     *            the csCaseId to set
     */
    public void setCsCaseId( long csCaseId )
    {
        this.csCaseId = csCaseId;
    }

    /**
     * @return the csCaseTypeId
     */
    public int getCsCaseTypeId()
    {
        return csCaseTypeId;
    }

    /**
     * @param csCaseTypeId
     *            the csCaseTypeId to set
     */
    public void setCsCaseTypeId( int csCaseTypeId )
    {
        this.csCaseTypeId = csCaseTypeId;
    }

    /**
     * @return the lastUpdate
     */
    public Date getLastUpdate()
    {
        return lastUpdate;
    }

    /**
     * @param lastUpdate
     *            the lastUpdate to set
     */
    public void setLastUpdate( Date lastUpdate )
    {
        this.lastUpdate = lastUpdate;
    }

    /**
     * @return the userId
     */
    public long getUserId()
    {
        return userId;
    }

    /**
     * @param userId
     *            the userId to set
     */
    public void setUserId( long userId )
    {
        this.userId = userId;
    }

    /**
     * @return the csCaseEntryList
     */
    public List<CSCaseEntry> getCsCaseEntryList()
    {
        return csCaseEntryList;
    }

    /**
     * @param csCaseEntryList
     *            the csCaseEntryList to set
     */
    public void setCsCaseEntryList( List<CSCaseEntry> csCaseEntryList )
    {
        this.csCaseEntryList = csCaseEntryList;
    }

    public int getCsCaseStatusTypeId()
    {
        return csCaseStatusTypeId;
    }

    public void setCsCaseStatusTypeId( int csCaseStatusTypeId )
    {
        this.csCaseStatusTypeId = csCaseStatusTypeId;
    }

    public String getLocaleStr()
    {
        return localeStr;
    }

    public void setLocaleStr( String l )
    {
        this.localeStr = l;
    }

    public Locale getLocale()
    {
        return locale;
    }

    public void setLocale( Locale locale )
    {
        this.locale = locale;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }




}
