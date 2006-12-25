package com.blubb.podsyncr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.RequestContext;
import com.aetrion.flickr.auth.Auth;
import com.aetrion.flickr.auth.AuthInterface;
import com.aetrion.flickr.auth.Permission;
import com.aetrion.flickr.photos.PhotoList;
import com.aetrion.flickr.photosets.Photoset;
import com.aetrion.flickr.photosets.Photosets;
import com.aetrion.flickr.photosets.PhotosetsInterface;
import com.aetrion.flickr.util.FileAuthStore;

public class FlickrComm {
	static String restHost = "www.flickr.com";
	Flickr f;
	REST rest;
	RequestContext requestContext;
	String frob = "";
	String token = "";
	FileAuthStore fas = new FileAuthStore(new java.io.File(Prefs.getHomeDir()));
	Auth auth = null;
	AuthInterface authInterface = null;
	String sharedsecret = "27ed81518a3bd16d";

	public FlickrComm() throws IOException, ParserConfigurationException {
		rest = new REST();
		rest.setHost(restHost);
		f = new Flickr("95f40ef797b2800f4e5b62e4708fd6a8",rest);
		// Set the shared secret which is used for any calls which require signing.
		requestContext = RequestContext.getRequestContext();
		requestContext.setSharedSecret(sharedsecret);
		Auth[] auths = fas.retrieveAll();
		if(auths == null || auths.length == 0) {
			Prefs.setStatus("not authed");
		} else {
			auth = auths[0];
		}
		if(auth != null)
			requestContext.setAuth(auth);
	}
	
	public java.net.URL getLoginURL() throws IOException, SAXException {
		RequestContext.getRequestContext().setSharedSecret(sharedsecret);
		authInterface = f.getAuthInterface();
		try {
			frob = authInterface.getFrob();
		} catch(FlickrException e) {
			e.printStackTrace();
		}
		URL url = authInterface.buildAuthenticationUrl(Permission.READ, frob);
		System.out.println("Press return after you granted access at this URL:");
		System.out.println(url.toExternalForm());
		return url;
	}
	
	void doLogin() throws IOException, SAXException {
		
		try {
			auth = authInterface.getToken(frob);
			System.out.println("Authentication success");
			System.out.println("Token: "+auth.getToken());
			System.out.println("nsid: "+auth.getUser().getId());
			System.out.println("Realname: "+auth.getUser().getRealName());
			System.out.println("Username: "+auth.getUser().getUsername());
			System.out.println("Permission: "+auth.getPermission().getType());
			fas.store(auth);
		} catch(FlickrException e) {
			Prefs.setStatus("Authentication failed");
			e.printStackTrace();
		}
		requestContext.setAuth(auth);
	}
	
	void download(PhotoDatabase db) throws IOException, SAXException, FlickrException {
		RequestContext.getRequestContext().setSharedSecret(sharedsecret);
		RequestContext.getRequestContext().setAuth(auth);
		PhotosetsInterface psets = f.getPhotosetsInterface();
		Photosets sets = psets.getList(auth.getUser().getId());
		Collection c = sets.getPhotosets();
		int i = 0;
		for(Object o : c) {
			if(i < 100) {
				Photoset ps = (Photoset)o;
				Album a = new Album();
				a.name = ps.getTitle();
				db.albums.add(a);
				Prefs.setStatus("Getting Set "+ ps.getTitle()+ " ("+ i +")");
				PhotoList pl = psets.getPhotos(ps.getId());
				a.addPhotoSet(db,pl);
			}
			i++;
		}
		i = 1;
		Album a = new Album();
		a.name = "Not in a set";
		db.albums.add(a);
		PhotoList pl = f.getPhotosInterface().getNotInSet(100, 1);
		while(i < 100 && i <= pl.getPages()) {
			a.addPhotoSet(db,pl);
			i++;
			Prefs.setStatus("Getting page "+pl.getPage() + " of "+ pl.getPages());
			if(i <= pl.getPages()) pl = f.getPhotosInterface().getNotInSet(100,i);
		}
		Prefs.setStatus("done");
	}
}
