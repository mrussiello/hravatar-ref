/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref.results;

import com.tm2ref.entity.ref.RcCheck;
import java.util.Locale;

/**
 *
 * @author miker_000
 */
public interface RcResultEmailFormatter {
    
    String getResultEmailContent( RcCheck rc, Locale locale );
    
}
