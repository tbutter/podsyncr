package com.blubb.podsyncr;

public class Prefs {
	static String homedir = System.getProperty("user.home")+"/.podsyncr/";
	static StatusListener status = null;
	
	static public void setStatusListener(StatusListener sl) {
		status = sl;
	}
	
	static public String getHomeDir() {
		return homedir;
	}
	
	static public void setStatus(String s) {
		if(Prefs.status != null) Prefs.status.setStatus(s);
		else System.out.println(s);
	}
}
