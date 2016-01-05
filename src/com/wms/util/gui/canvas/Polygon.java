/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.wms.util.gui.canvas;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author mstemen
 */
public class Polygon implements List<Point>
{
    private ArrayList<Point> coordinates = new ArrayList<Point>();
    private Point closePoint = null;


    /**
     * The following excerpt is via:
     * PNPOLY - Point Inclusion in Polygon Test by W. Randolph Franklin (WRF)
     * http://www.ecse.rpi.edu/Homepages/wrf/Research/Short_Notes/pnpoly.html
    //   *
    //   Here is the code, for reference. Excluding lines with only braces, there are only 7 lines of code.
    //
    //    int pnpoly(int nvert, float *vertx, float *verty, float testx, float testy)
    //    {
    //    int i, j, c = 0;
    //    for (i = 0, j = nvert-1; i < nvert; j = i++) {
    //    if ( ((verty[i]>testy) != (verty[j]>testy)) &&
    //    (testx < (vertx[j]-vertx[i]) * (testy-verty[i]) / (verty[j]-verty[i]) + vertx[i]) )
    //    c = !c;
    //    }
    //    return c;
    //    }
    //
    //    Argument	Meaning
    //    nvert 	Number of vertices in the polygon. Whether to repeat the first vertex at the end is discussed below.
    //    vertx, verty 	Arrays containing the x- and y-coordinates of the polygon's vertices.
    //    testx, testy	X- and y-coordinate of the test point.
    //
    //
    //
     */

    // Polygon methods
    public boolean pointInPoly(Point testPoint)
    {
        boolean pointWasContained = false;
        int i, j, c = 0;
        int nvert = NumVert();
        double testx = testPoint.getX();
        double testy = testPoint.getY();

        for (i = 0, j = nvert - 1; i < nvert; j = i++) {
            double vertix = coordinates.get(i).getX();
            double vertiy = coordinates.get(i).getY();
            double vertjx = coordinates.get(j).getX();
            double vertjy = coordinates.get(j).getY();

            if (((vertiy > testy) != (vertjy > testy)) &&
                    (testx < (vertjx - vertix) * (testy - vertiy) / (vertjy - vertiy) + vertix)) {
                pointWasContained = !pointWasContained;
            }

        }

        return pointWasContained;
    }

    public void closePolygon()
    {
        // called to force the polygon closed
        // the first point added will be the close point
        closePoint = this.coordinates.get(0);
        this.coordinates.add(closePoint);
    }

    public boolean isClosed()
    {
        return closePoint != null;
    }

    public Point getClosePoint()
    {
        return closePoint;
    }

    // number of vertex in the polygon;
    public int NumVert()
    {
        return coordinates.size();
    }

    // Interface methods
    public int size()
    {
        return coordinates.size();
    }

    public boolean isEmpty()
    {
        return coordinates.isEmpty();
    }

    public boolean contains(Object arg0)
    {
        return coordinates.contains(arg0);
    }

    public Iterator<Point> iterator()
    {
        return coordinates.iterator();
    }

    public Object[] toArray()
    {
        return coordinates.toArray();
    }

    public <T> T[] toArray(T[] arg0)
    {
        return coordinates.toArray(arg0);
    }

    public boolean add(Point arg0)
    {
        return coordinates.add(arg0);
    }

    public boolean remove(Object arg0)
    {
        return coordinates.remove(arg0);
    }

    public boolean containsAll(Collection<?> arg0)
    {
        return coordinates.containsAll(arg0);
    }

    public boolean addAll(Collection<? extends Point> arg0)
    {
        return coordinates.addAll(arg0);
    }

    public boolean addAll(int arg0, Collection<? extends Point> arg1)
    {
        return coordinates.addAll(arg0, arg1);
    }

    public boolean removeAll(Collection<?> arg0)
    {
        return coordinates.removeAll(arg0);
    }

    public boolean retainAll(Collection<?> arg0)
    {
        return coordinates.retainAll(arg0);
    }

    public void clear()
    {
        coordinates.clear();
    }

    public Point get(int arg0)
    {
        return coordinates.get(arg0);
    }

    public Point set(int arg0, Point arg1)
    {
        return coordinates.set(arg0, arg1);
    }

    public void add(int arg0, Point arg1)
    {
        coordinates.add(arg0, arg1);
    }

    public Point remove(int arg0)
    {
        return coordinates.remove(arg0);
    }

    public int indexOf(Object arg0)
    {
        return coordinates.indexOf(arg0);
    }

    public int lastIndexOf(Object arg0)
    {
        return coordinates.lastIndexOf(arg0);
    }

    public ListIterator<Point> listIterator()
    {
        return coordinates.listIterator();
    }

    public ListIterator<Point> listIterator(int arg0)
    {
        return coordinates.listIterator(arg0);
    }

    public List<Point> subList(int arg0, int arg1)
    {
        return coordinates.subList(arg0, arg1);
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Point>points = this.iterator();
        while( points.hasNext())
        {
            Point p = points.next();
            sb.append("(").append(p.x).append(",").append(p.y).append(") ");
        }
        return sb.toString();
    }
}
