package com.tm2ref.entity.user;

import java.io.Serializable;


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
@Table( name = "orgautotest" )
@XmlRootElement
@NamedQueries({
    @NamedQuery ( name="OrgAutoTest.findByOrgAutoTestId", query="SELECT o FROM OrgAutoTest AS o  WHERE o.orgAutoTestId = :orgAutoTestId" )
})

public class OrgAutoTest implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="orgautotestid")
    private int orgAutoTestId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="suborgid")
    private int suborgId;

    @Column( name = "productid" )
    private int productId;

    /**
     * 0=Test Plus
     * 1 = Reference Check ONLY
     */
    @Column(name="orgautotesttypeid")
    private int orgAutoTestTypeId;
    
    
    @Column(name="authuserid")
    private long authUserId = 0;

    @Column( name = "corpid" )
    private int corpId = 0;

    @Column(name="lang")
    private String lang;

    @Column(name="langreport")
    private String langReport;

    @Column(name="countrycode")
    private String countryCode;
    
    @Column( name = "suppresssendaccesskey" )
    private int suppressSendAccessKey;
    
    @Column(name="emailresultsto")
    private String emailResultsTo;

    @Column(name="textresultsto")
    private String textResultsTo;
    
    @Column( name = "rcscriptid" )
    private int rcScriptId;
        

    @Override
    public String toString() {
        return "OrgAutoTest{" + "orgAutoTestId=" + orgAutoTestId + ", orgId=" + orgId + ", suborgId=" + suborgId + ", productId=" + productId + '}';
    }

    public int getOrgAutoTestId() {
        return orgAutoTestId;
    }

    public void setOrgAutoTestId(int orgAutoTestId) {
        this.orgAutoTestId = orgAutoTestId;
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

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getOrgAutoTestTypeId() {
        return orgAutoTestTypeId;
    }

    public void setOrgAutoTestTypeId(int orgAutoTestTypeId) {
        this.orgAutoTestTypeId = orgAutoTestTypeId;
    }

    public long getAuthUserId() {
        return authUserId;
    }

    public void setAuthUserId(long authUserId) {
        this.authUserId = authUserId;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLangReport() {
        return langReport;
    }

    public void setLangReport(String langReport) {
        this.langReport = langReport;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public int getSuppressSendAccessKey() {
        return suppressSendAccessKey;
    }

    public void setSuppressSendAccessKey(int suppressSendAccessKey) {
        this.suppressSendAccessKey = suppressSendAccessKey;
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

    public int getRcScriptId() {
        return rcScriptId;
    }

    public void setRcScriptId(int rcScriptId) {
        this.rcScriptId = rcScriptId;
    }



}
