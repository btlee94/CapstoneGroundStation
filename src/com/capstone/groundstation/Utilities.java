package com.capstone.groundstation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;


public class Utilities {
		private static final String[] MAVproxyCmd = {"C:\\Program Files (x86)\\MAVProxy\\mavproxy.exe", "master=udpin:0.0.0.0:14550", "out=udpout:127.0.0.1:14552", "out=udpout:127.0.0.1:14549", "out=udpout:127.0.0.1:14555"};
		private static final String[] JSONServerCmd = {"C:\\Program Files\\nodejs\\node.exe", "droneJsonServer\\index.js"};
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
		
		ProcessBuilder pb2 = new ProcessBuilder(flightScriptCmd);	//windows
		//ProcessBuilder pb2 = new ProcessBuilder("python", "scripts/flight.py", flightScriptParams);				//linux
		flightProcess = pb2.start();
	}
	
	public static void launchMAVproxy() throws IOException{
<<<<<<< HEAD
		ProcessBuilder pb = new ProcessBuilder(MAVproxyCmd);	//windows
		//ProcessBuilder pb = new ProcessBuilder(mavproxy, MAVproxyParams);	//linux
=======
		ProcessBuilder pb = new ProcessBuilder(MAVproxyPath, MAVproxyParams);	//windows
		//ProcessBuilder pb1 = new ProcessBuilder(mavproxy, MAVproxyParams);	//linux
>>>>>>> parent of 73bab90... fix
		MAVproxyProcess = pb.start();
	}
	
	public static void launchJSONServer() throws IOException{
<<<<<<< HEAD
		ProcessBuilder pb = new ProcessBuilder(JSONServerCmd);				//windows
		//ProcessBuilder pb = new ProcessBuilder("node", "droneJsonServer/index.js");	//linux
=======
		ProcessBuilder pb = new ProcessBuilder(nodeExePath, JSONServer);				//windows
		//ProcessBuilder pb1 = new ProcessBuilder("node", "droneJsonServer/index.js");	//linux
>>>>>>> parent of 73bab90... fix
		jsonServerProcess = pb.start();
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
		flightProcess.destroy();
	}
	
	public static void closeCLIProcesses(){
		MAVproxyProcess.destroy();
		jsonServerProcess.destroy();
	}
}
