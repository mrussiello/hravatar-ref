/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.api;

import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.event.TestEventLogUtils;
import com.tm2ref.purchase.ProductType;
import com.tm2ref.service.EmailUtils;
import com.tm2ref.service.LogService;
import com.tm2ref.util.Base64Encoder;
import com.tm2ref.util.HttpUtils;
import java.io.IOException;
import java.net.ConnectException;
import java.util.Locale;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ConnectionRequestTimeoutException;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

/**
 *
 * @author Mike
 */
public class DefaultResultPoster extends BaseResultPoster implements ResultPoster {

    TestKey testKey;
    RcCheck rc;

    public DefaultResultPoster( TestKey tk, RcCheck rc )
    {
        this.testKey = tk;
        this.rc=rc;
    }
    @Override
    public void postTestResults() throws Exception
    {
        AssessmentResult arr;
        // AssessmentResult.AssessmentStatus aoas;
        String payload = null;
        String url = null;

        if( testKey!=null )
            url = testKey.getResultPostUrl();

        // int statusCode = 0;
        try
        {
            if( testKey==null )
                throw new Exception( "testKey is null" );

            if( rc==null )
                throw new Exception( "rc is null." );

            if( !rc.getRcCheckStatusType().getIsComplete() )
                throw new Exception( "rc is not complete. rc.rcCheckStatusTypeId=" + rc.getRcCheckStatusTypeId()  );

            if( testKey.getProductTypeId()!=ProductType.REFERENCECHECK.getProductTypeId() )
                throw new Exception( "TestKey.productTypeId is invalid. productTypeId=" + testKey.getProductTypeId() );

            if( url==null || url.isEmpty() )
                throw new Exception( "ResultPostUrl is missing: " + url );

            LogService.logIt( "DefaultResultPoster.postTestResults() Start  rc.rcCheckId=" + rc.getRcCheckId() + ", testKeyId=" + testKey.getTestKeyId() );

            // Prepare the content
            arr = new AssessmentResult();
            // aoas = new AssessmentResult.AssessmentStatus();

            ReferenceCheckStatusCreator asc = new ReferenceCheckStatusCreator();

            payload = asc.getAssessmentResultFromTestKey(arr, testKey, rc, Locale.US.getLanguage(), null, null );

            LogService.logIt( "DefaultResultPoster.postTestResults() have payload size=" + payload.length() + ", rc.rcCheckId=" + rc.getRcCheckId() + ", testKeyId=" + testKey.getTestKeyId() + ", url=" + url );

            if( 1==1 )
                LogService.logIt( "DefaultResultPoster.postTestResults() payload:\n" + payload+ "\n" );
            
            Map<String,String> basicAuthCreds = getBasicAuthCredsFromReportFlags( rc, testKey ); // testKey.getBasicAuthParmsForResultsPost();
            final String payload2 = payload;

            try (CloseableHttpClient client = HttpUtils.getHttpClient(30))
            {
                HttpPost post = new HttpPost( url );
                post.setHeader( HttpHeaders.CONTENT_TYPE, "application/xml");

                if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
                {
                    String un = basicAuthCreds.get( "username" );
                    String pwd = basicAuthCreds.get( "password" );

                    String b6 = Base64Encoder.encodeString( un + ":" + pwd );

                    post.setHeader( "Authorization", "Basic " + b6  );

                    // LogService.logIt( "DefaultResultPoster.postTestResults() Set basic Auth header: Basic " + b6);
                }

                post.setEntity( new StringEntity(payload) );

                client.execute(post, (ClassicHttpResponse response) -> {
                    int status = response.getCode();
                    // LogService.logIt( "IpUtils.getIPLocationData() statusCode="+ statusCode );
                    final HttpEntity entity2 = response.getEntity();
                    // String ss = EntityUtils.toString(entity2);

                    if( status==307 )
                    {
                        String location = null;
                        Header[] hdrs = response.getHeaders("Location" );

                        for( Header h : hdrs )
                        {
                            location = h.getValue();
                            if( location!=null && !location.isBlank() )
                                break;
                        }

                        // LogService.logIt( "DefaultResultPoster.postTestResults() Method failed with Temp Redirect: " + response.getReasonPhrase() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId() + ", location hdr=" + location );

                        if( location!=null && !location.isBlank() )
                        {
                            HttpPost post2 = new HttpPost( location );

                            post2.setHeader( HttpHeaders.CONTENT_TYPE, "application/xml");

                            if( basicAuthCreds != null && basicAuthCreds.size()>= 2 )
                            {
                                String un = basicAuthCreds.get( "username" );
                                String pwd = basicAuthCreds.get( "password" );
                                String b6 = Base64Encoder.encodeString( un + ":" + pwd );
                                post2.setHeader( "Authorization", "Basic " + b6  );
                            }

                            post2.setEntity( new StringEntity(payload2) );
                            return client.execute(post2, (ClassicHttpResponse response2) -> {
                                    int status2 = response2.getCode();
                                    // LogService.logIt( "DefaultResultPoster.postTestResults() statusCode="+ statusCode );
                                    final HttpEntity entity22 = response2.getEntity();
                                    if( entity22!=null )
                                         EntityUtils.consume(entity22);
                                    if( status2<200 || status2>=300 )
                                        throw new IOException( "DefaultResultPoster.postTestResults() statusCode="+ status2 + ", reason=" + response2.getReasonPhrase() );
                                    return null;
                                });
                        }
                    }

                    if( status<200 || status>=300 )
                        throw new IOException( "IpUtils.getIPLocationData() statusCode="+ status + ", reason=" + response.getReasonPhrase() );

                    if( entity2!=null )
                        EntityUtils.consume(entity2);

                    return null;
                    });

                TestEventLogUtils.createTestKeyLogEntry(testKey.getTestKeyId(), 2, "Tm2REF DefaultResultPoster.postTestResults() Result Post Succeeded. url=" + url);

                // LogService.logIt( "DefaultResultPoster.postTestResults() Response Code : " + r.getCode() );
            }
            catch( ConnectionRequestTimeoutException e )
            {
                LogService.logIt( "DefaultResultPoster.postTestResults() STERR " + e.toString() + ", url=" + url + ", payload=" + (payload==null ? "null" : payload ) + ", req stats=" + HttpUtils.getConnManagerStatsStr() );
                HttpUtils.resetPooledConnectionManagerIfNeeded();
                TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, "Tm2REF DefaultResultPoster.postTestResults() url=" + url + ", error: " + e.toString() );
                throw e;
            }
            catch( IOException e )
            {
                LogService.logIt( "DefaultResultPoster.postTestResults() XXX.2 posting results. " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId() );
                TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, "Tm2REF DefaultResultPoster.postTestResults() url=" + url + ", error: " + e.toString() );
                throw e;
            }

