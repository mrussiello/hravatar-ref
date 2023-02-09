package com.tm2ref.entity.user;

import com.tm2ref.user.UserActionType;
import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;



@Entity
@Table( name="useraction" )
public class UserAction implements Serializable
{
    @Transient
    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="useractionid")
    private long userActionId;

    @Column(name="userid")
    private long userId;

    @Column(name="orgid")
    private int orgId = 0;

    @Column(name="identifier")
    private String identifier;

    @Column(name="uid")
    private String uid;
    
    
    @Column(name="intparam1")
    private int intParam1;

    @Column(name="intparam2")
    private int intParam2;

    @Column(name="intparam3")
    private int intParam3;

    @Column(name="useractiontypeid")
    private int userActionTypeId;

    @Column(name="longparam1")
    private long longParam1;

    @Column(name="longparam2")
    private long longParam2;

    @Column(name="longparam3")
    private long longParam3;

    @Column(name="longparam4")
    private long longParam4;
        
    @Column(name="ipaddress")
    private String ipAddress;

    @Column(name="ipcountry")
    private String ipCountry;

    @Column(name="ipstate")
    private String ipState;


    @Column(name="ipcity")
    private String ipCity;

    @Column(name="strparam1")
    private String strParam1;

    @Column(name="strparam2")
    private String strParam2;

    @Column(name="strparam3")
    private String strParam3;

    @Column(name="strparam4")
    private String strParam4;

    @Column(name="strparam5")
    private String strParam5;

    @Column(name="strparam6")
    private String strParam6;

    @Column(name="textparam1")
    private String textParam1;
    
    
    @Column(name="referringpage")
    private String referringPage;

    @Column(name="useragent")
    private String userAgent;

    @Column(name="bot")
    private int bot;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="createdate")
    private Date createDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="eventdate")
    private Date eventDate;

    @Transient
    private User user;

    public UserActionType getUserActionType()
    {
        return UserActionType.getValue( userActionTypeId );
    }

    @Override
    public String toString() {
        return "UserAction{" + "userActionId=" + userActionId + ", userId=" + userId + ", identifier=" + identifier + ", intParam1=" + intParam1 + ", intParam2=" + intParam2 + ", userActionTypeId=" + userActionTypeId + ", longParam1=" + longParam1 + ", longParam2=" + longParam2 + ", strParam1=" + strParam1 + ", strParam2=" + strParam2 + ", createDate=" + createDate + '}';
    }

    public long getUserActionId() {
        return userActionId;
    }

    public void setUserActionId(long userActionId) {
        this.userActionId = userActionId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public int getIntParam2() {
        return intParam2;
    }

    public void setIntParam2(int intParam2) {
        this.intParam2 = intParam2;
    }

    public int getIntParam3() {
        return intParam3;
    }

    public void setIntParam3(int intParam3) {
        this.intParam3 = intParam3;
    }

    public int getUserActionTypeId() {
        return userActionTypeId;
    }

    public void setUserActionTypeId(int userActionTypeId) {
        this.userActionTypeId = userActionTypeId;
    }

    public long getLongParam1() {
        return longParam1;
    }

    public void setLongParam1(long longParam1) {
        this.longParam1 = longParam1;
    }

    public long getLongParam2() {
        return longParam2;
    }

    public void setLongParam2(long longParam2) {
        this.longParam2 = longParam2;
    }

    public String getStrParam1() {
        return strParam1;
    }

    public void setStrParam1(String strParam1) {
        this.strParam1 = strParam1;
    }

    public String getStrParam2() {
        return strParam2;
    }

    public void setStrParam2(String strParam2) {
        this.strParam2 = strParam2;
    }

    public String getStrParam3() {
        return strParam3;
    }

    public void setStrParam3(String strParam3) {
        this.strParam3 = strParam3;
    }

    public String getStrParam4() {
        return strParam4;
    }

    public void setStrParam4(String strParam4) {
        this.strParam4 = strParam4;
    }

    public String getStrParam5() {
        return strParam5;
    }

    public void setStrParam5(String strParam5) {
        this.strParam5 = strParam5;
    }

    public String getStrParam6() {
        return strParam6;
    }

    public void setStrParam6(String strParam6) {
        this.strParam6 = strParam6;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getOrgId() {
        return orgId;
    }

    public void setOrgId(int orgId) {
        this.orgId = orgId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpCountry() {
        return ipCountry;
    }

    public void setIpCountry(String ipCountry) {
        this.ipCountry = ipCountry;
    }

    public String getIpCity() {
        return ipCity;
    }

    public void setIpCity(String ipCity) {
        this.ipCity = ipCity;
    }

    public String getIpState() {
        return ipState;
    }

    public void setIpState(String ipState) {
        this.ipState = ipState;
    }

    public String getReferringPage() {
        return referringPage;
    }

    public void setReferringPage(String referringPage) {
        this.referringPage = referringPage;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getBot() {
        return bot;
    }

    public void setBot(int bot) {
        this.bot = bot;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getLongParam3() {
        return longParam3;
    }

    public void setLongParam3(long longParam3) {
        this.longParam3 = longParam3;
    }

    public long getLongParam4() {
        return longParam4;
    }

    public void setLongParam4(long longParam4) {
        this.longParam4 = longParam4;
    }

    public String getTextParam1() {
        return textParam1;
    }

    public void setTextParam1(String textParam1) {
        this.textParam1 = textParam1;
    }

    public Date getEventDate() {
        return eventDate;
    }

    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }


}
