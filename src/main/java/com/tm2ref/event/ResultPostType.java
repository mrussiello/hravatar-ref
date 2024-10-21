package com.tm2ref.event;


public enum ResultPostType
{
    DEFAULT(0,"Default (Standard XML Post)");

    private int resultPostTypeId;

    private String name;

    private ResultPostType( int typeId , String key )
    {
        this.resultPostTypeId = typeId;

        this.name = key;
    }

    public String getName()
    {
        return name;
    }

    public int getResultPostTypeId() {
        return resultPostTypeId;
    }



}
