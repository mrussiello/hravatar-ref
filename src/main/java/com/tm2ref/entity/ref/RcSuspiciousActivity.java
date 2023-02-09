package com.tm2ref.entity.ref;


import com.tm2ref.entity.user.User;
import com.tm2ref.ref.RcSuspiciousActivityType;
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
@Table( name = "rcsuspiciousactivity" )
@NamedQueries({
        @NamedQuery( name = "RcSuspiciousActivity.findByRcCheckAndRater", query = "SELECT o FROM RcSuspiciousActivity AS o WHERE o.rcCheckId=:rcCheckId AND o.rcRaterId=:rcRaterId" ),    
        @NamedQuery( name = "RcSuspiciousActivity.findByRcCheckId", query = "SELECT o FROM RcSuspiciousActivity AS o WHERE o.rcCheckId=:rcCheckId" )   
})
public class RcSuspiciousActivity implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcsuspiciousactivityid")
    private long rcSuspiciousActivityId;

    @Column(name="rccheckid")
    private long rcCheckId;
        
    @Column(name="rcraterid")
    private long rcRaterId;
    
    @Column(name="suspiciousactivitytypeid")
    private int suspiciousActivityTypeId;

    @Column(name="note")
    private String note;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Transient
    private User user;

    
    @Transient
    private String specialNote = null;
    
    public RcSuspiciousActivityType getRcSuspiciousActivityType()
    {
        return RcSuspiciousActivityType.getValue(suspiciousActivityTypeId);
    }

    @Override
    public String toString() {
        return "RcSuspiciousActivity{" + "rcSuspiciousActivityId=" + rcSuspiciousActivityId + ", rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", suspiciousActivityTypeId=" + suspiciousActivityTypeId + ", note=" + note + '}';
    }
    
    
    
    public long getRcSuspiciousActivityId() {
        return rcSuspiciousActivityId;
    }

    public void setRcSuspiciousActivityId(long rcSuspiciousActivityId) {
        this.rcSuspiciousActivityId = rcSuspiciousActivityId;
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

    public int getSuspiciousActivityTypeId() {
        return suspiciousActivityTypeId;
    }

    public void setSuspiciousActivityTypeId(int suspiciousActivityTypeId) {
        this.suspiciousActivityTypeId = suspiciousActivityTypeId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getSpecialNote() {
        return specialNote;
    }

    public void setSpecialNote(String specialNote) {
        this.specialNote = specialNote;
    }

    
}
