package com.tm2ref.entity.cscase;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Cacheable;

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


@Cacheable
@Entity
@Table( name="cscaseentry" )
@NamedQueries({
})
public class CSCaseEntry implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="cscaseentryid")
    private long csCaseEntryId;

    @Column(name="cscaseid")
    private long csCaseId;

    @Column(name="userid")
    private long userId;

    @Column(name="message")
    private String message;

    @Column(name="emailsent")
    private int emailSent;

    @Column(name="format")
    private int format;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    public long getCsCaseEntryId() {
        return csCaseEntryId;
    }

    public void setCsCaseEntryId(long csCaseEntryId) {
        this.csCaseEntryId = csCaseEntryId;
    }

    public long getCsCaseId() {
        return csCaseId;
    }

    public void setCsCaseId(long csCaseId) {
        this.csCaseId = csCaseId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getEmailSent() {
        return emailSent;
    }

    public void setEmailSent(int emailSent) {
        this.emailSent = emailSent;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }




}
