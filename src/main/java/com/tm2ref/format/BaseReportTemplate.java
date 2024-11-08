/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.format;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.tm2ref.custom.ct2.ITextUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.global.STException;
import com.tm2ref.ref.RcCheckUtils;
import com.tm2ref.report.ReportData;
import com.tm2ref.report.ReportTemplate;
import com.tm2ref.report.ReportUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.MessageFactory;
import com.tm2ref.util.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public abstract class BaseReportTemplate extends StandardReportSettings implements ReportTemplate {

    public static float MAX_CUSTLOGO_W_V2 = 110; // 80
    public static float MAX_CUSTLOGO_H_V2 = 60;  // 40
    
    public boolean devel = false;
    public Image custLogo = null;

    public ReportData reportData = null;

    public Document document = null;
    public ByteArrayOutputStream baos;
    public PdfWriter pdfWriter;

    public float pageWidth = 0;
    public float pageHeight = 0;
    public float usablePageHeight = 0;
    public String title;

    public float headerHgt;
    public float footerHgt;
    public float lastY = 0;

    public TableBackground dataTableEvent;
    public TableBackground tableHeaderRowEvent;

    public ReportUtils reportUtils;

    public float PAD = 5;
    public float TPAD = 8;

    // float bxX;
    // float bxWid;
    //float barGrphWid;
    //float barGrphX;
    public float lineW = 0.8f;


    public float currentYLevel = 0;
    public float previousYLevel = 0;

    public java.util.List<String> prepNotes;
    
    public String competencySummaryStr = null;
    
    public RcCheckUtils rcCheckUtils;
    

    @Override
    public abstract byte[] generateReport() throws Exception;
    

    public synchronized void initFonts() throws Exception
    {
        initSettings( reportData );

        String logoUrl = reportData.getReportCompanyImageUrl();
        if( logoUrl == null )
            logoUrl = reportData.o.getReportLogoUrl() ;
        if( logoUrl != null && StringUtils.isCurlyBracketed( logoUrl ) )
            logoUrl = RuntimeConstants.getStringValue( "translogoimageurl" );

        try
        {
            custLogo = (logoUrl == null || logoUrl.isBlank()) ? null : Image.getInstance( com.tm2ref.util.HttpUtils.getURLFromString( logoUrl ) );
        }
        catch( Exception e )
        {
            if( logoUrl!=null && logoUrl.trim().toLowerCase().startsWith("https:"))
            {
                LogService.logIt( "BaseReportTemplate.initFonts() NONFATAL error getting custLogo. Will try http instead of https. logo=" + logoUrl );       
                String logo2 = "http:" + logoUrl.trim().substring(6, logoUrl.length());
                
                try
                {
                    custLogo = Image.getInstance( new URI( logo2 ).toURL() );                    
                }                
                catch( IOException ee )
                {
                    int orgId=reportData==null || reportData.getO()==null ? 0 : reportData.getO().getOrgId();                    
                    if( orgId<=0 && reportData!=null && reportData.getRc()!=null )
                        orgId = reportData.getRc().getOrgId();
                    
                    LogService.logIt( "BaseReportTemplate.initFonts() NONFATAL error getting custLogo using http. Will use null. OrgId=" + orgId + ", logo=" + logoUrl + ", logo2=" + logo2 + ", " + ee.toString() + ", " + ee.getMessage() );                    

                    if( reportData.getO()!=null && reportData.getO().getReportLogoUrl()!=null && reportData.getO().getReportLogoUrl().equals(logoUrl))
                    {                    
                        try
                        {
                            UserFacade uf = UserFacade.getInstance();
                            LogService.logIt( "BaseReportTemplate.initFonts() Removing erroneous image reference for OrgId=" + orgId + ", logo=" + logoUrl + ", " + ee.toString() + ", " + ee.getMessage() );                    
                            reportData.getO().setReportLogoUrl(null);
                            uf.saveOrg(reportData.getO());
                        }
                        catch( Exception eee )
                        {
                            LogService.logIt( eee, "BaseReportTemplate.initFonts() Could not remove erroneous image reference for OrgId=" + orgId );                                            
                        }
                    }
                }
                catch( Exception ee )
                {
                    int orgId=reportData==null || reportData.getO()==null ? 0 : reportData.getO().getOrgId();                    
                    if( orgId<=0 && reportData!=null && reportData.getRc()!=null )
                        orgId = reportData.getRc().getOrgId();
                    LogService.logIt( ee, "BaseReportTemplate.initFonts() NONFATAL error getting custLogo using http. OrgId=" + orgId + ". Will use null. logo=" + logoUrl + ", logo2=" + logo2 );                    
                }                
            }                        
            else
            {
                LogService.logIt( "BaseReportTemplate.initFonts() NONFATAL error getting custLogo. Will use null. logo= " + logoUrl + ", Exception=" + e.toString() );
            }
        }


        // !reportData.hasCustLogo() ? null : Image.getInstance( reportData.getCustLogoUrl() );

        if( custLogo != null )
        {
            float imgSclW = 100;
            float imgSclH = 100;
            // float maxImgWid = 80;
            // float maxImgHgt = 40;

            if( custLogo.getWidth() > MAX_CUSTLOGO_W )
                imgSclW = 100 * MAX_CUSTLOGO_W/custLogo.getWidth();

            if( custLogo.getHeight() > MAX_CUSTLOGO_H )
                imgSclH = 100 * MAX_CUSTLOGO_H/custLogo.getHeight();

            imgSclW = Math.min( imgSclW, imgSclH );

            if( imgSclW < 100 )
                custLogo.scalePercent( imgSclW );

        }

        title = StringUtils.replaceStr( reportData.getReportName(), "[SCRIPTNAME]", reportData.getScriptName() );

        String reportCompanyName = reportData.getReportCompanyName();
        if( StringUtils.isCurlyBracketed( reportCompanyName ) )
            reportCompanyName = "                      ";

        if( reportCompanyName == null || reportCompanyName.isEmpty() )
            reportCompanyName = reportData.getOrgName();

        title = StringUtils.replaceStr( title, "[ORGNAME]", reportCompanyName );

        // LogService.logIt( "BaseCT2ReportTemplate.initFonts() title=" + title );
    }


    @Override
    public void init( ReportData rd ) throws Exception
    {
        reportData = rd; // new ReportData( rd.getTestKey(), rd.getTestEvent(), rd.getReport(), rd.getUser(), rd.getOrg() );

        devel = rd.getR().getFloatParam1()==1;
        
        initFonts();
        
        initColors();  
        
        if( 1==1 )
        {
            if( ct2Colors!=null )
                ct2Colors.clearBorders();

            scoreBoxBorderWidth = 0;
            lightBoxBorderWidth=0;
        }

        

        prepNotes = new ArrayList<>();

        //Rectangle layout = new Rectangle(PageSize.LETTER);
        //layout.setBackgroundColor(BaseColor.WHITE);
    
        //document = new Document(layout);         
        
        document = new Document( PageSize.LETTER );

        baos = new ByteArrayOutputStream();

        pdfWriter = PdfWriter.getInstance(document, baos);

        // LogService.logIt( "BaseCT2ReportTemplate.init() title=" + rd.getReportName() );
        
        StandardHeaderFooter hdr = new StandardHeaderFooter( document, rd.getLocale(), rd.getReportName(), reportData, this );

        pdfWriter.setPageEvent(hdr);

        document.open();
        document.setMargins(36, 36, 36, 36 );
        pageWidth = document.getPageSize().getWidth();
        pageHeight = document.getPageSize().getHeight();

        float[] hghts = hdr.getHeaderFooterHeights( pdfWriter );
        headerHgt = hghts[0];
        footerHgt = hghts[1];

        usablePageHeight = pageHeight - headerHgt - footerHgt - 4*PAD;

        // LogService.logIt( "BaseCT2ReportTemplate.init() pageDims=" + pageWidth + "," + pageHeight + ", margins: " + document.topMargin() + "," + document.rightMargin() + "," + document.bottomMargin() + "," + document.leftMargin() );

        dataTableEvent = new TableBackground( BaseColor.LIGHT_GRAY , 0.2f, BaseColor.WHITE );
        tableHeaderRowEvent = new TableBackground( null , 0, getTablePageBgColor() );
    }
    
    
    public Image getItextThumbImage( String url, float maxWid, boolean hasIds)
    {
        if( maxWid<=0 )
            maxWid=80;
        
        if( url==null || url.isBlank() )
            return null;
        try
        {
            Image photoImg = Image.getInstance( new URI( url ).toURL() );
            if( photoImg!=null )
            {
                 LogService.logIt( "BaseReportTemplate.getItextThumbImage() wid hgt=" + photoImg.getWidth() + "," + photoImg.getHeight() );
                 float scale = 1;
                 if( photoImg.getWidth()>maxWid )
                     scale = maxWid/photoImg.getWidth();
                 
                 else if( photoImg.getWidth()<=0 )
                 {
                     scale = 0.15f;
                 }
                 //else if( photoImg.getHeight()>100 )
                 //    scale = 100f/photoImg.getHeight();
                 
                 photoImg.scalePercent(scale*100f);
                // photoImg.getHeight();
            }
            return photoImg;
        }
        catch( Exception e )
        {
            LogService.logIt(e, "BaseReportTemplate.getItextThumbImage() url=" + url );
            return null;
        }
    }
    
    public void addPreparationNotesSection() throws Exception
    {
        try
        {
            // skip if need to. 
            boolean minOnly = reportData.getReportRuleAsBoolean("prepnotesoff");
            
            // LogService.logIt(  "BaseCT2ReportTemplate.addPreparationNotesSection() START" );
            if( prepNotes==null )
                prepNotes = new ArrayList<>();
            
            List<String> customNotes = null;             
            if( !minOnly && reportData.getR().getTextParam3()!=null && !reportData.getR().getTextParam3().isEmpty() )
            {
                customNotes = new ArrayList<>();                 
                String[] pns = reportData.getR().getTextParam3().split("\\|");                
                for( String pn : pns )
                {
                    pn=pn.trim();
                    if( pn.isEmpty() )
                        continue;
                    customNotes.add(pn);
                }
            }
            if( customNotes !=null && !customNotes.isEmpty() )
                prepNotes.addAll( customNotes );

            String minNote = this.getMinimalPrepNotesStr();            
            prepNotes.add( minNote );
             
            if( prepNotes.isEmpty() )
                return;

            //if( prepNotes.size()>1 )
            //    addNewPage();
            //else
            //    currentYLevel += 2*TPAD;
            

            // First create the table
            PdfPCell c;
            PdfPTable t = new PdfPTable( new float[] { 1f } );
            float outerWid = pageWidth - 2*CT2_MARGIN - 2*CT2_BOX_EXTRAMARGIN;
            t.setTotalWidth( outerWid );
            t.setLockedWidth( true );
            setRunDirection( t );

            if( reportData.getIsLTR() )
            {
                com.itextpdf.text.List cl = new com.itextpdf.text.List( com.itextpdf.text.List.UNORDERED, 12 );
                cl.setListSymbol( "\u2022");

                for( String ct : prepNotes )
                {
                    if( ct==null || ct.isEmpty() )
                        continue;
                    cl.add( new ListItem( new Paragraph( ct , getFont() ) ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( cl );
                setRunDirection( c );
                t.addCell(c);
            }
            else
            {
                PdfPTable tt = new PdfPTable( new float[] { 1f } );
                // t.setHorizontalAlignment( Element.ALIGN_CENTER );
                tt.setTotalWidth( outerWid - 20 );
                tt.setLockedWidth( true );
                setRunDirection( tt );

                c = tt.getDefaultCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setPadding( 5 );
                setRunDirection( c );

                for( String ct : prepNotes )
                {
                    if( ct.isEmpty() )
                        continue;

                    tt.addCell( new Phrase(ct, getFont() ) );
                }

                c = new PdfPCell();
                c.setBorder( Rectangle.NO_BORDER );
                c.setBackgroundColor( BaseColor.WHITE );
                //c.setBorderColor( ct2Colors.scoreBoxBorderColor );
                c.setPaddingTop( 8 );
                c.setPaddingLeft(10);
                c.setPaddingRight(5);
                c.setPaddingBottom( 14 );
                c.addElement( tt );
                setRunDirection( c );
                t.addCell(c);
            }

            float thgt = t.calculateHeights();
            
            previousYLevel =  currentYLevel;
            float y = currentYLevel;
            if( thgt + 75 > y )
            {
                LogService.logIt( "BaseReportTemplate.addPreparationNotesSection() ZZZ.2  Too close to bottom of page, Adding new page. currentYLevel=" + currentYLevel + ", thgt=" + thgt );
                addNewPage();
                y = currentYLevel;
            }            
            
            y = addTitle( y, lmsg( "g.PreparationNotes" ), null );
            currentYLevel = addTableToDocument(y, t, false );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addPreparationNotesSection()" );
            throw new STException( e );
        }
    }
    

    
    protected String getMinimalPrepNotesStr() throws Exception
    {
        Calendar cal = new GregorianCalendar();            
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm z");            
        String dtStr = df.format( cal.getTime() );        
        if( reportData.getReportRuleAsBoolean( "hidedatespdf" ) )
            dtStr="***";

        String  note = "Internal Use: Rc: " + reportData.getRc().getRcCheckId() + ", R: " + reportData.getR().getReportId() + ", loc: " + reportData.getLocale().toString() + ", " + dtStr;
        if( reportData.getRc().getUserAgent()!=null && !reportData.getRc().getUserAgent().isBlank() )
            note += "\nUser-Agent: " + reportData.getRc().getUserAgent();        
        return note;
    }
    
    
    
    
    @Override
    public boolean getIsReportGenerationPossible()
    {
        if( reportData==null )
            return false;
        return true;
    }
    
    
    
    
    @Override
    public void dispose() throws Exception {
        
    }

    @Override
    public Locale getReportLocale() {
        return null;
    }

    
    public float addTitle( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            if( !reportData.getIsLTR() )
                return addTitleRTL( startY,  title,  subtitle );

            if( startY > 0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    LogService.logIt( "BaseReportTemplate.addTitle() Add NEW PAGE startY=" + startY + ", ulY=" + ulY + ", footerHgt=" + footerHgt );
                    document.newPage();
                    startY = 0;
                    currentYLevel = pageHeight - PAD -  headerHgt;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            // Add Title
            ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // No subtitle
            if( subtitle==null || subtitle.isBlank() )
                return y;

            // Change getFont()
            fnt =  getFont();

            float leading = fnt.getSize();

            float spaceLeft = y - PAD - footerHgt;

            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() y for title=" + y + ", spaceleft=" + spaceLeft );

            float txtW = pageWidth - 2*CT2_MARGIN-2*CT2_TEXT_EXTRAMARGIN;

            float txtHght = ITextUtils.getDirectTextHeight( pdfWriter, subtitle, txtW, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt);

            y -=  PAD;//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();
            // y -=  getHeaderFontXLarge().getSize();//  + fnt.getSize(); // + txtHght; //   1.5*PAD + txtHght - 1.1*fnt.getSize();

            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() y for TEXT BOX=" + y + ", spaceleft=" + spaceLeft + ", txtHght=" + txtHght );

            // float txtW = pageWidth - 2*CT2_MARGIN - 2*CT2_TEXT_EXTRAMARGIN; // - 4*PAD;

            float txtLlx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN; // + 2*PAD;
            float txtUrx = CT2_MARGIN + CT2_TEXT_EXTRAMARGIN + txtW;

            // if have room, draw it here. Otherwise, need to use columnText. If RTL need to use Column Text anyway.
            if( reportData.getIsLTR() && txtHght <= spaceLeft )
            {
                // y -= txtHght;
                Rectangle rect = new Rectangle( txtLlx, y-txtHght, txtUrx, y  );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() rect.left=" + rect.getLeft() + " rect.bottom=" + rect.getBottom()+ ", right=" + rect.getRight() + ", rect.top=" + rect.getTop() + ", " + rect.toString() );

                ITextUtils.addDirectText(  pdfWriter, subtitle, rect, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, leading, fnt, false );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() enough space is avail on page txtHght=" + txtHght + " vs spaceLeft=" + spaceLeft + ", currentYLevelBefore=" + currentYLevel + ", currentLevelAfter=y=" + (y-txtHght) );
                currentYLevel = y - txtHght;

                return currentYLevel;
            }

            else
            {
                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() RTL or overview text height is too high at " + txtHght + " vs spaceLeft=" + spaceLeft + ", using columns." );

                // llx,lly,urx,ury
                Rectangle colDims1 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, footerHgt + spaceLeft );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

                //float c2h = txtHght - spaceLeft;

                Rectangle colDims2 = new Rectangle( txtLlx, footerHgt + 2*PAD, txtUrx, pageHeight - headerHgt - 2*PAD );

                // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col2 dims=" + colDims2.getLeft() + "," + colDims2.getBottom() + " - " + colDims2.getRight() + "," + colDims2.getTop() );

                ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );

                setRunDirection( ct );

                Phrase p = new Phrase( subtitle, fnt );

                // p.setLeading( leading );
                ct.setLeading( leading ); // fnt.getSize() );

                ct.addText( p );

                ct.setSimpleColumn( colDims1.getLeft(), colDims1.getBottom(), colDims1.getRight(), colDims1.getTop() );
                // ct.setSimpleColumn( colDims1 );

                int status = ct.go();

                while( ColumnText.hasMoreText(status) )
                {
                    // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() adding second column "  );

                    document.newPage();

                    ct.setSimpleColumn( colDims2.getLeft(), colDims2.getBottom(), colDims2.getRight(), colDims2.getTop() );

                    ct.setYLine( colDims2.getTop() );

                    status = ct.go();

                    currentYLevel = ct.getYLine();
                }


                return currentYLevel;
            }
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addTitleAndSubtitle()" );
            throw new STException( e );
        }
    }


    public float addTitleRTL( float startY, String title, String subtitle ) throws Exception
    {
        try
        {
            if( startY > 0 )
            {
                float ulY = startY - 16* PAD;

                if( ulY < footerHgt + 3*PAD )
                {
                    document.newPage();

                    startY = 0;
                }
            }

            previousYLevel =  currentYLevel;

            Font fnt =   getHeaderFontXLarge();

            float y = startY>0 ? startY - fnt.getSize() - TPAD :  pageHeight - headerHgt - fnt.getSize() - TPAD;
            // float y = startY>0 ? startY - fnt.getSize() - 2*PAD :  pageHeight - headerHgt - fnt.getSize() - 2*PAD;

            // float y = previousYLevel - 6*PAD - getFont().getSize();

            PdfPTable t = new PdfPTable( 1 );

            t.setTotalWidth( new float[] { pageWidth-2*CT2_MARGIN- 2*CT2_TEXT_EXTRAMARGIN } );
            t.setLockedWidth( true );
            setRunDirection(t);

            PdfPCell c = t.getDefaultCell();
            c.setPadding( 0 );
            c.setBorder( Rectangle.NO_BORDER );
            c.setBorderWidth( 0 );
            setRunDirection(c);

            t.addCell( new Phrase( title , fnt ) );
            
            if( subtitle != null && !subtitle.isEmpty() )
            {
                c = t.getDefaultCell();
                c.setPadding( 0 );
                c.setBorder( Rectangle.NO_BORDER );
                c.setBorderWidth( 0 );
                setRunDirection(c);

                t.addCell( new Phrase( subtitle ,  getFont() ) );                
            }

            float ht = t.calculateHeights(); //  + 500;

            // float yy = pageHeight/2 - (pageHeight/2 - ht)/2;

            float tw = t.getTotalWidth();

            float tableX = (pageWidth - tw )/2;

            t.writeSelectedRows(0, -1,tableX, y, pdfWriter.getDirectContent() );


            currentYLevel = y - ht;

            return currentYLevel;

            // Add Title
            // ITextUtils.addDirectText( pdfWriter, title, CT2_MARGIN + CT2_TEXT_EXTRAMARGIN, y, reportData.getIsLTR() ? Element.ALIGN_LEFT : Element.ALIGN_RIGHT, fnt, false);

            // return y;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "BaseReportTemplate.addTitleRTL()" );

            throw new STException( e );
        }
    }

    
    
    
    
    
    
    
    
    
    
    
    public float addTableToDocument( float startY, PdfPTable t, boolean onePageIfPossible) throws Exception
    {
            float ulY = startY - 2*PAD;  // 4* PAD;

            float tableHeight = t.calculateHeights(); //  + 500;
            float tableHeaderHeight = t.getHeaderRows() >0 ? t.getHeaderHeight() : 0;

            int rowCount = t.getRows().size(); //  - t.getHeaderRows() - t.getFooterRows();

            // LogService.logIt( "BaseReportTemplate.addTableToDocument() startY=" + startY + ", rowCount=" + rowCount + ", tableHeight=" + tableHeight + ", tableHeaderHeight=" + tableHeaderHeight );
            
            float maxRowHeight=0;

            float[] rowHgts = new float[rowCount];

            for( int i=0; i<rowCount; i++ )
            {
                rowHgts[i]=t.getRowHeight(i);
                maxRowHeight = Math.max( maxRowHeight, rowHgts[i] );
                // LogService.logIt( "BaseReportTemplate.addTableToDocument() row=" + i + ", rowHeight=" + rowHgts[i] );
            }
            

            float firstRowHgt = rowHgts.length>t.getHeaderRows() ? rowHgts[t.getHeaderRows()] : 0;

            float heightAvailNewPage = pageHeight - headerHgt - 3*PAD - footerHgt - 3*PAD - tableHeaderHeight;

            if( maxRowHeight >= heightAvailNewPage*0.5 )
                t.setSplitLate(false);

            //LogService.logIt( "BaseReportTemplate.addTableToDocument() rows=" + rowCount + ", tableHeight=" + tableHeight + ", tableHeaderHeight=" + tableHeaderHeight + ", maxRowHeight=" + maxRowHeight + ", splitLate=" + t.isSplitLate() );
            
            if( onePageIfPossible && tableHeight<=(heightAvailNewPage - 3*PAD) && tableHeight > (ulY - footerHgt - 3*PAD) )
            {
                //LogService.logIt( "BaseReportTemplate.addTableToDocument() CCC adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
                // currentYLevel = pageHeight - PAD -  headerHgt;
            }
            
            // If first row doesn't fit on this page
            else if( firstRowHgt > ulY- footerHgt - 3*PAD - tableHeaderHeight ) // ulY < footerHgt + 8*PAD )
            {
                //LogService.logIt( "BaseReportTemplate.addTableToDocument() DDD adding new page. "  );
                document.newPage();

                ulY = pageHeight - headerHgt - 3*PAD;
            }

            //if( maxRowHeight > usablePageHeight )
            //    t.setSplitLate(false);
            float tableXlft = CT2_MARGIN + CT2_BOX_EXTRAMARGIN;
            float tableXrgt = CT2_MARGIN + CT2_BOX_EXTRAMARGIN + t.getTotalWidth();

            Rectangle colDims = new Rectangle( tableXlft, footerHgt + 3*PAD, tableXrgt, ulY );
            // LogService.logIt( "BaseReportTemplate.addAssessmentOverview() col1 dims=" + colDims1.getLeft() + "," + colDims1.getBottom() + " - " + colDims1.getRight() + "," + colDims1.getTop() );

            float heightNoHeader = tableHeight - tableHeaderHeight;


            Object[] dta = calcTableHghtUsed(colDims.getTop() - colDims.getBottom() - tableHeaderHeight, 0, t.getHeaderRows(), rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
            int nextIndex = (Integer) dta[0];
            float heightUsedNoHeader = (Float) dta[1];
            float residual = (Float) dta[2];

            // LogService.logIt( "BaseReportTemplate.addTableToDocument() tableHeight=" + t.calculateHeights() + ", headerHeight=" + headerHeight + ", maxRowHeight=" + maxRowHeight + ", heightAvailNewPage=" + heightAvailNewPage + ", initial heightUsedNoHeader=" + heightUsedNoHeader + ", residual=" + residual );


            ColumnText ct = new ColumnText( pdfWriter.getDirectContent() );
            setRunDirection( ct );

            // NOTE - this forces Composite mode (using ColumnText.addElement)
            ct.addElement( t );

            ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );
            // ct.setSimpleColumn( colDims1 );


            int status = ct.go();

            // int linesWritten = ct.getLinesWritten();

            // LogService.logIt( "BaseReportTemplate.addTableToDocument() initial lines written. NO_MORE_COLUMN=" + ColumnText.NO_MORE_COLUMN + ", NO_MORE_TEXT=" + ColumnText.NO_MORE_TEXT  );

            int pages = 0;

            float heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

            float hgtUsedThisPage = 0;

            // If need to add any pages
            // while( ColumnText.hasMoreText( status ) && heightNeededNoHeader>0 && pages<20 )
            while( ColumnText.hasMoreText( status ) && heightNeededNoHeader > -300 && pages<20 ) // 6-28-2019 - removed the restriction on height as there's something not quite right.
            {
                // Top of writable area
                ulY = pageHeight - headerHgt - 3*PAD;


                dta = calcTableHghtUsed(heightAvailNewPage, residual, nextIndex, rowCount-t.getFooterRows(), t.isSplitLate(), rowHgts ); //   colDims.getTop() - colDims.getBottom() - headerHeight;
                nextIndex = (Integer) dta[0];
                hgtUsedThisPage = (Float) dta[1];
                residual = (Float) dta[2];

                heightUsedNoHeader += hgtUsedThisPage;

                heightNeededNoHeader = heightNoHeader - heightUsedNoHeader;

                // LogService.logIt( "BaseReportTemplate.addTableToDocument() AFTER adding next page. hgtUsedThisPage=" + hgtUsedThisPage +  ", Total HeightNeededNoHeader=" + heightNeededNoHeader + ", Total HeightUsedNoHeader=" + heightUsedNoHeader + ", pages=" + pages );

                colDims = new Rectangle( tableXlft, ulY - heightAvailNewPage , tableXrgt, ulY );

                document.newPage();

                ct.setSimpleColumn( colDims.getLeft(), colDims.getBottom(), colDims.getRight(), colDims.getTop() );

                ct.setYLine( colDims.getTop() );

                status = ct.go();

                // linesWritten += ct.getLinesWritten();

                // LogService.logIt( "BaseReportTemplate.addTableToDocument() status=" + status + ", ColumnText.hasMoreText( status )=" + ColumnText.hasMoreText( status ) );

                pages++;
            }

            return ct.getYLine();
    }



    /**
     * Returns
     *    Next Index -- if three is a residual, it's the index of the residual, else it's the next index
     *    Amount of height used
     *    Residual height unused from split cell
     *
     * @param maxRoom
     * @param startIndex
     * @param maxIndex
     * @param isSplitLate
     * @param rowHgts
     * @return
     */
    public Object[] calcTableHghtUsed( float maxRoom, float prevResidual, int startIndex, int maxIndex, boolean isSplitLate, float[] rowHgts )
    {
        // LogService.logIt( "BaseReportTemplate.calcTableHghtUsed( maxRoom=" + maxRoom + ", prevResidual=" + prevResidual + ", startIndex=" + startIndex + ", maxIndex=" + maxIndex + ", isSplitLate=" + isSplitLate + ", " + ")");

        Object[] dta = new Object[] {(int)(startIndex) , (float)(0), (float)(0) };

        if( rowHgts.length<=startIndex )
            return dta;

        float hgt = 0;
        float resid = 0;

        if( prevResidual>0 )
        {
            // Bigger than max
            if( prevResidual>= maxRoom )
            {
                dta[1] = (float)( maxRoom );
                dta[2] = (float)( prevResidual -  maxRoom );

                if( prevResidual== maxRoom)
                {
                    dta[0]=startIndex+1;
                    dta[2] = (float)( 0 );
                }

                return dta;
            }

            hgt = prevResidual;
            maxRoom -= prevResidual;
            startIndex++;
        }

        for( int i=startIndex; i<rowHgts.length && i<=maxIndex; i++ )
        {
            if( rowHgts[i] + hgt == maxRoom )
            {
                dta[0]=(int)( i+1 );
                dta[1] = (float)(hgt);
                return dta;
            }

            if( rowHgts[i] + hgt > maxRoom )
            {
                if( i==startIndex || !isSplitLate )
                {
                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() AAA i=" + i + ", hgt=" + hgt );

                    resid = rowHgts[i] - (maxRoom-hgt);
                    dta[2] = (float)( resid );
                    hgt = maxRoom;

                    // LogService.logIt( "BaseCoreTestReportTemplt.calcTableHghtUsed() BBB hgt=" + hgt + ", resid=" + resid );
                }

                dta[0] = (int)(i);
                dta[1] = (float)(hgt);
                return dta;
            }

            hgt += rowHgts[i];
        }

        dta[0] = (int)(maxIndex+1);
        dta[1] = (float)(hgt);
        return dta;
    }
    
    public void addNewPage() throws Exception
    {
        document.newPage();
        this.currentYLevel = pageHeight - PAD -  headerHgt;
    }

    
    
    public float getMinYForNewSection()
    {
        if( pageHeight>100 )
            return 0.2f*pageHeight;
        
        return 200f;
    }
    
    
    public void closeDoc() throws Exception
    {
        if( document != null && document.isOpen() )
            document.close();

        document = null;
    }

    public byte[] getDocumentBytes() throws Exception
    {
        if( baos == null )
            return null;

        return baos.toByteArray();
    }


    
    
    
    // Standard Locale key
    public String lmsg( String key )
    {
        return MessageFactory.getStringMessage( reportData.getLocale() , key, null );
    }

    
    // Standard Locale key
    public String lmsg( String key, String[] prms )
    {
        return MessageFactory.getStringMessage( reportData.getLocale() , key, prms );
    }
    
    
    

    public void setRunDirection( PdfPCell c )
    {
        if( c == null || reportData == null || reportData.getLocale() == null )
            return;

        // if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        c.setRunDirection( reportData.getTextRunDirection() );
    }

    public void setRunDirection( PdfPTable t )
    {
        if( t == null || reportData == null || reportData.getLocale() == null )
            return;

        t.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }

    public void setRunDirection( ColumnText ct )
    {
        if( ct == null || reportData == null || reportData.getLocale() == null )
            return;

        ct.setRunDirection( reportData.getTextRunDirection() );

        //if( I18nUtils.isTextRTL( reportData.getLocale() ) )
        //    t.setRunDirection( PdfWriter.RUN_DIRECTION_RTL );
    }
    
    
    
    
}
