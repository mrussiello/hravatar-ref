/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.custom.ct2;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPCellEvent;
import com.itextpdf.text.pdf.PdfPTable;
import com.tm2ref.report.RcHistogramRow;
import com.tm2ref.service.LogService;


/**
 *
 * @author Mike
 */
public class HistogramBarCellEvent implements PdfPCellEvent
{

    RcHistogramRow row;


    public HistogramBarCellEvent( RcHistogramRow row )
    {
        this.row = row;
    }



    @Override
    public void cellLayout( PdfPCell ppc, Rectangle rctngl, PdfContentByte[] pcbs )
    {
        try
        {
            PdfContentByte pcb = pcbs[ PdfPTable.TEXTCANVAS ];

            pcb.saveState();

            pcb.setColorFill( row.getBaseColor() );
            pcb.setLineWidth(0.5f);
            pcb.setColorStroke( row.getBaseColor() );

            // Draw the bar first
            float wid = rctngl.getWidth()-2;
            float hgt = rctngl.getHeight()-2;
            float llx = rctngl.getLeft();
            float lly = rctngl.getBottom() + 1;

            // LogService.logIt( "HistogramBarCellEvent.cellLayout() wid=" + wid + ", hgt=" + hgt );

            float lineHgt = 10;
            int bup = Math.round(hgt - lineHgt - 5);
            if( bup<0 )
                bup=0;

            float barWid = 0; // wid*(Math.abs(row.getAvgScore())/row.getRcRatingScaleType().getMaxScore());
            
            if( Math.abs(row.getAvgScore())>row.getRcRatingScaleType().getMaxScore())
                barWid = wid;

            else if(row.getHistogramRoleTypeId()!=40 && row.getHistogramRoleTypeId()!=19 && (row.getAvgScore()<=0 || Math.abs(row.getAvgScore())<row.getRcRatingScaleType().getMinScore()))
                barWid = 0;

            else
                barWid = wid*(Math.abs(row.getAvgScore())/row.getRcRatingScaleType().getMaxScore());
            
            
            //PdfShading shade = PdfShading.simpleAxial( pcb.getPdfWriter(), llx, lly, llx+barWid, lly+lineHgt, row.getBaseColor(), row.getBaseColor(), false, false );
            // pcb.paintShading(shade);

            //PdfShadingPattern sp = new PdfShadingPattern(shade);
            //pcb.setShadingFill(sp);
            pcb.rectangle( llx, lly+bup, barWid, lineHgt );
            pcb.fillStroke();


             // float txtPosX;
            //float txtPosY = lly + 2;

            pcb.restoreState();

        }

        catch( Exception e )
        {
            LogService.logIt( e, "HistogramBarCellEvent.cellLayout() " );
        }


    }



}