            catch( Exception e )
            {
                LogService.logIt( e, "DefaultResultPoster.postTestResults() XXX.3 Exception posting results. url=" + url + ", rcCheckId=" + (rc==null ? "null" : rc.getRcCheckId()) + ", testKeyId=" + (testKey==null ? "null" : testKey.getTestKeyId()) );
                throw e;
            }
            //finally
            //{
                //if( r != null )
                //    r.close();
            //}
            // LogService.logIt( "DefaultResultPoster.postTestResults() back from call response." + response );
        }
        catch( ConnectException e )
        {
            LogService.logIt( e, "DefaultResultPoster.postTestResults() ZZZ.1 ERROR - ConnectException " + e.toString() + ", url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload )  );
            TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, "Tm2REF DefaultResultPoster.postTestResults() ZZZ.1 ConnectException, url=" + url + ", error: " + e.toString() + ", payload.size=" + (payload==null ? "null" : payload.length()) );
            EmailUtils.getInstance().sendEmailToAdmin( "Tm2REF DefaultResultPoster.postTestResults() ZZZ.1 ConnectException", e.toString() + "  url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );
        }
        catch( Exception e )
        {
            LogService.logIt( e, "DefaultResultPoster.postTestResults() ZZZ.2 url=" + url + ", testKeyId=" + testKey.getTestKeyId()+ " payload=" + (payload==null ? "null" : payload ) );
            TestEventLogUtils.createTestKeyLogEntry( testKey.getTestKeyId(), 0, "Tm2REF DefaultResultPoster.postTestResults() Exception, url=" + url + ", error: " + e.toString() + ", payload.size=" + (payload==null ? "null" : payload.length()) );
            // EmailUtils.getInstance().sendEmailToAdmin( "DefaultResultPoster.postTestResults() Exception", e.toString() + " " + testKey.toString() + " payload=" + (payload==null ? "null" : payload ) );
            throw e;
        }
    }

    //private boolean isStatusCodeOk( int statusCode )
    //{
    //    return statusCode>=200 && statusCode<300;
    //}


    //private XMLGregorianCalendar getXmlDate( GregorianCalendar gc ) throws Exception
    //{
    //    return DatatypeFactory.newInstance().newXMLGregorianCalendar( gc );
    //}




}
