package com.capstone.groundstation;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.WaypointPainter;

public class WayPointManager {
	
	private Set<MyWaypoint> wps;
	private List<GeoPosition> track;
	private WaypointPainter<MyWaypoint> waypointPainter;
	private RoutePainter routePainter;
	private MyWaypoint wp;
	private List<Painter<JXMapViewer>> painters;
	private CompoundPainter<JXMapViewer> painter;
	
	private int numPoints = 1;
	
	

public WayPointManager(){
	
}

public void clearPoints(){
	wps.clear();
	waypointPainter.setWaypoints(wps);
	track.clear();
	
	numPoints = 1;
}

public void initPaint(GeoPosition loc){

	track = new ArrayList<GeoPosition>();
	
	wp = new MyWaypoint("blank", Color.BLUE, loc);

	wps = new HashSet<MyWaypoint>(Arrays.asList(new MyWaypoint("", Color.WHITE, loc)));
	
	waypointPainter = new WaypointPainter<MyWaypoint>();
	waypointPainter.setWaypoints(wps);
	waypointPainter.setRenderer(new FancyWaypointRenderer());
	
	painters = new ArrayList<Painter<JXMapViewer>>();
	routePainter = new RoutePainter(track);
	
	painters.add(routePainter);

	
	painters.add(waypointPainter);
	
	painter = new CompoundPainter<JXMapViewer>(painters);
}

public void addPoint(GeoPosition marker){

	wp = new MyWaypoint(Integer.toString(numPoints), Color.BLUE, marker); //Color not currently implemented
	wps.add(wp);
	track.add(marker);
	waypointPainter.setWaypoints(wps);
	
	numPoints++;
}
	

public CompoundPainter<JXMapViewer> getPainter(){
	return painter;
}
	
	

}
