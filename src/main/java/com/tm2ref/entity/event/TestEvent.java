package com.tm2ref.entity.event;

import com.tm2ref.entity.purchase.Product;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.previousresult.PreviousResult;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.MessageFactory;
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
import java.util.Locale;


@Entity
@Table( name = "testevent" )
@NamedQueries( {
        @NamedQuery( name = "TestEvent.findByTestEventId", query = "SELECT o FROM TestEvent AS o WHERE o.testEventId=:testEventId ORDER BY o.testEventId DESC" )
} )
public class TestEvent implements Serializable, PreviousResult
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testeventid" )
    private long testEventId;

    @Column( name = "testeventstatustypeid" )
    private int testEventStatusTypeId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "productid" )
    private int productId;

    @Column( name = "userid" )
    private long userId = 0;

    @Column( name = "overallscore" )
    private float overallScore;

    @Column(name="extref")
    private String extRef;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastaccessdate")
    private Date lastAccessDate;
        
    @Transient
    private long testEventArchiveId;
    
    @Transient
    private Product product;

    @Transient
    private User user;
    
    @Transient
    Locale locale;
    

    @Override
    public String getPreviousResultTypeName()
    {
        if( this.locale==null )
            this.locale=Locale.US;
        return MessageFactory.getStringMessage(locale, "g.Assessment");
    }
    
    @Override
    public String getPreviousResultName()
    {
        if( product!=null )
            return product.getName();
        
        if( user!=null )
            return user.getLastName();
        
        return "";
    }
    
    @Override
    public long getPreviousResultId()
    {
        return testEventId;
    }
        
    @Override
    public Date getPreviousResultDate()
    {
        return this.lastAccessDate==null ? new Date() : lastAccessDate;
    }
    
    @Override
    public float getPreviousResultOverallScore()
    {
        return this.overallScore;
    }
    
    @Override
    public String getPreviousResultViewUrl()
    {
        try
        {
           return RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/r.xhtml?t=" + getTestEventIdEncrypted() + "&r=0&c=1";
        }

        catch( Exception e )
        {
            LogService.logIt( e, "TestEvent.getDirectLinkUrl()" );
        }

        return "";
    }
    
    
    public String getTestEventIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( testEventId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "TestEvent.getTestEventIdEncrypted() " + toString()  );

            return "";
        }
    }
    
    
    @Override
    public String toString() {
        return "TestEvent{" + "testEventId=" + testEventId  + '}';
    }
    public long getTestEventId() {
        return testEventId;
    }

    public void setTestEventId(long testEventId) {
        this.testEventId = testEventId;
    }

    public int getTestEventStatusTypeId() {
        return testEventStatusTypeId;
    }

    public void setTestEventStatusTypeId(int testEventStatusTypeId) {
        this.testEventStatusTypeId = testEventStatusTypeId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getUser() {
        return user;
    }

    @Override
    public void setUser(User user) {
        this.user = user;
    }

    public long getTestEventArchiveId() {
        return testEventArchiveId;
    }

    public void setTestEventArchiveId(long testEventArchiveId) {
        this.testEventArchiveId = testEventArchiveId;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }


}
