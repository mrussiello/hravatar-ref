package com.tm2ref.entity.corp;


import com.tm2ref.corp.CorpType;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.ref.RefUserType;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.StringUtils;
import java.io.Serializable;

import java.util.Locale;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;


@Entity
@Table( name = "corp" )
@NamedQueries({
	@NamedQuery( name = "Corp.findByCorpId", query = "SELECT o FROM Corp AS o WHERE o.corpId=:corpId" )
})
public class Corp implements Serializable, Comparable<Corp>, Cloneable
{
    @Transient
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Column( name = "corpid" )
    private int corpId;

    @Column( name = "name" )
    private String name = "";

    @Column( name = "adminuserid" )
    private long adminUserId = 0;


    @Column( name = "localestr" )
    private String localeStr = "en_US";

    @Column( name = "returnurl" )
    private String defaultReturnUrl;
    
    @Column( name = "usedirectexit" )
    private int useDirectExit;
    
    @Column( name = "corptypeid" )
    private int corpTypeId;

    
    @Column( name = "errorreturnurl" )
    private String defaultErrorReturnUrl;

    //@Column( name = "template" )
    //private String template;

    @Column( name = "mobiletemplate" )
    private String mobileTemplate;

    @Column( name = "faviconuri" )
    private String faviconUri;

    @Column( name = "faviconuri16" )
    private String faviconUri16;

    @Column( name = "faviconuri32" )
    private String faviconUri32;

    @Column( name = "faviconuri60" )
    private String faviconUri60;
    
    
    
    @Column( name = "imageiconfilename" )
    private String imageIconFilename;

    @Column( name = "width" )
    private int width;

    @Column( name = "pagetitle" )
    private String pageTitle = "Automated Reference Checks";


    /*
     0=not required
     1=required
     2=optional
    */
    @Column( name= "releaserqd" )
    private int releaseRqd = 0;
        
    @Column( name = "releasemessage" )
    private String releaseMessage;

    @Column( name = "welcomemessage" )
    private String welcomeMessage;

    @Column( name = "completecustomtext" )
    private String completeCustomText;
    
    @Column( name = "header" )
    private String header;

    @Column( name = "footer" )
    private String footer;

    @Column( name = "specialinstructions" )
    private String specialInstructions;

    @Column( name = "headcode" )
    private String headCode;
    
    @Column( name = "proctorparams" )
    private String proctorParams;
    
    
    

    @Transient
    private boolean isDefault = false;


