/*
 * ParallaxEvent.java
 *
 * Created on January 24, 2005, 10:10 PM
 */

package com.wms.util.gui.message;

import java.util.EventObject;
/**
 *
 * @author  mstemen
 */
public class WMSMessageEvent extends EventObject {

    private WMSMessage afMessage = null;
    private String userMessage = "";
    Object sourceObject = null;
    /**
     * Creates a new instance of ParallaxEvent 
     */
    public WMSMessageEvent( Object source, WMSMessage message) {
        super( source );
        if( source == null )
            sourceObject = this;
        else
            sourceObject = source;
        afMessage = message;
    }
    
    public WMSMessageEvent( Object source, WMSMessage message, String userMessage ) {
        super( source );
        afMessage = message;
        if( source == null )
            sourceObject = this;
        else
            sourceObject = source;
        this.userMessage = userMessage;
    }

    public WMSMessage getMessage()
    {
        return afMessage;
    }
    
    public String getUserMessage()
    {
        return userMessage;
    }
    
    public Object getSource()
    {
        return sourceObject;
    }
}
