package com.tm2ref.entity.event;

import com.tm2ref.api.ApiType;
import com.tm2ref.event.TestKeyStatusType;
import com.tm2ref.util.JsonUtils;
import java.io.Serializable;
import java.util.Date;
import jakarta.json.JsonObject;
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
import java.util.HashMap;
import java.util.Map;


@Entity
@Table( name = "testkey" )
@NamedQueries( {
        @NamedQuery( name = "TestKey.findByTestKeyId", query = "SELECT o FROM TestKey AS o WHERE o.testKeyId=:testKeyId" ),
        @NamedQuery ( name="TestKey.findByOrgAndExtRef", query="SELECT o FROM TestKey AS o  WHERE o.orgId=:orgId AND o.extRef=:extRef" )
})
public class TestKey implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "testkeyid" )
    private long testKeyId;

    @Column( name = "statustypeid" )
    private int testKeyStatusTypeId;

    @Column(name="testkeysourcetypeid")
    private int testKeySourceTypeId;
    
    @Column( name = "creditid" )
    private long creditId;

    @Column( name = "creditindex" )
    private int creditIndex;
    
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

    
    @Transient
    private long testKeyArchiveId = 0;

    @Transient
    private TestKeyArchive testKeyArchive;
    

    public TestKeyArchive getTestKeyArchiveToSave()
    {
        if( testKeyArchive==null )
            return null;
        
        testKeyArchive.setTestKeyStatusTypeId(testKeyStatusTypeId);
        return testKeyArchive;
    }
    
    
    
    public Map<String,String> getBasicAuthParmsForResultsPost()
    {
        String s =  getCustomParameterValue( "basicAuthParmsForResultsPost" );

        if( s == null || s.trim().isEmpty() )
            return null;

        String delim = ";";

        if( !s.contains(";") && s.indexOf( ":" )>0 )
            delim = ":";

        String[] sa = s.split( delim );

        if( sa.length<2 )
            return null;

        String un = sa[0].trim();
        String pwd = sa[1].trim();

        if( un.isEmpty() || pwd.isEmpty() )
            return null;

        Map<String,String> out = new HashMap<>();

        out.put( "username", un );
        out.put( "password", pwd );

        return out;
    }

    public TestKeyStatusType getTestKeyStatusType()
    {
        return TestKeyStatusType.getValue( testKeyStatusTypeId );
    }

    
    public ApiType getApiType()
    {
        return ApiType.getValue( apiTypeId );
    }
    
    
    public int getRcScriptId()
    {
        return getIntCustomParameterValue( "rcscrpid" );
    }    
    public int getIntCustomParameterValue( String nm )
    {
        String s = getCustomParameterValue( nm );
        
        if( s == null )
            return 0;
        
        return Integer.parseInt(s);
    }
    public String getCustomParameterValue( String name )
    {
        if( getCustomParameters()==null || getCustomParameters().isEmpty() )
            return null;

        JsonObject jo = JsonUtils.getJsonObject( getCustomParameters() );

        return jo.getString( name, null );
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

    public long getTestKeyArchiveId() {
        return testKeyArchiveId;
    }

    public void setTestKeyArchiveId(long testKeyArchiveId) {
        this.testKeyArchiveId = testKeyArchiveId;
    }

    public TestKeyArchive getTestKeyArchive() {
        return testKeyArchive;
    }

    public void setTestKeyArchive(TestKeyArchive testKeyArchive) {
        this.testKeyArchive = testKeyArchive;
    }

 

    public Date getLastAccessDate() {
        return lastAccessDate;
    }

    public void setLastAccessDate(Date lastAccessDate) {
        this.lastAccessDate = lastAccessDate;
    }

    public int getCreditIndex() {
        return creditIndex;
    }

    public void setCreditIndex(int creditIndex) {
        this.creditIndex = creditIndex;
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
