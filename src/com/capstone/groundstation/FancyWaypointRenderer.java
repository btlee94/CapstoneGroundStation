package com.capstone.groundstation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.WaypointRenderer;

public class FancyWaypointRenderer implements WaypointRenderer<MyWaypoint> {
private static final Log log = LogFactory.getLog(FancyWaypointRenderer.class);


private BufferedImage img = null;


public FancyWaypointRenderer()
{
	try
	{
		img = ImageIO.read(FancyWaypointRenderer.class.getResource("/waypoint_white.png"));
	}
	catch (Exception ex)
	{
		log.warn("couldn't read waypoint_white.png", ex);
	}
}

public void paintWaypoint(Graphics2D g, JXMapViewer map, MyWaypoint w)
{
	String label = w.getLabel();

	if (img == null || label == "")
		return;

	Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
	
	int x = (int)point.getX() -img.getWidth() / 2;
	int y = (int)point.getY() -img.getHeight();
	
	g.drawImage(img, x, y, null);
	
	g.setColor(Color.BLACK);
	FontMetrics metrics = g.getFontMetrics();
	int tw = metrics.stringWidth(label);
	int th = metrics.getHeight();
	
	g.drawString(label, x+img.getWidth()/2-tw/2, y+img.getHeight()/2-th/4);

}

}