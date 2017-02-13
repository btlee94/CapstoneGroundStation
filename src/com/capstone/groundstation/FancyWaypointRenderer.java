package com.capstone.groundstation;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointRenderer;

public class FancyWaypointRenderer implements WaypointRenderer<Waypoint> {
private static final Log log = LogFactory.getLog(FancyWaypointRenderer.class);


private BufferedImage img = null;

/**
 * Uses a default waypoint image
 */
public FancyWaypointRenderer(String image)
{
	try
	{
		if(image.equals("solo"))
		img = ImageIO.read(FancyWaypointRenderer.class.getResource("resources/solo.png"));
		else
		img = ImageIO.read(FancyWaypointRenderer.class.getResource("resources/waypoint_white.png"));
	}
	catch (Exception ex)
	{
		log.warn("couldn't read standard_waypoint.png", ex);
	}
}

public void paintWaypoint(Graphics2D g, JXMapViewer map, Waypoint w)
{
	if (img == null)
		return;

	Point2D point = map.getTileFactory().geoToPixel(w.getPosition(), map.getZoom());
	
	int x = (int)point.getX() -img.getWidth() / 2;
	int y = (int)point.getY() -img.getHeight();
	
	g.drawImage(img, x, y, null);
}
}
