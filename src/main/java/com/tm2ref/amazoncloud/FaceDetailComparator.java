/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.amazoncloud;

import java.util.Comparator;
import software.amazon.awssdk.services.rekognition.model.BoundingBox;
import software.amazon.awssdk.services.rekognition.model.FaceDetail;

/**
 *
 * @author miker_000
 */
public class FaceDetailComparator implements Comparator<FaceDetail>{

    @Override
    public int compare(FaceDetail o1, FaceDetail o2) 
    {
        BoundingBox bb1 = o1.boundingBox();
        BoundingBox bb2 = o2.boundingBox();

        float area1 = bb1==null ? 0 : bb1.width()*bb1.height();
        float area2 = bb2==null ? 0 : bb2.width()*bb2.height();

        float conf1 = o1.confidence();
        float conf2 = o2.confidence();

        // much higher conf 1
        if( conf1>=conf2*2 )
            return -1;
        
        // much higher conf 2
        if( conf2>=conf1*2 )
            return 1;
        
        if( area1>0 && area2>0 )
        {
            // 1 is much bigger
            if( area1>=area2*2 )
                return -1;

            // 2 is much bigger
            if( area2>=area1*2 )
                return 1;
            
           Float f = area1*conf1;
           Float f2 = area2*conf2;
           return f2.compareTo(f);
        }
        
        // at this point create a composite
        Float f = conf1;
        Float f2 = conf2;
        return f2.compareTo(f);

        
        //ImageQuality iq1 = o1.getQuality();
        //ImageQuality iq2 = o2.getQuality();
        
    }
    
    
}
