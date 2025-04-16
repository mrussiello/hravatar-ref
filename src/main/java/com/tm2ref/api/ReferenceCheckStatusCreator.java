/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.api;

import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.entity.user.Org;
import com.tm2ref.entity.user.User;
import com.tm2ref.event.EventFacade;
import com.tm2ref.event.TestKeyStatusType;
import com.tm2ref.global.I18nUtils;
import com.tm2ref.ref.RcFacade;
import com.tm2ref.service.EncryptUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.user.EthnicCategoryType;
import com.tm2ref.user.RacialCategoryType;
import com.tm2ref.user.UserFacade;
import com.tm2ref.util.JaxbUtils;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

/**
 * @author Mike
 */
public class ReferenceCheckStatusCreator {

    UserFacade userFacade;

    // @Inject
    EventFacade eventFacade;

    RcFacade rcFacade;

    Locale rptLocale;

    /**
     * Creates a new instance of AssessmentOrderResource
     */
    public ReferenceCheckStatusCreator() {
    }

    public String getAssessmentResultFromTestKey( AssessmentResult arr,
                                                  TestKey testKey,
                                                  RcCheck rc,
                                                  String reportLanguage,
                                                  String reportTitle,
                                                  byte[] reportBytes) throws Exception
    {

        try
        {
            if( testKey==null )
                throw new Exception( "TestKey is null" );

            AssessmentResult.AssessmentStatus aoas = new AssessmentResult.AssessmentStatus();

            aoas.setStatusDate( getXmlDate( new GregorianCalendar() ) );

            aoas.setStatusCode( testKey.getTestKeyStatusTypeId() );

            aoas.setStatusName( "Scored" );

            arr.setAssessmentStatus(aoas);

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() START " + testKey.toString() );

            int orgId = testKey.getOrgId();

            AssessmentResult.ClientId aoac = new AssessmentResult.ClientId();
            arr.setClientId(aoac);
            aoac.setIdValue( createIdValue( "OrgId", EncryptUtils.urlSafeEncrypt( orgId ) ));

            AssessmentResult.ClientOrderId aoaco = new AssessmentResult.ClientOrderId();
            arr.setClientOrderId(aoaco);
            aoaco.setIdValue( createIdValue( "OrderId", testKey.getExtRef() ) );

            if( userFacade==null )
                userFacade = UserFacade.getInstance();

            User authUser = userFacade.getUser( testKey.getAuthorizingUserId() );

            if( authUser == null )
                throw new Exception( "Cannot find authUser " + testKey.getAuthorizingUserId() );

            Org org = rc==null ? null : rc.getOrg();
            if( org==null )
                org = userFacade.getOrg( testKey.getOrgId() );

            //Suborg suborg = rc.getSuborgId()>0 ? userFacade.getSuborg( rc.getSuborgId()) : null;
            //if( suborg==null && testKey.getSuborgId()>0)
            //{
            //    suborg=userFacade.getSuborg(testKey.getSuborgId());
                // testKey.setSuborg(suborg);
            //}

            // Report report = null;

            if( eventFacade == null )
                eventFacade = EventFacade.getInstance();

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() " + testKey.toString() );

            AssessmentResult.UserArea aoaUA = new AssessmentResult.UserArea();

            arr.setUserArea( aoaUA );

            aoaUA.setTestKey( EncryptUtils.urlSafeEncrypt( testKey.getTestKeyId() ) );

            List<IdValue> idvl = new ArrayList<>();

            // Product product = eventFacade.getProduct( testKey.getProductId() );

            idvl.add( this.createIdValue( "AssessmentId" , EncryptUtils.urlSafeEncrypt( testKey.getProductId() ) ) );
            idvl.add( this.createIdValue( "AssessmentStatusTypeId" , Integer.toString( testKey.getTestKeyStatusTypeId() ) ) );
            idvl.add( this.createIdValue( "AssessmentStatusTypeName" , testKey.getTestKeyStatusType().getKey() ) );

            //Added for HRNX change and duplicate logic.
            String duplicateOrderId;
            for( int i=1;i<=20;i++ )
            {
                duplicateOrderId = testKey.getCustomParameterValue( "dupordid" + i );

                if( duplicateOrderId!=null && !duplicateOrderId.isEmpty() )
                    idvl.add( this.createIdValue( "DuplicateClientOrderId" + i , duplicateOrderId ) );
                else
                    break;
            }


            aoaUA.idValue = idvl;

            AssessmentResult.AssessmentSubject aoau = new AssessmentResult.AssessmentSubject();
            arr.setAssessmentSubject(aoau);

            AssessmentResult.AssessmentSubject.SubjectId aoauSid = new AssessmentResult.AssessmentSubject.SubjectId();
            AssessmentResult.AssessmentSubject.PersonName aoauNm = new AssessmentResult.AssessmentSubject.PersonName();
            AssessmentResult.AssessmentSubject.ContactMethod aoauEm = new AssessmentResult.AssessmentSubject.ContactMethod();
            AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors aoauDemo = new AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors();

            // aoaUA.setAssessmentStatusTypeId( testKey.getTestKeyStatusTypeId() );

            User user;

            if( testKey.getUserId()>0 )
            {
                user = userFacade.getUser( testKey.getUserId() );

                if( user == null )
                    throw new Exception( "User not found. " + testKey.toString() );

                aoauSid.getIdValue().add( this.createIdValue( "userid" , EncryptUtils.urlSafeEncrypt( user.getUserId() ) ) );

                if( user.getExtRef()!=null && !user.getExtRef().isEmpty() )
                    aoauSid.getIdValue().add( this.createIdValue( "userreferenceid" , user.getExtRef() ) );

                aoau.setSubjectId(aoauSid);

                aoauNm.setGivenName( user.getFirstName() );
                aoauNm.setGivenName( user.getLastName() );

                aoau.setPersonName(aoauNm);

                if( user.getEmail()!= null && user.getEmail().isEmpty() && user.getUserType().getNamed() )
                {
                    aoauEm.setInternetEmailAddress(user.getEmail() );
                    aoau.setContactMethod(aoauEm);
                }

                if( user.getHasDemoInfo() || user.getGenderTypeId()>0 || user.getBirthYear()>0 || user.getCountryCode()!=null && !user.getCountryCode().isEmpty()  )
                {
                    AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors.BiologicalDescriptors aoauDemoBio = new AssessmentResult.AssessmentSubject.AssessmentPersonDescriptors.BiologicalDescriptors();

                    if( user.getGenderTypeId()> 0 )
                    {
                        aoauDemoBio.setGenderCode( user.getGenderTypeId() );
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getBirthYear()>0 )
                    {
                        GregorianCalendar gc = new GregorianCalendar( user.getBirthYear(), 0, 1 );
                        aoauDemoBio.setBirthDate( getXmlDate( gc.getTime() ) );
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getRacialCategories() != null && user.getRacialCategories().length()> 0 )
                    {
                        StringBuilder sb = new StringBuilder();

                        RacialCategoryType rct;

                        for( int rci : user.getRacialCategoryIdList() )
                        {
                            rct = RacialCategoryType.getType(rci);

                            if( rct==null )
                                continue;

                            if( sb.length()>0 )
                                sb.append(",");

                            sb.append( rct.getName( Locale.US ) );
                        }

                        if( sb.length()> 0 )
                        {
                            aoauDemoBio.setRaceCode( sb.toString() );
                            aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                        }
                    }

                    if( user.getEthnicCategoryId()>0 )
                    {
                        EthnicCategoryType ect = EthnicCategoryType.getType( user.getEthnicCategoryId() );

                        if( ect != null )
                        {
                            aoauDemoBio.setEnthicityCode( ect.getName( Locale.US ) );
                            aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                        }
                    }

                    if( user.getCountryCode()!=null && !user.getCountryCode().isEmpty() )
                    {
                        aoauDemoBio.setNationalityCode(user.getCountryCode());
                        aoauDemo.setBiologicalDescriptors(aoauDemoBio);
                    }

                    if( user.getLocaleStr()!=null && !user.getLocaleStr().isBlank() )
                        aoauDemoBio.setLanguageCode( user.getLocaleStr() );
                }
            }

            if( aoauDemo.getBiologicalDescriptors()!=null )
                aoau.setAssessmentPersonDescriptors(aoauDemo);

            arr.setAssessmentStatusTypeId( testKey.getTestKeyStatusTypeId() );
            arr.setAssessmentStatusTypeName( testKey.getTestKeyStatusType().getKey() );

            boolean scoreReady = rc!=null && rc.getRcCheckStatusType().getIsComplete(); // testKey.getTestKeyStatusTypeId()>= TestKeyStatusType.REPORTS_COMPLETE.getTestKeyStatusTypeId() && testKey.getTestKeyStatusTypeId()<= TestKeyStatusType.DISTRIBUTION_ERROR.getTestKeyStatusTypeId();

            // LogService.logIt("AssessmentStatusCreator.getAssessmentResultFromTestKey() scoreReady=" + scoreReady + ", testKeyStatusTypeId=" + testKey.getTestKeyStatusTypeId() );

            float percentComplete = rc==null ? 0 : rc.getPercentComplete();

            aoas.setLastAccessDate( getXmlDate( rc==null ? testKey.getLastAccessDate() : rc.getLastUpdate() ) );

            aoas.setPercentComplete(percentComplete);
            aoas.setStatusName( testKey.getTestKeyStatusType().getName() );

            // Incomplete, Complete, Scored, API Error, Other Error
            if( testKey.getTestKeyStatusTypeId()<=TestKeyStatusType.STARTED.getTestKeyStatusTypeId() )
               aoas.setStatus( "Incomplete" );
            else if( testKey.getTestKeyStatusTypeId()<= TestKeyStatusType.SCORING_STARTED.getTestKeyStatusTypeId()  )
               aoas.setStatus( "Complete" );
            else if( scoreReady )
               aoas.setStatus( "Scored" );
            else
            {
                aoas.setStatus( "Other Error" );
                aoas.setDetails( "Error: " + testKey.getTestKeyStatusType().getKey() );
                aoas.setErrorMessage( "Error: " + testKey.getTestKeyStatusType().getKey() + " testKeyStatusTypeId=" + testKey.getTestKeyStatusTypeId() );

                if( testKey.getTestKeyStatusType().equals( TestKeyStatusType.SCORE_ERROR ) ||  testKey.getTestKeyStatusType().equals( TestKeyStatusType.REPORT_ERROR ) )
                    aoas.setErrorCode( 206 );
                else if( testKey.getTestKeyStatusType().equals( TestKeyStatusType.EXPIRED ) ||  testKey.getTestKeyStatusType().equals( TestKeyStatusType.DEACTIVATED ) )
                    aoas.setErrorCode( 205 );
                else
                    aoas.setErrorCode( 100 );
            }

            String resultsViewUrl;
            String userAgent;

            if( scoreReady && rc!=null )
            {
                aoas.setBatteryOverallScore( rc.getOverallScore() );
                aoas.setStatus("Scored");
                aoas.setStatusCode(TestKeyStatusType.DISTRIBUTION_COMPLETE.getTestKeyStatusTypeId() );
                aoas.setStatusName("Distribution Complete");

                AssessmentResult.Results rslts;

                List<AssessmentResult.Results> aoaRl = new ArrayList<>();
                arr.results = aoaRl;

                setReportLocale(org);

                resultsViewUrl = rc.getResultsViewUrl(); // RuntimeConstants.getStringValue( "baseprotocol" ) + "://" + RuntimeConstants.getStringValue( "baseadmindomain" ) + "/ta/r.xhtml?t=" + te.getTestEventIdEncrypted();

                userAgent = rc.getUserAgent();

                if( rcFacade==null )
                    rcFacade = RcFacade.getInstance();

                // Report report = ReportFacade.getInstance().getReport( rc.getReportId() );

                rslts = new AssessmentResult.Results();
                rslts.setProfile( rc.getRcScript().getName() );
                rslts.setLangStr( rptLocale.toString() );
                rslts.setAssessmentId( EncryptUtils.urlSafeEncrypt( testKey.getProductId() ) );

                aoaRl.add(rslts);

                GregorianCalendar gcal = new GregorianCalendar();

                gcal.setTime( rc.getCompleteDate());

                rslts.setAssessmentCompleteDate( getXmlDate( gcal ) );

                AssessmentResult.Results.AssessmentOverallResult overa = new AssessmentResult.Results.AssessmentOverallResult();

                overa.setProductTypeId(testKey.getProductTypeId() );

                overa.setScoreNumeric(rc.getOverallScore());
                overa.setResultsViewUrl( resultsViewUrl );


                rslts.setAssessmentOverallResult(overa);
                aoas.setBatteryResultsViewUrl( resultsViewUrl );

                if( userAgent!=null && !userAgent.isBlank() )
                    aoaUA.idValue.add( this.createIdValue("UserAgent", userAgent));
            }
        }
        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.getAssessmentResultFromTestKey() " + testKey.toString() );
            // Tracker.addApiError();
            throw e;
        }



        try
        {
            String out = JaxbUtils.marshalAssessmentResultXml(arr);
            // Next, convert to XML Object

            return out;
        }

        catch( Exception e )
        {
            LogService.logIt(e, "AssessmentStatusCreator.getAssessmentResultFromTestKey() Marshalling error. " + testKey.toString() );


            throw e;
        }

    }



    private void setReportLocale( Org org )
    {
        rptLocale = I18nUtils.getLocaleFromCompositeStr( org.getDefaultTestTakerLang());
        if( rptLocale == null )
            rptLocale = Locale.US;
    }




    private XMLGregorianCalendar getXmlDate( Date d ) throws Exception
    {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(d);
        gc.setTimeZone( TimeZone.getTimeZone("UTC"));

        return getXmlDate( gc );
    }


    private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    {
        // return getXmlDate( gc.getTime() );
        return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    }


    private IdValue createIdValue( String name, String value )
    {
        IdValue idv = new IdValue();

        idv.setName(name);
        idv.setValue(value);

        return idv;
    }


}
