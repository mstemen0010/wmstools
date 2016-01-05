/*
 * GesLog.java
 *
 * Created on October 14, 2004, 12:12 PM
 */


package com.wms.util;


import java.io.*;
import java.util.*;
import java.awt.event.*;
import java.awt.Color;
import javax.swing.JPanel;
import javax.swing.Icon;
import javax.swing.JScrollPane;





/**
 *
 * Class to create and control
 *
 * a log file for an application
 *
 * @author mstemen
 *
 */

/*
 * Log file template symbols key:
 *
 *  %D = date stamp
 *  %I - thread Id
 *  %H - Hashcode
 *  %M - message
 *  %L - level
 */



public class WMSLog implements Runnable {


    public enum LogLevel {
        // Parallax LEVELS
        Critical(0),
        Error(1),
        Warning(2),
        Message(3),
        Debug(4),
        Trace(5),
        // Parallax MGR ADDITIONAL LEVELS
        Log_Off(6),
        Gui_Trace(7),
        Iso(8),
        Fatal(9);
        
        int level = 0;
        LogLevel( int newLevel ) {
            level = newLevel;
        }
        
        public static boolean isValid( String newLevel )
        {
            boolean valid = false;
            try 
            {
                valueOf( newLevel );
                valid = true;
            }
            catch( IllegalArgumentException e )
            {
                // not valid, it will bail from here
                valid = false;
            }
            return valid;
        }
    }
    
    class StackThrowException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 3946860391241688727L;
    }
    
    // Constants
    
