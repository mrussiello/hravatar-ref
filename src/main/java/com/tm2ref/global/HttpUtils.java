/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.global;

import com.tm2ref.service.LogService;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Mike
 */
public class HttpUtils {

    public static Map<String,Locale> supportedLanguages;




    protected static synchronized void initLocales()
    {
        if( supportedLanguages == null )
        {
            supportedLanguages = new HashMap<>();

            supportedLanguages.put( "ar_JO", new Locale("ar", "JO") );
            supportedLanguages.put( "ar_LB", new Locale("ar", "LB") );
            supportedLanguages.put( "da_DK", new Locale("da", "DK") );
            supportedLanguages.put( "de_DE", new Locale("de", "DE") );
            supportedLanguages.put( "en_AU", new Locale("en", "AU") );
            supportedLanguages.put( "en_CA", new Locale("en", "CA") );
            supportedLanguages.put( "en_GB", new Locale("en", "GB") );
            supportedLanguages.put( "en_IN", new Locale("en", "IN") );
            supportedLanguages.put( "en_US", new Locale("en", "US") );
            supportedLanguages.put( "es_ES", new Locale("es", "ES") );
            supportedLanguages.put( "es_MX", new Locale("es", "MX") );
            supportedLanguages.put( "fr_CA", new Locale("fr", "CA") );
            supportedLanguages.put( "fr_FR", new Locale("fr", "FR") );
            supportedLanguages.put( "it_IT", new Locale("it", "IT") );
            supportedLanguages.put( "he_IL", new Locale("he", "IL") );
            supportedLanguages.put( "in_ID", new Locale("in", "ID") );
            supportedLanguages.put( "ja_JP", new Locale("ja", "JP") );
            supportedLanguages.put( "ko_KR", new Locale("ko", "KR") );
            supportedLanguages.put( "nb_NO", new Locale("nb", "NO") );
            supportedLanguages.put( "nl_NL", new Locale("nl", "NL") );
            supportedLanguages.put( "ru_RU", new Locale("ru", "RU") );
            supportedLanguages.put( "ro_RO", new Locale("ro", "RO") );
            supportedLanguages.put( "pl_PL", new Locale("pl", "PL") );
            supportedLanguages.put( "pt_BR", new Locale("pt", "BR") );
            supportedLanguages.put( "pt_PT", new Locale("pt", "PT") );
            supportedLanguages.put( "sv_SE", new Locale("sv", "SE") );
            supportedLanguages.put( "zh_CN", new Locale("zh", "CN") );

            
            
            supportedLanguages.put( "ar", new Locale("ar", "JO") );
            supportedLanguages.put( "da", new Locale("da", "DK") );
            supportedLanguages.put( "de", new Locale("de", "DE") );
            supportedLanguages.put( "en", new Locale("en", "US") );
            supportedLanguages.put( "es", new Locale("es", "ES") );
            supportedLanguages.put( "fr", new Locale("fr", "FR") );
            supportedLanguages.put( "it", new Locale("it", "IT") );
            supportedLanguages.put( "ja", new Locale("jp", "JP") );
            supportedLanguages.put( "ko", new Locale("ko", "KR") );
            supportedLanguages.put( "nb", new Locale("nb", "NO") );
            supportedLanguages.put( "nl", new Locale("nl", "NL") );
            supportedLanguages.put( "pl", new Locale("pl", "PL") );
            supportedLanguages.put( "pt", new Locale("pt", "PT") );
            supportedLanguages.put( "ro", new Locale("ro", "RO") );
            supportedLanguages.put( "ru", new Locale("ru", "RU") );
            supportedLanguages.put( "sv", new Locale("sv", "SE") );
            supportedLanguages.put( "zh", new Locale("zh", "CN") );
            
            
            

        }
    }



    public static Locale detectLocale(HttpServletRequest request, boolean supportedOnly) {

        // LogService.logIt( "HttpUtis.detctLocale()" + request.getLocale().toString() );

        if( request==null )
            return Locale.US;
        
        if( !supportedOnly )
        {
            try
            {
                return request.getLocale();
            }
            catch( NullPointerException ee )
            {
                LogService.logIt( "HttpUtils.detectLocale() HttpServletRequest threw NullPointer when getting LocaleStr." );
                return Locale.US;
            }
        }

        if( supportedLanguages == null )
            initLocales();

        Enumeration<Locale> locs = request.getLocales();
        while (locs.hasMoreElements()) {
            Locale l = (Locale) locs.nextElement();
            if ( supportedLanguages.containsValue(l) ) {
                return l;
            }
        }

        locs = request.getLocales();

        while (locs.hasMoreElements()) {
            Locale l = (Locale) locs.nextElement();
            if ( supportedLanguages.containsKey(l.getLanguage()) ) {
                return l;
            }
        }

        return Locale.US;
    }





}
