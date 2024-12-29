package com.tm2ref.util;

import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Pattern;




/**
 * This class is a set of static utility methods for working with String objects
 */
public class StringUtils
{

    /**
     * A table of hex digits
     *
     */
    // private static final char[] hexDigit = { '0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F' };


    private static final char[] alphaDigits = { '2','3','4','5','6','7','8','9','A','B','C','D','E','F','G','H','I','J','K','L','M','N','P','Q','R','S','T','U','V','W','X','Y','Z' };


    public static String removeWhitespaceAndControlCharsPlusLowercase(String str )
    {
        if( str==null )
            return str;        
        return removeNonPrintable(str).replaceAll("[\\r\\n\\t\\s]", "").toLowerCase();        
    }
    
    public static String removeNonAscii(String str){
        if( str==null )
            return str;        
        return str.replaceAll("[^\\x00-\\x7F]", "");
    }

    public static String removeNonPrintable(String str){ // All Control Char
        if( str==null )
            return str;        
        return str.replaceAll("[\\p{C}]", "");
    }

    public static String removeAllControlChars(String str)
    {
        if( str==null )
            return str;        
        return removeNonPrintable(str).replaceAll("[\\r\\n\\t]", "").trim();
    }    
    
    public static String capitalizeFirstChar( String inStr )
    {
        if( inStr==null || inStr.isBlank() )
            return inStr;
        
        return Pattern.compile("^.").matcher(inStr).replaceFirst(m -> m.group().toUpperCase());
    }
    
    public static NVPair getNVPairFromList( String name, String inStr, String delim )
    {
        if( name==null || name.isEmpty() )
            return null;

        List<NVPair> out = parseNVPairsList( inStr, delim );
        
        for( NVPair p : out )
        {
            if( p.getName()!=null && p.getName().equals(name ) )
                return p;
        }
        
        return null;
    }

    
    public static String padIntegerToLength( int theInt, int theLength )
    {
    	String s = Integer.toString(theInt);
        StringBuilder sb = new StringBuilder();
        
        while( sb.length()<theLength-s.length() )
            sb.append("0");
        sb.append( s );
        return sb.toString();
    }
        
    
    public static List<NVPair> parseNVPairsList( String inStr, String delim )
    {
        List<NVPair> out = new ArrayList<>();

        if( inStr==null || inStr.isEmpty() )
            return out;

        StringTokenizer st = new StringTokenizer( inStr, delim );

        String rule;
        String value;

        // LogService.logIt( "StringUtils.parseNVPairsList()  " + inStr );
        
        while( st.hasMoreTokens() )
        {
            rule = st.nextToken();

            if( !st.hasMoreTokens() )
                break;

            value = st.nextToken();
            
            // LogService.logIt( "StringUtils.parseNVPairsList() rule=" + rule + ", value=" + value );

            if( rule != null && !rule.isEmpty() && value!=null && !value.isEmpty() )
                out.add( new NVPair( rule,value ) );
        }

        
        return out;
    }
    
    public static String getDaysHrsMinsSecsStr( long msec, Locale locale )
    {
        if( locale==null )
            locale = Locale.US;
        if( msec<1000 )
            return "";

        int secs = (int) Math.floor( ((float)msec)/(1000) );
        
        int days = secs>=24*60*60 ? secs/(24*60*60) : 0;
        secs -= days*24*60*60;

        int hrs = secs>=60*60 ? secs/(60*60) : 0;
        secs -= hrs*60*60;

        int mins = secs>60 ? secs/60 : 0;
        secs -= mins*60;
        
        if( days>0 )
            return MessageFactory.getStringMessage(locale, "g.TmStrDaysHrsMinsSecs", new String[] {Integer.toString(days), Integer.toString(hrs), Integer.toString(mins), Integer.toString(secs)} );
        if( hrs>0 )
            return MessageFactory.getStringMessage(locale, "g.TmStrHrsMinsSecs", new String[] {Integer.toString(hrs), Integer.toString(mins), Integer.toString(secs)} );
        if( mins>0 )
            return MessageFactory.getStringMessage(locale, "g.TmStrMinsSecs", new String[] {Integer.toString(mins), Integer.toString(secs)} );        
        return MessageFactory.getStringMessage(locale, "g.TmStrSecs", new String[] {Integer.toString(secs)} );        
    }
    
    
    public static String getDaysHrsMinsStr( long msec, Locale locale )
    {
        if( locale==null )
            locale = Locale.US;
        if( msec<1000 )
            return "";
        
        int mins = (int) Math.round( ((float)msec)/(1000*60f) );
        int days = mins>=24*60 ? mins/(24*60) : 0;
        mins -= days*24*60;
        int hrs = mins>=60 ? mins/60 : 0;
        mins -= hrs*60;
        
        if( days>0 )
            return MessageFactory.getStringMessage(locale, "g.TmStrDaysHrsMins", new String[] {Integer.toString(days), Integer.toString(hrs), Integer.toString(mins)} );
        if( hrs>0 )
            return MessageFactory.getStringMessage(locale, "g.TmStrHrsMins", new String[] {Integer.toString(hrs), Integer.toString(mins)} );
        
        return MessageFactory.getStringMessage(locale, "g.TmStrMins", new String[] {Integer.toString(mins)} );        
    }    
    
    
    /**
     * returns string with bracketed artifact removed.
     * 
     * @param inStr
     * @param name
     * @return 
     */
    public static String removeBracketedArtifactFromString( String inStr, String name )
    {
        if( name == null || name.isBlank() || inStr == null || inStr.isEmpty() )
            return inStr;

        name = name.trim();

        if( name.startsWith("[" ) )
            name = name.substring(1, name.length() );
        if( name.endsWith( "]") )
            name = name.substring(0,name.length()-1);

        int idx = inStr.indexOf( "[" + name + "]" );

        // not found.
        if( idx <0 )
            return inStr;
        
        // get info that comes before
        String out = idx>0 ? inStr.substring( 0, idx ) : "";

        // next info.
        int idx2 = inStr.indexOf(  "[" , idx+2 + name.length() );

        // no following tag
        if( idx2 < 0 )
            return out;
        
        // include following info.
        out += inStr.substring( idx2, inStr.length() );        
        return out;
    }

