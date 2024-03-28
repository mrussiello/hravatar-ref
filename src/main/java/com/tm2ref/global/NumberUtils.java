package com.tm2ref.global;

import com.tm2ref.service.LogService;
import java.text.DecimalFormat;



/**
 * This class provides a variety of useful number-oriented utilities
 */
public class NumberUtils
{
    public static int intFmString( String inStr ) throws STException
    {
    	return intFmString( inStr, true );
    }

    public static int intFmString( String inStr, boolean error ) throws STException
    {
    	try
    	{
    		double d = Double.parseDouble( inStr );

    		return (int) Math.floor( d );
    	}

    	catch( Exception e )
    	{
    		LogService.logIt(e, "NumberUtils.intFmString() " + inStr );

    		if( error )
    			throw new STException( e );
    	}

    	return 0;
    }


    public static boolean isOdd( int num )
    {
    	return num % 2 != 0;
    }

    /**
     * Returns a number rounded to the requested number of decimal places
     */
    public static double roundIt( double theNumber , int decimalPlaces )
    {
        int bump = 1;

        for( int i=0 ; i<decimalPlaces ; i++ )
            bump *= 10;

        theNumber *= bump;

        theNumber = Math.floor( theNumber );

        return theNumber/bump;

    }

    /**
     * Always returns a string matching this pattern: ###,##0.00 where
     * the # signs are optional (will not show up if 0, and the 0 symbols are numbers that always
     * show up.
     *
     * This is set to avoid any effects from rounding inside the decimal format class.
     *
     */
    public static String getTwoDecimalFormattedAmount( double theNumber )
    {
        DecimalFormat decimalFormat = new DecimalFormat( "###,##0.000" );

        String temp = decimalFormat.format( theNumber );

        return temp.substring( 0 , temp.length() - 1 );
    }



    /**
     * Returns a number rounded to a computed number of decimal places
     */
    public static double roundItNatural( double theNumber )
    {
        int decimalPlaces = 0;

        if( theNumber < 10.0 )
            decimalPlaces = 1;

        if( theNumber < 1.0 )
            decimalPlaces = 2;

        int bump = 1;

        for( int i=0 ; i<decimalPlaces ; i++ )
            bump *= 10;

        theNumber *= bump;

        theNumber = Math.floor( theNumber );

        return theNumber/bump;

    }


    public static String getNaturalRoundedString( double d )
    {
        return Double.toString( roundItNatural( d ) );
    }

    /**
     * returns the desired number of decimal places for a specific number
     */
    public static int getNaturalDecimalPlaces( double theNumber )
    {
        if( theNumber < 0.01 )
            return 4;

        if( theNumber < 0.1 )
            return 3;

        if( theNumber < 1.0 )
            return 2;

        if( theNumber < 10.0 )
            return 1;

        return 0;
    }

}