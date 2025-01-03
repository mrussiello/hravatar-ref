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
public class CoverIncludedBarCellEvent implements PdfPCellEvent
{
    static float MAX_BAR_HEIGHT = 20;
    static float MAX_BAR_WIDTH = 180;
    
    // #0b508b
    static BaseColor bgColor = new BaseColor( 0x0b, 0x50, 0x8b);
    
    
    // Image blueArrowImage;
    String text;
    Font font;
    
    

    public CoverIncludedBarCellEvent( String text, Font font )
    {
        // this.blueArrowImage=blueArrowImage;
        this.text=text;
        this.font = font;
    }
    
    
    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {            
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom();
            float cellWidth = rctngl.getRight()-rctngl.getLeft();
            float cellHeight = rctngl.getTop()-rctngl.getBottom();
            float barHeight = Math.min(MAX_BAR_HEIGHT, cellHeight);
            float barArrowWidth = 0.5f*barHeight;
            float barWidth = Math.min(MAX_BAR_WIDTH, cellWidth );
            float barRectWidth = barWidth - barArrowWidth;
            float barXPad = 0; // barWidth<cellWidth ? (cellWidth - barWidth)/2f : 0;
            float barYPad = barHeight<cellHeight ? (cellHeight - barHeight)/2f : 0;

            BaseFont bfont = font.getBaseFont();
            // BaseColor color = font.getColor();
            float textWidth = bfont.getWidthPoint(text, font.getSize() );
            
            float fontSize = font.getSize();
            
            while( textWidth>barRectWidth-4 && fontSize>5 )
            {
                fontSize--;
                textWidth = bfont.getWidthPoint(text, fontSize );
            }

            float textHeight = bfont.getAscentPoint(text, fontSize ) - bfont.getDescentPoint(text, fontSize );
            
            while( textHeight>barHeight && fontSize>5 )
            {
                fontSize--;
                textHeight = bfont.getAscentPoint(text, fontSize ) - bfont.getDescentPoint(text, fontSize );
            }
                        
            float textPad =(barHeight - textHeight)/2f;
            
            // LogService.logIt( "CoverIncludedBarCellEvent.cellLayout() cellWidth=" + cellWidth + ", cellHeight=" + cellHeight + ", barWidth=" + barWidth + ",barHeight=" + barHeight + ", barArrowWidth=" + barArrowWidth + ", textHeight=" + textHeight + ", textWidth=" + textWidth + ", fontSize=" + fontSize + ", textPad=" + textPad + ", text=" + text );
            
            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setColorStroke(bgColor);
            pcb.setColorFill( bgColor );
            pcb.moveTo(llx + barXPad, lly + barYPad);
            pcb.rectangle(llx + barXPad, lly + barYPad, barRectWidth, barHeight);
            pcb.fill();

            pcb.moveTo(llx + barXPad + barRectWidth, lly + barYPad);
            pcb.lineTo(llx + barXPad + barRectWidth, lly + barYPad + barHeight);
            pcb.lineTo(llx + barXPad + barRectWidth + barArrowWidth, lly + barYPad + barHeight/2f);
            pcb.lineTo(llx + barXPad + barRectWidth, lly + barYPad);
            pcb.fill();
                        
            pcb.restoreState();
                 
            // Now draw text
            
            pcb = pcbs[ PdfPTable.TEXTCANVAS ];
            
            pcb.saveState();
     
            pcb.setColorFill( BaseColor.WHITE );
            pcb.beginText();
            pcb.setTextRenderingMode( PdfContentByte.TEXT_RENDER_MODE_FILL );
            pcb.setFontAndSize(bfont, font.getSize());                        
            pcb.showTextAligned(Element.ALIGN_LEFT, text, llx + barXPad+ 5, lly + barYPad + textPad, 0);
            pcb.endText();
                        
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CoverBlueBarCellEvent.cellLayout() " );
        }


    }



}
