/*
 * This file is part of CapstoneGroundStation.
 *
 * CapstoneGroundStation is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CapstoneGroundStation is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CapstoneGroundStation.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2016-2017 Brandon Lee, Veronica Eaton
 * 
 * CapstoneGroundStation makes use of JxBrowser (https://www.teamdev.com/jxbrowser)
 */
package com.capstone.groundstation;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
//import com.teamdev.jxbrowser.chromium.Browser;
//import com.teamdev.jxbrowser.chromium.internal.ipc.LatchUtil;
//import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.MouseInputAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
/**
 * 
 * @authors Veronica Eaton, Brandon Lee
 *
 */
public class SetupPage extends JFrame {
	private static final long serialVersionUID = 1L;
//	private Browser browser;
	private JTextArea waypointArea;
	private boolean bodyCount = false;
	private boolean vidOn = false;
	private int waypointNum = 1;
	private StringBuilder currentPoints = new StringBuilder();
	//private ArrayList<Waypoint> waypoints = new ArrayList<>();
	
	private JXMapKit jXMapKit;
	
	private TileFactoryInfo info;
	private DefaultTileFactory tileFactory;

	private WaypointPainter<Waypoint> waypointPainter;
	private List<Painter<JXMapViewer>> painters;
	private CompoundPainter<JXMapViewer> painter;
	private Set<Waypoint> wps;
	private DefaultWaypoint wp;
	

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(
					UIManager.getSystemLookAndFeelClassName());
					
					SetupPage frame = new SetupPage();
					//frame.setExtendedState(JFrame.MAXIMIZED_BOTH); 
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Construct the frame.
	 * TODO pass bodyCount and any other script parameters to control page as flags - control page will handle scripts
	 */
	public SetupPage() {
		super("Ground Station");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		
		//draw the window components
		createGUI();
		
		//initiate embedded maps application
		//mapApp();
	}
	
