/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util.gui.canvas;

import java.awt.Image;
import java.awt.Point;

/**
 *
 * @author mstemen
 */
public class PolyImage {


    private Image image;
    private Polygon polygon;
    private String name;
    private String imageName;
    private Point closePoint;

    public PolyImage(String name, Polygon polygon)
    {
        this.name = name;
        this.polygon = polygon;
    }

    /**
     * @return the image
     */
    public boolean containsPoint(Point testPoint)
    {
        return polygon.pointInPoly(testPoint);
    }

    public void closePolygon()
    {
        // we will use the last point added as the close point
        this.polygon.closePolygon();
    }
    public Image getImage()
    {
        return image;
    }

    public String getImageName()
    {
        return imageName;
    }

    public void setImage(Image image)
    {
        this.image = image;
    }

    public void setImageName(String imageName)
    {
        this.imageName = imageName;
    }

    /**
     * @return the polygon
     */
    public Polygon getPolygon()
    {
        return polygon;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Image name: ").append(this.imageName).append(" Points: ").append(this.polygon.toString());
        return sb.toString();
    }

}
