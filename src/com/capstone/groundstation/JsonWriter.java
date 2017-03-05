package com.capstone.groundstation;

import java.io.File;
import java.io.FileWriter;

import org.json.JSONObject;

public class JsonWriter {
	
	private String filePath;
	
	public JsonWriter(String path){
		filePath = path;
	}
	
	public void update(){
		File file = new File(filePath);
		
		JSONObject droneStats;
		JSONObject analytics;
		JSONObject main;
		
		while(true){
			
			droneStats = new JSONObject()
			.put("battery", Stats.battery)
			.put("velocity", Stats.velocity)
			.put("longitude", Stats.longitude)
			.put("latitude", Stats.latitude)
			.put("relAltitude", Stats.relAltitude)
			.put("vehicleState", Stats.vehicleState)
			.put("heading", Stats.heading)
			.put("homeLongitude", Stats.homeLong)
			.put("homeLatitude", Stats.homeLat)
			.put("targetLongitude", Stats.targLong)
			.put("targetLatitude", Stats.targLat);
			
			analytics = new JSONObject()
			.put("flag",Stats.analytics);
			
			main = new JSONObject()
			.put("analytics", analytics)
			.put("droneStats", droneStats);
			
			try(FileWriter fw = new FileWriter(file)){
				fw.write(main.toString());
				fw.close();
				Thread.sleep(1*3000);
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
		
	}

}