	/**
	 * Place elements in a border layout
	 */
	private void createGUI(){
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel featuresPanel = new JPanel();
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		
		buttonPanel.setPreferredSize(new Dimension(200,120));
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		
		featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
		
		JButton launchButton = new JButton("Launch Drone");
		launchButton.setPreferredSize(new Dimension(200, 60));
		launchButton.setFont(new Font("Tahoma", Font.PLAIN, 25));
		launchButton.setContentAreaFilled(false);
		launchButton.setBackground(new Color(0, 153, 255));
		launchButton.setForeground(Color.WHITE);
		launchButton.setOpaque(true);
		launchButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//don't do anything unless one of the check boxes has been selected
				if(!vidOn && !bodyCount)
					return;
				
				//close window and open control page
				dispose();
				
				ControlPage controlPage = new ControlPage(vidOn, bodyCount);
				controlPage.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				controlPage.setVisible(true);
			}
		});
		
		JButton clearPoints = new JButton("Clear Flags");
		clearPoints.setPreferredSize(new Dimension(200, 60));
		clearPoints.setFont(new Font("Tahoma", Font.PLAIN, 25));
		clearPoints.setContentAreaFilled(false);
		clearPoints.setBackground(new Color(69, 119, 198));
		clearPoints.setForeground(Color.WHITE);
		clearPoints.setOpaque(true);
		clearPoints.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				wps.clear();
				waypointPainter.setWaypoints(wps);
				
				waypointArea.setText("");
				currentPoints.setLength(0);
				waypointNum = 1;
				jXMapKit.getMainMap().repaint();
			}
		});
		
		
		
		JLabel label = new JLabel(" Drone Features");
		label.setFont(new Font("Tahoma", Font.PLAIN, 20));
		label.setForeground(new Color(0, 153, 255));

		final JCheckBox bodyCountBox = new JCheckBox("Body Count Analytics");
		bodyCountBox.setFont(new Font("Tahoma", Font.PLAIN, 15));
		bodyCountBox.setFocusPainted(false);
		
		final JCheckBox videoFeedBox = new JCheckBox("Live Video Feed");
		videoFeedBox.setFont(new Font("Tahoma", Font.PLAIN, 15));
		videoFeedBox.setFocusPainted(false);
		
		//action listeners for each checkbox - since we can't do anaytics and video simultaneously, only one can be selected at a time
		bodyCountBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(videoFeedBox.isSelected()){
					videoFeedBox.setSelected(false);
					vidOn = false;
				}
				
				bodyCount = true;
			}
		});
		
		
		videoFeedBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bodyCountBox.isSelected()){
					bodyCountBox.setSelected(false);
					bodyCount = false;
				}
				
				vidOn = true;
			}
		});
		
		
		waypointArea = new JTextArea();
		waypointArea.setEditable(false);
		waypointArea.setFont(new Font("Tahoma", Font.PLAIN, 15));
		waypointArea.setBackground(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(waypointArea);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		featuresPanel.add(label);
		featuresPanel.add(bodyCountBox);
		featuresPanel.add(videoFeedBox);
		
		leftPanel.add(featuresPanel, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		
		leftPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		buttonPanel.add(launchButton, BorderLayout.SOUTH);
		buttonPanel.add(clearPoints, BorderLayout.NORTH);
		
		
//		browser = new Browser();	
//		BrowserView browserView = new BrowserView(browser);
//		browserView.setMinimumSize(new Dimension(10, 60));
		
		jXMapKit = new JXMapKit();
		
		info = new OSMTileFactoryInfo();
		tileFactory = new DefaultTileFactory(info);
		jXMapKit.setTileFactory(tileFactory);
		
		tileFactory.setThreadPoolSize(8);
		
		GeoPosition UofC = new GeoPosition(51.079948, -114.125534);
		
		wp = new DefaultWaypoint(UofC);
		
		wps = new HashSet<Waypoint>(Arrays.asList(new DefaultWaypoint()));
		
		waypointPainter = new WaypointPainter<Waypoint>();
		waypointPainter.setWaypoints(wps);
		waypointPainter.setRenderer(new FancyWaypointRenderer(new String("white")));
		
		painters = new ArrayList<Painter<JXMapViewer>>();
		painters.add(waypointPainter);
		
		painter = new CompoundPainter<JXMapViewer>(painters);
		jXMapKit.getMainMap().setOverlayPainter(painter);
		
		jXMapKit.setZoom(3);
		jXMapKit.getMainMap().setAddressLocation(UofC);
		jXMapKit.setAddressLocationShown(false);
		
		rightPanel.add(jXMapKit);
		
		jXMapKit.getMainMap().addMouseListener(new MouseInputAdapter(){
			public void mouseClicked(MouseEvent e){
				Point point = e.getPoint();
				
				GeoPosition marker = jXMapKit.getMainMap().convertPointToGeoPosition(point);
				wp = new DefaultWaypoint(marker);
				wps.add(wp);
				
				waypointPainter.setWaypoints(wps);
				jXMapKit.getMainMap().repaint();
				
				DecimalFormat df = new DecimalFormat();
				df.setMaximumFractionDigits(5);
				
				String entry = System.lineSeparator() + 
						"Waypoint " + waypointNum + 
						System.lineSeparator() + 
						"Lon: " + df.format(marker.getLongitude()) + " "  +
						System.lineSeparator() +
						"Lat: " + df.format(marker.getLatitude()) + 
						System.lineSeparator();
				
				waypointNum++;
				
				currentPoints.append(entry);
				waypointArea.setText(currentPoints.toString());
			}
		});
		
		getContentPane();
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.CENTER);
//		add(browserView, BorderLayout.CENTER);
		pack();
		setSize(1280, 720);
	}
	
	/**
	 * TODO embed maps application
	 * this is only a stub method; chances are waypoint extraction and call to updateUI will be done from mapClickListener instead of here
	 */
	public void mapApp(){
//		browser.loadURL("https://www.google.ca/maps/"); //testing purposes only
		//test data to see what it looks like with multiple waypoints
		//TODO remove; add coordinates to waypoints list as Waypoint objects and call updateUI once after every update to the list
		updateUI();	
		updateUI();
		updateUI();
		updateUI();
		updateUI();
	}
	
	/**
	 * update screen with waypoint coordinates extracted from map
	 */
	public void updateUI(){
		/*******
		 * TODO this is for UI testing only; remove and use below code instead; delete waypointNum variable
		 */
		String entry = System.lineSeparator() + 
				"Waypoint " + waypointNum + 
				System.lineSeparator() + 
				"Lon: " + 51.077269 + " " +
				"Lat: " + -114.129303 + 
				System.lineSeparator();
		
		waypointNum++;
		
		/********
		 * TODO use this instead of above code
		 * might need to be modified as ive written this based on jxmapviewer documentation alone
		 *
		for(int i = 0; i < waypoints.size(); i++){
			String entry = System.lineSeparator() + 
					"Waypoint " + i + 
					System.lineSeparator() + 
					"Lon: " + waypoints.get(i).getPosition().getLatitude() + " " +
					"Lat: " + waypoints.get(i).getPosition().getLongitude() + 
					System.lineSeparator();
		}
		*/
		currentPoints.append(entry);
		waypointArea.setText(currentPoints.toString());
	}
}
