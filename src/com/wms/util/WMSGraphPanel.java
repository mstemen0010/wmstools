/*
 * GESGraphPanel.java
 *
 * Created on July 12, 2005, 10:06 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.wms.util;

import java.awt.*;
import javax.swing.*;
import java.awt.geom.*;
/**
 *
 * @author Matt Stemen
 */
public class WMSGraphPanel extends JPanel {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -8203482839134559800L;
	/** Creates a new instance of GESGraphPanel */
    // private int xScaleFactor = 0;
    // private int yScaleFactor = 0;
    private int xUnits = 0;
    private int yUnits = 0;
    private int xSpan = 10;
    // private int ySpan = 10;
    private int yRange = 10;
    private int xRange = 10;
    private String xAxisLabel;
    private String yAxisLabel;
    private FontMetrics fm = null;
    private int axisOffset = 25 ;
    private int tickLength = 5;
    private int totalUnits = 1;
    private int[] graph = new int[totalUnits];
    private Graphics2D g2d;
    private GeneralPath path = new GeneralPath();
    
    public WMSGraphPanel( int xUnits, int yUnits, String xAxisLabel, String yAxisLabel )
    {
        this.setPreferredSize( new Dimension( xUnits, yUnits ) );
        this.xUnits = xUnits;
        this.yUnits = yUnits;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        g2d = (Graphics2D) super.getGraphics();
        graph = new int[xUnits];
        setBackground( Color.GREEN );
        setUnits();
        fm = getFontMetrics( getFont() );
        
        setBorder( BorderFactory.createLineBorder( Color.DARK_GRAY, 1 ) );
        // test
        
        setXRange( 100 );
        setYRange( 100 );
        plot( 10, 25 );
       //  plot( 20, 75 );
       // plot( 23, 45 );
       //  plot( 47, 61 );
       //  plot( 68, 88 );
        repaint();
    }
    
    private void setUnits()
    {
        
    }
    public void paint( Graphics g )
    {
       
        g2d = (Graphics2D) g;
                       
        g2d.setColor( Color.GRAY.brighter() );
        g2d.fillRect( 1, 1, (int)getSize().getWidth(), (int)getSize().getHeight() );
        drawXAxis( g2d );
        drawYAxis( g2d );
        plotPoints( g2d );
        if( path != null )
        {
            g2d.setColor( Color.GREEN.darker() );        
            g2d.draw( path );
        }
    }
    private void drawXAxis( Graphics2D g2d )
    {
        g2d.setColor( Color.RED );                
        g2d.drawLine(axisOffset, (int)getSize().getHeight() - axisOffset, (int)getSize().getWidth(), (int)getSize().getHeight() - axisOffset  );
        // g2d.drawString( xAxisLabel, (int)getSize().getWidth()/2, (int)getSize().getHeight() - 3 );        
        g2d.drawString( xAxisLabel, (int)getSize().getWidth()/2, (int)getSize().getHeight() - ( (axisOffset/2)) + (fm.getAscent()/2) );        
        // draw the "ticks"
        g2d.setColor( Color.BLUE );
        for( int i = axisOffset; i < xUnits; i += 10 )
        {
            g2d.drawLine( i, (int)getSize().getHeight() - Math.abs(axisOffset - tickLength ), i, ((int)getSize().getHeight() - axisOffset) );
        }
    }    
    
    private void drawYAxis( Graphics2D g2d )
    {
        AffineTransform at = g2d.getTransform();
        g2d.setColor( Color.BLACK );        
        g2d.drawLine(axisOffset, 1, axisOffset, (int)getSize().getHeight() - axisOffset );       
        
        g2d.translate(15, getSize().getHeight()/2 );
        g2d.rotate( Math.toRadians(-90));                
        g2d.drawString( yAxisLabel, 0, 0 );
        g2d.setTransform(at);
        for( int i = 1; i < yUnits - axisOffset; i += xSpan )
        {
            g2d.drawLine( axisOffset, i, axisOffset + 5, i);
        }
        
    }
    private void plotPoints( Graphics2D g2d )
    {
        path.reset();
        path.moveTo(  axisOffset, (int)getSize().getHeight() - axisOffset );
        for( int x = 0; x < totalUnits; x++ )
        // for( int x = 0; x < totalUnits; x++ )
        {
            // if( graph[x] != 0 )
                path.lineTo(x, graph[x] );
        }
        path.closePath();
    }
    
    public void plot( int x, int y )
    {
        if( x < totalUnits)
        {           
           graph[x] = y;
        }
            
    }

    
    public int getAxisOffset() {
        return axisOffset;
    }

    public void setAxisOffset(int axisOffset) {
        this.axisOffset = axisOffset;
    }

    public int getTickLength() {
        return tickLength;
    }

    public void setTickLength(int tickLength) {
        this.tickLength = tickLength;
    }

    public int getTotalUnits() {
        return totalUnits;
    }

    public void setTotalUnits(int totalUnits) {
        this.totalUnits = totalUnits;
        if (totalUnits > 0 )
            xSpan = (int)getPreferredSize().getWidth()/totalUnits;
        graph = new int[totalUnits];
    }

    public int getYRange()
    {
        return yRange;
    }

    public void setYRange(int yRange)
    {
        this.yRange = yRange;
    }

    public int getXRange()
    {
        return xRange;
    }

    public void setXRange(int xRange)
    {
        this.xRange = xRange;
        setTotalUnits( xRange );
    }
   
}
