/*
 * Created on Dec 30, 2006
 *
 */
package com.tm2ref.user;

import com.tm2ref.service.LogService;
import com.tm2ref.util.MessageFactory;
import java.util.*;
import jakarta.enterprise.context.ApplicationScoped;

import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;

import jakarta.inject.Named;

@Named
@ApplicationScoped
public class CountryCodeLister
{
    private static List<String> countryCodeList = null;
    // private static Map<Locale,Map<String,String>> countryListForLocaleMap = null;

    public static CountryCodeLister getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (CountryCodeLister) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "countryCodeLister" );
    }


    private synchronized void init()
    {
        if( countryCodeList == null )
        {
            countryCodeList = new ArrayList<>();
            try
            {
                UserFacade userFacade = UserFacade.getInstance();
                countryCodeList = userFacade.getCountryCodeList();
            }
            catch( Exception e )
            {
                LogService.logIt(e, "CountryCodeLister.init() " );
                
                countryCodeList.add( "US" );
            }
        }
    }
    
    



    public static String getCountryForCode( Locale locale , String countryCode )
    {
        if( countryCode==null )
            return null;

        if( locale==null )
            locale = Locale.US;

        return MessageFactory.getStringMessage( locale, "cntry." + countryCode.toUpperCase() , null );
    }


    /*
    public List<String> getCountryNameList()
    {
        List<String> out = new ArrayList<>();

        Map<String,String> cMap = getCountryMap();

        for( String nm : cMap.keySet() )
        {
            out.add(nm);
        }

        return out;
    }
    */


    /**
     * @return the countryMap
     */
    public List<SelectItem> getCountrySelectItemList( Locale locale )
    {
        if( locale == null )
            locale = Locale.getDefault();

        init();
        
        List<SelectItem> out = new ArrayList<>();

        for( String cc : countryCodeList )
        {
            out.add( new SelectItem( cc, MessageFactory.getStringMessage(locale, "cntry." + cc, null )) );
        }

        Collections.sort( out, new SelectItemLabelComparator() );
        
        return out;
    }
}