    public static String getBracketedArtifactFromString( String inStr, String name )
    {
        String t = inStr;

        if( name == null || t == null || t.isEmpty() )
            return null;

        name = name.trim();

        if( name.startsWith("[" ) )
            name = name.substring(1, name.length() );

        if( name.endsWith( "]") )
            name = name.substring(0,name.length()-1);

        if(name.isEmpty() )
            return null;

        int idx = t.indexOf( "[" + name + "]" );

        if( idx <0 )
            return null;

        int idx2 = t.indexOf(  "[" , idx+2 + name.length() );

        if( idx2 < 0 )
            idx2 = t.length();

        return t.substring( idx + 2 + name.length() , idx2 ).trim();
    }


    
    

   public static boolean isValidNameMatch( String n1, String ne1, String n2, String ne2 )
    {
        if( n1==null )
            n1="";
        else
            n1=n1.trim();
        
        if( n2==null)
            n2="";
        else
            n2=n2.trim();

        if( ne1==null )
            ne1="";
        else
            ne1=ne1.trim();

        if( ne2==null )
            ne2="";
        else
            ne2=ne2.trim();

        if( !n1.isEmpty()  )
        {
            if( n1.equalsIgnoreCase(n2) || n1.equalsIgnoreCase(ne2) )
                return true;
        }

        if( !ne1.isEmpty()  )
        {
            if( ne1.equalsIgnoreCase(n2) || ne1.equalsIgnoreCase(ne2) )
                return true;

        }

        return false;
    }
    
    
    public static List<String> getKeywordsFmStr( String inStr , int minLen )
    {
        if( inStr == null || inStr.length() == 0 )
            return new ArrayList<>();

        List<String> words = StringUtils.getWords( inStr );

        ListIterator<String> li = words.listIterator();

        while( li.hasNext() )
        {
            if( li.next().length() < minLen )
               li.remove();
        }

        return words;
    }


    public static String conditionForCSV( String inStr )
    {
        inStr = replaceStr( inStr , "\n" , " " );

        inStr = replaceStr( inStr , "\r" , "" );

        return inStr;
    }


    public static String truncateStringWithTrailer( String inStr , int maxLength, boolean lastWhitespace )
    {
        if( inStr == null )
            return "";

        if( inStr.length() <= maxLength )
            return inStr;

        if( maxLength < 4 )
            return inStr.substring( 0 , maxLength -1 );

        if( lastWhitespace )
            return truncateString( inStr , maxLength ) + "...";

        else
            return inStr.substring( 0 , maxLength -1 ) + "...";
    }




    public static String correctAsciiQuotes( String s )
    {
        String newStr = s;

        if ( newStr != null )
        {
            newStr = s.replace('`', '\'' );

            newStr = newStr.replace('\u2018', '\'');

            newStr = newStr.replace('\u2019', '\'');

            newStr = newStr.replace('\u201A', '\'');

            newStr = newStr.replace('\u201B', '\'');

            newStr = newStr.replace('\u201C', '\"');

            newStr = newStr.replace('\u201D', '\"');

            newStr = newStr.replace('\u201E', '\"');
        }

        return newStr;
    }






