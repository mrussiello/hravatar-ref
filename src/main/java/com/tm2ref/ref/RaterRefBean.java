/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcItem;
import com.tm2ref.entity.user.User;
import com.tm2ref.service.LogService;
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
public class RaterRefBean  extends BaseRefBean implements Serializable {
    
    RcItemWrapper rcItemWrapper;
    int selectedRadioIndex=-1;
    //int[] selectedCheckboxes;
    String[] selectedCheckboxesStr;
    
    int itemDisplayOrderInUse = 0;
    
    private User referralUser;
    
    private String referralNotes;
    
    
    public static RaterRefBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();
        return (RaterRefBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "raterRefBean" );
    }

    public void clearBean()
    {
        rcItemWrapper=null;
        selectedRadioIndex=-1;
        selectedCheckboxesStr=null;
    }
    
    public String getRcItemIdEncrypted()
    {
        if( rcItemWrapper==null || rcItemWrapper.getRcItem()==null )
            return "";

        return rcItemWrapper.getRcItem().getRcItemIdEncrypted();
    }
    
    public RcItemWrapper getRcItemWrapper() {
        return rcItemWrapper;
    }

    public void setRcItemWrapper(RcItemWrapper rcItemWrapper, boolean isCandidateOrEmployee) 
    {
        this.rcItemWrapper = rcItemWrapper;
        
        if( rcItemWrapper!=null )
            itemDisplayOrderInUse=isCandidateOrEmployee ? rcItemWrapper.getCandidateDisplayOrder() : rcItemWrapper.getRaterDisplayOrder();
        else
            itemDisplayOrderInUse=0;
        
        LogService.logIt( "RaterRefBean.setRcItemWrapper() itemDisplayOrderInUse="  + itemDisplayOrderInUse);
        
    }

    public int getSelectedRadioIndex() {
        return selectedRadioIndex;
    }

    public void setSelectedRadioIndex(int selectedRadioIndex) {
        this.selectedRadioIndex = selectedRadioIndex;
    }

    public int getItemDisplayOrder() {
        
        return itemDisplayOrderInUse;
    }
    
    public RcItem getRcItem()
    {
        if( rcItemWrapper==null )
            return null;
        return rcItemWrapper.getRcItem();
        
    }    
    
    
    public RcItemFormatType getRcItemFormatType()
    {
        if( rcItemWrapper==null || rcItemWrapper.getRcItem()==null )
            return RcItemFormatType.NONE;
        return rcItemWrapper.getRcItem().getRcItemFormatType();
    }

    public String[] getSelectedCheckboxesStr() {
        return selectedCheckboxesStr;
    }

    public void setSelectedCheckboxesStr(String[] selectedCheckboxesStr) {
        this.selectedCheckboxesStr = selectedCheckboxesStr;
    }

    public int getItemDisplayOrderInUse() {
        return itemDisplayOrderInUse;
    }

    public void setItemDisplayOrderInUse(int itemDisplayOrderInUse) {
        this.itemDisplayOrderInUse = itemDisplayOrderInUse;
    }

    public User getReferralUser() {
        
        if( referralUser==null )
            referralUser=new User();
        
        return referralUser;
    }

    public void setReferralUser(User referralUser) {
        this.referralUser = referralUser;
    }

    public String getReferralNotes() {
        return referralNotes;
    }

    public void setReferralNotes(String referralNotes) {
        this.referralNotes = referralNotes;
    }


    
    
    
}
