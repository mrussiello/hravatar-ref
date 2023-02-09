/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.custom.ct2;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfGState;
import com.itextpdf.text.pdf.PdfShading;
import com.itextpdf.text.pdf.PdfShadingPattern;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.ShadingColor;

/**
 *
 * @author Mike
 */
public class ITextUtils
{

    public static void addDirectBox( PdfWriter pdfWriter, BaseColor color, float lineWid, float x, float y, float w, float h,  boolean isUnder ) throws Exception
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            // draw the border
            cb.saveState();

            cb.setColorStroke(color);
            cb.setLineWidth(lineWid);
            cb.rectangle(x, y, w, h);
            cb.stroke();
            cb.restoreState();
    }



    public static void addDirectShadedRect( PdfWriter pdfWriter, BaseColor color1, BaseColor color2, float x, float y, float w, float h, float r, float opacity, boolean isUnder, boolean isVertical ) throws Exception
    {
            PdfShading axial = PdfShading.simpleAxial(pdfWriter, x, y, x+w, y+h, color1, color2);

            PdfShadingPattern shading = new PdfShadingPattern(axial);

            addDirectColorRect(  pdfWriter, new ShadingColor( shading ), x, y, w, h, r, opacity, isUnder);
    }


    public static void addDirectVerticalGrid(PdfWriter pdfWriter, BaseColor color, float x, float y, float w, float h, float interval,  boolean isUnder) throws Exception
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            // draw the border
            // addDirectBox( pdfWriter, color, 0.8f, x, y, w, h, isUnder );

            // now draw each vert dashed line.
            cb.saveState();
            cb.setColorStroke(color);
            cb.setLineWidth(0.5f);
            cb.setLineDash(3,1);

            float llx = x + interval;
            float lly = y;
            float uly = y + h;

            while( llx < x + w - 0.1 )
            {
                cb.moveTo( llx, uly );
                cb.lineTo( llx, lly );

                llx += interval;
            }

            cb.stroke();
            cb.restoreState();
    }


    public static void addThreeSidedOutline( PdfWriter pdfWriter, BaseColor color, float x, float y, float w, float h, float lineW, boolean isUnder ) throws Exception
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            // Rectangle rect = new Rectangle(  );

            cb.saveState();
            cb.setColorStroke(color);
            cb.setLineWidth(lineW);

            cb.moveTo( x, y+h );
            cb.lineTo( x+w, y+h );
            cb.lineTo( x+w, y );
            cb.lineTo( x, y );

            cb.stroke();
            cb.restoreState();
    }



    public static void addDirectColorRect( PdfWriter pdfWriter, BaseColor color, float x, float y, float w, float h, float r, float opacity, boolean isUnder) throws Exception
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            // Rectangle rect = new Rectangle(  );

            cb.saveState();

            if( opacity < 1.0f )
            {
                PdfGState pgs = new PdfGState();
                pgs.setFillOpacity( opacity );
                pgs.setStrokeOpacity( opacity );

                pgs.setBlendMode( PdfGState.BM_NORMAL );
                cb.setGState(pgs);
            }

            cb.setColorFill( color );

            if( r<=0 )
                cb.rectangle( x, y, w, h );

            else
                cb.roundRectangle(x, y, w, h, r);

            cb.fill();
            cb.restoreState();
    }





    public static void addDirectImage(  PdfWriter pdfWriter, Image img, float x, float y, boolean isUnder ) throws Exception
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            cb.addImage( img, img.getScaledWidth(), 0,0,img.getScaledHeight(), x, y );
    }


    public static void addDirectText(  PdfWriter pdfWriter, String t, float x, float y, BaseFont font, int size, BaseColor color, boolean isUnder )
    {
            PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

            cb.saveState();
            cb.beginText();

            // over.setLineWidth(1.5f);
            // over.setColorStroke( color );
            cb.setColorFill( color );
            cb.setFontAndSize( font,  size );
            cb.setTextMatrix( x, y );
            cb.showText( t );
            cb.endText();
            cb.restoreState();
    }


    public static void addDirectText(  PdfWriter pdfWriter, String t, float x, float y, int alignment, Font fnt, boolean isUnder )
    {
        Phrase p = new Phrase( t, fnt );

        PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

        ColumnText.showTextAligned( cb, alignment, p, x, y, 0 );
    }


    public static float getDirectTextHeight( PdfWriter pdfWriter, String t, float colWid, int alignment, float leading, Font fnt ) throws Exception
    {
        if( t==null || t.isEmpty() )
            return 0;

        Paragraph p = new Paragraph( t, fnt );

        return  getDirectTextHeight(  pdfWriter, p,  colWid,  alignment,  leading,  fnt );
    }


    public static float getDirectTextHeight( PdfWriter pdfWriter, Paragraph p, float colWid, int alignment, float leading, Font fnt ) throws Exception
    {
        if( p==null )
            return 0;

        //Phrase p = new Phrase( t, fnt );

        if( leading == 0)
            leading = p.getLeading();

        PdfContentByte cb = pdfWriter.getDirectContent();

        ColumnText col = new ColumnText( cb );

        float hgt  = 10000;

        Rectangle rect = new Rectangle( 0,0,colWid,hgt);

        col.setSimpleColumn( p, rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop(), leading, alignment );

        col.go( true );

        return hgt - col.getYLine();
    }



    public static float getMaxChunkWidth( java.util.List<Chunk> chunkList )
    {
        return getMaxTextWidth( null, chunkList, null, 0 );
    }


    public static float getMaxTextWidth( java.util.List<String> textList, BaseFont font, float fontSize )
    {
        return getMaxTextWidth( textList, null, font, fontSize );
    }


    public static float getMaxTextWidth( java.util.List<String> textList, java.util.List<Chunk> chunkList, BaseFont font, float fontSize )
    {
        float w = 0;
        float tw;

        if( textList != null )
        {
            for( String text : textList )
            {
                tw = getTextWidth(  text,  font, fontSize );

                if( tw>w )
                    w = tw;
            }
        }

        if( chunkList != null )
        {
            for( Chunk chunk : chunkList )
            {
                if( chunk.getWidthPoint()>w )
                    w = chunk.getWidthPoint();
            }
        }

        return w;
    }



    public static float getTextWidth( String text, BaseFont font, float fontSize )
    {
        return font.getWidthPoint( text, fontSize );
    }


    public static float getChunckWidth( Chunk chunk )
    {
        return chunk.getWidthPoint();
    }


    public static void addDirectText(  PdfWriter pdfWriter, String t, Rectangle rect, int alignment, float leading, Font fnt, boolean isUnder ) throws Exception
    {
        Phrase p = new Phrase( t, fnt );

        if( leading == 0)
            leading = p.getLeading();

        // LogService.logIt( "ITextUtils.addDirectText() " + t + ", leading=" + leading + ", ll=" +  rect.getLeft() + "," + rect.getBottom() + " ul=" + rect.getRight() + "," + rect.getTop());

        PdfContentByte cb = isUnder ? pdfWriter.getDirectContentUnder() : pdfWriter.getDirectContent();

        ColumnText col = new ColumnText( cb );

        col.setSimpleColumn( p, rect.getLeft(), rect.getBottom(), rect.getRight(), rect.getTop(), leading, alignment );

        col.go();
    }



}
