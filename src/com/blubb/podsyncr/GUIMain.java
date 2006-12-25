package com.blubb.podsyncr;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.jnlp.BasicService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.FlickrException;

public class GUIMain implements StatusListener {
	PhotoDatabase db = new PhotoDatabase();
	FlickrComm flickr = null;
	JLabel status = null;	
	boolean synced = false;
	JComboBox model;
	
	public GUIMain() {
		try {
			flickr = new FlickrComm();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
			return;
		}
		
		JFrame frame = new JFrame("podsyncr");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel main = new JPanel(new GridLayout(5,1));
		JPanel modelpanel = new JPanel(new FlowLayout());
		modelpanel.add(new JLabel("IPod Model: "));
		model = new JComboBox(db.getAvailableModels());
		modelpanel.add(model);
		main.add(modelpanel);
		
		if(flickr.auth != null) {
			JLabel auth = new JLabel("Authenticated as "+flickr.auth.getUser().getUsername());
			main.add(auth);
		} else {
			JButton auth = new JButton("Authenticate");
			auth.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					BasicService bs;
					try {
						bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
						bs.showDocument(flickr.getLoginURL());
						JOptionPane.showMessageDialog(null, "Press OK after login in browser", "Authenticate", JOptionPane.PLAIN_MESSAGE);
						flickr.doLogin();
						JButton button = (JButton)e.getSource();
						button.setText("Authenticated as "+flickr.auth.getUser().getUsername());
						button.setEnabled(false);
					} catch (Exception e1) {
						try {
							JOptionPane.showInputDialog("Go to the url and press OK after login in browser", flickr.getLoginURL().toExternalForm());
							flickr.doLogin();
							JButton button = (JButton)e.getSource();
							button.setText("Authenticated as "+flickr.auth.getUser().getUsername());
							button.setEnabled(false);
						} catch (Exception e2) {
							e2.printStackTrace();
						}
					}
				}
			});
			main.add(auth);
		}
		
		JButton syncbutton = new JButton("sync");
		JButton aboutbutton = new JButton("about");
		main.add(syncbutton);
		main.add(aboutbutton);
		syncbutton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				if(synced) System.exit(0);
				((JButton)e.getSource()).setEnabled(false);
				new Thread() {
					public void run() {
						try {
							flickr.download(db);
							db.generateThumbnailFiles((PhotoDatabase.Model)model.getSelectedItem());
							db.writeDatabase((PhotoDatabase.Model)model.getSelectedItem());
							setStatus("done");
							((JButton)e.getSource()).setText("quit");
							((JButton)e.getSource()).setEnabled(true);
							synced = true;
						} catch (IOException e2) {
							e2.printStackTrace();
						} catch (SAXException e2) {
							e2.printStackTrace();
						} catch (FlickrException e2) {
							e2.printStackTrace();
						}
					}
				}.start();
			}
		});
		aboutbutton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "<html><h1>podsyncr</h1>"+
						"(c) 2006 Thomas Butter<br>"+
						"http://butter.eu/ <br>"+
						"http://code.google.com/p/podsyncr/<br>"+
						"This product includes software developed by Aetrion LLC.",
						"About",JOptionPane.INFORMATION_MESSAGE);
			}
		});
		status = new JLabel("Status...");
		main.add(status);
		Prefs.setStatusListener(this);
		frame.add(main);
		frame.pack();
		frame.setVisible(true);
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new GUIMain();
	}

	public void setStatus(String s) {
		status.setText(s);
		status.repaint();
	}

}
