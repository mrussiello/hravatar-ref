/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref.results;

import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.report.Report;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.entity.ref.RcReferral;
import com.tm2ref.entity.ref.RcSuspiciousActivity;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.file.MediaTempUrlSourceType;
import com.tm2ref.global.Constants;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.RcCheckType;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcCompetencyWrapper;
import com.tm2ref.ref.RcCompetencyWrapperNameComparator;
import com.tm2ref.ref.RcContactPermissionType;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcItemWrapper;
import com.tm2ref.ref.RcMessageUtils;
import com.tm2ref.ref.RcRatingScaleType;
import com.tm2ref.ref.RcScriptFacade;
import com.tm2ref.ref.RcTopBottomSrcType;
import com.tm2ref.report.RcHistogram;
import com.tm2ref.report.RcHistogramRow;
import com.tm2ref.report.ReportFacade;
import com.tm2ref.report.ReportUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.NVPair;
import com.tm2ref.util.StringUtils;
import java.awt.ComponentOrientation;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;

/**
 *
 * @author miker_000
 */
public class BaseFormatter {
    
    static String interviewQStarUrl;
    static String AUDIOCOMMENT_ICON_CONV_URL;
    static String VIDEOCOMMENT_ICON_CONV_URL;
    static String AUDIOCOMMENT_ICON_URL;
    static String VIDEOCOMMENT_ICON_URL;
    
    RcCheck rc;
    RcFacade rcFacade;
    RcScriptFacade rcScriptFacade;
    UserFacade userFacade;
    
    Locale locale;
    TimeZone timeZone;
    Report report;
    public List<NVPair> reportRules = null;
    boolean incompleteOk = true;
    
    
    
    /**
        out[0] = adminUser.getFullname();
        out[1] = rc.getOrg().getName();
        out[2] = rc.getUser().getFullname();
        out[3] = rc.getJobTitle();
        out[4] = null; //rc.getCandidateStartUrl();
        out[5] = reminder ? MessageFactory.getStringMessage( locale, "g.REMINDERC" ) + " " : "";
        
        out[6] = getRcCheckTypeName( rc );
        out[7] = Float.toString( rc.getPercentComplete() );
        out[8] = Float.toString( rc.getOverallScore() );
        out[9] = rc.getResultsViewUrl();    
        * 
        RATER
        * out[15] = rater.getUser().getFullname();
          out[16] = rater.getRaterStartUrl();
          out[18] = Float.toString( rater.getOverallScore() );
          * 
        CUSTOM
        coreParams[20] = candidateName;
        coreParams[21] = raterName;
        coreParams[22] = ratersName;     * 
     */
    String[] coreParams;
    
    boolean prehire = true;
    String rcCheckTypeName;
    String candidateName;
    String raterName;
    String ratersName;
    User candidateUser;    
    User adminUser;
    Org org;

    public String rowStyleHdr = " style=\"background-color:#e6e6e6;vertical-align:top\"";
    //public String rowStyleHdrRed = " style=\"background-color:#ff0000;vertical-align:top;color:white\"";
    public String rowStyleSubHdr = " style=\"background-color:#AFD6E9;vertical-align:top\"";
    public String rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
    public String rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
    public String rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";
    public String rowStyle3 = " style=\"background-color:#eaeaea;vertical-align:top\"";
    public String rowStyle4 = " style=\"background-color:#f7f7f7;vertical-align:top\"";
    
    public String candidateRaterRowStype = " style=\"background-color:#C0FFC4;vertical-align:top\"";
    
