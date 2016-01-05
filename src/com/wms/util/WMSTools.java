/*
 * tools.java
 *
 * Created on March 12, 2005, 11:08 AM
 */

package com.wms.util;

import java.util.*;
import java.text.*;
import java.awt.Color;

/**
 *
 * @author Matt Stemen
 */
public class WMSTools
{
    
    /** Creates a new instance of tools */
    public WMSTools()
    {
    }
    
    public static int findNextInSet( HashSet<Integer> set )
    {
        
        
        if( set.size() == 0 ) // empty set, first is always "1"
            return -1;
        ArrayList<Integer> values = new ArrayList<Integer>( set ); // populate the arraylist with our set        
        int lowestNumber = Collections.min(values);
        int highestNumber  = Collections.max( values) ; // find the highest number in the set        
        int targetNumber = lowestNumber; // what we are looking for
        int last = -1; // a place holder
        Collections.sort( values ); // we need the values in the list to be sorted
        
        // our algorithm for finding the missing number, can only be missing from the front, middle or end of the series
        int index = 0;
        for( int i = lowestNumber; i <=  highestNumber; i++ )
        {
            int current = i; // the "ordinal" that might be missing
            int num= values.get(index).intValue(); // get the current set item as an int
            
            if( last == -1 ) // set the place holder for the first time
                last = current;
            
            if( num > current ) // is the first number missing
            {
                targetNumber = current;
                break;
            }
            else if( num == (current + 1 )) // is a middle number missing
            {
                targetNumber = current + 1;
                break;
            }
            else 
            {
                last = current; // neither was true advance to the next number, there may not be a break in the series
            }                                
            index++;
        }
        if( targetNumber == lowestNumber ) // none of the above was met, so this must go at the end of the series
            targetNumber = highestNumber + 1;

        return ( targetNumber );                                   
    }
    public static String findMissingInSet( HashSet<Integer> set )
    {     
        int targetNumber = 0; // what we are looking for
        
        if( set.size() == 0 ) // empty set, first is always "1"
            return "1";
        ArrayList<Integer> values = new ArrayList<Integer>( set ); // populate the arraylist with our set        
        int highestNumber  = Collections.max( values ).intValue(); // find the highest number in the set        
        int last = -1; // a place holder
        Collections.sort( values ); // we need the values in the list to be sorted
        
        // our algorithm for finding the missing number, can only be missing from the front, middle or end of the series
        for( int i = 1; i < highestNumber; i++ )
        {
            int current = i; // the "ordinal" that might be missing
            int num= values.get(i - 1).intValue(); // get the current set item as an int
            
            if( last == -1 ) // set the place holder for the first time
                last = current;
            
            if( num > current ) // is the first number missing
            {
                targetNumber = current;
                break;
            }
            else if( num == (current + 1 )) // is a middle number missing
            {
                targetNumber = current + 1;
                break;
            }
            else 
            {
                last = current; // neither was true advance to the next number, there may not be a break in the series
            }                                
        }
        if( targetNumber == 0 ) // none of the above was met, so this must go at the end of the series
            targetNumber = set.size() + 1;

        return ( String.valueOf( targetNumber  ) );                           
    }
    public static String convertBytes( long bytes )
    {
        
        DecimalFormat df = new DecimalFormat("0.##");
        NumberFormat nf = NumberFormat.getNumberInstance();
       StringBuilder sb = new StringBuilder( nf.format( bytes ) );
        double tera = Math.pow( 1024, 4 );
        sb.append( " bytes (");
        
        
        if( bytes > 0 &&  bytes /tera >= 1 )
            sb.append( df.format( ((float)bytes/tera ) ) ).append( " terabytes)" );
        else if( bytes > 0 && (bytes / (1073741824)) >= 1 )            
            sb.append( df.format( ((float)bytes/1073741824 ) ) ).append( " gigabytes)" );
        else if( bytes > 0 && (bytes / 1048576) >= 1 )            
            sb.append( df.format( ((float)bytes/1048576 ) ) ).append( " megabytes)" );
        else if( bytes > 0 && (bytes / 1024) >= 1 )            
            sb.append( df.format( ((float)bytes/1024 ) ) ).append( " kilobytes)" );        
        else
            sb.append( bytes + " bytes)" );        
        
        return sb.toString();
    }    
    
    public static java.util.Date convertDateFromMySQLString( String dateAsString, WMSLog errorLog )
    {
        //YYYYMMDDHHMMSS
        String pattern = "yyyy-MM-DD HH:MM:ss.S";
        java.util.Date date = null;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat( pattern );
        
        if( sdf != null )
        {
            try
            {                
                
                date = sdf.parse( dateAsString );
            }
            catch( java.text.ParseException e )
            {
               errorLog.write(e.getMessage());
            }
        }
        
        return date;
    }
    
    public static Color convertColorFromString( String colorValues )
    {
        
        Color newColor = null;
        int rgb[] = {0,0,0};
        int rgbIndex = 0;
        StringTokenizer st = new StringTokenizer( colorValues, "," );
        while( st.hasMoreTokens() )
        {
            try
            {
                rgb[rgbIndex++] = Integer.valueOf( st.nextToken() );
            }
            catch( NumberFormatException e )
            {
                
            }
        }
        
        newColor = new Color( rgb[0], rgb[1], rgb[2] );        
        return newColor;
    }
}
