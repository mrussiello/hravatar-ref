package com.tm2ref.entity.user;



import com.tm2ref.service.LogService;
import com.tm2ref.user.ResumeEducation;
import com.tm2ref.user.ResumeExperience;
import com.tm2ref.util.JsonUtils;
import com.tm2ref.util.StringUtils;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import java.io.Serializable;

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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Entity
@Table( name = "xresume" )
@NamedQueries( {
        @NamedQuery( name = "Resume.findByResumeId", query = "SELECT o FROM Resume AS o WHERE o.resumeId=:resumeId"),
        @NamedQuery( name = "Resume.findByUserId", query = "SELECT o FROM Resume AS o WHERE o.userId=:userId")
} )
public class Resume implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "resumeid" )
    private int resumeId;

    @Column( name = "userid" )
    private long userId;

    @Column( name = "orgid" )
    private int orgId;

    /*
     0 = active
     99 = archived
    */
    @Column( name = "resumestatustypeid" )
    private int resumeStatusTypeId;

    @Column( name = "needsparse" )
    private int needsParse;
    
    @Column( name = "localestr" )
    private String localeStr;

    /*
    {
      "summary":"",
      "objective":"",
      "education":[ {} ],
      "experience":[ {} ],
      "other_qualifications":[ "" ]
    }
    */
    @Column( name = "jsonstr" )
    private String jsonStr;

    @Column( name = "uploadedtext" )
    private String uploadedText;

    @Column( name = "uploadfilename" )
    private String uploadFilename;

    @Column( name = "plaintext" )
    private String plainText;

    @Column( name = "lastaicallhistoryid" )
    private long lastAiCallHistoryId;


    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "createdate" )
    private Date createDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastupdate" )
    private Date lastUpdate;


    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastparsedate" )
    private Date lastParseDate;

    @Temporal( TemporalType.TIMESTAMP )
    @Column( name = "lastinputdate" )
    private Date lastInputDate;

    @Transient
    String summary;

    @Transient
    String objective;

    @Transient
    List<ResumeEducation> education;

    @Transient
    List<ResumeExperience> experience;

    @Transient
    List<String> otherQuals;



    public void clearForNewParseResults()
    {
        summary=null;
        objective=null;
        education=null;
        experience=null;
        otherQuals=null;
    }

    public boolean getHasAnyDataForAiParseCall()
    {
        return (getUploadedText()!=null && !getUploadedText().isBlank()); // || (this.getPlainText()!=null && !getPlainText().isBlank());
    }

    public boolean getHasAnyFormData()
    {
        return (summary!=null && !summary.isBlank()) ||
               (objective!=null && !objective.isBlank()) ||
               (education!=null && !education.isEmpty()) ||
               (experience!=null && !experience.isEmpty()) ||
               (otherQuals!=null && !otherQuals.isEmpty());
    }


    public void sanitizeUserInput()
    {

    }

    public void packJsonStr() throws Exception
    {
        if( !getHasAnyFormData() )
        {
            jsonStr = null;
            return;
        }

        boolean found = false;
        try
        {
            JsonObjectBuilder topJob = Json.createObjectBuilder();

            if( summary!=null && !summary.isBlank() )
            {
                topJob.add( "summary", summary );
                found=true;
            }

            if( objective!=null && !objective.isBlank() )
            {
                topJob.add( "objective", objective );
                found=true;
            }

            if( education!=null && !education.isEmpty())
            {
                JsonArrayBuilder jab = Json.createArrayBuilder();
                for( ResumeEducation ed : education )
                {
                    jab.add( ed.getJsonObjectBuilder() );
                }
                topJob.add( "education", jab );
                found=true;
            }

            if( experience!=null && !experience.isEmpty() )
            {
                JsonArrayBuilder jab = Json.createArrayBuilder();
                for( ResumeExperience ed : experience )
                {
                    jab.add( ed.getJsonObjectBuilder() );
                }
                topJob.add( "experience", jab );
                found=true;
            }

            if( otherQuals!=null && !otherQuals.isEmpty() )
            {
                JsonArrayBuilder jab = Json.createArrayBuilder();
                for( String s : otherQuals )
                {
                    if( s!=null && !s.isBlank() )
                        jab.add(s);
                }
                topJob.add("other_qualifications", jab );
                found=true;
            }

            if( found )
                jsonStr = JsonUtils.convertJsonObjectToString(topJob.build() );
            else
                jsonStr = null;
        }
        catch( Exception e )
        {
            LogService.logIt( e, "Resume.packJsonStr() " + toString() );
            throw e;
        }
    }

    public synchronized void parseJsonStr() throws Exception
    {
        // do not overwrite.
        if( summary!=null && !summary.isBlank() )
            return;
        if( objective!=null && !objective.isBlank() )
            return;
        if( education!=null && !education.isEmpty() )
            return;
        if( experience!=null && !experience.isEmpty() )
            return;
        if( otherQuals!=null && !otherQuals.isEmpty() )
            return;

        if( jsonStr==null || jsonStr.isBlank() )
        {
            this.summary="";
            this.objective="";
            this.education=null;
            this.experience=null;
            this.otherQuals=null;
            return;
        }

        try
        {
            JsonObject  jo = JsonUtils.convertJsonStringToObject(jsonStr );

            if( jo==null )
                return;

            if( jo.containsKey("summary") && !jo.isNull("summary"))
                summary = JsonUtils.getStringFmJson(jo, "summary");

            if( jo.containsKey("objective") && !jo.isNull("objective"))
                objective = JsonUtils.getStringFmJson(jo, "objective");

            if( jo.containsKey("education") && !jo.isNull("education"))
            {
                education = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("education");
                for( JsonObject eo : ja.getValuesAs(JsonObject.class))
                {
                    education.add( new ResumeEducation(eo ) );
                }
            }
            if( jo.containsKey("experience") && !jo.isNull("experience"))
            {
                experience = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("experience");
                for( JsonObject eo : ja.getValuesAs(JsonObject.class))
                {
                    experience.add( new ResumeExperience(eo ) );
                }
            }
            if( jo.containsKey("other_qualifications") && !jo.isNull("other_qualifications"))
            {
                otherQuals = new ArrayList<>();
                JsonArray ja = jo.getJsonArray("other_qualifications");
                for( String s : ja.getValuesAs(JsonString::getString))
                {
                    if( s!=null && !s.isBlank() )
                        otherQuals.add(s);
                }
            }
        }
        catch( Exception e )
        {
            LogService.logIt( e, "Resume.parseJsonStr() " + toString() );
            throw e;
        }
    }


    public synchronized JsonObject getJsonObject() throws Exception
    {
        if( jsonStr==null || jsonStr.isBlank() )
            return null;
        return JsonUtils.convertJsonStringToObject(jsonStr );
    }



    public synchronized void parseJsonStrNoErrors()
    {
        try
        {
            parseJsonStr();
        }
        catch( Exception e )
        {
            LogService.logIt(e, "Resume.parseJsonStrNoErrors() " + toString() );
        }
    }



    public int getResumeId() {
        return resumeId;
    }

    public void setResumeId(int resumeId) {
        this.resumeId = resumeId;
    }

    public int getResumeStatusTypeId() {
        return resumeStatusTypeId;
    }

    public void setResumeStatusTypeId(int resumeStatusTypeId) {
        this.resumeStatusTypeId = resumeStatusTypeId;
    }

    public String getLocaleStr() {
        return localeStr;
    }

    public void setLocaleStr(String localeStr) {
        this.localeStr = localeStr;
    }


    public String getSummaryXhtml()
    {
        return StringUtils.replaceStandardEntities(getSummary());
    }

    
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUploadedText() {
        return uploadedText;
    }

    public void setUploadedText(String uploadedText) {
        this.uploadedText = uploadedText;
    }

    public String getPlainText() {
        return plainText;
    }

    public void setPlainText(String plainText) {
        this.plainText = plainText;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
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

    public String getObjective() {

        if( objective==null && this.jsonStr!=null && !this.jsonStr.isBlank() )
            parseJsonStrNoErrors();

        return objective;
    }
    
    public String getObjectiveXhtml()
    {
        return StringUtils.replaceStandardEntities(getObjective());
    }
    
    

    public void setObjective(String objective) {
        this.objective = objective;
    }

    public List<ResumeEducation> getEducation() {
        if( education==null && this.jsonStr!=null && !this.jsonStr.isBlank() )
            parseJsonStrNoErrors();

        //if( education==null )
        //    education = new ArrayList<>();

        return education;
    }

    public void setEducation(List<ResumeEducation> edl) {
        this.education = edl;
    }

    public List<ResumeExperience> getExperience() {
        if( experience==null && this.jsonStr!=null && !this.jsonStr.isBlank() )
            parseJsonStrNoErrors();

        //if( experience==null )
        //     experience = new ArrayList<>();

        return experience;
    }

    public void setExperience(List<ResumeExperience> experience) {
        this.experience = experience;
    }

    public List<String> getOtherQuals() {
        if( otherQuals==null && this.jsonStr!=null && !this.jsonStr.isBlank() )
            parseJsonStrNoErrors();

        //if( otherQuals==null )
        //    otherQuals = new ArrayList<>();

        return otherQuals;
    }

    public void setOtherQuals(List<String> otherQuals) {
        this.otherQuals = otherQuals;
    }

    public String getJsonStr() {
        return jsonStr;
    }

    public void setJsonStr(String jsonStr) {
        this.jsonStr = jsonStr;
    }

    public Date getLastParseDate() {
        return lastParseDate;
    }

    public void setLastParseDate(Date lastParseDate) {
        this.lastParseDate = lastParseDate;
    }

    public Date getLastInputDate() {
        return lastInputDate;
    }

    public void setLastInputDate(Date lastInputDate) {
        this.lastInputDate = lastInputDate;
    }

    public long getLastAiCallHistoryId() {
        return lastAiCallHistoryId;
    }

    public void setLastAiCallHistoryId(long lastAiCallHistoryId) {
        this.lastAiCallHistoryId = lastAiCallHistoryId;
    }

    public String getUploadFilename() {
        return uploadFilename;
    }

    public void setUploadFilename(String uploadFilename) {
        this.uploadFilename = uploadFilename;
    }

    public int getNeedsParse()
    {
        return needsParse;
    }

    public void setNeedsParse(int needsParse)
    {
        this.needsParse = needsParse;
    }
    
}
