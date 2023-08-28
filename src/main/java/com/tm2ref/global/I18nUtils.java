package com.tm2ref.global;


import com.tm2ref.service.LogService;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;



/**
 * Various utilities for localizing information
 */
public class I18nUtils
{

    private static final Set<String> RTL;


    static {
        Set<String> lang = new HashSet<>();
        lang.add("ar");
        lang.add("dv");
        lang.add("fa");
        lang.add("ha");
        lang.add("he");
        lang.add("ji");
        lang.add("ps");
        lang.add("ur");
        lang.add("yi");
        RTL = Collections.unmodifiableSet(lang);
    }

    public static boolean isTextRTL( Locale locale )
    {
      if( locale == null )
          return false;

      return RTL.contains(locale.getLanguage());
    }


    /**
     * get a formatted number string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (double)
     * @param _maxFractionDigits The number of digits to the right of decimal point.
     *
     */
    public static String getFormattedNumber( Locale _locale,
                                             double _number,
                                             int _maxFractionDigits )
    {
        if( _locale == null )
            _locale = Locale.US;

        NumberFormat numberFormatter = NumberFormat.getNumberInstance(  _locale  );

        numberFormatter.setMaximumFractionDigits( _maxFractionDigits );

        return numberFormatter.format( _number );

    }



    /**
     * get a formatted integer string
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (int)
     *
     */
     public static String getFormattedInteger( Locale _locale,
                                               long _number )
    {
         if( _locale == null )
             _locale = Locale.US;

        NumberFormat numberFormatter = NumberFormat.getIntegerInstance(  _locale  );

        return numberFormatter.format( _number );

    }


    /**
     * get a formatted currency string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _amount The number to be formatted (double)
     * @param _maxFractionDigits The number of digits to the right of decimal point.
     *
     */
     public static String getFormattedCurrency( Locale _locale,
                                               double _amount,
                                               int _maxFractionDigits )
    {

         if( _locale == null )
             _locale = Locale.US;

        NumberFormat numberFormatter = NumberFormat.getCurrencyInstance(  _locale  );

        numberFormatter.setMaximumFractionDigits( _maxFractionDigits );

        return numberFormatter.format( _amount );

    }




    /**
     * get a formatted percentage string with set digits.
     *
     * @param _locale The current java.util.Locale
     * @param _number The number to be formatted (double)
     * @param _maxFractionDigits The number of digits to the right of decimal point.
     *
     */
    public static String getFormattedPercent(  Locale _locale,
                                               double _number,
                                               int _maxFractionDigits )
    {



        NumberFormat numberFormatter = NumberFormat.getPercentInstance(  _locale  );

        numberFormatter.setMaximumFractionDigits( _maxFractionDigits );

        return numberFormatter.format( _number );

    }



    /**
     * get a formatted date string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param _style desired style.
     *
     *
    public static String getFormattedDate(  Locale _locale,
                                            Date _date,
                                            int _style )
    {

        if( _locale == null )
            _locale = Locale.US;

        DateFormat dateFormatter = DateFormat.getDateInstance(  _style, _locale  );

        return dateFormatter.format( _date );

    }
    */


    /**
     * get a formatted date string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param _style desired style.
     *
     *
    public static String getFormattedDate(  Locale _locale,
                                            Date _date,
                                            String _pattern )
    {

        if( _locale == null )
            _locale = Locale.US;

        SimpleDateFormat dateFormatter = new SimpleDateFormat( _pattern , _locale );

        return dateFormatter.format( _date );

    }
    */






    /**
     * get a formatted date string in the standard style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     *
     *
    public static String getFormattedDate(  Locale _locale,
                                            Date _date )
    {

        return getFormattedDate(  _locale, _date, DateFormat.LONG  );

    }
    */


    /**
     * get a formatted date and time string in the desired style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param timezone TODO
     * @param _style desired style.
     *
     */
    public static String getFormattedDateTime(  Locale _locale,
                                                Date _date,
                                                int _dateStyle,
                                                int _timeStyle,
                                                TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        DateFormat dateFormatter = DateFormat.getDateTimeInstance(  _dateStyle, _timeStyle, _locale  );

        if( timezone != null )
            dateFormatter.setTimeZone( timezone );

        return dateFormatter.format( _date );

    }



    /**
     * get a formatted date time string in the standard style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param timezone TODO
     *
     */
    public static String getFormattedDateTime(  Locale _locale,
                                                Date _date,
                                                TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        if( _date == null )
            _date = new Date();

        if( timezone == null )
            timezone = TimeZone.getDefault();

        return getFormattedDateTime(  _locale, _date, DateFormat.LONG , DateFormat.LONG, timezone );
    }

    public static String getFormattedDateTimeShort(  Locale _locale,
                                                Date _date,
                                                TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        if( _date == null )
            _date = new Date();

        if( timezone == null )
            timezone = TimeZone.getDefault();

        return getFormattedDateTime(  _locale, _date, DateFormat.SHORT , DateFormat.SHORT, timezone );
    }
    
    

    /**
     * get a formatted date time string in the standard style for locale.
     *
     * @param _locale The current java.util.Locale
     * @param _date java.util.Date to be formatted
     * @param timezone TODO
     *
     */
    public static String getFormattedDate(  Locale _locale,
                                            Date _date,
                                            TimeZone timezone )
    {
        if( _locale == null )
            _locale = Locale.US;

        if( _date == null )
            _date = new Date();

        DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM, _locale  );

        if( timezone != null )
            dateFormatter.setTimeZone( timezone );

        return dateFormatter.format( _date );
    }
    
    

    public static Locale getLocaleFromCompositeStr( String inStr )
    {
        try
        {
            if( inStr == null || inStr.isEmpty() || inStr.equals( "ttln" )|| inStr.equals( "brln" ) )
                return Locale.US;

            String[] pieces = inStr.split( "_" );

            if( pieces.length == 0 )
                return Locale.US;

            if( pieces.length == 1 )
                return new Locale( pieces[0] );

            else
                return new Locale( pieces[0] , pieces[1] );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "getLocaleFromCompositeStr( " + inStr + " ) " );

            return Locale.US;
        }
    }
}