package com.tm2ref.entity.ref;


import java.io.Serializable;
import java.util.Date;
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
@Table( name = "rcorgprefs" )
@NamedQueries({
        @NamedQuery( name = "RcOrgPrefs.findByOrgId", query = "SELECT o FROM RcOrgPrefs AS o WHERE o.orgId=:orgId" )    
})
public class RcOrgPrefs implements Serializable, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="rcorgprefsid")
    private int rcOrgPrefsId;

    @Column(name="orgid")
    private int orgId;

    @Column(name="corpid")
    private int corpId;

    @Column(name="avcommentstypeid")
    private int avCommentsTypeId;
    
    @Column(name="reportidprehire")
    private int reportIdPrehire;

    @Column(name="reportidemployee")
    private int reportIdEmployee;
    
    @Column(name="reportidemployeefbk")
    private int reportIdEmployeeFbk;
        
    @Column(name="remindertypeid")
    private int reminderTypeId = 3;

    @Column(name="showjobtitle")
    private int showJobTitle = 1;

    @Column( name = "showcompanyname" )
    private int showCompanyName = 1;
    
    @Column(name="distributiontypeid")
    private int distributionTypeId;
    
    @Column(name="defaultrcchecktypeid")
    private int defaultRcCheckTypeId=0;
    
    @Column(name="followupok")
    private int followupOk=1;

    @Column(name="minsupervisors")
    private int minSupervisors = 1;

    
    @Column(name="minraters")
    private int minRaters = 2;

    @Column(name="maxraters")
    private int maxRaters = 8;
    
    /**
     * This is the number of days after expiration date that an incomplete rater can re-enter to finish their ratings.
     * The default is 10 days. 
     */
    @Column(name="ratergraceperiod")
    private int raterGracePeriod=10;

    

    @Column(name="enforceraterlimits")
    private int enforceRaterLimits;

    @Column(name="collectcandidateratings")
    private int collectCandidateRatings;

    @Column(name="candidatecannotaddraters")
    private int candidateCannotAddRaters;

    @Column(name="invitationsubj")
    private String invitationSubj;

    @Column(name="invitationsubjrater")
    private String invitationSubjRater;

    @Column(name="invitation")
    private String invitation;

    @Column(name="invitationrater")
    private String invitationRater;

    @Column(name="otherrolename")
    private String otherRoleName;

    @Column(name="otherrolename2")
    private String otherRoleName2;

    @Column(name="otherrolename3")
    private String otherRoleName3;

    @Column(name="candidatephotocapturetypeid")
    private int candidatePhotoCaptureTypeId;
            
    @Column(name="raterphotocapturetypeid")
    private int raterPhotoCaptureTypeId;
    
    
     /**
     * 0=no
     * 1=audio
     * 2=video
     */
    @Column(name="audiovideook")
    private int audioVideoOk=0;
   
    @Column(name="defaultrcscriptid")
    private int defaultRcScriptId=0;
   
    /**
     * This is the number of competencies included in the top/bottom competencies lists in reports.
     */
    @Column(name="topbottomcount")
    private int topBottomCount=3;
   
    /*
     This is the category of ratings used to select the top/bottom.
     0 = others (all except the candidate)
     1 = the candidate.
     10=all ratings
    */
    @Column(name="topbottomsrctypeid")
    private int topBottomSrcTypeId=3;
   
    
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lastupdate")
    private Date lastUpdate;
   
        
    
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    @Override
    public String toString() {
        return "RcOrgPrefs{" + "rcOrgPrefsId=" + rcOrgPrefsId + ", orgId=" + orgId + '}';
    }

    public String[] getOtherRoleTypeNames( RcSuborgPrefs rsop )
    {
        
        if( rsop==null || !rsop.hasRoleNameData() )
            return new String[] {  this.otherRoleName==null || otherRoleName.isBlank() ? null : otherRoleName, 
                                    this.otherRoleName2==null || otherRoleName2.isBlank() ? null : otherRoleName2,
                                    this.otherRoleName3==null || otherRoleName3.isBlank() ? null : otherRoleName3 };
        
        // use rsop values if present. Otherwise use rcop values
        String[] subs = rsop.getOtherRoleTypeNames();
        
        if( subs[0]==null || subs[0].isBlank() )
            subs[0] = this.otherRoleName==null || otherRoleName.isBlank() ? null : otherRoleName;
        
        if( subs[1]==null || subs[1].isBlank() )
            subs[1] = this.otherRoleName2==null || otherRoleName2.isBlank() ? null : otherRoleName2;
        
        if( subs[2]==null || subs[2].isBlank() )
            subs[2] = this.otherRoleName3==null || otherRoleName3.isBlank() ? null : otherRoleName3;
                
        return subs;
    }
    
    
    
    public int getRcOrgPrefsId() {
        return rcOrgPrefsId;
    }

    public void setRcOrgPrefsId(int rcOrgPrefsId) {
        this.rcOrgPrefsId = rcOrgPrefsId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public int getShowJobTitle() {
        return showJobTitle;
    }

    public void setShowJobTitle(int showJobTitle) {
        this.showJobTitle = showJobTitle;
    }

    public boolean getShowJobTitleB() {
        return showJobTitle==1;
    }

    public void setShowJobTitleB(boolean b) {
        this.showJobTitle = b ? 1 : 0;
    }

    public int getShowCompanyName() {
        return showCompanyName;
    }

    public void setShowCompanyName(int showCompanyName) {
        this.showCompanyName = showCompanyName;
    }

    public boolean getShowCompanyNameB() {
        return showCompanyName==1;
    }

    public void setShowCompanyNameB(boolean b) {
        this.showCompanyName = b ? 1 : 0;
    }

    public int getCorpId() {
        return corpId;
    }

    public void setCorpId(int corpId) {
        this.corpId = corpId;
    }

    public int getFollowupOk() {
        return followupOk;
    }

    public void setFollowupOk(int followupOk) {
        this.followupOk = followupOk;
    }

    public boolean getFollowupOkB() {
        return followupOk==1;
    }

    public int getReminderTypeId() {
        return reminderTypeId;
    }

    public void setReminderTypeId(int reminderTypeId) {
        this.reminderTypeId = reminderTypeId;
    }

    public int getDistributionTypeId() {
        return distributionTypeId;
    }

    public void setDistributionTypeId(int distributionTypeId) {
        this.distributionTypeId = distributionTypeId;
    }

    public int getDefaultRcCheckTypeId() {
        return defaultRcCheckTypeId;
    }

    public void setDefaultRcCheckTypeId(int defaultRcCheckTypeId) {
        this.defaultRcCheckTypeId = defaultRcCheckTypeId;
    }

    public int getMinSupervisors() {
        return minSupervisors;
    }

    public void setMinSupervisors(int minSupervisors) {
        this.minSupervisors = minSupervisors;
    }

    public int getMinRaters() {
        return minRaters;
    }

    public void setMinRaters(int minRaters) {
        this.minRaters = minRaters;
    }

    public int getMaxRaters() {
        return maxRaters;
    }

    public void setMaxRaters(int maxRaters) {
        this.maxRaters = maxRaters;
    }

    public int getEnforceRaterLimits() {
        return enforceRaterLimits;
    }

    public void setEnforceRaterLimits(int enforceRaterLimits) {
        this.enforceRaterLimits = enforceRaterLimits;
    }

    public int getDefaultRcScriptId() {
        return defaultRcScriptId;
    }

    public void setDefaultRcScriptId(int defaultRcScriptId) {
        this.defaultRcScriptId = defaultRcScriptId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public int getCollectCandidateRatings() {
        return collectCandidateRatings;
    }

    public void setCollectCandidateRatings(int collectCandidateRatings) {
        this.collectCandidateRatings = collectCandidateRatings;
    }

    public int getCandidateCannotAddRaters() {
        return candidateCannotAddRaters;
    }

    public void setCandidateCannotAddRaters(int candidateCannotAddRaters) {
        this.candidateCannotAddRaters = candidateCannotAddRaters;
    }

    public int getReportIdPrehire() {
        return reportIdPrehire;
    }

    public void setReportIdPrehire(int reportIdPrehire) {
        this.reportIdPrehire = reportIdPrehire;
    }

    public int getReportIdEmployee() {
        return reportIdEmployee;
    }

    public void setReportIdEmployee(int reportIdEmployee) {
        this.reportIdEmployee = reportIdEmployee;
    }

    public int getReportIdEmployeeFbk() {
        return reportIdEmployeeFbk;
    }

    public void setReportIdEmployeeFbk(int reportIdEmployeeFbk) {
        this.reportIdEmployeeFbk = reportIdEmployeeFbk;
    }

    public String getInvitationSubj() {
        return invitationSubj;
    }

    public void setInvitationSubj(String invitationSubj) {
        this.invitationSubj = invitationSubj;
    }

    public String getInvitationSubjRater() {
        return invitationSubjRater;
    }

    public void setInvitationSubjRater(String invitationSubjRater) {
        this.invitationSubjRater = invitationSubjRater;
    }

    public String getInvitation() {
        return invitation;
    }

    public void setInvitation(String invitation) {
        this.invitation = invitation;
    }

    public String getInvitationRater() {
        return invitationRater;
    }

    public void setInvitationRater(String invitationRater) {
        this.invitationRater = invitationRater;
    }

    public String getOtherRoleName() {
        return otherRoleName;
    }

    public void setOtherRoleName(String otherRoleName) {
        this.otherRoleName = otherRoleName;
    }

    public String getOtherRoleName2() {
        return otherRoleName2;
    }

    public void setOtherRoleName2(String otherRoleName2) {
        this.otherRoleName2 = otherRoleName2;
    }

    public String getOtherRoleName3() {
        return otherRoleName3;
    }

    public void setOtherRoleName3(String otherRoleName3) {
        this.otherRoleName3 = otherRoleName3;
    }

    public int getRaterGracePeriod() {
        return raterGracePeriod;
    }

    public void setRaterGracePeriod(int raterGracePeriod) {
        this.raterGracePeriod = raterGracePeriod;
    }

    public int getCandidatePhotoCaptureTypeId() {
        return candidatePhotoCaptureTypeId;
    }

    public void setCandidatePhotoCaptureTypeId(int candidatePhotoCaptureTypeId) {
        this.candidatePhotoCaptureTypeId = candidatePhotoCaptureTypeId;
    }

    public int getRaterPhotoCaptureTypeId() {
        return raterPhotoCaptureTypeId;
    }

    public void setRaterPhotoCaptureTypeId(int raterPhotoCaptureTypeId) {
        this.raterPhotoCaptureTypeId = raterPhotoCaptureTypeId;
    }

    public int getAvCommentsTypeId() {
        return avCommentsTypeId;
    }

    public void setAvCommentsTypeId(int avCommentsTypeId) {
        this.avCommentsTypeId = avCommentsTypeId;
    }

    public int getTopBottomCount() {
        return topBottomCount;
    }

    public void setTopBottomCount(int topBottomCount) {
        this.topBottomCount = topBottomCount;
    }

    public int getTopBottomSrcTypeId() {
        return topBottomSrcTypeId;
    }

    public void setTopBottomSrcTypeId(int topBottomSrcTypeId) {
        this.topBottomSrcTypeId = topBottomSrcTypeId;
    }


    

    
}