    /**
     * Returns a truncated String that is truncated at the latest whitespace prior to index.
     */
    public static String truncateString( String inStr , int index )
    {
    	if( inStr == null || inStr.length() < index )
    		return inStr;

        // get most previous whiteSpace index
        int pwi = getPreviousWhitespaceIndex( inStr , index );

        if( pwi > inStr.length() - 1 )
        	pwi = inStr.length() - 1;

        // if found a whitespace character
        if( pwi > 0 )
            return inStr.substring( 0 , pwi );

        // hard truncate
        return inStr.substring( 0 , index );
    }

    /**
     * Returns a truncated String that is truncated at the latest whitespace prior to index.
     */
    public static String truncateStringFromFront( String inStr , int index )
    {
    	if( inStr == null || inStr.length() < index )
    		return inStr;


        // hard truncate
        return inStr.substring( inStr.length()-index , inStr.length() );
    }



    /**
     * Returns the index of the next occurance of a whitespace character within the provided string, of
     * of the String length if there is none.
     */
    public static int getNextWhitespaceIndex( String inStr , int index )
    {
        // if no length to string, return 0
        if( inStr.length() == 0 )
            return 0;

        // if already at length, return length
        if( index >= inStr.length() )
            return inStr.length();


        // get first char
        char ch = inStr.charAt( index );

        while( !Character.isWhitespace( ch )  )
        {
            index++;

            if( index == inStr.length() )
                return index;

            // get next ch
            ch = inStr.charAt( index );

        }  // while

        return index;
    }



    /**
     * Returns the index of the most recent previous occurance of a whitespace character within the provided string, or 0
     * if there is none.
     */
    public static int getPreviousWhitespaceIndex( String inStr , int index )
    {

        // if no length to string, return 0
        if( inStr == null || inStr.length() == 0 )
            return 0;

        // if already at length, return length
        if( index >= inStr.length() )
            return inStr.length() - 1;

        // get first char
        char ch = inStr.charAt( index );

        while( !Character.isWhitespace( ch ) && index > 0 )
        {
            index--;

            if( index == 0 )
                return 0;

            // get next ch
            ch = inStr.charAt( index );

        }  // while

        return index;
    }




    public static String addLineBreaksXhtml( String inStr )
    {
        if( inStr == null )
            return "";

        // put at end!
        if( inStr.indexOf( "\n" ) >= 0 )
            inStr = replaceStr( inStr , "\n" , "<br />" );

        else
            inStr = replaceStr( inStr , "\r" , "<br />" );

        return inStr;
        // return replaceStr( inStr , "\n" , "<br />" );
    }

    public static String replaceStandardEntities( String inStr )
    {
        if( inStr == null )
            return "";

        inStr = inStr.replaceAll( "&" , "&amp;" );

        inStr = inStr.replaceAll( "  " , " &#160;" );

        inStr = inStr.replaceAll( "<" , "&lt;" );

        inStr = inStr.replaceAll( ">" , "&gt;" );

        inStr = inStr.replaceAll( "\"" , "&quot;" );

        inStr = inStr.replaceAll( "`" , "'" );

        // put at end!
        if( inStr.indexOf( "\n" ) >= 0 )
            inStr = replaceStr( inStr , "\n" , "<br />" );

        else
            inStr = replaceStr( inStr , "\r" , "<br />" );

        return inStr;
    }


    public static String sanitizeStringForCSSOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = removeAllControlChars(inStr );
        
        // HTML tag, including Script tag - Cross-site scripting
        // matches < and </ plus any letter, any number,
        inStr = inStr.replaceAll( "((%3C)|<)((%2F)|\\/)*[ac-hj-tv-zAC-HJ-TV-Z0-9%]+((%3E)|>)" , "" );

        // IMG src= tag.  - Cross-site scripting
        inStr = inStr.replaceAll( "((%3C)|<)((%69)|[iI]|(%49))((%6D)|[mM]|(%4D))((%67)|[gG]|(%47))[^\n]+((%3E)|>)" , "" );

