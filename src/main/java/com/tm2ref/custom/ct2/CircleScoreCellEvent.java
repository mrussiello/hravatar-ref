/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.custom.ct2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
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
public class CircleScoreCellEvent implements PdfPCellEvent
{
    String scoreStr;
    BaseColor bgColor;
    BaseColor fgColor;
    float radius = 15;
    Font font;
    

    public CircleScoreCellEvent( String scoreStr, BaseColor bgColor, BaseColor fgColor, Font font )
    {
        this.scoreStr=scoreStr;
        this.bgColor=bgColor;
        this.fgColor=fgColor;
        this.font = font;
    }
    
    
    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {            
            
            // Draw the bar first
            float wid = rctngl.getWidth();
            float hgt = rctngl.getHeight();
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom();

            // LogService.logIt( "CellBackgroundCellEvent.cellLayout() bgColor=" + bgColor.getRed() + "," + bgColor.getGreen() + "," + bgColor.getBlue() + ", fgColor=" + fgColor.getRed() + "," + fgColor.getGreen() + "," + fgColor.getBlue() );
            
            
            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            pcb.setColorFill( BaseColor.WHITE );
            pcb.rectangle(llx, lly, wid, hgt);
            pcb.fill();
                        
            pcb.setColorFill( bgColor );
            pcb.circle(llx + wid/2, lly + hgt/2, radius);
            pcb.fill();
            
            BaseFont bfont = font.getBaseFont();
            // BaseColor color = font.getColor();
            float textHeight = bfont.getDescentPoint(scoreStr, font.getSize() ) - bfont.getAscentPoint(scoreStr, font.getSize() );
            
            pcb.setColorFill( this.fgColor );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(bfont, font.getSize());
            
            pcb.showTextAligned(Element.ALIGN_CENTER, scoreStr, llx + wid/2, lly + hgt/2 + textHeight/2, 0);
            pcb.endText();
                        
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CellBackgroundCellEvent.cellLayout() " );
        }


    }



}
