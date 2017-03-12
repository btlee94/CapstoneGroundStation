package com.capstone.groundstation;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.jxmapviewer.viewer.GeoPosition;


public class Utilities {
		private static final String pythonScriptPath_droneStats = "scripts\\vehicleStats.py";
		private static final String pythonScriptPath_droneFlight = "scripts\\flight.py";
		private static final String pythonExePath = "C:\\Python27\\python.exe ";
		private static final String MAVproxyPath = "C:\\Program Files (x86)\\MAVProxy\\mavproxy.exe ";
		private static final String MAVproxyParams = "master=udpin:0.0.0.0:14550 --out=udpout:127.0.0.1:14552 --out=udpout:127.0.0.1:14549 --out=udpout:127.0.0.1:14555";
		private static final String nodeExePath = "C:\\Program Files\\nodejs\\node.exe";
		private static final String JSONServer = "droneJsonServer\\index.js";
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

		ProcessBuilder pb1 = new ProcessBuilder(pythonExePath, pythonScriptPath_droneStats);	//windows
		//ProcessBuilder pb1 = new ProcessBuilder("python", "scripts/vehicleStats.py");			//linux
		statsProcess = pb1.start();
		
		ProcessBuilder pb2 = new ProcessBuilder(pythonExePath, pythonScriptPath_droneFlight, flightScriptParams);	//windows
		//ProcessBuilder pb2 = new ProcessBuilder("python", "scripts/flight.py", flightScriptParams);				//linux
		flightProcess = pb2.start();
		
		System.out.println(pythonExePath + pythonScriptPath_droneFlight + flightScriptParams);
	}
	
	public static void launchMAVproxy() throws IOException{
		ProcessBuilder pb = new ProcessBuilder(MAVproxyPath, MAVproxyParams);	//windows
		//ProcessBuilder pb = new ProcessBuilder(mavproxy, MAVproxyParams);	//linux
		MAVproxyProcess = pb.start();
		
		System.out.println(MAVproxyPath + MAVproxyParams);
	}
	
	public static void launchJSONServer() throws IOException{
		ProcessBuilder pb = new ProcessBuilder(nodeExePath, JSONServer);				//windows
		//ProcessBuilder pb = new ProcessBuilder("node", "droneJsonServer/index.js");	//linux
		jsonServerProcess = pb.start();
	}
	
	public static InputStream getStatsInputStream(){
		return statsProcess.getInputStream();
	}
	
	public static void buildFlightParamString(String alt, String rad, List<GeoPosition> waypoints){
		StringBuilder args = new StringBuilder();
		
		args.append(" waypoints \"");	
		int i =0;
		for(GeoPosition wp : waypoints){
			if(i == 0){
				args.append(wp.getLatitude() + " " + wp.getLongitude());
				i++;
			}
			args.append(" "+ wp.getLatitude() + " " + wp.getLongitude());
		}
		args.append("\"");
		args.append(" --altitude " + alt);
		args.append(" --radius " + rad);
		
		flightScriptParams = args.toString();
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
