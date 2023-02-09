/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.custom.ct2;

import com.itextpdf.text.Annotation;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.ref.RcRater;
import com.tm2ref.entity.ref.RcRating;
import com.tm2ref.entity.ref.RcSuspiciousActivity;
import com.tm2ref.format.BaseReportTemplate;
import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.global.Constants;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.ref.RcCompetencyWrapper;
import com.tm2ref.ref.RcCompetencyWrapperNameComparator;
import com.tm2ref.ref.RcContactPermissionType;
import com.tm2ref.ref.RcItemWrapper;
import com.tm2ref.ref.RcRatingScaleType;
import com.tm2ref.ref.RcTopBottomSrcType;
import com.tm2ref.report.RcHistogram;
import com.tm2ref.report.RcHistogramRow;
import com.tm2ref.report.ReportData;
import com.tm2ref.report.ReportTemplate;
import com.tm2ref.report.ReportUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author miker_000
 */
public abstract class BaseRcReportTemplate extends BaseReportTemplate implements ReportTemplate
{
    @Override
    public abstract byte[] generateReport() throws Exception;
    
    @Override
    public void init(ReportData reportData) throws Exception {
       
        super.init(reportData);
    }
    
    
    
    public void addNotesSection() throws Exception
    {
        if( reportData.getReportRuleAsBoolean( "usernotesoff" ) )
            return;
                
        addTitle( currentYLevel - TPAD , lmsg("g.Notes"), lmsg( "g.NotesSubtitle" ) );
    }

    
    public void addInterviewGuide() throws Exception
    {
        try
        {
            if( !reportData.getRc().getRcCheckType().getIsPrehire()  )
                return;
            
            int q = Constants.DEFAULT_INTERVIEW_QUESTIONS;
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
                        
            List<RcCompetencyWrapper> rcl = rcCheckUtils.getRcCompetenciesForInterviewQuestions( reportData.getRc(), q );

            // LogService.logIt( "BaseRcReportTemplate.addInterviewGuide() competencies to list=" + rcl.size() );
            
            if( rcl.isEmpty() )
                return;
            
            previousYLevel =  currentYLevel;
            float y = previousYLevel;
            float thgt =0;// t!= null  t.calculateHeights();
                        
            // boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );                         
            // String rcCheckTypeName = reportData.getRc().getRcCheckType().getName( reportData.getLocale() );            
            // String ratersName = lmsg( reportData.getRc().getRcCheckType().getIsPrehire() ? "g.References" : "g.Reviewers" );
            
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { 2,6 } : new float[] { 6,2 };

                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );

            setRunDirection( t );
            // float importanceWidth = 25;

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            t.setTotalWidth( outerWid );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 0 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font theTextFont = this.font;
            Font boldFont = this.fontBold;
            Font theTextFontSm = this.font;
            

            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;            
            boolean top = true;
            Phrase ph;
            // int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            

            PdfPTable t2;
            for( RcCompetencyWrapper rcw : rcl )
            {
                // LogService.logIt( "BaseRcReportTemplate.addInterviewGuide() BBB adding=" + rcw.getRcCompetency().getName() + ", " + rcw.getRcCompetency().getInterviewQuestion() );
                ph = new Phrase( new Chunk(rcw.getRcCompetency().getName(), boldFont) );
                
                if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                    ph.add( new Chunk( "\n\n" + rcw.getRcCompetency().getDescription(),theTextFontSm ) );
                
                c = new PdfPCell( ph );                  
                if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                t2 = getInterviewQuestionTable( rcw, shade, theTextFont, theTextFontSm, outerWid );

                // response table
                c = new PdfPCell(); 
                c.addElement( t2 );
                if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);                
                                
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
                top = false;
            }
            
            thgt = t.calculateHeights();
            
            if( thgt> pageHeight )
            {
                y -= 2*TPAD;
                LogService.logIt( "BaseRcReportTemplate.addInterviewGuide() ZZZ.1 Table height is higher than page height." );
                t.setHeaderRows( 1 );
                t.setSplitLate( false );
                
            }
            if( thgt + 75 > y )
            {
                LogService.logIt( "BaseRcReportTemplate.addInterviewGuide() ZZZ.2  Too close to bottom of page, Adding new page." );
                addNewPage();
                y = currentYLevel;
            }            

