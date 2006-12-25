package com.blubb.podsyncr;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.PhotoList;

public class Album implements java.io.Serializable {
	String name;
	ArrayList<String> photos = new ArrayList<String>();
	
	public void addPhotoSet(PhotoDatabase db, PhotoList pl) throws IOException {
		for(Object o2 : pl) {
			Photo p = (Photo)o2;
			photos.add(p.getId());
			db.getIpodID(p.getId());
			File origfile = new File(Prefs.getHomeDir()+p.getId()+"-small.jpg");
			if(!origfile.exists()) {
				InputStream is = p.getSmallAsInputStream();
				int buf = 0;
				FileOutputStream fosorig = new FileOutputStream(
						origfile);
				while ((buf = is.read()) != -1) {
					fosorig.write(buf);
				}
				fosorig.close();
			}
			PhotoDatabase.genThumb(p.getId(),176,132, true);
			PhotoDatabase.genThumb(p.getId(),42,37, false);
		}
	}
}
