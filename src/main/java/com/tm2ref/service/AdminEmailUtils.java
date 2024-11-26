/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.service;

import com.tm2ref.entity.cscase.CSCase;
import com.tm2ref.global.Constants;
import com.tm2ref.global.RuntimeConstants;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 *
 * @author Mike
 */
public class AdminEmailUtils {

    
    public static void sendCsCaseEmail( CSCase csCase )
    {
        String subject = "Reference Check Support Request";
        String content = "CsCase Id: " + csCase.getCsCaseId() + "\n\n" + csCase.getCsCaseEntryList().get(0).getMessage();        
        sendAdminEmail( subject, content, "CSCaseEmails" );     
    }
    
    
    public static void sendAdminEmail( String subject, String content, String emailsRuntimeConstantsKey )
    {
         Map<String, Object> emailMap = new HashMap<>();

         try
         {
            if(!subject.toLowerCase().contains("tm2ref") )
                subject = "Tm2Ref " + subject;
            
            EmailerFacade emailerFacade = EmailerFacade.getInstance();

            emailMap.put( EmailConstants.SUBJECT,  subject );

            content = EmailUtils.addNoReplyMessage(content, false, Locale.US );
                        
            emailMap.put( EmailConstants.CONTENT, content );

            // StringBuilder sb = new StringBuilder();

            emailMap.put(EmailConstants.TO, RuntimeConstants.getStringValue(emailsRuntimeConstantsKey ) );

            emailMap.put( EmailConstants.FROM, RuntimeConstants.getStringValue("no-reply-email")  );

            emailerFacade.sendEmail( emailMap );

         }

         catch( Exception e )
         {
            LogService.logIt( e, "AdminEmailUtils.sendAdminEmail() subj=" + subject + ", content=" + content );
         }
    }


}
