/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.proctor;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.User;
import com.tm2ref.faces.FacesBean;
import com.tm2ref.ref.RefUserType;
import java.io.Serializable;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;

/**
 *
 * @author miker_000
 */
@Named
@SessionScoped
public class ProctorBean extends FacesBean implements Serializable {
    
    private boolean sessionPhotoComplete;
    private boolean sessionIdPhotoComplete;
    private boolean cameraOptOut;
    
    String email;
    String mobileNum;
    
    int photoUploadAttempts=0;
    
    
    
    public static ProctorBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (ProctorBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "proctorBean" );
    }

    public void clearBean()
    {
        sessionPhotoComplete=false;
        sessionIdPhotoComplete=false;
        cameraOptOut=false;
        email=null;
        mobileNum=null;
    }
    
    public void init( RcCheck rc, RefUserType refUserType )
    {
        User user = null;
        
        if( refUserType.getIsCandidate() )
            user = rc.getUser();

        else
            user = rc.getRcRater().getUser();
        
        if( user==null || !user.getUserType().getNamed() )
            return;
        
        email = user.getEmail();
        mobileNum = user.getMobilePhone();
    }


    public boolean getSessionPhotoComplete() {
        return sessionPhotoComplete;
    }

    public void setSessionPhotoComplete(boolean sessionPhotoComplete) {
        this.sessionPhotoComplete = sessionPhotoComplete;
    }

    public boolean getSessionIdPhotoComplete() {
        return sessionIdPhotoComplete;
    }

    public void setSessionIdPhotoComplete(boolean sessionIdPhotoComplete) {
        this.sessionIdPhotoComplete = sessionIdPhotoComplete;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public boolean getCameraOptOut() {
        return cameraOptOut;
    }

    public void setCameraOptOut(boolean cameraOptOut) {
        this.cameraOptOut = cameraOptOut;
    }

    public int getPhotoUploadAttempts() {
        return photoUploadAttempts;
    }

    public void setPhotoUploadAttempts(int faceUploadAttempts) {
        this.photoUploadAttempts = faceUploadAttempts;
    }
    
    
    
}
