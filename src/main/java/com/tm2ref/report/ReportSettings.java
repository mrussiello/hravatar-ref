/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.report;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.pdf.BaseFont;


/**
 *
 * @author Mike
 */
public interface ReportSettings {

    BaseColor getBarGraphCoreShade1();

    BaseColor getBarGraphCoreShade2();

    BaseFont getBaseFont();

    BaseFont getBaseFontCalibri();

    BaseFont getBaseFontCalibriBold();

    BaseFont getBaseFontCalibriBoldItalic();

    BaseFont getBaseFontCalibriItalic();

    BaseColor getDarkFontColor();

    int getFONTSZ();

    Font getFont();

    Font getFontBold();

    Font getFontBoldItalic();

    Font getFontItalic();

    Font getFontLarge();

    Font getFontLargeBold();

    Font getFontLargeBoldItalic();

    Font getFontLargeItalic();

    Font getFontLargeLight();

    Font getFontLargeLightBold();

    Font getFontLargeWhite();

    Font getFontLight();

    Font getFontLightBold();

    Font getFontLightItalic();

    Font getFontSectionTitle();

    Font getFontSmall();

    Font getFontSmallBold();

    Font getFontSmallBoldItalic();

    Font getFontSmallItalic();

    Font getFontSmallLight();

    Font getFontSmallLightBold();

    Font getFontSmallLightItalic();

    Font getFontSmallWhite();

    Font getFontWhite();

    Font getFontXLarge();

    Font getFontXLargeBold();

    Font getFontXLargeBoldItalic();

    Font getFontXLargeItalic();

    Font getFontXLargeLight();

    Font getFontXLargeLightBold();

    Font getFontXLargeWhite();

    Font getFontXSmall();

    Font getFontXSmallBold();

    Font getFontXSmallBoldItalic();

    Font getFontXSmallItalic();

    Font getFontXSmallLight();

    Font getFontXSmallWhite();

    Font getFontXXLarge();

    Font getFontXXLargeBold();

    Font getFontXXLargeBoldItalic();

    Font getFontXXLargeItalic();

    Font getFontXXLargeLight();

    Font getFontXXLargeWhite();

    Font getFontXXSmall();

    Font getFontXXSmallBold();

    Font getFontXXSmallBoldItalic();

    Font getFontXXSmallItalic();

    Font getFontXXSmallLight();

    Font getFontXXSmallWhite();

    BaseFont getHeaderBaseFont();

    BaseColor getHeaderDarkBgColor();

    Font getHeaderFontLarge();

    Font getHeaderFontLargeWhite();

    Font getHeaderFontXLarge();

    Font getHeaderFontXLargeWhite();

    Font getHeaderFontXXLarge();

    Font getHeaderFontXXLargeWhite();

    Image getHraLogoBlackText();

    Image getHraLogoBlackTextSmall();

    Image getHraLogoWhiteText();

    Image getHraLogoWhiteTextSmall();

    BaseColor getHraBaseReportColor();

    int getLFONTSZ();

    BaseColor getLightFontColor();

    BaseColor getPageBgColor();

    int getSFONTSZ();

    BaseColor getScoreBoxBgColor();

    BaseColor getScoreBoxBorderColor();

    BaseColor getScoreBoxHeaderBgColor();

    BaseColor getTablePageBgColor();

    BaseColor getTitlePageBgColor();

    BaseColor getWhiteFontColor();

    int getXLFONTSZ();

    int getXSFONTSZ();

    int getXXLFONTSZ();

    int getXXSFONTSZ();

    void initSettings(ReportData reportData) throws Exception;

    void setBarGraphCoreShade1(BaseColor barGraphOrange1);

    void setBarGraphCoreShade2(BaseColor barGraphOrange2);

    void setBaseFont(BaseFont baseFont);

    void setBaseFontCalibri(BaseFont baseFontCalibri);

    void setBaseFontCalibriBold(BaseFont baseFontCalibriBold);

    void setBaseFontCalibriBoldItalic(BaseFont baseFontCalibriBoldItalic);

    void setBaseFontCalibriItalic(BaseFont baseFontCalibriItalic);

    void setDarkFontColor(BaseColor darkFontColor);

    void setFONTSZ(int FONTSZ);

    void setFont(Font font);

    void setFontBold(Font fontBold);

    void setFontBoldItalic(Font fontBoldItalic);

    void setFontItalic(Font fontItalic);

    void setFontLarge(Font fontLarge);

    void setFontLargeBold(Font fontLargeBold);

    void setFontLargeBoldItalic(Font fontLargeBoldItalic);