/*
//
//DEBUG output control............
//
#define CRITICALERROR_LEVEL      0x00           //unrecoverable errors which will probably cause the system to crash
#define ERROR_LEVEL              0x01           //serious errors that may not be recoverable, user notification probably required
#define WARNING_LEVEL            0x02           //interesting soft failures, conditions, and unexpected states, that the user might be interested in
#define MESSAGE_LEVEL            0x03           //soft failures and conditions, which are somewhat unexpected, but also somewhat common, more useful for debugging than user notification
#define DEBUG_LEVEL              0x04           //debug messages used to indicate some state, or interesting transition
#define TRACE_LEVEL              0x05           //debug messages used to indicated enter/exit and other method->method flow level messages, and seemingly useless debug info
//class control
 */
    /**
     * Constant value to set the logning
     * to an OFF state
     */
    // Parallax LEVELS
    public static final int CRITICAL = 0;
    public static final int ERROR = 1;
    public static final int WARNING = 2;
    public static final int MESSAGE = 3;
    public static final int DEBUG = 4;
    public static final int TRACE = 5;
    // Parallax MGR ADDITIONAL LEVELS
    public static final int LOG_OFF = 6;
    public static final int GUI_TRACE = 7;
    public static final int ISO = 8;
    public static final int FATAL = 9;
    
    
    final static String[] logLevels = new String[]{ "Critical", "Error", "Warning", "Message", "Debug", "Trace", "OFF" , "GUI_DEBUG", "***ISOLATED***", "Fatal"};
    private Color[] logLevelColorsFg = new Color[]{ Color.RED, Color.RED.darker(), Color.ORANGE, Color.GREEN.darker(), Color.YELLOW.darker().darker(), Color.BLUE, Color.BLACK , Color.CYAN, Color.GRAY, Color.RED.brighter()};
    private Color[] logLevelColorsBg = new Color[]{ Color.BLACK, Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK, Color.WHITE, Color.YELLOW, Color.BLACK, Color.GRAY};
    
    static private int numLogStates = 10;
    
    String logName = new String();
    StringBuffer logLoc = new StringBuffer();
    File logFile = null;
    FileOutputStream fileOut = null;
    FileInputStream fileIn = null;
    String logEntry = new String("%D   %L : %M");
    String numberedLogEntry = new String("%D   %N   %L : %M");
    String logTemplate = new String();
    String isoLogEntryNoNumbered = new String("%L  %D : %M");
    String isoLogEntryNumbered = new String("%L  %N                        %D : %M");
    
    String dateStamp = new String();
    String timeStamp = new String();
    String threadId = new String();
    String hashCode = new String();
    String strLevel = new String();
    int logLevel = DEBUG; // default the log level to debug
    boolean toSysOut = false;
    boolean showStackHeader = false;
    int maxWidth = 14;
    int maxThrowHeaderWidth = 40;
    int minWidth = 3;
    boolean iso = false;
    boolean readOnly = false;
    boolean useColor = false;
    boolean useGlyphs = false;
    
    int logOriginLevel = 4;
    Object caller = null;
    
    private int maxLogSize = 100;
    private Stack<String> logStack = new Stack<String>();
    private java.text.SimpleDateFormat dateFormat;
    private JPanel logPanel = null;
    // private GESLogView  logPane = null;
    private Color messageColor = Color.GRAY;
    private Color debugColor = Color.ORANGE;
    private Color guiTraceColor = Color.BLUE;
    private long charCount = 0;
    private long lineCount = 0;
    private long logLinesRead = 0;
    // private SCPTypes.UDTLogType logType = SCPTypes.UDTLogType.UNKNOWN;
    
    private BufferedReader reader = null; // it is out at this level so the log can be "prebuffered"
    public WMSLog( String logLoc, String logName ) {
        logStack.setSize( maxLogSize );
        this.logLoc = new StringBuffer( logLoc );
        this.logName = logName;
        setTemplate( logEntry );
        dateFormat = new java.text.SimpleDateFormat( "DD/MM/yy-HH:mm:ss.SSS" );
        /*
        logPane = new GESLogView( this );
        logPane.setMaxLines( maxLogSize );
        // try and figure out the log type based on the name
        if ( logName.indexOf( "dss" ) >= 0 ) {
            setLogType(SCPTypes.UDTLogType.DSS);
        } else if( logName.indexOf( "UDTConfig.log") >= 0 ) {
            setLogType(SCPTypes.UDTLogType.PARALLAXMGR);
        }
         */
        openLog();
        
    }
    
    
    public WMSLog( String logLoc, String logName, boolean readOnly ) {
        logStack.setSize( maxLogSize );
        this.readOnly = true;
        this.logLoc = new StringBuffer( logLoc );
        this.logName = logName;
        if ( ! readOnly ) {
            setTemplate( logEntry );
            dateFormat = new java.text.SimpleDateFormat( "DD/MM/yy-HH:mm:ss.SSS" );
        }
        // logPane.setEditable(false);
        
        
        // logPane.setDoubleBuffered(true);
        // try and figure out the log type based on the name
//        if ( logName.indexOf( "dss" ) >= 0 ) {
//            setLogType(SCPTypes.UDTLogType.DSS);
//        } else if( logName.indexOf( "UDTConfig.log") >= 0 ) {
//            // setLogType(SCPTypes.UDTLogType.PARALLAXMGR);
//        }
        if( readOnly ) {
            openLog( true);
        } else
            openLog( false );
//        logPane = new GESLogView( this );
//        logPane.init();
//        logPane.setMaxLines( maxLogSize );
//        logPane.addKeyListener(new java.awt.event.KeyAdapter() {
//            public void keyPressed(java.awt.event.KeyEvent evt) {
//                if( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ) {
//                    // logPane.addEntry( new GESLogEntry( "\n" ) );
//                    evt.consume();
//                } else {
//                    evt.consume();
//                }
//            }
//        });
    }
    
    public void start() {
        Thread me = new Thread( this );
        me.setName( "GesLogReaderThread");
        me.start();
    }
    /**
     *
     * reates a new instance of GesLog
     *
     * @param caller the reference to
     *
     * the object that owns
     *
     * the log. This allows
     *
     * for the thread id
     *
     * and or hashcode to
     *
     * be logged
     *
     * @param logLoc path to the log file
     *
     * @param logName name of log file
     *
     */
    
    public WMSLog( Object caller, String logLoc, String logName ) {
        this.caller = caller;
        this.logLoc = new StringBuffer( logLoc );
        this.logName = logName;
        
        // logPane.setEditable(false);
        // logPane.setEditable(false);
        /*
        logPane.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ) {
                    // logPane.addEntry( new GESLogEntry( "\n" ) );
                    evt.consume();
                } else {
                    evt.consume();
                }
            }
        });
         */
        openLog();
    }
    
    
    public void init( ) {
//        if( logPane != null ) {
//            logPane.init();
//        }
    }
    
    public  void setScrollPane( JScrollPane scrollPane ) {
    }
    
    public static String getLevelStr( int logStateInt ) {
        if( logStateInt == WMSLog.DEBUG )
            return logLevels[ WMSLog.DEBUG ];
        else if ( logStateInt == WMSLog.FATAL )
            return logLevels[ WMSLog.FATAL ];
        else if ( logStateInt == WMSLog.GUI_TRACE)
            return logLevels[ WMSLog.GUI_TRACE ];
        else if ( logStateInt == WMSLog.ISO )
            return logLevels[ WMSLog.ISO ];
        else if ( logStateInt == WMSLog.LOG_OFF )
            return logLevels[ WMSLog.LOG_OFF ];
        else if ( logStateInt == WMSLog.MESSAGE )
            return logLevels[ WMSLog.MESSAGE ];
        else if ( logStateInt == WMSLog.TRACE )
            return logLevels[ WMSLog.TRACE ];
        else if ( logStateInt == WMSLog.WARNING )
            return logLevels[ WMSLog.WARNING ];
        else if ( logStateInt == WMSLog.CRITICAL )
            return logLevels[ WMSLog.CRITICAL ];
        else if ( logStateInt == WMSLog.ERROR )
            return logLevels[ WMSLog.ERROR ];
        
        
        return "";
    }
    
    public String getLogLocation() {
        String retStr = null;
        if( logFile != null ) {
            retStr = logFile.getAbsolutePath();
        }
        return retStr;
    }
        
