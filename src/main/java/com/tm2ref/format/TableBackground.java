/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.format;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

/**
 *
 * @author Mike
 */
public class TableBackground implements PdfPTableEvent, PdfPCellEvent
{
    float borderWidth = 0.2f;
    BaseColor borderColor = BaseColor.LIGHT_GRAY;
    BaseColor bgColor = BaseColor.WHITE;
    float radius = 4;


    public TableBackground()
    {}

    public TableBackground( BaseColor brdCol, float brdWid, BaseColor bgCol, float r )
    {
        borderColor = brdCol;
        borderWidth = brdWid;
        bgColor = bgCol;
        radius = r;
    }

    public TableBackground( BaseColor brdCol, float brdWid, BaseColor bgCol )
    {
        borderColor = brdCol;
        borderWidth = brdWid;
        bgColor = bgCol;
    }


    @Override
    public void tableLayout(PdfPTable table,
                            float[][] width,
                            float[] height,
                            int headerRows,
                            int rowStart,
                            PdfContentByte[] canvas)
    {
            PdfContentByte bg;

            if( bgColor != null )
            {
                bg = canvas[PdfPTable.BASECANVAS];

                bg.saveState();

                bg.setColorFill(bgColor);

                bg.setLineWidth(borderWidth);

                // LogService.logIt( "Table Background.tableLayout() height[0]=" + height[0] + ", height[height.length - 1]=" + height[height.length - 1] );

                if( radius > 0 )
                    bg.roundRectangle(  width[0][0]-3,
                                        height[height.length - 1] - 4,
                                        width[0][ width[0].length -1 ] - width[0][0] + 6,
                                        height[0] - height[height.length - 1] + 8,
                                        radius );

                else
                    bg.rectangle(   width[0][0]-3,
                                    height[height.length - 1] - 4,
                                    width[0][ width[0].length -1 ] - width[0][0] + 6,
                                    height[0] - height[height.length - 1] + 8 );


                if( bgColor != null )
                    bg.fill();

                if( borderColor != null && borderWidth>0 )
                    bg.stroke();

                bg.restoreState();
            }

            if( borderColor != null && borderWidth>0 )
            {
                bg = canvas[PdfPTable.LINECANVAS];

                bg.setColorStroke( borderColor );
                bg.setLineWidth(borderWidth);

                bg.saveState();

                bg.setLineWidth(borderWidth);

                if( radius > 0 )
                    bg.roundRectangle(  width[0][0]-3,
                                        height[height.length - 1] - 4,
                                        width[0][ width[0].length -1 ] - width[0][0] + 6,
                                        height[0] - height[height.length - 1] + 8,
                                        radius );
                else
                    bg.rectangle(   width[0][0]-3,
                                    height[height.length - 1] - 4,
                                    width[0][ width[0].length -1 ] - width[0][0] + 6,
                                    height[0] - height[height.length - 1] + 8 );


                bg.stroke();

                bg.restoreState();
            }


    }


    @Override
    public void cellLayout( PdfPCell cell,
                            Rectangle rect,
                            PdfContentByte[] canvas)
    {
            PdfContentByte cb = canvas[PdfPTable.BACKGROUNDCANVAS];
            cb.saveState();

            if( radius > 0 )
                cb.roundRectangle(
                    rect.getLeft(), rect.getBottom(), rect.getWidth(),
                    rect.getHeight(), radius );
            else
                cb.rectangle(
                    rect.getLeft(), rect.getBottom(), rect.getWidth(),
                    rect.getHeight() );


            cb.setColorFill(bgColor);
            cb.fill();
            cb.restoreState();
    }

}
