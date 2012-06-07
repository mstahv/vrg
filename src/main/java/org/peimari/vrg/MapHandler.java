package org.peimari.vrg;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.HashMap;

import com.vaadin.terminal.DownloadStream;
import com.vaadin.terminal.URIHandler;

public class MapHandler implements URIHandler {

	public static final MapHandler instance = new MapHandler();

	private static class MapImage {
		File f;
		int id;
		String type;
	}

	private HashMap<String, MapImage> m = new HashMap<String, MapImage>();

	public static void addFile(File file, int id, String type) {
		MapImage mapImage = new MapImage();
		mapImage.f = file;
		mapImage.id = id;
		mapImage.type = type;
		instance.m.put("" + id, mapImage);
	}

	public DownloadStream handleURI(URL context, String relativeUri) {
		if (relativeUri.contains("MAPS/")) {
			try {
				String id = relativeUri.substring(relativeUri.lastIndexOf("/")+1);
				MapImage mapImage = m.get(id);

				String contentType = "image/jpg";
				if (mapImage.type.equals("gif")) {
					contentType = "image/gif";
				}
				DownloadStream downloadStream = new DownloadStream(
						new FileInputStream(mapImage.f), contentType, "map");
				return downloadStream;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

}
