/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.api;

/**
 *
 * @author Mike
 */
public class APIException extends Exception {

    int code;
    String message2;

    public APIException( int code, String msg, String msg2 )
    {
        super(msg);
        this.code = code;
        this.message2 = msg2;
    }

    public APIException( int code, String msg )
    {
        super(msg);
        this.code = code;
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage2() {
        return message2;
    }

    public void setMessage2(String message2) {
        this.message2 = message2;
    }


}
