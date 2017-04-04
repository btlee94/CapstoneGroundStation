package com.capstone.groundstation;

import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
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
	private ScheduledFuture<?> jsonHandler;
	
	private JTextArea droneStats;
	private JLabel vidAnalytics;
	
	//Reads from command line application executed by python scripts
	private BufferedReader droneStatsReader;
	
	//json file path
	private static final String jsonPath = "droneJsonServer\\public\\drone.json";
	
	private static final String videoFeedParamsPath = "sololink.sdp";
	
	private SoloMarkerManager smm;
	
	//Map Variables
	private JXMapKit mapViewer;
	private TileFactoryInfo info;
	private GeoPosition currLoc;
	private DefaultTileFactory tileFactory;
	
	//Temp long/lat variables to simulate movement on maps application
	private double cLong = -114.12997; 
	private double cLat = 51.08037;

	
	private JavaSSH jsch;
	

	/**
	 * Construct the frame.
	 * @param vidOn is a flag for enabling live stream of drone camera
	 */
	public ControlPage(boolean vidOn, boolean objectDetect) {
		super("Ground Station");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				//cancel all active threads
				executeAbortSequence();
				Utilities.closeScriptProcesses();
				System.exit(0);
			}
		});
		
		this.videoOn = vidOn;
		this.objectDetect = objectDetect;
		
		//find libVLC
		boolean found = new NativeDiscovery().discover();
		
		//draw the window components
		createGUI();	
		
		// execute launch python scripts
		try {
			Utilities.executeLaunchScripts();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//initiate thread for reading drone stats and updating the window
		readDroneStats();	
	
		//initiate thread for update drone location on jxmapviewer2 component
		fetchDroneLoc();
		
		//initiate thread for updating JSON file with analytics and drone stats
		runUpdateJson();
		
		
		if(objectDetect){
			fetchVidAnalytics();
		}
		//initiate video feed if selected
		else{
			if(found)
				playVideoFeed();
		}	
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
		droneStats.setRows(15);
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
				Utilities.closeScriptProcesses();
				
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
		

		currLoc = new GeoPosition(cLat, cLong);
		
		smm.init(currLoc);
		
		mapViewer.getMainMap().setOverlayPainter(smm.getPainter());
		
		mapViewer.setZoom(3);
		mapViewer.setAddressLocation(currLoc);
		
	}
	

	private void readDroneStats(){
		final Runnable statsUpdater = new Runnable() {
			public void run() { try {
				updateUI();
			} catch (IOException e) {
				e.printStackTrace();
			} }
		};
		
		final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		droneStatsHandler = scheduler.schedule(statsUpdater, 1, TimeUnit.SECONDS);
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
		
		double longitude = Double.parseDouble(Stats.longitude);
		double lat = Double.parseDouble(Stats.latitude);
		
		//if a GPS lock exists, use the drone's GPS coordinates
		if(longitude != 0 && lat != 0)
			currLoc = new GeoPosition(lat, longitude);
		else
			currLoc = new GeoPosition(cLat, cLong);
		
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
	
	private void runUpdateJson(){		
		final JsonWriter jw = new JsonWriter(jsonPath);
		
		final Runnable statsUpdater4 = new Runnable() {
			public void run() {
				try{
					updateJson(jw);
				}
				catch(Exception e){
					
				}
			}
		};
		
		final ScheduledExecutorService scheduler4 = Executors.newScheduledThreadPool(1);
		jsonHandler = scheduler4.schedule(statsUpdater4, 1, TimeUnit.SECONDS);
		
	}
	
	private void updateJson(JsonWriter jw){
		jw.update();
	}
	
	/**
	 * Read current values of drones attributes from vehicleStats.py and update textView
	 * @throws IOException 
	 */
	private void updateUI() throws IOException{
		// read the output from vehicleStats.py and update screen
		droneStatsReader = new BufferedReader(new InputStreamReader(Utilities.getStatsInputStream()));
		StringBuilder attributes = new StringBuilder();
		String line = "";
		try {
			while((line = droneStatsReader.readLine()) != null) {
				if(line.equals("end")){
					attributes.setLength(0);
					continue;
				}
				attributes.append(line);
				attributes.append("\n");
				droneStats.setText(attributes.toString());
				
				if(line.contains("Altitude"))
					Stats.relAltitude = line.substring(19);
				else if(line.contains("Battery"))
					Stats.battery = line.substring(17);
				else if(line.contains("Groundspeed"))
					Stats.grndSpeed = line.substring(13);
				else if(line.contains("Heading"))
					Stats.heading = line.substring(9);
				else if(line.contains("Mode"))
					Stats.vehicleMode = line.substring(6);
				else if(line.contains("Longitude")){
					if(line.contains("Home"))
						Stats.homeLong = line.substring(16);
					else
						Stats.longitude = line.substring(11);
				}
				else if(line.contains("Latitude")){
					if(line.contains("Home"))
						Stats.homeLat = line.substring(15);
					else
						Stats.latitude = line.substring(10);
				}
				if(objectDetect)
					vidAnalytics.setText(Stats.analytics);	
			}
		} catch (IOException e) {
			droneStatsReader.close();
		}	
	}
	
	
	/**
	 * Cancel flight and return to home
	 */
	private void executeAbortSequence(){
		if(videoOn)
			mediaPlayerComponent.release();
		else
			vidAnalyticHandler.cancel(true);
		
		droneStatsHandler.cancel(true);
		droneLocHandler.cancel(true);
		jsonHandler.cancel(true);
		try {
			droneStatsReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Play video feed from drone
	 */
	private void playVideoFeed(){
		mediaPlayerComponent.getMediaPlayer().playMedia(videoFeedParamsPath);
	}
}
