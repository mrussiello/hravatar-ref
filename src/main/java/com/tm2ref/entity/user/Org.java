package com.tm2ref.entity.user;

import com.tm2ref.report.ReportUtils;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.user.CandidateImageViewType;
import com.tm2ref.user.OrgCreditUsageType;
import com.tm2ref.user.OrgStatusType;
import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;


@Entity
@Table( name = "org" )
@XmlRootElement
@NamedQueries({
@NamedQuery ( name="Org.findByOrgId", query="SELECT o FROM Org AS o WHERE o.orgId=:orgId" ),
@NamedQuery ( name="Org.findByAffiliateId", query="SELECT o FROM Org AS o WHERE o.affiliateId=:affiliateId" ),
@NamedQuery ( name="Org.findByAffiliateIdAndExtRef", query="SELECT o FROM Org AS o WHERE o.affiliateId=:affiliateId AND o.affiliateExtRef=:affiliateExtRef" ),
@NamedQuery ( name="Org.findByAffiliateIdAndAffiliateAccountTypeId", query="SELECT o FROM Org AS o WHERE o.affiliateId=:affiliateId AND o.affiliateAccountTypeId=:affiliateAccountTypeId" )

})
public class Org implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="orgid")
    private int orgId;

    @Column(name="name")
    private String name;

    @Column(name="orgstatustypeid")
    private int orgStatusTypeId = 0;

    @Column(name="adminuserid")
    private long adminUserId = 0;

    @Column( name = "defaultcorpid" )
    private int defaultCorpId = 0;

    @Column( name = "defaulttesttakerlang" )
    private String defaultTestTakerLang;
    
    @Column( name = "supportsendemail" )
    private String supportSendEmail;
        
    //@Column( name = "defaultreportlang" )
    //private String defaultReportLang;

    @Column( name = "orgidtouseforcredits" )
    private int orgIdToUseForCredits = 0;


    @Column( name = "orgcreditusagetypeid" )
    private int orgCreditUsageTypeId = 0;

    @Column( name = "candidateimageviewtypeid" )
    private int candidateImageViewTypeId;
    
    @Column( name = "candidateaudiovideoviewtypeid" )
    private int candidateAudioVideoViewTypeId;
    
    
    //@Column( name = "orgcreditusagemaxevents" )
   // private int orgCreditUsageMaxEvents = 0;

    //@Column( name = "orgcreditusageeventcount" )
    //private int orgCreditUsageEventCount = 0;

    //@Column( name = "onlineproctoringcredittypeid" )
    //private int onlineProctoringCreditTypeId = 0;
    
    //@Column( name = "suspiciousactivitythresholdtypeid" )
    //private int suspiciousActivityThresholdTypeId = 0;
    
    
    //@Column( name = "useranonymitytypeid" )
    //private int userAnonymityTypeId = 0;

   // @Column( name = "pseudonymizationperiodtypeid" )
    //private int pseudonymizationPeriodTypeId;
    
    //@Column( name = "dataretentionperiodtypeid" )
    //private int dataRetentionPeriodTypeId;
    
    //@Column(name="autoadvancebatterytests")
    //private int autoAdvanceBatteryTests = 0;
    
    //@Column(name="includescorecalcinfoinreports")
    //private int includeScoreCalcInfoInReports = 1;
    
    //@Column( name = "reportdownloadtypeid" )
    //private int reportDownloadTypeId;
    
    @Column(name="reportlogourl")
    private String reportLogoUrl;


    
    
    @Column( name = "affiliateextref" )
    private String affiliateExtRef;

    @Column( name = "affiliateid" )
    private String affiliateId;

    @Column( name = "affiliateaccounttypeid" )
    private int affiliateAccountTypeId = 0;

    @Column( name = "affiliatedemoacct" )
    private int affiliateDemoAcct;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="orgcreditusageenddate")
    private Date orgCreditUsageEndDate;


    @Column( name = "defaultcorpexiturl" )
    private String defaultCorpExitUrl;

    @Column( name = "defaultcorperrorurl" )
    private String defaultCorpErrorUrl;


    /**
     * 0 = none
     * 1 = invitations only
     * 2 = invitations and reminders only
     * 10 = all (invitations, reminders, reports, etc).
     */   
    @Column(name="cconcandemails")
    private int ccOnCandEmails;



    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;

    
    
    /**
     * packed string ruleid1|value1|ruleid2|value2;
     */
    @Column(name="reportflags")
    private String reportFlags;



    
    
    @Column(name="hqcountry")
    private String hqCountry;


    @Transient
    private User adminUser;

    @Override
    public boolean equals( Object o )
    {
        if( o instanceof Org )
        {
            Org u = (Org) o;

            return orgId == u.getOrgId();
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + (int) (this.orgId ^ (this.orgId >>> 32));
        return hash;
    }


    
    public boolean getUseInitiatorNameInEmails()
    {
        return ReportUtils.getReportFlagBooleanValue("initiatornameemail", null, this, null );
    }


    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public OrgStatusType getOrgStatusType()
    {
        return OrgStatusType.getValue( this.orgStatusTypeId );
    }

    public OrgCreditUsageType getOrgCreditUsageType()
    {
        return OrgCreditUsageType.getValue( this.orgCreditUsageTypeId );
    }

    @Override
    public String toString() {
        return "Org{" + "orgId=" + orgId + ", name=" + name;
    }
    
    
    public List<NVPair> getReportFlagList(Suborg suborg, com.tm2ref.entity.report.Report report )
    {
        List<NVPair> out = new ArrayList<>();
        
        List<NVPair> l2;
              
        // Start with Report - lowest level
        if( report != null )
            out = report.getReportFlagList();
        
        // LogService.logIt( "Org.getReportFlagList() AAA.1 out has " + out.size() + " report flags." );        
        
            // Next level is Org
        if( reportFlags !=null && !reportFlags.isEmpty() )
        {
            l2 = getReportFlagList();
            
            for( NVPair pr : out )
            {
                // Only add from out if not in l2
                if( !hasNvPair( pr, l2 ) )
                {
                    l2.add( pr );
                }
            }
            
            out=l2;
        }

        // LogService.logIt( "Org.getReportFlagList() AAA.2 out has " + out.size() + " report flags." );        
        
        // Highest level is Suborg list
        if( suborg != null )
        {
            l2 = suborg.getReportFlagList();            
            for( NVPair pr : out )
            {
                if( !hasNvPair( pr, l2 ) )
                {
                    l2.add( pr );
                }
            }
            out=l2;
        }

        // LogService.logIt( "Org.getReportFlagList() AAA.3 out has " + out.size() + " report flags." );        
        
        //for( NVPair pr : out )
        //{
        //    LogService.logIt( "Org.getReportFlagList() AAA.4 " + pr.getName() + "=" + pr.getValue() );                
        //}        
        
        return out;
    }

    public boolean getInternationalSmsOk()
    {
        if( reportFlags==null || reportFlags.isBlank() )
            return false;
        return reportFlags.contains( "|intlsmsok|1" ) || reportFlags.startsWith( "intlsmsok|1" );
    }
    
    public boolean getIsSmsOk()
    {
        if( reportFlags==null || reportFlags.isBlank() )
            return true;
        return !reportFlags.contains( "|allsmsoff|1" ) && !reportFlags.startsWith( "allsmsoff|1" );
    }
    
    
    
    
    public boolean getHasCustomSupportSendEmail()
    {
        return this.supportSendEmail!=null && !this.supportSendEmail.isBlank() && EmailUtils.validateEmailNoErrors(supportSendEmail);
    }
    
    public List<NVPair> getReportFlagList()
    {
        return StringUtils.parseNVPairsList( reportFlags, "|" );          
    }
    
    
    
    public boolean hasNvPair( NVPair nvp, List<NVPair> pl  )
    {
        if( nvp == null )
            return false;
        
        for( NVPair pr : pl )
        {
            if( pr.getName()!=null && pr.getName().equals( nvp.getName() ) )
                return true;
        }
        
        return false;
    }
    
    
    public CandidateImageViewType getCandidateImageViewType()
    {
        return CandidateImageViewType.getValue( candidateImageViewTypeId );
    }


    public User getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(User adminUser) {
        this.adminUser = adminUser;
    }

    public long getAdminUserId() {
        return adminUserId;
    }

    public void setAdminUserId(long adminUserId) {
        this.adminUserId = adminUserId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getOrgStatusTypeId() {
        return orgStatusTypeId;
    }

    public void setOrgStatusTypeId(int orgStatusTypeId) {
        this.orgStatusTypeId = orgStatusTypeId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getDefaultCorpId() {
        return defaultCorpId;
    }

    public void setDefaultCorpId(int defaultCorpId) {
        this.defaultCorpId = defaultCorpId;
    }

    public String getDefaultTestTakerLang() {
        return defaultTestTakerLang;
    }

    public void setDefaultTestTakerLang(String defaultTestTakerLang) {
        this.defaultTestTakerLang = defaultTestTakerLang;
    }

    public int getOrgIdToUseForCredits() {
        return orgIdToUseForCredits;
    }

    public void setOrgIdToUseForCredits(int orgIdToUseForCredits) {
        this.orgIdToUseForCredits = orgIdToUseForCredits;
    }

    public int getOrgCreditUsageTypeId() {
        return orgCreditUsageTypeId;
    }

    public void setOrgCreditUsageTypeId(int orgCreditUsageTypeId) {
        this.orgCreditUsageTypeId = orgCreditUsageTypeId;
    }

    public String getAffiliateExtRef() {
        return affiliateExtRef;
    }

    public void setAffiliateExtRef(String affiliateExtRef) {
        this.affiliateExtRef = affiliateExtRef;
    }

    public String getAffiliateId() {
        return affiliateId;
    }

    public void setAffiliateId(String affiliateId) {
        this.affiliateId = affiliateId;
    }

    public int getAffiliateAccountTypeId() {
        return affiliateAccountTypeId;
    }

    public void setAffiliateAccountTypeId(int affiliateAccountTypeId) {
        this.affiliateAccountTypeId = affiliateAccountTypeId;
    }

    public int getAffiliateDemoAcct() {
        return affiliateDemoAcct;
    }

    public void setAffiliateDemoAcct(int affiliateDemoAcct) {
        this.affiliateDemoAcct = affiliateDemoAcct;
    }

    public Date getOrgCreditUsageEndDate() {
        return orgCreditUsageEndDate;
    }

    public void setOrgCreditUsageEndDate(Date orgCreditUsageEndDate) {
        this.orgCreditUsageEndDate = orgCreditUsageEndDate;
    }

    public String getDefaultCorpExitUrlHttp() {
        if( defaultCorpExitUrl==null || defaultCorpExitUrl.isBlank() )
            return defaultCorpExitUrl;
        if( defaultCorpExitUrl.toLowerCase().trim().startsWith("http") )
            return defaultCorpExitUrl;
        return "http://" + defaultCorpExitUrl;
    }

    
    public String getDefaultCorpExitUrl() {
        return defaultCorpExitUrl;
    }

    public void setDefaultCorpExitUrl(String defaultCorpExitUrl) {
        this.defaultCorpExitUrl = defaultCorpExitUrl;
    }

    public String getDefaultCorpErrorUrl() {
        return defaultCorpErrorUrl;
    }

    public void setDefaultCorpErrorUrl(String defaultCorpErrorUrl) {
        this.defaultCorpErrorUrl = defaultCorpErrorUrl;
    }

    public String getReportFlags() {
        return reportFlags;
    }

    public void setReportFlags(String reportFlags) {
        this.reportFlags = reportFlags;
    }

    public String getHqCountry() {
        return hqCountry;
    }

    public void setHqCountry(String hqCountry) {
        this.hqCountry = hqCountry;
    }

    public String getReportLogoUrl() {
        return reportLogoUrl;
    }

    public void setReportLogoUrl(String reportLogoUrl) {
        this.reportLogoUrl = reportLogoUrl;
    }

    public int getCandidateImageViewTypeId() {
        return candidateImageViewTypeId;
    }

    public void setCandidateImageViewTypeId(int candidateImageViewTypeId) {
        this.candidateImageViewTypeId = candidateImageViewTypeId;
    }

    public int getCandidateAudioVideoViewTypeId() {
        return candidateAudioVideoViewTypeId;
    }

    public void setCandidateAudioVideoViewTypeId(int candidateAudioVideoViewTypeId) {
        this.candidateAudioVideoViewTypeId = candidateAudioVideoViewTypeId;
    }

    public String getSupportSendEmail() {
        return supportSendEmail;
    }

    public void setSupportSendEmail(String supportSendEmail) {
        this.supportSendEmail = supportSendEmail;
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

    public int getCcOnCandEmails() {
        return ccOnCandEmails;
    }

    public void setCcOnCandEmails(int ccOnCandEmails) {
        this.ccOnCandEmails = ccOnCandEmails;
    }


}
