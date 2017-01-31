package com.capstone.groundstation;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * 
 * @author Veronica Eaton
 *
 */
public class StartUp {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StartUp window = new StartUp();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public StartUp() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{40, 40,40, 0, 0, 0, 0, 40, 40, 40};
		gridBagLayout.rowHeights = new int[]{0, 0, 132, 0, 0, 0, 0, -1, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		frame.getContentPane().setLayout(gridBagLayout);
		
		JPanel panel = new JPanel(new BorderLayout());
		ImageIcon image = new ImageIcon("3drLogo.jpg");
		JLabel label = new JLabel("", image, JLabel.CENTER);
		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 5);
		gbc_label.gridx = 4;
		gbc_label.gridy = 2;
		frame.getContentPane().add(label, gbc_label);
		
		
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.insets = new Insets(0, 0, 5, 5);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 5;
		gbc_panel.gridy = 2;
		frame.getContentPane().add(panel, gbc_panel);
		
		JButton btnSetUp = new JButton("Set Up");
		btnSetUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				/**
				 * Added by Brandon Lee
				 * Close window before launching a new one
				 */
				frame.dispose();
				
				
				SetupPage set= new SetupPage();
				set.setVisible(true);
			}
		});
	
		/* Dont need this 
		 * 
		btnSetUp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				
			}
		});
		*/
		btnSetUp.setForeground(new Color(0, 153, 255));
		btnSetUp.setFont(new Font("Tahoma", Font.PLAIN, 18));
		GridBagConstraints gbc_btnSetUp = new GridBagConstraints();
		gbc_btnSetUp.insets = new Insets(0, 0, 5, 5);
		gbc_btnSetUp.gridx = 4;
		gbc_btnSetUp.gridy = 4;
		frame.getContentPane().add(btnSetUp, gbc_btnSetUp);
		
		/**
		 * Added by Brandon Lee
		 */
		frame.pack();
		frame.setSize(720, 480);
	}

}
