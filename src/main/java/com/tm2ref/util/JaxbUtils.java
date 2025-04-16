/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.tm2ref.api.AssessmentResult;
import com.tm2ref.api.result.AssessmentStatusRequest;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import java.io.StringReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 *
 * @author Mike
 */
public class JaxbUtils
{


    public static AssessmentStatusRequest ummarshalAssessmentStatusRequestXml( String xml ) throws Exception
    {
        try
        {
            JAXBContext jc = JAXBContext.newInstance(AssessmentStatusRequest.class);

            Unmarshaller u = jc.createUnmarshaller();
            XMLInputFactory xif = XMLInputFactory.newFactory();
            xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
            xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(xml));            
            return (AssessmentStatusRequest) u.unmarshal( xsr );
        }

        catch( Exception e )
        {
            if( e instanceof org.xml.sax.SAXParseException )
                LogService.logIt( "JaxbUtils.ummarshalAssessmentStatusRequestXml() AAA ResultXml appears to be malformed. SAXParseException. " + e.toString() + ", xml=" + xml );

            else if( e instanceof jakarta.xml.bind.UnmarshalException )
                LogService.logIt( "JaxbUtils.ummarshalAssessmentStatusRequestXml() AAA ResultXml appears to be malformed. jakarta.xml.bind.UnmarshalException. " + e.toString() + ", xml=" + xml );

            else
                LogService.logIt( e, "JaxbUtils.ummarshalAssessmentStatusRequestXml() " + xml );

            throw new STException( e );
        }
    }


    
    public static String marshalAssessmentResultXml( AssessmentResult assessmentResult ) throws Exception
    {
        try
        {
            JAXBContext jc = JAXBContext.newInstance(AssessmentResult.class);//JAXBContext.newInstance( "com.tm2test.api" );
            Marshaller u = jc.createMarshaller();

            u.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            StringWriter sw = new StringWriter();
            u.marshal(assessmentResult, sw );
            return sw.toString();
        }

        catch( Exception e )
        {
            LogService.logIt( e, "JaxbUtils.marshalAssessmentResultXml() orgId=" + assessmentResult.getClientId().getIdValue().getValue() +", ClientOrderId=" + assessmentResult.getClientOrderId().getIdValue().getValue() );

            throw new STException( e );
        }
    }


}
