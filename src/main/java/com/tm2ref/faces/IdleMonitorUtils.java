package com.tm2ref.faces;



import com.tm2ref.global.Constants;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserBean;
import com.tm2ref.user.UserUtils;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;


@Named
@RequestScoped
public class IdleMonitorUtils
{

    @Inject 
    UserBean userBean; 
      
    public int getSessionTimeoutMilliseconds()
    {
        return Constants.IDLE_SESSION_TIMEOUT_MINS*60*1000;
    }
    
    
    public void onIdle() 
    {
        try
        {
            LogService.logIt( "IdleMonitorUtils.onIdle() Tm2Ref loggedOnAccount=" + (userBean==null ? "NULL" : userBean.getUserLoggedOnAsAdmin()) );
            // if logged on, logout.
            if( userBean!=null && userBean.getUserLoggedOnAsAdmin() )
            {
                UserUtils userUtils = UserUtils.getInstance();                
                userUtils.processUserLogOff();
            }

            FacesContext fc = FacesContext.getCurrentInstance();
            fc.getExternalContext().invalidateSession();
            fc.getExternalContext().redirect( "/tr/index.xhtml" ); 
        }
        catch( Exception e )
        {
            LogService.logIt( "IdleMonitorUtils.onIdle() " + userBean.getUserLoggedOnAsAdmin() );
        }
    }
 
    

}
