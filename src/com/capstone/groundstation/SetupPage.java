package com.capstone.groundstation;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import com.teamdev.jxbrowser.chromium.Browser;
import com.teamdev.jxbrowser.chromium.swing.BrowserView;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * 
 * @authors Veronica Eaton, Brandon Lee
 *
 */
public class SetupPage extends JFrame {
	private static final long serialVersionUID = 1L;
	private Browser browser;
	private JTextArea waypoints;
	private boolean bodyCount = false;
	private boolean videoOn = false;
	

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
		mapApp();
	}
	
	/**
	 * Place elements in a border layout
	 */
	private void createGUI(){
		JPanel leftPanel = new JPanel(new BorderLayout());
		JPanel featuresPanel = new JPanel();
		featuresPanel.setLayout(new BoxLayout(featuresPanel, BoxLayout.Y_AXIS));
		
		JButton abortButton = new JButton("Launch Drone");
		abortButton.setPreferredSize(new Dimension(200, 60));
		abortButton.setFont(new Font("Tahoma", Font.PLAIN, 25));
		abortButton.setContentAreaFilled(false);
		abortButton.setBackground(new Color(0, 153, 255));
		abortButton.setForeground(Color.WHITE);
		abortButton.setOpaque(true);
		abortButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//close window and open control page
				dispose();
				
				ControlPage controlPage = new ControlPage(videoOn);
				controlPage.setExtendedState(JFrame.MAXIMIZED_BOTH); 
				controlPage.setVisible(true);
			}
		});
		
		
		JLabel label = new JLabel(" Drone Features");
		label.setFont(new Font("Tahoma", Font.PLAIN, 20));
		label.setForeground(new Color(0, 153, 255));

		JCheckBox bodyCountBox = new JCheckBox("Body Count Analytics");
		bodyCountBox.setFont(new Font("Tahoma", Font.PLAIN, 15));
		
		JCheckBox videoFeedBox = new JCheckBox("Live Video Feed");
		videoFeedBox.setFont(new Font("Tahoma", Font.PLAIN, 15));
		
		//action listeners for each checkbox - since we can't do anaytics and video simultaneously, only one can be selected at a time
		bodyCountBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(videoFeedBox.isSelected()){
					videoFeedBox.setSelected(false);
					videoOn = false;
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
				
				videoOn = true;
			}
		});
		
		
		waypoints = new JTextArea();
		waypoints.setEditable(false);
		waypoints.setFont(new Font("Tahoma", Font.PLAIN, 15));
		waypoints.setBackground(null);
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(waypoints);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());

		featuresPanel.add(label);
		featuresPanel.add(bodyCountBox);
		featuresPanel.add(videoFeedBox);
		
		leftPanel.add(featuresPanel, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		leftPanel.add(abortButton, BorderLayout.SOUTH);
		
		
		browser = new Browser();	
		BrowserView browserView = new BrowserView(browser);
		browserView.setMinimumSize(new Dimension(10, 60));
		
		
		getContentPane();
		add(leftPanel, BorderLayout.WEST);
		add(browserView, BorderLayout.CENTER);
		pack();
		setSize(1280, 720);
	}
	
	/**
	 * TODO embed maps application
	 * not sure how it will work; browser is the web wrapper object you will likely need to use
	 * keep outside of createGUI
	 */
	public void mapApp(){
		browser.loadURL("https://www.bing.com/maps/"); //testing purposes only
	}
	
	/**
	 * update screen with waypoint coordinates extracted from map
	 */
	public void updateUI(){
		
	}

}