        return inStr;
    }

    
    public static String sanitizeNameEmail( String s )
    {
        if( s==null || s.isEmpty() )
            return "";
        
        s = removeAllControlChars(s );
        
        
        s = StringUtils.replaceStr(s,  "''", "" );
        
        if( s.startsWith("'") )
            s = s.substring(1,s.length());
        
        if( s.endsWith("'"))
            s = s.substring(0, s.length()-1 );
        
        return s;
    }
    
    
    public static String sanitizeAllHtml( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = removeAllControlChars(inStr );

        // HTML tag, including Script tag - Cross-site scripting
        // matches < and </ plus any letter, any number,
        inStr = inStr.replaceAll( "((%3C)|<)((%2F)|\\/)*[a-zA-Z0-9%]+((%3E)|>)" , "" );

        // IMG src= tag.  - Cross-site scripting
        inStr = inStr.replaceAll( "((%3C)|<)((%69)|[iI]|(%49))((%6D)|[mM]|(%4D))((%67)|[gG]|(%47))[^\n]+((%3E)|>)" , "" );

        return inStr;
    }


    public static String sanitizeStringFull( String in )
    {
        if( in == null || in.length() == 0 )
            return in;

        // inStr = escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );

        // SQL Injection - screen metacharacters
        String inStr = in.replaceAll( "(\\%27)|(\\')|(\\-\\-)|(\\%23)|(#)" , "" );

        // Single quote and or combination
        inStr = inStr.replaceAll( "\\w*((%27)|(\\'))((%6F)|[oO]|(%4F))((%72)|[rR]|(%52))" , "" );

        // Single quote and union combination
        inStr = inStr.replaceAll( "\\w*((%27)|(\\'))[uU][nN][iI][oO][nN]" , "" );

        inStr = sanitizeStringForCSSOnly( inStr );

        // inStr = inStr.replaceAll( "<" , "%3C" );
        // LogService.logIt( "Sanitizing: " + in + ", returning " + inStr );

        return inStr; // inStr.replaceAll( ">" , "%3E" );
    }

    public static String sanitizeForSqlQuery( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = removeAllControlChars(inStr );
        
        inStr = replaceStr( inStr, "\\" , "\\\\" );
        
        // return escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
        return replaceStr( inStr, "\'" , "\'\'" ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
        // return escapeChar( inStr, '\'' , '\\' ); //replaceUnescapedChar( inStr , '\'' ,"\\\'" ); // inStr.replaceAll( "\'" , "\\\'" );
    }


    public static String escapeForHtml( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = replaceStr( inStr ,  "<" , "&lt;" );

        inStr = replaceStr( inStr ,  ">" , "&gt;" );

        return replaceStr( inStr ,  "\"" , "&quot;" );
    }

    public static String urlEncode( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        try
        {
            return java.net.URLEncoder.encode( inStr, "UTF-8" );
        }

        catch( Exception e )
        {
            LogService.logIt( e , "ENCODING ERROR: StringUtils.urlEncode( ) " + inStr );

            return inStr;
        }
    }


    public static String convertHtml2PlainText( String inStr , boolean eliminateBlankLines )
    {
        String outStr = inStr;
        try
        {
            if( inStr == null || inStr.length() == 0 )
                return "";
            
            if( !inStr.contains("<") && !inStr.contains( ">" ) && !inStr.contains( "&" ) )
                return inStr;
            

            // first, convert all br tags to hard returns
            outStr = replaceStr( outStr , "<br>", "\n" );
            outStr = replaceStr( outStr , "<br/>", "\n" );
            outStr = replaceStr( outStr , "<br />", "\n" );
            outStr = replaceStr( outStr , "<p>", "\n" );
            outStr = replaceStr( outStr , "</li>", "\n" );
            outStr = replaceStr( outStr , "</ul>", "\n" );
            outStr = replaceStr( outStr , "</ol>", "\n" );
            outStr = replaceStr( outStr , "</tr>", "\n" );
            outStr = replaceStr( outStr , "</td>", "     " );

            outStr = replaceStr( outStr , "&nbsp;", " " );
            outStr = replaceStr( outStr , "&#160;", " " );
            outStr = replaceStr( outStr , "&quot;", "\"" );

            int first=0;

            int last=0;

            while( true )
            {
                first = outStr.indexOf( "<" );

                last = outStr.indexOf( ">" );

                if( first > -1 && last > -1 )
                {
                    outStr = outStr.substring( 0 , first ) + outStr.substring( last + 1 , outStr.length() );
                }

                else
                    break;
            }

            if( eliminateBlankLines )
            {
                String finalStr = "";

                String[] segments = outStr.split( "\n" );

                for( int i=0; i<segments.length ; i++ )
                {
                    if( segments[i] == null || segments[i].trim().length() == 0 )
                        continue;

                    if( finalStr.length() > 0 )
                        finalStr += "\n";

                    finalStr += segments[i];
                }

                outStr = finalStr;
            }

            return outStr;
        }

        catch( Exception e )
        {
            LogService.logIt( e , "StringUtils.convertHtml2PlainText() inStr=" + inStr + ", outStr=" + outStr );
            return outStr;
        }
    }


    public static String escapeTextForJson( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return "";

        inStr = replaceStr( inStr , "\n", "" );

        inStr = replaceStr( inStr , "\r", "" );

        inStr = replaceStr( inStr , "'", "\\'" );

        inStr = replaceStr( inStr , "\"" , "\\\"" );

        inStr = replaceStr( inStr , "<" , "%3C" );

        inStr = replaceStr( inStr , ">" , "%3E" );

        return inStr;
    }


    public static String escapeTextForJavascript( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return "";

        inStr = replaceStr( inStr , "\n", "\\n" );

        inStr = replaceStr( inStr , "\r", "\\r" );

        inStr = replaceStr( inStr , "'", "\\\'" );

        inStr = replaceStr( inStr , "\"" , "\\\"" );

        inStr = replaceStr( inStr , ";" , "%3B" );

        inStr = replaceStr( inStr , "<" , "%3C" );

        inStr = replaceStr( inStr , ">" , "%3E" );

        return inStr;
    }




    /**
     * replaces outChar with inChar and returns finished String
     */
    public static String replaceChar( String inStr, char outChar , char inChar )
    {
        if( inStr == null || inStr.length() == 0 )
            return "";

        StringBuilder outStr = new StringBuilder();

        for ( int i = 0; i < inStr.length(); i++ )
        {
            if( inStr.charAt( i ) == outChar )
                outStr.append( inChar );

            else
                outStr.append( inStr.charAt( i ) );
        }

        return outStr.toString();
    }



    public static String replaceChar( String inStr, char out, String in )
    {
      if ( ( inStr == null ) || ( inStr.length() == 0 ) )
        return ( "" );

      StringBuilder outStr = new StringBuilder( "" );

      for ( int i = 0; i < inStr.length(); i++ )
      {
        if ( inStr.charAt( i ) == out )
          outStr.append( in );
        else
          outStr.append( inStr.charAt( i ) );
      }

      return ( outStr.toString() );
    }

    public static String escapeChar( String inStr, char charToEscape , char escapeChar )
    {
      if ( ( inStr == null ) || ( inStr.length() == 0 ) )
        return ( "" );

      StringBuilder outStr = new StringBuilder( "" );

      for ( int i = 0; i < inStr.length(); i++ )
      {
        if ( inStr.charAt( i ) == charToEscape )
        {
            if( i == 0 || inStr.charAt( i-1 ) != escapeChar )
                outStr.append( escapeChar );

            outStr.append( charToEscape );
        }
        else
          outStr.append( inStr.charAt( i ) );
      }

      return ( outStr.toString() );
    }


    public static String removeChar( String inStr, char out )
    {
      StringBuilder outStr = new StringBuilder();

      for ( int i = 0;i < inStr.length();i++ )
      {
        if ( inStr.charAt( i ) != out )
          outStr.append( inStr.charAt( i ) );
      }

      return outStr.toString();
    }



    public static String replaceStr( String inStr , String oldPiece , String newPiece  )
    {

        if( inStr == null || inStr.length() == 0 )
            return "";

        if( oldPiece == null || oldPiece.length() == 0 )
            return inStr;

        if( newPiece == null )
            newPiece = "";

        StringBuilder outStr = new StringBuilder();

        int index = inStr.indexOf( oldPiece , 0 );

        if( index < 0 )
            return inStr;

        int lastIndex = 0;

        while( index >= 0 )
        {
            if( index > 0 )
                outStr.append( inStr.substring( lastIndex , index ) );

            outStr.append( newPiece );

            lastIndex =  index + oldPiece.length();

            if( lastIndex >= inStr.length() )
                break;

            index = inStr.indexOf( oldPiece , lastIndex );
        }

        // attach tail
        if( lastIndex < inStr.length() )
            outStr.append( inStr.substring( lastIndex , inStr.length() ) );

        return outStr.toString();
    }


    public static  String replaceStr(   String inStr ,
                                        String findStr ,
                                        String replaceStr ,
                                        boolean ignoreCase) throws Exception
    {
        try
        {
            if( inStr == null || inStr.length() == 0 )
                return inStr;

            if( findStr == null || findStr.length() == 0 )
                return inStr;

            if( replaceStr == null )
                replaceStr = "";

            if( !ignoreCase )
                return inStr.replaceAll( findStr , replaceStr );

            // work on upper case
            findStr = findStr.toUpperCase();

            String tempInStr = inStr.toUpperCase();

            int index = tempInStr.indexOf( findStr );

            int startIndex = 0;

            String outStr = "";

            while( index >= 0 && index < tempInStr.length() )
            {
                outStr += inStr.substring( startIndex , index );

                outStr += replaceStr;

                index += findStr.length();

                startIndex = index;

                if( index < inStr.length() )
                    index = tempInStr.indexOf( findStr , index );
            }

            if( startIndex < inStr.length() )
                outStr += inStr.substring( startIndex , inStr.length() );

            return outStr;
        }

        catch( Exception e )
        {
            logIt( "replaceStr( inStr=" + inStr + ", findStr=" + findStr + ", replaceStr=" + replaceStr + ", replaceStr=" + replaceStr + " ) " + e.toString() );

            throw new STException( e );
        }
    }

    public static boolean isCurlyBracketed( String inStr )
    {
        if( inStr == null || inStr.trim().isEmpty() )
            return false;

        inStr = inStr.trim();

        return inStr.indexOf('{')==0 && inStr.lastIndexOf('}')==inStr.length()-1;

    }
    
    

    /**
     * Returns the number of occurences of 'key' inside inStr
     *
     * @param inStr
     * @param key
     * @param caseSensitive
     * @return
     * @throws Exception
     */
    public static int getKeyCount( String inStr ,
                                   String key ,
                                   boolean caseSensitive ) throws Exception
    {
        if( inStr == null || inStr.length() == 0 || key == null || key.length() == 0 )
            return 0;

        try
        {
            if( !caseSensitive )
            {
                inStr = inStr.toLowerCase();

                key = key.toLowerCase();
            }

            int count = 0;

            int index = inStr.indexOf( key );

            while( index >= 0 && index < inStr.length() )
            {
                count++;

                index += key.length();

                if( index < inStr.length() )
                    index = inStr.indexOf( key , index );
            }

            return count;
        }

        catch( Exception e )
        {
            logIt( "getKeyCount( " + inStr + ", key=" + key + ", caseSensitive="+ caseSensitive + " ) " + e.toString() );

            throw new STException( e );
        }
    }


    public static long getWordCount( String line )
    {
        long numWords = 0;

        // first convert to only alphas, numbers, and space
        String newLine = line.replaceAll( "[^a-zA-Z0-9]" , " " );

        // LogService.logIt( "StringUtils.getWordCount( " + line + " ) " + newLine );

        String[] strs = newLine.split( "\\x20" );

        for( String str : strs )
        {
            if( str.trim().length() > 0 )
                numWords++;
        }

        return numWords;
    }

    /*

    public static long getWordCount( String line )
    {
        long numWords = 0;

        int index = 0;

        boolean prevWhitespace = true;

        while (index < line.length())
        {
            char c = line.charAt( index++ );

            boolean currWhitespace = Character.isWhitespace(c);

            if ( prevWhitespace && !currWhitespace )
            {
                numWords++;
            }
            prevWhitespace = currWhitespace;
        }

        return numWords;
    }
    */

    public static List<String> getWords( String line )
    {
        List<String> outList = new ArrayList<String>();

        // first convert to only alphas, numbers, and space
        String newLine = line.replaceAll( "[^a-zA-Z0-9]" , " " );

        // LogService.logIt( "StringUtils.getWords( " + line + " ) " + newLine );

        String[] strs = newLine.split( "\\x20" );

        for( String str : strs )
        {
            if( str.trim().length() > 0 )
                outList.add( str );
        }

        return outList;
    }



    public static String convertToCSV( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return "";

        // Fields with embedded commas must be delimited with double-quote characters.
        //Fields that contain double quote characters must be surounded by double-quotes, and the embedded double-quotes must each be represented by a pair of consecutive double quotes.
        //A field that contains embedded line-breaks must be surounded by double-quotes

        // replace quoes with double quotes
        String outStr = replaceStr( inStr , "\"", "\"\"" );

        // if contains commas or double quotes
        if( outStr.indexOf( "\n" ) >= 0 || outStr.indexOf( "\"" ) >= 0 || outStr.indexOf( "," ) >= 0 )
            outStr = "\"" + outStr + "\"";

        else if( outStr.startsWith( " " ) || outStr.endsWith( " " ) )
            outStr = "\"" + outStr + "\"";

        return outStr;
    }


    /**
     * Truncates the provided String by whole segments using separator as the segment
     * splitter.  If the first segment does not fit, this is truncated without regard
     * to separator.
     *
     * @param inStr
     * @param maxLength
     * @param separator
     * @return
     */
    public static String truncateWhole( String inStr ,
                                        int maxLength ,
                                        String separator )
    {
        // if not possible, don't bother
        if( inStr == null || inStr.length() == 0 || separator == null || separator.length() == 0 || maxLength == 0 || inStr.length() < separator.length() )
            return "";

        // if no need to truncate
        if( inStr.length() < maxLength )
            return inStr;

        String[] keys = inStr.split( separator );

        StringBuilder outStr = new StringBuilder();

        for( String key : keys )
        {
            // first key
            if( outStr.length() == 0 )
            {
                // first key is too long, OK to break the first key.
                if( key.length() > maxLength )
                    outStr.append( key.substring( 0 , maxLength ) );

                // first key is not too long
                else
                    outStr.append( key );
            }

            // next key is not too long
            else if( outStr.length() + separator.length() + key.length() < maxLength )
                outStr.append( separator + key );

            // next key must be too long
            else
                break;
        }

        return outStr.toString();

    }


    
    public static String generateRandomStringForPin( int length )
    {
        String p = generateRandomString( length );
        
        while( p.toLowerCase().startsWith( "oat" ) )
        {
            p = generateRandomString( length );
        }
        
        return p;
    }

    


    /**
     * Returns a random alphanumeric string of the desired length
     *
     * @param length
     * @return
     */
    public static String generateRandomString( int length )
    {
        StringBuilder sb = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        int index;
        for( int i=0 ; i<length ; i++ )
        {
            index = secureRandom.nextInt(alphaDigits.length);

            sb.append( alphaDigits[ index ] );
        }

        return sb.toString();
    }    


    public static String correctOddQuotes( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = correctAsciiQuotes( inStr );
        inStr = replaceChar( inStr , '`' , '\'' );

        return inStr;
    }



    public static String getShortState( String state )
    {
        if( state.equalsIgnoreCase( "Armed Forces Americas (except Canada)" ) ) return "AA";
        else if( state.equalsIgnoreCase( "Armed Forces Africa/Canada/Europe/Middle East" ) ) return "AE";
        else if( state.equalsIgnoreCase( "Alaska" ) ) return "AK";
        else if( state.equalsIgnoreCase( "Alabama" ) ) return "AL";
        else if( state.equalsIgnoreCase( "Armed Forces Pacific" ) ) return "AP";
        else if( state.equalsIgnoreCase( "Arkansas" ) ) return "AR";
        else if( state.equalsIgnoreCase( "American Samoa" ) ) return "AS";
        else if( state.equalsIgnoreCase( "Arizona" ) ) return "AZ";
        else if( state.equalsIgnoreCase( "California" ) ) return "CA";
        else if( state.equalsIgnoreCase( "Colorado" ) ) return "CO";
        else if( state.equalsIgnoreCase( "Connecticut" ) ) return "CT";
        else if( state.equalsIgnoreCase( "District of Columbia" ) ) return "DC";
        else if( state.equalsIgnoreCase( "Delaware" ) ) return "DE";
        else if( state.equalsIgnoreCase( "Florida" ) ) return "FL";
        else if( state.equalsIgnoreCase( "Federated States of Micronesia" ) ) return "FM";
        else if( state.equalsIgnoreCase( "Georgia" ) ) return "GA";
        else if( state.equalsIgnoreCase( "Guam" ) ) return "GU";
        else if( state.equalsIgnoreCase( "Hawaii" ) ) return "HI";
        else if( state.equalsIgnoreCase( "Iowa" ) ) return "IA";
        else if( state.equalsIgnoreCase( "Idaho" ) ) return "ID";
        else if( state.equalsIgnoreCase( "Illinois" ) ) return "IL";
        else if( state.equalsIgnoreCase( "Indiana" ) ) return "IN";
        else if( state.equalsIgnoreCase( "Kansas" ) ) return "KS";
        else if( state.equalsIgnoreCase( "Kentucky" ) ) return "KY";
        else if( state.equalsIgnoreCase( "Louisiana" ) ) return "LA";
        else if( state.equalsIgnoreCase( "Massachusetts" ) ) return "MA";
        else if( state.equalsIgnoreCase( "Maryland" ) ) return "MD";
        else if( state.equalsIgnoreCase( "Maine" ) ) return "ME";
        else if( state.equalsIgnoreCase( "Marshall Islands" ) ) return "MH";
        else if( state.equalsIgnoreCase( "Michigan" ) ) return "MI";
        else if( state.equalsIgnoreCase( "Minnesota" ) ) return "MN";
        else if( state.equalsIgnoreCase( "Missouri" ) ) return "MO";
        else if( state.equalsIgnoreCase( "Northern Mariana Islands" ) ) return "MP";
        else if( state.equalsIgnoreCase( "Mississippi" ) ) return "MS";
        else if( state.equalsIgnoreCase( "Montana" ) ) return "MT";
        else if( state.equalsIgnoreCase( "North Carolina" ) ) return "NC";
        else if( state.equalsIgnoreCase( "North Dakota" ) ) return "ND";
        else if( state.equalsIgnoreCase( "Nebraska" ) ) return "NE";
        else if( state.equalsIgnoreCase( "New Hampshire" ) ) return "NH";
        else if( state.equalsIgnoreCase( "New Jersey" ) ) return "NJ";
        else if( state.equalsIgnoreCase( "New Mexico" ) ) return "NM";
        else if( state.equalsIgnoreCase( "Nevada" ) ) return "NV";
        else if( state.equalsIgnoreCase( "New York" ) ) return "NY";
        else if( state.equalsIgnoreCase( "Ohio" ) ) return "OH";
        else if( state.equalsIgnoreCase( "Oklahoma" ) ) return "OK";
        else if( state.equalsIgnoreCase( "Oregon" ) ) return "OR";
        else if( state.equalsIgnoreCase( "Pennsylvania" ) ) return "PA";
        else if( state.equalsIgnoreCase( "Puerto Rico" ) ) return "PR";
        else if( state.equalsIgnoreCase( "Palau" ) ) return "PW";
        else if( state.equalsIgnoreCase( "Rhode Island" ) ) return "RI";
        else if( state.equalsIgnoreCase( "South Carolina" ) ) return "SC";
        else if( state.equalsIgnoreCase( "South Dakota" ) ) return "SD";
        else if( state.equalsIgnoreCase( "Tennessee" ) ) return "TN";
        else if( state.equalsIgnoreCase( "Texas" ) ) return "TX";
        else if( state.equalsIgnoreCase( "Utah" ) ) return "UT";
        else if( state.equalsIgnoreCase( "Virginia" ) ) return "VA";
        else if( state.equalsIgnoreCase( "Virgin Islands" ) ) return "VI";
        else if( state.equalsIgnoreCase( "Vermont" ) ) return "VT";
        else if( state.equalsIgnoreCase( "Washington" ) ) return "WA";
        else if( state.equalsIgnoreCase( "Wisconsin" ) ) return "WI";
        else if( state.equalsIgnoreCase( "West Virginia" ) ) return "WV";
        else if( state.equalsIgnoreCase( "Wyoming" ) ) return "WY";

        return state;
    }



    /**
     * Returns a new version of inStr with first character set to upper case.
     *
     * @param inStr
     * @return
     */
    public static String setFirstCharToUpperCase( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        inStr = inStr.trim();

        if( inStr.length() == 0 )
            return inStr;

        char ch = inStr.charAt( 0 );

        char ch2 = Character.toUpperCase( ch );

        StringBuilder sb = new StringBuilder();

        sb.append( ch2 );

        if( inStr.length() > 1 )
            sb.append( inStr.substring( 1 , inStr.length() ) );

        return sb.toString();
    }



    public static String removeNonDigitChars( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        String outStr = "";

        for( int i=0 ; i<inStr.length() ; i++ )
        {
            if( Character.isDigit( inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
        }

        return outStr;
    }

    /*
    public static String removeNonAscii( String inStr )
    {
        if( inStr == null || inStr.isEmpty() )
            return inStr;

        return inStr.replaceAll( "[^\\p{ASCII}]", "");
    }
    */
    
    
    public static String alphaCharsOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        String outStr = "";

        for( int i=0 ; i<inStr.length() ; i++ )
        {
            if( Character.isLetter( inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
        }

        return outStr;

    }

    public static String alphaDigitDashCharsOnly( String inStr )
    {
        if( inStr == null || inStr.length() == 0 )
            return inStr;

        String outStr = "";

        for( int i=0 ; i<inStr.length() ; i++ )
        {
            if( Character.isLetter( inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
            else if( Character.isDigit(inStr.charAt( i ) ) )
                outStr += inStr.charAt( i );
            else if( inStr.charAt( i )=='-' )
                outStr += inStr.charAt( i );
        }

        return outStr;

    }
    
    

    /**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(String in)
    {
        StringBuilder out = new StringBuilder(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.

        for (int i = 0; i < in.length(); i++)
        {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    /**
     * logs messages
     */
    private static void logIt( String message )
    {
        LogService.getLogger().fine( message );
    }

    public static String escapeUnicodeToAsciiStr(String inStr) {
        StringBuilder out = new StringBuilder();
        char ch;
        for (int i = 0; i < inStr.length(); i++) {
            ch = inStr.charAt(i);
            if (ch <= 127) {
                out.append(ch);
            } else {
                out.append("\\u").append(String.format("%04x", (int) ch));
            }
        }
        return out.toString();
    }

    public static String escapeUnicodeToAsciiStrForExtV2(String inStr) {
        StringBuilder out = new StringBuilder();
        char ch;
        for (int i = 0; i < inStr.length(); i++) {
            ch = inStr.charAt(i);
            if (ch <= 127) {
                out.append(ch);
            } else {
                out.append("U+").append(String.format("%04x", (int) ch));
            }
        }
        return out.toString();
    }

    /**
     * This method worked for Chinese!
     * 
     * @param inStr
     * @return 
     */
    public static String escapeUnicodeToAsciiStrForExt(String inStr) {
        StringBuilder out = new StringBuilder();
        char ch;
        int val;
        for (int i = 0; i < inStr.length(); i++) {
            ch = inStr.charAt(i);
            if (ch <= 127) {
                out.append(ch);
            } else {
                
                val = ch;
                
                out.append("&#" + val + ";");
            }
        }
        return out.toString();
    }




}