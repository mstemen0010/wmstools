/*
 * GESSpellBookPageInterface.java
 *
 * Created on May 23, 2006, 11:19 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import java.util.Iterator;
/**
 *
 * @author mstemen
 */
public interface WMSSpellBookPageInterface {
    public abstract void addSpell( WMSSpellInterface spell );
    
    public abstract Iterator<WMSSpellInterface> getSpells();
}
