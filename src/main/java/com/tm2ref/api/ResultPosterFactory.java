/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.tm2ref.api;

import com.tm2ref.entity.event.TestKey;
import com.tm2ref.entity.ref.RcCheck;
import com.tm2ref.event.ResultPostType;


/**
 *
 * @author Mike
 */
public class ResultPosterFactory {

    public static ResultPoster getResultPosterInstance( TestKey tk, RcCheck rc )
    {        
        if( tk.getResultPostUrl()!= null && !tk.getResultPostUrl().isEmpty() )
            return tk.getResultPostTypeId()==ResultPostType.DEFAULT.getResultPostTypeId() ? new DefaultResultPoster( tk, rc ) : new DefaultResultPoster( tk, rc );
        
        return null;
    }
}
