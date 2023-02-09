/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheckLog;
import com.tm2ref.faces.HttpReqUtils;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.servlet.http.HttpServletRequest;

/**
 *
 * @author Dad
 */
@Named
@RequestScoped
public class RefLogItEntry
{

    RcFacade rcFacade = null;

    String rcid,rtrid,tran,logentry,level;
    
    //@Inject
    //TestBean testBean;


    public void doLogItEntry( ComponentSystemEvent ev )
    {
        long rcCheckId = 0;
        long rcRaterId = 0;

        try
        {
            // LogService.logIt( "RefLogItEntry.doLogItEntry() starting tran=" + tran + ", level=" + level + ", rcid=" + rcid + ", rtrid=" + rtrid + ", logentry=" + logentry );

            // FacesContext fc = FacesContext.getCurrentInstance();


            if( rcFacade == null )
                rcFacade = RcFacade.getInstance();

            if( tran == null || !tran.equalsIgnoreCase( "logit" ) )
                return;

            //if( rcid==null || rcid.isEmpty() )
            //    return;

            if( logentry == null || logentry.isEmpty() )
                return;

            if( rcid==null || rcid.isEmpty() )
            {
                LogService.logIt( "RefLogItEntry.doLogItEntry() ERROR Log Entry received but no RcCheckId present. level=" + level + ", Entry=" + logentry + ", ip=" + getIpAddress() );
                return;
            }

            rcCheckId = Long.parseLong( EncryptUtils.urlSafeDecrypt( rcid ));

            if( rcCheckId <= 0 )
            {
                LogService.logIt( "RefLogItEntry.doLogItEntry() ERROR rcCheckId Invalid= " + rcCheckId + ", rcid=" + rcid + ", rtrid=" + rtrid + ", level=" + level + ", Entry=" + logentry + ", ip=" + getIpAddress() );
                return;
                // throw new Exception( "rcCheckId Invalid= " + rcCheckId );
            }
            
            if( rtrid!=null && !rtrid.isBlank() && !rtrid.equalsIgnoreCase("0") )
            {
                try
                {
                    rcRaterId = Long.parseLong( EncryptUtils.urlSafeDecrypt( rtrid ));
                }
                catch( NumberFormatException e )
                {
                    LogService.logIt( "RefLogItEntry.doLogItEntry() parsing rtrid. " + e.toString() + ", rcCheckId=" + rcCheckId + ", rcid=" + rcid + ", rtrid=" + rtrid + ", level=" + level + ", Entry=" + logentry + ", ip=" + getIpAddress() );
                    rcRaterId=0;
                }
            }
                        
            int intParam1=0;
            int intParam2=0;
            
            if( logentry!=null )
            {
                int idx = logentry.indexOf( ", lint1:" );
                if( idx>=0 )
                {
                    idx += 8;
                    String i1 = logentry.substring( idx, logentry.indexOf( " ", idx ) );
                    intParam1 = Integer.parseInt( i1 );
                }

                idx = logentry.indexOf( ", lint2:" );
                if( idx>=0 )
                {
                    idx += 8;
                    String i2 = logentry.substring( idx, logentry.indexOf( " ", idx ) );
                    intParam2 = Integer.parseInt( i2 );
                }                
            }

            // RcCheckLog ahl = rcFacade.getRcCheckLogByTestEventId(rcCheckId);

            RcCheckLog ahl = new RcCheckLog();

            ahl.setRcCheckId( rcCheckId );
            ahl.setRcRaterId(rcRaterId);
            
            ahl.setIntParam1(intParam1);
            ahl.setIntParam2(intParam2);

            // if( ahl == null )
            // {
            //     ahl = new RcCheckLog();

            //     ahl.setTestEventId( rcCheckId );
            // }

            int lastLevel = Integer.parseInt( level==null || level.isEmpty() ? "0" : level );

            ahl.appendLogEntry( logentry, lastLevel );

            try
            {
               HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

               ahl.setIpAddress(HttpReqUtils.getClientIpAddress(req) );
               ahl.setUserAgent( req.getHeader( "User-Agent" ) );
            }

            catch( Exception e )
            {
                LogService.logIt( e, "RefLogItEntry.doLogItEntry() Error getting UserAgent/IpAddress. rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", level=" + level + ", entry=" + logentry + ", ip=" + getIpAddress()  );
            }


            rcFacade.saveRcCheckLog(ahl);

            if( lastLevel == 1 )
                Tracker.addRcLogWarningMessage();

            else if( lastLevel == 0 )
                Tracker.addRcLogErrorMessage();

            else
                Tracker.addRcLogInfoMessage();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "RefLogItEntry.doLogItEntry() rcCheckId=" + rcCheckId + ", rcRaterId=" + rcRaterId + ", rcid=" + rcid + ", rtrid=" + rtrid + ", level=" + level + ", entry=" + logentry + ", ip=" + getIpAddress() );
        }

    }
    
    private String getIpAddress()
    {
            try
            {
               HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();

               if( req!=null )
                   return HttpReqUtils.getClientIpAddress(req);
            }

            catch( Exception e )
            {
                LogService.logIt( e, "RefLogItEntry.getIpAddress() " );
            }
            return null;
    }
    



    public String getTran() {
        return tran;
    }

    public void setTran(String tran) {
        this.tran = tran;
    }

    public String getLogentry() {
        return logentry;
    }

    public void setLogentry(String logentry) {
        this.logentry = logentry;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getRcid() {
        return rcid;
    }

    public void setRcid(String rcid) {
        this.rcid = rcid;
    }

    public String getRtrid() {
        return rtrid;
    }

    public void setRtrid(String rtrid) {
        this.rtrid = rtrid;
    }





}
