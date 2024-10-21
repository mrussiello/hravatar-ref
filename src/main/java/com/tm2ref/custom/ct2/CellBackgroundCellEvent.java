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
public class CellBackgroundCellEvent implements PdfPCellEvent
{
    boolean topLeft=false;
    boolean topRight=false;
    boolean bottomRight=false;
    boolean bottomLeft=false;
    BaseColor colorToUse = null;
    //boolean both = false;
    //boolean left = true;
    float radius = 6;
    boolean ltr = true;
    

    public CellBackgroundCellEvent( boolean ltr, BaseColor colorToUse, boolean topLeft, boolean topRight, boolean bottomRight, boolean bottomLeft)
    {
        this.ltr=ltr;
        this.colorToUse=colorToUse;
        this.topLeft=ltr ? topLeft : topRight;
        this.topRight=ltr ? topRight : topLeft;
        this.bottomRight=ltr ? bottomRight : bottomLeft;
        this.bottomLeft=ltr ? bottomLeft : bottomRight;
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

            PdfContentByte pcb = pcbs[ PdfPTable.BACKGROUNDCANVAS ];

            pcb.saveState();

            pcb.setLineWidth(0 );

            float borderWidth = 0.4f;
            
            pcb.setColorStroke(BaseColor.WHITE);
            pcb.setColorFill( BaseColor.WHITE );
            pcb.rectangle(llx, lly, wid, hgt);
            pcb.fillStroke();
            
            
            pcb.setColorFill( colorToUse );   
            pcb.setColorStroke(colorToUse);
            pcb.roundRectangle(llx, lly + borderWidth, wid, hgt-2*borderWidth, radius);
            pcb.fillStroke();
            
            if( !topLeft )
            {
                pcb.rectangle(llx-borderWidth, lly + hgt/2 + borderWidth, wid/2, (hgt/2) - 2*borderWidth);
                pcb.fillStroke();
            }

            if( !bottomLeft )
            {
                pcb.rectangle(llx-borderWidth, lly + borderWidth, wid/2, (hgt/2) - 2*borderWidth);
                pcb.fillStroke();
            }
            
            if( !topRight )
            {
                pcb.rectangle(llx+wid/2, lly + hgt/2 + borderWidth, wid/2+ borderWidth, (hgt/2) - 2*borderWidth);
                pcb.fillStroke();
            }

            if( !bottomRight )
            {
                pcb.rectangle(llx+wid/2, lly + borderWidth, wid/2+borderWidth, (hgt/2) - 2*borderWidth);
                pcb.fillStroke();
            }

            
            pcb.restoreState();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "CellBackgroundCellEvent.cellLayout() ltr=" + ltr + ", topLeft=" + topLeft + ", topRight=" + topRight + ", botttomRight=" + bottomRight + ", bottomLeft=" + bottomLeft );
        }


    }



}
