/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.faces.FacesUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.UserBean;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.ConfigurableNavigationHandler;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.inject.Inject;

/**
 *
 * @author Dad
 */
@Named
@RequestScoped
public class SelectDeviceEntry
{

    String deviceid,tran,knd,view,devicelabel;
    
    @Inject
    UserBean userBean;
    


    public void doSelectDeviceEntry( ComponentSystemEvent ev )
    {
        try
        {
            LogService.logIt( "SelectDeviceEntry.doSelectDeviceEntry() deviceid=" + deviceid + ", knd=" + knd + ", tran=" + tran + ", view=" + view );
            
            if( tran!=null && tran.equals("seldevice") && deviceid!=null && !deviceid.isBlank() && knd!=null && !knd.isBlank()  )
            {
                if( knd.equals("videoinput") )
                {
                    userBean.setSelCamera(deviceid);
                    FacesUtils.getInstance().setInfoMessage("g.WebcamChanged", new String[]{devicelabel} );
                }
                else if( knd.equals("audioinput") )
                {
                    userBean.setSelMicrophone(deviceid);
                    FacesUtils.getInstance().setInfoMessage("g.MicChanged", new String[]{devicelabel} );
                }
            }
            
            if( view==null || view.isBlank() )
                view="/pp/help-camera.xhtml";
            
            

            ConfigurableNavigationHandler nav  = (ConfigurableNavigationHandler) FacesContext.getCurrentInstance().getApplication().getNavigationHandler();
            nav.performNavigation( view );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "SelectDeviceEntry.doSelectDeviceEntry() deviceid=" + deviceid + ", knd=" + knd + ", tran=" + tran );
        }

    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
    }

    public String getTran() {
        return tran;
    }

    public void setTran(String tran) {
        this.tran = tran;
    }

    public String getKnd() {
        return knd;
    }

    public void setKnd(String knd) {
        this.knd = knd;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public String getDevicelabel() {
        return devicelabel;
    }

    public void setDevicelabel(String devicelabel) {
        this.devicelabel = devicelabel;
    }
    

}
