/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.job;

import com.tm2ref.ai.AiCallType;
import com.tm2ref.ai.AiRequestUtils;
import com.tm2ref.entity.user.User;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import jakarta.json.JsonObject;

/**
 *
 * @author miker
 */
public class EvalPlanSubmissionThread implements Runnable {
    
    static Boolean AI_SCORING_OK = null;
    
    User user;
    long testKeyId;
    long rcCheckId;
    boolean forceRescore;
    
    public EvalPlanSubmissionThread( User user, long testKeyId, long rcCheckId, boolean forceRescore )
    {
        this.user=user;
        this.testKeyId=testKeyId;
        this.rcCheckId=rcCheckId;
        this.forceRescore=forceRescore;
    }
    
    
    public synchronized void init()
    {
        if( AI_SCORING_OK!=null )
            return;
        AI_SCORING_OK = RuntimeConstants.getBooleanValue("tm2ai_rest_api_ok") && RuntimeConstants.getBooleanValue("tm2ai_evalplan_scoring_ok");
    }
    
    @Override
    public void run()
    {
        try
        {
            boolean result = submitEvalPlanScore();
            LogService.logIt( "EvalPlanSubmissionThread.run() result=" + result + ", rcCheckId=" + rcCheckId + ", testKeyId=" + testKeyId + ", userId=" + (user==null ? "null" : user.getUserId()) );
            
        }
        catch( Exception e )
        {
            LogService.logIt(e,"EvalPlanSubmissionThread.run() rcCheckId=" + rcCheckId + ", testKeyId=" + testKeyId + ", userId=" + (user==null ? "null" : user.getUserId()) );
        }
    }
    
    public boolean submitEvalPlanScore() throws Exception
    {
        try
        {
            if( AI_SCORING_OK==null )
                init();
            
            if( !AI_SCORING_OK )
            {
                LogService.logIt("EvalPlanSubmissionThread.submitEvalPlanScore() AI Scoring is not enabled. rcCheckId=" + rcCheckId + ", testKeyId=" + testKeyId + ", userId=" + (user==null ? "null" : user.getUserId()) );
                return false;
            }
            
            if( testKeyId<=0 && rcCheckId<=0 )
                throw new Exception( "TestKeyId and RcCheckId are both invalid. " );

            if( user==null )
                throw new Exception( "User is null." );
            
            JsonObject responseJo = AiRequestUtils.doEvalPlanScoringCall(AiCallType.EVALPLAN_SCORE, forceRescore, user, testKeyId, rcCheckId);
            
            if( !AiRequestUtils.wasAiCallSuccess( responseJo ) )
            {
                LogService.logIt("\"EvalPlanSubmissionThread.submitEvalPlanScore() AI Call failed. rcCheckId=" + rcCheckId + ", testKeyId=" + testKeyId + ", userId=" + (user==null ? "null" : user.getUserId()) );
                return false;
            }
            
            return true;
        }
        catch( Exception e )
        {
            LogService.logIt(e,"EvalPlanSubmissionThread.submitEvalPlanScore() rcCheckId=" + rcCheckId + ", testKeyId=" + testKeyId + ", userId=" + (user==null ? "null" : user.getUserId()) );
            throw e;
        }
        
    }
    
}
