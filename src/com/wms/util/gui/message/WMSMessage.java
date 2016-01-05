/*
 * ParallaxMessage.java
 *
 * Created on January 24, 2005, 10:12 PM
 */

package com.wms.util.gui.message;

/**
 *
 * @author  mstemen
 */
public class WMSMessage {

    public enum WMSMess
    {
        UnSelect,
        Fade,
        Select,
        Redraw,
        PopDebug,
        PopWarn,
        PopMess,
        PopYesNo,
        PopYesNo2;
    }

    public static final WMSMessage RESETBUTTON = new WMSMessage("ResetButton");
    public static final WMSMessage UNSELECT = new WMSMessage("unselect");
    public static final WMSMessage FADE = new WMSMessage("fade");
    public static final WMSMessage SELECT = new WMSMessage( "select");
    public static final WMSMessage REDRAW = new WMSMessage( "redraw");
    public static final WMSMessage COLORCHANGE = new WMSMessage( "colorChange" );
    public static final WMSMessage RESET = new WMSMessage( "reset");
    public static final WMSMessage POPDEBUG = new WMSMessage( "PopupDebug" );
    public static final WMSMessage POPWARN = new WMSMessage( "PopupWarning" );
    public static final WMSMessage POPERR = new WMSMessage( "PopupError");
    public static final WMSMessage POPMESS = new WMSMessage( "PopupMessage");
    public static final WMSMessage POPYESNO = new WMSMessage( "PopupYesNo");
    public static final WMSMessage POPYESNO2 = new WMSMessage( "PopupYesNo2");
    public static final WMSMessage CHANGESERVERID = new WMSMessage("ChangeServerID");
    public static final WMSMessage REMOVEOBJECTTOTRASH = new WMSMessage( "RemoveObjectToTrash" );
    public static final WMSMessage RESTOREOBJECTFROMTRASH = new WMSMessage( "RestoreObjectToTrash" );
    public static final WMSMessage DELETEOBJECTFROMTRASH = new WMSMessage("DeleteItemFromTrash");
    public static final WMSMessage REMOVEOBJECT = new WMSMessage( "RemoveObject");
    public static final WMSMessage RESTOREOBJECT = new WMSMessage( "RestoreObject");
    public static final WMSMessage FILTERREALTAPES = new WMSMessage( "FilterRealTapes" );
    public static final WMSMessage FILTERREALTAPESFROMTYPE = new WMSMessage( "FilterRealTapesFromType" );;
    public static final WMSMessage SUSPENDDBUPDATES  = new WMSMessage( "SuspendDBUpdates" );
    public static final WMSMessage RESUMEDBUPDATES  = new WMSMessage( "ResumeDBUpdates" );
    public static final WMSMessage SUSPENDEVENTS  = new WMSMessage( "SuspendEvents" );
    public static final WMSMessage RESUMEEVENTS  = new WMSMessage( "ResumeEvents" );
    public static final WMSMessage SERVERISALIVE = new WMSMessage("ServerIsAlive");
    public static final WMSMessage SERVERISDEAD = new WMSMessage("ServerIsDead");
    public static final WMSMessage SHOWCURSORBUSY = new WMSMessage("ShowCursorBusy");
    public static final WMSMessage SHOWBUSYCURSOROTHER = new WMSMessage( "ShowBusyCursorOther");
    public static final WMSMessage SHOWNORMALCURSOROTHER = new WMSMessage( "ShowBusyCursorOther");
    public static final WMSMessage SHOWCURSORNORMAL = new WMSMessage("ShowCursorNormal");
    public static final WMSMessage BROADCASTEVENT = new WMSMessage("BroadcastEvent");
    public static final WMSMessage SHOWBUSYDIALOG = new WMSMessage( "ShowBusyDialog");
    
    public static final WMSMessage CLOSEBUSYDIALOG = new WMSMessage( "CloseBusyDialog");
    /**
     * Creates a new instance of ParallaxMessage 
     */
    
    private String message;
    
    public String toString()
    {
        return message;
    }
    private WMSMessage( String message ) {
        this.message = message;
    }
    
}
