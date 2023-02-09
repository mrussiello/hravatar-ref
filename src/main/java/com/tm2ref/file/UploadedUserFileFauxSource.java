/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.tm2ref.file;

import java.util.Map;

/**
 *
 * @author miker_000
 */
public interface UploadedUserFileFauxSource {
    
    public Map<Integer, Integer> getFailedIndexMap();
    public void initFailedIndexMap();
    public int getMaxThumbIndex();
    
}