            y = addTitle( y, lmsg( "g.IGSuggInterviewQuestions"),  lmsg( "g.IGSuggInterviewQuestions.P1") );
            y -= 2*TPAD;
            y = addTableToDocument( y, t, true );
            currentYLevel = y; //  - t.calculateHeights();

            
            // t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            addNewPage();
            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addInterviewGuide()" );
            throw e;
        }                                
    }
    

    public void addTopCompetenciesTable( boolean high ) throws Exception
    {
        try
        {
            int q = reportData.getRc().getTopBottomCount(); // Constants.DEFAULT_HILOWCOMPETENCIES;            
            int scoredComps = 0;
            for( RcCompetencyWrapper rcw : reportData.getRcScript().getRcCompetencyWrapperList() )
            {
                if( rcw.getHasNumericScore() )
                    scoredComps++;
            }
            
            if( scoredComps<=0 )
                return;
            
            else if( q==3 && reportData.getRc().getRcCheckType().getIsEmployeeFeedback() && scoredComps>0 && scoredComps<q*2 )
                q = scoredComps/2;
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            
            List<RcCompetencyWrapper> rcl = high ? rcCheckUtils.getHighScoringRcCompetencies(reportData.getRc(), q ) : rcCheckUtils.getLowScoringRcCompetencies(reportData.getRc(), q );

            //LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() high=" + high + ", scoredComps=" + scoredComps + ", found comps=" + rcl.size() );            
            
            if( rcl.isEmpty() )
                return;            
            
            String topBotSrcTypeName = RcTopBottomSrcType.getValue( reportData.getRc().getTopBottomSrcTypeId()).getName(reportData.getLocale()).toLowerCase();
            
            String titleKey = high ? "g.RSCompetenciesHigh" : "g.RSCompetenciesLow"; // ) + (devel ? ".your" : "" );            
            if( reportData.getIsEmployee() )
            {   
                titleKey += "360";
                if( devel )
                    titleKey += ".your";
                else if( reportData.getRc().getIsSelfOnly() )
                    titleKey+="SO";
            }
            else if( reportData.getRc().getIsSelfOnly() )
                titleKey+="SO";
            
            
            // LogService.logIt( "BaseRcReportTemplate.addInterviewGuide() competencies to list=" + rcl.size() );
            
            float thgt =0;// t!= null  t.calculateHeights();
                        
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { 3,7 } : new float[] { 7,3 };

                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );
            setRunDirection( t );
            // float importanceWidth = 25;

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            t.setTotalWidth( outerWid );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 0 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font theTextFont = this.font;
            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;            
            boolean top = true;
            boolean bottom = false;
            //Phrase ph;
            // int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            

            PdfPTable t2;
            RcCompetencyWrapper rcw;
            ListIterator<RcCompetencyWrapper> iter = rcl.listIterator();
            
            String devSuggStr = null;
            boolean hasDevSug;
            
            while( iter.hasNext())
            {
                rcw = iter.next();
                
                t2 = getTopCompetenciesBarChartTable(rcw, high, shade);                

                //LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() BBB.1 adding=" + rcw.getRcCompetency().getName() + ", t2=" + (t2==null ? "null" : "not null" ) );
                if( t2==null )
                    continue;
                
                bottom = !iter.hasNext();
                
                devSuggStr = rcw.getRcCompetency().getDevelopmentSuggestions();
                hasDevSug = devSuggStr!=null && !devSuggStr.isBlank();
                
                //LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() BBB.2 adding=" + rcw.getRcCompetency().getName() + ", bottom=" + bottom );
                
                c = new PdfPCell( new Phrase( rcw.getRcCompetency().getName(), theTextFont) );                  
                if( top && bottom && !hasDevSug )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM );
                else if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP : Rectangle.RIGHT | Rectangle.TOP );
                else if( bottom && !hasDevSug )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );                
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);


                // response table
                c = new PdfPCell(); 
                c.addElement( t2 );
                if( top && bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM );
                else if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP : Rectangle.LEFT | Rectangle.TOP );
                else if( bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );                
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);                
                        
                if(  hasDevSug )
                {
                    c = new PdfPCell( new Phrase( lmsg( "g.RSDevSuggsX", new String[]{devSuggStr} ), theTextFont) );                  
                    if( bottom )
                        c.setBorder( Rectangle.LEFT | Rectangle.RIGHT | Rectangle.BOTTOM );
                    else
                        c.setBorder( Rectangle.LEFT | Rectangle.RIGHT );     
                    c.setColspan( 2 );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 4 );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);                    
                }
                
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
                top = false;
            }
            
            previousYLevel =  currentYLevel;
            float y = previousYLevel - TPAD;
            thgt = t.calculateHeights();
            
            if( thgt + 75 > y )
            {
                LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() ZZZ.2  Too close to bottom of page, Adding new page. high=" + high + ", thgt=" + thgt + ", y=" + y );
                addNewPage();
                y = currentYLevel;
            }            

            // LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() BBB.3 thgt=" +thgt + ", y=" + y );
            
            y = addTitle( y, lmsg(titleKey, new String[]{topBotSrcTypeName}),  null );
            y -= TPAD;
            // LogService.logIt( "BaseRcReportTemplate.addTopCompetenciesTable() BBB.4 y=" + y );
            
            y = addTableToDocument( y, t, true );
            currentYLevel = y; 
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addTopCompetenciesTable() high=" + high );
            throw e;
        }                                        
    }
    

    public void addTopItemsTable( boolean high ) throws Exception
    {
        try
        {
            if( !reportData.getReportRuleAsBoolean( "rcitemtopbottomtable" ) )
                return;
            
            int q = reportData.getRc().getTopBottomCount(); // Constants.DEFAULT_HILOWCOMPETENCIES;            
            int scoredItems = 0;
            for( RcCompetencyWrapper rcw : reportData.getRcScript().getRcCompetencyWrapperList() )
            {
                for( RcItemWrapper rciw : rcw.getRcItemWrapperList() )
                {
                    if( rciw.getHasRatingInfoToShow() )
                        scoredItems++;
                }
            }
            
            if( scoredItems<=0 )
                return;
            
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            
            List<RcItemWrapper> rcl = high ? rcCheckUtils.getHighScoringRcItems(reportData.getRc(), q ) : rcCheckUtils.getLowScoringRcItems(reportData.getRc(), q );

            //LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() high=" + high + ", scoredComps=" + scoredComps + ", found comps=" + rcl.size() );            
            
            if( rcl.isEmpty() )
                return;            
            
            String topBotSrcTypeName = RcTopBottomSrcType.getValue( reportData.getRc().getTopBottomSrcTypeId()).getName(reportData.getLocale()).toLowerCase();
            
            String titleKey = high ? "g.RSItemsHigh" : "g.RSItemsLow"; // ) + (devel ? ".your" : "" );            
            if( reportData.getIsEmployee() )
            {   
                titleKey += "360";
                if( devel )
                    titleKey += ".your";
                else if( reportData.getRc().getIsSelfOnly() )
                    titleKey+="SO";
            }
            else if( reportData.getRc().getIsSelfOnly() )
                titleKey+="SO";
            
            
            // LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() competencies to list=" + rcl.size() );
            
            float thgt =0;
                        
            int cols = 2;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { 3,7 } : new float[] { 7,3 };

                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );
            setRunDirection( t );

            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;

            t.setTotalWidth( outerWid );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 0 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font theTextFont = this.font;
            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;            
            boolean top = true;
            boolean bottom;

            PdfPTable t2;
            RcItemWrapper rcw;
            ListIterator<RcItemWrapper> iter = rcl.listIterator();
            
            
            while( iter.hasNext())
            {
                rcw = iter.next();
                
                t2 = getTopItemsBarChartTable(rcw, high, shade);                

                //LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() BBB.1 adding=" + rcw.getRcCompetency().getName() + ", t2=" + (t2==null ? "null" : "not null" ) );
                if( t2==null )
                    continue;
                
                bottom = !iter.hasNext();
                
                //LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() BBB.2 adding=" + rcw.getRcCompetency().getName() + ", bottom=" + bottom );
                
                c = new PdfPCell( new Phrase( rcw.getQuestionWithSubs(), theTextFont) );                  

                if( top && bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM );
                else if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.TOP : Rectangle.RIGHT | Rectangle.TOP );
                else if( bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );                

                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);


                // response table
                c = new PdfPCell(); 
                c.addElement( t2 );
                if( top && bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.TOP | Rectangle.BOTTOM );
                else if( top )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.TOP : Rectangle.LEFT | Rectangle.TOP );
                else if( bottom )
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );                
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 4 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);                
                                        
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
                top = false;
            }
            
            previousYLevel =  currentYLevel;
            float y = previousYLevel - TPAD;
            thgt = t.calculateHeights();
            
            if( thgt + 75 > y )
            {
                // LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() ZZZ.2  Too close to bottom of page, Adding new page. high=" + high + ", thgt=" + thgt + ", y=" + y );
                addNewPage();
                y = currentYLevel;
            }            

            // LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() BBB.3 thgt=" +thgt + ", y=" + y + ", adding title: " + lmsg(titleKey, new String[]{topBotSrcTypeName}) );
            
            y = addTitle( y, lmsg(titleKey, new String[]{topBotSrcTypeName}),  null );
            y -= TPAD;
            
            // LogService.logIt( "BaseRcReportTemplate.addTopItemsTable() BBB.4 y=" + y );
            
            y = addTableToDocument( y, t, true );
            y -= TPAD;
            currentYLevel = y; 
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addTopItemsTable() high=" + high );
            throw e;
        }                                        
    }
    

    
    
    public PdfPTable getInterviewQuestionTable( RcCompetencyWrapper rcw, BaseColor shade, Font theTextFont, Font theTextFontSm, float outerWid ) throws Exception
    {
        // LogService.logIt( "BaseRcReportTemplate.getInterviewQuestionTable() AAA rcw is " + (rcw==null ? "null" : " not null, rcCompetencyId=" + rcw.getRcCompetencyId()) );
        
        // next, the interview guide. A 15 column table!
        PdfPTable igT = new PdfPTable( 15 );
        igT.setTotalWidth(0.9f * outerWid*5.5f/9f );
        igT.setLockedWidth(true);
        igT.setHorizontalAlignment( Element.ALIGN_CENTER );
        setRunDirection( igT );

        PdfPCell c = igT.getDefaultCell();
        c.setBackgroundColor( shade );
        c.setBorder( Rectangle.NO_BORDER );
        c.setPadding( 2 );
        setRunDirection( c );

        Phrase ep = new Phrase( "", theTextFontSm );

        // ROW 1 - the question
        c = new PdfPCell( new Phrase( rcw.getRcCompetency().getInterviewQuestion(), theTextFontSm ) );
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setColspan( 15 );
        c.setPadding(2);
        c.setPaddingBottom( 10 );
        setRunDirection( c );
        igT.addCell(c);

        // Row 2 - Color Dots (stars)
        igT.addCell(ep);
        c =  new PdfPCell( interviewStar );
        // c.addElement(redDot);
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setHorizontalAlignment( Element.ALIGN_CENTER );
        c.setPadding( 1 );
        setRunDirection( c );
        igT.addCell( c );
        igT.addCell(ep);

        igT.addCell(ep);
        c =  new PdfPCell( interviewStar );
        // c.addElement( redYellowDot );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_CENTER );
        c.setBackgroundColor( shade );
        c.setPadding( 1 );
        setRunDirection( c );
        igT.addCell( c );
        igT.addCell(ep);

        igT.addCell(ep);
        c =  new PdfPCell( interviewStar );
        // c.addElement( yellowDot );
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setHorizontalAlignment( Element.ALIGN_CENTER );
        c.setPadding( 1 );
        setRunDirection( c );
        igT.addCell( c );
        igT.addCell(ep);

        igT.addCell(ep);
        c =  new PdfPCell( interviewStar );
        // c.addElement( yellowGreenDot );
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setHorizontalAlignment( Element.ALIGN_CENTER );
        c.setPadding( 1 );
        setRunDirection( c );
        igT.addCell( c );
        igT.addCell(ep);

        igT.addCell(ep);
        c =  new PdfPCell( interviewStar );
        // c.addElement( greenDot );
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setHorizontalAlignment( Element.ALIGN_CENTER );
        c.setPadding( 1 );
        setRunDirection( c );
        igT.addCell( c );
        igT.addCell(ep);

        // ROW 3 - numbers
        c = igT.getDefaultCell();
        c.setHorizontalAlignment( Element.ALIGN_CENTER);
        c.setBorder( Rectangle.NO_BORDER );
        c.setBackgroundColor( shade );
        c.setPadding( 0 );
        setRunDirection( c );

        igT.addCell(ep);
        igT.addCell( new Phrase( "1", theTextFontSm ) );
        igT.addCell(ep);
        igT.addCell(ep);
        igT.addCell( new Phrase( "2", theTextFontSm ) );
        igT.addCell(ep);
        igT.addCell(ep);
        igT.addCell( new Phrase( "3", theTextFontSm ) );
        igT.addCell(ep);
        igT.addCell(ep);
        igT.addCell( new Phrase( "4", theTextFontSm ) );
        igT.addCell(ep);
        igT.addCell(ep);
        igT.addCell( new Phrase( "5", theTextFontSm ) );
        igT.addCell(ep);

        c = igT.getDefaultCell();
        c.setHorizontalAlignment( Element.ALIGN_LEFT);
        c.setPadding( 2 );
        c.setPaddingBottom( 10 );
        setRunDirection( c );


        // row 4 - anchors
        c = new PdfPCell( new Phrase( rcw.getRcCompetency().getAnchorLow(), theTextFontSm ) );
        c.setBackgroundColor( shade );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_LEFT);
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setColspan( 6 );
        c.setPaddingBottom( 10 );
        setRunDirection( c );
        igT.addCell(c);
        igT.addCell(ep);

        c = new PdfPCell( new Phrase( "", theTextFontSm ) );
        c.setBackgroundColor( shade );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_RIGHT);
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setColspan( 1 );
        c.setPaddingBottom( 10 );
        setRunDirection( c );
        igT.addCell(c);
        igT.addCell(ep);

        c = new PdfPCell( new Phrase( rcw.getRcCompetency().getAnchorHi(), theTextFontSm ) );
        c.setBackgroundColor( shade );
        c.setBorder( Rectangle.NO_BORDER );
        c.setHorizontalAlignment( Element.ALIGN_RIGHT );
        c.setVerticalAlignment( Element.ALIGN_TOP );
        c.setColspan( 6 );
        c.setPaddingBottom( 10 );
        setRunDirection( c );
        igT.addCell(c);
        
        return igT;        
        
    }



    
    public void addRatingsTable() throws Exception
    {
        try
        {
            if( reportData.getRc().getRcRaterList()==null || reportData.getRc().getRcRaterList().isEmpty() )
                return;
            
            previousYLevel =  currentYLevel;
            float y = previousYLevel;
            float thgt =0;// t!= null  t.calculateHeights();
                
            boolean histogramAtItemLevel = reportData.getReportRuleAsBoolean("rchistogramitemlevel");
            // histogramAtItemLevel = true;
            // boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );                         
            // String rcCheckTypeName = reportData.getRc().getRcCheckType().getName( reportData.getLocale() );            
            // String ratersName = lmsg( reportData.getRc().getRcCheckType().getIsPrehire() ? "g.References" : "g.Reviewers" );
            
            int cols = 3;
            float[] colRelWids = reportData.getIsLTR() ?  new float[] { 3,1,6 } : new float[] { 6,1,3 };

                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );

            setRunDirection( t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font theTextFont = this.font;
            Font boldFont = this.fontBold;
            
            c = new PdfPCell( new Phrase( lmsg(  "g.Question" ), fontLargeWhiteBold ) );            
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg(  "g.Score" ), fontLargeWhiteBold ) );            
            c.setBorder( Rectangle.TOP  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            
            c = new PdfPCell( new Phrase( lmsg(  "g.ResponseDetails" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;            
            boolean bottom = false;
            int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            
            
            String questionStr;
            if( rcCheckUtils==null )
                rcCheckUtils = new RcCheckUtils();
            String scoreStr;
            RcItem item;
            //Chunk chk;
            Phrase ph;
            RcCompetencyWrapper rcw;
            RcItemWrapper rciw;
            PdfPTable t2;
            PdfPTable t3;
            
            //LogService.logIt( "BaseRcReportTemplate.addRatingsTable() AAA competenciess=" + reportData.getRc().getRcScript().getRcCompetencyWrapperList().size() );
            //long candidateRaterId = reportData.getRc().getCandidateRcRaterId();
            //List<Long> rcRaterIdsToSkip = null;        
            //if( candidateRaterId>0 )
            //{
            //    rcRaterIdsToSkip = new ArrayList<>();
            //    rcRaterIdsToSkip.add(candidateRaterId);
            //}
            
            
            ListIterator<RcCompetencyWrapper> iter = reportData.getRc().getRcScript().getRcCompetencyWrapperList().listIterator();
            ListIterator<RcItemWrapper> qiter;
            int prevRcCompetencyId = 0;
            boolean includeHistogram = false;
            int scoredCt = 0;
            boolean histogramComplete;
            
            while( iter.hasNext() )
            {
                rcw = iter.next();
                histogramComplete = false;
                
                scoredCt = 0;
                if( rcw.getHasRatingInfoToShow() && rcw.getHasNumericScore() && rcw.getRcItemWrapperList()!=null && rcw.getRcItemWrapperList().size()>1 && rcw.getScoreAvgNoCandidate()>0 )
                {
                    scoredCt = 0;
                    for( RcItemWrapper w : rcw.getRcItemWrapperList() )
                    {
                        // not scored? Don't count it.
                        if( w.getHasRatingInfoToShow() && w.getScoreAvgNoCandidate()>0 )
                            scoredCt++;
                    }
                    if( scoredCt>1 )
                    {
                        // Add an overall row
                        scoreStr = I18nUtils.getFormattedNumber(reportData.getLocale(), rcw.getScoreAvgNoCandidate(), scrDigits );

                        if( histogramAtItemLevel )
                            t3 = null;            
                        else
                        {
                            t3 = getHistogramTable(rcw, null);
                            histogramComplete = true;
                        }
                                               
                        // question
                        ph = new Phrase();
                        ph.add(  new Chunk( rcw.getRcCompetency().getName() + " (" + lmsg( "g.overall" ) + ")", boldFont ) );                         
                        if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                            ph.add( new Chunk( "\n\n" + rcw.getRcCompetency().getDescription(), theTextFont ) );
                        
                        c = new PdfPCell( ph );                  
                        if( bottom )
                            c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                        else
                            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c);

                        // score
                        c = new PdfPCell( new Phrase( scoreStr, theTextFont) );                  
                        if( bottom )
                            c.setBorder( Rectangle.BOTTOM   );
                        else
                            c.setBorder( Rectangle.NO_BORDER   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c);

                        // blank or Histogram
                        c = new PdfPCell(); 
                        c.addElement( t3==null ? new Phrase("", theTextFont): t3 );
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c); 
                        
                        tog = !tog;
                        shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                          
                    }                
                }
                
                
                //LogService.logIt( "BaseRcReportTemplate.addRatingsTable() BBB rcw=" + rcw.getRcCompetency().getName() + ", items=" + rcw.getRcItemWrapperList().size() );
                qiter = rcw.getRcItemWrapperList().listIterator();
                while( qiter.hasNext() )
                {
                    rciw = qiter.next();                    
                    if( !rciw.getHasRatingInfoToShow() )
                        continue;
                    
                    includeHistogram = histogramAtItemLevel || ( rcw.getRcCompetencyId()!=prevRcCompetencyId && !histogramComplete );
                    prevRcCompetencyId = rcw.getRcCompetencyId();

                    //LogService.logIt( "BaseRcReportTemplate.addRatingsTable() CCC Adding Row for itemId="+ rciw.getRcItemId() + ", rciw.question=" + rciw.getRcItem().getQuestion() );
                    t2 = getItemResponseTable( rcw, rciw );                    
                    t3 = includeHistogram ? ( histogramAtItemLevel ? getHistogramTable(null, rciw) : getHistogramTable(rcw, null) ): null;
                    
                    bottom = (!devel || !rciw.getHasCommentsToShow()) && t3==null && !iter.hasNext() && !qiter.hasNext();
                    
                    item = rciw.getRcItem();
                    scoreStr = item.getIsItemScored() ? I18nUtils.getFormattedNumber( reportData.getLocale(), rciw.getScoreAvgNoCandidate(), scrDigits ) : "";

                    questionStr = item.getQuestion();
                    questionStr = rcCheckUtils.performSubstitutions( questionStr, reportData.getRc(), null, reportData.getLocale() );

                    ph = new Phrase();
                    ph.add(  new Chunk( rcw.getRcCompetency().getName() + ": ", boldFont ) );                    
                    ph.add(  new Chunk( questionStr, theTextFont ) );
                    if( scoredCt<=1 && rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                        ph.add(  new Chunk( "\n\n" + rcw.getRcCompetency().getDescription(), theTextFont ) );
                    // sb.append( "<tr " + style + "><td style=\"width:35%\"><b>" + rcw.getRcCompetency().getName() + ":</b> " + StringUtils.replaceStandardEntities( item.getQuestion() ) + "</td><td style=\"width:8%;text-align:center\">" + scoreStr  + "</td><td style=\"width:57%;text-align:center\">" + getRaterResponseTableForItem( rciw ) + "</td></tr>\n" );            

                    // question
                    c = new PdfPCell( ph );                  
                    if( bottom )
                        c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);
                    
                    // score
                    c = new PdfPCell( new Phrase( scoreStr, theTextFont) );                  
                    if( bottom )
                        c.setBorder( Rectangle.BOTTOM   );
                    else
                        c.setBorder( Rectangle.NO_BORDER   );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);
                    
                    // response table
                    c = new PdfPCell(); 
                    c.addElement( t3==null ? (devel || t2==null ? new Phrase("", theTextFont) : t2 ): t3 );
                    if( bottom )
                        c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);
                    
                    if( !devel && t3!=null && t2!=null )
                    {
                        bottom = !iter.hasNext() && !qiter.hasNext();

                        // question
                        c = new PdfPCell( new Phrase("", theTextFont) );                  
                        if( bottom )
                            c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                        else
                            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c);

                        // score
                        c = new PdfPCell( new Phrase( "", theTextFont) );                  
                        if( bottom )
                            c.setBorder( Rectangle.BOTTOM   );
                        else
                            c.setBorder( Rectangle.NO_BORDER   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_LEFT );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c);

                        // response table
                        c = new PdfPCell(); 
                        c.addElement( t2 );
                        if( bottom )
                            c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                        else
                            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                        c.setBorderWidth( scoreBoxBorderWidth );
                        c.setPadding( 4 );
                        c.setPaddingBottom( 5 );
                        c.setHorizontalAlignment( Element.ALIGN_CENTER );
                        c.setBackgroundColor( shade );
                        setRunDirection( c );
                        t.addCell(c);
                    }

                    if( devel || t2==null )
                    {
                        bottom = !iter.hasNext() && !qiter.hasNext();
                        t2 = getDevelCommentTable( rciw );                        
                        if( t2!=null )
                        {
                            // comments only response table
                            c = new PdfPCell(); 
                            c.addElement( t2 );
                            c.setColspan( 3 );
                            if( bottom )
                                c.setBorder( Rectangle.BOTTOM | Rectangle.RIGHT | Rectangle.LEFT  );
                            else
                                c.setBorder( Rectangle.RIGHT | Rectangle.LEFT   );
                            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                            c.setBorderWidth( scoreBoxBorderWidth );
                            c.setPadding( 4 );
                            c.setPaddingBottom( 5 );
                            c.setHorizontalAlignment( Element.ALIGN_CENTER );
                            c.setBackgroundColor( shade );
                            setRunDirection( c );
                            t.addCell(c);
                        }
                    }

                    
                    tog = !tog;
                    shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
                } 
                
            }
            thgt = t.calculateHeights();
            
            if( thgt> pageHeight )
            {
                y -= 2*TPAD;
                LogService.logIt( "BaseRcReportTemplate.addRatingsTable() ZZZ.1 Table height is higher than page height." );
                t.setHeaderRows( 1 );
                t.setSplitLate( false );                

                if( y<120 )
                {
                    LogService.logIt( "BaseRcReportTemplate.addRatingsTable() ZZZ.1B  Too close to bottom of page, Adding new page." );
                    addNewPage();
                    y = currentYLevel;
                }           
            }

            else if( thgt + 75 > y )
            {
                LogService.logIt( "BaseRcReportTemplate.addRatingsTable() ZZZ.2  Too close to bottom of page, Adding new page." );
                addNewPage();
                y = currentYLevel;
            }            

            String subTitleKey = devel ? "g.RSRatingsByQuesSubDevel" : ( reportData.getRc().getRcCheckType().getIsPrehire() ?  null : null );
            
            y = addTitle( y, lmsg( "g.RSRatingsByQuestion"), subTitleKey==null ? null : lmsg( subTitleKey ) );
            y -= 2*TPAD;
            y = addTableToDocument( y, t, true );
            
            // t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y; //  - t.calculateHeights();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addRatingsTable()" );
            throw e;
        }                                
    }
    
    
    
    public PdfPTable getHistogramTable( RcCompetencyWrapper rcw, RcItemWrapper rciw) throws Exception
    {
        int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;        
        
        // LogService.logIt( "BaseRcReportTemplate.getHistogramTable() AAA rciw is " + (rciw==null ? "null" : " not null, itemId=" + rciw.getRcItemId() + ", ratings.size=" + (rciw.getRcRatingList()==null ? "null" : "not null " + rciw.getRcRatingList().size()) ) );
        RcHistogram h = new RcHistogram( reportData.getRc().getRcOrgPrefs().getOtherRoleTypeNames(reportData.getRc().getRcSuborgPrefs()), reportData.getRc().getRcScript().getRcRatingScaleType() ); 
        h.init(reportData.getRc(), rcw, rciw, scrDigits);        
              
        if( !h.getHasData() )
            return null;
                
        
        float[] colRelWids = reportData.getIsLTR() ?  new float[] { 25,7,66 } : new float[] { 66,7,25 };
        // float[] colRelWids = reportData.getIsLTR() ?  new float[] { 3,1,6 } : new float[] { 6,1,3 };

        // First create the table
        PdfPCell c;
        PdfPTable t = new PdfPTable( 3 );
        setRunDirection( t );
        t.setWidths( colRelWids );
        t.setWidthPercentage( 100 );
        //t.setLockedWidth( true );
        t.setHeaderRows( 0 );

        c = t.getDefaultCell();
        c.setPadding( 0 );
        c.setBorder( Rectangle.NO_BORDER );
        setRunDirection( c );        

        if( rciw!=null && ( rciw.getRcRatingList()==null || rciw.getRcRatingList().isEmpty() ) )
            return t;
        
        boolean bottom = false;
        RcHistogramRow row;
        for( int i=0;i<h.getRowList().size();i++ )
        {
            row = h.getRowList().get(i);
            bottom = i==h.getRowList().size()-1;
            
            c = new PdfPCell( new Phrase( lmsg(row.getLangKey() + ".count", new String[]{Integer.toString( row.getCount()), row.getOtherNameIfPresent()}), getFont() ) );                  
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 2 );
            c.setPaddingBottom( bottom ? 8 : 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c); 
            
            c = new PdfPCell( new Phrase( I18nUtils.getFormattedNumber(reportData.getLocale(), row.getAvgScore(), scrDigits), getFont() ) );                  
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 2 );
            c.setPaddingBottom( bottom ? 8 : 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setBackgroundColor( BaseColor.WHITE );
            setRunDirection( c );
            t.addCell(c); 

            c = new PdfPCell( new Phrase( " ", this.getFontWhite() ) );
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 1 );
            c.setPaddingBottom( bottom ? 3 : 1 );
            c.setFixedHeight(14);
            c.setBackgroundColor( BaseColor.WHITE );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setCellEvent( new HistogramBarCellEvent( row ) );
            setRunDirection( c );
            t.addCell(c);
        }
        
        return t;        
    }
    
    public PdfPTable getTopItemsBarChartTable(RcItemWrapper rciw, boolean high, BaseColor shade) throws Exception
    {
        float scoreAll = rciw.getAverageScore(null);
        float scoreOthers = rciw.getScoreAvgNoCandidate();
        float scoreSelf = rciw.getScoreCandidate( reportData.getRc().getCandidateRcRaterId() );

        return getTopBarChartTable( scoreAll, scoreOthers, scoreSelf, high, shade);
    }

    public PdfPTable getTopCompetenciesBarChartTable( RcCompetencyWrapper rcw, boolean high, BaseColor shade) throws Exception
    {
        float scoreAll = rcw.getAverageScore(null);
        float scoreOthers = rcw.getScoreAvgNoCandidate();
        float scoreSelf = rcw.getAvgScoreCandidate( reportData.getRc().getCandidateRcRaterId() );
        
        return getTopBarChartTable( scoreAll, scoreOthers, scoreSelf, high, shade);        
    }
    

    
    private PdfPTable getTopBarChartTable( float scoreAll, float scoreOthers, float scoreSelf,boolean high, BaseColor shade) throws Exception
    {
        
        // LogService.logIt( "BaseRcReportTemplate.getTopCompetenciesBarChartTable() AAA rciw is " + (rciw==null ? "null" : " not null, itemId=" + rciw.getRcItemId() + ", ratings.size=" + (rciw.getRcRatingList()==null ? "null" : "not null " + rciw.getRcRatingList().size()) ) );        
        float[] colRelWids = reportData.getIsLTR() ?  new float[] { 1,5 } : new float[] { 5,1 };

        // First create the table
        PdfPCell c;
        PdfPTable t = new PdfPTable( colRelWids );
        setRunDirection( t );
        t.setWidthPercentage( 100 );
        //t.setLockedWidth( true );
        t.setHeaderRows( 0 );

        c = t.getDefaultCell();
        c.setPadding( 0 );
        c.setBorder( Rectangle.NO_BORDER );
        setRunDirection( c );        

        int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;        

        boolean isSelfOnly =  reportData.getRc().getIsSelfOnly();         
        
        Font smallFont = getFontSmall();
        BaseColor color;

        String srcKey = "g.RSHiLoOthers";            
        float scoreToUse = scoreOthers;

        RcTopBottomSrcType srcTyp = RcTopBottomSrcType.getValue( reportData.getRc().getTopBottomSrcTypeId() );
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
            
            c = new PdfPCell( new Phrase( lmsg(srcKey), smallFont  ) );
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 1 );
            c.setPaddingRight(3 );
            c.setBackgroundColor( shade );
            c.setFixedHeight(14);
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            setRunDirection( c );
            t.addCell(c);            
            
            color = getCompBarRgbColor( scoreToUse, true );
            c = new PdfPCell( new Phrase( "" ) );
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 1 );
            c.setPaddingRight(3);
            c.setBackgroundColor( shade );
            c.setFixedHeight(14);
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setCellEvent( new RcHiLoCompetencyCellEvent( I18nUtils.getFormattedNumber( reportData.getLocale(), scoreToUse, scrDigits), scoreToUse, color , getBaseFontCalibri() ) );
            setRunDirection( c );
            t.addCell(c);            
        }
        
        if( scoreSelf>0 && (isSelfOnly  || !srcTyp.getIsSelf()) )
        {
            hasData=true;
            
            c = new PdfPCell( new Phrase( lmsg("g.RSHiLoSelf"), smallFont  ) );
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 1 );
            c.setPaddingRight(3);
            c.setBackgroundColor( shade );
            c.setFixedHeight(14);
            c.setHorizontalAlignment( Element.ALIGN_RIGHT );
            c.setVerticalAlignment( Element.ALIGN_TOP );
            setRunDirection( c );
            t.addCell(c);            

            color = getCompBarRgbColor( scoreSelf, false );
            c = new PdfPCell( new Phrase( "" ) );
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 1 );
            c.setPaddingRight(3);
            c.setBackgroundColor( shade );
            c.setFixedHeight(14);
            c.setVerticalAlignment( Element.ALIGN_TOP );
            c.setCellEvent( new RcHiLoCompetencyCellEvent( I18nUtils.getFormattedNumber( reportData.getLocale(), scoreSelf, scrDigits), scoreSelf, color , getBaseFontCalibri() ) );
            setRunDirection( c );
            t.addCell(c);            
        }
              
        if( !hasData )
            return null;
                
        return t;                        
    }
    
    
    public BaseColor getCompBarRgbColor( float scr, boolean others )
    {
        RcRatingScaleType rst = this.reportData.getRc().getRcScript().getRcRatingScaleType();
        
        if( scr<= rst.getMaxLowRatedCompScore() ) // Constants.RC_MAX_LOWRATED_COMP_SCORE )
            return others ? Constants.LOW_COMP_COLOR_OTHERS : Constants.LOW_COMP_COLOR_SELF;
        if( scr<rst.getMinHighRatedCompScore() ) //  Constants.RC_MIN_HIGHRATED_COMP_SCORE )
            return others ? Constants.MED_COMP_COLOR_OTHERS : Constants.MED_COMP_COLOR_SELF;
        return others ? Constants.HIGH_COMP_COLOR_OTHERS : Constants.HIGH_COMP_COLOR_SELF;
    }
    
    
    
    
    public PdfPTable getItemResponseTable( RcCompetencyWrapper rcw, RcItemWrapper rciw ) throws Exception
    {
        if( devel )
            return null;
        // LogService.logIt( "BaseRcReportTemplate.getItemResponseTable() AAA rciw is " + (rciw==null ? "null" : " not null, itemId=" + rciw.getRcItemId() + ", ratings.size=" + (rciw.getRcRatingList()==null ? "null" : "not null " + rciw.getRcRatingList().size()) ) );
        
        float[] colRelWids = reportData.getIsLTR() ?  new float[] { 20,7,66,7 } : new float[] { 7,66,7,20 };

        // First create the table
        PdfPCell c;
        PdfPTable t = new PdfPTable( 4 );
        setRunDirection( t );
        t.setWidths( colRelWids );
        t.setWidthPercentage( 100 );
        //t.setLockedWidth( true );
        t.setHeaderRows( 0 );

        c = t.getDefaultCell();
        c.setPadding( 0 );
        c.setBorder( Rectangle.NO_BORDER );
        setRunDirection( c );        

        if( rciw.getRcRatingList()==null || rciw.getRcRatingList().isEmpty() )
            return t;
        
        int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            
        RcItem item = rciw.getRcItem();
        String scoreStr;
        String commentStr;
        String subtext;
        String avPlaybackUrl;
        Image avPlaybackIcon;
        
        String selectedRespStr;
        String style="";
        long candidateRcRaterId = 0;
        boolean bottomBorder;
        String bottomText;
        
        Font theTextFont = this.font;
        Font greenFont = this.fontGreen;
            
        
        BaseColor shade = ct2Colors.tableShadeGray3;
        boolean tog = true;  
        boolean isCandidate = false;

        for( RcRater r : reportData.getRc().getRcRaterList() )
        {
            if( r.getIsCandidateOrEmployee() )
                candidateRcRaterId = r.getRcRaterId();
        }
        
        boolean hasAv = false;
        if( !devel )
        {
            for( RcRating rating : rciw.getRcRatingList() )
            {
                if( rating.getHasRecordingInConversion() || rating.getHasRecordingReadyForPlayback() )
                {
                    hasAv=true;
                    break;
                }
            }
        }
        
        String lastname;
        
        Phrase ph;
        for( RcRating rating : rciw.getRcRatingList() )
        {
            isCandidate = rating.getRcRaterId()==candidateRcRaterId;
            
            scoreStr = item.getRcItemFormatType().getIsScoreOk() && rating.getScore()>0 ? I18nUtils.getFormattedNumber(reportData.getLocale(), rating.getFinalScore(), scrDigits ) : "";            
            selectedRespStr = rating.getRcRatingStatusType().getIsSkipped() ? lmsg("g.Skipped") : rating.getSelectedChoicesText();
            commentStr = rating.getText()==null || rating.getText().isBlank() ? "" : rating.getText();
            
            subtext = rating.getSubtext()!=null && !rating.getSubtext().isBlank() ? rating.getSubtext() : null;
            avPlaybackUrl=null;
            avPlaybackIcon=null;
            
            if( (!isCandidate || reportData.o.getCandidateImageViewType().getShowPhotos()) && !devel && rating.getRcUploadedUserFile()!=null )
            {
                if(  rating.getRcUploadedUserFile().getHasRecordingInConversion() )
                {
                    avPlaybackIcon = Image.getInstance( rating.getRcUploadedUserFile().getIsAudio() ? audioCommentConvIconUrl : videoCommentConvIconUrl );
                    avPlaybackIcon.scalePercent(32f);
                }  
                else if( rating.getRcUploadedUserFile().getHasRecordingReadyForPlayback())
                {
                    avPlaybackIcon = Image.getInstance( rating.getRcUploadedUserFile().getIsAudio() ? audioCommentIconUrl : videoCommentIconUrl );
                    avPlaybackIcon.scalePercent(32f);
                    
                    //avPlaybackUrl = ReportUtils.getMediaTempUrlSourceLink( reportData.getRc().getOrgId(), rating.getRcUploadedUserFile(), LMFONTSZ, style, MediaTempUrlSourceType.FILE_UPLOAD);
                    avPlaybackUrl = rating.getRcUploadedUserFile().getReportingMediaUrl();
                    avPlaybackIcon.setAnnotation( new Annotation( 0,0,0,0,avPlaybackUrl));                
                }            
            }
            
            // need lower border
            bottomBorder =  selectedRespStr!=null && !selectedRespStr.isBlank() && ((commentStr!=null && !commentStr.isBlank()) || (subtext!=null && !subtext.isBlank()));
            
            bottomText = "";
            
            if( bottomBorder )
            {
                if( commentStr!=null && !commentStr.isBlank() )
                    bottomText = commentStr;
                
                if( subtext!=null && !subtext.isBlank() )
                {
                    if( !bottomText.isBlank() )
                        bottomText += "\n";
                    bottomText += subtext;
                }
            }
            
            // LogService.logIt( "BaseRcReportTemplate.getItemResponseTable() BBB  scoreStr=" + scoreStr + ", selectedRespStr=" + selectedRespStr + ", commentStr=" + commentStr );

            if( rating.getRcRater()==null )
                rating.setRcRater( reportData.getRc().getRcRaterForRcRaterId(rating.getRcRaterId()) );
            
            if( rating.getUser()==null )
                rating.setUser( rating.getRcRater().getUser() );
                  
            if( isCandidate )
                lastname = rating.getUser().getLastName();
            else
                lastname = rating.getRcRater().getUserLastnameOrAnonymousName();
             
            // last name
            c = new PdfPCell( new Phrase( lastname, isCandidate ? greenFont : theTextFont ) );                  
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 2 );
            c.setPaddingBottom( 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);
            
            // Score
            c = new PdfPCell( new Phrase( scoreStr, isCandidate ? greenFont : theTextFont ) );                  
            c.setBorder( Rectangle.NO_BORDER   );
            c.setPadding( 2 );
            c.setPaddingBottom( 3 );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);

            ph = new Phrase();
            
            if( selectedRespStr!=null && !selectedRespStr.isBlank() )
                ph.add( new Chunk(selectedRespStr, isCandidate ? greenFont : theTextFont) );
            else if( commentStr!=null && !commentStr.isBlank() )
                ph.add( new Chunk(commentStr, isCandidate ? greenFont : theTextFont) );
            //if( subtext!=null && !subtext.isBlank() )
            //    ph.add( new Chunk( "\n" + subtext, fontItalic) );
            // selected & comments
            c = new PdfPCell( ph );                  
            c.setBorder( bottomBorder ? Rectangle.BOTTOM : Rectangle.NO_BORDER   );
            
            if( avPlaybackIcon==null )
                c.setColspan(2);
            c.setBorderColor( ct2Colors.darkFontColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingBottom( 3 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);
            
            if( avPlaybackIcon!=null )
            {
                c = new PdfPCell( avPlaybackIcon );                  
                c.setBorder( bottomBorder ? Rectangle.BOTTOM : Rectangle.NO_BORDER   );
                c.setBorderColor( ct2Colors.darkFontColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 2 );
                c.setPaddingBottom( 3 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);                
            }
            
            if( bottomBorder )
            {
                c = new PdfPCell( new Phrase(" ", theTextFont ) );  
                c.setBorder( Rectangle.NO_BORDER   );
                c.setPadding( 2 );
                c.setColspan( 2 );
                c.setPaddingBottom( 3 );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                c = new PdfPCell( new Phrase(bottomText, isCandidate ? greenFont : theTextFont ) );  
                c.setBorder( Rectangle.NO_BORDER   );
                c.setColspan(2);
                c.setPadding( 2 );
                c.setPaddingBottom( 3 );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);
            }
            // sb.append( "<tr " + style + "><td style=\"width:15%;font-size:11pt\">" + rating.getUser().getLastName() + "</td><td style=\"width:10%;text-align:center;font-size:11pt\">" + scoreStr + "</td><td style=\"width:75%;font-size:11pt\">" + selectedRespStr + commentStr + "</td></tr>\n");

            tog = !tog;
            shade = tog ? ct2Colors.tableShadeGray3 : ct2Colors.tableShadeGray4;                            
        }
        
        return t;        
    }
    
    
    public PdfPTable getDevelCommentTable( RcItemWrapper rciw ) throws Exception
    {
        if( !rciw.getHasCommentsToShow() )
            return null;
        
        // First create the table
        PdfPCell c;
        PdfPTable t = new PdfPTable( 1 );
        setRunDirection( t );
        t.setWidthPercentage( 100 );
        t.setHorizontalAlignment( Element.ALIGN_CENTER );
        //t.setLockedWidth( true );
        t.setHeaderRows( 0 );

        c = t.getDefaultCell();
        c.setPadding( 0 );
        c.setBorder( Rectangle.NO_BORDER );
        setRunDirection( c );        

        if( rciw.getRcRatingList()==null || rciw.getRcRatingList().isEmpty() )
            return t;
        
        RcItem item = rciw.getRcItem();
        String commentsQuestionStr;
        String commentStr;
        
        long candidateRcRaterId = 0;
        boolean bottom = false;
        
        Font boldFont = this.fontBold;
        Font theTextFont = this.font;
        Font greenFont = this.fontGreen;
            
        
        BaseColor shade = BaseColor.WHITE;
        // boolean isCandidate = false;
        String candidateStr = null;

        for( RcRater r : reportData.getRc().getRcRaterList() )
        {
            if( r.getIsCandidateOrEmployee() )
                candidateRcRaterId = r.getRcRaterId();
        }

        commentsQuestionStr = item.getCommentsPlaceholder(); 
        
        if( commentsQuestionStr==null || commentsQuestionStr.isBlank() )
            commentsQuestionStr = lmsg( "g.RCDefaultCommentStrReport" );
        
        //if( commentsQuestionStr!=null && !commentsQuestionStr.isBlank() )
        //{
        c = new PdfPCell( new Phrase( commentsQuestionStr, boldFont ) );                  
        c.setBorder( Rectangle.NO_BORDER   );
        c.setPadding( 2 );
        c.setPaddingBottom( 8 );
        c.setHorizontalAlignment( Element.ALIGN_LEFT );
        c.setBackgroundColor( shade );
        setRunDirection( c );
        t.addCell(c);                
        //}
                    
        List<String> cts = new ArrayList<>();
        for( RcRating rating : rciw.getRcRatingList() )
        {
            commentStr = rating.getText()==null || rating.getText().isBlank() ? "" : rating.getText();            
            if( commentStr.isBlank() )
                continue; 
            
            if( rating.getRcRaterId()==candidateRcRaterId )
                candidateStr = commentStr;
            else
                cts.add(commentStr);
        }
        
        if( cts.isEmpty() && ( candidateStr==null || candidateStr.isBlank()) )
            return null;
        
        if( candidateStr!=null && !candidateStr.isBlank() )
        {
            bottom = cts.isEmpty();
            c = new PdfPCell( new Phrase( candidateStr, greenFont ) );   
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 2 );
            c.setPaddingBottom( bottom ? 3 : 10 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);                
        }
        
        Collections.shuffle(cts);
        
        ListIterator<String> iter = cts.listIterator();
        String cmt;
        while( iter.hasNext() )
        {
            cmt = iter.next();
            bottom = iter.hasNext();
            
            c = new PdfPCell( new Phrase( cmt, theTextFont ) );   
            c.setBorder( Rectangle.NO_BORDER );
            c.setPadding( 2 );
            c.setPaddingBottom( bottom ? 3 : 10 );
            c.setHorizontalAlignment( Element.ALIGN_LEFT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);                
        }
        
        return t;                
    }

    
    public void addSuspiciousActivityTable() throws Exception
    {
        try
        {
            if( devel || reportData.getRc().getRcSuspiciousActivityList()==null || reportData.getRc().getRcSuspiciousActivityList().isEmpty() )
                return;
            
            previousYLevel =  currentYLevel;
            float y = previousYLevel;
            float thgt =0;// t!= null  t.calculateHeights();
            
                        
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" );                         
            //String rcCheckTypeName = reportData.getRc().getRcCheckType().getName( reportData.getLocale() );            
            //String ratersName = lmsg( reportData.getRc().getRcCheckType().getIsPrehire() ? "g.References" : "g.Reviewers" );
            
            int cols = 2;
            int[] colRelWids = reportData.getIsLTR() ?  new int[] { 2,4 } : new int[] { 4,2 };

                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );

            setRunDirection( t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            //Font theTextFont = this.fontLarge;
            
            c = new PdfPCell( new Phrase( lmsg(  "g.Name" ), fontLargeWhiteBold ) );            
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg(  "g.Description" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            
            String name;
            String type;
            String specialNote;
            String date;

            Font fontToUse = fontLarge;
            Font redFontToUse = fontLargeRed;
        
            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;
            
            boolean bottom = false;
            
            //RcRater rater;
            RcSuspiciousActivity sa;
            ListIterator<RcSuspiciousActivity> iter = reportData.getRc().getRcSuspiciousActivityList().listIterator();
        
            int anonymous = reportData.getRc().getForceAllAnonymous()>=0 ? reportData.getRc().getForceAllAnonymous() : reportData.getRcScript().getForceAllAnonymous();
            String lastname;
            RcRater rtr;
            
            while( iter.hasNext() )
            {   
                sa = iter.next();

                bottom = !iter.hasNext();
                
                if( sa.getRcSuspiciousActivityType().getIsRaterRaterMatch() )
                    name=lmsg("g.RCMultiple");
                else
                {
                    if( anonymous>=1 && sa.getRcRaterId()>0 )
                    {
                        rtr = reportData.getRc().getRcRaterForRcRaterId( sa.getRcRaterId() );
                        if( rtr!=null && rtr.getAnonymousName()!=null )
                            name = rtr.getAnonymousName();
                        else
                            name = "****";
                    }
                    else
                        name = sa.getUser()==null ? "" : ( anonymous>=1 ? "****" : sa.getUser().getFullname() );
                }

                type = lmsg( sa.getRcSuspiciousActivityType().getKey() );
                if( sa.getSpecialNote()!=null )
                    specialNote="\n" + sa.getSpecialNote();
                else
                    specialNote="";
                date = includeDates ? I18nUtils.getFormattedDateTimeShort(reportData.getLocale(), sa.getCreateDate(), reportData.getTimeZone() ) : "" ;

                // Name
                c = new PdfPCell( new Phrase( name, redFontToUse ) );                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                // Description
                c = new PdfPCell( new Phrase( type + specialNote, fontToUse ) );                                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                //else
                //    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
                // sb.append( "<tr " + style + "><td style=\"vertical-align:top;text-align:center;color:red\">" + name + "</td><td style=\"text-align:left;vertical-align:top;color:red\">" + type + (specialNote.isBlank() ? "" : "<br /><p>" + specialNote + "</p>")  + "</td></tr>\n" );            
            }

            thgt = t.calculateHeights();
            
            if( thgt> pageHeight )
                t.setSplitLate( false );                
                        
            if( thgt + 75 > y )
            {
                addNewPage();
                y = currentYLevel;
            }            
                        
            y = addTitle( y, lmsg( "g.RcSuspiciousActivityTitle"), lmsg( "g.RcSuspiciousActivitySubtitle") );
            y -= 2*TPAD;

            //t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - t.calculateHeights();
            
            y = addTableToDocument( y, t, true );
            currentYLevel = y;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addSuspiciousActivityTable()" );
            throw e;
        }                        
    }
    
    
    public void addCompetencySummaryTable() throws Exception
    {
        try
        {
            previousYLevel =  currentYLevel;
                                    
            float y = previousYLevel;
            float thgt =0;// t!= null  t.calculateHeights();
            
            String competencySummaryName = lmsg( "g.CompSummaryTitle");
            
            int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            
            //String scr;
            long candidateRaterId = reportData.getRc().getCandidateRcRaterId();
            boolean hasCandidate = reportData.getRc().getCollectCandidateRatings()>0 && candidateRaterId>0;
                        
            int cols = hasCandidate ? 3 : 2;
            int[] colRelWids = hasCandidate ? (reportData.getIsLTR() ?  new int[] { 3,1,1 } : new int[] { 1,1,3 }) : (reportData.getIsLTR() ?  new int[] { 3,1 } : new int[] { 1,3 } );
            
            boolean incIdeal = reportData.getRcScript().getIdealScoresOk()==1;
            
            if( incIdeal )
            {
                cols++;
                colRelWids = hasCandidate ? (reportData.getIsLTR() ?  new int[] { 3,1,1,1 } : new int[] { 1,1,1,3 }) : (reportData.getIsLTR() ?  new int[] { 3,1,1 } : new int[] { 1,1,3 } );
            }
            
            boolean includeNumScores = true; // reportData.getR().getIncludeSubcategoryNumeric()==1;
            // LogService.logIt( "BaseRcReportTemplate.addCompetencySummaryTable() AAA" );
                   
            boolean isSelfOnly =  reportData.getRc().getIsSelfOnly();     
                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );
            setRunDirection( t );
            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            //Font theTextFont = this.fontLarge;
            String candidateNameKey = reportData.getRc().getRcCheckType().getIsPrehire() ? "g.Candidate" : "g.Employee";
            
            c = new PdfPCell( new Phrase( lmsg(  "g.Competency" ), fontLargeWhiteBold ) );            
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            if( incIdeal )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.RCIdealScr" ), fontLargeWhiteBold ) );
                c.setBorder( Rectangle.TOP   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);                
            }
                        
            c = new PdfPCell( new Phrase( lmsg( hasCandidate ? candidateNameKey :  "g.Score" ), fontLargeWhiteBold ) );
            
            if( hasCandidate )
                c.setBorder( Rectangle.TOP   );
            else
                c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )   );
                
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            if( hasCandidate  )
            {
                c = new PdfPCell( new Phrase( lmsg( "g.Others" ), fontLargeWhiteBold ) );
                c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);                
            }
            
            
            RcCompetencyWrapper rcw;
            
            // place in alpha order
            Collections.sort(reportData.getRc().getRcScript().getRcCompetencyWrapperList(), new RcCompetencyWrapperNameComparator() );
            
            ListIterator<RcCompetencyWrapper> iter = reportData.getRc().getRcScript().getRcCompetencyWrapperList().listIterator();
            ListIterator<RcItemWrapper> qiter;
            float score, scoreCandidate;
            int rows = 0;
            int row = 0;
            String scoreStr, scoreStrCandidate, idealScoreStr;
            Font theTextFont = fontLarge;
            Font boldFont = fontLargeBold;
            Font theTextFontItalic = fontItalic;
            boolean tog = true;
            BaseColor shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1; 
            Chunk chk;
            Phrase ph;
            
            while( iter.hasNext() )
            {
                rcw = iter.next();
                if( rcw.getHasAnyScoredItems() )
                    rows++;
            }    
            
            if( rows<=0 )
                return;
                        
            iter = reportData.getRc().getRcScript().getRcCompetencyWrapperList().listIterator();
            while( iter.hasNext() )
            {
                rcw = iter.next();
                
                // no items produce a score.
                if( !rcw.getHasAnyScoredItems() )
                    continue;
                    
                row++;
                
                score = rcw.getScoreAvgNoCandidate();
                scoreCandidate = hasCandidate ? rcw.getAvgScoreCandidate( candidateRaterId ) : 0;
                
                scoreStr = score>0 ? I18nUtils.getFormattedNumber( reportData.getLocale(), score, scrDigits ) : "-";
                scoreStrCandidate = scoreCandidate>0 ? I18nUtils.getFormattedNumber( reportData.getLocale(), scoreCandidate, scrDigits ) : "-";
                idealScoreStr = incIdeal && rcw.getIdealScore()>0 ? I18nUtils.getFormattedNumber( reportData.getLocale(), rcw.getIdealScore(), scrDigits ) : "-";
                
                ph = new Phrase();
                chk = new Chunk( rcw.getRcCompetency().getName(), theTextFont );
                ph.add(chk);
                if( rcw.getRcCompetency().getDescription()!=null && !rcw.getRcCompetency().getDescription().isBlank() )
                {
                    chk = new Chunk( "\n" + rcw.getRcCompetency().getDescription(), theTextFontItalic );
                    ph.add(chk);
                }
                c = new PdfPCell( ph );                  
                if( row==rows )
                    c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);
                
                if( incIdeal )
                {
                    c = new PdfPCell( new Phrase( idealScoreStr, theTextFont ) );  
                    if( row==rows )
                        c.setBorder( Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);                    
                }
                

                c = new PdfPCell( new Phrase( hasCandidate ? scoreStrCandidate : scoreStr, theTextFont ) );  
                if( hasCandidate )
                {
                    if( row==rows )
                        c.setBorder( Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.NO_BORDER );
                }
                else
                {
                    if( row==rows )
                        c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                }
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                if( hasCandidate )
                {
                    c = new PdfPCell( new Phrase( isSelfOnly ? "-" : scoreStr, boldFont ) );    
                    if( row==rows )
                        c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                    else
                        c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setVerticalAlignment( Element.ALIGN_MIDDLE );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);                    
                }
                
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                          
            }

            // place back into displayorder order
            Collections.sort(reportData.getRc().getRcScript().getRcCompetencyWrapperList());

            thgt = t.calculateHeights();
            
            thgt = t.calculateHeights();
            
            if( thgt> pageHeight )
                t.setSplitLate( false );                
            
            if( thgt + 75 > y )
            {
                addNewPage();
                y = currentYLevel;
            }            
            
            y = addTitle( y, competencySummaryName, null );
            y -= 2*TPAD;            
            //t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - t.calculateHeights();
            y = addTableToDocument( y, t, true );
            currentYLevel = y;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addCompetencySummaryTable()" );
            throw e;
        }                
    }
    
    
    public void addReferencesTable() throws Exception
    {
        try
        {
            if( devel )
                return;
            
            previousYLevel =  currentYLevel;
                        
            boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" );
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 
                        
            //String rcCheckTypeName = reportData.getRc().getRcCheckType().getName( reportData.getLocale() );
            
            String ratersName = lmsg( reportData.getRc().getRcCheckType().getIsPrehire() ? "g.References" : "g.Reviewers" );
            
            boolean hasPhotos = reportData.getRc().getHasRaterPhotos() && !devel;
            boolean hasIdPhotos = reportData.getRc().getHasRaterIdPhotos() && !devel;

            
            float y = previousYLevel;
            float thgt =0;// t!= null  t.calculateHeights();
            
            
            //float scrValue;            
            //String overallScoreTitle = reportData.getR().getStrParam4()!=null && !reportData.getR().getStrParam4().isEmpty() ? reportData.getR().getStrParam4()  :  lmsg( "g.Score");            
            int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            
            //String scr;

            int cols = 5;
            int[] colRelWids = reportData.getIsLTR() ?  new int[] { 2,2,2,2,1 } : new int[] { 1,2,2,2,2 };
            
            if( hasPhotos && !hasIdPhotos )
                colRelWids = reportData.getIsLTR() ?  new int[] { 2,1,2,2,1 } : new int[] { 1,2,2,1,2 };
            
            else if( hasPhotos && hasIdPhotos )
            {
                colRelWids = reportData.getIsLTR() ?  new int[] { 2,1,1,2,2,1 } : new int[] { 1,2,2,1,1,2 };
                cols=6;
            }
            
            boolean includeNumScores = !hideOverallNumeric; // reportData.getR().getIncludeSubcategoryNumeric()==1;
            // LogService.logIt( "BaseRcReportTemplate.getReportInfoHeader() AAA" );
                                                
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );

            setRunDirection( t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            //Font theTextFont = this.fontLarge;
            
            c = new PdfPCell( new Phrase( lmsg(  "g.Name" ), fontLargeWhiteBold ) );            
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            c = new PdfPCell( new Phrase(  lmsg( hasPhotos ? "g.Photo" : "g.Role" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            if( hasPhotos && hasIdPhotos )
            {
                c = new PdfPCell( new Phrase(  lmsg( "g.PhotoId" ), fontLargeWhiteBold ) );
                c.setBorder( Rectangle.TOP  );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( ct2Colors.hraBlue );
                setRunDirection( c );
                t.addCell(c);
            }

            c = new PdfPCell( new Phrase( lmsg(  "g.Contact" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg(  "g.Status" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( lmsg(  "g.Score" ), fontLargeWhiteBold ) );
            c.setBorder( Rectangle.TOP | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            String scoreStr = null;
            String statusStr = null;
             String contactInfo = null;
            String contactOkStr;
            String recruitingOkStr;
            String roleType;

            Phrase contact;
            Chunk chk;
            
            Font fontToUse = fontLarge;
        
            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;
            
            boolean bottom = false;
            
            RcRater rater;
            ListIterator<RcRater> iter = reportData.getRc().getRcRaterList().listIterator();
        
            String fullname;
            int anonymous = reportData.getRc().getForceAllAnonymous()>=0 ? reportData.getRc().getForceAllAnonymous() : reportData.getRcScript().getForceAllAnonymous();
            
            String thumbUrl;
            
            while( iter.hasNext() )
            {   
                rater = iter.next();
                
                // skip candidate.
                if( rater.getIsCandidateOrEmployee() )
                    continue;
                
                fullname = rater.getUserFullnameOrAnonymousName();
                
                bottom = !iter.hasNext();

                scoreStr = "";
                contactOkStr = "";
                recruitingOkStr = "";
                
                contactInfo = anonymous>=1 ? "" : rater.getUser().getEmail() + (rater.getUser().getHasMobilePhone() ? "\n" + rater.getUser().getMobilePhone() : "");

                contact=new Phrase();
                chk = new Chunk( contactInfo, fontToUse );
                contact.add( chk );
                
                if( rater.getRcRaterStatusType().getIsComplete() )
                {
                    scoreStr = I18nUtils.getFormattedNumber( reportData.getLocale(), rater.getOverallScore(), scrDigits );

                    if( rater.getContactPermissionTypeId()==RcContactPermissionType.YES.getRcContactPermissionTypeId()  )
                        contactOkStr = "\n" + lmsg( "g.RSOkToContactYes" );
                    else if( rater.getContactPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId()  )
                        contactOkStr = "\n" + lmsg( "g.RSOkToContactNo" );
                    if( !contactOkStr.isBlank() )
                        contact.add( new Chunk( contactOkStr,rater.getContactPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId() ? fontLargeRed : fontToUse ) );

                    if( rater.getRecruitingPermissionTypeId()==RcContactPermissionType.YES.getRcContactPermissionTypeId()  )
                        recruitingOkStr = "\n" + lmsg( "g.RSOkToContactRecruitYes" );
                    else if( rater.getRecruitingPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId()  )
                        recruitingOkStr = "\n" + lmsg( "g.RSOkToContactRecruitNo" );                
                    if( !recruitingOkStr.isBlank() )
                        contact.add( new Chunk( recruitingOkStr, rater.getRecruitingPermissionTypeId()==RcContactPermissionType.NO.getRcContactPermissionTypeId() ? fontLargeRed : fontToUse ) );
                }
                statusStr = rater.getRcRaterStatusType().getName(reportData.getLocale());
                if( includeDates && rater.getRcRaterStatusType().getIsComplete() )
                    statusStr += "\n" + I18nUtils.getFormattedDateTimeShort(reportData.getLocale(), rater.getCompleteDate(), reportData.getTimeZone() );
                else if( includeDates && !rater.getRcRaterStatusType().getCompleteOrHigher() && rater.getLastUpdate()!=null )
                    statusStr += "\n" + lmsg( "g.RSLastUpdateX", new String[]{I18nUtils.getFormattedDateTimeShort(reportData.getLocale(), rater.getCompleteDate(), reportData.getTimeZone() )} );
                
                if( rater.getHasIpLocationData() )
                    statusStr += "\n"  + lmsg( "g.RCIpLocDataX", new String[]{rater.getIpLocationData()} );
                
                roleType = rater.getRcRaterRoleType().getName(reportData.getLocale(), rater.getRcCheck()==null || rater.getRcCheck().getRcOrgPrefs()==null ? null : rater.getRcCheck().getRcOrgPrefs().getOtherRoleTypeNames(rater.getRcCheck().getRcSuborgPrefs()) );
                // Name
                if( hasPhotos )
                    fullname = fullname + "\n" + roleType;
                
                c = new PdfPCell( new Phrase( fullname, fontToUse ) );                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT )  );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_LEFT );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);

                // Role
                if( hasPhotos )
                {
                    if( rater.getHasPhotos()  )
                    {
                        RcUploadedUserFile u = rater.getSinglePhotoFauxRcUploadedUserFile();
                        //Image photoImg = Image.getInstance( new URL( u.getThumbUrl() ) );
                        String fn = u.getThumbFilename();
                        if( fn!=null && fn.contains(  ".IDX." ) )
                            fn = StringUtils.replaceStr( fn, ".IDX." , "." + u.getTempInt1() + "." );
                        thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( u, fn );                        
                        
                        // thumbUrl = ReportUtils.getMediaTempUrlSourceLink( reportData.o.getOrgId(), u, 1, u.getThumbFilename(), MediaTempUrlSourceType.REF_THUMB );                        
                        Image photoImg = getItextThumbImage(thumbUrl, hasIdPhotos ? 48 : 60, false ); 
                        
                        // Image photoImg = getItextThumbImage( u.getThumbUrl(), 60 ); 
                        if( photoImg==null )
                            c = new PdfPCell( new Phrase("",fontToUse) );
                        else
                            c = new PdfPCell( photoImg);
                    }
                    else
                        c = new PdfPCell( new Phrase( "", fontToUse ) );
                }
                else
                    c = new PdfPCell( new Phrase( roleType, fontToUse ) );                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM  );
                else
                    c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);
                
                if( hasPhotos && hasIdPhotos )
                {
                    if( rater.getHasIdPhotos()  )
                    {
                        RcUploadedUserFile u = rater.getSingleIdPhotoFauxRcUploadedUserFile();
                        //Image photoImg = Image.getInstance( new URL( u.getThumbUrl() ) );
                        String fn = u.getThumbFilename();
                        if( fn!=null && fn.contains(  ".IDX." ) )
                            fn = StringUtils.replaceStr( fn, ".IDX." , "." + u.getTempInt1() + "." );
                        thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( u, fn );                        
                        
                        // thumbUrl = ReportUtils.getMediaTempUrlSourceLink( reportData.o.getOrgId(), u, 1, u.getThumbFilename(), MediaTempUrlSourceType.REF_THUMB );                        
                        Image photoImg = getItextThumbImage(thumbUrl, 48, false ); 
                        
                        // Image photoImg = getItextThumbImage( u.getThumbUrl(), 60 ); 
                        if( photoImg==null )
                            c = new PdfPCell( new Phrase("",fontToUse) );
                        else
                            c = new PdfPCell(photoImg);
                    }
                    else
                        c = new PdfPCell(new Phrase( "", fontToUse ));
                    if( bottom )
                        c.setBorder( Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_CENTER );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);
                }  
                
                
                // Contact
                //if( anonymous<1 )
                //{
                    c = new PdfPCell( anonymous<1 ? contact : new Phrase("") );                  
                    if( bottom )
                        c.setBorder( Rectangle.BOTTOM  );
                    else
                        c.setBorder( Rectangle.NO_BORDER );
                    c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                    c.setBorderWidth( scoreBoxBorderWidth );
                    c.setPadding( 4 );
                    c.setPaddingBottom( 5 );
                    c.setHorizontalAlignment( Element.ALIGN_LEFT );
                    c.setBackgroundColor( shade );
                    setRunDirection( c );
                    t.addCell(c);
                //}
                
                // Status
                c = new PdfPCell( new Phrase( statusStr, fontToUse ) );                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM  );
                else
                    c.setBorder( Rectangle.NO_BORDER );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);
                
                // Score
                c = new PdfPCell( new Phrase( includeNumScores ? scoreStr : "-", fontToUse ) );                  
                if( bottom )
                    c.setBorder( Rectangle.BOTTOM | (reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT )  );
                else
                    c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT   );
                c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setBorderWidth( scoreBoxBorderWidth );
                c.setPadding( 4 );
                c.setPaddingBottom( 5 );
                c.setHorizontalAlignment( Element.ALIGN_CENTER );
                c.setBackgroundColor( shade );
                setRunDirection( c );
                t.addCell(c);
                                
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;                
            }

            thgt = t.calculateHeights();
            
            if( thgt> pageHeight )
                t.setSplitLate( false );                
            
            if( thgt + 75 > y )
            {
                addNewPage();
                y = currentYLevel;
            }            
            
            y = addTitle( y, ratersName, null );
            y -= 2*TPAD;            
            //t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            //currentYLevel = y - t.calculateHeights();
            
            y = addTableToDocument( y, t, true );
            currentYLevel=y;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addReferencesTable()" );
            throw e;
        }                
    }
    
    public void addReportInfoHeader() throws Exception 
    {
        try
        {
            previousYLevel =  currentYLevel;
            
            // Font fnt = getFontXLarge();
            if( reportData.getReportRuleAsBoolean( "ovroff" ) )
                return;                
            
            boolean hideOverallNumeric = reportData.getReportRuleAsBoolean( "ovrnumoff" );
            boolean hideOverallScoreText = reportData.getReportRuleAsBoolean( "ovrscrtxtoff" );
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 
            boolean includeCompanyInfo = true;
            
            
            String rcCheckTypeName = reportData.getRc().getRcCheckType().getName( reportData.getLocale() );
            
            String subTitleKey = devel ? "g.RSRepInfoHdrSubDevel" : null;
            
            float y = addTitle( previousYLevel, lmsg(  "g.RSRefInfo" , new String[]{rcCheckTypeName} ), subTitleKey==null ? null : lmsg( subTitleKey ) );
            y -= 2*TPAD;

            float scrValue = reportData.getRc().getOverallScore();            
            //String overallScoreTitle = reportData.getR().getStrParam4()!=null && !reportData.getR().getStrParam4().isEmpty() ? reportData.getR().getStrParam4()  :  lmsg( "g.Score");            
            int scrDigits = reportData.getR().getIntParam2() >= 0 ? reportData.getR().getIntParam2() : Constants.DEFAULT_SCORE_PRECISION_DIGITS;            
            String scr = I18nUtils.getFormattedNumber( reportData.getLocale(), scrValue, scrDigits );

            int cols = 2;
            int[] colRelWids = reportData.getIsLTR() ?  new int[] { 3, 5 } : new int[] { 5, 3 };

            boolean includeNumScores = !hideOverallNumeric; // reportData.getR().getIncludeSubcategoryNumeric()==1;
            // LogService.logIt( "BaseRcReportTemplate.getReportInfoHeader() AAA" );
                        
            // Next row - Text
            String scrTxt = null; //getRc().getOverallTestEventScore().getScoreText();

            // TO DO - add Score Text                 
            
            if( hideOverallScoreText || reportData.getR().getIncludeScoreText() != 1 )
                scrTxt = null;            
                        
            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( cols );

            setRunDirection( t );
            // float importanceWidth = 25;

            t.setTotalWidth( pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN );
            t.setWidths( colRelWids );
            t.setLockedWidth( true );
            t.setHeaderRows( 1 );

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            setRunDirection( c );

            Font theTextFont = this.fontLarge;
            
            c = new PdfPCell( new Phrase( lmsg(  "g.RSRefInfo" , new String[]{rcCheckTypeName} ), fontLargeWhiteBold ) );
            c.setColspan(2);
            c.setBorder( Rectangle.TOP | Rectangle.LEFT | Rectangle.RIGHT );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 4 );
            c.setPaddingBottom( 5 );
            //c.setPaddingLeft( CT2_BOXHEADER_LEFTPAD );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( ct2Colors.hraBlue );
            setRunDirection( c );
            t.addCell(c);
            
            BaseColor shade = ct2Colors.tableShadeGray2;
            boolean tog = true;
            
            // Name
            add2ColTableRow(t, shade, lmsg( reportData.getIsPrehire() ? "g.CandidateC" : "g.EmployeeC" ), reportData.getUserName(), theTextFont, false, null );

            // Candidate Photo
            if( reportData.getRc().getHasPhotos() && reportData.o.getCandidateImageViewType().getShowPhotos() )
            {
                RcUploadedUserFile u = reportData.getRc().getSingleFauxPhotoRcUploadedUserFile();
                String fn = u.getThumbFilename();
                if( fn!=null && fn.contains(  ".IDX." ) )
                    fn = StringUtils.replaceStr( fn, ".IDX." , "." + u.getTempInt1() + "." );
                String thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( u, fn );                        

                Image photoImg = getItextThumbImage(thumbUrl, 100, false ); //  Image.getInstance( new URL( u.getThumbUrl() ) );
                // Image photoImg = getItextThumbImage( u.getThumbUrl(), 100 ); //  Image.getInstance( new URL( u.getThumbUrl() ) );
                if( photoImg!=null )
                {
                    tog = !tog;
                    shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                    add2ColTableRow(t, shade, lmsg( "g.Photo" ) + ":", "", theTextFont, false, photoImg );
                }
            }
            // Candidate IdPhoto
            if( reportData.getRc().getHasIdPhotos() )
            {
                RcUploadedUserFile u = reportData.getRc().getSingleFauxPhotoIdRcUploadedUserFile();
                String fn = u.getThumbFilename();
                if( fn!=null && fn.contains(  ".IDX." ) )
                    fn = StringUtils.replaceStr( fn, ".IDX." , "." + u.getTempInt1() + "." );
                String thumbUrl = ReportUtils.getUploadedUserFileThumbUrl( u, fn );                        
                
                // String thumbUrl = ReportUtils.getMediaTempUrlSourceLink( reportData.o.getOrgId(), u, 1, u.getThumbFilename(), MediaTempUrlSourceType.REF_THUMB ); 
                Image photoImg = getItextThumbImage(thumbUrl, 100, false ); //  Image.getInstance( new URL( u.getThumbUrl() ) );
                // Image photoImg = getItextThumbImage( u.getThumbUrl(), 100 ); //  Image.getInstance( new URL( u.getThumbUrl() ) );
                if( photoImg!=null )
                {
                    tog = !tog;
                    shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                    add2ColTableRow(t, shade, lmsg( "g.PhotoId" ) + ":", "", theTextFont, false, photoImg );
                }
            }
            
            // Status
            tog = !tog;
            shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
            add2ColTableRow(t, shade, lmsg( "g.StatusC" ), reportData.getRc().getRcCheckStatusType().getName( reportData.getLocale() ), theTextFont, false, null );
            
            if( !devel && reportData.getRc().getHasSuspiciousActivity() )
                add2ColTableRow(t, shade, "", lmsg( "g.RCSusActDetectSeeBelow" ), fontLargeRed, false, null );                
            
            // Score
            if( includeNumScores )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.ScoreC" ), scr, theTextFont, false, null );
            }
                        
            // ScoreText
            if( scrTxt!=null && !scrTxt.isBlank() )
                add2ColTableRow(t, shade, scrTxt, scr, theTextFont, false, null );
            
            tog = !tog;
            shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
            add2ColTableRow(t, shade, lmsg( "g.EmailC" ), reportData.getU().getEmail(), theTextFont, false, null );

            if( reportData.getU().getHasMobilePhone() )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.MobileC" ), reportData.getU().getMobilePhone(), theTextFont, false, null );
            }

            if( reportData.getU().getCountryCode()!=null && !reportData.getU().getCountryCode().isBlank() )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.CountryC" ), lmsg( "cntry." + reportData.getU().getCountryCode() ), theTextFont, false, null );
            }
            
            if( reportData.getU().getHasAltIdentifierInfo() )
            {
                String ainame = reportData.getU().getAltIdentifierName();
                if( ainame == null || ainame.isEmpty() )
                    ainame = lmsg(  "g.DefaultAltIdentifierName" );

                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, ainame + ":", reportData.getU().getAltIdentifier(), theTextFont, false, null );                
            }    
            
            if( reportData.getRc().getExtRef()!=null && !reportData.getRc().getExtRef().isBlank() )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg(  "g.ExtRefC" ), reportData.getRc().getExtRef(), theTextFont, false, null );                
            }            
            
            if( reportData.getRc().getRcCheckType().getIsPrehire() && reportData.getRc().getJobTitle()!=null && !reportData.getRc().getJobTitle().isBlank() )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.TitleC" ), reportData.getRc().getJobTitle(), theTextFont, false, null );                                
            }
            
            tog = !tog;
            shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
            add2ColTableRow(t, shade, lmsg( "g.RSTemplateC" ), reportData.getRc().getRcScript().getName(), theTextFont, false, null );                                

            if( includeDates && reportData.getRc().getCandidateCompleteDate()!=null )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.RSCandCompleteDateC" ), I18nUtils.getFormattedDateTimeShort(reportData.getLocale(), reportData.getRc().getCandidateCompleteDate(), reportData.getTimeZone() ), theTextFont, false, null );                                
            }

            if( includeDates && reportData.getRc().getRcCheckType().getIsPrehire() && reportData.getRc().getFirstCandidateSendDate()!=null && reportData.getRc().getFirstCandidateReferenceDate()!=null )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                long msec = reportData.getRc().getFirstCandidateReferenceDate().getTime() - reportData.getRc().getFirstCandidateSendDate().getTime();
                add2ColTableRow(t, shade, lmsg( "g.RCCandFirstRefTime" ) + ":", StringUtils.getDaysHrsMinsStr( msec, reportData.getLocale() ), theTextFont, false, null );                                
            }

            if( includeDates && reportData.getRc().getRcCheckType().getIsPrehire() && reportData.getRc().getFirstCandidateReferenceDate()!=null && reportData.getRc().getLastCandidateReferenceDate()!=null && reportData.getRc().getLastCandidateReferenceDate().getTime() - reportData.getRc().getFirstCandidateReferenceDate().getTime() > 300*1000 )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                long msec = reportData.getRc().getLastCandidateReferenceDate().getTime() - reportData.getRc().getFirstCandidateReferenceDate().getTime();
                add2ColTableRow(t, shade, lmsg( "g.RCCandLastRefTime" ) + ":", StringUtils.getDaysHrsMinsStr( msec, reportData.getLocale() ), theTextFont, false, null );                                
            }

            if( includeDates && reportData.getRc().getRcCandidateStatusType().getIsCompletedOrHigher() && reportData.getRc().getCandidateStartDate()!=null && reportData.getRc().getCandidateCompleteDate()!=null )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                long msec = reportData.getRc().getCandidateCompleteDate().getTime() - reportData.getRc().getCandidateStartDate().getTime();
                add2ColTableRow(t, shade, lmsg( "g.RCCandComplTime" ) + ":", StringUtils.getDaysHrsMinsSecsStr( msec, reportData.getLocale() ), theTextFont, false, null );                                
            }
            
            
            if( includeDates )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.RSCompletedC" ), I18nUtils.getFormattedDateTimeShort(reportData.getLocale(), reportData.getRc().getCompleteDate(), reportData.getTimeZone() ), theTextFont, false, null );                                                
            }

            if( reportData.getRc().getHasCandidateIpLocationData() )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.RCCandIpLocDataC" ), reportData.getRc().getCandidateIpLocationData(), theTextFont, false, null );                                                
            }
            

            // include only if there is an auth user name.
            if( includeCompanyInfo )
            {
                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
                add2ColTableRow(t, shade, lmsg( "g.RSAuthorizedByC" ), reportData.getRc().getAdminUser().getFullname(), theTextFont, false, null );                                                

                tog = !tog;
                shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;

                if( reportData.hasCustLogo() && custLogo!=null )
                    add2ColTableRow(t, shade, lmsg( "g.OrganizationC" ), reportData.getOrgName(), theTextFont, false, custLogo );                 
                else
                    add2ColTableRow(t, shade, lmsg( "g.OrganizationC" ), reportData.getOrgName(), theTextFont, false, null );                                                
            }

            tog = !tog;
            shade = tog ? ct2Colors.tableShadeGray2 : ct2Colors.tableShadeGray1;
            PdfPTable rrt = getStandardRatersByRoleTable( shade, theTextFont );

            String ratersName = lmsg( reportData.getIsPrehire() ?  "g.References" : "g.Reviewers" );
            
            c = new PdfPCell( new Phrase( lmsg( "g.RSRatersByRoleBySimpC", new String[]{ratersName} ), theTextFont ) );
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setPaddingBottom( 5 );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell();
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
            c.setBorderColor( ct2Colors.scoreBoxBorderColor );
            c.setBorderWidth( scoreBoxBorderWidth );
            c.setPadding( 2 );
            c.setPaddingLeft( 4 );
            c.setPaddingRight( 4 );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( shade );
            c.addElement(rrt);
            t.addCell(c);        
                                    
            t.writeSelectedRows(0, -1,CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, pdfWriter.getDirectContent() );
            currentYLevel = y - t.calculateHeights();
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseRcReportTemplate.addReportInfoHeader()" );
            throw e;
        }        
    }
    
    public PdfPTable getStandardRatersByRoleTable( BaseColor shade, Font theTextFont )
    {
        PdfPTable t = new PdfPTable( 2 );
        setRunDirection( t );
        t.setWidthPercentage( 100 );

        PdfPCell c = t.getDefaultCell();
        c.setBorder( Rectangle.NO_BORDER );
        setRunDirection( c );
        
        int[] rrt = reportData.getRc().getRaterRoleTypeCounts();

        String[] left = new String[] {"", lmsg("rcrrt.supervisorormanager"),lmsg("rcrrt.peer"),lmsg("rcrrt.subordinate"),lmsg("rcrrt.otherorunknown")}; 
        for( int i=1;i<=4;i++ )
        {
            c = new PdfPCell( new Phrase( left[i] + ":", theTextFont ) );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);

            c = new PdfPCell( new Phrase( Integer.toString( rrt[i] ), theTextFont ) );
            c.setBorderWidth( 0 );
            c.setPadding( 2 );
            c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
            c.setBackgroundColor( shade );
            setRunDirection( c );
            t.addCell(c);                    
        }
        
        return t;
    }    
    
    
    
    public void add2ColTableRow( PdfPTable t, BaseColor shade, String left, String right, Font theTextFont, boolean bottomRow, Image imgObj)
    {
        PdfPCell c = new PdfPCell( new Phrase( left, theTextFont ) );
        if( bottomRow )
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT | Rectangle.BOTTOM : Rectangle.RIGHT | Rectangle.BOTTOM );
        else
            c.setBorder( reportData.getIsLTR() ? Rectangle.LEFT : Rectangle.RIGHT );
        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
        c.setBorderWidth( scoreBoxBorderWidth );
        c.setPadding( 2 );
        c.setPaddingLeft( 4 );
        c.setPaddingRight( 4 );
        c.setPaddingBottom( 5 );
        c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
        c.setBackgroundColor( shade );
        setRunDirection( c );
        t.addCell(c);

        if( imgObj!=null )
            c = new PdfPCell( imgObj );
        else
            c = new PdfPCell( new Phrase( right, theTextFont ) );
        if( bottomRow )
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT | Rectangle.BOTTOM : Rectangle.LEFT | Rectangle.BOTTOM );
        else
            c.setBorder( reportData.getIsLTR() ? Rectangle.RIGHT : Rectangle.LEFT );
        c.setBorderColor( ct2Colors.scoreBoxBorderColor );
        c.setBorderWidth( scoreBoxBorderWidth );
        c.setPadding( 2 );
        c.setPaddingLeft( 4 );
        c.setPaddingRight( 4 );
        c.setHorizontalAlignment( reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT );
        c.setBackgroundColor( shade );
        setRunDirection( c );
        t.addCell(c);        
    }



    public void addCoverPage(boolean includeDescriptiveText) throws Exception
    {
        try
        {
            if( reportData.getReportRuleAsBoolean( "covrdescripoff" ) )
                includeDescriptiveText = false;
                
            float y = pageHeight - getHraLogoBlackText().getScaledHeight() - 20;  // ( (pageHeight - t.getTotalHeight() )/2 ) - cfLogo.getHeight() )/2 - cfLogo.getHeight();

            ITextUtils.addDirectImage( pdfWriter, getHraLogoBlackText(), CT2_MARGIN, y, false );

            java.util.List<Chunk> cl = new ArrayList<>();

            String reportCompanyName = reportData==null ? null : reportData.getReportCompanyName();

            if( reportCompanyName==null || reportCompanyName.isEmpty() )
                reportCompanyName = reportData.getOrgName();

            if( StringUtils.isCurlyBracketed( reportCompanyName ) )
                reportCompanyName = "                        ";

            String reportCompanyAdminName = reportData==null ? null : reportData.getReportCompanyAdminName();

            if( reportData.getRc().getAdminUser() != null && (reportCompanyAdminName==null || reportCompanyAdminName.isEmpty())  )
                reportCompanyAdminName = reportData.getRc().getAdminUser().getFullname();

            else if( StringUtils.isCurlyBracketed( reportCompanyAdminName ) )
                reportCompanyAdminName = "                        ";


            if( reportCompanyAdminName != null && reportCompanyAdminName.indexOf( "AUTOGEN" )>=0 )
                reportCompanyAdminName = null;

            boolean includeCompanyInfo = reportCompanyName!=null && !reportData.getReportRuleAsBoolean( "companyinfooff" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );            
            if( !includeCompanyInfo )
            {
                reportCompanyName = "";
                custLogo = null;
            }
            
            boolean includeDates = !reportData.getReportRuleAsBoolean( "hidedatespdf" ); 
            
            boolean includePreparedFor = includeCompanyInfo && reportCompanyAdminName!=null && !reportData.getReportRuleAsBoolean( "ct3excludepreparedfor" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );

            boolean employeeFeedback = reportData.getIsEmployee();
            // boolean sports = includeCompanyInfo && reportData.getReportRuleAsBoolean( "sportstest" ); // ==null || !reportData.getReportRule( "ct3excludepreparedfor" ).equalsIgnoreCase( "1" ) );
            
            if( !includePreparedFor )
                reportCompanyAdminName = "";
            
            String testTakerTitle = lmsg( employeeFeedback ? "g.EmployeeC" : "g.CandidateC" );
            
            cl.add( new Chunk( testTakerTitle, getFontXLarge() ) );
            cl.add( new Chunk( lmsg( "g.TemplateC" ), getFontXLarge() ) );
            
            if( includeDates )
                cl.add( new Chunk( lmsg( "g.CompletedC" ), getFontXLarge() ) );

            if( !employeeFeedback && includePreparedFor && reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getRc().getAdminUser() != null )
                cl.add( new Chunk( lmsg( "g.PreparedForC" ), getFontXLarge() ) );
            
            if( includePreparedFor && includeCompanyInfo ) // reportData.getRc().getAdminUser() != null )
                cl.add( new Chunk( lmsg( "g.CompanyC" ), getFontXLarge() ) );
            
            float titleWid = ITextUtils.getMaxChunkWidth( cl ) + 20;

            cl.clear();

            cl.add( new Chunk( reportData.getUserName(), getFontXLargeBold() ) );
            //cl.add( new Chunk( reportData.getUserName(), getHeaderFontXLarge() ) );
            cl.add( new Chunk( reportData.getScriptName(), getFontXLarge() ) );
            
            if( includeDates )
                cl.add( new Chunk( reportData.getCompleteDateFormatted(), getFontXLarge() ) );

            if( !employeeFeedback && includePreparedFor && reportCompanyAdminName != null && !reportCompanyAdminName.isEmpty() ) // reportData.getRc().getAdminUser() != null )
            {
                cl.add( new Chunk( reportCompanyAdminName, getFontXLarge() ) );

                if( includeCompanyInfo && (!reportData.hasCustLogo() || custLogo==null) && reportCompanyName != null && !reportCompanyName.isEmpty() )
                    cl.add( new Chunk( reportCompanyName, getFontXLarge() ) );
            }
                                    
            float infoWid = ITextUtils.getMaxChunkWidth( cl ) + 10;

            if( custLogo!=null && custLogo.getScaledWidth()>infoWid )
                infoWid = custLogo.getScaledWidth();

            // First create the table
            PdfPCell c;

            // First, add a table
            PdfPTable t = new PdfPTable( 2 );

            t.setTotalWidth( reportData.getIsLTR() ?  new float[] { titleWid+4, infoWid+14 } : new float[] { infoWid+14,titleWid+4 } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setPaddingRight( 15 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            Font font = this.fontXLarge;

            t.addCell( new Phrase( testTakerTitle , font ) );
            t.addCell( new Phrase( reportData.getUserName(), getFontXLargeBold() ) );

            t.addCell( new Phrase( lmsg( "g.TemplateC" ) , font ) );
            t.addCell( new Phrase( reportData.getScriptName(), font ) );

            if( includeDates )
            {
                t.addCell( new Phrase( lmsg( "g.CompletedC" ) , font ) );
                t.addCell( new Phrase( reportData.getCompleteDateFormatted(), font ) );
            }

            if(!employeeFeedback && includeCompanyInfo && includePreparedFor && reportCompanyAdminName!=null && !reportCompanyAdminName.isEmpty() ) // reportData.getRc().getAdminUser() != null )
            {
                // LogService.logIt( "BaseReportTemplate.addCoverPage() Adding prepared " + lmsg( "g.PreparedForC" ) + ", includeCompanyInfo=" + includeCompanyInfo + ", devel=" + devel );
                t.addCell( new Phrase( lmsg( "g.PreparedForC" ) + " " , font ) );
                t.addCell( new Phrase( reportCompanyAdminName, font ) );
            }

            if( includeCompanyInfo )
            {
                t.addCell( new Phrase( lmsg( "g.CompanyC" ), font ) );

                if( reportData.hasCustLogo() && custLogo!=null )
                {
                    c = new PdfPCell( custLogo );
                    c.setBorder( Rectangle.NO_BORDER );
                    c.setHorizontalAlignment(Element.ALIGN_LEFT );
                    c.setPadding( 0 );
                    c.setPaddingTop( 12 );
                    setRunDirection(c);
                    t.addCell( c );
                }

                else
                    t.addCell( new Phrase( reportCompanyName, fontXLarge ) );
            }

            float tableH = t.calculateHeights(); //  + 500;

            float tableY = y + 10 - (y - pageHeight/2 - tableH)/2;

            float tableW = t.getTotalWidth();

            float tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            // addDirectText( "Assessment", 300, 300, baseFontCalibri, 24, getHraOrangeColor(), false );

            // Add the blue below
            ITextUtils.addDirectColorRect( pdfWriter, getHraBaseReportColor(), 0, 0, pageWidth, pageHeight/2, 0, 1, true );

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            // c.setBorder( Rectangle.BOX );
            //c.setBorderWidth( 0.5f );
            //c.setBackgroundColor( BaseColor.LIGHT_GRAY );
            //c.setBorderColor( BaseColor.DARK_GRAY );

            // t.addCell( "\n\n\n\n\n\n\n\n\n" );
            
            if( !includeDescriptiveText )
                t.addCell( "\n\n\n\n\n" );
            
            c = new PdfPCell( new Phrase( reportData.getReportName(), getHeaderFontXXLargeWhite() ) );
            // c = new PdfPCell( new Phrase( reportData.getReportName() , getHeaderFontXXLargeWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );
            
            int returnCt = 0;

            if( includeDescriptiveText )
            {
                if( coverDescrip != null &&  !coverDescrip.isEmpty() )
                {}

                else if( reportData.getR().getTextParam1()!=null && !reportData.getR().getTextParam1().isEmpty() )
                    coverDescrip = reportData.getR().getTextParam1();                                        

                else
                {
                    String coverDetailKey = reportData.getR()!=null && reportData.getR().getStrParam1()!=null && !reportData.getR().getStrParam1().isEmpty() ? reportData.getR().getStrParam1() : "g.CT2CoverDescrip";            
                    coverDescrip = lmsg( coverDetailKey, new String[] {reportData.getScriptName()} );                
                }
                
                if( coverDescrip!=null )
                {
                    returnCt++;
                    
                    int idx = coverDescrip.indexOf("\n" );
                    while( idx>=0 )
                    {
                        returnCt++;
                        idx = coverDescrip.indexOf("\n" , idx+1);
                    }
                }
                
                // cuont \n's in coverDescrip
            }            
            
            int rc = 9 - returnCt;
            
            if( rc<1)
                rc=1;
            
            String rets = "";
            for( int i=0;i<rc;i++ )
                rets += "\n";
            
            t.addCell( rets );

            //String coverDescrip=null; 

            if( includeDescriptiveText )
            {
                c = new PdfPCell( new Phrase( coverDescrip , fontLLWhite ) );
                c.setBorder( Rectangle.NO_BORDER );
                setRunDirection(c);

                t.addCell( c );
            }
            //t.addCell( "\n\n\n" );
            //c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            //c.setBorder( Rectangle.NO_BORDER );
            //c.setHorizontalAlignment( Element.ALIGN_CENTER );
            //setRunDirection(c);
            //t.addCell( c );

            tableH = t.calculateHeights(); //  + 500;

            tableY = pageHeight/2 - (pageHeight/2 - tableH)/2;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );

            t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);


            c = new PdfPCell( new Phrase( lmsg( "g.ProprietaryAndConfidential" ) , getFontWhite() ) );
            c.setBorder( Rectangle.NO_BORDER );
            c.setHorizontalAlignment( Element.ALIGN_CENTER );
            setRunDirection(c);
            t.addCell( c );

            // tableH = t.calculateHeights(); //  + 500;

            tableY = 20;

            tableW = t.getTotalWidth();

            tableX = (pageWidth - tableW)/2;

            t.writeSelectedRows(0, -1,tableX, tableY, pdfWriter.getDirectContent() );


        }

        catch( DocumentException e )
        {
            LogService.logIt( e, "BaseReportTemplate.addCoverPage()" );
        }
    }


    
    
}
