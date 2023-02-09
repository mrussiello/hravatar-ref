package com.tm2ref.global;

import com.tm2ref.service.LogService;
import java.text.DecimalFormat;
import java.util.Random;



/**
 * This class provides a variety of useful number-oriented utilities
 */
public class NumberUtils
{

    /**
     * Random number generator used by various
     */
    private static Random random = null;


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



    /**
     * Returns a double between 0 (inclusive) - 1.0 (exclusive) on a uniform distribution.
     */
    public static double getUniformRandom()
    {
        if( random == null )
            random = new Random( new java.util.Date().getTime() );

        return random.nextFloat();
    }


    /**
     * Returns the next pseudorandom, Gaussian ( "normally") distributed double value
     * with mean 0.0 and standard deviation 1.0 from this random number generator's sequence.
     */
    public static double getNormalRandom()
    {
        if( random == null )
            random = new Random( new java.util.Date().getTime() );

        return random.nextGaussian();
    }


    /**
     * Returns the lowest digit of the sum of all the digits in the provided number.
     */
    public static int computeCheckSumDigit( int theNumber )
    {
        String tempStr = new Integer( theNumber ).toString();

        int sum = 0;

        String ch = null;

        for( int i=0 ; i<tempStr.length() ; i++ )
        {
            ch = tempStr.substring( i , i + 1 );

            sum += new Integer( ch ).intValue();
        }

        // get first digit of string as a sum
        tempStr = new Integer( sum ).toString().substring( 0 , 1 );

        // return first digit of string as an integer
        return new Integer( tempStr ).intValue();
    }

}