package com.capstone.groundstation;

import java.awt.EventQueue;
import java.awt.FlowLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.event.MouseInputAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

/**
 * This is the pre-flight drone setup page
 */
public class SetupPage extends JFrame {
	private static final long serialVersionUID = 1L;
	private JTextArea waypointArea;
	private JTextField radiusField;
	private JTextField altitudeField;
	private boolean objectDetect = false;
	private boolean videoOn = false;
	private int waypointNum = 1;
	private StringBuilder currentPoints = new StringBuilder();
	
	//Map variables
	private JXMapKit jXMapKit;
	private TileFactoryInfo info;
	private DefaultTileFactory tileFactory;
	
	//Manages Waypoint Markers
	private WayPointManager wpm;
	

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
	 */
	public SetupPage() {
		super("Ground Station");
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e){
				System.exit(0);
			}
		});
		
		//draw the window components
		createGUI();
	}
	
	/**
	 * Place elements in a border layout
	 */
	private void createGUI(){
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel featuresPanel = new JPanel();
		featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
		
		JPanel buttonPanel = new JPanel(new BorderLayout());
		buttonPanel.setPreferredSize(new Dimension(200,120));
		
		JPanel mapPanel = new JPanel(new BorderLayout());
		
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
				if(!videoOn && !objectDetect)
					return;
				//don't do anything if user hasn't specified an altitude and a radius
				if(altitudeField.getText().equals("") || radiusField.getText().equals(""))
					return;
				
				//set parameters needed for flight script
				setFlightParams();
				
				//close window and open control page
				dispose();
				
				ControlPage controlPage = new ControlPage(videoOn, objectDetect);
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
				
				wpm.clearPoints();
				
				waypointArea.setText("");
				currentPoints.setLength(0);
				waypointNum = 1;
				jXMapKit.getMainMap().repaint();
			}
		});
		
		
		
		JLabel featuresLabel = new JLabel("Drone Features");
		featuresLabel.setFont(new Font("Tahoma", Font.PLAIN, 20));
		featuresLabel.setForeground(new Color(0, 153, 255));

		final JCheckBox bodyCountBox = new JCheckBox("Object Detection");
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
					videoOn = false;
				}
				
				objectDetect = true;
			}
		});
		
		
		videoFeedBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(bodyCountBox.isSelected()){
					bodyCountBox.setSelected(false);
					objectDetect = false;
				}
				
				videoOn = true;
			}
		});
		
		
		JLabel altitudeLabel = new JLabel("Set Relative Altitude");
		altitudeLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		altitudeField = new JTextField();
		altitudeField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		//altitude.setBorder(BorderFactory.createEmptyBorder());
		
		JLabel radiusLabel = new JLabel("Set Radius");
		radiusLabel.setFont(new Font("Tahoma", Font.PLAIN, 15));
		radiusField = new JTextField();
		radiusField.setFont(new Font("Tahoma", Font.PLAIN, 15));
		//radius.setBorder(BorderFactory.createEmptyBorder());
		
		
		waypointArea = new JTextArea();
		waypointArea.setEditable(false);
		waypointArea.setFont(new Font("Tahoma", Font.PLAIN, 15));
		waypointArea.setBackground(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportView(waypointArea);
		
		
		featuresPanel.add(featuresLabel);
		featuresPanel.add(bodyCountBox);
		featuresPanel.add(videoFeedBox);
		featuresPanel.add(altitudeLabel);
		featuresPanel.add(altitudeField);
		featuresPanel.add(radiusLabel);
		featuresPanel.add(radiusField);

		buttonPanel.add(launchButton, BorderLayout.SOUTH);
		buttonPanel.add(clearPoints, BorderLayout.NORTH);
		
		leftPanel.add(featuresPanel, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		leftPanel.add(buttonPanel, BorderLayout.SOUTH);
		
		
		jXMapKit = new JXMapKit();
		
		info = new OSMTileFactoryInfo();
		tileFactory = new DefaultTileFactory(info);
		jXMapKit.setTileFactory(tileFactory);
		
		tileFactory.setThreadPoolSize(8);
		
		GeoPosition UofC = new GeoPosition(51.079948, -114.125534);
		
		wpm = new WayPointManager();
		wpm.initPaint(UofC);
		
		jXMapKit.getMainMap().setOverlayPainter(wpm.getPainter());
		
		jXMapKit.setZoom(3);
		jXMapKit.getMainMap().setAddressLocation(UofC);
		jXMapKit.setAddressLocationShown(false);
		
		mapPanel.add(jXMapKit);
		
		jXMapKit.getMainMap().addMouseListener(new MouseInputAdapter(){
			public void mouseClicked(MouseEvent e){
				Point point = e.getPoint();
				
				GeoPosition marker = jXMapKit.getMainMap().convertPointToGeoPosition(point);
	
				wpm.addPoint(marker);
				
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
		add(mapPanel, BorderLayout.CENTER);
		pack();
		setSize(1280, 720);
	}
	
	public void setFlightParams() {
		Utilities.buildFlightParamString(altitudeField.getText(), radiusField.getText(), wpm.getWaypoints());
	}
}