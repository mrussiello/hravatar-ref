/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.custom.ct2;

import com.itextpdf.text.BaseColor;

/**
 *
 * @author miker_000
 */
public class CT2Colors 
{
    public  BaseColor gray = null;
    public  BaseColor lightergray = null;

    public  BaseColor tableShadeGray1 = null;
    public  BaseColor tableShadeGray2 = null;
    public  BaseColor tableShadeGray3 = null;
    public  BaseColor tableShadeGray4 = null;

    //public  BaseColor blue = null;
    //public  BaseColor  green = null;
    //public  BaseColor  yellowgreen = null;
    //public  BaseColor yellow = null;
    //public  BaseColor  redyellow = null;
    //public  BaseColor red = null;
    //public  BaseColor profileBlue = null;
    //public  BaseColor markerBlack  = null;
    
    
    public BaseColor whiteFontColor;  // #ffffff
    public BaseColor darkFontColor;   // #282828
    public BaseColor lightFontColor;  // #525252

    
    public BaseColor scoreBoxHeaderBgColor;  // #e9e9e9
    public BaseColor scoreBoxShadeBgColor;  // #e9e9e9
    public BaseColor scoreBoxBgColor;  // #ffffff
    public BaseColor scoreBoxBorderColor;  // #525252
    public float scoreBoxBorderWidth = 0.8f;

    public BaseColor lightBoxBorderColor;  // #eaeaea
    
    public BaseColor headerDarkBgColor;    // #3a3a3a
    public BaseColor titlePageBgColor; // #ffffff
    public BaseColor pageBgColor;      // #eaeaea
    public BaseColor hraBaseReportColor;   // #f1592a
    public BaseColor tablePageBgColor; // #ffffff
    //public BaseColor redShadeColor;
    public BaseColor keyBackgroundColor = new BaseColor( 0xe6, 0xe6, 0xe6 ); // e6e6e6    
    
    // public BaseColor hraBlue = new BaseColor( 0x21, 0x96, 0xf3 );
    public BaseColor hraBlue = new BaseColor( 0x00, 0x77, 0xcc );
    
    //public BaseColor barGraphCoreShade1 = new BaseColor( 0x27, 0xb2, 0xe7 ); // f68d2f // new BaseColor( 0xf6, 0x8d, 0x2f ); // f68d2f
    //public BaseColor barGraphCoreShade2 = new BaseColor( 0xab, 0xe7, 0xff );
    
    
    public static CT2Colors getCt2Colors()
    {
        CT2Colors ctc = new CT2Colors();
        
       
        ctc.whiteFontColor = BaseColor.WHITE;  // #ffffff
        ctc.darkFontColor = new BaseColor(0x4d,0x4d,0x4d);   // #4d4d4d
        ctc.lightFontColor = new BaseColor(0x80,0x80,0x80);  // #525252

        ctc.scoreBoxHeaderBgColor = ctc.hraBlue;  // #e9e9e9
        ctc.scoreBoxBgColor = BaseColor.WHITE;  // #ffffff
        ctc.scoreBoxBorderColor = new BaseColor( 0x92, 0x92, 0x92);  // #525252
        ctc.scoreBoxShadeBgColor = new BaseColor( 0xca, 0xe4, 0xee );

        ctc.headerDarkBgColor = ctc.hraBlue; //  new BaseColor(39, 178, 231); // #27b2e7 // new BaseColor( 58,58,58 );    // #3a3a3a
        ctc.titlePageBgColor =  BaseColor.WHITE; // new BaseColor(255, 255, 255); // #ffffff
        ctc.pageBgColor =    BaseColor.WHITE;  // new BaseColor(234, 234, 234);      // #eaeaea
        ctc.hraBaseReportColor = ctc.hraBlue; // new BaseColor(39, 178, 231); // #27b2e7   //   new BaseColor( 241,90,41 );   // #f1592a

        ctc.tablePageBgColor = BaseColor.WHITE; //  new BaseColor(0xf9, 0xf9, 0xf9);
        
        //ctc.markerBlack = new BaseColor(0x00,0x00,0x00);   
        
        ctc.lightBoxBorderColor=new BaseColor( 0xea,0xea,0xea );
        
        ctc.gray = new BaseColor(0x80,0x80,0x80);
        ctc.lightergray = new BaseColor(0xc5,0xc5,0xc5);
        
        ctc.tableShadeGray1 = new BaseColor(0xe6,0xe6,0xe6);
        ctc.tableShadeGray2 = new BaseColor(0xf3,0xf3,0xf3);
        ctc.tableShadeGray3 = new BaseColor(0xea,0xea,0xea);
        ctc.tableShadeGray4 = new BaseColor(0xf7,0xf7,0xf7);
        
        //ctc.blue = new BaseColor(0xb8,0xe1,0xe7);
        //ctc.profileBlue = new BaseColor(0x17,0xb4,0xee);            
        //ctc.green = new BaseColor(0x69,0xa2,0x20);
        //ctc.yellowgreen = new BaseColor(0x8c,0xc6,0x3f);
        //ctc.yellow = new BaseColor(0xfc,0xee,0x21);
        //ctc.redyellow = new BaseColor(0xf1,0x75,0x23);
        //ctc.red = new BaseColor(0xff,0x00,0x00);            
        
        return ctc;
    }
    
