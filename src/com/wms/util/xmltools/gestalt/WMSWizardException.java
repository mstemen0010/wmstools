/*
 * GESWizardException.java
 *
 * Created on May 19, 2006, 7:40 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.xmltools.gestalt;

/**
 *
 * @author Matt
 */
public class WMSWizardException extends Exception
{   
    /** Creates a new instance of GESWizardException */
    public WMSWizardException() {
    }
    public WMSWizardException( String message )
    {
        super( message );
    }
}
