/*
 * WMSJob.java
 *
 * Created on October 26, 2005, 12:20 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.wms.util.gui.progress;

import java.util.*;
import java.awt.*;
import javax.swing.*;



/**
 *
 * @author mstemen
 */
class WMSProgressFrame extends JFrame
{
    private static final long serialVersionUID = -6116402878191232662L;
    private String title;

    public WMSProgressFrame( String title )
    {
        super( title );
        this.title = title;
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
        setDefaultLookAndFeelDecorated( true );
    }

    @Override
    public void setTitle( String title )
    {
        super.setTitle( title );
    }

    public void close()
    {
        setVisible( false );
    }
}
public abstract class WMSJob implements WMSJobInterface {

    /**
     * @param defHeight the defHeight to set
     */
    public void setDefHeight(int defHeight)
    {
        this.defHeight = defHeight;
    }

    /**
     * @param defWidth the defWidth to set
     */
    public void setDefWidth(int defWidth)
    {
        this.defWidth = defWidth;
    }

    /**
     * @param defBorder the defBorder to set
     */
    public void setDefBorder(int defBorder)
    {
        this.defBorder = defBorder;
    }

    /**
     * @return the defHeight
     */
    public int getDefHeight()
    {
        return defHeight;
    }

    /**
     * @return the defWidth
     */
    public int getDefWidth()
    {
        return defWidth;
    }

    /**
     * @return the defBorder
     */
    public int getDefBorder()
    {
        return defBorder;
    }

    public enum JobTypes
    {
        Long,
        Init,
        Short
    }
    private Object workResults;
    private int lengthOfTask;
    private int current = 0;
    private boolean done = false;
    private boolean cancelled = false;
    private String jobStatement = "";
    private WMSProgressPanel wmsProgressPanel = null;
    private String note = "";
    private Vector<WMSJobWatcherInterface> watchers = new Vector<WMSJobWatcherInterface>();
    private WMSProgressFrame statusFrame;
    private int defHeight = 480;
    private int defWidth = 640;
    private int defBorder = 10;
    private Dimension frameSize = new Dimension( 640, 480 );
    private Dimension panelSize = new Dimension( 640, 480 );



    /** Creates a new instance of WMSJob */
    public WMSJob( JFrame parentFrame, String jobStatement, int lengthOfTask, boolean showProgress ) {

        frameSize = new Dimension( defWidth, defHeight );
        panelSize = new Dimension( defWidth - defBorder, defHeight - defBorder );

        this.jobStatement = jobStatement;
        if( showProgress )
        {
            System.out.println("Forcing show Progress");
            statusFrame = new WMSProgressFrame("");

            statusFrame.setTitle( WMSJob.this.jobStatement );
            statusFrame.setMinimumSize( frameSize );
            statusFrame.setPreferredSize( frameSize );

            Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
            frameSize = statusFrame.getSize ();
            int x = (screenSize.width - frameSize.width) / 2;
            int y = (screenSize.height - frameSize.height) / 2;

            statusFrame.setLocation (x - 200, y );
            statusFrame.validate();
            statusFrame.repaint();

            wmsProgressPanel = new WMSProgressPanel( 0, 100 );
            wmsProgressPanel.setJob( WMSJob.this );
            statusFrame.add( wmsProgressPanel );
            statusFrame.pack();

            statusFrame.setVisible( true );
        }

        /*
        if( showProgress )
        {

            try
            {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        statusFrame = new WMSProgressFrame();
                        statusFrame.setTitle( WMSJob.this.jobStatement );
                        statusFrame.setSize( new Dimension( 500, 200 ));
                        statusFrame.setVisible( true );
                        Dimension screenSize = Toolkit.getDefaultToolkit ().getScreenSize ();
                        Dimension frameSize = statusFrame.getSize ();
                        int x = (screenSize.width - frameSize.width) / 2;
                        int y = (screenSize.height - frameSize.height) / 2;

                        statusFrame.setLocation (x, y);
                        statusFrame.validate();
                        statusFrame.repaint();
                    }
                });
            }
            catch( Exception e )
            {

            }
        }
         */
    }

    public void start() {
        final WMSWorker worker = new WMSWorker( this );
       // wmsProgressPanel.start();
        worker.start();
    }

    public abstract WMSJobInterface process();

    public abstract boolean checkDone();

    public int getLengthOfTask() {
        return lengthOfTask;
    }

    public int getCurrent() {
        return current;
    }

    public Object getWorkResults()
    {
        return workResults;
    }

    public void setWorkResults( Object newWorkResults)
    {
        workResults = newWorkResults;
    }

    public void stop() {
        cancelled = true;
        jobStatement = null;
    }

    public boolean isDone(){ return done; }

    public void setMessage( String message ) {
        this.jobStatement = message;
    }

    public String getMessage() {
        return jobStatement;
    }

    public void setPanel( WMSProgressPanel panel ) {
        this.wmsProgressPanel = panel;
        panel.setMessage( jobStatement );
    }

    public void setNote( String note ) {
        if( wmsProgressPanel != null )
            wmsProgressPanel.setNote( note );
    }
    public String getNote() {
        return note;
    }

    protected void complete() {
        done = true;
        if( statusFrame != null )
            statusFrame.setVisible( false );
        wakeAll();
    }

    public void addWatcher( WMSJobWatcherInterface watcher )
    {
        watchers.add( watcher );
    }

    public void removeWatcher( WMSJobWatcherInterface watcher )
    {
        if( watchers.contains( watcher ))
            watchers.remove( watcher );
    }

    private void wakeAll()
    {
        Iterator<WMSJobWatcherInterface> it = watchers.iterator();
        while( it.hasNext() )
        {
            it.next().jobComplete();
        }
    }

    public void update( int newCurrent )
    {
        System.out.println("Startup Progress: " + newCurrent);
        current = newCurrent;
        if( wmsProgressPanel != null )
        {
            wmsProgressPanel.setValue( newCurrent );
            wmsProgressPanel.validate();
        }
        statusFrame.validate();
        statusFrame.repaint();
    }

}

