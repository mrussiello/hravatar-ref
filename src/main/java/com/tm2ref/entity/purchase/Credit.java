package com.tm2ref.entity.purchase;



import com.tm2ref.purchase.CreditStatusType;
import java.io.Serializable;
import java.util.Date;
import java.util.Locale;

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
@Table( name = "credit" )
@NamedQueries( {
    @NamedQuery( name = "Credit.findByCreditId", query = "SELECT o FROM Credit AS o WHERE o.creditId=:creditId" ),
    @NamedQuery( name = "Credit.findAvailEntriesForOrg", query = "SELECT o FROM Credit AS o WHERE o.orgId=:orgId AND o.creditStatusTypeId=1 AND o.creditTypeId=:creditTypeId AND o.expireDate>:today AND o.remainingCount>0 ORDER BY o.expireDate" ),
    @NamedQuery( name = "Credit.findLastEntryForOrg", query = "SELECT o FROM Credit AS o WHERE o.orgId=:orgId AND o.creditTypeId=:creditTypeId and o.creditStatusTypeId IN (1,2,3,5) ORDER BY o.expireDate DESC" )
} )
public class Credit implements Serializable, Comparable<Credit>
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "creditid" )
    private long creditId;

    @Column( name = "credittypeid" )
    private int creditTypeId;
    
    @Column( name = "orderid" )
    private long orderId;

    @Column( name = "orderitemid" )
    private long orderItemId;

    @Column( name = "userid" )
    private long userId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "creditsourcetypeid" )
    private int creditSourceTypeId;

    @Column( name = "creditstatustypeid" )
    private int creditStatusTypeId = CreditStatusType.ACTIVE.getCreditStatusTypeId();

    @Column( name = "initialcount" )
    private int initialCount;

    @Column( name = "usedcount" )
    private int usedCount;

    @Column( name = "remainingcount" )
    private int remainingCount;

    @Column( name = "overagecount" )
    private int overageCount;

    
    @Column( name = "affiliatedemo" )
    private int affiliateDemo;

    @Column( name = "directpurchaseamount" )
    private float directPurchaseAmount;

    @Column( name = "note" )
    private String note;
    
    /*
     0 means no processing needed at this time.
     1 means processing needed for lead generation.
    */
    @Column( name = "creditzerostatustypeid" )
    private int creditZeroStatusTypeId;
    
    /*
     * this is the date that this credit record was last reduced to 0. 
    */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="creditzerodate")
    private Date creditZeroDate;
    

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="expiredate")
    private Date expireDate;

    @Transient
    private Locale locale;

    public int compareTo( Credit c )
    {
        if( createDate != null && c.getCreateDate() != null )
            return createDate.compareTo( c.getCreateDate() );

        return new Long( creditId ).compareTo( new Long( c.getCreditId() ) );
    }

    public void appendNote( String msg )
    {
        if( msg == null || msg.trim().length() == 0 )
            return;

        if( note == null )
            note = "";

        if( note.length() > 0 )
            note += "\n.................\n";

        note += msg;
    }



    public CreditStatusType getCreditStatusType()
    {
        return CreditStatusType.getValue( creditStatusTypeId );
    }

    @Override
    public String toString()
    {
        return "Credit " + creditId + ", userId=" + userId + ", orderId=" + orderId + ", status=" + creditStatusTypeId + ", initial=" + initialCount + ", used=" + usedCount + ", remaining=" + remainingCount + ", created=" +  ( createDate == null ? "null" : createDate.toString() ) + ", expires=" + ( expireDate == null ? "null" : expireDate.toString() );
    }

    public long getCreditId()
    {
        return creditId;
    }

    public void setCreditId( long creditId )
    {
        this.creditId = creditId;
    }

    public long getOrderId()
    {
        return orderId;
    }

    public void setOrderId( long orderId )
    {
        this.orderId = orderId;
    }

    public long getUserId()
    {
        return userId;
    }

    public void setUserId( long userId )
    {
        this.userId = userId;
    }

    public int getCreditStatusTypeId()
    {
        return creditStatusTypeId;
    }

    public void setCreditStatusTypeId( int creditStatusTypeId )
    {
        this.creditStatusTypeId = creditStatusTypeId;
    }

    public int getInitialCount()
    {
        return initialCount;
    }

    public void setInitialCount( int initialCount )
    {
        this.initialCount = initialCount;
    }

    public int getUsedCount()
    {
        return usedCount;
    }

    public void setUsedCount( int usedCount )
    {
        this.usedCount = usedCount;
    }

    public int getRemainingCount()
    {
        return remainingCount;
    }

    public void setRemainingCount( int remainingCount )
    {
        this.remainingCount = remainingCount;
    }

    public Date getCreateDate()
    {
        return createDate;
    }

    public void setCreateDate( Date createDate )
    {
        this.createDate = createDate;
    }

    public Date getExpireDate()
    {
        return expireDate;
    }

    public void setExpireDate( Date expireDate )
    {
        this.expireDate = expireDate;
    }



    public String getNote()
    {
        return note;
    }



    public void setNote( String note )
    {
        if( note != null && note.trim().length() == 0 )
            note = null;

        this.note = note;
    }



    public long getOrderItemId()
    {
        return orderItemId;
    }



    public void setOrderItemId( long orderItemId )
    {
        this.orderItemId = orderItemId;
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

    public int getCreditSourceTypeId() {
        return creditSourceTypeId;
    }

    public void setCreditSourceTypeId(int creditSourceTypeId) {
        this.creditSourceTypeId = creditSourceTypeId;
    }

    public int getAffiliateDemo() {
        return affiliateDemo;
    }

    public void setAffiliateDemo(int affiliateDemo) {
        this.affiliateDemo = affiliateDemo;
    }

    public float getDirectPurchaseAmount() {
        return directPurchaseAmount;
    }

    public void setDirectPurchaseAmount(float directPurchaseAmount) {
        this.directPurchaseAmount = directPurchaseAmount;
    }

    public int getCreditTypeId() {
        return creditTypeId;
    }

    public void setCreditTypeId(int creditTypeId) {
        this.creditTypeId = creditTypeId;
    }

    public int getOverageCount() {
        return overageCount;
    }

    public void setOverageCount(int overageCount) {
        this.overageCount = overageCount;
    }

    public int getCreditZeroStatusTypeId() {
        return creditZeroStatusTypeId;
    }

    public void setCreditZeroStatusTypeId(int creditZeroStatusTypeId) {
        this.creditZeroStatusTypeId = creditZeroStatusTypeId;
    }

    public Date getCreditZeroDate() {
        return creditZeroDate;
    }

    public void setCreditZeroDate(Date creditZeroDate) {
        this.creditZeroDate = creditZeroDate;
    }



}
