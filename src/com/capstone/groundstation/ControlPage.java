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

import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.component.EmbeddedMediaPlayerComponent;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.swing.*;
import java.awt.*;

/**
 * This is the 'In-Flight' screen
 *
 * @author Brandon Lee
 *
 */
public class ControlPage extends JFrame {
	private boolean videoOn = false;
	private EmbeddedMediaPlayerComponent mediaPlayerComponent;
	private ScheduledFuture<?> droneStatsHandler;
	private JTextArea droneStats;
	private BufferedReader pyConsolInpt;
	//define path to python.exe and any scripts that need to be run
	//TODO be aware of path differences between Windows and Linux; Win = \\ Linux = /
	private static final String pythonScriptPath_droneStats = "scripts\\droneStats.py";
	private static final String pythonExePath = "C:\\Python27\\python.exe ";
	//define patterns for parsing vehicleStats.py output
	//TODO these patterns look for a number and then end of line; likely need to change assuming there are units after the numbers
	private static final List<Pattern> regexs = Arrays.asList(
					Pattern.compile("Vehicle state:$"),
					Pattern.compile("\\sRelative\\sAltitude:\\s\\d*$"),
					Pattern.compile("\\sVelocity:\\s\\d*$"),
					Pattern.compile("\\sBattery\\sPercent:\\s\\d*$"),
					Pattern.compile("\\sGroundspeed:\\s\\d*$"),
					Pattern.compile("\\sAirspeed:\\s\\d*$"),
					Pattern.compile("\\sMode:\\s\\d*$"));
	
	/**
	 * Launch the application.
	 * TODO testing purposes only, remove this main method after complete integration
	 */
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					 UIManager.setLookAndFeel(
					 UIManager.getSystemLookAndFeelClassName());
					    
					ControlPage frame = new ControlPage(true);
					frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Construct the frame.
	 * @param vidOn is a flag for enabling live stream of drone camera
	 */
	public ControlPage(boolean vidOn) {
		super("Ground Station");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				if(videoOn)
					mediaPlayerComponent.release();
				droneStatsHandler.cancel(true);
				System.exit(0);
			}
		});
		
		this.videoOn = vidOn;
		if(videoOn){	//find libVLC
			boolean found = new NativeDiscovery().discover();
	        System.out.println(found);
	        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
		}
		
		
		createGUI();
		try {
			executeLaunchScripts();	// execute necessary python scripts
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		readDroneStats();
		if(videoOn)
			playVideoFeed();
	}
	
	/**
	 * Place elements in a border layout
	 * Map and video stream will be resizeable
	 */
	private void createGUI(){
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel vidPanel = new JPanel(new BorderLayout());
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		JScrollPane scrollPane = new JScrollPane();
		droneStats = new JTextArea();
		droneStats.setEditable(false);
		droneStats.setFont(new Font("Tahoma", Font.PLAIN, 15));
		scrollPane.setViewportView(droneStats);
		
		JButton abortButton = new JButton("Abort");
		abortButton.setPreferredSize(new Dimension(200, 60));
		abortButton.setFont(new Font("Tahoma", Font.PLAIN, 30));
		abortButton.setContentAreaFilled(false);
		abortButton.setBackground(new Color(213, 0, 0));
		abortButton.setForeground(Color.WHITE);
		abortButton.setOpaque(true);
		abortButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(videoOn)
					mediaPlayerComponent.release();
				droneStatsHandler.cancel(true);
				//executeAbortScripts();
				
				//close control page and return to setup page
				dispose();
				SetupPage setupPage = new SetupPage();
				//setupPage.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				setupPage.setVisible(true);
				
			}
		});
		
		JLabel label = new JLabel(" Drone Attributes");
		label.setFont(new Font("Tahoma", Font.PLAIN, 20));
		label.setForeground(new Color(0, 153, 255));
		
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		leftPanel.add(abortButton, BorderLayout.SOUTH);
		leftPanel.add(label, BorderLayout.NORTH);
		
		
		final Browser browser = new Browser();
		browser.loadURL("https://www.bing.com/maps/");	//Test purposes only; replace with URL for tower app
		BrowserView browserView = new BrowserView(browser);
		browserView.setMinimumSize(new Dimension(10, 60));
		
		
		splitPane.setTopComponent(browserView);
		
		if(videoOn){
			mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
			mediaPlayerComponent.setMinimumSize(new Dimension(10, 60));
			vidPanel.add(mediaPlayerComponent);
			splitPane.setBottomComponent(vidPanel);	
			splitPane.setResizeWeight(0.5);
		}
		
		
		getContentPane();
		add(leftPanel, BorderLayout.WEST);
		add(splitPane, BorderLayout.CENTER);
		pack();
		setSize(1280, 720);
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
	
	/**
	 * Read current values of drones attributes from vehicleStats.py and update textView
	 * NOTE: Never done regular expression before so if there is a better way to do this please fix
	 * TODO include body count updates here - as of now, not sure where that value will come from 
	 */
	private void updateUI(){
		
		// read the output from vehicleStats.py and update screen
		StringBuilder attributes = new StringBuilder();
		String line = "";
		try {
			while((line = pyConsolInpt.readLine()) != null) {
				for(Pattern regex : regexs){
					if(regex.matcher(line).matches()){
						attributes.append(line);
						attributes.append(System.lineSeparator());
						droneStats.setText(attributes.toString());
						
						//reset StringBuilder when we know output will start looping
						if(line.contains("Mode: "))
							attributes.setLength(0);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Execute python scripts required for flight
	 * TODO add Simon's scripts through SSH here
	 * @throws IOException
	 */
	private void executeLaunchScripts() throws IOException{
		String[] cmd = new String[2];
		cmd[0] = pythonExePath;	//path to python.exe
		cmd[1] = pythonScriptPath_droneStats;	//path to python script
		
		// create runtime to execute python scripts
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);
		 
		// initialize input stream - this will be used to read output from python scripts
		pyConsolInpt = new BufferedReader(new InputStreamReader(pr.getInputStream()));
	}
	
	/**
	 * Execute python scripts required for a ReturnToHome
	 * NOTE: not sure if this is needed - will leave anyway
	 * @throws IOException
	 */
	private void executeAbortScripts() throws IOException{
		String[] cmd = new String[2];
		cmd[0] = pythonExePath;
		cmd[1] = "INSERT PATH TO SCRIPTS";
		
		// create runtime to execute python scripts
		Runtime rt = Runtime.getRuntime();
		Process pr = rt.exec(cmd);
	}
	
	/**
	 * Play video feed from drone
	 */
	private void playVideoFeed(){
		mediaPlayerComponent.getMediaPlayer().playMedia("droneFootage.mkv");	//testing purposes only
		
		/***********
		 * I dont know the specifics of our video stream so i cant implement or test it but heres a link to a tutorial for streaming RTP
		 * https://github.com/caprica/vlcj/blob/master/src/test/java/uk/co/caprica/vlcj/test/streaming/StreamRtp.java
		 */
	}
}
