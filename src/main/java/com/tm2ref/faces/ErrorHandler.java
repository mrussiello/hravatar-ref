/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.faces;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

@Named
@RequestScoped
public class ErrorHandler {
   
	 

    public String getRequestURI()
    {
        return (String)FacesContext.getCurrentInstance().getExternalContext().getRequestMap().get("jakarta.servlet.error.request_uri");
    }    
}
