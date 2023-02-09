/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tm2ref.ref;

import com.tm2ref.faces.FacesBean;
import java.io.Serializable;

/**
 *
 * @author miker_000
 */
public class BaseRefBean extends FacesBean implements Serializable
{
    String strParam1;
    String strParam2;
    String strParam3;
    String strParam4;
    String strParam5;
    String strParam6;
    
    int intParam1;
    
    boolean booleanParam1;
             
    
    
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

    public int getIntParam1() {
        return intParam1;
    }

    public void setIntParam1(int intParam1) {
        this.intParam1 = intParam1;
    }

    public boolean isBooleanParam1() {
        return booleanParam1;
    }

    public void setBooleanParam1(boolean booleanParam1) {
        this.booleanParam1 = booleanParam1;
    }

    
    
}
