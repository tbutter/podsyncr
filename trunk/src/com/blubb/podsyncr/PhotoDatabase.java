package com.blubb.podsyncr;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

public class PhotoDatabase implements java.io.Serializable {
	static HashMap<String,String> ipodmapping = new HashMap<String,String>();
	ArrayList<Album> albums = new ArrayList<Album>();
	
	int firstid = 100;
	int lastid = firstid - 1;
	ArrayList<String> photos = new ArrayList<String>();
	
	String getIpodID(String id) {
		String ipodid = ipodmapping.get(id);
		if(ipodid == null) {
			ipodid = ""+(++lastid);
			ipodmapping.put(id, ipodid);
			photos.add(id);
		}
		return ipodid;
	}
	
	void generateThumbnailFile(int w, int h, File f1) throws IOException {
		Prefs.setStatus("Generating thumbnailfile for "+w+"x"+h);
		FileOutputStream fos = new FileOutputStream(f1);
		byte buffer[] = new byte[w*h*2];
		for(String s : photos) {
			FileInputStream fis = new FileInputStream(Prefs.getHomeDir()+s+"-"+w+"x"+h+".ithmb");
			fis.read(buffer);
			fos.write(buffer);
			fis.close();
		}
		fos.close();
	}
	
	void writeDatabase(Model model) throws IOException {
		// calc album sizes
		int albsizes = 0;
		for(Album a: albums) {
			albsizes += 0x94 + 37+a.name.length() + 40 * a.photos.size();
		}
		
		DataOutputStream dos = new DataOutputStream(new FileOutputStream(Prefs.getHomeDir()+"Photo Database"));
		// mhfd
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('f');
		dos.writeByte('d');
		// header len
		writeInt(dos, 0x84);
		// len of whole file
//		writeInt(dos, 1701297);
		writeInt(dos, 320 + 512*photos.size() + 0x60 + 0x5c + 0x94 + 44 + 40 * photos.size() +436 + albsizes);
		// 0, 1, 3, 0
		writeInt(dos, 0);
		writeInt(dos, 1);
		writeInt(dos, 3);
		writeInt(dos, 0);
		// next id
		writeInt(dos, 100 + albums.size() + photos.size());
		// unknown 2 *8
		writeInt(dos, 0);
		writeInt(dos, 0);
		writeInt(dos, 0);
		writeInt(dos, 0);
		// 2
		writeInt(dos, 2);
		// padding
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		
		// mhsd
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('s');
		dos.writeByte('d');
		// header len
		writeInt(dos, 0x60);
		// total len
		writeInt(dos, 512*photos.size()+0x60+0x5c);
		// index -> image list
		writeInt(dos, 0x01);
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		
		// mhli
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('l');
		dos.writeByte('i');
		// header size
		writeInt(dos, 0x5c);
		// number of images
		writeInt(dos, photos.size());
		for(int i=0; i<20; i++)
			writeInt(dos, 0);

		int id = 100;
		int j = 0;
		// 320 byte until this point
		for(@SuppressWarnings("unused") String s : photos) { // 512 byte per pic
			// mhii
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('i');
			dos.writeByte('i');
			// header len
			writeInt(dos, 0x98);
			// total len
			writeInt(dos, 512);
			// PhotoDB has 2 children in nano
			writeInt(dos, 2);
			// id
			writeInt(dos, id++);
			// album id?
			writeInt(dos, 102);
			writeInt(dos, 0);
			//unknown
			writeInt(dos, 0);
			//rating
			writeInt(dos, 0);
			//unknown
			writeInt(dos, 0);
			//date 1
			writeInt(dos, 0);
			//date 2
			writeInt(dos, 0);
			//orig image size
			writeInt(dos, 0x1234);
			for(int i=0; i<25; i++)
				writeInt(dos, 0);
			
			//mhod
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('o');
			dos.writeByte('d');
			// header len
			writeInt(dos, 0x18);
			// total len
			writeInt(dos, 0xb4);
			// type 2
			writeInt(dos, 2);
			// unknown
			writeInt(dos, 0);
			writeInt(dos, 0);
			
			//	mhni
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('n');
			dos.writeByte('i');
			// header len
			writeInt(dos, 0x4c);
			// total len
			writeInt(dos, 0x9c);
			// childs
			writeInt(dos, 1);
			// correlation id 1023 for big, 1032 for small
			writeInt(dos, 1023);
			// thumb offset
			writeInt(dos, j*46464);
			// thumb byte size
			writeInt(dos, 46464);
			// pads?
			writeInt(dos, 0);
			// size
			dos.writeByte(132);
			dos.writeByte(0);
			dos.writeByte(176);
			dos.writeByte(0);
			for(int i=0; i<10; i++)
				writeInt(dos, 0);
			
			//mhod
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('o');
			dos.writeByte('d');
			// header len
			writeInt(dos, 24);
			writeInt(dos, 80);
			dos.writeByte(3);
			dos.writeByte(0);
			dos.writeByte(0);
			dos.writeByte(2);
			writeInt(dos, 0);
			writeInt(dos, 0);
			writeInt(dos, 42);
			writeInt(dos, 2);
			writeInt(dos, 0);
			
			utf16hack(dos,":Thumbs:F1023_1.ithmb");
			dos.writeByte(0);
			dos.writeByte(0);
			
			// and for the small thumb
			
//			mhod
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('o');
			dos.writeByte('d');
			// header len
			writeInt(dos, 0x18);
			// total len
			writeInt(dos, 0xb4);
			// type 2
			writeInt(dos, 2);
			// unknown
			writeInt(dos, 0);
			writeInt(dos, 0);
			
			//	mhni
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('n');
			dos.writeByte('i');
			// header len
			writeInt(dos, 0x4c);
			// total len
			writeInt(dos, 0x9c);
			// childs
			writeInt(dos, 1);
			// correlation id 1023 for big, 1032 for small
			writeInt(dos, 1032);
			// thumb offset
			writeInt(dos, j*3108);
			// thumb byte size
			writeInt(dos, 3108);
			// pads?
			writeInt(dos, 0);
			// size
			dos.writeByte(37);
			dos.writeByte(0);
			dos.writeByte(42);
			dos.writeByte(0);
			for(int i=0; i<10; i++)
				writeInt(dos, 0);
			
			//mhod
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('o');
			dos.writeByte('d');
			// header len
			writeInt(dos, 24);
			writeInt(dos, 80);

			dos.writeByte(3);
			dos.writeByte(0);
			dos.writeByte(0);
			dos.writeByte(2);
			writeInt(dos, 0);
			
			writeInt(dos, 0);
			writeInt(dos, 42);
			writeInt(dos, 2);
			writeInt(dos, 0);
			
			utf16hack(dos,":Thumbs:F1032_1.ithmb");
			dos.writeByte(0);
			dos.writeByte(0);
			
			// end small thumb
			j++;
		}
		// and now the albums
		// mhsd
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('s');
		dos.writeByte('d');
		// header len
		writeInt(dos, 0x60);
		// total len
		writeInt(dos, 0x60 + 0x5c + 0x94 + 44 + 40 * photos.size() + albsizes);
		// index -> image list
		writeInt(dos, 0x02);
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		
		// mhla
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('l');
		dos.writeByte('a');
		// header len
		writeInt(dos, 0x5c);
		// number of children
		writeInt(dos, 1 + albums.size());
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		
		// all the photos
		// mhba
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('b');
		dos.writeByte('a');
		// header len
		writeInt(dos, 0x94);
		// total len
		writeInt(dos, 0x94 + 44 + 40 * photos.size());
		// data count
		writeInt(dos, 1);
		// child count
		writeInt(dos, photos.size());
		writeInt(dos, 0x65);
		// unknown
		writeInt(dos, 0);
		dos.writeByte(0);
		dos.writeByte(0);
		// type
		dos.writeByte(1); // master list
		// playmusic
		dos.writeByte(0);
		// repeat
		dos.writeByte(0);
		// random
		dos.writeByte(0);
		// showtitles
		dos.writeByte(0);
		// transition direction
		dos.writeByte(0);
		// slide duration
		writeInt(dos, 4);
		// transition duration
		writeInt(dos, 100);
		// unknown
		writeInt(dos, 0);
		writeInt(dos, 0);
		writeInt(dos, 101);
		writeInt(dos, 0);
		writeInt(dos, 100);
		for(int i=0; i<21; i++)
			writeInt(dos, 0);
//		mhod
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('o');
		dos.writeByte('d');
		// header len
		writeInt(dos, 24);
		writeInt(dos, 44);
		dos.writeByte(1);
		dos.writeByte(0);
		dos.writeByte(0);
		dos.writeByte(1);
		
		writeInt(dos, 0);
		writeInt(dos, 0);
		writeInt(dos, 7);
		writeInt(dos, 1);
		writeInt(dos, 0);
		dos.writeByte('L');
		dos.writeByte('i');
		dos.writeByte('b');
		dos.writeByte('r');
		dos.writeByte('a');
		dos.writeByte('r');
		dos.writeByte('y');
		dos.writeByte(0);
		
		// and now every photo
		j = 100;
		for(@SuppressWarnings("unused") String s : photos) {
			// mhia
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('i');
			dos.writeByte('a');
			writeInt(dos, 40);
			writeInt(dos, 40);
			writeInt(dos, 0);
			writeInt(dos, j++);
			for(int i=0; i<5; i++)
				writeInt(dos, 0);
		}
		
		// every album
		
		for(Album a : albums) {
			// mhba
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('b');
			dos.writeByte('a');
			// header len
			writeInt(dos, 0x94);
			// total len
			writeInt(dos, 0x94 + 37+a.name.length() + 40 * a.photos.size());
			// data count
			writeInt(dos, 1);
			// child count
			writeInt(dos, a.photos.size());
			writeInt(dos, 0x65);
			// unknown
			writeInt(dos, 0);
			dos.writeByte(0);
			dos.writeByte(0);
			// type
			dos.writeByte(2); // master list
			// playmusic
			dos.writeByte(0);
			// repeat
			dos.writeByte(0);
			// random
			dos.writeByte(0);
			// showtitles
			dos.writeByte(0);
			// transition direction
			dos.writeByte(0);
			// slide duration
			writeInt(dos, 4);
			// transition duration
			writeInt(dos, 100);
			// unknown
			writeInt(dos, 0);
			writeInt(dos, 0);
			writeInt(dos, 101);
			writeInt(dos, 0);
			writeInt(dos, 100);
			for(int i=0; i<21; i++)
				writeInt(dos, 0);
//			mhod
			dos.writeByte('m');
			dos.writeByte('h');
			dos.writeByte('o');
			dos.writeByte('d');
			// header len
			writeInt(dos, 24);
			writeInt(dos, 37+a.name.length());
			dos.writeByte(1);
			dos.writeByte(0);
			dos.writeByte(0);
			dos.writeByte(1);
			
			writeInt(dos, 0);
			writeInt(dos, 0);
			writeInt(dos, a.name.length());
			writeInt(dos, 1);
			writeInt(dos, 0);
			for(int k = 0; k < a.name.length(); k++)
				dos.writeByte(a.name.charAt(k));
			dos.writeByte(0);
			
			// and now every photo
			for(String s : a.photos) {
				// mhia
				dos.writeByte('m');
				dos.writeByte('h');
				dos.writeByte('i');
				dos.writeByte('a');
				writeInt(dos, 40);
				writeInt(dos, 40);
				writeInt(dos, 0);
				writeInt(dos, Integer.parseInt(getIpodID(s)));
				for(int i=0; i<5; i++)
					writeInt(dos, 0);
			}
		}
		
		// and the strange file list
		// mhsd
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('s');
		dos.writeByte('d');
		// header len
		writeInt(dos, 0x60);
		// total len
		writeInt(dos, 436);
		// index -> image list
		writeInt(dos, 0x03);
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		
		// mhlf
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('l');
		dos.writeByte('f');
		// header len
		writeInt(dos, 0x5c);
		// children
		writeInt(dos, 2);
		for(int i=0; i<20; i++)
			writeInt(dos, 0);
		// mhif
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('i');
		dos.writeByte('f');
		// header len
		writeInt(dos, 0x7c);
		writeInt(dos, 0x7c);
		writeInt(dos, 0);
		writeInt(dos, 1023);
		writeInt(dos, 46464);
		for(int i=0; i<25; i++)
			writeInt(dos, 0);
		// mhif
		dos.writeByte('m');
		dos.writeByte('h');
		dos.writeByte('i');
		dos.writeByte('f');
		// header len
		writeInt(dos, 0x7c);
		writeInt(dos, 0x7c);
		writeInt(dos, 0);
		writeInt(dos, 1032);
		writeInt(dos, 3108);
		for(int i=0; i<25; i++)
			writeInt(dos, 0);
		dos.close();
	}
	
