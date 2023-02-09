package com.tm2ref.entity.user;

import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
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
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;


@Entity
@Table( name = "suborg" )
@XmlRootElement
@NamedQueries({
@NamedQuery ( name="Suborg.findBySuborgId", query="SELECT o FROM Suborg AS o WHERE o.suborgId=:suborgId" )
})
public class Suborg implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="suborgid")
    private int suborgId;

    @Column(name="name")
    private String name;

    @Column(name="suborgstatustypeid")
    private int suborgStatusTypeId = 0;

    @Column(name="orgid")
    private int orgId = 0;

    @Column(name="adminuserid")
    private long adminUserId = 0;

    @Column( name = "defaultcorpid" )
    private int defaultCorpId = 0;

    @Column( name = "defaulttesttakerlang" )
    private String defaultTestTakerLang;

    @Column( name = "defaultreportlang" )
    private String defaultReportLang;

    @Column( name = "onlineproctoringtypeid" )
    private int onlineProctoringTypeId = -1;

    @Column( name = "suspiciousactivitythresholdtypeid" )
    private int suspiciousActivityThresholdTypeId = -1;

    @Column( name = "feedbackreportok" )
    private int feedbackReportOk = 0;
    
    @Column(name="defaulttestkeyexpiredays")
    private int defaultTestKeyExpireDays;


    
    @Column( name = "defaultcorpexiturl" )
    private String defaultCorpExitUrl;

    @Column(name="emailcandidateok")
    private int emailCandidateOk;

    @Column(name="emaillogomessageok")
    private int emailLogoMessageOk;

    @Column(name="emailonettasklistok")
    private int emailOnetTaskListOk;

    @Column(name="emailactivitylistok")
    private int emailActivityListOk;

    @Column(name="defaultmessagetext")
    private String defaultMessageText;

    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;


    @Column(name="customfieldvalues1")
    private String customFieldValues1;

    @Column(name="customfieldvalues2")
    private String customFieldValues2;

    @Column(name="customfieldvalues3")
    private String customFieldValues3;
    
    @Column(name="customcollectintest1")
    private int customCollectInTest1;

    @Column(name="customcollectintest2")
    private int customCollectInTest2;

    @Column(name="customcollectintest3")
    private int customCollectInTest3;
    

    /**
     * Substitutions are [APPLICANT] - name, default to 'Applicant'
     *                   [CANDIDATE] - name, default to Candidate
     *                   [EMPLOYEE] - name, default to Employee
     *                   [COMPANY] - company (Org) name
     *                   [DEPARTMENT] - department (Suborg) name
     *                   [TEST] - product name
     *                   [TESTKEY] - Test Key PIN
     *                   [URL] - full URL to enter assessment
     *
     *
     */
    @Column(name="testkeyemailsubj")
    private String testKeyEmailSubj;

    @Column(name="testkeyemailmsg")
    private String testKeyEmailMsg;

    @Column(name="testkeysmsmsg")
    private String testKeySmsMsg;

    /**
     * packed string ruleid1|value1|ruleid2|value2;
     */
    @Column(name="reportflags")
    private String reportFlags;

    
    @Override
    public boolean equals( Object o )
    {
        if( o instanceof Suborg )
        {
            Suborg u = (Suborg) o;

            return orgId == u.getOrgId() && u.getSuborgId()==suborgId;
        }

        return false;
    }


    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 71 * hash + (int) (this.suborgId ^ (this.suborgId >>> 32));
        return hash;
    }

    @Override
    public String toString() {
        return "Suborg{" + "suborgId=" + suborgId + ", name=" + name + ", suborgStatusTypeId=" + suborgStatusTypeId + ", orgId=" + orgId + '}';
    }
    
    public List<NVPair> getReportFlagList()
    {
        return StringUtils.parseNVPairsList( reportFlags, "|" );        
    }

    

    public int getSuborgId()
    {
        return suborgId;
    }

    public void setSuborgId(int suborgId)
    {
        this.suborgId = suborgId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getSuborgStatusTypeId()
    {
        return suborgStatusTypeId;
    }

    public void setSuborgStatusTypeId(int suborgStatusTypeId)
    {
        this.suborgStatusTypeId = suborgStatusTypeId;
    }

    public int getOrgId()
    {
        return orgId;
    }

    public void setOrgId(int orgId)
    {
        this.orgId = orgId;
    }

    public long getAdminUserId()
    {
        return adminUserId;
    }

    public void setAdminUserId(long adminUserId)
    {
        this.adminUserId = adminUserId;
    }

    public int getDefaultCorpId()
    {
        return defaultCorpId;
    }

    public void setDefaultCorpId(int defaultCorpId)
    {
        this.defaultCorpId = defaultCorpId;
    }

    public int getEmailCandidateOk() {
        return emailCandidateOk;
    }

    public void setEmailCandidateOk(int emailCandidateOk) {
        this.emailCandidateOk = emailCandidateOk;
    }

    public int getEmailLogoMessageOk() {
        return emailLogoMessageOk;
    }

    public void setEmailLogoMessageOk(int emailLogoMessageOk) {
        this.emailLogoMessageOk = emailLogoMessageOk;
    }

    public int getEmailOnetTaskListOk() {
        return emailOnetTaskListOk;
    }

    public void setEmailOnetTaskListOk(int emailOnetTaskListOk) {
        this.emailOnetTaskListOk = emailOnetTaskListOk;
    }

    public int getEmailActivityListOk() {
        return emailActivityListOk;
    }

    public void setEmailActivityListOk(int emailActivityListOk) {
        this.emailActivityListOk = emailActivityListOk;
    }

    public String getDefaultMessageText() {
        return defaultMessageText;
    }

    public void setDefaultMessageText(String defaultMessageText) {
        this.defaultMessageText = defaultMessageText;
    }

    public String getDefaultCorpExitUrl() {
        return defaultCorpExitUrl;
    }

    public void setDefaultCorpExitUrl(String defaultCorpExitUrl) {
        this.defaultCorpExitUrl = defaultCorpExitUrl;
    }

    public String getDefaultTestTakerLang() {
        return defaultTestTakerLang;
    }

    public void setDefaultTestTakerLang(String defaultTestTakerLang) {
        this.defaultTestTakerLang = defaultTestTakerLang;
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

    public String getDefaultReportLang() {
        return defaultReportLang;
    }

    public void setDefaultReportLang(String defaultReportLang) {
        this.defaultReportLang = defaultReportLang;
    }

    public String getCustomFieldValues1() {
        return customFieldValues1;
    }

    public void setCustomFieldValues1(String customFieldValues1) {
        this.customFieldValues1 = customFieldValues1;
    }

    public String getCustomFieldValues2() {
        return customFieldValues2;
    }

    public void setCustomFieldValues2(String customFieldValues2) {
        this.customFieldValues2 = customFieldValues2;
    }

    public String getCustomFieldValues3() {
        return customFieldValues3;
    }

    public void setCustomFieldValues3(String customFieldValues3) {
        this.customFieldValues3 = customFieldValues3;
    }

    public int getCustomCollectInTest1() {
        return customCollectInTest1;
    }

    public void setCustomCollectInTest1(int customCollectInTest1) {
        this.customCollectInTest1 = customCollectInTest1;
    }

    public int getCustomCollectInTest2() {
        return customCollectInTest2;
    }

    public void setCustomCollectInTest2(int customCollectInTest2) {
        this.customCollectInTest2 = customCollectInTest2;
    }

    public int getCustomCollectInTest3() {
        return customCollectInTest3;
    }

    public void setCustomCollectInTest3(int customCollectInTest3) {
        this.customCollectInTest3 = customCollectInTest3;
    }

    public int getOnlineProctoringTypeId() {
        return onlineProctoringTypeId;
    }

    public void setOnlineProctoringTypeId(int onlineProctoringTypeId) {
        this.onlineProctoringTypeId = onlineProctoringTypeId;
    }

    public int getFeedbackReportOk() {
        return feedbackReportOk;
    }

    public void setFeedbackReportOk(int feedbackReportOk) {
        this.feedbackReportOk = feedbackReportOk;
    }

    public String getTestKeyEmailSubj() {
        return testKeyEmailSubj;
    }

    public void setTestKeyEmailSubj(String testKeyEmailSubj) {
        this.testKeyEmailSubj = testKeyEmailSubj;
    }

    public String getTestKeyEmailMsg() {
        return testKeyEmailMsg;
    }

    public void setTestKeyEmailMsg(String testKeyEmailMsg) {
        this.testKeyEmailMsg = testKeyEmailMsg;
    }

    public String getTestKeySmsMsg() {
        return testKeySmsMsg;
    }

    public void setTestKeySmsMsg(String testKeySmsMsg) {
        this.testKeySmsMsg = testKeySmsMsg;
    }

    public int getDefaultTestKeyExpireDays() {
        return defaultTestKeyExpireDays;
    }

    public void setDefaultTestKeyExpireDays(int defaultTestKeyExpireDays) {
        this.defaultTestKeyExpireDays = defaultTestKeyExpireDays;
    }

    public int getSuspiciousActivityThresholdTypeId() {
        return suspiciousActivityThresholdTypeId;
    }

    public void setSuspiciousActivityThresholdTypeId(int suspiciousActivityThresholdTypeId) {
        this.suspiciousActivityThresholdTypeId = suspiciousActivityThresholdTypeId;
    }

    public String getReportFlags() {
        return reportFlags;
    }

    public void setReportFlags(String reportFlags) {
        this.reportFlags = reportFlags;
    }


}
