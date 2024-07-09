/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.itextpdf.text.pdf.PdfWriter;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcScript;
import com.tm2ref.entity.report.Report;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import com.tm2ref.util.JsonUtils;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;
import java.awt.ComponentOrientation;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import jakarta.json.JsonObject;

/**
 *
 * @author miker_000
 */
public class ReportData {
    
     public static String hraLogoBlackTextFilename = null; // "hra-two-color-tagline-logo-trans-800.png";

     public static String hraLogoWhiteTextFilename = null; // "hra-white-tagline-logo-trans-800.png"; 

     public static String hraLogoBlackTextSmallFilename = null; // "hra-two-color-tagline-logo-trans-420.png"; 
     
     public static String hraLogoWhiteTextSmallFilename = null; // "hra-white-tagline-logo-trans-412.png"; 
    
    Locale reportLocale;
    TimeZone timeZone;
    
    RcCheck rc;
    RcScript rcs;
    Report r;
    public  User u;

    public Org o;

    public Suborg s;

    public List<NVPair> reportRules;

    
    public ReportData( RcCheck rc, Report r, Suborg s, Locale rl )
    {
        this.rc = rc;
        this.rcs = rc.getRcScript();
        this.r = r;
        this.u = rc.getUser();
        this.o = rc.getOrg();
        this.s = s;
        this.reportRules = o.getReportFlagList( s, r );
        this.reportLocale = rl;
        
        timeZone = rc.getAdminUser().getTimeZone();
        if( timeZone==null )
            timeZone = u.getTimeZone();
        if( timeZone == null )
            timeZone = TimeZone.getDefault();
        
        if( hraLogoBlackTextFilename==null  || hraLogoBlackTextFilename.isBlank() )
            init();
    }
    
    
    public static synchronized void init()
    {
        if( hraLogoBlackTextFilename!=null && !hraLogoBlackTextFilename.isBlank() )
            return;
        
        hraLogoBlackTextFilename = RuntimeConstants.getStringValue("hraLogoBlackTextFilename");
        hraLogoWhiteTextFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextFilename");
        hraLogoBlackTextSmallFilename = RuntimeConstants.getStringValue("hraLogoBlackTextSmallFilename");
        hraLogoWhiteTextSmallFilename = RuntimeConstants.getStringValue("hraLogoWhiteTextSmallFilename");    
    }
    
    
    
    public String getOrgName() {
        return o.getName();
    }

    
    public boolean getUsesNonAscii()
    {
        Locale l = getLocale();
        
        // Any right to left
        if( I18nUtils.isTextRTL( l ) )
            return true;
        
        // Check the product language
        if( rc!=null && rc.getLangCode()!=null && !rc.getLangCode().isEmpty() && I18nUtils.isTextRTL( I18nUtils.getLocaleFromCompositeStr(rc.getLangCode()) ) )
            return true;
            
        return false;
    }

    
    
    
    public String getReportCompanyName()
    {
        return getCustomParameterValue( "reportCompanyName" );
    }

    public String getReportCompanyImageUrl()
    {
        return getCustomParameterValue( "reportCompanyImageUrl" );
    }

    public String getReportCompanyAdminName()
    {
        return getCustomParameterValue( "reportCompanyAdminName" );
    }
    
