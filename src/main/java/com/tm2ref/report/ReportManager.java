/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.report.Report;
import com.tm2ref.entity.user.Suborg;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcCheckStatusType;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.StringUtils;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class ReportManager {
    
    RcFacade rcFacade;
    
    RcCheckUtils rcCheckUtils;
    
    ReportFacade reportFacade;
    
    UserFacade userFacade;
    
    /*List of:
       data[0] = rcCheckId
       data[1] = bytes for report
       data[2] = suggested filename
       data[3] = date/time
    */
    public List<Object[]> generateReportsForRcCheckAndLanguage(  long rcCheckId, 
                                                                int reportId, 
                                                                String langStr ) throws Exception
    {
        List<Object[]> out = new ArrayList<>();
        
        try
        {
            //Date procStart = new Date();

            // LogService.logIt("ReportManager.generateReportForRcCheckAndLanguage() START creating report.  rcCheckId=" + rcCheckId + ", reportId=" + reportId + ", langStr=" + langStr   );

            if( rcFacade == null )
                rcFacade = RcFacade.getInstance();

            RcCheck rc = rcFacade.getRcCheck(rcCheckId, true );

            if( rc == null )
                throw new Exception( "RCCheck not found. rcCheckId=" + rcCheckId );

            if(  rc.getRcCheckStatusTypeId() != RcCheckStatusType.COMPLETED.getRcCheckStatusTypeId()  )
                throw new Exception( "Cannot generate report. RcCheck is in invalid status. RcCheckId=" + rcCheckId + ", status=" + rc.getRcCheckStatusType().getName() );

            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            
            rcCheckUtils.loadRcCheckForScoringOrResults(rc, false);
            
            //if( reportId <= 0 )
            //    reportId = rc.getReportId();

            //if( reportId<=0 )
            //{
            //    RcOrgPrefs rcop = rcFacade.getRcOrgPrefsForOrgId( rc.getOrgId() );
            //    if( rcop!=null )
            //        reportId = rc.getRcCheckType().getIsPrehire() ? rcop.getReportIdPrehire() : rcop.getReportIdEmployee();
            //}
            
            if( langStr==null || langStr.isBlank() )
                langStr = rc.getLangCode();
            
            if( langStr==null || langStr.isBlank() )
                langStr = "en_US";
            
            List<Integer> ridl;
            
            if( reportId>0 )
            {
                ridl = new ArrayList<>();
                ridl.add( reportId );
            }
            else
                ridl = rcCheckUtils.getReportIdsForRcCheck( rc, langStr );
            
            
            Locale locale = I18nUtils.getLocaleFromCompositeStr(langStr);

            RcCheckUtils.addCandidateRoleRespToRaterRoleRespItemResponses(rc, locale );

            RcCheckUtils.addAnonymousNames(rc, locale);
            
            rcCheckUtils.prepSuspiciusActivityForReporting( rc, locale );
            
            //String lang = locale.getLanguage();

            //LogService.logIt("ReportManager.generateReportForRcCheckAndLanguage() langStr=" + langStr + ", lang=" + lang + ", comp=" + ("DefaultRcReportPrehire" + "_" + lang)  );
            
            //if( reportId<=0 )
            //    reportId = RuntimeConstants.getIntValue( rc.getRcCheckType().getIsPrehire() ? "DefaultRcReportPrehire" + "_" + lang : "DefaultRcReportEmployee" + "_" + lang );

            //if( reportId<=0 )
            //    reportId = RuntimeConstants.getIntValue( rc.getRcCheckType().getIsPrehire() ? "DefaultRcReportPrehire" : "DefaultRcReportEmployee" );
                        
            // LogService.logIt("ReportManager.generateReportForRcCheckAndLanguage() reportId= " + reportId  );

            if( reportFacade==null )
                reportFacade = ReportFacade.getInstance();

            Report r;
            ReportData rd;
            boolean createPdfDoc;
            byte[] rptBytes;

            
            // SimJ simJ = JaxbUtils.ummarshalSimDescriptorXml( sd.getXml() );
            
            // long engEquivSimId = product.getLongParam4();
            
            // RcCheckScore tes = null;         
            
            Suborg suborg = null;            
            if( rc.getSuborgId()>0 )
            {
                if( userFacade==null )
                    userFacade = UserFacade.getInstance();
                suborg = userFacade.getSuborg( rc.getSuborgId() );
            }
            
            for( Integer rid : ridl )
            {
                r = reportFacade.getReport(rid);
                if( r == null )
                    throw new Exception( "Report not found: " + rid );            
                r = (Report) r.clone();                
                rd = new ReportData( rc, r, suborg, locale );
                r.setLocaleForReportGen( locale );
                createPdfDoc = r.getNoPdfDoc()==0;

                rptBytes = null;
                if( !createPdfDoc )
                    continue;
            
                Object[] rout = new Object[4];
                rout[0] = ((long) rcCheckId);
                
                LogService.logIt("ReportManager.generateReportsForRcCheckAndLanguage() locale=" + locale.toString() + "  rcCheckId="  + rc.getRcCheckId() + ", rcCheck.langStr=" + rc.getLangCode()  );

                // now get the report template class
                String tmpltClassname = r.getImplementationClass();

                if( !r.getReportTemplateType().getIsStandard())
                    tmpltClassname = RuntimeConstants.getStringValue( rc.getRcCheckType().getIsPrehire() ? "StandardRefCheckReportClassPrehire" : "StandardRefCheckReportClassEmployee" );

                else if( !r.getReportTemplateType().getIsCustom() )
                    tmpltClassname = r.getReportTemplateType().getImplementationClass();

                Class<ReportTemplate> tmpltClass = (Class<ReportTemplate>) Class.forName( tmpltClassname );

                Constructor ctor = tmpltClass.getDeclaredConstructor();
                ReportTemplate rt = (ReportTemplate) ctor.newInstance();
                if( rt == null )
                    throw new Exception( "Could not generate template class instance: " + tmpltClassname );

                rt.init( rd );
                if( !rt.getIsReportGenerationPossible() )
                    throw new Exception( "Report generation not possible." );

                // LogService.logIt("ReportManager.generateReportForRcCheckAndLanguage() GeneratingReport() START" );

                rptBytes = rt.generateReport();
                // LogService.logIt("ReportManager.generateReportForRcCheckAndLanguage() GeneratingReport() FINISH " + ( rptBytes == null ? "null" : rptBytes.length ) );
                rt.dispose();                

                if( rptBytes == null || rptBytes.length == 0 )                    
                    throw new Exception( "Generated report is empty. ReportId=" + r.getReportId() );                

                rout[1] = rptBytes;
                rout[2] = getReportFilename( rc, r ) + ".PDF";
                rout[3] = new Date();
                
                out.add( rout );
            }
                        
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "ReportManager.generateReportsForRcCheckAndLanguage() rcCheckId=" + rcCheckId + ", reportId=" + reportId +", langStr=" + langStr  );

            return null;
        }

    }
    
    public String getReportFilename( RcCheck rc, Report r )
    {
        String out = "";
        User user = rc.getUser();

        out = StringUtils.alphaCharsOnly( user.getLastName() );            
        out = StringUtils.removeNonAscii(out);
        out = StringUtils.removeChar( out , ' ' );
        if( out.length() > 20 )
            out = out.substring(0, 20 );

        // No Acsii in name.
        if( out.length()==0 )
            out += Long.toString( rc.getRcCheckId());

        out += "_";        
        
        String comprRptNm = StringUtils.removeChar( r.getTitle()!=null && !r.getTitle().isEmpty() ? r.getTitle() : r.getName() , ' ' );
        comprRptNm = StringUtils.alphaCharsOnly( comprRptNm );
        if( comprRptNm.length() > 35 )
            comprRptNm = comprRptNm.substring(0, 35 );

        out += comprRptNm + "_";

        Calendar c = new GregorianCalendar();
        out += c.get( Calendar.YEAR ) + "-" + ( c.get( Calendar.MONTH ) + 1 ) + "-" + c.get( Calendar.DAY_OF_MONTH );
        return out;        
    }
    
    
}
