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
public class ReminderThread implements Runnable {

    boolean isManual = false;
    
    public ReminderThread()
    {        
    }

    public ReminderThread( boolean isManual )
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
                // LogService.logIt( "AutoScoreThread.doScoreAutoBatch() Starting Score Batch "  );
                ReminderUtils sm = new ReminderUtils();

                try
                {
                    sm.doReminderBatch();
                }

                catch( Exception e )
                {
                    LogService.logIt(e, "ReminderThread.run() Error during Score Batch." );
                    // EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
                }
            }

        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "AutoScoreThread.run() Uncaught Exception during autobatch." );
            // EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreThread.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
