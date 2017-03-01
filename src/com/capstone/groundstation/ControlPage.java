package com.capstone.groundstation;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.awt.event.ActionEvent;

import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * This is the 'In-Flight' screen
 */
public class ControlPage extends JFrame {
	private static final long serialVersionUID = 1L;
	
	private boolean videoOn = false;
	private boolean objectDetect = false;
	
	
	//Responsible for displaying VLC player and .sdp stream
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	
	
	//Schedulers for managing threads reading from command line applications, and for SSH connection
	private ScheduledFuture<?> droneStatsHandler;
	private ScheduledFuture<?> droneLocHandler;
	private ScheduledFuture<?> vidAnalyticHandler;
	
	private JTextArea droneStats;
	private JLabel vidAnalytics;
	
	//Reads from command line application executed by python scripts
	private BufferedReader pyConsolInpt;
	
	//Python script execution strings
	private static final String pythonScriptPath_droneStats = "scripts\\droneStats.py";
	private static final String pythonExePath = "C:\\Python27\\python.exe ";
	private static final String videoFeedParamsPath = "sololink.sdp";
	private static String flightScriptCommand;
	
	//define patterns for parsing vehicleStats.py output
	private static final List<Pattern> regexs = Arrays.asList(
					Pattern.compile("Vehicle state:$"),
					Pattern.compile("Relative Altitude:\\s.*$"),
					Pattern.compile("Velocity:\\s.*$"),
					Pattern.compile("Battery\\sPercent:\\s.*$"),
					Pattern.compile("Groundspeed:\\s.*$"),
					Pattern.compile("Airspeed:\\s.*$"),
					Pattern.compile("Mode:\\s.*$"),
					Pattern.compile("Longitude:\\s.*$"),
					Pattern.compile("Latitude:\\s.*$"));
	
	private SoloMarkerManager smm;
	
	//Map Variables
	private JXMapKit mapViewer;
	private TileFactoryInfo info;
	private GeoPosition currLoc;
	private DefaultTileFactory tileFactory;
	
	//Temp long/lat variables to simulate movement on maps application
	private double cLong;
	private double cLat;
	
	//Holds the string displaying analytic data (eg., Object Not Detected, or Object Detected)
	public static String analyticData = "";
	
	//Will hold the drone's GPS coordinates fetched by the buffered reader
	private static double droneLong;
	private static double droneLat;
	
	private JavaSSH jsch;
	

