/*
 * TimeFormat.java
 *
 * Created on October 12, 2005, 11:39 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util;

import java.text.*;
/**
 *
 * @author Matt Stemen
 */
public class TimeFormat extends Format
{
    
    String formatStrShort = "mm:ss";
    String formatStrLong = "hh:mm:ss";
    SimpleDateFormat df = new SimpleDateFormat( formatStrLong );
    int mins = 0;
    int hours = 0;
    int secs = 0;
    
    String minsStr = "";
    String hoursStr = "";
    String secsStr = "";
    
    /** Creates a new instance of TimeFormat */
    public TimeFormat()
    {
        
    }
    
    public String parseObject( String obj, ParsePosition pos )
    {
        return null;
    }
    
    public StringBuffer format( Object obj, StringBuffer buf, FieldPosition pos )
    {
        return buf;
    }
    
    public String parseTime( long time )
    {
        return null;
    }
}