//    public static Icon getLevelIcon( int logStateInt ) {
//        if( logStateInt == GesLog.DEBUG )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.DEBUG );
//        else if ( logStateInt == GesLog.FATAL )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.FATAL );
//        else if ( logStateInt == GesLog.GUI_TRACE)
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.TRACE );
//        else if ( logStateInt == GesLog.ISO )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.SPECIAL );
//        else if ( logStateInt == GesLog.LOG_OFF )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.LOG_INFO );
//        else if ( logStateInt == GesLog.MESSAGE )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.MESSAGE );
//        else if ( logStateInt == GesLog.TRACE )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.TRACE );
//        else if ( logStateInt == GesLog.WARNING )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.WARNING );
//        else if ( logStateInt == GesLog.CRITICAL )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.FATAL );
//        else if ( logStateInt == GesLog.ERROR )
//            return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.FATAL );
//        
//        return SCPMgr.getMgr().getIcon( SCPTypes.LogGlyphs.SPECIAL );
//    }
    
    private void setLogSizes() {
        FileReader tempReader = null;
        
        int ic = 0;
        try {
            tempReader = new FileReader(  logFile );
//            SCPMgr.getMgrLog().write( "Starting file scan...");
            while( ( ic = tempReader.read() ) != -1 ) {
                char c = (char) ic;
                // Character ch = new Character( c );
                if( c == '\n' )
                    lineCount++;
                charCount++;
            }
//            SCPMgr.getMgrLog().write( "Ended file scan");
        } catch( IOException e ) {
            
        }
    }
    
    private ArrayList<String> preBufferFromEndRandAccess( int tailCount ) throws IOException {
        ArrayList<String> readBuffer = new ArrayList<String>();
        RandomAccessFile raf = new RandomAccessFile( logFile, "r" );
        
        int linesFound = 0;
        long charsRead = 0;
        long eof = logFile.length();
        long pos = eof - 1;
        raf.seek( pos ); // intial pos, last char of file
        StringBuffer line = new StringBuffer();
        while( linesFound < tailCount) {
            int ic = raf.read();
            char ch = (char ) ic;
            if( ch == '\n'  ) {
                linesFound++;
                line = line.reverse(); // the line is backward
                readBuffer.add( line.toString() );
                line.delete( 0, line.length() ); // reset the buffer
            } else {
                line.append( ch );
            }
            pos -= 1;
            raf.seek( pos );
            charsRead++;
        }
        // reverse the order of the buffer
//        SCPMgr.getMgr().setLogLinesRead(linesFound);
        logLinesRead = linesFound;
        Collections.reverse( readBuffer );
        return readBuffer;
    }
    
    @SuppressWarnings("unused")
	private ArrayList<String> preBuffer( int tailCount ) throws IOException {
        setLogSizes();
        FileReader tFileReader = new FileReader( logFile );
        StringBuffer lineRead = new StringBuffer();
        ArrayList<String> readBuffer = new ArrayList<String>();
        
        // first figure out how many characters are in the file
        
        try {
            
            long lineCountTarget = tailCount;
            if( lineCount > tailCount )
                lineCountTarget = lineCount - tailCount;
            
            // now reopen the stream and read to the target line
            // reader = new BufferedReader( new InputStreamReader( fileIn ) );
            int linesRead = 0;
            boolean isRead = true;
            char c;
            int ic = 0;
            while( ( ic = tFileReader.read() ) != -1 ) {
                c = (char) ic;
                if( c == '\n' ) {
                    if( linesRead >= lineCountTarget ) {
                        StringBuffer line = new StringBuffer();
                        line.append( String.valueOf( linesRead + 1 ) ).append( " ").append( lineRead.toString() );
                        readBuffer.add( line.toString() );
                        lineRead.delete(0, lineRead.length() );
                    }
                    linesRead++;
                } else if( linesRead >= lineCountTarget ) {
                    lineRead.append( c );
                }
                /*
                if( c != '\n' )
                    lineRead.append( c );
                else
                {
                    linesRead++;
                    if( linesRead >= lineCountTarget )
                    {
                        StringBuffer line = new StringBuffer();
                        line.append( String.valueOf( linesRead ) ).append( " ").append( lineRead.toString() );
                        readBuffer.add( line.toString() );
                    }
                    lineRead.delete(0, lineRead.length() );
                }
                 */
            }
        } catch( IOException e ) {
            
        }
        return readBuffer;
    }
    
    public void run() {
        
        // BufferedReader reader = new BufferedReader( new InputStreamReader( fileIn ) );
        // get all of the logs contends before we start to read the log.
        ArrayList<String> logRead = null;
        boolean preBuffer = true;
        
        preBuffer = true;
        reader = new BufferedReader( new InputStreamReader( fileIn ) );
        if( preBuffer ) {
            try {
                logRead = preBufferFromEndRandAccess(maxLogSize);
                if( logRead != null ) {
                    Iterator<String> it = logRead.iterator();
                    while( it.hasNext() ) {
                        String t = it.next();
                        if( t != null ) {
//                            logPane.addEntry( new GESLogEntry( t ) );
                            
                        }
                    }
                }
            } catch( IOException e ) {
                
            }
//            logPane.insertEntries(logRead);
        }
        try {
            reader.skip( logFile.length() );
        } catch( IOException e ) {
            
        }
        
        
        try {
            Thread.sleep( 300 );
        } catch( InterruptedException e ) {
            
        }
        
        
        
//        ParallaxManagerInterface mgr = SCPMgr.getMgr();
        // readOnly = false;
        while( readOnly ) {
            // logPane.setVisible(false);
            try {
                
                while( reader.ready()  ) {
                    // lineRead = new StringBuffer();
////                    if( logPane != null ) {
////                        logPane.addEntry( new GESLogEntry( reader.readLine() ) );
//                        logLinesRead++;
//                        mgr.setLogLinesRead(logLinesRead);
//                    }
                }
                
//                logPane.repaint();
                
            } catch( OutOfMemoryError e ) {
//                SCPMgr.getMgrLog().write( "Out of Memory in GesLog: " + e.getMessage());
                System.gc();
            } catch( IOException e ) {
            }
            
            
            try {
                Thread.sleep( 100 );
            } catch( InterruptedException e ) {
            }
        }
        // logPane.setVisible(true);
        
    }
    
    public void setIso( boolean newIso )
    
    {
        iso = newIso;
        if( iso ) {
            if( showStackHeader )
                setTemplate( this.isoLogEntryNumbered );
            else
                setTemplate( this.isoLogEntryNoNumbered );
        }
    }
    
    
    public void setShowStackHeader( boolean newShow ) {
        this.showStackHeader = newShow;
        if( newShow ) {
            setTemplate( this.numberedLogEntry);
        }
    }
    
    synchronized public void iso( String message ) {
        // do not log greater than the current log level
        
        
        
        String msg =  makeEntry( logLevels[ 6 ], message );
        try {
            fileOut.write( msg.getBytes() );
            fileOut.write( '\n' );
            fileOut.flush();
        } catch( IOException e ) {
            System.err.println( e.getMessage() );
        }
        if(  toSysOut ) {
            System.out.println(msg);
        }
    }
    
    /**
     *
     * Method to write an entry to the
     *
     * log at the given log level
     *
     * @param level Log level for this
     *
     * entry
     *
     * @param message the Message to the log
     *
     */
    
    synchronized public void write( int level, String message ) {
        // do not log greater than the current log level
        if( level > logLevel && level != FATAL )
            return;
        
        
        if( iso && logLevel != ISO ) {
            return;
        }
        if( level >= logLevels.length ) {
            System.err.print( "Illegal log level: " + level + ", not logging message");
        } else {
            String msg =  makeEntry( logLevels[ level ], message );
            
            if( ! toSysOut ) {
                try {
                    fileOut.write( msg.getBytes() );
                    fileOut.write( '\n' );
                    fileOut.flush();
                } catch( IOException e ) {
                    System.err.println( e.getMessage() );
                }
            } else {
                System.out.println(msg);
            }
            if( logPanel != null ) {
                // logPane.set( logMessageColor );
//                logPane.addEntry( new GESLogEntry( msg + "\n" ) ) ;
//                logPane.invalidate();
            }
        }
    }
    
