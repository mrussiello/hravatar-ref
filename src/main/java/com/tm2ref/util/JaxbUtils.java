/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.util;

import com.tm2ref.api.AssessmentResult;
import com.tm2ref.global.STException;
import com.tm2ref.service.LogService;
import java.io.StringWriter;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;

/**
 *
 * @author Mike
 */
public class JaxbUtils
{



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
