/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import java.util.Locale;

/**
 *
 * @author Mike
 */
public interface ReportTemplate
{
    void init( ReportData reportData ) throws Exception;

    byte[] generateReport() throws Exception;

    void dispose() throws Exception;
    
    Locale getReportLocale();
    
    boolean getIsReportGenerationPossible();
            
}