    RcCheckUtils rcCheckUtils;
    int scrDigits = Constants.DEFAULT_SCORE_PRECISION_DIGITS;
    
    
    protected void init() throws Exception
    {
        if( interviewQStarUrl==null )
        {
            interviewQStarUrl = RuntimeConstants.getStringValue( "RcPdfInterQuesStarUrl" );
            AUDIOCOMMENT_ICON_CONV_URL = RuntimeConstants.getStringValue( "AUDIOCOMMENT_ICON_CONV_URL" );
            VIDEOCOMMENT_ICON_CONV_URL = RuntimeConstants.getStringValue( "VIDEOCOMMENT_ICON_CONV_URL" );
            AUDIOCOMMENT_ICON_URL = RuntimeConstants.getStringValue( "AUDIOCOMMENT_ICON_URL" );
            VIDEOCOMMENT_ICON_URL = RuntimeConstants.getStringValue( "VIDEOCOMMENT_ICON_URL" );            
        }
        
        
        
        if( rc==null )
            throw new Exception( "RcCheck is null" );
        
        if( !incompleteOk && !rc.getRcCheckStatusType().getIsComplete() )
            throw new Exception( "RcCheck is not complete." );
        
        if( rc.getAdminUser()== null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ) );            
        }        
        if( rc.getUser()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        }
        if( rc.getOrg()==null )
        {
            if( userFacade == null )
                userFacade = UserFacade.getInstance();
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        }
        

        prehire = rc.getRcCheckType().getIsPrehire();
        rcCheckTypeName = prehire ? RcCheckType.PREHIRE.getName(locale) : RcCheckType.EMPLOYEE_FBK.getName(locale);
        candidateName = lmsg( prehire ?  "g.Candidate" : "g.Employee" );
        raterName =  lmsg( prehire ?  "g.Reference" : "g.Reviewer" );
        ratersName =  lmsg( prehire ?  "g.References" : "g.Reviewers" );

        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();
        rcCheckUtils.loadRcCheckForScoringOrResults(rc);
        rcCheckUtils.prepSuspiciusActivityForReporting( rc, locale );
        
        
        if( report==null )
        {
            int reportId = rc.getReportId();
            if( reportId<=0 )
            {
                List<Integer> ridl = rcCheckUtils.getReportIdsForRcCheck(rc, locale==null ? rc.getLangCode() : locale.toString() );
                reportId = ridl.size()>=1 ? ridl.get(0) : 0;
            }
            report = ReportFacade.getInstance().getReport(reportId);
        }
        
        scrDigits = report!=null && report.getIntParam2() >= 0 ? report.getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;        
        
        candidateUser = rc.getUser();
        adminUser = rc.getAdminUser();
        org = rc.getOrg();

        reportRules = org.getReportFlagList(null, report );
        
        
        if( adminUser.getTimeZone()!=null )
            timeZone = adminUser.getTimeZone();
        else if( candidateUser.getTimeZone()!=null )
            timeZone = candidateUser.getTimeZone();
            
        if( timeZone==null )
            timeZone = TimeZone.getDefault();
        
        coreParams = RcMessageUtils.getMessageParams( rc.getAdminUser(), rc, null, locale, false );     
        
        coreParams[20] = candidateName;
        coreParams[21] = raterName;
        coreParams[22] = ratersName;
        
    }
    
    
    protected void updateCoreParamsForRater( RcRater rater )
    {
        coreParams[15] = rater.getUser().getFullname();
        coreParams[16] = rater.getRaterStartUrl();
        coreParams[18] = Float.toString( rater.getOverallScore() );
        
    }
    
    
    public Object[] getStandardHeaderSection( boolean tog, String introLangKey )
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();
        
        boolean includeCompanyInfo = true;
        boolean includeTop = true;
        
        String label;
        String value;

        if( includeTop )
        {
            String intro = introLangKey!=null && !introLangKey.isBlank() ? lmsg(  introLangKey , coreParams ) : null;

            if( intro != null && !intro.isBlank())
                sb.append( "<tr " + rowStyle0 + "><td colspan=\"7\" style=\"border-bottom:0px solid black;padding:10px\">" + intro + "</td></tr>\n" );
        }

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        if( includeTop )
        {
            // title Row
            label = lmsg(  "g.RSRefInfo" , new String[]{rcCheckTypeName} );
            sb.append( getRowTitle( rowStyleHdr, label, null, null, null ) );


            // this is 
            value = candidateUser.getFullname();
            String nameKey = "g.NameC";
            label = lmsg(  nameKey , null );
            if( value != null && value.length() > 0 )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                sb.append( getRow( style, label, value, false ) );
            }

            if( rc.getHasPhotos() )
            {
                value = getPhotoTableHtml(rc.getFauxPhotoRcUploadedUserFilesPhoto(), true );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    label = lmsg(  "g.Photos" , null );
                    sb.append( getRow( style, label, value, false ) );
                }
                
                value = getPhotoTableHtml(rc.getFauxPhotoRcUploadedUserFilesId(), false);
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    label = lmsg(  "g.PhotosId" , null );
                    sb.append( getRow( style, label, value, false ) );
                }

            }
            
            value = rc.getRcCheckStatusType().getName( locale );
            nameKey = "g.StatusC";
            label = lmsg(  nameKey , null );
            if( value != null && value.length() > 0 )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                sb.append( getRow( style, label, value, false ) );
            }

            if( prehire && rc.getRcCheckStatusType().getIsComplete()  )
            {
                value = I18nUtils.getFormattedNumber(locale, rc.getOverallScore(), scrDigits);
                if( rc.getHasSuspiciousActivity() )
                {
                    value += "<br /><span style=\"color:red\">" + lmsg( "g.RCSusActDetectSeeBelow" ) + "</span>";
                }
                nameKey = "g.ScoreC";
                label = lmsg(  nameKey , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                } 
            }

            if( !StringUtils.isCurlyBracketed( candidateUser.getEmail() ) )
            {
                value = candidateUser.getEmail();
                label = lmsg(  "g.EmailC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            if( candidateUser.getHasMobilePhone() && !StringUtils.isCurlyBracketed( candidateUser.getMobilePhone() ) )
            {
                value = candidateUser.getMobilePhone();
                label = lmsg(  "g.MobileC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            if( candidateUser.getCountryCode()!=null && !candidateUser.getCountryCode().isBlank() )
            {
                value = lmsg( "cntry." + candidateUser.getCountryCode() );
                label = lmsg(  "g.CountryC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            if( candidateUser.getHasAltIdentifierInfo() )
            {
                String ainame = candidateUser.getAltIdentifierName();

                if( ainame == null || ainame.isEmpty() )
                    ainame = lmsg(  "g.DefaultAltIdentifierName" );

                value = candidateUser.getAltIdentifier();
                label = ainame + ":";
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            if( rc.getExtRef()!=null && !rc.getExtRef().isBlank() )
            {
                value = rc.getExtRef();
                label = lmsg(  "g.ExtRefC" );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }
            
            
            // this is 
            if( rc.getRcCheckType().getIsPrehire() )
            {
                value = rc.getJobTitle();
                nameKey = "g.TitleC";
                label = lmsg(  nameKey , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }
            

            // Template
            label = lmsg(  "g.RSTemplateC" , null );
            value = rc.getRcScript().getName();                                
            if( value != null && value.length() > 0 )
            {
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                sb.append( getRow( style, label, value, false ) );
            }

            if( rc.getCandidateCompleteDate()!=null )
            {
                value =I18nUtils.getFormattedDateTimeShort(locale, rc.getCandidateCompleteDate(), timeZone );
                label = lmsg(  "g.RSCandCompleteDateC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }                    
            }
            
            if( rc.getRcCheckType().getIsPrehire() && rc.getFirstCandidateSendDate()!=null && rc.getFirstCandidateReferenceDate()!=null )
            {
                long msec = rc.getLastCandidateReferenceDate().getTime() - rc.getFirstCandidateSendDate().getTime();                
                value =  StringUtils.getDaysHrsMinsStr( msec, locale );
                label = lmsg(  "g.RCCandFirstRefTime" , null ) + ":";
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }                    
            }

            if( rc.getRcCheckType().getIsPrehire() && rc.getFirstCandidateReferenceDate()!=null && rc.getLastCandidateReferenceDate()!=null && rc.getLastCandidateReferenceDate().getTime() - rc.getFirstCandidateReferenceDate().getTime() > 300*1000 )
            {
                long msec = rc.getLastCandidateReferenceDate().getTime() - rc.getFirstCandidateReferenceDate().getTime();                
                value =  StringUtils.getDaysHrsMinsStr( msec, locale );
                label = lmsg(  "g.RCCandLastRefTime" , null ) + ":";
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }                    
            }

            if( rc.getRcCandidateStatusType().getIsCompletedOrHigher() && rc.getCandidateStartDate()!=null && rc.getCandidateCompleteDate()!=null )
            {
                long msec = rc.getCandidateCompleteDate().getTime() - rc.getCandidateStartDate().getTime();                
                value =  StringUtils.getDaysHrsMinsSecsStr( msec, locale );
                label = lmsg(  "g.RCCandComplTime" , null ) + ":";
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }                    
            }            
            
            if( rc.getRcCheckStatusType().getIsComplete() )
            {
                value =I18nUtils.getFormattedDateTimeShort(locale, rc.getCompleteDate(), timeZone );
                label = lmsg(  "g.RSCompletedC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            else
            {
                value = Math.round( rc.getPercentComplete() ) + "%";
                label = lmsg(  "g.RCPctCompleteC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }
            
            if( rc.getHasCandidateIpLocationData() )
            {
                value = rc.getCandidateIpLocationData();
                label = lmsg(  "g.RCCandIpLocDataC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }                
            }

            
            // include only if there is an auth user name.
            if( includeCompanyInfo )
            {
                value = adminUser.getFullname();
                label = lmsg(  "g.RSAuthorizedByC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }

                value = org.getName();

                if( org.getReportLogoUrl() != null && !org.getReportLogoUrl().isEmpty() )
                    value = "<img src=\"" + org.getReportLogoUrl() + "\" alt=\"" +  StringUtils.replaceStandardEntities( org.getName() ) + "\"/>";

                label = lmsg(  "g.OrganizationC" , null );
                if( value != null && value.length() > 0 )
                {
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }

            if( rc.getRcCheckStatusType().getIsComplete() )
            {
                tog = !tog;
                value = getStandardRatersByRoleTable( tog );
                label = lmsg(  "g.RSRatersByRoleByC" , coreParams );
                if( value != null && value.length() > 0 )
                {
                    style = tog ? rowStyle1 : rowStyle2;
                    sb.append( getRow( style, label, value, false ) );
                }
            }
        } // if includeTop


        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }
    
    public String getStandardRatersByRoleTable( boolean tog )
    {
        String style = tog ? rowStyle1 : rowStyle2;
        StringBuilder sb = new StringBuilder();
        
        int[] rrt = rc.getRaterRoleTypeCounts();
        
        sb.append( "<table cellpadding=\"1\" cellspacing=\"0\" style=\"margin-left:0\">\n" );         
        if( rrt[1]>0 )
            sb.append( "<tr " + style + "><td>" + lmsg("rcrrt.supervisorormanager") + ":&#160;</td><td>" + rrt[1] + "</td></tr>\n" );
        if( rrt[2]>0 )
            sb.append( "<tr " + style + "><td>" + lmsg("rcrrt.peer") + ":&#160;</td><td>" + rrt[2] + "</td></tr>\n" );
        if( rrt[3]>0 )
            sb.append( "<tr " + style + "><td>" + lmsg("rcrrt.subordinate") + ":&#160;</td><td>" + rrt[3] + "</td></tr>\n" );
        if( rrt[4]>0 )
            sb.append( "<tr " + style + "><td>" + lmsg("rcrrt.otherorunknown") + ":&#160;</td><td>" + rrt[4] + "</td></tr>\n" );        
        sb.append( "</table>\n");
        
        return sb.toString();
    }    
    
    public Object[] getStandardSuspiciousActivityTable( boolean tog )
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();
        if( rc.getRcSuspiciousActivityList()==null || rc.getRcSuspiciousActivityList().isEmpty() )
        {
            out[0]="";
            out[1]=tog;
            return out;
        }

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        int anonymous = rc.getForceAllAnonymous()>=0 ? rc.getForceAllAnonymous() : rc.getRcScript().getForceAllAnonymous();
        
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( "g.RcSuspiciousActivityTitle"), null, null, null ) );

        // header row
        // sb.append( "<tr " + this.rowStyleSubHdr + "><td>" + lmsg("g.Name") + "</td><td style=\"text-align:center\">" + lmsg("g.Description") + "</td><td style=\"text-align:center\">" + lmsg("g.Date") + "</td></tr>\n" );
        sb.append( "<tr " + this.rowStyleSubHdr + "><td>" + lmsg("g.Name") + "</td><td style=\"text-align:center\">" + lmsg("g.Description") + "</td></tr>\n" );

        String name;
        String type;
        String specialNote;
        String date;
        String lastname;
        RcRater rtr;
        
        for( RcSuspiciousActivity sa : rc.getRcSuspiciousActivityList())
        {        
            if( sa.getRcSuspiciousActivityType().getIsRaterRaterMatch() )
                name=lmsg("g.RCMultiple");
            else
            {
                if( anonymous>=1 && sa.getRcRaterId()>0 )
                {
                    rtr = rc.getRcRaterForRcRaterId( sa.getRcRaterId() );
                    if( rtr!=null && rtr.getAnonymousName()!=null )
                        name = rtr.getAnonymousName();
                    else
                        name = "****";
                }
                else
                    name = sa.getUser()==null ? "" : ( anonymous>=1 ? "****" : sa.getUser().getFullname() );
                
                //name = sa.getUser()==null ? "" : sa.getUser().getFullname();
            }
            
            type = lmsg( sa.getRcSuspiciousActivityType().getKey() );
            if( sa.getSpecialNote()!=null )
                specialNote=StringUtils.replaceStandardEntities( sa.getSpecialNote() );
            else
                specialNote="";
            date = I18nUtils.getFormattedDateTimeShort(locale, sa.getCreateDate(), timeZone );
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            //sb.append( "<tr " + style + "><td style=\"vertical-align:top\">" + name + "</td><td style=\"text-align:left;vertical-align:top\">" + type + (specialNote.isBlank() ? "" : "<br /><p>" + specialNote + "</p>")  + "</td><td style=\"text-align:center;vertical-align:top\">" + date + "</td></tr>\n" );            
            sb.append( "<tr " + style + "><td style=\"vertical-align:top;text-align:left;color:red\">" + name + "</td><td style=\"text-align:left;vertical-align:top;\">" + type + (specialNote.isBlank() ? "" : "<br /><p>" + specialNote + "</p>")  + "</td></tr>\n" );            
        }
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    
    

    public Object[] getStandardTopItemsTable( boolean tog, boolean high ) throws Exception
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        //if( !rc.getRcCheckType().getIsPrehire()  )
        //{
        //    out[0]=sb.toString();
        //    return out;
        //}
            
        int q = rc.getTopBottomCount(); // Constants.DEFAULT_HILOWCOMPETENCIES;
        int scoredItems = 0;
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( rciw.getHasRatingInfoToShow() )
                    scoredItems++;
            }
        }
        if( scoredItems<=0 )
        {
            out[0]=sb.toString();
            return out;
        }
        
        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();  
        
        List<RcItemWrapper> rcl = high ? rcCheckUtils.getHighScoringRcItems(rc, q ) : rcCheckUtils.getLowScoringRcItems(rc, q );
        LogService.logIt( "BaseFormatter.getStandardTopItemsTable() competencies to list=" + rcl.size() );
            
        if( rcl.isEmpty() )
        {
            out[0]=sb.toString();
            return out;
        }
        
        String topBotSrcTypeName = RcTopBottomSrcType.getValue( rc.getTopBottomSrcTypeId()).getName(locale).toLowerCase();
        
        String titleKey = high ? "g.RSItemsHigh" : "g.RSItemsLow"; // ) + (devel ? ".your" : "" );            
        if( rc.getRcCheckType().getIsEmployeeFeedback() )
            titleKey += "360";
        
        if( rc.getIsSelfOnly() )
            titleKey += "SO";
        
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( titleKey, new String[]{topBotSrcTypeName} ), null, null, null ) );

        // header row
        // sb.append( "<tr " + this.rowStyleSubHdr + "><td style=\"width:30%;text-align:center\">" + lmsg("g.Competency") + "</td><td  style=\"width:70%;text-align:center\">" + lmsg("g.Score") + "</td></tr>\n" );
                
        tog = true;
        String style = rowStyle1;
        
        String questionStr;
        String barChartStr;
        

        //lmsg( "g.IGSuggInterviewQuestions"),  lmsg( "g.IGSuggInterviewQuestions.P1")        
        
        for( RcItemWrapper rciw : rcl )
        {
            LogService.logIt( "BaseFormatter.getStandardTopItemsTable() BBB adding=" + rciw.getRcItem().getRcItemId() );
            
            questionStr= StringUtils.replaceStandardEntities( rciw.getQuestionWithSubs());
            
            barChartStr = getTopItemsTableHtml(rciw, style );

            sb.append("<tr " + style + "><td style=\"vertical-align:middle;width:40%;text-align:left;padding:5px\">" + questionStr + "</td><td style=\"width:60%;padding:5px\">"+ barChartStr + "</td></tr>\n" );            
            
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;                
        }
                
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    


    
    public Object[] getStandardTopCompetenciesTable( boolean tog, boolean high ) throws Exception
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        //if( !rc.getRcCheckType().getIsPrehire()  )
        //{
        //    out[0]=sb.toString();
        //    return out;
        //}
            
        int q = rc.getTopBottomCount(); // Constants.DEFAULT_HILOWCOMPETENCIES;
        int scoredComps = 0;
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            if( rcw.getHasNumericScore() )
                scoredComps++;
        }
        if( scoredComps<=0 )
        {
            out[0]=sb.toString();
            return out;
        }
        else if( q==3 && rc.getRcCheckType().getIsEmployeeFeedback() && scoredComps>0 && scoredComps<q*2 )
            q = scoredComps/2;
                
                
        
        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();  
        
        List<RcCompetencyWrapper> rcl = high ? rcCheckUtils.getHighScoringRcCompetencies( rc, q ) : rcCheckUtils.getLowScoringRcCompetencies( rc, q );
        // LogService.logIt( "BaseFormatter.getStandardTopCompetenciesTable() competencies to list=" + rcl.size() );
            
        if( rcl.isEmpty() )
        {
            out[0]=sb.toString();
            return out;
        }
        
        String topBotSrcTypeName = RcTopBottomSrcType.getValue( rc.getTopBottomSrcTypeId()).getName(locale).toLowerCase();
        
        String titleKey = high ? "g.RSCompetenciesHigh" : "g.RSCompetenciesLow"; // ) + (devel ? ".your" : "" );            
        if( rc.getRcCheckType().getIsEmployeeFeedback() )
            titleKey += "360";
        
        if( rc.getIsSelfOnly() )
            titleKey += "SO";
        
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( titleKey, new String[]{topBotSrcTypeName} ), null, null, null ) );

        // header row
        // sb.append( "<tr " + this.rowStyleSubHdr + "><td style=\"width:30%;text-align:center\">" + lmsg("g.Competency") + "</td><td  style=\"width:70%;text-align:center\">" + lmsg("g.Score") + "</td></tr>\n" );
                
        tog = true;
        String style = rowStyle1;
        
        String competencyStr;
        String barChartStr;
        

        //lmsg( "g.IGSuggInterviewQuestions"),  lmsg( "g.IGSuggInterviewQuestions.P1")        
        
        for( RcCompetencyWrapper rcw : rcl )
        {
            LogService.logIt( "BaseFormatter.getStandardTopCompetenciesTable() BBB adding=" + rcw.getRcCompetency().getName() + ", " + rcw.getRcCompetency().getInterviewQuestion() );
            
            competencyStr= StringUtils.replaceStandardEntities( rcw.getRcCompetency().getName() );
            
            //if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
            //    competencyStr += "<br />" + StringUtils.replaceStandardEntities( rcw.getRcCompetency().getDescription() );
            
            barChartStr = getTopCompetenciesTableHtml(rcw, style );

            sb.append( "<tr " + style + "><td style=\"vertical-align:middle;width:40%;text-align:left;padding:5px\">" + competencyStr + "</td><td style=\"width:60%;padding:5px\">"+ barChartStr + "</td></tr>\n" );            
            
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;                
        }
                
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    

    
    
    public Object[] getStandardReferralsTable( boolean tog ) throws Exception
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        if( !rc.getRcCheckType().getIsPrehire() || rc.getAskForReferrals()!=1 )
        {
            out[0]=sb.toString();
            return out;
        }
        
        if( rcFacade==null )
            rcFacade=RcFacade.getInstance();        
        
        List<RcReferral> rl = rcFacade.getRcReferralList( rc.getRcCheckId() );
        
        if( rl==null || rl.isEmpty() )
        {
            out[0]=sb.toString();
            return out;
        }
        
        if( userFacade==null )
            userFacade = UserFacade.getInstance();
        
        for( RcReferral r : rl )
        {
            r.setUser( userFacade.getUser( r.getUserId() ));
            r.setReferrerUser( userFacade.getUser( r.getReferrerUserId()));            
        }
                        
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( "g.RtrReferrals"), null, null, null ) );

        // header row
        sb.append( "<tr " + this.rowStyleSubHdr + "><td>" + lmsg("g.Name") + "</td><td>" + lmsg("g.Email") + "</td><td>" + lmsg("g.MobilePhone") + "</td><td>" + lmsg("g.Referror") + "</td></tr>\n" );
                
        tog = true;
        String style = rowStyle1;
                
        
        for( RcReferral r : rl )
        {
            sb.append( "<tr " + style + "><td>" + r.getUser().getFullname() + "</td><td>"+ (r.getUser().getHasValidEmail() ? r.getUser().getEmail() : "") + "</td><td>"+ (r.getUser().getHasMobilePhone() ?  r.getUser().getMobilePhone() : "") + "</td><td>"+ (r.getRcReferralTypeId()==1 ?  r.getReferrerUser().getLastName() : lmsg("rcrft.raterself") )+ "</td></tr>\n" );                        
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;                
        }
                
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    
    

    public Object[] getStandardInterviewQuestionTable( boolean tog ) throws Exception
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        if( !rc.getRcCheckType().getIsPrehire()  )
        {
            out[0]=sb.toString();
            return out;
        }
            
        int q =Constants.DEFAULT_INTERVIEW_QUESTIONS;
        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();        
        List<RcCompetencyWrapper> rcl = rcCheckUtils.getRcCompetenciesForInterviewQuestions( rc, q );
        LogService.logIt( "BaseFormatter.getStandardInterviewQuestionTable() competencies to list=" + rcl.size() );
            
        if( rcl.isEmpty() )
        {
            out[0]=sb.toString();
            return out;
        }
            
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( "g.IGSuggInterviewQuestions"), null, null, null ) );

        // header row
        sb.append( "<tr " + this.rowStyleSubHdr + "><td style=\"width:30%;text-align:center\">" + lmsg("g.Competency") + "</td><td  style=\"width:70%;text-align:center\">" + lmsg("g.Question") + "</td></tr>\n" );
        
        
        tog = true;
        String style = rowStyle1;
        
        String competencyStr;
        String questionStr;
        

        //lmsg( "g.IGSuggInterviewQuestions"),  lmsg( "g.IGSuggInterviewQuestions.P1")        
        
        for( RcCompetencyWrapper rcw : rcl )
        {
            LogService.logIt( "BaseFormatter.addInterviewGuide() BBB adding=" + rcw.getRcCompetency().getName() + ", " + rcw.getRcCompetency().getInterviewQuestion() );
            
            competencyStr= "<b>" + StringUtils.replaceStandardEntities( rcw.getRcCompetency().getName() ) + "</b>";
            
            if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                competencyStr += "<br />" + StringUtils.replaceStandardEntities( rcw.getRcCompetency().getDescription() );
            
            questionStr = getInterviewQuestionTableHtml( rcw );

            sb.append( "<tr " + style + "><td style=\"vertical-align:top;text-align:left;padding:5px;\">" + competencyStr + "</td><td style=\"padding:5px\">"+ questionStr + "</td></tr>\n" );            
            
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;                
        }
                
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    
    
    
    public String getTopItemsTableHtml( RcItemWrapper rcw, String style)
    {
        float scoreAll = rcw.getAverageScore(null);
        float scoreOthers = rcw.getScoreAvgNoCandidate();
        float scoreSelf = rcw.getScoreCandidate( rc.getCandidateRcRaterId() );   
        
        return getTopTableHtml( scoreAll, scoreOthers, scoreSelf, style );
    }
    
    public String getTopCompetenciesTableHtml( RcCompetencyWrapper rcw, String style)
    {
        float scoreAll = rcw.getAverageScore(null);
        float scoreOthers = rcw.getScoreAvgNoCandidate();
        float scoreSelf = rcw.getAvgScoreCandidate( rc.getCandidateRcRaterId() );
        
        return getTopTableHtml( scoreAll, scoreOthers, scoreSelf, style );
    }
    
    public String getTopTableHtml( float scoreAll, float scoreOthers, float scoreSelf, String style )
    {
        StringBuilder sb = new StringBuilder();        
        sb.append( "<table cellpadding=\"2\" cellspacing=\"0\" class=\"" + style + "\" style=\"margin-left:0;width:100%;font-size:10pt\">\n" );        
        
        boolean isSelfOnly =  rc.getIsSelfOnly(); 
        
        int totalBarWid = 200;
        int barWid = 0;
        String color;

        RcTopBottomSrcType srcTyp = RcTopBottomSrcType.getValue( rc.getTopBottomSrcTypeId() );
        String srcKey = "g.RSHiLoOthers";            
        float scoreToUse = scoreOthers;

        if( srcTyp.getIsAll() )
        {
            srcKey="g.RSHiLoAll";
            scoreToUse = scoreAll;
        }
        else if( isSelfOnly || srcTyp.getIsSelf() )
        {
            srcKey="g.RSHiLoSelf";
            scoreToUse = scoreSelf;
        }

        boolean hasData = false;
        
        if( !isSelfOnly && scoreToUse>0 )
        {       
            hasData=true;
            
            barWid = (int) (((float)totalBarWid) *(scoreToUse/10f));

            color = getCompBarRgbColor( scoreToUse, true );

            sb.append( "<tr><td style=\"width:14%;padding-right:2px;text-align:right\">" + lmsg(srcKey) + "</td><td style=\"width:86%\">" ); 
            // sb.append( "<tr><td style=\"width:20%\">" + lmsg("g.RSHiLoOthers") + "</td><td style=\"width:7%;text-align:center\">" + I18nUtils.getFormattedNumber(locale, scoreOthers, scrDigits) + "</td><td style=\"width:73%\">" ); 
            sb.append("<div style=\"width:300px;height:20px;position:relative;display:block;padding-top:1px;padding-bottom:1px\">" );

            sb.append( "<div style=\"float:left;width:" + barWid + "px;height:18px;background-color:" + color + "\"></div>");
            sb.append( "<div style=\"float:left;width:30px;padding-left:6px;height:18px;\">" + I18nUtils.getFormattedNumber(locale, scoreToUse, scrDigits) + "</div>");
            
            sb.append("</div>" );            
            sb.append("</td></tr>\n" );
        }
        
        if( scoreSelf>0 && (isSelfOnly  || !srcTyp.getIsSelf()) )
        {
            hasData = true;
            
            barWid = (int) (((float)totalBarWid) *(scoreSelf/10f));
            // color = high ? Constants.HIGH_COMP_RGBCOLOR_SELF : Constants.LOW_COMP_RGBCOLOR_SELF;
            color = getCompBarRgbColor( scoreSelf, false );
            sb.append( "<tr><td style=\"width:14%;padding-right:2px;text-align:right\">" + lmsg("g.RSHiLoSelf") + "</td><td style=\"width:86%\">" ); 
            // sb.append( "<tr><td style=\"width:20%\">" + lmsg("g.RSHiLoOthers") + "</td><td style=\"width:7%;text-align:center\">" + I18nUtils.getFormattedNumber(locale, scoreOthers, scrDigits) + "</td><td style=\"width:73%\">" ); 
            sb.append("<div style=\"width:300px;height:20px;position:relative;display:block;padding-top:1px;padding-bottom:1px\">" );

                sb.append( "<div style=\"float:left;width:" + barWid + "px;height:18px;background-color:" + color + "\"></div>");
                sb.append( "<div style=\"float:left;width:50px;padding-left:6px;height:18px;\">" + I18nUtils.getFormattedNumber(locale, scoreSelf, scrDigits) + "</div>");
            
            sb.append("</div>" );            
            sb.append("</td></tr>\n" );
        }
        
        if( !hasData ) 
            return null;
        
        sb.append( "</table>\n" );
        return sb.toString();        
    }

    
    public String getCompBarRgbColor( float scr, boolean others )
    {
        RcRatingScaleType rst = rc.getRcScript().getRcRatingScaleType();
        
        if( scr<=rst.getMaxLowRatedCompScore() ) // Constants.RC_MAX_LOWRATED_COMP_SCORE )
            return others ? Constants.LOW_COMP_RGBCOLOR_OTHERS : Constants.LOW_COMP_RGBCOLOR_SELF;
        if( scr<rst.getMinHighRatedCompScore() ) //Constants.RC_MIN_HIGHRATED_COMP_SCORE )
            return others ? Constants.MED_COMP_RGBCOLOR_OTHERS : Constants.MED_COMP_RGBCOLOR_SELF;
        return others ? Constants.HIGH_COMP_RGBCOLOR_OTHERS : Constants.HIGH_COMP_RGBCOLOR_SELF;
    }
    
    
    public String getInterviewQuestionTableHtml( RcCompetencyWrapper rcw )
    {
        StringBuilder sb = new StringBuilder();
        
        sb.append( "<table style=\"width:100%;margin-left:0\" cellpadding=\"2\" cellspacing=\"0\">\n" );
        
        sb.append( "<tr><td colspan=\"11\" style=\"padding-top:4px;padding-bottom:4px;text-align:left\">" + StringUtils.replaceStandardEntities( rcw.getRcCompetency().getInterviewQuestion() ) + "</td></tr>\n" );
        
        // interviewQStarUrl
        sb.append( "<tr><td style=\"width:5%\">&#160;</td>" );
        for( int i=1;i<=5; i++ )
        {
            sb.append( "<td style=\"text-align:center;width:10%\"><img src=\"" + interviewQStarUrl + "\"</td>" ); 
            if( i<5 )
                sb.append( "<td style=\"width:10%\">&#160;</td>\n" );
        }        
        sb.append( "<td style=\"width:5%\">&#160;</td></tr>\n" );

        sb.append( "<tr><td style=\"width:5%\">&#160;</td>" );
        for( int i=1;i<=5; i++ )
        {
            sb.append( "<td style=\"text-align:center;width:10%\">" + i + "</td>" );        
            if( i<5 )
                sb.append( "<td style=\"width:10%\">&#160;</td>" );
        }        
        sb.append( "<td style=\"width:5%\">&#160;</td></tr>\n" );

        sb.append( "<tr><td style=\"width:5%\">&#160;</td><td colspan=\"4\" style=\"text-align:left;vertical-align:top\">" + rcw.getRcCompetency().getAnchorLow() + "</td><td style=\"width:10%\">&#160;</td>" );
        sb.append( "<td colspan=\"4\" style=\"text-align:right;vertical-align:top\">" + rcw.getRcCompetency().getAnchorHi() + "</td><td style=\"width:5%\">&#160;</td></tr>" );
        
        sb.append( "</table>\n" );
        
        return sb.toString();
    }
    
    

    public Object[] getStandardPdfReportTable( boolean tog ) throws Exception
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        tog = true;
        String style = rowStyle1;

        if( !rc.getRcCheckStatusType().getIsComplete() )
        {
            out[0]="";
            out[1]=tog;
            return out;
        }
        
        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg( "g.RcPdfDownloads"), null, null, null ) );

        if( rcCheckUtils==null )
            rcCheckUtils=new RcCheckUtils();

        List<Integer> rild = rcCheckUtils.getReportIdsForRcCheck(rc, null );
        
        for( Integer reportId : rild )
        {
            String url = rcCheckUtils.getPdfDownloadUrl( rc, reportId, null );        
            Report report = ReportFacade.getInstance().getReport(reportId);

            String title = getReportName( report );
            String pdfImgUrl = RuntimeConstants.getStringValue("baseadminurl") + "/resources/images/pdf_download2.png";
            sb.append( "<tr " + style + "><td style=\"vertical-align:middle;text-align:center;paddding:7px;width:10%\"><a href=\"" + url + "\" title=\"" + lmsg("g.RcPdfDownloadTitle" ) + "\"><img src=\"" + pdfImgUrl + "\" alt=\"\"/></a></td><td style=\"width:90%;vertical-align:middle;text-align:left;paddding:7px\"><a href=\"" + url + "\" title=\"" + lmsg("g.RcPdfDownloadTitle" ) + "\">"+ title + "</a></td></tr>\n" );            
        }

        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    
    


    
    public String getReportName( Report r ) {

        String ttl = "";
        
        if( r!=null && r.getStrParam3()!=null && !r.getStrParam3().isEmpty() )
            ttl = r.getStrParam3();
        
        else if( r!=null && r.getStrParam2()!=null && !r.getStrParam2().isEmpty() )
        {
            String key = r.getStrParam2(); // "g.TestResultsAndInterviewGuide";
        
            ttl = MessageFactory.getStringMessage(locale, key, null );
            
            if( ttl ==null )
                ttl = r.getTitle();
        }
        
        else if( r.getTitle()!=null && !r.getTitle().isEmpty() )
            ttl = r.getTitle();            
        
        else
            ttl = r.getName();
        
        return StringUtils.replaceStr(ttl, "[SCRIPTNAME]" , rc.getRcScript().getName() );
    }
    
    
    
    
    public Object[] getStandardRatersSection( boolean tog )
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();

        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        // title Row
        sb.append( getRowTitle( rowStyleHdr, ratersName, null, null, null ) );

        String photoEle = rc.getHasRaterPhotos() ? "<td>" + lmsg("g.Photo") + "</td>" : "";

        String photoIdEle = rc.getHasRaterIdPhotos() ? "<td>" + lmsg("g.PhotoId") + "</td>" : "";
        
        // header row
        sb.append( "<tr " + this.rowStyleSubHdr + "><td>" + lmsg("g.Name") + "</td>" + photoEle + photoIdEle + "<td style=\"text-align:center\">" + lmsg("g.Role") + "</td><td style=\"text-align:center\">" + lmsg("g.Contact") + "</td><td style=\"text-align:center\">" + lmsg("g.Status") + "</td><td style=\"text-align:center\">" + lmsg("g.Score") + "</td></tr>\n" );
        
        String scoreStr = null;
        String statusStr = null;
       
        String contactOkStr;
        String recruitingOkStr;
        
        if( rc.getRcRaterList()==null || rc.getRcRaterList().isEmpty() )
        {
             out[0]="";
             out[1]=tog;
             return out;
        }
        
        int anonymous = rc.getForceAllAnonymous()>=0 ? rc.getForceAllAnonymous() : rc.getRcScript().getForceAllAnonymous();
        String fullname;
        String contactInfoStr;
        String thumbUrl;
        RcUploadedUserFile ruuf;
        String fn;
                
        for( RcRater rater : rc.getRcRaterList() )
        {   
            // skip candidate.
            if( rater.getIsCandidateOrEmployee() )
                continue;
            
            scoreStr = "";
            contactOkStr = "";
            recruitingOkStr = "";
            contactInfoStr = "";
            fullname = rater.getUserFullnameOrAnonymousName();
            
            thumbUrl=null;
            if( rater.getHasPhotos() )
            {
                ruuf = rater.getSinglePhotoFauxRcUploadedUserFile();
                fn = ruuf.getThumbFilename();
                if( fn!=null && fn.contains(  ".IDX." ) )
                    fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );
            
                thumbUrl = ReportUtils.getMediaTempUrlSourceLink( this.rc.getOrgId(), ruuf, 1, fn, MediaTempUrlSourceType.REF_THUMB );                        
            }
            
            photoEle = rater.getHasPhotos() && thumbUrl!=null && !thumbUrl.isBlank() ? "<td><img src=\"" + thumbUrl + "\" style=\"max-width:100px;border:0\"" + "</td>" : ( rc.getHasRaterPhotos() ? "<td></td>" : "");

            if( rater.getHasIdPhotos() )
            {
                ruuf = rater.getSingleIdPhotoFauxRcUploadedUserFile();
                fn = ruuf.getThumbFilename();
                if( fn!=null && fn.contains(  ".IDX." ) )
                    fn = StringUtils.replaceStr( fn, ".IDX." , ".1." );
            
                thumbUrl = ReportUtils.getMediaTempUrlSourceLink( this.rc.getOrgId(), ruuf, 1, fn, MediaTempUrlSourceType.REF_THUMB );                        
            }
            photoIdEle = rater.getHasIdPhotos() && thumbUrl!=null && !thumbUrl.isBlank() ? "<td><img src=\"" + thumbUrl + "\" style=\"max-width:100px;border:0\"" + "</td>" : ( rc.getHasRaterIdPhotos() ? "<td></td>" : "");

            // photoEle = rater.getHasPhotos() ? "<td><img src=\"" + rater.getSinglePhotoFauxRcUploadedUserFile().getThumbUrl() + "\" style=\"max-width:100px;border:0\"" + "</td>" : ( rc.getHasRaterPhotos() ? "<td></td>" : "");
            
            if( rater.getRcRaterStatusType().getIsComplete() )
            {
                
                scoreStr = I18nUtils.getFormattedNumber(locale, rater.getOverallScore(), scrDigits );
                
                // NOT Anonymous
                if( anonymous<1 )
                {
                    if( rater.getContactPermissionTypeId()==RcContactPermissionType.YES.getRcContactPermissionTypeId()  )
                        contactOkStr = "<br />" + lmsg( "g.RSOkToContactYes" );
                    else if( rater.getContactPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId()  )
                        contactOkStr = "<br /><span style=\"color:red\">" +  lmsg( "g.RSOkToContactNo" ) + "</span>";

                    if( rater.getRecruitingPermissionTypeId()==RcContactPermissionType.YES.getRcContactPermissionTypeId()  )
                        recruitingOkStr = "<br />" + lmsg( "g.RSOkToContactRecruitYes" );
                    else if( rater.getRecruitingPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId()  )
                        recruitingOkStr = "<br /><span style=\"color:red\">" +  lmsg( "g.RSOkToContactRecruitNo" ) + "</span>";   
                }
                else
                {
                    contactOkStr="";
                    recruitingOkStr="";
                }
            }

            // NOT anonymous
            if( anonymous<1 )
                contactInfoStr = rater.getUser().getEmail() + (rater.getUser().getHasMobilePhone() ? "<br />" + rater.getUser().getMobilePhone() : "" );
                        
            statusStr = rater.getRcRaterStatusType().getName(locale);
            if( rater.getRcRaterStatusType().getIsComplete() )
                statusStr += "<br />" + I18nUtils.getFormattedDateTimeShort(locale, rater.getCompleteDate(), timeZone );
            else if( !rater.getRcRaterStatusType().getCompleteOrHigher() && rater.getLastUpdate()!=null )
                statusStr += "<br />" + lmsg( "g.RSLastUpdateX", new String[]{I18nUtils.getFormattedDateTimeShort(locale, rater.getCompleteDate(), timeZone )} );
            
            if( rater.getHasIpLocationData() )
                statusStr += "<br />"  + lmsg( "g.RCIpLocDataX", new String[]{rater.getIpLocationData()} );
            
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;
            sb.append( "<tr " + style + "><td style=\"vertical-align:top\">" + fullname + "</td>" + photoEle + photoIdEle + "<td style=\"text-align:center;vertical-align:top\">" + rater.getRcRaterRoleType().getName(locale, rater.getRcCheck()==null || rater.getRcCheck().getRcOrgPrefs()==null ? null : rater.getRcCheck().getRcOrgPrefs().getOtherRoleTypeNames(rater.getRcCheck().getRcSuborgPrefs()))  + "</td><td style=\"text-align:center;vertical-align:top\">" + contactInfoStr + contactOkStr + recruitingOkStr + "</td><td style=\"vertical-align:top\">" + statusStr  + "</td><td style=\"text-align:center;vertical-align:top\">" + scoreStr + "</td></tr>\n" );            
        }

        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    

    
    
    public Object[] getStandardCompetencySummarySection( boolean tog )
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();
                
        // histogramAtItemLevel = true;
        String competencySummaryName = lmsg( "g.CompSummaryTitle");

        int scrDigits = report.getIntParam2() >= 0 ? report.getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;   
        
        boolean incIdeal = this.rc.getRcScript().getIdealScoresOk()==1;
        
        //String scr;
        long candidateRaterId = rc.getCandidateRcRaterId();
        boolean hasCandidate = rc.getCollectCandidateRatings()>0 && candidateRaterId>0;
        String candidateNameKey = rc.getRcCheckType().getIsPrehire() ? "g.Candidate" : "g.Employee";
             
        boolean isSelfOnly =  rc.getIsSelfOnly();     
        
        tog = true;
        String style = tog ? rowStyle1 : rowStyle2;


        RcCompetencyWrapper rcw;
        
        // place in alpha order
        Collections.sort(rc.getRcScript().getRcCompetencyWrapperList(), new RcCompetencyWrapperNameComparator() );
                    
        ListIterator<RcCompetencyWrapper> iter = rc.getRcScript().getRcCompetencyWrapperList().listIterator();
        ListIterator<RcItemWrapper> qiter;
        float score, scoreCandidate;
        int rows = 0;
        String scoreStr, scoreStrCandidate, idealScoreStr;
        String descripStr;
        
        while( iter.hasNext() )
        {
            rcw = iter.next();
            if( rcw.getHasAnyScoredItems() )
                rows++;
        }    

        if( rows<=0 )
        {
             out[0]="";
             out[1]=tog;
             return out;
        }

        // title Row
        sb.append( getRowTitle( rowStyleHdr, competencySummaryName, null, null, null ) );

        // header row
        sb.append( "<tr " + rowStyleSubHdr + "><td style=\"font-weight:bold\">" + lmsg("g.Competency") + "</td>" + 
                (incIdeal ? "<td style=\"text-align:center;font-weight:bold\">" + lmsg( "g.RCIdealScr" ) + "</td>" : "") + 
                "<td style=\"text-align:center;font-weight:bold\">" + lmsg( hasCandidate ? candidateNameKey :  "g.Score" ) + "</td>" + 
                (hasCandidate ? "<td style=\"text-align:center;font-weight:bold\">" + lmsg("g.Others") + "</td>" : "" )+ 
                "</tr>\n" );

        iter = rc.getRcScript().getRcCompetencyWrapperList().listIterator();
        while( iter.hasNext() )
        {
            rcw = iter.next();

            // no items produce a score.
            if( !rcw.getHasAnyScoredItems() )
                continue;

            score = rcw.getScoreAvgNoCandidate();
            scoreCandidate = hasCandidate ? rcw.getAvgScoreCandidate( candidateRaterId ) : 0;

            scoreStr = score>0 && !isSelfOnly ? I18nUtils.getFormattedNumber( locale, score, scrDigits ) : "-";
            scoreStrCandidate = scoreCandidate>0 ? I18nUtils.getFormattedNumber( locale, scoreCandidate, scrDigits ) : "-";
            idealScoreStr = incIdeal && rcw.getIdealScore()>0 ? I18nUtils.getFormattedNumber( locale, rcw.getIdealScore(), scrDigits ) : "-";
            
            descripStr = "";
            if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                descripStr = "<br /><i>" + StringUtils.replaceStandardEntities( rcw.getRcCompetency().getDescription() ) + "</i>";
            
            sb.append( "<tr " + style + "><td style=\"width:60%\"><b>" + rcw.getRcCompetency().getName() + "</b>" + 
                        descripStr + "</td>" + 
                        (incIdeal ? "<td style=\"width:10%;text-align:center;vertical-align:middle\">" + idealScoreStr + "</td>" : "" ) + 
                        "<td style=\"width:20%;text-align:center;vertical-align:middle\">" + ( hasCandidate ? scoreStrCandidate : scoreStr)  + "</td>" + 
                        ( hasCandidate ? "<td style=\"width:20%;text-align:center;vertical-align:middle\">" + scoreStr + "</td>" : "" ) + 
                        "</tr>\n" );     
            
            tog = !tog;
            style = tog ? rowStyle1 : rowStyle2;        
        }        
        
        // put back into displayorder order
        Collections.sort(rc.getRcScript().getRcCompetencyWrapperList() );
                    
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    
        
    
    public Object[] getStandardRatingsByQuestionSection( boolean tog )
    {
        Object[] out = new Object[2];
        StringBuilder sb = new StringBuilder();
        
        boolean histogramAtItemLevel = this.getReportRuleAsBoolean("rchistogramitemlevel");
        
        // histogramAtItemLevel = true;
        
        tog = true;
        String style; //  = tog ? rowStyle1 : rowStyle2;

        // title Row
        sb.append( getRowTitle( rowStyleHdr, lmsg("g.RSRatingsByQuestion"), null, null, null ) );

        // header row
        sb.append( "<tr " + rowStyleSubHdr + "><td style=\"font-weight:bold\">" + lmsg("g.Category") + "</td><td style=\"text-align:center;font-weight:bold\">" + lmsg("g.Score") + "</td><td style=\"text-align:center;font-weight:bold\">" + lmsg("g.ResponseDetails") + "</td></tr>\n" );
        
        String scoreStr;
        RcItem item;
        String questionStr;
        if( rcCheckUtils==null )
            rcCheckUtils = new RcCheckUtils();
        
        //long candidateRaterId = rc.getCandidateRcRaterId();
        //List<Long> rcRaterIdsToSkip = null;        
        //if( candidateRaterId>0 )
        //{
        //    rcRaterIdsToSkip = new ArrayList<>();
         //   rcRaterIdsToSkip.add(candidateRaterId);
        //}
        String raterTbl;
        String histogramTbl;
        int prevRcCompetencyId = 0;
        boolean includeHistogram = false;
        int scoredCt = 0;
        boolean histogramComplete;
        String descripStr;
        
        int dataCt = 0;
        
        for( RcCompetencyWrapper rcw : rc.getRcScript().getRcCompetencyWrapperList() )
        {
            histogramComplete = false;
            scoredCt = 0;
            if( rcw.getHasRatingInfoToShow() && rcw.getHasNumericScore() && rcw.getRcItemWrapperList()!=null && rcw.getRcItemWrapperList().size()>1 && rcw.getScoreAvgNoCandidate()>0 )
            {
                //scoredCt = 0;
                for( RcItemWrapper w : rcw.getRcItemWrapperList() )
                {
                    // not scored? Don't count it.
                    if( w.getHasRatingInfoToShow() && w.getScoreAvgNoCandidate()>0 )
                        scoredCt++;
                }
                if( scoredCt>1 )
                {
                    if( histogramAtItemLevel )
                        histogramTbl = "";            
                    else
                    {
                        histogramTbl = getRcItemHistogram(rcw, null);
                        histogramComplete = true;
                    }
                    
                    descripStr = rcw.getRcCompetency().getDescription();
                    if( descripStr!=null && !descripStr.isBlank() )
                        descripStr = "<br /><br />" + StringUtils.replaceStandardEntities( descripStr );
                    else
                        descripStr = "";
                        
                    // Add an overall row
                    scoreStr = I18nUtils.getFormattedNumber(locale, rcw.getScoreAvgNoCandidate(), scrDigits );
                    tog = !tog;
                    style = tog ? rowStyle1 : rowStyle2;
                    
                    sb.append( "<tr " + style + "><td style=\"width:35%\"><b>" + rcw.getRcCompetency().getName() + " (" +  lmsg( "g.overall") + ")</b>" + descripStr + "</td><td style=\"width:8%;text-align:center\">" + scoreStr  + "</td><td style=\"width:57%;text-align:center\">" + histogramTbl + "</td></tr>\n" );                                
                    dataCt++;
                }                
            }
            
            for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
            {
                if( !rciw.getHasRatingInfoToShow() )
                    continue;
                
                includeHistogram = histogramAtItemLevel || (rcw.getRcCompetencyId()!=prevRcCompetencyId && !histogramComplete);
                prevRcCompetencyId = rcw.getRcCompetencyId();

                descripStr = scoredCt>1 || rcw.getRcCompetency()==null ? null : rcw.getRcCompetency().getDescription();
                if( descripStr!=null && !descripStr.isBlank() )
                    descripStr = "<br /><br />" + StringUtils.replaceStandardEntities( descripStr );
                else
                    descripStr = "";
                
                item = rciw.getRcItem();
                scoreStr = item.getIsItemScored() ? I18nUtils.getFormattedNumber(locale, rciw.getScoreAvgNoCandidate(), scrDigits ) : "";
                tog = !tog;
                style = tog ? rowStyle1 : rowStyle2;
                
                questionStr = item.getQuestion();
                questionStr = rcCheckUtils.performSubstitutions( questionStr, rc, null, locale );
                
                raterTbl = getRaterResponseTableForItem( rciw );
                histogramTbl = includeHistogram ? ( histogramAtItemLevel ? getRcItemHistogram(null, rciw) : getRcItemHistogram(rcw, null) ): null;
                
                if( histogramTbl==null || histogramTbl.isBlank() )
                {
                    histogramTbl = raterTbl;
                    raterTbl = null;
                }    
                
                sb.append( "<tr " + style + "><td style=\"width:35%\"><b>" + rcw.getRcCompetency().getName() + ":</b> " + StringUtils.replaceStandardEntities( questionStr ) + descripStr + "</td><td style=\"width:8%;text-align:center\">" + scoreStr  + "</td><td style=\"width:57%;text-align:center\">" + histogramTbl + "</td></tr>\n" );            

                if( raterTbl!=null )
                    sb.append( "<tr " + style + "><td style=\"width:35%\"></td><td style=\"width:8%;text-align:center\"></td><td style=\"width:57%;text-align:center\">" + raterTbl + "</td></tr>\n" );            

                dataCt++;
            }             
        }
        
        // No data. 
        if( dataCt <= 0 )
        {
            out[0]="";
            out[1]=tog;
            return out;
        }
        
        out[0] = getHtmlTableStart( locale ) + sb.toString() + getHtmlTableEnd( locale );
        out[1] = tog;
        return out;
    }    

    
    protected String getPhotoTableHtml( List<RcUploadedUserFile> rufl, boolean isCandidate)
    {
        if( rufl==null || rufl.isEmpty() )
            return null;
        
        if( isCandidate && !org.getCandidateImageViewType().getShowPhotos() )
            return null;
        
        StringBuilder sb = new StringBuilder();
        String thumbUrl;
        String fn;
        for( RcUploadedUserFile u : rufl )
        {
            fn = u.getThumbFilename();
            if( fn!=null && fn.contains(  ".IDX." ) )
                fn = StringUtils.replaceStr( fn, ".IDX." , "." + u.getTempInt1() + "." );
            
            thumbUrl = ReportUtils.getMediaTempUrlSourceLink( this.rc.getOrgId(), u, u.getTempInt1(), fn, MediaTempUrlSourceType.REF_THUMB );
            sb.append( "<img src=\"" + thumbUrl + "\" style=\"float:left;margin:4px;max-width:100px\"/>\n" );
        }
        return sb.toString();
    }
    
    protected String getRaterResponseTableForItem( RcItemWrapper rciw )
    {
        //LogService.logIt( "BaseFormatter.getRaterResponseTableForItem() rciw is " + (rciw==null ? "null" : " not null, itemId=" + rciw.getRcItemId() + ", ratings.size=" + (rciw.getRcRatingList()==null ? "null" : "not null " + rciw.getRcRatingList().size()) ) );
        StringBuilder sb = new StringBuilder();
        
        if( rciw.getRcRatingList()==null || rciw.getRcRatingList().isEmpty() )
            return sb.toString();
        
        // int anonymous = rc.getForceAllAnonymous()>=0 ? rc.getForceAllAnonymous() : rc.getRcScript().getForceAllAnonymous();
        String lastname;
        
        sb.append( "<table cellpadding=\"2\" cellspacing=\"0\" style=\"margin-left:0;border:0px solid darkgrey;width:100%\">\n" );
        RcItem item = rciw.getRcItem();
        String scoreStr;
        String commentStr;
        String avPlaybackStr;
        String subtext;
        boolean tog=true;
        
        String selectedRespStr;
        String style="";
        long candidateRcRaterId = 0;
        for( RcRater r : rc.getRcRaterList() )
        {
            if( r.getIsCandidateOrEmployee() )
                candidateRcRaterId = r.getRcRaterId();
        }
        
        boolean hasAv = false;
        for( RcRating rating : rciw.getRcRatingList() )
        {
            if( rating.getRcUploadedUserFile()!=null && (rating.getRcUploadedUserFile().getHasRecordingInConversion() || rating.getRcUploadedUserFile().getHasRecordingReadyForPlayback()) )
            {
                hasAv=true;
                break;
            }
        }
        
        for( RcRating rating : rciw.getRcRatingList() )
        {
            scoreStr = item.getRcItemFormatType().getIsScoreOk() && rating.getScore()>0 ? I18nUtils.getFormattedNumber(locale, rating.getFinalScore(), scrDigits ) : "";            
            selectedRespStr = rating.getRcRatingStatusType().getIsSkipped() ? lmsg("g.Skipped") : rating.getSelectedChoicesTextXhtml();
            commentStr = rating.getText()==null || rating.getText().isBlank() ? "" : StringUtils.replaceStandardEntities( rating.getText() );
            avPlaybackStr = "";
            
            if( rating.getRcUploadedUserFile()!=null && (rating.getRcRaterId()!=candidateRcRaterId || org.getCandidateImageViewType().getShowPhotos()) )
            {
                if( rating.getRcUploadedUserFile().getHasRecordingInConversion() )
                    avPlaybackStr="<img src=\"" + (rating.getRcUploadedUserFile().getIsAudio() ? AUDIOCOMMENT_ICON_CONV_URL : VIDEOCOMMENT_ICON_CONV_URL) + "\" title=\"" + lmsg( "g.RCMediaInConversion" ) + "\" style=\"max-width:30px\"/>\n";
                else if( rating.getRcUploadedUserFile().getHasRecordingReadyForPlayback())
                    avPlaybackStr="<a href=\"" + rating.getRcUploadedUserFile().getReportingMediaUrl() + "\" title=\"" + lmsg("g.RCMediaClickToPlay") + "\"><img src=\"" + (rating.getRcUploadedUserFile().getIsAudio() ? AUDIOCOMMENT_ICON_URL : VIDEOCOMMENT_ICON_URL) + "\" style=\"max-width:30px\"/></a>\n";                
            }            
            
            subtext = rating.getSubtext()!=null && !rating.getSubtext().isBlank() ? "<br /><span style=\"font-style:italic\">" + StringUtils.replaceStandardEntities( rating.getSubtext() ) + "</span>" : "";
            
            if( selectedRespStr!=null && !selectedRespStr.isBlank() && ((commentStr!=null && !commentStr.isBlank()) || (subtext!=null && !subtext.isBlank())) )
                selectedRespStr += "\n<div style=\"margin-bottom: 8px;padding-top:6px;border-bottom: 1px solid darkgrey\"></div>\n";
            
            if( rating.getRcRater()==null )
                rating.setRcRater( rc.getRcRaterForRcRaterId(rating.getRcRaterId()) );
            
            if( rating.getUser()==null )
                rating.setUser( rating.getRcRater().getUser() );

            lastname = rating.getRcRaterId()==candidateRcRaterId ? rating.getUser().getLastName() : rating.getRcRater().getUserLastnameOrAnonymousName();
                                
            //if( rating.getUser()==null )
            //    rating.setUser( rc.getRcRaterForRcRaterId(rating.getRcRaterId()).getUser() );
            
            tog = !tog;
            style = tog ? rowStyle3 : rowStyle4;
            
            if( rating.getRcRaterId()==candidateRcRaterId )
                style=this.candidateRaterRowStype;
            
            if( hasAv )
                sb.append( "<tr " + style + "><td style=\"width:20%;font-size:11pt\">" + lastname + "</td><td style=\"width:7%;text-align:center;font-size:11pt\">" + scoreStr + "</td><td style=\"width:73%;font-size:11pt;text-align:left\">" + selectedRespStr + commentStr + subtext + "</td><td style=\"padding:4px\">" + avPlaybackStr + "</td></tr>\n");
            else
                sb.append( "<tr " + style + "><td style=\"width:20%;font-size:11pt\">" + lastname + "</td><td style=\"width:7%;text-align:center;font-size:11pt\">" + scoreStr + "</td><td style=\"width:73%;font-size:11pt;text-align:left\">" + selectedRespStr + commentStr + subtext + "</td></tr>\n");
            // sb.append( "<tr " + style + "><td style=\"width:20%;font-size:11pt\">" + rating.getUser().getLastName() + "</td><td style=\"width:7%;text-align:center;font-size:11pt\">" + scoreStr + "</td><td style=\"width:73%;font-size:11pt;text-align:left\">" + selectedRespStr + commentStr + subtext + "</td></tr>\n");
        }
        
        sb.append( "</table>\n" );
        return sb.toString();
        
    }
    
    protected String getRcItemHistogram( RcCompetencyWrapper rcw, RcItemWrapper rciw)
    {
        RcHistogram h = new RcHistogram( rc.getRcOrgPrefs()!=null ? rc.getRcOrgPrefs().getOtherRoleTypeNames(rc.getRcSuborgPrefs()) : new String[0], rc.getRcScript().getRcRatingScaleType() );
        int scrDigits = report.getIntParam2() >= 0 ? report.getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;        
        
        h.init(rc, rcw, rciw, scrDigits);
        
        if( !h.getHasData() )
            return "";
        
        StringBuilder sb = new StringBuilder();        
        sb.append( "<table cellpadding=\"2\" cellspacing=\"0\" style=\"background-color:white;margin-left:0;border:1px solid darkgrey;width:100%;font-size:11pt\">\n" );        
        
        int totalBarWid = 200;
        int barWid = 0;
        for( RcHistogramRow row : h.getRowList() )
        {
            // barWid = (int) (((float)totalBarWid) *(Math.abs(row.getAvgScore())/10f));
            barWid = 0; // (int) (((float)totalBarWid) *(Math.abs(row.getAvgScore())/(rc.getRcScript().getRcRatingScaleType().getMaxScore())));
            
            if( Math.abs(row.getAvgScore())>rc.getRcScript().getRcRatingScaleType().getMaxScore() )
                barWid=totalBarWid;
            
            // below min and not allowed.
            else if( row.getHistogramRoleTypeId()!=40 && row.getHistogramRoleTypeId()!=19 && ( row.getAvgScore()<0 || Math.abs(row.getAvgScore())<rc.getRcScript().getRcRatingScaleType().getMinScore()) )
                barWid=0;
            else
                barWid = (int) (((float)totalBarWid) *(Math.abs(row.getAvgScore())/(rc.getRcScript().getRcRatingScaleType().getMaxScore())));
            
            sb.append( "<tr><td style=\"width:26%\">" + lmsg(row.getLangKey() + ".count", new String[]{Integer.toString( row.getCount()), row.getOtherNameIfPresent()}) + "</td><td style=\"width:7%;text-align:center\">" + I18nUtils.getFormattedNumber(locale, row.getAvgScore(), scrDigits) + "</td><td style=\"width:67%\">" ); 
            sb.append("<div style=\"width:200px;height:20px;position:relative;display:block;padding-top:1px;padding-bottom:1px\">" );

                sb.append( "<div style=\"width:" + barWid + "px;height:18px;background-color:" + row.getRgbColor() + "\"></div>");
            
            sb.append("</div>" );            
            sb.append("</td></tr>\n" );
        }
        
        sb.append( "</table>\n" );
        return sb.toString();
    }
    
    
    
    protected String getRowTitle( String style, String title, String datcol1, String datcol2, String datcol3)
    {
        boolean hasD1 = datcol1 != null && !datcol1.isEmpty();
        boolean hasD2 = datcol2 != null && !datcol2.isEmpty();
        boolean hasD3 = datcol3 != null && !datcol3.isEmpty();

        // No data Columns,
        // title - 5
        if( !hasD1 && !hasD2 && !hasD3 )
            return "<tr " + style + "><td colspan=\"7\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td></tr>\n";

        // Missing Col 3
        // title - 2
        // d1 - 1
        // d2 - 1
        // blank 1
        else if( !hasD3 )
        {
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td><td style=\"font-weight:bold;vertical-align:top\" colspan=\"1\">" + (hasD1 ? datcol1 : "") + "</td><td colspan=\"4\" style=\"font-weight:bold;vertical-align:top\">" + (hasD2 ? datcol2 : "") + "</td></tr>\n";
        }

        // All
        // title - 2
        // d1 - 1
        // d2 - 1
        // d3 - 1
        else
        {
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td><td style=\"font-weight:bold;vertical-align:top\">" + (hasD1 ? datcol1 : "") + "</td><td style=\"font-weight:bold;vertical-align:top\">" + (hasD2 ? datcol2 : "") + "</td><td colspan=\"3\" style=\"font-weight:bold;vertical-align:top\">" + (hasD3 ? datcol3 : "") + "</td></tr>\n";
        }
    }
    
    protected String getRowTitleSubtitle( String style, String title, String subtitle )
    {
        boolean hasSubtitle = subtitle != null && !subtitle.isEmpty();

        if( !hasSubtitle )
            return "<tr " + style + "><td colspan=\"7\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td></tr>\n";

        else
            return "<tr " + style + "><td colspan=\"2\" style=\"font-weight:bold;vertical-align:top\">" + title + "</td>"
                    + "<td style=\"font-weight:bold;vertical-align:top\" colspan=\"5\">" + subtitle + "</td></tr>\n";
    }




    protected String getRow( String style, String value, boolean bold )
    {
        return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + " colspan=\"6\">" + value + "</td></tr>\n";
    }

    protected String getRow( String style, String label, String value, boolean bold )
    {
        return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td colspan=\"5\" " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td></tr>\n";
    }

    protected String getRow( String style, String label, String value, String value2, boolean bold  )
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td><td colspan=\"4\" " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value2 + "</td></tr>\n";
    }

    protected String getRow( String style, List<String> caveats  )
    {         
         if( caveats ==null || caveats.isEmpty() )
             return "";

        String cavs = "<ul>\n";

        for( String c : caveats )
            cavs += "<li style=\"font-weight:normal;vertical-align:top\">" + c + "</li>\n";

        cavs += "</ul>\n";
        return "<tr " + style + "><td></td><td colspan=\"6\" style=\"font-weight:normal;vertical-align:top\"" + ">" + cavs + "</td></tr>\n";             
    }
    
    
    protected String getRow( String style, String label, String value, String value2, String value3, boolean bold  )
    {
         return "<tr " + style + "><td style=\"width:20px\"></td><td " + ( bold ? "style=\"font-weight:bold;vertical-align:top\"" : "style=\"font-weight:normal;vertical-align:top\"" ) + ">" + label + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value + "</td><td " + ( bold ? "style=\"font-weight:bold\"" : "" ) + ">" + value2 + "</td><td colspan=\"3\"" + ( bold ? "font-weight:bold" : "" ) +  "\">" + value3 + "</td></tr>\n";
    }

    protected String getTableSpacer()
    {
        return "<br style=\"clear:both\" />\n";
    }


    protected String getRowSpacer( String style )
    {
        return "<tr " + style + "><td colspan=\"7\">&#160;</td></tr>\n";
    }

    
    
    
    
    
    
    
    protected String lmsg( String key )
    {
        return MessageFactory.getStringMessage(locale, key);
    }
    protected String lmsg( String key, String[] params )
    {
        return MessageFactory.getStringMessage(locale, key, params );
    }

    protected String getCountryName( String countryCode )
    {
        if( countryCode == null || countryCode.isEmpty() )
            countryCode = "US";

        String c = lmsg( "cntry." + countryCode );

        if( c == null || c.isEmpty() )
            return lmsg( "g.Country" );

        return c;
    }

    

    /*
    protected void loadRcCheckForResults( RcCheck rc, Locale locale ) throws Exception
    {
        if( rc==null )
            return;
        
        if( userFacade==null )
            userFacade=UserFacade.getInstance();

        if( rc.getUser()==null )
            rc.setUser( userFacade.getUser( rc.getUserId() ));
        
        if( rc.getOrg()==null )
            rc.setOrg( userFacade.getOrg( rc.getOrgId() ));
        
        if( rc.getAdminUser()==null && rc.getAdminUserId()>0 )
            rc.setAdminUser( userFacade.getUser( rc.getAdminUserId() ));
        
        // rc.setLocale( getLocale() );
        
        if( rcFacade==null )
            rcFacade = RcFacade.getInstance();
        
        if( rc.getRcOrgPrefs()==null )
            rc.setRcOrgPrefs( rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId( )));

        //if( refUserType.getIsCandidate() )
        //{
        if( rc.getRcScript()==null )
        {
            if( rcScriptFacade==null )
                rcScriptFacade = RcScriptFacade.getInstance();
            RcScript rcs = rcScriptFacade.getRcScript( rc.getRcScriptId() );
            rc.setRcScript( (RcScript)rcs.clone() );
            rcScriptFacade.loadScriptObjects(rc.getRcScript(), true );
        }
        
            
        // if( rc.getRcRaterList()==null )
        rc.setRcRaterList( rcFacade.getRcRaterList( rc.getRcCheckId() ));                

        for( RcRater r : rc.getRcRaterList() )
        {
            if( r.getUser()==null )
                r.setUser( userFacade.getUser( r.getUserId() ));

            //r.setNeedsResendEmail(false);
            //r.setNeedsResendMobile(false);

            // r.setLocale( getLocale() );                
            r.setRcRaterSourceType( RcRaterSourceType.getForRcRater(rc, r));
            
            r.setRcRatingList( rcFacade.getRcRatingList( rc.getRcCheckId(), rc.getRcRater().getRcRaterId() ) );
            rc.setRcRatingsInScript(r.getRcRatingList(), true );
        }
    }
    */
    
    protected String getHtmlTableSpacerRow( String style )
    {
        return "<tr " + style + "><td colspan=\"7\">&#160;</td></tr>\n";
    }
    
    protected String getHtmlTableStart( Locale locale )
    {
        if( locale == null )
            locale = Locale.US;
        boolean ltr = getIsLTR( locale );
        return (ltr ? "" : "<div dir=\"rtl\">") + "<table cellpadding=\"3\" cellspacing=\"0\" style=\"width:800px;margin-left:10px;font-family:arial,verdana,tahoma\" " + (ltr ? "" : "dir=\"rtl\"")  + ">\n";
    }

    protected String getHtmlTableEnd(Locale locale)
    {
        if( locale == null )
            locale = Locale.US;

        boolean ltr = getIsLTR( locale );
        return "</table>\n" +( ltr ? "" : "</dir>" );
    }

    
    
    public boolean getIsLTR( Locale locale )
    {
        return ComponentOrientation.getOrientation( locale ).isLeftToRight();
    }

    public boolean getReportRuleAsBoolean( String name )
    {
       return getReportRuleAsInt( name ) == 1;
    }    
    
    public int getReportRuleAsInt( String name )
    {
        //if(1==1)
        //    return null;
        String sv = getReportRuleAsString( name );

        if( sv == null || sv.trim().isEmpty() )
            return 0;

        try
        {
            int v = Integer.parseInt( sv );
            return v;
        }

        catch( NumberFormatException e )
        {
            LogService.logIt( e, "ReportData.getReportRuleAsInt() " + name + ", value=" + sv );
        }

        return 0;
    }
    
    public String getReportRuleAsString( String name )
    {
        if( name == null || name.isEmpty() || reportRules == null || reportRules.isEmpty() )
            return null;

        for( NVPair p : reportRules )
        {
            if( p.getName().equals( name ) )
                return (String) p.getValue();
        }

        return null;
    }


    
}
