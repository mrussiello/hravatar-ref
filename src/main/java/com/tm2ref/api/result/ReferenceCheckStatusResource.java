/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.api.result;


import com.tm2ref.affiliate.AffiliateAccountType;
import com.tm2ref.api.APIException;
import com.tm2ref.api.AssessmentResult;
import com.tm2ref.api.ReferenceCheckStatusCreator;
import com.tm2ref.api.IdValue;
import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.event.EventFacade;
import com.tm2ref.event.TestKeyStatusType;
import com.tm2ref.global.RuntimeConstants;
import com.tm2ref.purchase.ProductType;
import com.tm2ref.purchase.PurchaseFacade;
import com.tm2ref.ref.RcCheckStatusType;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.ref.RcScriptFacade;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.service.Tracker;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.JaxbUtils;
import com.tm2ref.util.StringUtils;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import jakarta.ejb.EJBException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import java.util.Locale;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * REST Web Service
 *
 * @author Mike
 */
@Path("referencecheckstatus")
@RequestScoped
public class ReferenceCheckStatusResource {

    UserFacade userFacade;
    EventFacade eventFacade;
    PurchaseFacade purchaseFacade;
    RcFacade rcFacade;


    @Context
    private UriInfo context;

    /**
     * Creates a new instance of AssessmentOrderResource
     */
    public ReferenceCheckStatusResource() {
    }

    @GET
    @Produces("application/xml")
    public String getXml() {

        return "<Error>Please Use HTTP POST for this service.</Error>";
    }



