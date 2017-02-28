package com.capstone.groundstation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;

public class SoloMarkerManager {
	
	private WaypointPainter<Waypoint> waypointPainter;
	private List<Painter<JXMapViewer>> painters;
	private CompoundPainter<JXMapViewer> painter;
	private Set<Waypoint> waypoints;
	private DefaultWaypoint wp;
	
	public SoloMarkerManager(){
		
	}
	
	
	public void init(GeoPosition currLoc){
		
		wp = new DefaultWaypoint(currLoc);
		
		waypoints = new HashSet<Waypoint>(Arrays.asList(wp));
		
		waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(waypoints);
		waypointPainter.setRenderer(new SoloWayPointRenderer());
		
		painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(waypointPainter);
		
		painter = new CompoundPainter<JXMapViewer>(painters);
		
	}
	
	public CompoundPainter<JXMapViewer> getPainter(){
		return painter;
	}
	
	public void update(GeoPosition currLoc){
		
		wp = new DefaultWaypoint(currLoc);
		
		waypoints.clear();
		waypoints.add(wp);
		
		waypointPainter.setWaypoints(waypoints);
	}

}
