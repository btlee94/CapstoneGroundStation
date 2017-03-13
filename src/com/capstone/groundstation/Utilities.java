package com.capstone.groundstation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;


public class Utilities {
		private static final String[] droneStatsScriptCmd = {"C:\\Python27\\python.exe", "scripts\\vehicleStats.py"};
		private static String[] flightScriptCmd = new String[5];
		private static String flightScriptParams;
		public static Process statsProcess;
		public static Process flightProcess;
		public static Process MAVproxyProcess;
		public static Process jsonServerProcess;

	/**
	 * Execute python scripts required for flight
	 * @throws IOException
	 */
	public static void executeLaunchScripts() throws IOException{

		ProcessBuilder pb1 = new ProcessBuilder(droneStatsScriptCmd);	//windows
		//ProcessBuilder pb1 = new ProcessBuilder("python", "scripts/vehicleStats.py");			//linux
		statsProcess = pb1.start();
		
		//ProcessBuilder pb2 = new ProcessBuilder(flightScriptCmd);												//windows
		//ProcessBuilder pb2 = new ProcessBuilder("python", "scripts/flight.py", flightScriptParams);				//linux
		//flightProcess = pb2.start();
	}
	
	public static InputStream getStatsInputStream(){
		return statsProcess.getInputStream();
	}
	
	public static void buildFlightParamString(String alt, String rad, List<GeoPosition> waypoints){
		StringBuilder waypointArgs = new StringBuilder();
		
		waypointArgs.append("waypoints \"");	
		int i =0;
		for(GeoPosition wp : waypoints){
			if(i == 0){
				waypointArgs.append(wp.getLatitude() + " " + wp.getLongitude());
				i++;
			}
			waypointArgs.append(" "+ wp.getLatitude() + " " + wp.getLongitude());
		}
		waypointArgs.append("\"");
		//args.append(" --altitude " + alt);
		//args.append(" --radius " + rad);
		
		//flightScriptParams = args.toString();
		
		flightScriptCmd[0] = "C:\\Python27\\python.exe";
		flightScriptCmd[1] = "scripts\\flight.py";
		flightScriptCmd[2] = waypointArgs.toString();
		flightScriptCmd[3] = "altitude " + alt;
		flightScriptCmd[4] = "radius " + rad;
	}
	
	public static void closeScriptProcesses(){
		statsProcess.destroy();
		//flightProcess.destroy();
	}
}