//    public void setLogPanel( JPanel panel ) {
//        logPanel = panel;
//        if( logPanel != null ) {
//            logPane = new GESLogView( this );
//            logPane.init();
//            
//            logPane.setMaxLines( this.maxLogSize );
//            
//            logPane.addMouseListener( new MouseListener() {
//                public void mouseClicked( MouseEvent e ) {
//                    // selectionStart = logPane.getSelectionStart();
//                    // selectionEnd = logPane.getSelectionEnd();
//                }
//                public void mouseEntered( MouseEvent e ) {
//                }
//                public void mouseExited( MouseEvent e ) {
//                }
//                public void mouseReleased( MouseEvent e ) {
//                }
//                public void mousePressed( MouseEvent e ) {
//                    
//                    // selectionStart = logPane.getSelectionStart();
//                    // selectionEnd = logPane.getSelectionEnd();
//                }
//            }
//            );
//            logPane.addKeyListener(new java.awt.event.KeyAdapter() {
//                public void keyPressed(java.awt.event.KeyEvent evt) {
//                    if( evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER ) {
//                        logPane.addEntry( new GESLogEntry("      \n" ) );
//                        // evt.consume();
//                    } else {
//                        evt.consume();
//                    }
//                }
//            });
//            // init();
//            logPanel.add( logPane.getList() );
//        }
//        
//    }
    
    /**
     *
     * Method to write an entry to the
     *
     * log at the given log level
     *
     * @param message Message to be written in this entry
     *
     */
    
    synchronized public void write( String message ) {
        // this method always logs at DEBUG level
        if( logLevel != DEBUG )
            return;
        if( iso && logLevel != ISO ) {
            return;
        }
        String msg = makeEntry( logLevels[DEBUG], message );
        if( ! toSysOut ) {
            try {
                fileOut.write( msg.getBytes() );
                fileOut.write( '\n' );
                fileOut.flush();
            } catch( IOException e ) {
                System.err.println( e.getMessage() );
            }
        } else {
            System.out.println( msg );
        }
        if( logPanel != null ) {
            // logPane.setForeground(logMessageColor );
//            logPane.addEntry( new GESLogEntry( msg ) );//  + "\n" ) );
//            logPane.validate();
        }
    }
    
    /**
     *
     *
     *
     * @param newTemplate
     *
     */
    
    private void setTemplate( String newTemplate ) {
        logTemplate= newTemplate;
    }
    
    private void openLog( boolean readOnly ) {
        if( logLoc != null &&  logName != null ) {
            String logLocation = logLoc.toString();
            if( ! logLocation.endsWith( "/" ) ) {
                logLoc.append("/");
            }
            if( logLocation.indexOf( logName ) < 0 ) {
                logLoc.append( logName );
            }
            logFile = new File( logLoc.toString() );
        }
        
        if( ! logFile.exists() && ! readOnly ) {
            createLog();
        } else if( ! logFile.exists() && readOnly ) {
            // abort
            return;
        } else if( logFile.exists() && ! readOnly ) {
            try {
                fileOut = new FileOutputStream( logFile, true );
            } catch( FileNotFoundException e ) {
                System.err.println( "Unable to open log file, creating it:  " + e.getMessage() );
                try {
                    logFile.createNewFile();
                } catch( IOException ex ) {
                    System.err.println( "Unable to create file: " + ex.getMessage() );
                }
            }
        } else if( logFile.exists() &&  readOnly ) {
            try {
                fileIn = new FileInputStream( logFile );
            } catch( FileNotFoundException e ) // this shouldn't happen
            {
                return;
            }
        }
        
    }
    
    private void openLog() {
        if( logLoc != null &&  logName != null ) {
            String logLocation = logLoc.toString();
            if( ! logLocation.endsWith( "/" ) ) {
                logLoc.append("/");
            }
            if( logLocation.indexOf( logName ) < 0 ) {
                logLoc.append( logName );
            }
            logFile = new File( logLoc.toString() );
        }
        
        if( ! logFile.exists() )
            createLog();
        else {
            try {
                fileOut = new FileOutputStream( logFile, true );
            } catch( FileNotFoundException e ) {
                System.err.println( "Unable to open log file, creating it:  " + e.getMessage() );
                try {
                    logFile.createNewFile();
                } catch( IOException ex ) {
                    System.err.println( "Unable to create file: " + ex.getMessage() );
                }
            }
        }
    }
    
    
    
    private void createLog() {
        if( readOnly ) // do not create it, if it is supposed to be read only
            return;
        if( logLoc != null &&  logName != null ) {
            String logLocation = logLoc.toString();
            if( ! logLocation.endsWith( "/" ) ) {
                logLoc.append("/");
            }
            
            if( logLocation.indexOf( logName ) < 0 ) {
                logLoc.append( logName );
            }
            
            logFile = new File( logLoc.toString() );
            if( logFile != null ) {
                
                try
                        
                {
                    
                    fileOut = new FileOutputStream( logFile );
                    
                }
                
                catch( FileNotFoundException e )
                
                {
                    
                    System.err.println( "Unable to open log file, creating it:  " + e.getMessage() );
                    
                    try
                            
                    {
                        
                        logFile.createNewFile();
                        
                    }
                    
                    catch( IOException ex )
                    
                    {
                        
                        System.err.println( "Unable to create file: " + ex.getMessage() );
                        
                    }
                    
                }
                
            }
            
        }
        
        else
            
        {
            
            System.err.println( "Unable to create log with NULL log name or log location ");
            
            return;
            
        }
        
    }
    
    
    
    /**
     *
     *
     *
     * @param level
     *
     * @param message
     *
     * @return
     *
     */
    
    private String makeEntry( String level, String message )
    
    {
        
        /*
         * Log file template symbols key:
         *
         *  %D = date stamp
         *  %I - thread Id
         *  %H - Hashcode
         *  %M - message
         *  %L - level
         */
        
        
        
        String retStr = new String();
        
        if( logTemplate == null )
            
        {
            System.out.println( "Unable to make log entry, template is null");
            return null;
        }
        
        // replace each part
        
        Date currentDate = new Date();
        if( logLevel == TRACE ||  logLevel == CRITICAL ) // this is an intensive thing to do, so only request it at trace level
            retStr = swapTag( "%N", logTemplate, padHeader( getStackHeader() ) );
        else
            retStr = swapTag( "%N", logTemplate, "" );
        retStr = swapTag( "%L", retStr, padLevel(level) );
        try
        {
            String date = dateFormat.format( java.util.Calendar.getInstance().getTime() ).toString();
            retStr = swapTag( "%D", retStr, date );
        }
        catch( NullPointerException e )
        {
            System.out.println( "Unable to make log entry, data was null");
        }
        // retStr = swapTag( "%D", retStr, new Date().toString() );
        retStr = swapTag( "%M", retStr, message );
        
        
        
        
        
        return retStr;
        
    }
    
    
    
    /**
     *
     *
     *
     * @param tag
     *
     * @param template
     *
     * @param newValue
     *
     * @return
     *
     */
    
    private String swapTag( String tag, String template, String newValue )
    
    {
        
        StringBuffer buf = new StringBuffer("");
        
        if( template.indexOf( tag ) >= 0 )
            
        {
            
            int beginIndex = template.indexOf( tag );
            
            int endIndex = beginIndex + tag.length();
            
            
            
            String begining = template.substring( 0, beginIndex );
            
            String ending = template.substring( endIndex, template.length() );
            
            buf.append( begining ).append( newValue).append( ending );
            
        }
        
        else
            
            return template;
        
        
        
        return buf.toString();
        
    }
    
    
    
    private void deleteLog()
    
    {
        
        
        
    }
    
    
    
    /**
     *
     * Method to add a notable break
     *
     * in the log file
     *
     */
    
    synchronized public void addBreak()
    
    {
        
        if( ! toSysOut )
            
        {
            
            
            
            try
                    
            {
                
                if( fileOut != null )
                    
                {
                    
                    fileOut.write( new String("=====================================================================================").getBytes());
                    
                    fileOut.write('\n' );
                    
                    fileOut.flush();
                    
                }
                
            }
            
            
            
            catch( IOException e ) {
                System.err.println( e.getMessage() );
            }
        } else {
            System.out.println(new String("====================================================================================="));
        }
    }
    
    
    
    
    
    /**
     *
     * Method to change the logging
     *
     * level to the log object
     *
     * @param newLevel Logging level to
     *
     * set the log to
     *
     */
    
    public void setLogLevel( int newLevel )
    
    {
        
        write( logLevel, "GesLog::setLogLevel: log level is now: " + logLevels[ logLevel ]);
        
    }
    
    public void setLogLevel( String newLevel, int watchBits, int ignoreBits ) {
        
        if( ! LogLevel.isValid(newLevel) )
            newLevel = "Message";
        
        LogLevel level = LogLevel.valueOf(newLevel);
        int overAllLevel = 0;
        Color overAllColor = null;
        int dssLogLevel = 0;
        switch( level ) {
            case Critical:
                overAllLevel = CRITICAL;
                overAllColor = guiTraceColor;
                dssLogLevel = CRITICAL;
                break;
            case Error:
                overAllLevel = ERROR;
                overAllColor = guiTraceColor;
                dssLogLevel = ERROR;
                break;
            case Warning:
                overAllLevel = WARNING;
                overAllColor = guiTraceColor;
                dssLogLevel = WARNING;
                break;
            case Message:
                overAllLevel = MESSAGE;
                overAllColor = messageColor;
                dssLogLevel = MESSAGE;
                break;
            case Debug:
                overAllLevel = DEBUG;
                overAllColor = debugColor;
                dssLogLevel = DEBUG;
                break;
            case Trace:
                overAllLevel = TRACE;
                overAllColor = guiTraceColor;
                dssLogLevel = TRACE;
                break;
            case Log_Off:
                overAllLevel = LOG_OFF;
                overAllColor = guiTraceColor;
                dssLogLevel = LOG_OFF;
                
                
        }
        
        
//        if( logType == SCPTypes.UDTLogType.PARALLAXMGR ) {
//            logLevel = overAllLevel;
//            write( logLevel, "GesLog::setLogLevel: log level is now: " + logLevels[ logLevel ]);
//        } else if( logType == SCPTypes.UDTLogType.DSS ) {
//            // ParallaxMgr.getMgr()
//        }
        
        
        
    }
    
    private void rollOverLog() {
        
    }
    
    private boolean testRollOver() {
        return true;
    }
    
    private String padLevel( String level ) {
        StringBuffer buf = new StringBuffer("");
        buf.append( level );
        for( int i = level.length(); i < maxWidth; i++ ) {
            buf.append( " " );
        }
        return buf.toString();
    }
    
    private String padHeader( String header ) {
        StringBuffer buf = new StringBuffer("");
        buf.append( header );
        for( int i = header.length(); i < this.maxThrowHeaderWidth; i++ ) {
            buf.append( " " );
        }
        return buf.toString();
    }
    
    public void toSysOut( boolean flag ) {
        this.toSysOut = flag;
    }
    
    public void setReadOnly( boolean flag ) {
        this.readOnly = flag;
    }
    
    
    public String getStackHeader( ) {
        //  super();
        //  dateFormat = new java.text.SimpleDateFormat( "HH:mm:ss.SSS" );
        // this.type = type;
        
        StringBuffer messageBuf = new StringBuffer();
        StackTraceElement[] elements;
        
        try {
            throw_stack();
        }
        
        catch ( StackThrowException ste ) {
            elements = ste.getStackTrace();
            
            StackTraceElement element = elements[logOriginLevel];
            StringTokenizer stk = new StringTokenizer( element.getFileName(), "." );
            String className = stk.nextToken();
            if( elements != null && elements.length > 0 ) {
                messageBuf.append(  className ) .append("::").append( element.getMethodName() );
                messageBuf.append("()[").append( element.getLineNumber()).append("]");
            }
        }
        return messageBuf.toString();
    }
    
    public int getLogLevel() {
        return this.logLevel;
    }
    
    protected void throw_stack() throws StackThrowException {
        throw new StackThrowException();
    }
    
    static public int getNumLogStates() {
        return numLogStates;
    }
    
