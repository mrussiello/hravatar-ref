/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.entity.report;

import com.tm2ref.report.ReportTemplateType;
import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.Basic;
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
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Mike
 */
@Cacheable
@Entity
@Table( name = "report" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="Report.findByReportId", query="SELECT o FROM Report AS o WHERE o.reportId=:reportId" )
})
public class Report implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="reportid")
    private long reportId;

    @Column(name="reportstatustypeid")
    private int reportStatusTypeId=0;

    @Column(name="reporttemplatetypeid")
    private int reportTemplateTypeId=0;

    @Column(name="reportpurposetypeid")
    private int reportPurposeTypeId;

    @Column(name="implementationclass")
    private String implementationClass;

    @Column(name="emailformatterclass")
    private String emailFormatterClass;

    @Column(name="testtakeremailformatterclass")
    private String testtakerEmailFormatterClass;

    @Column(name="title")
    private String title;

    @Column(name="name")
    private String name;

    @Column(name="orgid")
    private long orgId;

    @Column(name="suborgid")
    private long suborgId;

    @Column(name="nopdfdoc")
    private int noPdfDoc = 0;

    @Column(name="emailtesttaker")
    private int emailTestTaker = 0;

    @Column( name = "nameenglish" )
    private String nameEnglish;

    @Column(name="includescoretextinfo")
    private int includeScoreText = 1;


    
    @Column(name="includenumericscores")
    private int includeNumericScores = 1;

    @Column(name="includeoverallscore")
    private int includeOverallScore = 1;

    @Column(name="includeoverviewtext")
    private int includeOverviewText = 1;

    @Column(name="includecompetencyscores")
    private int includeCompetencyScores = 1;

    @Column(name="includeitemscores")
    private int includeItemScores = 0;


    /**
     * packed string ruleid1|value1|ruleid2|value2;
     */
    @Column(name="reportflags")
    private String reportFlags;


    @Column(name="intparam1")
    private int intParam1;

    @Column(name="intparam2")
    private int intParam2;

    @Column(name="intparam3")
    private int intParam3;
    
    /**
     * English Equivalent Report Id
     */
    @Column(name="longparam1")
    private long longParam1;

    

    @Column(name="floatparam1")
    private float floatParam1;

    @Column(name="floatparam2")
    private float floatParam2;

    @Column(name="floatparam3")
    private float floatParam3;

    /**
     * CT2 Report - Custom Key for Detail Text on cover page.
     */
    @Column(name="strparam1")
    private String strParam1;

    /**
     * CT2 Report - Custom Key for Report Title
     */
    @Column(name="strparam2")
    private String strParam2;

    /**
     * CT2 Report - Custom Report Title (overrides key and default title for report).
     */
    @Column(name="strparam3")
    private String strParam3;

    @Column(name="strparam4")
    private String strParam4;

    @Column(name="strparam5")
    private String strParam5;

    @Column(name="strparam6")
    private String strParam6;
    
    /**
     * CT2 Report - Custom Detail Text on cover page (overrides key and default text).
     */
    @Column(name="textparam1")
    private String textParam1;

    /**
     * CT2 Development Report Text
     * 
     */
    @Column(name="textparam2")
    private String textParam2;

    @Column(name="textparam3")
    private String textParam3;


    @Column(name="localestr")
    private String localeStr;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Transient
    private Locale localeForReportGen = null;



    public List<NVPair> getReportFlagList()
    {
        return StringUtils.parseNVPairsList( reportFlags, "|" );        
    }


    public ReportTemplateType getReportTemplateType()
    {
        return ReportTemplateType.getValue( reportTemplateTypeId );
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone(); //To change body of generated methods, choose Tools | Templates.
    }

    
    
    @Override
    public String toString() {
        return "Report[ id=" + reportId + ", name: "  + name + "]";
    }

    public long getReportId() {
        return reportId;
    }

    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    public int getReportStatusTypeId() {
        return reportStatusTypeId;
    }

    public void setReportStatusTypeId(int reportStatusTypeId) {
        this.reportStatusTypeId = reportStatusTypeId;
    }

    public int getReportTemplateTypeId() {
        return reportTemplateTypeId;
    }

    public void setReportTemplateTypeId(int reportTemplateTypeId) {
        this.reportTemplateTypeId = reportTemplateTypeId;
    }

    public int getReportPurposeTypeId() {
        return reportPurposeTypeId;
    }

    public void setReportPurposeTypeId(int reportPurposeTypeId) {
        this.reportPurposeTypeId = reportPurposeTypeId;
    }

    public String getImplementationClass() {
        return implementationClass;
    }

    public void setImplementationClass(String implementationClass) {
        this.implementationClass = implementationClass;
    }

    public String getEmailFormatterClass() {
        return emailFormatterClass;
    }

    public void setEmailFormatterClass(String emailFormatterClass) {
        this.emailFormatterClass = emailFormatterClass;
    }

    public String getTesttakerEmailFormatterClass() {
        return testtakerEmailFormatterClass;
    }

    public void setTesttakerEmailFormatterClass(String testtakerEmailFormatterClass) {
        this.testtakerEmailFormatterClass = testtakerEmailFormatterClass;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getOrgId() {
        return orgId;
    }

    public void setOrgId(long orgId) {
        this.orgId = orgId;
    }

    public long getSuborgId() {
        return suborgId;
    }

    public void setSuborgId(long suborgId) {
        this.suborgId = suborgId;
    }

    public int getNoPdfDoc() {
        return noPdfDoc;
    }

    public void setNoPdfDoc(int noPdfDoc) {
        this.noPdfDoc = noPdfDoc;
    }

    public int getEmailTestTaker() {
        return emailTestTaker;
    }

    public void setEmailTestTaker(int emailTestTaker) {
        this.emailTestTaker = emailTestTaker;
    }

    public String getNameEnglish() {
        return nameEnglish;
    }

    public void setNameEnglish(String nameEnglish) {
        this.nameEnglish = nameEnglish;
    }

    public int getIncludeNumericScores() {
        return includeNumericScores;
    }

    public void setIncludeNumericScores(int includeNumericScores) {
        this.includeNumericScores = includeNumericScores;
    }

    public int getIncludeOverallScore() {
        return includeOverallScore;
    }

    public void setIncludeOverallScore(int includeOverallScore) {
        this.includeOverallScore = includeOverallScore;
    }

    public int getIncludeOverviewText() {
        return includeOverviewText;
    }

    public void setIncludeOverviewText(int includeOverviewText) {
        this.includeOverviewText = includeOverviewText;
    }

    public int getIncludeCompetencyScores() {
        return includeCompetencyScores;
    }

    public void setIncludeCompetencyScores(int includeCompetencyScores) {
        this.includeCompetencyScores = includeCompetencyScores;
    }

    public int getIncludeItemScores() {
        return includeItemScores;
    }

    public void setIncludeItemScores(int includeItemScores) {
        this.includeItemScores = includeItemScores;
    }

    public String getReportFlags() {
        return reportFlags;
    }

    public void setReportFlags(String reportFlags) {
        this.reportFlags = reportFlags;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public int getIntParam3() {
        return intParam3;
    }

    public void setIntParam3(int intParam3) {
        this.intParam3 = intParam3;
    }

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public float getFloatParam1() {
        return floatParam1;
    }

    public void setFloatParam1(float floatParam1) {
        this.floatParam1 = floatParam1;
    }

    public float getFloatParam2() {
        return floatParam2;
    }

    public void setFloatParam2(float floatParam2) {
        this.floatParam2 = floatParam2;
    }

    public float getFloatParam3() {
        return floatParam3;
    }

    public void setFloatParam3(float floatParam3) {
        this.floatParam3 = floatParam3;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public String getStrParam3() {
        return strParam3;
    }

    public void setStrParam3(String strParam3) {
        this.strParam3 = strParam3;
    }

    public String getStrParam4() {
        return strParam4;
    }

    public void setStrParam4(String strParam4) {
        this.strParam4 = strParam4;
    }

    public String getStrParam5() {
        return strParam5;
    }

    public void setStrParam5(String strParam5) {
        this.strParam5 = strParam5;
    }

    public String getStrParam6() {
        return strParam6;
    }

    public void setStrParam6(String strParam6) {
        this.strParam6 = strParam6;
    }

    public String getTextParam1() {
        return textParam1;
    }

    public void setTextParam1(String textParam1) {
        this.textParam1 = textParam1;
    }

    public String getTextParam2() {
        return textParam2;
    }

    public void setTextParam2(String textParam2) {
        this.textParam2 = textParam2;
    }

    public String getTextParam3() {
        return textParam3;
    }

    public void setTextParam3(String textParam3) {
        this.textParam3 = textParam3;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Locale getLocaleForReportGen() {
        return localeForReportGen;
    }

    public void setLocaleForReportGen(Locale localeForReportGen) {
        this.localeForReportGen = localeForReportGen;
    }

    public int getIncludeScoreText() {
        return includeScoreText;
    }

    public void setIncludeScoreText(int includeScoreText) {
        this.includeScoreText = includeScoreText;
    }


}
