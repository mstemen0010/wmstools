/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wms.util.gui.canvas;

import com.wms.util.gui.GUILogInterface;
import com.wms.util.gui.GUILogInterface.GLogLevel;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.ImageIcon;
import javax.swing.JLabel;




/**
 *
 * @author mstemen
 */
public class HyperCanvas extends Canvas
{

    private static final long serialVersionUID = 346695033740032781L;
    Image mainImage;
    JLabel mainImageArea;
    Point recievedPoint;
    String pointStr;
    GUILogInterface logInterface;
    Graphics gr;
    ArrayList<PolyImage> imageMap = new ArrayList<PolyImage>();




    public enum CanvasMode
    {

        Create,
        Use;
    }
    CanvasMode currMode = CanvasMode.Use;

    public void setImageMap( PolyImage pi )
    {
        this.imageMap.add(pi);
    }

    private void readPolys()
    {

    }

    private String testPointImage(Point p)
    {
        String imageName = null;
        Iterator<PolyImage> i = this.imageMap.iterator();
        while( i.hasNext())
        {
            PolyImage pi = i.next();
            if( pi.containsPoint(p))
            {
                imageName = pi.getImageName();

                // System.out.println("Point was found in Polygon");
                log("Point was found in Polygon", GLogLevel.GUI_Trace);
                break;
            }
        }
        return imageName;
    }

    public HyperCanvas(Graphics gr, GUILogInterface logInterface, Image image)
    {
        this.gr = gr;
        mainImage = image;
        ImageIcon imIcon = new ImageIcon(image);        
        this.logInterface = logInterface;
        int w = image.getWidth(this);
        int h = image.getHeight(this);
        this.setBackground(Color.DARK_GRAY);



//        effectArea.addMouseListener(new java.awt.event.MouseAdapter()
//        {
//
//            public void mouseEntered(java.awt.event.MouseEvent evt)
//            {
//                hyperCanvasMouseEntered(evt);
//            }
//        });


    }

    public void setUseMode()
    {
        currMode = CanvasMode.Use;
    }

    public void setCreateMode()
    {
        currMode = CanvasMode.Create;
    }

    public void processPoint(MouseEvent evt)
    {
        recievedPoint = evt.getPoint();
        StringBuilder sb = new StringBuilder("Got point: [");
        sb.append(recievedPoint.x).append(", ").append(recievedPoint.y).append("]");
        log( sb.toString(), GLogLevel.Trace );
        String im = this.testPointImage(recievedPoint);
        if( im != null )
        {
            sb.append("\n Image: " + im );
        }
        else
            sb.append("\n Image: None Found...");
        this.pointStr = sb.toString();
        // log( pointStr, GLogLevel.Trace );

        refresh();

    }

    private void log(String msg, GLogLevel lvl)
    {
        logInterface.writeToGui(msg, lvl);
        // System.out.println(msg);
    }

    @Override
    public void update(Graphics g)
    {
        refresh();
    }

    @Override
    public void paint(Graphics g)
    {
        refresh();
    }

    public void refresh()
    {
        log("repaint called", GLogLevel.GUI_Trace);
        switch (currMode) {
        case Use:
            gr.drawImage(mainImage, 0, 0, this);
            break;


        case Create:
            gr.drawImage(mainImage, 0, 0, this);
            if( this.recievedPoint != null )
            {
                gr.setColor(Color.MAGENTA);
                gr.drawString(pointStr, 10, 10);
            }

            break;
        }
    }
}
