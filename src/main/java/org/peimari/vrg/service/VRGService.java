package org.peimari.vrg.service;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.CompetitionMap;
import org.peimari.rgdomain.Competitor;
import org.peimari.rgdomain.Course;
import org.peimari.rgdomain.Graphic;
import org.peimari.rgdomain.Graphic.Type;
import org.peimari.rgdomain.Point;

public class VRGService {

	private static final int MAXROWLENGTH = 1024*100;

	static LinkedHashSet<URL> latestGadgets = new LinkedHashSet<URL>();

	private final URL root;
	private final File cacheRoot;

	private String md5Hex;

	public static URL[] latestUrls() {
		synchronized (latestGadgets) {
			return latestGadgets.toArray(new URL[latestGadgets.size()]);
		}
	}

	public VRGService(URL root) {
		this.root = root;
		md5Hex = DigestUtils.md5Hex(root.toString());
		cacheRoot = new File("/Users/Shared/vrgcache/"
				+ md5Hex);
		cacheRoot.mkdirs();
		synchronized (latestGadgets) {
			latestGadgets.remove(root);
			latestGadgets.add(root);
		}
	}

	private static Map<String,Map<Integer, WeakReference<Competition>>> cache = Collections.synchronizedMap(new HashMap<String,Map<Integer, WeakReference<Competition>>>());
	
