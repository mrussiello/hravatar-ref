package com.tm2ref.entity.event;

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
@Table( name = "testeventarchive" )
@NamedQueries( {
        @NamedQuery( name = "TestEventArchive.findByTestEventId", query = "SELECT o FROM TestEventArchive AS o WHERE o.testEventId=:testEventId" ),
        @NamedQuery( name = "TestEventArchive.findByTestEventArchiveId", query = "SELECT o FROM TestEventArchive AS o WHERE o.testEventArchiveId=:testEventArchiveId" )
} )
public class TestEventArchive implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testeventarchiveid" )
    private long testEventArchiveId;

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

    public TestEvent getTestEvent() throws Exception
    {
        TestEvent te = new TestEvent();

        te.setLastAccessDate(lastAccessDate);
        te.setOverallScore(overallScore);
        te.setProductId(productId);
        te.setTestEventArchiveId( testEventArchiveId );
        te.setTestEventId(testEventId);
        te.setTestEventStatusTypeId(testEventStatusTypeId);
        te.setTestKeyId(testKeyId);
        te.setUserId(userId);
        te.setExtRef(extRef);
        return te;
    }
    
    
    
    public long getTestEventArchiveId() {
        return testEventArchiveId;
    }

    public void setTestEventArchiveId(long testEventArchiveId) {
        this.testEventArchiveId = testEventArchiveId;
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

    
    
    
}
