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

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.GridLayout;
import javax.swing.SwingConstants;

/**
 * 
 * @author Veronica Eaton
 *
 */
public class Gui {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Gui window = new Gui();
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
	public Gui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 363, 341);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon image = new ImageIcon("Logo.png");
		frame.getContentPane().setLayout(new GridLayout(5, 4, 0, 0));
		JLabel label = new JLabel("", image, SwingConstants.CENTER);
		label.setFocusable(false);
		frame.getContentPane().add(label);
		
		JPanel panel_3 = new JPanel();
		frame.getContentPane().add(panel_3);
		
		JPanel panel_2 = new JPanel();
		frame.getContentPane().add(panel_2);

		JPanel panel_1 = new JPanel();
		frame.getContentPane().add(panel_1);

		JButton btnSetUp = new JButton("Set Up");
		btnSetUp.setFocusPainted(false);
		panel_1.add(btnSetUp);

		btnSetUp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {

				//When setup is clicked the GUI opens the setup frame
				//setUp set= new setUp();
				//set.setVisible(true);

				frame.dispose();
				SetupPage setupPage = new SetupPage();
				setupPage.setVisible(true);
			}
		});
		btnSetUp.setForeground(new Color(0, 153, 255));
		btnSetUp.setFont(new Font("Tahoma", Font.PLAIN, 18));

	
		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);

		//Brandon's Page is linked here
		JButton btnNewButton_1 = new JButton("Control");
		btnNewButton_1.setFocusPainted(false);
		panel.add(btnNewButton_1);
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//Control Page goes here when mouse is clicked for control, either a new class can be added or a new fram
				frame = new JFrame("new frame");
				frame.setVisible(true);
				frame.setBounds(100, 100, 800, 800);
			}
		});
		btnNewButton_1.setFont(new Font("Tahoma", Font.PLAIN, 18));
		btnNewButton_1.setForeground(new Color(0, 153, 255));
	
	}

}
