/*
 * GESSpellBookInterface.java
 *
 * Created on May 23, 2006, 11:37 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

import java.util.HashSet;
/**
 *
 * @author mstemen
 */
public interface WMSSpellBookInterface {
    public abstract void addPage( WMSSpellBookPage page );
    public abstract String getKey();
    public abstract HashSet<WMSSpellInterface> getSpells();
}
