package com.tm2ref.faces;

import com.tm2ref.global.RuntimeConstants;
import java.io.Serializable;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;

@Named
@SessionScoped
public class FacesBean implements Serializable
{
    private static final long serialVersionUID = 1L;

    private static String mediaServerBaseUrl = null;


    public static FacesBean getInstance()
    {
        FacesContext fc = FacesContext.getCurrentInstance();

        return (FacesBean) fc.getApplication().getELResolver().getValue( fc.getELContext(), null, "facesBean" );
    }


    public static synchronized String getMediaServerBaseUrlStatic()
    {
        if( mediaServerBaseUrl != null )
            return mediaServerBaseUrl;

        if( RuntimeConstants.getBooleanValue( "useAwsMediaServer" ).booleanValue() )
        {
            mediaServerBaseUrl = RuntimeConstants.getStringValue( "awsBaseUrlHttps" );
        }

        else
        {
            mediaServerBaseUrl = "http://" + RuntimeConstants.getStringValue( "mediaServerDomain" );

            Integer port = RuntimeConstants.getIntValue( "mediaServerPort" );

            if( port != null && port.intValue() != 80 )
                mediaServerBaseUrl += ":" + port;

            mediaServerBaseUrl += "/" + RuntimeConstants.getStringValue( "mediaServerWebapp" );
        }

        return mediaServerBaseUrl;
    }

    public synchronized String getMediaServerBaseUrl()
    {
        return getMediaServerBaseUrlStatic();
    }

}