//    public void useColor( boolean flag ) {
//        if( logPane != null ) {
//            logPane.setUseColor( flag );
//            logPane.validate();
//        }
//    }
//    
//    public void useGlyphs( boolean flag ) {
//        if( logPane != null ) {
//            logPane.setUseGlyphs( flag );
//            logPane.validate();
//        }
//    }
    public void setMaxLogSize( int newSize ) {
        maxLogSize = newSize;
        if( logStack != null )
            logStack.setSize( newSize );
//        if( logPane != null )
//            logPane.setMaxLines(newSize);
//        SCPMgr.getMgr().setLogLinesBuffer(newSize);
    }
    
    public Color getLogLevelFg( int level ) {
        return logLevelColorsFg[level];
    }
    
    
    public Color getLogLevelBg( int level ) {
        return logLevelColorsBg[level];
    }
    
//    public void setLogLevelFg( int level, Color fgColor ) {
//        logLevelColorsFg[level] = fgColor;
//        logPane.refreshColors();
//    }
//    
//    public void setLogLevelBg( int level, Color bgColor ) {
//        logLevelColorsBg[level] = bgColor;
//        logPane.refreshColors();
//    }
    
    /*
    public SCPTypes.UDTLogType getLogType() {
        return logType;
    }
    
    public void setLogType(SCPTypes.UDTLogType logType) {
        this.logType = logType;
    }
    */
}


