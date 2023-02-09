package com.tm2ref.util;

import java.io.Serializable;

public class NVPair implements Serializable, Comparable
{
    private static final long serialVersionUID = 1L;

    private Serializable name;

    private Serializable value;


    public NVPair()
    {}


    public NVPair( Serializable name , Serializable value )
    {
        this.name = name;

        this.value = value;
    }


    public int compareTo( Object o )
    {
        if( o instanceof NVPair )
        {
            NVPair other = (NVPair) o;

            if( name instanceof Comparable && other.getName() instanceof Comparable )
                return ((Comparable) name).compareTo( other.getName() );
        }

        return 0;
    }


    public Serializable getName()
    {
        return name;
    }



    public void setName( Serializable name )
    {
        this.name = name;
    }

    public Serializable getValue()
    {
        return value;
    }

    public void setValue( Serializable value )
    {
        this.value = value;
    }



}