	void utf16hack(DataOutputStream dos, String s) throws IOException {
		for(int i = 0; i<s.length(); i++) {
			dos.writeByte(s.charAt(i));
			dos.writeByte(0);
		}
	}
	
	void writeInt(DataOutputStream dos, int x) throws IOException {
		dos.writeByte(x & 0xff);
		dos.writeByte((x >> 8) & 0xff);
		dos.writeByte((x >> 16) & 0xff);
		dos.writeByte((x >> 24) & 0xff);
	}
	
	static void genThumb(String id, int w, int h, boolean be) throws IOException {
		File origfile = new File(Prefs.getHomeDir()+id+"-small.jpg");
		File thumbfile = new File(Prefs.getHomeDir()+id+"-"+w+"x"+h+".ithmb");
		if(thumbfile.exists() && thumbfile.length() == w * h * 2) {
			return;
		}
		System.out.println("generating "+thumbfile);
		BufferedImage bi = ImageIO.read(origfile);
		byte rgb565[] = getThumb(bi, w, h, be);
		FileOutputStream fos = new FileOutputStream(thumbfile);
		fos.write(rgb565);
		fos.close();		
	}
	
	static byte []getThumb(Image bi, int iw, int ih, boolean be) {
		int width = bi.getWidth(null);
		int height = bi.getHeight(null);
		int tw, th;
		tw = iw;
		th = ih;
		double wf = (double)iw / (double)width;
		double hf = (double)ih / (double)height;
		double fac = 0.0;
		if(wf > hf) {
			fac = hf;
		} else {
			fac = wf;
		}
		tw = (int)(fac * width);
		th = (int)(fac * height);
		BufferedImage bi2 = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
		bi2.getGraphics().drawImage(bi,0,0,tw,th,null);
		byte rgb565[] = new byte[iw * ih * 2];
		int leftoff = (iw - tw) / 2;
		int offset = 0;
		for(int y=0; y<ih; y++) {
			for(int x=0; x<iw; x++) {
				if(x >= leftoff && x < leftoff + tw && y < th) {
					int pixel = bi2.getRGB(x-leftoff,y);
					int r = ((pixel >> 16) & 0xff) >> 3;
					int g = ((pixel >> 8) & 0xff) >> 2;
					int b = (pixel & 0xff) >> 3;
					pixel = r << 11 | g << 5 | b;
					if(be) {
						rgb565[offset++] = (byte)(pixel >> 8);
						rgb565[offset++] = (byte)(pixel & 0xff);
					} else {
						rgb565[offset++] = (byte)(pixel & 0xff);
						rgb565[offset++] = (byte)(pixel >> 8);
					}
				} else {
					rgb565[offset++] = rgb565[offset++] = 0;
				}
			}
		}
		return rgb565;
	}
	
	public void generateThumbnailFiles(Model m) throws IOException {
		for(ThumbnailSpecs ts : m.thumbs) {
			generateThumbnailFile(ts.w, ts.h, new File(Prefs.getHomeDir()+ts.filename));			
		}
	}

	Vector<Model> getAvailableModels() {
		Vector<Model> models = new Vector<Model>();
		Model m = new Model();
		m.name = "Nano 2nd gen";
		m.thumbs.add(new ThumbnailSpecs(176,132, "F1023_1.ithmb"));
		m.thumbs.add(new ThumbnailSpecs(42,37, "F1032_1.ithmb"));
		models.add(m);
		return models;
	}
	
	class Model {
		String name;
		List<ThumbnailSpecs> thumbs = new LinkedList<ThumbnailSpecs>();
		
		public String toString() {
			return name;
		}
	}
	
	class ThumbnailSpecs {
		int w;
		int h;
		String filename;
		
		ThumbnailSpecs(int w, int h, String filename) {
			this.w = w;
			this.h = h;
			this.filename = filename;
		}
	}
}
