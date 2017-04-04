package com.capstone.groundstation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;


public class Utilities {
		private static final String[] droneStatsScriptCmd = {"C:\\Python27\\python.exe", "scripts\\vehicleStats.py"};
		private static String[] flightScriptCmd = new String[8];
		public static Process statsProcess;
		public static Process flightProcess;

	/**
	 * Execute python scripts required for flight
	 * @throws IOException
	 */
	public static void executeLaunchScripts() throws IOException{

		ProcessBuilder pb1 = new ProcessBuilder(droneStatsScriptCmd);	
		statsProcess = pb1.start();
		
		//ProcessBuilder pb2 = new ProcessBuilder(flightScriptCmd);			
		//flightProcess = pb2.start();
	}
	
	public static InputStream getStatsInputStream(){
		return statsProcess.getInputStream();
	}
	
	public static InputStream getTestInputStream(){
		return flightProcess.getInputStream();
	}
	
	public static void buildFlightParamString(String alt, String rad, List<GeoPosition> waypoints){
		StringBuilder waypointArgs = new StringBuilder();
		
		waypointArgs.append("\"");	
		int i =0;
		for(GeoPosition wp : waypoints){
			if(i == 0){
				waypointArgs.append(wp.getLatitude() + " " + wp.getLongitude());
				i++;
			}
			else
				waypointArgs.append(" "+ wp.getLatitude() + " " + wp.getLongitude());
		}
		waypointArgs.append("\"");

		
		flightScriptCmd[0] = "C:\\Python27\\python.exe";
		flightScriptCmd[1] = "scripts\\flight.py";
		flightScriptCmd[2] = "--waypoints";
		flightScriptCmd[3] = waypointArgs.toString();
		flightScriptCmd[4] = "--altitude";
		flightScriptCmd[5] = alt;
		flightScriptCmd[6] = "--radius";
		flightScriptCmd[7] = rad;
	}
	
	public static void closeScriptProcesses(){
		statsProcess.destroy();
		//flightProcess.destroy();
	}
}