	public Competition load(int id) {
		try {
			Map<Integer, WeakReference<Competition>> map = cache.get(md5Hex);
			if(map == null) {
				map = Collections.synchronizedMap(new HashMap<Integer, WeakReference<Competition>>());
				cache.put(md5Hex, map);
			}
			WeakReference<Competition> weakReference = map.get(id);
			if(weakReference != null && weakReference.get() != null) {
				return weakReference.get();
			}
			Competition readCompetition = readCompetition(id);
			weakReference = new WeakReference<Competition>(readCompetition);
			map.put(id, weakReference);
			return readCompetition;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public List<Competition> getCompetitions() {
		try {
			return readCompetitions();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public Competition load(Competition object) {
		return load(object.getId());
	}

	public InputStream openMapStream(CompetitionMap map) throws IOException {
		return new FileInputStream(getMapFile(map));
	}

	public File getMapFile(CompetitionMap map) {
		return cachedFile(map.getId() + ".jpg");
	}

	Competition readCompetition(int id) throws Exception {
		InputStream openStream = FileUtils
				.openInputStream(cachedFile("kisat.txt"));
		List<String> lines = IOUtils.readLines(openStream, "iso8859-1");
		for (String l : lines) {
			String[] split = l.split("\\|");
			if (split[0].equals("" + id)) {
				Competition competition = createCompetition(l);
				readCompetition(competition);
				return competition;
			}
		}
		throw new Exception("Competition not found!");
	}

	private Competition createCompetition(String l) {
		String[] split = l.split("\\|");
		Competition competition = new Competition();
		competition.setId(Integer.parseInt(split[0]));
		competition.setName(split[3]);
		competition.setMapId(split[1]);
		Date d = new Date(java.sql.Date.valueOf(split[4]).getTime());
		competition.setDate(d);
		return competition;
	}

	private void readCompetition(Competition competition) throws IOException {
		readMap(competition);
		readCourses(competition);
		readCoursePoints(competition);
		readClasses(competition);
		readCompetitors(competition);
		readRoutes(competition);
	}

	private void readRoutes(Competition competition) throws IOException {

		List<String> lines = IOUtils.readLines(inputStream("merkinnat_"
				+ competition.getId() + ".txt"), "iso8859-1");
		for (String l : lines) {
			String[] flds = l.split("\\|");
			CompetitionClass competitionClass = competition
					.getCompetitionClass(Integer.parseInt(flds[0]));
			Competitor competitor = competitionClass.getCompetitor(Integer
					.parseInt(flds[1]));
			if(competitor == null) {
				continue;
			}
			String[] pStr = flds[4].split("N");
			Point[] routePoints = new Point[pStr.length - 1];
			for (int i = 1; i < pStr.length; i++) {
				String[] lonlatstr = pStr[i].split(";");
				Point point = new Point();
				point.setLon(Integer.parseInt(lonlatstr[0]));
				point.setLat(Integer.parseInt(lonlatstr[1]));
				routePoints[i - 1] = point;
			}
			competitor.setRoutePoints(routePoints);
		}
	}

	private void readCompetitors(Competition competition) throws IOException {
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream("kilpailijat_"
						+ competition.getId() + ".txt"), "iso8859-1"));
		String l = null;
		
		while ((l = safeReadNextLine(bufferedReader)) != null) {
			String[] flds = l.split("\\|");
			Competitor c = new Competitor();
			c.setId(Integer.parseInt(flds[0]));
			System.out.println("Read competitor" + c.getId());
			c.setName(flds[3]);
			// 4 ok disq?? start time?
			try {
				int time = Integer.parseInt(flds[5]);
				c.setTime(time);
			} catch (NumberFormatException e) {
			}

			if (false && flds.length > 8) {
				String[] split = flds[8].split(";");
				int[] splits = new int[split.length];
				for (int i = 1; i < splits.length; i++) {
					splits[i] = Integer.parseInt(split[i - 1]);
				}
				c.setSplitTimes(splits);
			}
			int classId = Integer.parseInt(flds[1]);
			CompetitionClass competitionClass = competition
					.getCompetitionClass(classId);
			if (competitionClass != null) {
				competitionClass.addCompetitor(c);
			}

		}

	}

	/**
	 * Safe read next line. At least Jukola 2012 has some insane rows that will eat insane amounts of memory.
	 * 
	 * @param bufferedReader
	 * @return
	 * @throws IOException 
	 */
	private String safeReadNextLine(BufferedReader bufferedReader) throws IOException {
		StringBuilder sb = new StringBuilder();
		int readCount = 0;
		while(true) {
			int read = bufferedReader.read();
			if(read == -1) {
				return null;
			}
			if(read == '\n') {
				break;
			}
			if(readCount < MAXROWLENGTH) {
				sb.appendCodePoint(read);
				readCount++;
			}
		}
		
		
		return sb.toString();
	}

	private InputStream inputStream(String string) throws FileNotFoundException {
		return new FileInputStream(cachedFile(string));
	}

	private void readCoursePoints(Competition competition) throws IOException {
		try {

			InputStream file = inputStream("ratapisteet_" + competition.getId()
					+ ".txt");
			List<String> lines = IOUtils.readLines(file, "iso8859-1");
			for (String l : lines) {
				String[] fields = l.split("\\|");
				Course course = competition.getCourse(Integer
						.parseInt(fields[0]));
				String[] courcepoints = fields[1].split("N");
				Point[] points = new Point[courcepoints.length];
				for (int i = 0; i < points.length; i++) {
					Point p = points[i] = new Point();
					String[] split = courcepoints[i].split(";");
					p.setLon(Integer.parseInt(split[0]));
					p.setLat(Integer.parseInt(split[1]));
				}
				course.setControlPoints(points);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void readClasses(Competition competition) throws IOException {
		try {
			List<String> lines = IOUtils.readLines(inputStream("sarjat_"
					+ competition.getId() + ".txt"), "iso8859-1");
			for (String l : lines) {
				String[] fields = l.split("\\|");
				CompetitionClass c = new CompetitionClass();
				c.setId(Integer.parseInt(fields[0]));
				c.setName(fields[1]);
				c.setCourse(competition.getCourse(c.getId()));
				competition.addCompetitionClass(c);
			}

		} catch (FileNotFoundException e) {
			// TODO: handle exception
		}

	}

	private void readCourses(Competition competition) throws IOException {
		try {
			InputStream file = inputStream("radat_" + competition.getId()
					+ ".txt");
			List<String> lines = IOUtils.readLines(file, "iso8859-1");
			for (String l : lines) {
				String[] fields = l.split("\\|");
				Course course = new Course();
				course.setId(Integer.parseInt(fields[0]));
				course.setName(fields[2]);
				competition.addCourse(course);
				String[] pointStrs = fields[3].split("N");
				Graphic[] points = new Graphic[pointStrs.length];
				for (int i = 0; i < points.length; i++) {
					Graphic p = points[i] = new Graphic();
					String[] pointFields = pointStrs[i].split(";");
					Type type = Graphic.Type.values()[Integer
							.parseInt(pointFields[0])];
					p.setType(type);
					p.setLon(Integer.parseInt(pointFields[1]));
					p.setLat(Integer.parseInt(pointFields[2]));
					if (type == Type.LINE || type == Type.CONTROLNUMBER) {
						p.setLon2(Integer.parseInt(pointFields[3]));
						p.setLat2(Integer.parseInt(pointFields[4]));
					}
				}
				course.setGraphics(points);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void readMap(Competition competition) throws IOException {
		List<String> lines = IOUtils.readLines(inputStream("kartat.txt"),
				"iso8859-1");
		for (String l : lines) {
			String[] split = l.split("\\|");
			if (split[0].equals("" + competition.getMapId())) {
				CompetitionMap map = new CompetitionMap();
				map.setId(Integer.parseInt(split[0]));
				map.setName(split[1]);
				competition.setMap(map);
				return;
			}
		}
	}

	List<Competition> readCompetitions() throws IOException {
		File file = cachedFile("kisat.txt");
		List<String> lines = FileUtils.readLines(file, "iso8859-1");
		List<Competition> competitions = new ArrayList<Competition>();
		for (String l : lines) {
			Competition competition = createCompetition(l);
			competitions.add(competition);
		}
		Collections.reverse(competitions);
		return competitions;
	}

	private File cachedFile(String name) {
		File cacheFile = new File(cacheRoot, name);
		cache(cacheFile);
		return cacheFile;
	}

	private void cache(File file) {
		if (!file.exists()) {
			try {
				URL url = new URL(root + file.getName());
				file.createNewFile();
				IOUtils.copy(url.openStream(), new FileOutputStream(file));
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void clearCache() {
		File[] list = cacheRoot.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith("txt");
			}
		});
		for (File file : list) {
			file.delete();
		}
		cache.remove(md5Hex);
	}

	public ImageDetails getMapDetails(Competition competition) {
		File file = new File(cacheRoot, competition.getMapId() + ".dser");
		// Use cache if exists, image reading may be bit slow
		if (file.exists()) {
			ObjectInputStream objectInputStream = null;
			ImageDetails readObject;
			try {
				objectInputStream = new ObjectInputStream(new FileInputStream(
						file));
				readObject = (ImageDetails) objectInputStream.readObject();
				return readObject;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					objectInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			throw new RuntimeException();
		} else {
			InputStream o = null;
			try {
				o = openMapStream(competition.getMap());
				// Create an image input stream on the image
				ImageInputStream iis = ImageIO.createImageInputStream(o);

				// Find all image readers that recognize the image format
				Iterator<ImageReader> iter = ImageIO.getImageReaders(iis);
				if (!iter.hasNext()) {
					// No readers found
					throw new RuntimeException("Couldn't read map image");
				}

				// Use the first reader
				ImageReader reader = (ImageReader) iter.next();

				BufferedImage bi = ImageIO.read(iis);
				ImageDetails imageDetails = new ImageDetails();
				imageDetails.width = bi.getWidth();
				imageDetails.height = bi.getHeight();
				imageDetails.formatName = reader.getFormatName();
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(
						new FileOutputStream(file));
				objectOutputStream.writeObject(imageDetails);
				objectOutputStream.close();
				return imageDetails;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					o.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			throw new RuntimeException("Reading image details failed!");

		}
	}

	public static final class ImageDetails implements Serializable {
		private int width;
		private int height;
		private String formatName;

		public String getFormatName() {
			return formatName;
		}

		public void setFormatName(String formatName) {
			this.formatName = formatName;
		}

		public int getWidth() {
			return width;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		public int getHeight() {
			return height;
		}

		public void setHeight(int height) {
			this.height = height;
		}
	}
}