    public String getCustomParameterValue( String name )
    {
        // LogService.logIt( "ReportData.getCustomParameterValue() " + (tk==null ? "tk is null" : tk.getCustomParameters() ) );

        if( rc == null )
            return null;

        if( rc.getCustomParameters()==null || rc.getCustomParameters().isEmpty() )
            return null;

        JsonObject jo = JsonUtils.getJsonObject( rc.getCustomParameters() );

        return jo.getString( name, null );
    }    
    
    
    
    
    public String getReportRuleAsString( String name )
    {
       return ReportUtils.getReportFlagStringValue( name, s, o, r );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public boolean getReportRuleAsBoolean( String name )
    {
       return ReportUtils.getReportFlagBooleanValue( name, s, o, r );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public int getReportRuleAsInt( String name )
    {
       // LogService.logIt( "ReportData.getReportRuleAsInt(" + name + ") tk: " + (tk==null) + ", sub: " + (this.getSuborg()==null) + ", org: " + (this.getOrg()==null) + ", r2u: " + this.getR()  );
       return ReportUtils.getReportFlagIntValue( name, s, o, r );
        
       // return getReportRuleAsInt( name ) == 1;
    }    
    
    public boolean getIsPrehire()
    {
        return rc==null || (rc.getRcCheckType().getIsPrehire());
    }
    public boolean getIsEmployee()
    {
        return rc!=null && rc.getRcCheckType().getIsEmployeeFeedback();
    }
    
    public boolean getIsLTR()
    {
        return ComponentOrientation.getOrientation( getLocale() ).isLeftToRight();
    }


    public int getTextRunDirection()
    {
        return getIsLTR() ? PdfWriter.RUN_DIRECTION_LTR : PdfWriter.RUN_DIRECTION_RTL;
    }

    public Locale getLocale()
    {
        if( r!=null && r.getLocaleForReportGen()!=null )
            return r.getLocaleForReportGen();

        if( rc!=null && rc.getLangCode()!= null && !rc.getLangCode().isBlank() )
            return I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );

        else if( r != null && r.getLocaleStr()!= null && !r.getLocaleStr().isEmpty() )
            return I18nUtils.getLocaleFromCompositeStr( r.getLocaleStr() );

        return Locale.US;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
    

    
    public String getStartDateFormatted()
    {
        return I18nUtils.getFormattedDate(getLocale() , rc.getCreateDate(), getTimeZone() );
    }


    public String getStartDateTimeFormatted()
    {
        return I18nUtils.getFormattedDateTime(getLocale() , rc.getCreateDate(), getTimeZone() );
    }


    public String getCompleteDateFormatted()
    {
        return I18nUtils.getFormattedDate(getLocale() , rc.getCompleteDate(), getTimeZone() );
    }

    public String getCompleteDateTimeFormatted()
    {
        return I18nUtils.getFormattedDateTime(getLocale(), rc.getCompleteDate(), getTimeZone() );
    }
    
    
    
    
    public boolean hasCustLogo()
    {
        return o != null && o.getReportLogoUrl() != null && !o.getReportLogoUrl().isEmpty(); //  custLogoFilename != null && !custLogoFilename.isEmpty();
    }


    public URL getCustLogoUrl()
    {
       try
       {
           return hasCustLogo() ? new URL( o.getReportLogoUrl() ) : null; // new URL( baseImageUrl + custLogoFilename );
       }

       catch( Exception e )
       {
           LogService.logIt(e, "getCustLogoUrl() " );

           return null;
       }
    }

    
    
    public URL getLocalImageUrl( String fn )
    {
       try
       {
           return new URL( getBaseImageUrl() + "/" + fn );
       }

       catch( MalformedURLException e )
       {
           LogService.logIt(e, "ReportData.getImageUrl() " );
           return null;
       }
    }

    public String getBaseImageUrl()
    {
        return RuntimeConstants.getStringValue( "ReportImagesBaseUrl" ) + "/images/coretest";
    }

    
    public String getReportName() {

        //String scriptName = rc.getRcScript() != null ? rc.getRcScript().getName() : null;
        
        // LogService.logIt( "ReportData.getReportName() r " + (r==null ? "is null" : " not null, title=" + r.getTitle() + ", str2=" + r.getStrParam2() ) );
        
        String ttl = "";
        
        if( r!=null && r.getStrParam3()!=null && !r.getStrParam3().isEmpty() )
            ttl = r.getStrParam3();
        
        else if( r!=null && r.getStrParam2()!=null && !r.getStrParam2().isEmpty() )
        {
            String key = r.getStrParam2(); // "g.TestResultsAndInterviewGuide";
        
            ttl = MessageFactory.getStringMessage(getLocale(), key, null );
            
            if( ttl ==null )
                ttl = r.getTitle();
        }
        
        else if( r.getTitle()!=null && !r.getTitle().isEmpty() )
            ttl = r.getTitle();            
        
        else
            ttl = r.getName();
        
        return StringUtils.replaceStr(ttl, "[SCRIPTNAME]" , getScriptName() );
    }

    public String getScriptName() 
    {

        return rc.getRcScript() != null ? rc.getRcScript().getName() : (rc.getJobTitle()==null ? "" : rc.getJobTitle());
    }

    public String getUserName() {

        if( u!=null && u.getUserType().getPseudo() )
            return MessageFactory.getStringMessage( getLocale(), "g.Pseudonymized", null );
                    
        return u.getFullname();
    }
    
    
    
    
    public RcCheck getRc() {
        return rc;
    }

    public void setRc(RcCheck rc) {
        this.rc = rc;
    }

    public RcScript getRcScript() {
        return rcs;
    }

    public void setRcScript(RcScript rcScript) {
        this.rcs = rcScript;
    }

    public Report getR() {
        return r;
    }

    public void setR(Report r) {
        this.r = r;
    }

    public User getU() {
        return u;
    }

    public void setU(User u) {
        this.u = u;
    }

    public Org getO() {
        return o;
    }

    public void setO(Org o) {
        this.o = o;
    }

    public Suborg getS() {
        return s;
    }

    public void setS(Suborg s) {
        this.s = s;
    }

    public List<NVPair> getReportRules() {
        return reportRules;
    }

    public void setReportRules(List<NVPair> reportRules) {
        this.reportRules = reportRules;
    }
    
    
    public URL getHRALogoBlackTextUrl()
    {
        return getLocalImageUrl( hraLogoBlackTextFilename );
    }
    

    public URL getHRALogoBlackTextSmallUrl()
    {
        return getLocalImageUrl( hraLogoBlackTextSmallFilename );
    }


    
    public URL getHRALogoWhiteTextSmallUrl()
    {
        return getLocalImageUrl( hraLogoWhiteTextSmallFilename );
    }
    
    public URL getHRALogoWhiteTextUrl()
    {
        return getLocalImageUrl( hraLogoWhiteTextFilename );
    }

    
    
    
}