    @Override
    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }

    public CorpType getCorpType()
    {
        return CorpType.getValue( this.corpTypeId );
    }
    
    public String getOfflinePage()
    {
        return "/ref/offline.xhtml";
    }


    public String getHomePage()
    {
        return "/index.xhtml";
    }

    public boolean getHasHeadCode()
    {
        return headCode != null && !headCode.isEmpty();
    }

    public boolean getHasMobileTemplate()
    {
        return mobileTemplate != null && !mobileTemplate.isBlank();
    }

    public Locale getLocaleToUse()
    {
        if( localeStr == null || localeStr.isEmpty() )
            return Locale.US;

        return I18nUtils.getLocaleFromCompositeStr( localeStr );
    }



    public void setDefaultsForUnknown()
    {
    	defaultReturnUrl = null;

    	imageIconFilename = null;
    }


    
    public boolean getHasWelcomeMessage( RefUserType refUserType )
    {
        return getHasMessage( welcomeMessage, refUserType );
    }
    public boolean getHasReleaseMessage( RefUserType refUserType )
    {
        return getHasMessage( releaseMessage, refUserType );
    }
    public boolean getHasSpecialInstructions( RefUserType refUserType )
    {
        return getHasMessage( specialInstructions, refUserType );
    }

    public boolean getHasMessage( String inStr, RefUserType refUserType )
    {
        return getMessage( inStr, refUserType ) != null;
    }
    
    
    public String getWelcomeMessageXhtml( RefUserType refUserType )
    {
        return getStringXhtml( getWelcomeMessage( refUserType ) );
    }    
    
    private String getStringXhtml( String s )
    {
        // if the string has HTML do not mess with it. 
        if( s==null || s.contains("<") || s.contains(">") )
            return s;
        
        // assume no HTML, so replace entities.
        return StringUtils.replaceStandardEntities( s );        
    }
    
    public String getWelcomeMessage( RefUserType refUserType )
    {
        return getMessage( welcomeMessage, refUserType );
    }

    public String getReleaseMessageXhtml( RefUserType refUserType )
    {
        return getStringXhtml( getReleaseMessage( refUserType ) );
    }    
    public String getReleaseMessage( RefUserType refUserType )
    {
        return getMessage( releaseMessage, refUserType );
    }
    
    public String getSpecialInstructionsXhtml( RefUserType refUserType )
    {
        return getStringXhtml( getSpecialInstructions( refUserType ) );
    }    
    
    public String getSpecialInstructions( RefUserType refUserType )
    {
        return getMessage( this.specialInstructions, refUserType );
    }
        
    public String getMessage( String inStr, RefUserType refUserType )
    {
        if( inStr==null || inStr.isBlank() )
            return null;        
        String c = StringUtils.getBracketedArtifactFromString( inStr, "CANDIDATE" );
        String r = StringUtils.getBracketedArtifactFromString( inStr, "RATER" );
        
        if( c==null && r==null )
            return inStr;
        
        
        if( refUserType==null || refUserType.getIsCandidate() )
            return c;
        else
            return r;
    }
    
    

    

    @Override
    public int compareTo( Corp b )
    {
        if( name != null && name.length() > 0 && b.getName() != null && b.getName().length() > 0 )
            return name.compareTo( b.getName() );

        return ( (Integer) corpId ).compareTo( b.getCorpId() );
    }


    public boolean getHasHeader()
    {
        return header != null && header.length() > 0;
    }

    public boolean getHasFooter()
    {
        return footer != null && footer.length() > 0;
    }


    public String getCorpIdEncrypted()
    {
        try
        {
            return EncryptUtils.urlSafeEncrypt( Integer.toString( corpId ) );
        }

        catch( Exception e )
        {
            LogService.logIt( e, "getCorpIdEncrypted()" );

            return Integer.toString( corpId );
        }
    }

    public long getId()
    {
        return corpId;
    }


    public boolean getHasImageIcon()
    {
        return imageIconFilename != null && imageIconFilename.length() > 0;
    }


    @Override
    public String toString()
    {
        return "Corp id=" + corpId + ", name=" + name + ", adminUserId=" + adminUserId;
    }


    public int getActiveWidth()
    {
        return width > 0 ? width : 960;
    }


	public int getCorpId()
	{
		return corpId;
	}

	public void setCorpId(int corpId)
	{
		this.corpId = corpId;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public long getAdminUserId()
	{
		return adminUserId;
	}

	public void setAdminUserId(long adminUserId)
	{
		this.adminUserId = adminUserId;
	}

	public String getLocaleStr()
	{
		return localeStr;
	}

	public void setLocaleStr(String localeStr)
	{
		this.localeStr = localeStr;
	}

	public String getDefaultReturnUrl()
	{
		return defaultReturnUrl;
	}

	public void setDefaultReturnUrl(String defaultReturnUrl)
	{
		this.defaultReturnUrl = defaultReturnUrl;
	}

	public String getImageIconFilename()
	{
		return imageIconFilename;
	}

	public void setImageIconFilename(String imageIconFilename)
	{
		this.imageIconFilename = imageIconFilename;
	}


	public String getPageTitle()
	{
		return pageTitle;
	}

	public void setPageTitle(String pageTitle)
	{
		this.pageTitle = pageTitle;
	}

        /*
	public int getLogonRequired()
	{
		return logonRequired;
	}


	public void setLogonRequired(int logonRequired)
	{
		this.logonRequired = logonRequired;
	}
         *
         */

        public String getNameForUserOrName()
        {
            return name;
        }



	public String getWelcomeMessage()
	{
		return welcomeMessage;
	}

	public void setWelcomeMessage(String h)
	{
		if( h != null )
		{
			h = h.trim();

			if( h.equalsIgnoreCase("<br>" ))
				h=null;
		}

		this.welcomeMessage = h;
	}

	public String getHeader()
	{
		return header;
	}

	public void setHeader(String h)
	{
		if( h != null )
		{
			h = h.trim();

			if( h.equalsIgnoreCase("<br>" ))
				h=null;
		}

		this.header = h;
	}

	public String getFooter()
	{
		return footer;
	}

	public void setFooter(String h)
	{
		if( h != null )
		{
			h = h.trim();

			if( h.equalsIgnoreCase("<br>" ))
				h=null;
		}

		this.footer = h;
	}


    public boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }


    public String getReleaseMessage() {
        return releaseMessage;
    }

    public void setReleaseMessage(String releaseMessage) {
        this.releaseMessage = releaseMessage;
    }




    public String getSpecialInstructions() {
        return specialInstructions;
    }

    public void setSpecialInstructions(String specialInstructions) {
        this.specialInstructions = specialInstructions;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public String getFaviconUriWithDef() {

        if( faviconUri != null && !faviconUri.isEmpty() )
           return faviconUri;

        return RuntimeConstants.getStringValue( "defaultfaviconuri" );
    }

    public String getFaviconUri16WithDef() {

        if( faviconUri16 != null && !faviconUri16.isBlank() )
           return faviconUri16;

        return RuntimeConstants.getStringValue( "defaultfaviconuri16" );
    }

    public String getFaviconUri32WithDef() {

        if( faviconUri32 != null && !faviconUri32.isBlank() )
           return faviconUri32;

        return RuntimeConstants.getStringValue( "defaultfaviconuri32" );
    }

    public String getFaviconUri60WithDef() {

        if( faviconUri60 != null && !faviconUri60.isBlank() )
           return faviconUri60;

        return RuntimeConstants.getStringValue( "defaultfaviconuri60" );
    }

    
    

    public String getFaviconUri() {
        return faviconUri;
    }

    public void setFaviconUri(String faviconUri) {
        this.faviconUri = faviconUri;
    }

    public String getHeadCode() {
        return headCode;
    }

    public void setHeadCode(String headCode) {
        this.headCode = headCode;
    }


    public String getDefaultErrorReturnUrl() {
        return defaultErrorReturnUrl;
    }

    public void setDefaultErrorReturnUrl(String defaultErrorReturnUrl) {
        this.defaultErrorReturnUrl = defaultErrorReturnUrl;
    }


    public String getCompleteCustomText() {
        return completeCustomText;
    }

    public void setCompleteCustomText(String completeCustomText) {
        this.completeCustomText = completeCustomText;
    }

    public String getMobileTemplate() {
        return mobileTemplate;
    }

    public void setMobileTemplate(String mobileTemplate) {
        this.mobileTemplate = mobileTemplate;
    }

    public int getUseDirectExit() {
        return useDirectExit;
    }

    public void setUseDirectExit(int useDirectExit) {
        this.useDirectExit = useDirectExit;
    }

    public int getReleaseRqd() {
        return releaseRqd;
    }

    public void setReleaseRqd(int releaseRqd) {
        this.releaseRqd = releaseRqd;
    }

    public int getCorpTypeId() {
        return corpTypeId;
    }

    public void setCorpTypeId(int corpTypeId) {
        this.corpTypeId = corpTypeId;
    }

    public String getFaviconUri16() {
        return faviconUri16;
    }

    public void setFaviconUri16(String faviconUri16) {
        this.faviconUri16 = faviconUri16;
    }

    public String getFaviconUri32() {
        return faviconUri32;
    }

    public void setFaviconUri32(String faviconUri32) {
        this.faviconUri32 = faviconUri32;
    }

    public String getFaviconUri60() {
        return faviconUri60;
    }

    public void setFaviconUri60(String faviconUri60) {
        this.faviconUri60 = faviconUri60;
    }

    public String getProctorParams() {
        return proctorParams;
    }

    public void setProctorParams(String proctorParams) {
        this.proctorParams = proctorParams;
    }
    
    
}
