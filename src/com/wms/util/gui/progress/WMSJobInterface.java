/*
 * WMSWorkerJobInterface.java
 *
 * Created on October 26, 2005, 12:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.gui.progress;

/**
 *
 * @author mstemen
 */
public interface WMSJobInterface {
    public abstract WMSJobInterface process();   
    public abstract Object getWorkResults();
    public abstract void setWorkResults( Object workResults );
    public abstract boolean isDone();
    public abstract boolean checkDone();
    public abstract void stop();
    public abstract void setPanel( WMSProgressPanel panel );
}
