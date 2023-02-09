/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.custom.ct2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2ref.service.LogService;


/**
 *
 * @author Mike
 */
public class RcHiLoCompetencyCellEvent implements PdfPCellEvent
{
    String scoreStr;
    float score;
    BaseColor barColor;
    BaseFont textFont;


    public RcHiLoCompetencyCellEvent( String scoreStr, float score, BaseColor color , BaseFont font )
    {
        this.score = score;
        this.scoreStr = scoreStr;
        this.barColor = color;
        this.textFont = font;
    }



    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();

            pcb.setColorFill( barColor );
            pcb.setLineWidth(0.5f);
            pcb.setColorStroke( barColor );

            // Draw the bar first
            float wid = rctngl.getWidth()-25;
            float hgt = rctngl.getHeight()-2;
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom() + 1;

            // LogService.logIt( "RcHiLoCompetencyCellEvent.cellLayout() wid=" + wid + ", hgt=" + hgt + ", lly=" + lly );

            float lineHgt = 10;
            int bup = 0; //  Math.round(hgt - lineHgt - 5);
            //if( bup<0 )
            //    bup=0;

            float barWid = wid*(score/10);

            // LogService.logIt( "RcHiLoCompetencyCellEvent.cellLayout() score=" + score + ", wid=" + wid + ", hgt=" + hgt + ", barWid=" + barWid+ ", lly=" + lly );
            
            //PdfShading shade = PdfShading.simpleAxial( pcb.getPdfWriter(), llx, lly, llx+barWid, lly+lineHgt, row.getBaseColor(), row.getBaseColor(), false, false );
            // pcb.paintShading(shade);

            //PdfShadingPattern sp = new PdfShadingPattern(shade);
            //pcb.setShadingFill(sp);
            pcb.rectangle( llx, lly+bup, barWid, lineHgt );
            pcb.fillStroke();


            float txtPosX;
            float txtPosY = lly + 2 + bup;
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize( textFont, 9 );
            
            txtPosX = llx + barWid + 4;
            // pcb.setColorFill( barColor );
            pcb.showTextAligned( Element.ALIGN_LEFT, scoreStr, txtPosX, txtPosY, 0);
            pcb.endText();
            
            pcb.restoreState();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "RcHiLoCompetencyCellEvent.cellLayout() " );
        }


    }



}
