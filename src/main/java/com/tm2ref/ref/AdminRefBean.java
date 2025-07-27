/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.faces.FacesBean;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 *
 * @author miker_000
 */
@Named
@SessionScoped
public class AdminRefBean  extends FacesBean implements Serializable {
    
    String strParam1;
    long rcCheckId;
    long rcCheckId2;
    long rcCheckId3;
    long rcCheckId4;
    long rcCheckId5;
    long rcRaterId5;
    String langStr;
    String langStr2;
    String langStr4;
    
    String rcCheckIds;
    boolean forceAiRescoring;
    
    int reportId3;
    int reportId4;
    
    List<Object[]> pdfReportList;
    Object[] pdfReport;
    
    
    public static AdminRefBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (AdminRefBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "adminRefBean" );
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public long getRcCheckId2() {
        return rcCheckId2;
    }

    public void setRcCheckId2(long rcCheckId2) {
        this.rcCheckId2 = rcCheckId2;
    }

    public String getLangStr() {
        return langStr;
    }

    public void setLangStr(String langStr) {
        this.langStr = langStr;
    }

    public String getLangStr2() {
        return langStr2;
    }

    public void setLangStr2(String langStr2) {
        this.langStr2 = langStr2;
    }

    public long getRcCheckId3() {
        return rcCheckId3;
    }

    public void setRcCheckId3(long rcCheckId3) {
        this.rcCheckId3 = rcCheckId3;
    }

    public long getRcCheckId4() {
        return rcCheckId4;
    }

    public void setRcCheckId4(long rcCheckId4) {
        this.rcCheckId4 = rcCheckId4;
    }

    public String getLangStr4() {
        return langStr4;
    }

    public void setLangStr4(String langStr4) {
        this.langStr4 = langStr4;
    }

    public int getReportId3() {
        return reportId3;
    }

    public void setReportId3(int reportId3) {
        this.reportId3 = reportId3;
    }

    public int getReportId4() {
        return reportId4;
    }

    public void setReportId4(int reportId4) {
        this.reportId4 = reportId4;
    }
    
    public void addPdfReport( Object[] pdfReport )
    {
        if( pdfReportList==null )
            pdfReportList = new ArrayList<>();
        
        if( pdfReport==null || pdfReport.length<4 || pdfReport[1]==null )
            return;
        
        pdfReportList.add(0,  pdfReport );
        // pdfReportList.add( pdfReport );
    }

    public List<Object[]> getPdfReportList() {
        if( pdfReportList==null )
            pdfReportList = new ArrayList<>();
        
        return pdfReportList;
    }

    public void setPdfReportList(List<Object[]> pdfReportList) {
        this.pdfReportList = pdfReportList;
    }

    public Object[] getPdfReport() {
        return pdfReport;
    }

    public void setPdfReport(Object[] pdfReport) {
        this.pdfReport = pdfReport;
    }

    public long getRcCheckId5() {
        return rcCheckId5;
    }

    public void setRcCheckId5(long rcCheckId5) {
        this.rcCheckId5 = rcCheckId5;
    }

    public long getRcRaterId5() {
        return rcRaterId5;
    }

    public void setRcRaterId5(long rcRaterId5) {
        this.rcRaterId5 = rcRaterId5;
    }

    public String getRcCheckIds() {
        return rcCheckIds;
    }

    public void setRcCheckIds(String rcCheckIds) {
        this.rcCheckIds = rcCheckIds;
    }

    public boolean getForceAiRescoring()
    {
        return forceAiRescoring;
    }

    public void setForceAiRescoring(boolean forceAiRescoring)
    {
        this.forceAiRescoring = forceAiRescoring;
    }


    
    
}
