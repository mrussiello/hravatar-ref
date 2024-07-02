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

            supportedLanguages.put( "ar_JO", Locale.of("ar", "JO") );
            supportedLanguages.put( "ar_LB", Locale.of("ar", "LB") );
            supportedLanguages.put( "da_DK", Locale.of("da", "DK") );
            supportedLanguages.put( "de_DE", Locale.of("de", "DE") );
            supportedLanguages.put( "en_AU", Locale.of("en", "AU") );
            supportedLanguages.put( "en_CA", Locale.of("en", "CA") );
            supportedLanguages.put( "en_GB", Locale.of("en", "GB") );
            supportedLanguages.put( "en_IN", Locale.of("en", "IN") );
            supportedLanguages.put( "en_US", Locale.of("en", "US") );
            supportedLanguages.put( "es_ES", Locale.of("es", "ES") );
            supportedLanguages.put( "es_MX", Locale.of("es", "MX") );
            supportedLanguages.put( "fr_CA", Locale.of("fr", "CA") );
            supportedLanguages.put( "fr_FR", Locale.of("fr", "FR") );
            supportedLanguages.put( "it_IT", Locale.of("it", "IT") );
            supportedLanguages.put( "he_IL", Locale.of("he", "IL") );
            supportedLanguages.put( "in_ID", Locale.of("in", "ID") );
            supportedLanguages.put( "ja_JP", Locale.of("ja", "JP") );
            supportedLanguages.put( "ko_KR", Locale.of("ko", "KR") );
            supportedLanguages.put( "nb_NO", Locale.of("nb", "NO") );
            supportedLanguages.put( "nl_NL", Locale.of("nl", "NL") );
            supportedLanguages.put( "ru_RU", Locale.of("ru", "RU") );
            supportedLanguages.put( "ro_RO", Locale.of("ro", "RO") );
            supportedLanguages.put( "pl_PL", Locale.of("pl", "PL") );
            supportedLanguages.put( "pt_BR", Locale.of("pt", "BR") );
            supportedLanguages.put( "pt_PT", Locale.of("pt", "PT") );
            supportedLanguages.put( "sv_SE", Locale.of("sv", "SE") );
            supportedLanguages.put( "zh_CN", Locale.of("zh", "CN") );

            
            
            supportedLanguages.put( "ar", Locale.of("ar", "JO") );
            supportedLanguages.put( "da", Locale.of("da", "DK") );
            supportedLanguages.put( "de", Locale.of("de", "DE") );
            supportedLanguages.put( "en", Locale.of("en", "US") );
            supportedLanguages.put( "es", Locale.of("es", "ES") );
            supportedLanguages.put( "fr", Locale.of("fr", "FR") );
            supportedLanguages.put( "it", Locale.of("it", "IT") );
            supportedLanguages.put( "ja", Locale.of("jp", "JP") );
            supportedLanguages.put( "ko", Locale.of("ko", "KR") );
            supportedLanguages.put( "nb", Locale.of("nb", "NO") );
            supportedLanguages.put( "nl", Locale.of("nl", "NL") );
            supportedLanguages.put( "pl", Locale.of("pl", "PL") );
            supportedLanguages.put( "pt", Locale.of("pt", "PT") );
            supportedLanguages.put( "ro", Locale.of("ro", "RO") );
            supportedLanguages.put( "ru", Locale.of("ru", "RU") );
            supportedLanguages.put( "sv", Locale.of("sv", "SE") );
            supportedLanguages.put( "zh", Locale.of("zh", "CN") );
            
            
            

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
