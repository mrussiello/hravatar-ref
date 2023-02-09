/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.reminder;

import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 * @author Mike
 */
public class ReminderStarter implements Runnable {

    
    public static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static ScheduledFuture<?> sched = null;
    
    
    @Override
    public void run() {

        // LogService.logIt( "AutoScoreStarter.run() Starting"  );

        try
        {
            // LogService.logIt( "AutoScoreStarter.run() START AutoBatches On=" + RuntimeConstants.getBooleanValue( "autoScoreOk" ) );
            
            if( !RuntimeConstants.getBooleanValue( "autoRemindersOk" ) )
                return;
            
            // wait one minute
            // Thread.sleep( 60000 );
            Thread.sleep( 60000 );
            
            // LogService.logIt( "AutoScoreStarter.run() STARTING SETUP  BBBB ");
            final Runnable reminderThread = new ReminderThread();
    
            // final ScheduledFuture<?> sched = scheduler.scheduleAtFixedRate(autoScoreThread, 30, 180, SECONDS);
            sched = scheduler.scheduleAtFixedRate( reminderThread, 60*60, 60*60, SECONDS );
            LogService.logIt( "ReminderStarter.run() COMPLETED SETUP  CCCC ");
        }
        catch( Exception ee )
        {
            LogService.logIt(ee, "ReminderStarter.run() Uncaught Exception during auto batch." );
            // EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreStarter.run() Uncaught Exception during autobatch.", "Time: " + (new Date()).toString() + ", Error was: " + ee.toString() );
        }



    }



}
