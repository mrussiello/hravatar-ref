/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.reminder;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;

/**
 *
 * @author Mike
 */
public class DelayedInvitationThread implements Runnable {

    boolean isManual = false;
    
    public DelayedInvitationThread()
    {        
    }

    public DelayedInvitationThread( boolean isManual )
    {        
        this.isManual = isManual;
    }

    
    @Override
    public void run() {

        // LogService.logIt( "AutoScoreThread.run() Starting"  );

        try
        {
            if( isManual || RuntimeConstants.getBooleanValue( "autoRemindersOk" ) )
            {
                // LogService.logIt( "DelayedInvitationThread.run() Starting Delayed Invitation Batch "  );
                ReminderUtils sm = new ReminderUtils();

                try
                {
                    sm.doDelayedInvitationBatch();
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "DelayedInvitationThread.run() Error during Batch." );
                    // EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }
            }

        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "DelayedInvitationThread.run() Uncaught Exception during autobatch." );
            // EmailUtils.getInstance().sendEmailToAdmin( "DelayedInvitationThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