    @POST
    @Consumes("application/xml")
    @Produces("application/xml")
    public String postReferenceCheckStatusRequest(String xmlContent) {

        AssessmentResult arr = new AssessmentResult();

        TestKey testKey = null;

        try
        {
            Tracker.addApiAssessmentStatusRequest();

            AssessmentResult.AssessmentStatus aoas = new AssessmentResult.AssessmentStatus();
            aoas.setStatusDate( getXmlDate( new GregorianCalendar() ) );
            
            // aoas.setStatusCode( testKey.getTestKeyStatusTypeId() );

            arr.setAssessmentStatus(aoas);

            // LogService.logIt(  "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() received " + xmlContent );

            // AssessmentOrderAcknowledgement.UserArea
            if( !getNewTestStartsOk() )
                throw new APIException( 1, "System is undergoing temporary maintenance. " );

            if( xmlContent != null  )
                xmlContent = xmlContent.trim();

            if( xmlContent == null )
                throw new APIException( 101, "XML from request is null.", null );

            xmlContent = StringUtils.stripNonValidXMLCharacters(xmlContent);        

            if( xmlContent.isBlank() )
                throw new APIException( 101, "XML from request is empty.", null );
            
            AssessmentStatusRequest aor = JaxbUtils.ummarshalAssessmentStatusRequestXml(xmlContent);

            String clientId = getRequestIdValueValue( "OrgId", aor.getClientId().getIdValue() );

            if( clientId==null || clientId.isBlank() )
                throw new APIException( 101, "ClientId missing or is empty.", null );
            
            int orgId = Integer.parseInt( EncryptUtils.urlSafeDecrypt( clientId ) );

            AssessmentResult.ClientId aoac = new AssessmentResult.ClientId();

            arr.setClientId(aoac);

            aoac.setIdValue( createIdValue( "OrgId", clientId ));

            if( orgId <= 0 )
                throw new APIException( 102, "ClientId.OrgId is invalid: value=" + clientId + ", parsed OrgId=" + orgId, null );

            if( userFacade == null )
                userFacade = UserFacade.getInstance();

            Org org = userFacade.getOrg( orgId );

            if( org == null )
                throw new APIException( 102, "ClientId.OrgId is invalid: Org not found. value=" + clientId  +", parsed ordId=" + orgId, null );

            if( org.getOrgStatusType().getDenyTesting())
                throw new APIException( 103, "Org associated with ClientId.OrgId is not in an active state. value=" + clientId  +", parsed ordId=" + orgId );

            String username = getRequestIdValueValue( "Username", aor.getClientId().getIdValue() );

            String password = getRequestIdValueValue( "Password", aor.getClientId().getIdValue() );

            String eventRef = getRequestIdValueValue( "OrderId", aor.getClientOrderId().getIdValue() );

            AssessmentResult.ClientOrderId aoaco = new AssessmentResult.ClientOrderId();
            arr.setClientOrderId(aoaco);

            aoaco.setIdValue( createIdValue( "OrderId", eventRef ) );

            String testKeyIdStr=null;
            // int includeScoreCode=0;
            // int includeEnglishReportCode=0;

            Org custOrg = null;

            if( aor.getUserArea()!=null )
            {
                testKeyIdStr=aor.getUserArea().getTestKey();

                // includeScoreCode=aor.getUserArea().getIncludeScoreCode()==null ? 0 : aor.getUserArea().getIncludeScoreCode();

                String clientCustomerAccountUniqueId = getRequestIdValueValue( "ClientUniqueCustomerOrganizationId" , aor.getUserArea().getIdValue() );

                if( clientCustomerAccountUniqueId != null && !clientCustomerAccountUniqueId.isBlank() )
                {
                    if( userFacade == null )
                        userFacade = UserFacade.getInstance();

                    // LogService.logIt(  "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() seeking  clientCustomerAccountUniqueId" + clientCustomerAccountUniqueId );

                    int coid = Integer.parseInt( EncryptUtils.urlSafeDecrypt( clientCustomerAccountUniqueId ));

                    custOrg = userFacade.getOrg( coid );

                    if( custOrg == null )
                        throw new Exception( "Cannot find a matching Org for UserArea.idValue(ClientUniqueCustomerOrganizatonId)=" + clientCustomerAccountUniqueId + ", orgId=" + coid );
                }
                
                /*
                String includeEnglishReport = getRequestIdValueValue( "includeEnglishReport" , aor.getUserArea().getIdValue() );
                
                if( includeEnglishReport != null && !includeEnglishReport.isBlank() && !includeEnglishReport.trim().equals( "0" ) )
                {
                    try
                    {
                        // 1=yes include english. 2=no do not include english.
                        includeEnglishReportCode = Integer.parseInt( includeEnglishReport );
                        
                        if( includeEnglishReportCode <0 || includeEnglishReportCode > 2 )
                            throw new APIException( 160, "UserArea.idValue(includeEnglishReport) is invalid: " + includeEnglishReport + ", valid values are 0, 1, or 2.");
                    }
                    catch( NumberFormatException e )
                    {
                        LogService.logIt( e, "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() includeEnglishReport is invalid: " + includeEnglishReport );
                        
                        throw new APIException( 160, "UserArea.idValue(includeEnglishReport) is invalid: " + includeEnglishReport + ", valid values are 0, 1, or 2.");
                    }
                } 
                */
            }

            if( (eventRef == null || eventRef.isBlank()) && (testKeyIdStr==null || testKeyIdStr.isBlank())  )
                throw new APIException( 201, "ClientOrderId (HRA eventRef) or UserArea.TestKey is required. " );

            if( username == null || username.isBlank() )
                throw new APIException( 104, "ClientId.username is required. " + username );

            if( password == null || password.isBlank() )
                throw new APIException( 105, "ClientId.password is required. " );



            //if( username == null || username.isBlank() || password==null || password.isBlank() )
            //    throw new Exception( "Cannot authenticate username and/or password. Credentials missing: username=" + username + ", orgId=" + orgId );

            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            User authUser = userFacade.getUserByLogonInfo(username, password);

            // this is a fix to enable the api wrapper to operate without knowing user passwords.
            if( authUser==null )
            {
                User u2 = userFacade.getUserByUsername(username);
                
                if( u2!=null && u2.getUserIdEncrypted().equalsIgnoreCase( password ) )
                    authUser = u2;
            }
            
            if( authUser == null )
                throw new APIException( 106, "Cannot authenticate username and/or password. username=" + username + ", altEventRef=" + eventRef );

            if( !authUser.getRoleType().getIsAdmin() && orgId>0 && authUser.getOrgId()!=orgId )
            {
                // If org is an affiliate acocunt and authUser is from source account, swap with auth user from account.
                if( org.getAffiliateId() != null && !org.getAffiliateId().isBlank() && !AffiliateAccountType.getValue( org.getAffiliateAccountTypeId() ).getIsSource() )
                {
                    Org authUserOrg = userFacade.getOrg( authUser.getOrgId() );

                    // AuthUser is from source account, use an admin from the member account.
                    if( authUserOrg.getAffiliateId() != null &&
                        org.getAffiliateId().equalsIgnoreCase( authUserOrg.getAffiliateId() ) &&
                        AffiliateAccountType.getValue( authUserOrg.getAffiliateAccountTypeId() ).getIsSource() )
                    {
                        if( org.getAdminUserId()>0 )
                            authUser = userFacade.getUser( org.getAdminUserId() );

                        else
                        {
                            List<User> orgAdmins = userFacade.getAdminUsersForOrgId( org.getOrgId() );

                            if( orgAdmins != null && !orgAdmins.isEmpty() )
                                 authUser = orgAdmins.get(0);
                        }
                    }
                }

                if(  authUser.getOrgId()!=org.getOrgId() )
                    throw new APIException( 121, "ClientId (HRA OrgId) mismatch between authUser: " + authUser.toString() + " and provided orgId=" + orgId + ", org.affiliateId=" + org.getAffiliateId() );

            }

            if( custOrg != null )
            {
                org = custOrg;
                authUser = userFacade.getUser( custOrg.getAdminUserId() );
                orgId = org.getOrgId();
            }


            // if( orgId>0 && authUser.getOrgId()!=orgId )
            //     throw new Exception( "ClientId (HRA OrgId) mismatch between authUser: " + authUser.toString() + " and provided orgId=" + orgId );

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            if( testKeyIdStr !=null && !testKeyIdStr.isBlank() )
            {
                // LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() Seeking testKey via testKeyIdStr=" + testKeyIdStr );
                long testKeyId = Long.parseLong( EncryptUtils.urlSafeDecrypt( testKeyIdStr ) );

                testKey = eventFacade.getTestKey(testKeyId);

                if( testKey==null )
                    throw new APIException( 202, "Could not find TestKey for UserArea.testKeyId=" + testKeyId + ", encrypted=" + testKeyIdStr );
            }

            else if( eventRef!=null && !eventRef.isBlank()  )
                testKey = eventFacade.getTestKeyForOrgAndEventRef(orgId, eventRef);

            if( testKey==null )
                throw new APIException( 203, "Cannot find TestKey" );

            if( rcFacade==null )
                rcFacade=RcFacade.getInstance();
            
            RcCheck rc = rcFacade.getRcCheckForTestKeyId(testKey.getTestKeyId() );
            if( rc==null )
            {
                LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() TestKeyId=" + testKey.getTestKeyId() + ", testKeyStatusTypeId=" + testKey.getTestKeyStatusTypeId() + ", RcCheck is null.");
            }
            else
                rc.setRcScript( RcScriptFacade.getInstance().getRcScript( rc.getRcScriptId(), false));
            
            ProductType pt = ProductType.getValue(testKey.getProductTypeId());
            if( !pt.equals( ProductType.REFERENCECHECK ))
                throw new APIException( 205, "TestKey is not ReferenceCheck-Only product type. tkid=" + testKey.getTestKeyId() + ", orgId=" + testKey.getOrgId() + ", productTypeId=" + testKey.getProductTypeId() );
            
            aoas.setStatusCode( testKey.getTestKeyStatusTypeId() );

            aoas.setPercentComplete( rc!=null ? rc.getPercentComplete() : getTestKeyPercentComplete( testKey ) );            
            aoas.setLastAccessDate( rc!=null ? getXmlGregorianCalendar(rc.getLastUpdate()) : getTestKeyLastAccessDate(testKey) );
                        
            if( rc!=null )
            {
                if( rc.getRcCheckStatusTypeId() < RcCheckStatusType.COMPLETED.getRcCheckStatusTypeId())
                    throw new APIException( 205, "Reference Check is not in appropriate status (" +  rc.getRcCheckStatusTypeId() + ", " + rc.getRcCheckStatusType().getName() +  "). tkid=" + testKey.getTestKeyId() + ", orgId=" + testKey.getOrgId() + ", rcCheckId=" + rc.getRcCheckId() );
            }
            
            else
            {
                if( testKey.getTestKeyStatusTypeId() < TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId()  )
                    throw new APIException( 205, "TestKey is not in appropriate status (" +  testKey.getTestKeyStatusTypeId() + ", " + testKey.getTestKeyStatusType().getName() +  "). tkid=" + testKey.getTestKeyId() + ", orgId=" + testKey.getOrgId() );

                if( testKey.getTestKeyStatusTypeId()==TestKeyStatusType.SCORE_ERROR.getTestKeyStatusTypeId()  )
                    throw new APIException( 206, "TestKey experienced an error during scoring. tkid=" + testKey.getTestKeyId() + ", status=" + testKey.getTestKeyStatusType().getName() );
            }
            
            ReferenceCheckStatusCreator asc = new ReferenceCheckStatusCreator();

            // String outXml = asc.getAssessmentResultFromTestKey(arr, testKey, includeScoreCode,reportTitle, frcRptLangStr, includeEnglishReportCode, rptBytes );
            String outXml = asc.getAssessmentResultFromTestKey(arr, testKey, rc, Locale.US.getLanguage(), null, null );

            // LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() Success doc length=" + outXml.length() + ", frcRptLangStr=" + frcRptLangStr );

            return outXml;
        }

        catch( APIException e )
        {
            if( e.getCode()==205 )
            {
            } //    LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() 205 Error msg=" + e.getMessage() + ", msg2=" + e.getMessage2() + ", " + ( testKey==null ? "TestKey is null" : "tkid=" + testKey.getTestKeyId() ) ); // + ", " + xmlContent  );
            
            else if( e.getCode()==100 )
            {
                LogService.logIt( e, "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() 100 APIException - Unknown System Error. Error msg=" + e.getMessage() + ", msg2=" + e.getMessage2() + ", " + ( testKey==null ? "TestKey is null" : "tkid=" + testKey.getTestKeyId() ) );
                Tracker.addApiError();
            } 
            
            else
            {
                LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest()  Standard API Error  code=" + e.getCode() + ", msg=" + e.getMessage() + ", msg2=" + e.getMessage2() + ", content=" + xmlContent + ", tkid=" + (testKey==null ? "null" : testKey.getTestKeyId() ) );            
                Tracker.addApiError();
            }

            arr.getAssessmentStatus().setStatus("Error");
            arr.setResponseTypeId(e.getCode());
            arr.getAssessmentStatus().setDetails( e.getMessage() + ( e.getMessage2()==null ? "" : "\n" + e.getMessage2() ) );
            arr.getAssessmentStatus().setErrorMessage( e.getMessage() + ( e.getMessage2()==null ? "" : "\n" + e.getMessage2() ) );
            arr.getAssessmentStatus().setErrorCode( e.getCode() );
        }
        catch( EJBException | IOException e )
        {
            LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() " + e.toString() + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) + ", content=" + xmlContent );
            Tracker.addApiError();

            arr.getAssessmentStatus().setStatus("Error");
            arr.setResponseTypeId(100);
            arr.getAssessmentStatus().setDetails( "System Error. " + e.getMessage());
            arr.getAssessmentStatus().setErrorMessage("System Error. " + e.getMessage());
            arr.getAssessmentStatus().setErrorCode( 100 );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() " + e.toString() + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) + ", content=" + xmlContent );
            Tracker.addApiError();

            arr.getAssessmentStatus().setStatus("Error");
            arr.setResponseTypeId(100);
            arr.getAssessmentStatus().setDetails( "System Error. " + e.getMessage());
            arr.getAssessmentStatus().setErrorMessage("System Error. " + e.getMessage());
            arr.getAssessmentStatus().setErrorCode( 100 );
        }

        // if we get here that means we had an error.
        try
        {
            String out = JaxbUtils.marshalAssessmentResultXml(arr);
            // Tracker.addApiError();
            // Next, convert to XML Object
            // LogService.logIt( "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() Returning error XML:\n" + out + ", content=\n" + xmlContent );
            return out;
        }

        catch( Exception e )
        {
            LogService.logIt( e, "ReferenceCheckStatusResource.postreferenceCheckStatusRequest() " + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) + ", Marshalling error. content=" + xmlContent );
            Tracker.addApiError();
            return null;
        }

    }

    private XMLGregorianCalendar getXmlGregorianCalendar( Date d )
    {
        if( d==null )
            return null;
        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.setTimeZone( TimeZone.getTimeZone("UTC"));

        try
        {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
        }
        catch( Exception e )
        {
            LogService.logIt( "ReferenceCheckStatusResource.getXmlGregorianCalendar() date=" + (d.toString()));
            return null;
        }        
    }
    
    
    private XMLGregorianCalendar getTestKeyLastAccessDate( TestKey tk )
    {
        if( tk==null || tk.getTestKeyStatusType().getIsActive() || tk.getLastAccessDate()==null )
            return null;
        
        Date d = tk.getLastAccessDate();
                        
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.setTimeZone( TimeZone.getTimeZone("UTC"));

        try
        {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
        }
        catch( Exception e )
        {
            LogService.logIt( "ReferenceCheckStatusResource.getTestKeyLastAccessDate() testKeyId=" + tk.getTestKeyId() + ", lastAccessDate=" + (d.toString()));
            return null;
        }
        
    }
    

    private float getTestKeyPercentComplete( TestKey tk  )
    {
        if( tk==null )
            return 0;
        
        Date d = tk.getLastAccessDate();
        
        TestKeyStatusType tkst = tk.getTestKeyStatusType();
        
        if( tkst.getIsActive() || tkst.getIsDeactivated() )
            return 0;
        
        // conditions where it could have partial test events (started, expired, suspended)
        if( !tkst.getIsStarted() && !tkst.getIsExpired() && !tkst.getIsSuspended() )
        {
            // indicates test key status is complete or higher, not deactivated and not expired, it must have been completed.
            if( tkst.getIsCompleteOrHigher() )
                return 100;
            
            // 
            return 0;
        }
        
        return 0;
    }
    

    private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    {
        return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    }

    private String getRequestIdValueValue( String name, List<com.tm2ref.api.result.IdValue> idValueList )
    {
        if( name==null || name.isBlank() )
            return null;

        for( com.tm2ref.api.result.IdValue idValue : idValueList )
        {
            if( idValue.getName()!=null && idValue.getName().equals(name) )
                return idValue.getValue();
        }

        return null;
    }

    private String getResponseIdValueValue( String name, com.tm2ref.api.result.IdValue idValue )
    {
        if( name==null || name.isBlank() )
            return null;

        if( idValue.getName()!=null && idValue.getName().equals(name) )
            return idValue.getValue();

        return null;
    }


    private IdValue createIdValue( String name, String value )
    {
        IdValue idv = new IdValue();

        idv.setName(name);
        idv.setValue(value);

        return idv;
    }

    public boolean getNewTestStartsOk()
    {
        return RuntimeConstants.getBooleanValue( "newRefStartsOK").booleanValue();
    }



}
