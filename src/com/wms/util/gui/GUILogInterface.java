/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util.gui;

/**
 *
 * @author mstemen
 */
public interface GUILogInterface {

    public enum GLogLevel {

//        Critical(0),
//        Error(1),
//        Warning(2),
//        Message(3),
//        Debug(4),
//        Trace(5),
//        // Parallax MGR ADDITIONAL LEVELS
//        Log_Off(6),
//        Gui_Trace(7),
//        Iso(8),
//        Fatal(9);

        NotSet,
        Off,
        Fatal, // least verbose
        Critical,
        Error,
        Warning,
        Message,
        Debug,
        GUI_Trace,
        Trace; // most verbose
        
        

//        NotSet,
//        Off,
//        Fatal,
//        Critical,
//        Error,
//        Debug,
//        GUI_Trace,
//        Trace,
//        Warning,
//        Message;
        // Iso,
       

        static GLogLevel level = NotSet;


        static public GLogLevel
                getLevel()
        {
            return level;
        }

        static public void setLevel(GLogLevel newLevel )
        {
            level = newLevel;
        }

        static public boolean testMoreVerbose(GLogLevel testLevel)
        {
            return level.ordinal() >= testLevel.ordinal();
        }

        static public boolean testLessVerbose(GLogLevel testLevel )
        {
            return level.ordinal() < testLevel.ordinal();
        }
    }

    public static GLogLevel level = GLogLevel.NotSet;

    public void writeToGui(String msg, GLogLevel writeLevel);

    public void startSectionToGui(String sectionId, GLogLevel writeLevel);

    public void endSectionToGui(String sectionId, GLogLevel writeLevel);

    public void setLevel(GLogLevel newLevel);

    public GLogLevel getLevel();

    public boolean moreVerbose(GLogLevel testLevel);

    public boolean lessVerbose(GLogLevel testLevel);

}
