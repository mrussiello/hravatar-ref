/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.cscase;

import com.tm2ref.entity.cscase.CSCase;
import com.tm2ref.entity.cscase.CSCaseEntry;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.User;
import com.tm2ref.ref.RcCheckLogUtils;
import com.tm2ref.service.AdminEmailUtils;
import com.tm2ref.service.LogService;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author miker_000
 */
public class CsCaseUtils 
{
    public static CSCase createCsCaseAndEmailAdmin( RcCheck rc, User refUser, String userFullname, String userEmail, String userNote, String systemNote, boolean createRcLogEntry, String ipAddress, String userAgent )
    {
        try
        {
            StringBuilder sb = new StringBuilder();            
            sb.append( "Reference Check Issue:\n" + 
                       "RcCheck=" + rc.toStringShort() + "\n" + 
                       "User from Link: " + refUser.getFullname() + ", " + refUser.getEmail() + ", userId=" + refUser.getUserId() + "\n " + 
                       "User Info from form: " + userFullname + ", " + userEmail + "\n\n" + 
                       "User Note: " + userNote + "\n\n" + 
                       "System Note: " + systemNote );
            
            CSCase csCase = new CSCase();
            csCase.setRcCheckId( rc.getRcCheckId() );
            csCase.setUserId( refUser.getUserId() );
            csCase.setOrgId( rc.getOrgId() );
            csCase.setCreateDate( new Date() );
            csCase.setLastUpdate( new Date() );
            
            CSCaseFacade csCaseFacade = CSCaseFacade.getInstance();
            csCase = csCaseFacade.saveCSCase(csCase);
            
            CSCaseEntry cse = new CSCaseEntry();
            cse.setCsCaseId( csCase.getCsCaseId() );
            cse.setCreateDate( csCase.getCreateDate() );
            cse.setUserId( refUser.getUserId() );
            cse.setMessage( sb.toString() );
            cse.setFormat( 0 );
            cse = csCaseFacade.saveCSCaseEntry(cse);
            
            List<CSCaseEntry> csel = new ArrayList<>();
            csel.add( cse );
            csCase.setCsCaseEntryList(csel);
            
            AdminEmailUtils.sendCsCaseEmail( csCase );
            
            if( createRcLogEntry )
            {
                RcCheckLogUtils.createRcCheckLogEntry( rc.getRcCheckId(), rc.getRcRater()!=null ? rc.getRcRater().getRcRaterId() : 0, 1, sb.toString(), ipAddress, userAgent );
            }
            
            return csCase;            
        }
        catch( Exception e )
        {
            LogService.logIt( e, "CsCaseUtils.createCsCaseAndEmailAdmin() " + (rc==null ? "rcCheck is null" : rc.toStringShort() ) + ", userFullname=" + userFullname + ", userEmail=" + userEmail + ", userNote=" + userNote + ", systemNote=" + systemNote );
            return null;
            // AdminEmailUtils.sendAdminEmail( "RefCheck System Error", String content, String emailsRuntimeConstantsKey )
        }
    }
}
