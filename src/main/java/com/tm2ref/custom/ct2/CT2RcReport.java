/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.custom.ct2;

import com.tm2ref.global.STException;
import com.tm2ref.report.ReportTemplate;
import com.tm2ref.service.LogService;

/**
 *
 * @author miker_000
 */
public class CT2RcReport extends BaseRcReportTemplate implements ReportTemplate 
{
    public CT2RcReport()
    {
        super();
    }

    
    

    @Override
    public byte[] generateReport() throws Exception 
    {
        try
        {
            // LogService.logIt( "CTSelectionReport.generateReport() STARTING for " + reportData.getTestEvent().toString()  );
            addCoverPage(true);

            addNewPage();

            addReportInfoHeader();

            // If not for Employee, but it's an Employee Feedback Report, place top/bottom at the top.
            if( !devel && reportData.getRc().getRcCheckType().getIsEmployeeFeedback() )
            {
                addTopCompetenciesTable( true );
                addTopCompetenciesTable( false );                

                addTopItemsTable( true );
                addTopItemsTable( false );                

            }
            
            addCompetencySummaryTable();
                        
            addReferencesTable();
            
            addSuspiciousActivityTable();

            addRatingsTable();
            
            // If it is for Employee or it's a prehire, place top/bottom at the bottom of report.
            if( devel || reportData.getRc().getRcCheckType().getIsPrehire())
            {
                addTopCompetenciesTable( true );
                addTopCompetenciesTable( false );

                addTopItemsTable( true );
                addTopItemsTable( false );                
            }
            
            if( !devel )
                addInterviewGuide();
            
            addPreparationNotesSection();

            // addNewPage();

            addNotesSection();
            
            closeDoc();

            return getDocumentBytes();
        }

        catch( STException e )
        {
            throw e;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "StandardReport.CT2RcReport() " );
            throw new STException( e );
        }
    }
    
}
