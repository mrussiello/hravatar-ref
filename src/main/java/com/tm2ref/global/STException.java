/*
 * Created on Dec 30, 2006
 *
 */
package com.tm2ref.global;


public class STException extends Exception
{
    String key=null;

    String arg0=null;

    String[] params=null;

    boolean sendToReturn = false;


    public STException( String key , String message , String arg0 )
    {
        super( message );

        this.key = key;

        this.arg0 = arg0;
    }



    public STException( String key , String message )
    {
        super( message );

        this.key = key;

        this.arg0 = message;
    }


    public STException( String key , String[] params )
    {
        super( key );

        this.key = key;

        this.params = params;
    }


    public STException( String key )
    {
        this.key = key;
    }


    public STException( Exception e )
    {
        super( e.getMessage() );

        this.arg0 = e.getMessage();

        if( this.arg0 == null && e instanceof NullPointerException )
            this.arg0 = "NullPointerException";



        if( this.arg0 == null || this.arg0.length() == 0 )
            this.arg0 = "No Error Message Provided, " + e.toString();

        key = "g.SystemError";
    }

    
    @Override
    public String toString() {

        String p = "";

        if( params != null &&params.length>0 )
        {
            for( String s : params )
            {
                if( !p.isEmpty() )
                    p += ", ";

                p += s;
            }
        }

        return "STException{" + "key=" + key + ", arg0=" + arg0 + ", params=[" + p + "] " + getMessage();
    }




    public String[] getParams()
    {
        if( params != null )
            return params;

        String[] xparams = new String[ arg0 == null ? 0 : 1 ];

        if( arg0 != null )
            xparams[0] = arg0;

        return xparams;
    }


    /**
     * @return the key
     */
    public String getKey()
    {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key)
    {
        this.key = key;
    }


    /**
     * @return the arg0
     */
    public String getArg0()
    {
        return arg0;
    }


    /**
     * @param arg0 the arg0 to set
     */
    public void setArg0(String arg0)
    {
        this.arg0 = arg0;
    }



}
