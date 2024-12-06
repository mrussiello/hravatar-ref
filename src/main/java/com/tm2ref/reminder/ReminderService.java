/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.reminder;

import com.tm2ref.service.LogService;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

/**
 *
 * @author Mike
 */
@WebListener
public class ReminderService implements ServletContextListener {
    
    
  @Override
  public void contextInitialized(ServletContextEvent evt) 
  {
  
        try
        {            
            // LogService.logIt( "ReminderService.contextInitialized() STARTING SETUP  AAAA ");
            (new Thread(new ReminderStarter())).start();                       


            // LogService.logIt( "ReminderService.contextInitialized() COMPLETED SETUP  BBBB ");
            (new Thread(new DelayedInvitationStarter())).start();                       
            
            // LogService.logIt( "ReminderService.contextInitialized() COMPLETED SETUP  CCC ");
        }
        
        catch( Exception e )
        {
            LogService.logIt( e, "ReminderService.contextInitialized() ");
           //  EmailUtils.getInstance().sendEmailToAdmin( "AutoScoreService.doReportAutoBatch() Error during Score Batch.", "Time: " + (new Date()).toString() + ", Error was: " + e.toString() );
            
        }
    }
  
  
    @Override
  public void contextDestroyed(ServletContextEvent evt) 
  {
      try
      {
          
          
        if( ReminderStarter.sched != null )
            ReminderStarter.sched.cancel(false);
        
        if( ReminderStarter.scheduler != null )
            ReminderStarter.scheduler.shutdownNow();
      }
      
      catch( Exception e )
      {
          LogService.logIt(e, "ReminderService.coonextDestroyed() Stopping AutoScoreStarter." );
      }
 }  
    

}
