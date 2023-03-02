/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.entity.ref;

import com.tm2ref.entity.user.User;
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

/**
 *
 * @author miker
 */
@Entity
@Table( name = "rcreferral" )
@NamedQueries( {
    @NamedQuery( name = "RcReferral.findByRcCheckId", query = "SELECT o FROM RcReferral AS o WHERE o.rcCheckId=:rcCheckId" ),
    @NamedQuery( name = "RcReferral.findByRcCheckIdAndRcRaterId", query = "SELECT o FROM RcReferral AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId" )
} )
public class RcReferral  implements Serializable
{

    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "rcreferralid" )
    private long rcReferralId;

    @Column( name = "rcreferraltypeid" )
    private int rcReferralTypeId;
            
    @Column( name = "rcCheckId" )
    private long rcCheckId;

    @Column( name = "rcRaterId" )
    private long rcRaterId;

    @Column( name = "orgid" )
    private int orgId;
        
    @Column(name="referreruserid")
    private long referrerUserId;

    @Column(name="userid")
    private long userId;

    @Column(name="rcscriptid")
    private int rcScriptId;
    
    @Column(name="targetrole")
    private String targetRole;
    
    @Column(name="referrernotes")
    private String referrerNotes;
    
    @Column(name="notes")
    private String notes;
    
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    
    
    @Override
    public String toString() {
        return "RcReferral{" + "rcReferralId=" + rcReferralId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", userId=" + userId + '}';
    }

    @Transient
    private User user;
    
    @Transient
    private User referrerUser;
    
    
    
    public long getRcReferralId() {
        return rcReferralId;
    }

    public void setRcReferralId(long rcReferralId) {
        this.rcReferralId = rcReferralId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public long getRcRaterId() {
        return rcRaterId;
    }

    public void setRcRaterId(long rcRaterId) {
        this.rcRaterId = rcRaterId;
    }

    public long getReferrerUserId() {
        return referrerUserId;
    }

    public void setReferrerUserId(long referrerUserId) {
        this.referrerUserId = referrerUserId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRcScriptId() {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId) {
        this.rcScriptId = rcScriptId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getReferrerUser() {
        return referrerUser;
    }

    public void setReferrerUser(User referrerUser) {
        this.referrerUser = referrerUser;
    }

    public int getRcReferralTypeId() {
        return rcReferralTypeId;
    }

    public void setRcReferralTypeId(int rcReferralTypeId) {
        this.rcReferralTypeId = rcReferralTypeId;
    }

    public String getTargetRole() {
        return targetRole;
    }

    public void setTargetRole(String targetRole) {
        this.targetRole = targetRole;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getReferrerNotes() {
        return referrerNotes;
    }

    public void setReferrerNotes(String referrerNotes) {
        this.referrerNotes = referrerNotes;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    
    
}