    void setFontLargeItalic(Font fontLargeItalic);

    void setFontLargeLight(Font fontLargeLight);

    void setFontLargeLightBold(Font fontLargeLightBold);

    void setFontLargeWhite(Font fontLargeWhite);

    void setFontLight(Font fontLight);

    void setFontLightBold(Font fontLightBold);

    void setFontLightItalic(Font fontLightItalic);

    void setFontSectionTitle(Font fontSectionTitle);

    void setFontSmall(Font fontSmall);

    void setFontSmallBold(Font fontSmallBold);

    void setFontSmallBoldItalic(Font fontSmallBoldItalic);

    void setFontSmallItalic(Font fontSmallItalic);

    void setFontSmallLight(Font fontSmallLight);

    void setFontSmallLightBold(Font fontSmallLightBold);

    void setFontSmallLightItalic(Font fontSmallLightItalic);

    void setFontSmallWhite(Font fontSmallWhite);

    void setFontWhite(Font fontWhite);

    void setFontXLarge(Font fontXLarge);

    void setFontXLargeBold(Font fontXLargeBold);

    void setFontXLargeBoldItalic(Font fontXLargeBoldItalic);

    void setFontXLargeItalic(Font fontXLargeItalic);

    void setFontXLargeLight(Font fontXLargeLight);

    void setFontXLargeLightBold(Font fontXLargeLightBold);

    void setFontXLargeWhite(Font fontXLargeWhite);

    void setFontXSmall(Font fontXSmall);

    void setFontXSmallBold(Font fontXSmallBold);

    void setFontXSmallBoldItalic(Font fontXSmallBoldItalic);

    void setFontXSmallItalic(Font fontXSmallItalic);

    void setFontXSmallLight(Font fontXSmallLight);

    void setFontXSmallWhite(Font fontXSmallWhite);

    void setFontXXLarge(Font fontXXLarge);

    void setFontXXLargeBold(Font fontXXLargeBold);

    void setFontXXLargeBoldItalic(Font fontXXLargeBoldItalic);

    void setFontXXLargeItalic(Font fontXXLargeItalic);

    void setFontXXLargeLight(Font fontXXLargeLight);

    void setFontXXLargeWhite(Font fontXXLargeWhite);

    void setFontXXSmall(Font fontXXSmall);

    void setFontXXSmallBold(Font fontXXSmallBold);

    void setFontXXSmallBoldItalic(Font fontXXSmallBoldItalic);

    void setFontXXSmallItalic(Font fontXXSmallItalic);

    void setFontXXSmallLight(Font fontXXSmallLight);

    void setFontXXSmallWhite(Font fontXXSmallWhite);

    void setHeaderBaseFont(BaseFont headerBaseFont);

    void setHeaderDarkBgColor(BaseColor headerBgColor);

    void setHeaderFontLarge(Font headerFontLarge);

    void setHeaderFontLargeWhite(Font headerFontLargeWhite);

    void setHeaderFontXLarge(Font headerFontXLarge);

    void setHeaderFontXLargeWhite(Font headerFontXLargeWhite);

    void setHeaderFontXXLarge(Font headerFontXXLarge);

    void setHeaderFontXXLargeWhite(Font headerFontXXLargeWhite);

    void setHraLogoBlackText(Image hraLogoBlackText);

    void setHraLogoBlackTextSmall(Image hraLogoBlackTextSmall);

    void setHraLogoWhiteText(Image hraLogoWhiteText);

    void setHraLogoWhiteTextSmall(Image hraLogoWhiteTextSmall);

    void setHraBaseReportColor(BaseColor hraOrangeColor);

    void setLFONTSZ(int LFONTSZ);

    void setLightFontColor(BaseColor lightFontColor);

    void setPageBgColor(BaseColor pageBgColor);


    void setSFONTSZ(int SFONTSZ);

    void setScoreBoxBgColor(BaseColor scoreBoxBgColor);

    void setScoreBoxBorderColor(BaseColor scoreBoxBorderColor);

    void setScoreBoxHeaderBgColor(BaseColor scoreBoxHeaderBgColor);

    void setTablePageBgColor(BaseColor tablePageBgColor);

    void setTitlePageBgColor(BaseColor titlePageBgColor);

    void setWhiteFontColor(BaseColor whiteFontColor);

    void setXLFONTSZ(int XLFONTSZ);

    void setXSFONTSZ(int XSFONTSZ);

    void setXXLFONTSZ(int XXLFONTSZ);

    void setXXSFONTSZ(int XXSFONTSZ);


}