    public void clearBorders()
    {
        scoreBoxBorderColor=BaseColor.WHITE;
        scoreBoxBorderWidth = 0f;

        lightBoxBorderColor=BaseColor.WHITE;
        // lighterBoxBorderColor=BaseColor.WHITE;        
    }
    
    
    
    public BaseColor getGray() {
        return gray;
    }

    public void setGray(BaseColor gray) {
        this.gray = gray;
    }

    public BaseColor getLightergray() {
        return lightergray;
    }

    public void setLightergray(BaseColor lightergray) {
        this.lightergray = lightergray;
    }


    public BaseColor getLightBoxBorderColor() {
        return lightBoxBorderColor;
    }

    public BaseColor getWhiteFontColor() {
        return whiteFontColor;
    }

    public void setWhiteFontColor(BaseColor whiteFontColor) {
        this.whiteFontColor = whiteFontColor;
    }

    public BaseColor getDarkFontColor() {
        return darkFontColor;
    }

    public void setDarkFontColor(BaseColor darkFontColor) {
        this.darkFontColor = darkFontColor;
    }

    public BaseColor getLightFontColor() {
        return lightFontColor;
    }

    public void setLightFontColor(BaseColor lightFontColor) {
        this.lightFontColor = lightFontColor;
    }

    public BaseColor getScoreBoxHeaderBgColor() {
        return scoreBoxHeaderBgColor;
    }

    public void setScoreBoxHeaderBgColor(BaseColor scoreBoxHeaderBgColor) {
        this.scoreBoxHeaderBgColor = scoreBoxHeaderBgColor;
    }

    public BaseColor getScoreBoxShadeBgColor() {
        return scoreBoxShadeBgColor;
    }

    public void setScoreBoxShadeBgColor(BaseColor scoreBoxShadeBgColor) {
        this.scoreBoxShadeBgColor = scoreBoxShadeBgColor;
    }

    public BaseColor getScoreBoxBgColor() {
        return scoreBoxBgColor;
    }

    public void setScoreBoxBgColor(BaseColor scoreBoxBgColor) {
        this.scoreBoxBgColor = scoreBoxBgColor;
    }

    public BaseColor getScoreBoxBorderColor() {
        return scoreBoxBorderColor;
    }

    public void setScoreBoxBorderColor(BaseColor scoreBoxBorderColor) {
        this.scoreBoxBorderColor = scoreBoxBorderColor;
    }

    public float getScoreBoxBorderWidth() {
        return scoreBoxBorderWidth;
    }

    public void setScoreBoxBorderWidth(float scoreBoxBorderWidth) {
        this.scoreBoxBorderWidth = scoreBoxBorderWidth;
    }

    public BaseColor getHeaderDarkBgColor() {
        return headerDarkBgColor;
    }

    public void setHeaderDarkBgColor(BaseColor headerDarkBgColor) {
        this.headerDarkBgColor = headerDarkBgColor;
    }

    public BaseColor getTitlePageBgColor() {
        return titlePageBgColor;
    }

    public void setTitlePageBgColor(BaseColor titlePageBgColor) {
        this.titlePageBgColor = titlePageBgColor;
    }

    public BaseColor getPageBgColor() {
        return pageBgColor;
    }

    public void setPageBgColor(BaseColor pageBgColor) {
        this.pageBgColor = pageBgColor;
    }

    public BaseColor getHraBaseReportColor() {
        return hraBaseReportColor;
    }

    public void setHraBaseReportColor(BaseColor hraBaseReportColor) {
        this.hraBaseReportColor = hraBaseReportColor;
    }

    public BaseColor getTablePageBgColor() {
        return tablePageBgColor;
    }

    public void setTablePageBgColor(BaseColor tablePageBgColor) {
        this.tablePageBgColor = tablePageBgColor;
    }

    public BaseColor getKeyBackgroundColor() {
        return keyBackgroundColor;
    }

    public void setKeyBackgroundColor(BaseColor keyBackgroundColor) {
        this.keyBackgroundColor = keyBackgroundColor;
    }

    public BaseColor getHraBlue() {
        return hraBlue;
    }

    public void setHraBlue(BaseColor hraBlue) {
        this.hraBlue = hraBlue;
    }


    
    
}
