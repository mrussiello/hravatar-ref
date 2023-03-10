/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcRater;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 *
 * @author miker_000
 */
@Named
@SessionScoped
public class CandidateRefBean  extends BaseRefBean implements Serializable {
    
    RcRater rcRater;
    RcRater rcRater2;
    
    int candidateInputNumber = 1;
    String candidateInputStr = null;
    Date lastObsStartDate = null;
    Date lastObsEndDate = null;
    boolean backToCore = false;
    
    
    public static CandidateRefBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (CandidateRefBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "candidateRefBean" );
    }
    
    public void clearBean()
    {
        this.rcRater = null;
        this.rcRater2=null;
        backToCore = false;
    }
    
    public int getCurrentYear()
    {
        return (new GregorianCalendar()).get(Calendar.YEAR);
    }
    
    
    public String getRcRaterIdEncrypted()
    {
        if( rcRater==null )
            return "";
        return rcRater.getRcRaterIdEncrypted();
    }

    public RcRater getRcRater() {
        return rcRater;
    }

    public void setRcRater(RcRater rcRater) {
        this.rcRater = rcRater;
    }

    public int getCandidateInputNumber() {
        return candidateInputNumber;
    }

    public void setCandidateInputNumber(int candidateInputNumber) {
        this.candidateInputNumber = candidateInputNumber;
    }

    public String getCandidateInputStr() {
        return candidateInputStr;
    }

    public void setCandidateInputStr(String candidateInputStr) {
        this.candidateInputStr = candidateInputStr;
    }

    public Date getLastObsStartDate() {
        return lastObsStartDate;
    }

    public void setLastObsStartDate(Date lastObsStartDate) {
        this.lastObsStartDate = lastObsStartDate;
    }

    public Date getLastObsEndDate() {
        return lastObsEndDate;
    }

    public void setLastObsEndDate(Date lastObsEndDate) {
        this.lastObsEndDate = lastObsEndDate;
    }

    public boolean isBackToCore() {
        return backToCore;
    }

    public void setBackToCore(boolean backToCore) {
        this.backToCore = backToCore;
    }

    public RcRater getRcRater2() {
        return rcRater2;
    }

    public void setRcRater2(RcRater rcRater2) {
        this.rcRater2 = rcRater2;
    }
    
    
    
    
}
