/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.faces;

import jakarta.faces.context.ExceptionHandler;
import jakarta.faces.context.ExceptionHandlerFactory;

/**
 *
 * @author miker_000
 */
public class RefExceptionHandlerFactory extends ExceptionHandlerFactory {
    
private ExceptionHandlerFactory exceptionHandlerFactory;

    public RefExceptionHandlerFactory() {
    }

    public RefExceptionHandlerFactory(ExceptionHandlerFactory ehf) {
        this.exceptionHandlerFactory = ehf;
    }

    @Override
    public ExceptionHandler getExceptionHandler() {
        return new RefExceptionHandler(exceptionHandlerFactory.getExceptionHandler());
    }    
}
