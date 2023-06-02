package com.tm2ref.entity.ref;


import com.tm2ref.entity.file.RcUploadedUserFile;
import com.tm2ref.entity.user.User;
import com.tm2ref.file.UploadedUserFileType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.proctor.ProctorUtils;
import com.tm2ref.ref.RcContactMethodType;
import com.tm2ref.ref.RcContactPermissionType;
import com.tm2ref.ref.RcRaterRoleType;
import com.tm2ref.ref.RcRaterSourceType;
import com.tm2ref.ref.RcRaterStatusType;
import com.tm2ref.ref.RcRaterType;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;


@Entity
@Table( name = "rcrater" )
@NamedQueries({
        @NamedQuery( name = "RcRater.findByRcRaterId", query = "SELECT o FROM RcRater AS o WHERE o.rcRaterId=:rcRaterId" ),
        @NamedQuery( name = "RcRater.findByRcCheckId", query = "SELECT o FROM RcRater AS o WHERE o.rcCheckId=:rcCheckId" ),
        @NamedQuery( name = "RcRater.findByRaterAccessCode", query = "SELECT o FROM RcRater AS o WHERE o.raterAccessCode=:raterAccessCode" )
})
public class RcRater implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcraterid")
    private long rcRaterId;

    @Column(name="rccheckid")
    private long rcCheckId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="userid")
    private long userId;

    @Column(name="sourceuserid")
    private long sourceUserId;

    @Column(name="rateraccesscode")
    private String raterAccessCode;

    @Column(name="rcratertypeid")
    private int rcRaterTypeId;

    @Column(name="rcratersourcetypeid")
    private int rcRaterSourceTypeId;


    @Column(name="rcraterstatustypeid")
    private int rcRaterStatusTypeId;

    @Column(name="rcraterroletypeid")
    private int rcRaterRoleTypeId;


    @Column(name="candidateroleresp")
    private String candidateRoleResp;

    @Column(name="companyname")
    private String companyName;

    @Column(name="percentcomplete")
    private float percentComplete;

    @Column(name="overallscore")
    private float overallScore;

    @Column(name="contactpermissiontypeid")
    private int contactPermissionTypeId;

    @Column(name="recruitingpermissiontypeid")
    private int recruitingPermissionTypeId;


    @Column(name="contactmethodtypeid")
    private int contactMethodTypeId;

    @Column(name="raterstarts")
    private int raterStarts;

    @Column(name="raterseconds")
    private int raterSeconds;

    @Column(name="raternosend")
    private int raterNoSend;



    @Column(name="ipaddress")
    private String ipAddress;

    @Column(name="ipcountry")
    private String ipCountry;

    @Column(name="ipstate")
    private String ipState;

    @Column(name="ipcity")
    private String ipCity;


    @Column(name="useragent")
    private String userAgent;

    @Column(name="note")
    private String note;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="releasedate")
    private Date releaseDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="startdate")
    private Date startDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="completedate")
    private Date completeDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastprogressmsgdate")
    private Date lastProgressMsgDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="senddate")
    private Date sendDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastreminderdate")
    private Date lastReminderDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="observationstartdate")
    private Date observationStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="observationenddate")
    private Date observationEndDate;

    @Transient
    private RcCheck rcCheck;

    @Transient
    private List<RcRating> rcRatingList;

    @Transient
    private User user;

    @Transient
    private Locale locale;

    //@Transient
    //private RcRaterSourceType rcRaterSourceType;

    @Transient
    private String tempEmail;

    @Transient
    private String tempMobile;

    @Transient
    private boolean needsResendEmail;

    @Transient
    private boolean needsResendMobile;

    @Transient
    private Date lastSecondsDate;

    @Transient
    private boolean inGracePeriod;

    @Transient
    private String anonymousName;

    @Transient
    private List<RcUploadedUserFile> fauxRcUploadedUserFileList;

    @Transient
    private List<RcUploadedUserFile> rcUploadedUserFileList;

    @Transient
    private List<RcReferral> rcReferralList;


    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcRater{" + "rcRaterId=" + rcRaterId + ", rcCheckId=" + rcCheckId + ", userId=" + userId + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (int) (this.rcRaterId ^ (this.rcRaterId >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final RcRater other = (RcRater) obj;
        if (this.rcRaterId != other.rcRaterId) {
            return false;
        }
        return true;
    }
    
    public List<RcReferral> getRcReferralListNoSame()
    {
        if( getRcReferralList()==null || getRcReferralList().isEmpty() )
            return this.getRcReferralList();
        
        List<RcReferral> out = new ArrayList<>();
        for( RcReferral r : this.rcReferralList )
        {
            if( r.getUserId()==this.userId )
                continue;
            out.add(r);
        }
        return out;
    }

    public String getUserLastnameOrAnonymousName()
    {
        if( anonymousName!=null && !anonymousName.isBlank() )
            return anonymousName;

        if( user!=null )
            return user.getLastName();

        return "";
    }

    public String getUserFullnameOrAnonymousName()
    {
        if( anonymousName!=null && !anonymousName.isBlank() )
            return anonymousName;

        if( user!=null )
            return user.getFullname();

        return "";
    }


    public String getRcRaterIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( rcRaterId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcRater.getRcRaterIdEncrypted() " + toString()  );
            return "";
        }
    }
    public String getUserIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( userId );
        }
        catch( Exception e )
        {
            LogService.logIt(e, "RcRater.getUserIdEncrypted() " + toString()  );
            return "";
        }
    }

    public boolean getHasIpLocationData()
    {
        return ipCountry!=null && !ipCountry.isBlank();
    }

    public String getIpLocationData()
    {
        if( !getHasIpLocationData() )
            return "";

        String out = ipCountry;
        if( ipState!=null && !ipState.isBlank() )
            out += ", " + ipState;
        if( ipCity!=null && !ipCity.isBlank() )
            out += ", " + ipCity;
        return out;
    }


    public void setRcUploadedUserFilesInRatings()
    {
        // LogService.logIt("RcRater.setRcUploadedUserFilesInRatings() rcUploadedUserFileList=" + (rcUploadedUserFileList==null ? "null" : rcUploadedUserFileList.size()) + ", rcRatingList=" + (rcRatingList==null ? "null" : rcRatingList.size()) );
        if( rcUploadedUserFileList==null || rcUploadedUserFileList.isEmpty() || rcRatingList==null )
            return;

        for( RcRating rtg : rcRatingList )
        {
            rtg.setRcUploadedUserFile( getRcUploadedUserFileForItemComment( rtg.getRcItemId() ) );
        }
    }

    public RcUploadedUserFile getRcUploadedUserFileForItemComment( int rcItemId  )
    {
        if( rcUploadedUserFileList==null || rcItemId<=0 )
            return null;

        for( RcUploadedUserFile u : this.rcUploadedUserFileList )
        {
            // LogService.logIt("RcRater.getRcUploadedUserFileForItemComment( itemId=" + rcItemId + ") " + u.toString() );
            if( u.getUploadedUserFileType().getIsRcComment() && u.getRcRaterId()==rcRaterId && u.getRcItemId()==rcItemId )
                return u;
        }

        return null;
    }


    public boolean getHasIdPhotos()
    {
        return hasPhotos( UploadedUserFileType.REF_CHECK_RATER_ID );
    }

    public boolean getHasPhotos()
    {
        return hasPhotos( UploadedUserFileType.REF_CHECK_RATER );
    }
    public boolean hasPhotos(UploadedUserFileType uuft )
    {

        if( fauxRcUploadedUserFileList!=null && !fauxRcUploadedUserFileList.isEmpty() )
        {
            for( RcUploadedUserFile u : fauxRcUploadedUserFileList )
            {
                if( u.getUploadedUserFileType().equals(uuft) && u.getHasValidThumbs() )
                    return true;
            }

            return false;
        }

        if( rcUploadedUserFileList==null || rcUploadedUserFileList.isEmpty() )
            return false;

        for( RcUploadedUserFile u : this.rcUploadedUserFileList )
        {
            if( u.getUploadedUserFileType().equals(uuft) && u.getHasValidThumbs() )
                return true;
        }
        return false;
    }

    public List<RcUploadedUserFile> getFauxRcUploadedUserFilePhotoList()
    {
        return getFauxRcUploadedUserFileList(UploadedUserFileType.REF_CHECK_RATER);
    }

    public List<RcUploadedUserFile> getFauxRcUploadedUserFilePhotoIdList()
    {
        return getFauxRcUploadedUserFileList(UploadedUserFileType.REF_CHECK_RATER_ID);
    }

    public List<RcUploadedUserFile> getFauxRcUploadedUserFileList( UploadedUserFileType uuft )
    {
        List<RcUploadedUserFile> out = new ArrayList<>();

        if( fauxRcUploadedUserFileList!=null  )
        {
            RcUploadedUserFile u;
            for( int i=fauxRcUploadedUserFileList.size()-1;i>=0;i-- )
            {
                u = fauxRcUploadedUserFileList.get(i);
                if( u.getUploadedUserFileType().equals( uuft ))
                    out.add(u);
            }
        }

        return out;
    }


    public RcUploadedUserFile getSinglePhotoFauxRcUploadedUserFile()
    {
        return getSinglePhotoFauxRcUploadedUserFile( UploadedUserFileType.REF_CHECK_RATER);
    }

    public RcUploadedUserFile getSingleIdPhotoFauxRcUploadedUserFile()
    {
        return getSinglePhotoFauxRcUploadedUserFile( UploadedUserFileType.REF_CHECK_RATER_ID);
    }

    public RcUploadedUserFile getSinglePhotoFauxRcUploadedUserFile( UploadedUserFileType uuft )
    {
        if( fauxRcUploadedUserFileList!=null  )
        {
            RcUploadedUserFile u;
            for( int i=fauxRcUploadedUserFileList.size()-1;i>=0;i-- )
            {
                u = fauxRcUploadedUserFileList.get(i);
                if( u.getUploadedUserFileType().equals( uuft ))
                    return u;
            }
        }

        if( rcUploadedUserFileList==null || rcUploadedUserFileList.isEmpty() )
            return null;

        for( RcUploadedUserFile u : rcUploadedUserFileList )
        {
            if( u.getHasValidThumbs() && u.getUploadedUserFileType().equals( uuft ) )
                return ProctorUtils.getSingleFauxRcUploadedUserFileForThumb( u );

            if( uuft.getIsRcRaterPhoto() && u.getUploadedUserFileType().getIsRcComment() && u.getHasValidThumbs() )
                return u;
        }
        return null;
    }






    public boolean getIsCandidateOrEmployee()
    {
        return getRcRaterType().getIsCandidateOrEmployee();
    }

    public RcRating getRcRating( int rcItemId )
    {
        if( rcItemId<=0 || this.rcRatingList==null || this.rcRatingList.isEmpty() )
            return null;

        for( RcRating r : rcRatingList )
        {
            if( r.getRcItemId()==rcItemId )
                return r;
        }
        return null;
    }

    public RcRaterType getRcRaterType()
    {
        return RcRaterType.getValue( this.rcRaterTypeId );
    }


    public RcRaterStatusType getRcRaterStatusType()
    {
        return RcRaterStatusType.getValue( rcRaterStatusTypeId );
    }

    public RcContactPermissionType getRcContactPermissionType()
    {
        return RcContactPermissionType.getValue( contactPermissionTypeId );
    }

    public RcContactMethodType getRcContactMethodType()
    {
        return RcContactMethodType.getValue( contactMethodTypeId );
    }

    public RcRaterRoleType getRcRaterRoleType()
    {
        return RcRaterRoleType.getValue( this.rcRaterRoleTypeId );
    }

    public String getRcRaterRoleTypeName()
    {
        return getRcRaterRoleType().getName(locale, rcCheck==null || rcCheck.getRcOrgPrefs()==null ? null : rcCheck.getRcOrgPrefs().getOtherRoleTypeNames(rcCheck.getRcSuborgPrefs()) );
    }

    public String getRcRaterStatusTypeName()
    {
        return getRcRaterStatusType().getName(locale);
    }

    public boolean getIsAccountUserSource()
    {
        return getRcRaterSourceType().getIsAccountUserOrUnknown();
    }

    public boolean getCandidateCanEdit()
    {
        if( !getRcRaterStatusType().getCandidateCanEdit() )
            return false;

        if( getRcRaterSourceType().getIsAccountUserOrUnknown() )
            return false;

        return true;
    }

    public String getPercentCompleteRounded()
    {
        return Integer.toString( (int) percentComplete );
    }

    public boolean getCandidateCanSend()
    {
        if( this.rcRaterId<=0 )
            return false;

        if( this.getIsCandidateOrEmployee() || raterNoSend==1 )
            return false;

        if( getRcRaterSourceType().getIsAccountUserOrUnknown() )
            return false;

        return !getRcRaterStatusType().getCompleteOrHigher();
    }

    public boolean getCandidateCanCancel()
    {
        if( getRcRaterSourceType().getIsAccountUserOrUnknown() )
            return false;

        return !getRcRaterStatusType().getSentOrHigher();
    }


    public String getRaterStartUrl()
    {
        return RuntimeConstants.getStringValue( "RefCheckRaterBaseUrl" ) + raterAccessCode;
    }


    public long getRcRaterId() {
        return rcRaterId;
    }

    public void setRcRaterId(long rcRaterId) {
        this.rcRaterId = rcRaterId;
    }

    public long getRcCheckId() {
        return rcCheckId;
    }

    public void setRcCheckId(long rcCheckId) {
        this.rcCheckId = rcCheckId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getRcRaterStatusTypeId() {
        return rcRaterStatusTypeId;
    }

    public void setRcRaterStatusTypeId(int rcRaterStatusTypeId) {
        this.rcRaterStatusTypeId = rcRaterStatusTypeId;
    }

    public float getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(float overallScore) {
        this.overallScore = overallScore;
    }

    public int getContactMethodTypeId() {
        return contactMethodTypeId;
    }

    public void setContactMethodTypeId(int contactMethodTypeId) {
        this.contactMethodTypeId = contactMethodTypeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getContactPermissionTypeId() {
        return contactPermissionTypeId;
    }

    public void setContactPermissionTypeId(int contactPermissionTypeId) {
        this.contactPermissionTypeId = contactPermissionTypeId;
    }

    public float getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(float percentComplete) {
        this.percentComplete = percentComplete;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public List<RcRating> getRcRatingList() {
        return rcRatingList;
    }

    public void setRcRatingList(List<RcRating> rcRatingList) {
        this.rcRatingList = rcRatingList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getRaterAccessCode() {
        return raterAccessCode;
    }

    public void setRaterAccessCode(String raterAccessCode) {
        this.raterAccessCode = raterAccessCode;
    }

    public int getRaterStarts() {
        return raterStarts;
    }

    public void setRaterStarts(int raterStarts) {
        this.raterStarts = raterStarts;
    }

    public int getRaterSeconds() {
        return raterSeconds;
    }

    public void setRaterSeconds(int raterSeconds) {
        this.raterSeconds = raterSeconds;
    }

    public long getSourceUserId() {
        return sourceUserId;
    }

    public void setSourceUserId(long sourceUserId) {
        this.sourceUserId = sourceUserId;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public int getRcRaterRoleTypeId() {
        return rcRaterRoleTypeId;
    }

    public void setRcRaterRoleTypeId(int rcRaterRoleTypeId) {
        this.rcRaterRoleTypeId = rcRaterRoleTypeId;
    }


    public RcRaterSourceType getRcRaterSourceType() {
        return RcRaterSourceType.getValue(this.rcRaterSourceTypeId);
    }

    //public void setRcRaterSourceType(RcRaterSourceType rcRaterSourceType) {
    //    this.rcRaterSourceType = rcRaterSourceType;
    //}

    public String getTempEmail() {
        return tempEmail;
    }

    public void setTempEmail(String tempEmail) {
        this.tempEmail = tempEmail;
    }

    public String getTempMobile() {
        return tempMobile;
    }

    public void setTempMobile(String tempMobile) {
        this.tempMobile = tempMobile;
    }

    public boolean getNeedsResendEmail() {
        return needsResendEmail;
    }

    public void setNeedsResendEmail(boolean needsResendEmail) {
        this.needsResendEmail = needsResendEmail;
    }

    public boolean getNeedsResendMobile() {
        return needsResendMobile;
    }

    public void setNeedsResendMobile(boolean needsResendMobile) {
        this.needsResendMobile = needsResendMobile;
    }

    public String getCandidateRoleResp() {
        return candidateRoleResp;
    }

    public void setCandidateRoleResp(String c) {

        if( c!=null && c.length()>1999 )
            c = StringUtils.truncateString( c, 1999 );

        this.candidateRoleResp = c;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String c) {
        
        c = StringUtils.capitalizeFirstChar(c);
                
        if( c!=null && c.length()>254) 
            c = c.substring(0, 254 );
        this.companyName = c;
    }

    public Date getLastProgressMsgDate() {
        return lastProgressMsgDate;
    }

    public void setLastProgressMsgDate(Date lastProgressMsgDate) {
        this.lastProgressMsgDate = lastProgressMsgDate;
    }

    public Date getLastReminderDate() {
        return lastReminderDate;
    }

    public void setLastReminderDate(Date lastReminderDate) {
        this.lastReminderDate = lastReminderDate;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    public Date getObservationStartDate() {
        return observationStartDate;
    }

    public void setObservationStartDate(Date observationStartDate) {
        this.observationStartDate = observationStartDate;
    }

    public Date getObservationEndDate() {
        return observationEndDate;
    }

    public void setObservationEndDate(Date observationEndDate) {
        this.observationEndDate = observationEndDate;
    }

    public int getRecruitingPermissionTypeId() {
        return recruitingPermissionTypeId;
    }

    public void setRecruitingPermissionTypeId(int recruitingPermissionTypeId) {
        this.recruitingPermissionTypeId = recruitingPermissionTypeId;
    }

    public Date getLastSecondsDate() {
        return lastSecondsDate;
    }

    public void setLastSecondsDate(Date lastSecondsDate) {
        this.lastSecondsDate = lastSecondsDate;
    }

    public int getRcRaterTypeId() {
        return rcRaterTypeId;
    }

    public void setRcRaterTypeId(int rcRaterTypeId) {
        this.rcRaterTypeId = rcRaterTypeId;
    }

    public RcCheck getRcCheck() {
        return rcCheck;
    }

    public void setRcCheck(RcCheck rcCheck) {
        this.rcCheck = rcCheck;
    }

    public boolean getInGracePeriod() {
        return inGracePeriod;
    }

    public void setInGracePeriod(boolean inGracePeriod) {
        this.inGracePeriod = inGracePeriod;
    }

    public String getAnonymousName() {
        return anonymousName;
    }

    public void setAnonymousName(String s) {
        this.anonymousName = s;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public String getIpState() {
        return ipState;
    }

    public void setIpState(String ipState) {
        this.ipState = ipState;
    }

    public String getIpCity() {
        return ipCity;
    }

    public void setIpCity(String ipCity) {
        this.ipCity = ipCity;
    }

    public List<RcUploadedUserFile> getFauxRcUploadedUserFileList() {
        return fauxRcUploadedUserFileList;
    }

    public void setFauxRcUploadedUserFileList(List<RcUploadedUserFile> fauxRcUploadedUserFileList) {
        this.fauxRcUploadedUserFileList = fauxRcUploadedUserFileList;
    }

    public List<RcUploadedUserFile> getRcUploadedUserFileList() {
        return rcUploadedUserFileList;
    }

    public void setRcUploadedUserFileList(List<RcUploadedUserFile> rcUploadedUserFileList) {
        this.rcUploadedUserFileList = rcUploadedUserFileList;
    }

    public int getRcRaterSourceTypeId() {
        return rcRaterSourceTypeId;
    }

    public void setRcRaterSourceTypeId(int rcRaterSourceTypeId) {
        this.rcRaterSourceTypeId = rcRaterSourceTypeId;
    }

    public List<RcReferral> getRcReferralList() {
        return rcReferralList;
    }

    public void setRcReferralList(List<RcReferral> rcReferralList) {
        this.rcReferralList = rcReferralList;
    }

      public int getRaterNoSend() {
        return raterNoSend;
    }

    public void setRaterNoSend(int raterNosend) {
        this.raterNoSend = raterNosend;
    }

    public boolean getRaterNoSendB() {
        return raterNoSend==1;
    }

    public void setRaterNoSendB(boolean b) {
        this.raterNoSend = b ? 1 : 0;
    }




}
