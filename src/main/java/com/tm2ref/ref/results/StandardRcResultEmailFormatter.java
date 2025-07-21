/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref.results;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.service.LogService;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public class StandardRcResultEmailFormatter extends BaseFormatter implements RcResultEmailFormatter {

    
    public StandardRcResultEmailFormatter( RcCheck rc )
    {
        this.rc = rc;
        //this.locale = rc.getLocale();  // Do not use this because it may be a different language depending on the last person's Locale.
        
        if( rc.getLangCode()!=null && !rc.getLangCode().isBlank() )
            locale = I18nUtils.getLocaleFromCompositeStr( rc.getLangCode() );
        if( locale==null )
           locale=rc.getLocale();
        if( locale==null )
            locale=Locale.US;
        
        rowStyleHdr = " style=\"background-color:#0077cc;vertical-align:top;color:white\"";
        rowStyle0 = " style=\"background-color:#ffffff;vertical-align:top\"";
        rowStyle1 =  " style=\"background-color:#e6e6e6;vertical-align:top\"";
        rowStyle2 = " style=\"background-color:#f3f3f3;vertical-align:top\"";                
    }
    
    @Override
    public String getResultEmailContent(RcCheck rc , Locale loc ) {
        StringBuilder sb = new StringBuilder();
        boolean tog = false;
        try
        {
            if( loc!=null )
                this.locale = loc;
            
            init();

            // 
            RcCheckUtils.correctRcRaterListForReporting( rc );
            
            RcCheckUtils.addCandidateRoleRespToRaterRoleRespItemResponses(rc, loc);
                 
            RcCheckUtils.addAnonymousNames(rc, loc);
            
            boolean complete = rc.getRcCheckStatusType().getIsComplete();
            
            
            // Header Section
            String key = complete ? (prehire ? "g.RCEmailHireProgAllComplete.fulltop" : "g.RCEmailEmpFbkProgAllComplete.fulltop") : (prehire ? "g.RCEmailHireProgAllInProg.fulltop" : "g.RCEmailEmpFbkProgAllInProg.fulltop");
            Object[] out = getStandardHeaderSection( tog, key );
            String temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }
            
            // not devel and multi-rater feedback
            if( report.getFloatParam1()!=1 && rc.getRcCheckType().getIsEmployeeFeedback() )
            {            
                out = getStandardTopCompetenciesTable( tog, true );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }

                out = getStandardTopCompetenciesTable( tog, false );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }

                out = getStandardTopItemsTable( tog, true );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }

                out = getStandardTopItemsTable( tog, false );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }
            }
            
            out = getStandardCompetencySummarySection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            // AI Section
            if( !getReportRuleAsBoolean( "skipaiscoressection" ) &&  report.getIncludeAiScores()==1 )
            {
                out = getStandardGenAISection(tog );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    // if( !isBatt )
                    sb.append( getTableSpacer() );
                }
            }

            

            out = getStandardRatersSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            out = getStandardSuspiciousActivityTable( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            
            out = getStandardRatingsByQuestionSection( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            // devel or prehire
            if( report.getFloatParam1()==1 || rc.getRcCheckType().getIsPrehire())
            {            
                out = getStandardTopCompetenciesTable( tog, true );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }

                out = getStandardTopCompetenciesTable( tog, false );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }
                
                out = getStandardTopItemsTable( tog, true );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }

                out = getStandardTopItemsTable( tog, false );
                temp = (String) out[0];
                if( !temp.isEmpty() )
                {
                    sb.append( temp );
                    tog = ( (Boolean) out[1]);
                    sb.append( getTableSpacer() );
                }                
            }


            
            out = getStandardInterviewQuestionTable( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            out = getStandardReferralsTable( tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }

            
            
            out = this.getStandardPdfReportTable(tog );
            temp = (String) out[0];
            if( !temp.isEmpty() )
            {
                sb.append( temp );
                tog = ( (Boolean) out[1]);
                sb.append( getTableSpacer() );
            }
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "StandardRcResultEmailFormatter.getResultEmailContent() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId() ) + ", content=" + sb.toString() );
            return null;
        }
        
        return sb.toString();
    }
    
    
    
    
}
