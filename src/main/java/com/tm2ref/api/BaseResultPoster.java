/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.api;

import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.report.ReportUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserFacade;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author miker
 */
public class BaseResultPoster 
{
    protected Map<String,String> getBasicAuthCredsFromReportFlags( RcCheck rc, TestKey testKey ) throws Exception
    {
        Map<String,String> out = null;
        
        if( testKey!=null )
        {
            out = testKey.getBasicAuthParmsForResultsPost();

            if( out!=null && out.containsKey("username") && out.containsKey("password"))
                return out;
        
        }

        if( rc==null )
            return null;
                
        try
        {
            if( rc.getOrg()==null )
                rc.setOrg( UserFacade.getInstance().getOrg( rc.getOrgId()));
            
            if( rc.getOrg()==null )
                throw new Exception( "RcCheck.Org is null." );
            
            int authTypeId = ReportUtils.getReportFlagIntValue("resultpostauthtypeid", null, rc.getOrg(), null );
            if( authTypeId!=1 )
                return null;
            
            String username = ReportUtils.getReportFlagStringValue("resultpostauthparam1", null, rc.getOrg(), null);
            String password = ReportUtils.getReportFlagStringValue("resultpostauthparam2", null, rc.getOrg(), null);
            
            if( username==null || username.isBlank() || password==null || password.isBlank() )
            {
                LogService.logIt( "BaseResultPoster.getBasicAuthCredsFromReportFlags() username and/or password invalid. Returning null. username=" + (username==null ? null : username) + ", password.len=" + (password==null ? "null" : password.length()) + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) );
                return null;
            }

            username=username.trim();
            password = password.trim();
            out = new HashMap<>();
            out.put( "username", username);
            out.put( "password", password);
            return out;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "BaseResultPoster.getBasicAuthCredsFromReportFlags() rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) );
            return null;
        }
    }            
}
