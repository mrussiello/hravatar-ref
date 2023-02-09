/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ws;

import com.tm2ref.service.Tracker;



/**
 *
 * @author miker_000
 */
public class ApiException extends Exception {
   
    int code;
    int httpResponseCode = 400; 
    
    public ApiException( String message, int code, int httpResponseCode )
    {
        super(message);
        this.code=code;
        this.httpResponseCode=httpResponseCode;
        Tracker.addError();
    }

    public int getCode() {
        return code;
    }

    public int getHttpResponseCode() {
        return httpResponseCode;
    }
    
    @Override
    public String toString() 
    {
        return "ApiException " + getMessage() + ", HRA API ErrorCode=" + code + ", httpResponseCode=" + httpResponseCode; 
    }
    
}
