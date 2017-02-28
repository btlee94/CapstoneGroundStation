package com.capstone.groundstation;



import com.jcraft.jsch.*;
import java.io.*;
import java.util.*;

public class JavaSSH {
	
	private String address;
	private String user;
	private String pw;
	private String command;
	private String knownHosts;
	
	private String fileName;

	private BufferedReader in;
	
	private ChannelExec channel;
	
	private String dataReceived;
	
	private File file;
	
	private JSch jsch;
	
	
	public JavaSSH(String hostName, String userName, String password, String cmd, String hostsFP){
		address = hostName;
		user = userName;
		pw = password;
		command = cmd;
		knownHosts = hostsFP;
		
	}
	
	public void close(){
		channel.disconnect();
	}
	
	public void connect(){
		try{
			jsch = new JSch();
			jsch.setKnownHosts(knownHosts);
			
			Session session = jsch.getSession(user, address, 22);
			session.setPassword(pw);
			
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			
			session.connect();
			
			channel = (ChannelExec) session.openChannel("exec");
			in = new BufferedReader(new InputStreamReader(channel.getInputStream()));
			channel.setCommand(command);
			channel.connect();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public void createLogFile(){
		// Get date for file name
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH) + 1; // Note: zero based!
		int day = now.get(Calendar.DAY_OF_MONTH);
		int hour = now.get(Calendar.HOUR_OF_DAY);
		int minute = now.get(Calendar.MINUTE);
		int second = now.get(Calendar.SECOND);
		
		fileName = "logs/" + year + "-" + month + "-" + day + " " + hour + "-" + minute + "-" + second + ".txt"; // File to write to locally not on the server!!
		
		file = new File(fileName); // File instance
		// If file doesnt exists, then create it
		if (!file.exists()) {
			try{
			file.createNewFile();
			}
			catch (Exception ex){
				ex.printStackTrace();
			}
			
		}
	}
	
	public void fetchAnalyticData() {
		// Parse data being sent back
		try{
		while ((dataReceived = in.readLine()) != null) {
				ControlPage.analyticData = dataReceived;

		}
		}
		catch(Exception e){
			System.out.print("JScH Closed");
			close();
			
		}
		}

}
