/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.tm2ref.previousresult;

import com.tm2ref.entity.user.User;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author miker
 */
public interface PreviousResult {
    
    long getPreviousResultId();
    
    String getPreviousResultTypeName();
    
    String getPreviousResultName();

    String getPreviousResultViewUrl();
    
    Date getPreviousResultDate();
    
    float getPreviousResultOverallScore();
    
    void setLocale( Locale locale );

    void setUser( User user );
    
}
