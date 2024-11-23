package com.tm2ref.ref;

import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.User;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.service.LogService;
import java.io.Serializable;

import jakarta.faces.context.FacesContext;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;

@Named
@SessionScoped
public class RefBean extends BaseRefBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    // -1 = unknown and unchecked
    // 0 = unknown but checked.
    // 1 = microphone only
    // 2 = camera (assumes also has a microphone.
    private int recDevs = -1;
    private boolean medRecApi;
    private int hasGetUserMedia;
    
    
    private String accessCode;
    private String errorMessage;
    private String errorReturnUrl;
    private boolean errorAutoForward;
    
    private RefPageType refPageType = RefPageType.ENTRY;
    private RefUserType refUserType = RefUserType.RATER;
    
    private RcCheck rcCheck;
    private String activeAccessCodeX;
    private String activeRefPageTypeIdX;
    
    private User tgtUser;
    
    boolean adminOverride;
    boolean accessibleActive=false;
    
    
    // private String httpSessionId;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
        
    public static RefBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        if( fc==null )
        {
            LogService.logIt( "RefBean.getInstance() Cannot create an instance because fc is null." );
            return null;
        }
        
        return (RefBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "refBean" );
    }

    public void clearBean()
    {
        // can remove this down road.
        // recDevs=-1;
        // medRecApi=false;
        // hasGetUserMedia=0;
        accessibleActive=false;

        adminOverride = false;
        
        accessCode = null;
        rcCheck = null;
        errorMessage = null;
        refPageType = RefPageType.ENTRY;
        refUserType = RefUserType.RATER;
        
        intParam1=0;
        strParam1=null;
        strParam2=null;
        strParam3=null;
        strParam4=null;
        strParam5=null;
        tgtUser=null;
    }

    public boolean getIsMrf()
    {
        return rcCheck!=null && rcCheck.getRcCheckType().getIsEmployeeFeedback();
    }
    
    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

    public String getRcCheckIdEncrypted()
    {
        if( rcCheck==null  )
            return "";
        return rcCheck.getRcCheckIdEncrypted();
    }
    
    public String getUserIdEncrypted()
    {
        if( rcCheck==null || this.refUserType==null )
            return "";
        if( refUserType.getIsCandidate() )
            return rcCheck.getUserIdEncrypted();
        if( refUserType.getIsRater() && rcCheck.getRcRater()!=null )
            return rcCheck.getRcRater().getUserIdEncrypted();
        return "";        
    }
    
    public String getRcRaterIdEncrypted()
    {
        if( rcCheck==null || rcCheck.getRcRater()==null ) // || getRefUserType()==null || !getRefUserType().getIsRater() )
            return "";
        
        return rcCheck.getRcRater().getRcRaterIdEncrypted();
    }

    
    
    /*
    public String getRcRaterIdEncryptedForRaterOnly()
    {
        if( rcCheck==null || rcCheck.getRcRater()==null || getRefUserType()==null || !getRefUserType().getIsRater() )
            return "";
        
        return rcCheck.getRcRater().getRcRaterIdEncrypted();
    }
    */

    
    public int getUploadedUserFileTypeId()
    {
        if( rcCheck==null || getRefUserType()==null || this.refPageType==null )
            return 0;
        if( refUserType.getIsCandidate() )
        {
            if( refPageType.equals( RefPageType.PHOTO ) )
                return UploadedUserFileType.REF_CHECK_IMAGES.getUploadedUserFileTypeId();
            if( refPageType.equals( RefPageType.ID_PHOTO ) )
                return UploadedUserFileType.REF_CHECK_ID.getUploadedUserFileTypeId();
            return 0;
        }
        else
        {
            if( refPageType.equals( RefPageType.PHOTO ) )
                return UploadedUserFileType.REF_CHECK_RATER.getUploadedUserFileTypeId();
            if( refPageType.equals( RefPageType.ID_PHOTO ) )
                return UploadedUserFileType.REF_CHECK_RATER_ID.getUploadedUserFileTypeId();
            return 0;
        }        
    }

    
    public boolean getUsesCameraOrMicrophone()
    {
        return getUsesMicrophone() || getUsesCamera();
    }

    
    public boolean getUsesMicrophone()
    {
        if( rcCheck==null )
            return false;
        
        if( getAudioVideoCommentsOk() )
            return true;  
        
        return false;
    }

    
    public boolean getUsesCamera()
    {
        if( rcCheck==null )
            return false;
        
        // Media-based comments
        if( rcCheck.getRcAvType().getAudioOrVideo() )
            return true;
        
        // candidate photos
        if( refUserType.getIsCandidate() && rcCheck.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() )
            return true;
        
        // rater photos
        if( refUserType.getIsRater() && rcCheck.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() )
        {
            if( rcCheck.getRcRaterPhotoCaptureType().getSameIpOnly() )
            {                        
                if( rcCheck.getIpAddress()==null || !rcCheck.getIpAddress().isBlank() || 
                    rcCheck.getRcRater().getIpAddress()==null || rcCheck.getRcRater().getIpAddress().isBlank() )
                    return false;

                if( rcCheck.getIpAddress()!=null && !rcCheck.getIpAddress().isBlank() && 
                    rcCheck.getRcRater().getIpAddress()!=null && 
                    rcCheck.getRcRater().getIpAddress().equals( rcCheck.getIpAddress() ) )
                    return true;
            }
            else
                return true;
        }

        return false;
    }
    
    
    public boolean getNeedsBrowserPrecheck()
    {
        if( adminOverride )
            return false;
        
        if( recDevs>=0 )
            return false;
        
        if( rcCheck==null || refUserType==null )
            return false;
        
        // Media-based comments
        if( rcCheck.getRcAvType().getAnyMedia() )
            return true;
        
        // candidate photos
        if( refUserType.getIsCandidate() && rcCheck.getRcCandidatePhotoCaptureType().getRequiresAnyPhotoCapture() )
            return true;
        
        // rater photos
        if( refUserType.getIsRater() && rcCheck.getRcRaterPhotoCaptureType().getRequiresAnyPhotoCapture() )
        {
            if( rcCheck.getRcRaterPhotoCaptureType().getSameIpOnly() )
            {                        
                if( rcCheck.getIpAddress()==null || !rcCheck.getIpAddress().isBlank() || 
                    rcCheck.getRcRater().getIpAddress()==null || rcCheck.getRcRater().getIpAddress().isBlank() )
                    return false;

                if( rcCheck.getIpAddress()!=null && !rcCheck.getIpAddress().isBlank() && 
                    rcCheck.getRcRater().getIpAddress()!=null && 
                    rcCheck.getRcRater().getIpAddress().equals( rcCheck.getIpAddress() ) )
                    return true;
            }
            else
                return true;
        }
        
        return false;
    }
    
    public boolean getAudioVideoCommentsOk()
    {
        // LogService.logIt( "RefBean.getAudioVideoCommentsOk() MedRecApi=" + medRecApi + ", avCommentTypeId=" + rcCheck.getAvCommentsTypeId() );
        if( rcCheck==null || getHasGetUserMedia()<1 ) // || !isMedRecApi() )
            return false;
        
        return rcCheck.getAvCommentsTypeId()>0;
    }
    
    public boolean getVideoCommentsOk()
    {
        // LogService.logIt( "RefBean.getAudioVideoCommentsOk() MedRecApi=" + medRecApi + ", avCommentTypeId=" + rcCheck.getAvCommentsTypeId() );
        return getAudioVideoCommentsOk() && !getAudioCommentsOnly() && isMedRecApi();
    }
        
    
    
    public boolean getAudioCommentsOnly()
    {
        if( rcCheck==null )
            return false;
        return rcCheck.getAvCommentsTypeId()==1;
    }
    
    public String getStartUrl()
    {
        if( rcCheck!=null )
            return RuntimeConstants.getStringValue("baseprotocol") + "://" + RuntimeConstants.getStringValue("basedomain") + "/tr/rce/" + (rcCheck.getRcRater()==null ? rcCheck.getCandidateAccessCode() : rcCheck.getRcRater().getRaterAccessCode());

        return RuntimeConstants.getStringValue("baseprotocol") + "://" + RuntimeConstants.getStringValue("basedomain") + "/tr/index.xhtml";
    }
    
    public String getErrorReturnUrl() {
        return errorReturnUrl;
    }

    public void setErrorReturnUrl(String errorReturnUrl) {
        this.errorReturnUrl = errorReturnUrl;
    }

    public boolean isErrorAutoForward() {
        return errorAutoForward;
    }

    public void setErrorAutoForward(boolean errorAutoForward) {
        this.errorAutoForward = errorAutoForward;
    }

    public RcCheck getRcCheck() {
        return rcCheck;
    }

    public void setRcCheck(RcCheck rcCheck) {
        this.rcCheck = rcCheck;
    }

    public RefPageType getRefPageType() {
        
        if( refPageType==null )
            refPageType = RefPageType.ENTRY;
        
        return refPageType;
    }

    public void setRefPageType(RefPageType refPageType) {
        this.refPageType = refPageType;
        this.activeRefPageTypeIdX = refPageType==null ? "0" : Integer.toString(refPageType.getRefPageTypeId());
    }

    public User getRefUser()
    {
        if( refUserType==null || refUserType.getIsCandidate() )
            return rcCheck==null ? null : rcCheck.getUser();
        
        else if ( rcCheck!=null && rcCheck.getRcRater()!=null )
            return rcCheck.getRcRater().getUser();
        
        return null;
    }
    
    public RefUserType getRefUserType() {
        return refUserType;
    }

    public void setRefUserType(RefUserType refUserType) {
        this.refUserType = refUserType;
    }

    public String getActiveAccessCodeX() {
        return activeAccessCodeX;
    }

    public void setActiveAccessCodeX(String activeAccessCodeX) {
        this.activeAccessCodeX = activeAccessCodeX;
    }

    public String getActiveRefPageTypeIdX() {
        return activeRefPageTypeIdX;
    }

    public void setActiveRefPageTypeIdX(String activeRefPageTypeIdX) {
        this.activeRefPageTypeIdX = activeRefPageTypeIdX;
    }



    public User getTgtUser() {
        return tgtUser;
    }

    public void setTgtUser(User tgtUser) {
        this.tgtUser = tgtUser;
    }

    public int getRecDevs() {
        return recDevs;
    }

    public void setRecDevs(int recDevs) {
        this.recDevs = recDevs;
    }

    public boolean isMedRecApi() {
        
        //if( 1==1 )
        //    return false;
        return medRecApi;
    }

    public void setMedRecApi(boolean medRecApi) {
        this.medRecApi = medRecApi;
    }

    public int getHasGetUserMedia() {
        return hasGetUserMedia;
    }

    public void setHasGetUserMedia(int hasGetUserMedia) {
        this.hasGetUserMedia = hasGetUserMedia;
    }

    public boolean getAdminOverride() {
        return adminOverride;
    }

    public void setAdminOverride(boolean adminOverride) {
        this.adminOverride = adminOverride;
    }

    public boolean getAccessibleActive() {
        return accessibleActive;
    }

    public void setAccessibleActive(boolean accessibleActive) {
        this.accessibleActive = accessibleActive;
    }


    
    
}

