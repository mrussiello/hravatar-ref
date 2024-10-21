/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.custom.ct2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2ref.service.LogService;

/**
 *
 * @author Mike
 */
public class CoverBlueBarCellEvent implements PdfPCellEvent
{
    // #0b508b
    static BaseColor bgColor = new BaseColor( 0x0b, 0x50, 0x8b);
    float barWidth = 150;
    float barHeight = 12;
    

    public CoverBlueBarCellEvent()
    {
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

            float barWid = Math.min( barWidth, wid);
            float barHgt = Math.min( barHeight, hgt);
            
            // LogService.logIt( "CellBackgroundCellEvent.cellLayout() bgColor=" + bgColor.getRed() + "," + bgColor.getGreen() + "," + bgColor.getBlue() + ", fgColor=" + fgColor.getRed() + "," + fgColor.getGreen() + "," + fgColor.getBlue() );
            
            
            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            pcb.setColorFill( bgColor );
            pcb.rectangle(llx, lly + (hgt - barHgt)/2, barWid, barHgt);
            pcb.fill();
                                    
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CoverBlueBarCellEvent.cellLayout() " );
        }


    }



}
