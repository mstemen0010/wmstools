/*
 * GESSpellCauldron.java
 *
 * Created on May 18, 2006, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import java.util.Iterator;
import java.util.HashSet;
/**
 *
 * @author mstemen
 */
public class WMSSpellCauldron extends java.util.Properties
{
    
    /** Creates a new instance of GESSpellCauldron */
    public WMSSpellCauldron() {
    }
    
    public void add( String key, String value )
    {
        this.put( key, value );
        System.out.println( "\nCauldron has new name-value pair: " + key  + "=" + value );
        System.out.println( "Number of name-value pairs in Cauldron: " + this.size() + "\n" );
    }
    
    public void add( HashSet<WMSSpellInterface>spells )
    {
        Iterator<WMSSpellInterface> it = spells.iterator();
        while( it.hasNext() )
        {
            WMSSpellInterface spell = it.next();
            if( spell != null )
            {
                if( spell.getName().length() <= 0 )
                    continue;
                if( ! this.containsKey( spell.getName() ))
                {
                    
                    put( spell.getName(), spell.getValue() );
                    System.out.println( "\nCauldron has new name-value pair: " + spell.getName()  + "=" + spell.getValue() );
                    System.out.println( "Number of name-value pairs in Cauldron: " + this.size() + "\n" );
                }
            }
        }
    
    }
    
}
