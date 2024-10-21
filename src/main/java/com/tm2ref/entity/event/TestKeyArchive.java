package com.tm2ref.entity.event;

import java.io.Serializable;

import java.util.Date;
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
@Table( name = "testkeyarchive" )
@NamedQueries( {
        @NamedQuery( name = "TestKeyArchive.findByTestKeyId", query = "SELECT o FROM TestKeyArchive AS o WHERE o.testKeyId=:testKeyId" )
} )
public class TestKeyArchive implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testkeyarchiveid" )
    private long testKeyArchiveId;

    @Column( name = "testkeyid" )
    private long testKeyId;

    
    @Column( name = "statustypeid" )
    private int testKeyStatusTypeId;

    @Column(name="testkeysourcetypeid")
    private int testKeySourceTypeId;
    
    @Column( name = "creditid" )
    private long creditId;

    @Column( name = "orgid" )
    private int orgId;

    @Column( name = "suborgid" )
    private int suborgId;

    @Column( name = "userid" )
    private long userId = 0;

    @Column( name = "productid" )
    private int productId;

    @Column( name = "producttypeid" )
    private int productTypeId;

    
    @Column( name = "authorizinguserid" )
    private long authorizingUserId = 0;

    @Column( name = "lang" )
    private String localeStr;

    @Column(name="extref")
    private String extRef;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    @Column(name="returnurl")
    private String returnUrl;

    @Column(name="resultposturl")
    private String resultPostUrl;

    @Column(name="apitypeid")
    private int apiTypeId;
    
    @Column(name="resultposttypeid")
    private int resultPostTypeId;
    
    @Column(name="customparameters")
    private String customParameters;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastaccessdate")
    private Date lastAccessDate;



    public TestKey getTestKey()
    {
        TestKey tk = new TestKey();
        tk.setTestKeyArchiveId(testKeyArchiveId);
        tk.setTestKeyId(testKeyId);
        tk.setAuthorizingUserId(authorizingUserId);
        tk.setTestKeyStatusTypeId(testKeyStatusTypeId);
        tk.setLastAccessDate(lastAccessDate);
        tk.setLocaleStr(localeStr);
        tk.setOrgId(orgId);
        tk.setCreditId(creditId);
        tk.setSuborgId(suborgId);
        tk.setUserId(userId);
        tk.setExtRef(extRef);
        tk.setEmailResultsTo(emailResultsTo);
        tk.setTextResultsTo(textResultsTo);
        tk.setReturnUrl(returnUrl);
        tk.setCustomParameters(customParameters);
        tk.setTestKeySourceTypeId(testKeySourceTypeId);
        tk.setApiTypeId(apiTypeId);
        tk.setResultPostTypeId(resultPostTypeId);
        tk.setResultPostUrl(resultPostUrl);
        tk.setProductId( productId);
        tk.setProductTypeId(productTypeId);
        tk.setTestKeyArchive( this );
        return tk;
    }

    public long getTestKeyArchiveId() {
        return testKeyArchiveId;
    }

    public void setTestKeyArchiveId(long testKeyArchiveId) {
        this.testKeyArchiveId = testKeyArchiveId;
    }

    public long getTestKeyId() {
        return testKeyId;
    }

    public void setTestKeyId(long testKeyId) {
        this.testKeyId = testKeyId;
    }

    public int getTestKeyStatusTypeId() {
        return testKeyStatusTypeId;
    }

    public void setTestKeyStatusTypeId(int testKeyStatusTypeId) {
        this.testKeyStatusTypeId = testKeyStatusTypeId;
    }

    public long getCreditId() {
        return creditId;
    }

    public void setCreditId(long creditId) {
        this.creditId = creditId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(int suborgId) {
        this.suborgId = suborgId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getAuthorizingUserId() {
        return authorizingUserId;
    }

    public void setAuthorizingUserId(long authorizingUserId) {
        this.authorizingUserId = authorizingUserId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public String getExtRef() {
        return extRef;
    }

    public void setExtRef(String extRef) {
        this.extRef = extRef;
    }

    public String getEmailResultsTo() {
        return emailResultsTo;
    }

    public void setEmailResultsTo(String emailResultsTo) {
        this.emailResultsTo = emailResultsTo;
    }

    public String getTextResultsTo() {
        return textResultsTo;
    }

    public void setTextResultsTo(String textResultsTo) {
        this.textResultsTo = textResultsTo;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(String customParameters) {
        this.customParameters = customParameters;
    }

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public int getTestKeySourceTypeId() {
        return testKeySourceTypeId;
    }

    public void setTestKeySourceTypeId(int testKeySourceTypeId) {
        this.testKeySourceTypeId = testKeySourceTypeId;
    }

    public String getResultPostUrl() {
        return resultPostUrl;
    }

    public void setResultPostUrl(String resultPostUrl) {
        this.resultPostUrl = resultPostUrl;
    }

    public int getResultPostTypeId() {
        return resultPostTypeId;
    }

    public void setResultPostTypeId(int resultPostTypeId) {
        this.resultPostTypeId = resultPostTypeId;
    }

    public int getApiTypeId() {
        return apiTypeId;
    }

    public void setApiTypeId(int apiTypeId) {
        this.apiTypeId = apiTypeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getProductTypeId() {
        return productTypeId;
    }

    public void setProductTypeId(int productTypeId) {
        this.productTypeId = productTypeId;
    }


}
