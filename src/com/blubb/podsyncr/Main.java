package com.blubb.podsyncr;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photosets.Photoset;
import com.aetrion.flickr.photosets.Photosets;
import com.aetrion.flickr.photosets.PhotosetsInterface;
import com.aetrion.flickr.util.FileAuthStore;
import com.aetrion.flickr.util.IOUtilities;
import com.aetrion.flickr.util.ImageUtilities;

public class Main {
	FlickrComm flickr = null;
	
	PhotoDatabase db = null;
	
	public Main() throws ParserConfigurationException, IOException, SAXException, FlickrException, ClassNotFoundException {
		if(new File(Prefs.getHomeDir()+"state").exists()) {
			ObjectInputStream ois = new ObjectInputStream(new FileInputStream(Prefs.getHomeDir()+"state"));
			db = (PhotoDatabase) ois.readObject();
			ois.close();
			System.out.println("loaded state");
		} else {
			db = new PhotoDatabase();
			flickr = new FlickrComm();
			if(flickr.auth == null) {
				flickr.getLoginURL();
				BufferedReader infile =
					new BufferedReader ( new InputStreamReader (System.in) );
				infile.readLine();
				flickr.doLogin();
			}
			flickr.download(db);
		}
		// generate thumbnail file
		db.generateThumbnailFile(176, 132, new File(Prefs.getHomeDir()+"F1023_1.ithmb"));
		db.generateThumbnailFile(42, 37, new File(Prefs.getHomeDir()+"F1032_1.ithmb"));
		for(Album a : db.albums) {
			System.out.println("Album '"+a.name+"' contains " + a.photos.size() + " photos");
//			for(String s : a.photos) {
//				System.out.println("   "+s+" "+ipodmapping.get(s));
//			}
		}
		
		// save state
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(Prefs.getHomeDir()+"state"));
		oos.writeObject(db);
		oos.close();
		db.writeDatabase(null);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new Main();
	}
}