/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.util;

import com.itextpdf.text.BaseColor;
import com.tm2ref.service.LogService;
import java.awt.Color;

/**
 *
 * @author miker
 */
public class ColorUtils {
        
        
    public static Color parseRGB(String rgbString)
    {
        if (rgbString.startsWith("#") && rgbString.length() == 7) {
            try {
                int red = Integer.parseInt(rgbString.substring(1, 3), 16);
                int green = Integer.parseInt(rgbString.substring(3, 5), 16);
                int blue = Integer.parseInt(rgbString.substring(5, 7), 16);

                return new Color(red, green, blue);
            } 
            catch (NumberFormatException e) 
            {
                LogService.logIt("ColorUtils.parseRGB() " + e.toString()  + ", " + rgbString);
                return null;
            }
        } 
        else 
        {
            LogService.logIt( "ColorUtils.parseRGB() Invalid RGB string format: " + rgbString);
            return null;
        }
    }    
    
}
