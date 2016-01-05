/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util.reflectTools;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mstemen
 */
public class ReflectTools {

    /** utility class to do reflection with
     * 
     */
    
    
    
    private ReflectTools() {}
    
    public static Object respondsToInterface( Class classToTest, Class targetInterface ) throws ClassException
    {
        if( classToTest == null )
        {
            throw new ClassException(  "Relefect tools: First argument cannot be null");
        }       
        Class interfaceFound = null;
        Class[] interfaces = classToTest.getInterfaces();
        List<Class> li = Arrays.asList(interfaces);
        if( li == null ) // something realy bad happened
        {
            throw new ClassException( "Relefect tools: Unable to construct list from array of Interfaces found.");
        }
        if( li.contains( targetInterface ) )
        {
            try
            {
                int index = li.indexOf( targetInterface );
                interfaceFound = li.get(index);
                if( interfaceFound == null || ! interfaceFound.isInterface() )
                {
                    throw new ClassException( "Relefect tools: Resulting class was either null or not of type Interface");
                } 
            }
            catch( IndexOutOfBoundsException e )
            {
                ; // do nothing just return null
            }
            
        }
     
        
        return interfaceFound;
    }
}
