package com.capstone.groundstation;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.ButtonUI;

import java.awt.GridLayout;
import javax.swing.JCheckBoxMenuItem;
import java.awt.Color;
import java.awt.Font;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * @author Veronica Eaton
 *
 */
public class SetupPage extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public boolean bodyCount= true;
	public boolean faceDetect=false;
	
	private JPanel contentPane;

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
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public SetupPage() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		//contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));	not needed
		setContentPane(contentPane);
		contentPane.setLayout(new GridLayout(5, 5, 1, 1));
		
		JPanel panel = new JPanel();
		//panel.setBorder(null);	not needed
		contentPane.add(panel);
		
		/*
		JTextArea txtrSelectAnalyticFeatures = new JTextArea();
		panel.add(txtrSelectAnalyticFeatures);
		txtrSelectAnalyticFeatures.setEditable(false);
		txtrSelectAnalyticFeatures.setForeground(new Color(0, 153, 255));
		txtrSelectAnalyticFeatures.setFont(new Font("Tahoma", Font.PLAIN, 19));
		txtrSelectAnalyticFeatures.setText("Select Analytic Features:");
		*/
		
		/**
		 * Added by Brandon Lee
		 * Use JLabel instead of JTextArea for a header
		 */
		JLabel titleLabel = new JLabel("Select Analytic Features:");
		titleLabel.setForeground(new Color(0, 153, 255));
		titleLabel.setFont(new Font("Tahoma", Font.PLAIN, 19));
		panel.add(titleLabel);
		
		JPanel panel_4 = new JPanel();
		//panel_4.setBorder(null);	not needed
		contentPane.add(panel_4);
		
		JCheckBoxMenuItem chckbxmntmNewCheckItem = new JCheckBoxMenuItem("Body Count");
		chckbxmntmNewCheckItem.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//bodyCount = true;
				
				/**
				 * Added by Brandon Lee
				 */
				if(!bodyCount)
					bodyCount = true;
				else
					bodyCount = false;
			}
		});
		panel_4.add(chckbxmntmNewCheckItem);
		chckbxmntmNewCheckItem.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JCheckBoxMenuItem chckbxmntmNewCheckItem_1 = new JCheckBoxMenuItem("Face Detection");
		chckbxmntmNewCheckItem_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//faceDetect = true;
				
				/**
				 * Added by Brandon Lee
				 */
				if(!faceDetect)
					faceDetect = true;
				else
					faceDetect = false;
			}
			
		});
		panel_4.add(chckbxmntmNewCheckItem_1);
		chckbxmntmNewCheckItem_1.setFont(new Font("Tahoma", Font.PLAIN, 14));
		
		JPanel panel_1 = new JPanel();
		contentPane.add(panel_1);
		
		JButton btnNewButton = new JButton("Launch Drone");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				/**
				 * Added by Brandon Lee
				 * close current window and launch control page
				 */
				dispose();
				
				ControlPage controlPage = new ControlPage(true);
				controlPage.setVisible(true);
			}
		});
		panel_1.add(btnNewButton);
		btnNewButton.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnNewButton.setForeground(new Color(0, 153, 255));
		
		
		JPanel panel_3 = new JPanel();
		//panel_3.setBorder(null);	not needed
		contentPane.add(panel_3);
		
		/* Gonna embed map directly in frame instead of launching from another button
		 *
		JButton btnNewButton_1 = new JButton("Map");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				
				//embeded map event goes here
			}
		});
		panel_3.add(btnNewButton_1);
		btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnNewButton_1.setForeground(new Color(0, 153, 255));
		*/
		
		/**
		 * Added by Brandon Lee
		 */
		pack();
		setSize(720, 480);
	}

}