	/**
	 * Construct the frame.
	 * @param vidOn is a flag for enabling live stream of drone camera
	 */
	public ControlPage(boolean vidOn, boolean objectDetect, String alt, String rad, List<GeoPosition> wp) {
		super("Ground Station");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				//cancel all active threads
				executeAbortSequence();
				System.exit(0);
			}
		});
		
		this.videoOn = vidOn;
		this.objectDetect = objectDetect;
		
		//prepare arguments for flight script
		buildFlightParamString(alt, rad, wp);
		
		if(videoOn){	//find libVLC
			boolean found = new NativeDiscovery().discover();
		}
		
		//draw the window components
		createGUI();	
		
		// execute launch python scripts
		try {
			executeLaunchScripts();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//initiate thread for reading drone stats and updating the window
		readDroneStats();	
	
		//initiate thread for update drone location on jxmapviewer2 component
		fetchDroneLoc();
		
		if(objectDetect){
			fetchVidAnalytics();
		}
		//initiate video feed if selected
		else
			playVideoFeed();	
	}
	
	/**
	 * Place elements in a border layout
	 * Map and video stream will be resizeable
	 */
	private void createGUI(){
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel statsPanel = new JPanel();
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel vidPanel = new JPanel(new BorderLayout());
		
		droneStats = new JTextArea();
		droneStats.setEditable(false);
		droneStats.setFont(new Font("Tahoma", Font.PLAIN, 15));
		droneStats.setBackground(null);
		droneStats.setRows(11);
		JScrollPane scrollPane  = new JScrollPane(droneStats);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		
		JButton abortButton = new JButton("Abort");
		abortButton.setPreferredSize(new Dimension(220, 60));
		abortButton.setFont(new Font("Tahoma", Font.PLAIN, 30));
		abortButton.setContentAreaFilled(false);
		abortButton.setBackground(new Color(213, 0, 0));
		abortButton.setForeground(Color.WHITE);
		abortButton.setOpaque(true);
		abortButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//cancel all active threads 
				executeAbortSequence();
				
				//close control page and return to setup page
				dispose();
				SetupPage setupPage = new SetupPage();
				//setupPage.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				setupPage.setVisible(true);	
			}
		});
		
		
		JLabel attributesLabel = new JLabel("Drone Attributes");
		attributesLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		attributesLabel.setForeground(new Color(0, 153, 255));
		
		statsPanel.add(attributesLabel);
		statsPanel.add(scrollPane);
		
		leftPanel.add(statsPanel, BorderLayout.NORTH);
		leftPanel.add(abortButton, BorderLayout.SOUTH);
			
		initializeMap();
		
		if(videoOn){
			splitPane.setDividerSize(10);
			splitPane.setResizeWeight(0.5);
			
			mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
			mediaPlayerComponent.setMinimumSize(new Dimension(10, 60));
			vidPanel.add(mediaPlayerComponent);
			
			splitPane.setBottomComponent(vidPanel);	
			splitPane.setTopComponent(mapViewer);
		}
		else{
			JLabel analyticsLabel = new JLabel("Video Analytics");
			analyticsLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
			analyticsLabel.setForeground(new Color(0, 153, 255));
			
			vidAnalytics = new JLabel();
			vidAnalytics.setFont(new Font("Tahoma", Font.PLAIN, 15));
			
			statsPanel.add(analyticsLabel);
			statsPanel.add(vidAnalytics);
			splitPane.setTopComponent(mapViewer);
		}
		
		
		getContentPane();
		add(leftPanel, BorderLayout.WEST);
		add(splitPane, BorderLayout.CENTER);
		pack();
		setSize(1280, 720);
	}
	
	
	private void initializeMap(){
		
		mapViewer = new JXMapKit();
		
		smm = new SoloMarkerManager();
		
		info = new OSMTileFactoryInfo();
		tileFactory = new DefaultTileFactory(info);
		mapViewer.setTileFactory(tileFactory);
		
		tileFactory.setThreadPoolSize(8);
		
		cLong = 51.079948;
		cLat = -114.125534;
		currLoc = new GeoPosition(cLong, cLat);
		
		smm.init(currLoc);
		
		mapViewer.getMainMap().setOverlayPainter(smm.getPainter());
		
		mapViewer.setZoom(3);
		mapViewer.setAddressLocation(currLoc);
		
	}
	
	/**
	 * Initiate thread for updating drone stats every 'updateInterval' seconds
	 */
	private void readDroneStats(){
		int reRunInterval = 2;	// seconds
		final Runnable statsUpdater = new Runnable() {
			public void run() { updateUI(); }
		};
		
		
		//update drone stats in real time (speed depends on python script) with initial delay of 3 seconds to allow drone to arm and launch
		//if thread exists before mission complete, re launch thread after 'reRunInterval' seconds
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		droneStatsHandler = scheduler.scheduleAtFixedRate(statsUpdater, 3, reRunInterval, TimeUnit.SECONDS);
	}
	
	private void fetchDroneLoc(){
		int reRunInterval = 4;
		final Runnable statsUpdater2 = new Runnable() {
			public void run() { updateMap(); }
		};
		
		final ScheduledExecutorService scheduler2 = Executors.newScheduledThreadPool(1);
		droneLocHandler = scheduler2.scheduleAtFixedRate(statsUpdater2, 3, reRunInterval, TimeUnit.SECONDS);
	}
	
	private void updateMap(){
		cLong += 0.0001;
		cLat += 0.0001;
		
		//if a GPS lock exists, use the drone's GPS coordinates
		if(droneLong != 0 && droneLat != 0)
			currLoc = new GeoPosition(droneLong, droneLat);
		else
			currLoc = new GeoPosition(cLong, cLat);
		
		smm.update(currLoc);
		
		mapViewer.setAddressLocation(currLoc);
		
	}
	
	private void fetchVidAnalytics(){
		
		String hostName = "10.1.1.10";
		String username = "root";
		String password = "TjSDBkAu";
		String cmd = "python scripts/opencv/colorDetect.py";
		String knownHosts = "~/.ssh/known_hosts";
		
		
		jsch = new JavaSSH(hostName,username,password,cmd,knownHosts);
		jsch.connect();
		//jsch.createLogFile();
		final Runnable statsUpdater3 = new Runnable() {
			public void run() { try {
				updateAnalytics(jsch);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} }
		};
		
		final ScheduledExecutorService scheduler3 = Executors.newScheduledThreadPool(1);
		vidAnalyticHandler = scheduler3.schedule(statsUpdater3, 1, TimeUnit.SECONDS);
	}
	
	private void updateAnalytics(JavaSSH jsch) throws IOException{
		jsch.fetchAnalyticData();
	}
	
	/**
	 * Read current values of drones attributes from vehicleStats.py and update textView
	 * NOTE: Never done regular expression before so if there is a better way to do this please fix
	 */
	private void updateUI(){
		
		// read the output from vehicleStats.py and update screen
		StringBuilder attributes = new StringBuilder();
		String line = "";
		try {
			while((line = pyConsolInpt.readLine()) != null) {
				/*for(Pattern regex : regexs){
					if(regex.matcher(line).matches()){
						attributes.append(line);
						attributes.append(System.lineSeparator());
						droneStats.setText(attributes.toString());
						
						if(line.contains("Longitude")){
							droneLong = Double.parseDouble(line.substring(11));
						}
						if(line.contains("Latitude")){
							droneLat = Double.parseDouble(line.substring(10));							
						}
						//reset StringBuilder when we know output will start looping
						if(line.contains("Mode: "))
							attributes.setLength(0);
					}
				}
				*/
				attributes.append(line);
				attributes.append(System.lineSeparator());
				droneStats.setText(attributes.toString());
				
				if(line.contains("Longitude"))
					droneLong = Double.parseDouble(line.substring(11));
				if(line.contains("Latitude"))
					droneLat = Double.parseDouble(line.substring(10));
				if(line.contains("Mode"))
					attributes.setLength(0);

				
				if(!videoOn)
					vidAnalytics.setText(analyticData);	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute python scripts required for flight
	 * @throws IOException
	 */
	private void executeLaunchScripts() throws IOException{
		//String[] cmd = new String[2];
		//cmd[0] = pythonExePath;	//path to python.exe
		//cmd[1] = pythonScriptPath_droneStats;	//path to python script
		
		// create runtime to execute python scripts
		Runtime rt = Runtime.getRuntime();
		Process statsProcess = rt.exec("python scripts/vehicleStats.py");
		 
		// initialize input stream - this will be used to read output from python scripts
		pyConsolInpt = new BufferedReader(new InputStreamReader(statsProcess.getInputStream()));
		
		Process flightProcess = rt.exec(flightScriptCommand);
	}
	
	/**
	 * Cancel flight and return to home
	 */
	private void executeAbortSequence(){
		if(videoOn)
			mediaPlayerComponent.release();
		if(objectDetect){
			vidAnalyticHandler.cancel(true);

			if(jsch!=null)
			jsch.close();
		}
		droneStatsHandler.cancel(true);
		droneLocHandler.cancel(true);
	}
	
	private void buildFlightParamString(String alt, String rad, List<GeoPosition> waypoints){
		StringBuilder args = new StringBuilder();
		
		args.append("python scripts/flight.py --waypoints");
		for(GeoPosition wp : waypoints)
			args.append(" " + wp.getLongitude() + " " + wp.getLatitude());
		args.append(" --altitude " + alt);
		args.append(" --radius " + rad);
		
		flightScriptCommand = args.toString();
		
		System.out.println(flightScriptCommand);
	}
	
	/**
	 * Play video feed from drone
	 */
	private void playVideoFeed(){
		mediaPlayerComponent.getMediaPlayer().playMedia(videoFeedParamsPath);
	}
}
