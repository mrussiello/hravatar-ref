package com.tm2ref.entity.email;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
@Table( name="smsblock" )
@NamedQueries({
    
    @NamedQuery ( name="SmsBlock.findForPhoneNumber", query="SELECT o FROM SmsBlock AS o WHERE o.phoneNumber=:phoneNumber" ),
    @NamedQuery ( name="SmsBlock.findActiveForPhoneNumber", query="SELECT o FROM SmsBlock AS o WHERE o.phoneNumber=:phoneNumber AND (o.smsBlockReasonId=1 OR (o.smsBlockReasonId=0 AND o.lastUpdate IS NOT NULL AND o.lastUpdate<:maxDate)) " )
    
})
public class SmsBlock implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="smsblockid")
    private long smsBlockId;

    @Column(name="userid")
    private long userId;

    /*
      0 = temporary - undelivered. Do not text again for 1 month after last update
      1 = permanent - never text again
    */
    @Column(name="smsblockreasonid")
    private int smsBlockReasonId;

    /*
     Digits only. This is used for matching.
    */
    @Column(name="phonenumber")
    private String phoneNumber;

    /*
     As reported during block process.
    */
    @Column(name="phonenumberformatted")
    private String phoneNumberFormatted;
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Override
    public String toString() {
        return "SmsBlock{" + "smsBlockId=" + smsBlockId + ", userId=" + userId + ", smsBlockReasonId=" + smsBlockReasonId + ", phoneNumber=" + phoneNumber + ", phoneNumberFormatted=" + phoneNumberFormatted + '}';
    }


    public boolean getIsActiveBlock()
    {
        if( this.smsBlockReasonId<0 )
            return false;
        
        if( smsBlockReasonId==1 )
            return true;
        
        if( smsBlockReasonId!=0 || lastUpdate==null )
            return false;
        
        Calendar cal = new GregorianCalendar();
        cal.add( Calendar.MONTH, -1 );
        return lastUpdate.before(cal.getTime());
    }

    
    public Date getCreateDate()
    {
        return createDate;
    }


    public void setCreateDate( Date createDate )
    {
        this.createDate = createDate;
    }





    public long getUserId()
    {
        return userId;
    }


    public void setUserId( long userId )
    {
        this.userId = userId;
    }

    public long getSmsBlockId() {
        return smsBlockId;
    }

    public void setSmsBlockId(long smsBlockId) {
        this.smsBlockId = smsBlockId;
    }

    public int getSmsBlockReasonId() {
        return smsBlockReasonId;
    }

    public void setSmsBlockReasonId(int smsBlockReasonId) {
        this.smsBlockReasonId = smsBlockReasonId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public String getPhoneNumberFormatted() {
        return phoneNumberFormatted;
    }

    public void setPhoneNumberFormatted(String phoneNumberFormatted) {
        this.phoneNumberFormatted = phoneNumberFormatted;
    }




}
