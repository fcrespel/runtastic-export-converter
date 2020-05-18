package me.crespel.runtastic.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.topografix.gpx._1._1.GpxType;

import me.crespel.runtastic.mapper.DelegatingSportSessionMapper;
import me.crespel.runtastic.mapper.SportSessionMapper;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.SportSessionAlbums;
import me.crespel.runtastic.model.User;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * Export directory converter.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
public class ExportConverter {

	public static final String SPORT_SESSIONS_DIR = "Sport-sessions";
	public static final String PHOTOS_DIR = "Photos";
	public static final String PHOTOS_META_DATA_DIR = "Photos" + File.separator + "Images-meta-data";
	public static final String PHOTOS_SPORT_SESSION_ALBUMS_DIR = "Photos" + File.separator + "Images-meta-data" + File.separator + "Sport-session-albums";
	public static final String USER_DIR = "User";
	public static final String DEFAULT_FORMAT = "tcx";

	protected final SportSessionParser parser = new SportSessionParser();
	protected final SportSessionMapper<?> mapper = new DelegatingSportSessionMapper();

	public List<SportSession> listSportSessions(File path, boolean full) throws FileNotFoundException, IOException {
		path = normalizeExportPath(path, SPORT_SESSIONS_DIR);
		List<SportSession> sessions = new ArrayList<>();
		File[] files = path.listFiles(file -> file.getName().endsWith(".json"));
		for (File file : files) {
			sessions.add(parser.parseSportSession(file,full));
		}

		Collections.sort(sessions);
		return sessions;
	}

	public User getUser(File path) throws FileNotFoundException, IOException {
		return parser.parseUser(new File(normalizeExportPath(path, USER_DIR), "user.json"));
	}

	public SportSession getSportSession(File path, String id) throws FileNotFoundException, IOException {
		return parser.parseSportSession(new File(normalizeExportPath(path, SPORT_SESSIONS_DIR), id + ".json"), true);
	}

	public SportSession getSportSessionWithPhoto(File path, String photoid) throws FileNotFoundException, IOException {
		String sessionid = null;

		File photofile = new File(normalizeExportPath(path, PHOTOS_DIR), photoid + ".jpg");
		if( photofile.exists() ) {
			// photo file found ...

			ImagesMetaData image = parser.parseImagesMetaData(new File(normalizeExportPath(path, PHOTOS_META_DATA_DIR), photoid + ".json"));
			if( image != null ) {
				// photo meta data file found ...

				// search trough sport session album data, to find sport session related to the photo
				File[] files = normalizeExportPath(path, PHOTOS_SPORT_SESSION_ALBUMS_DIR).listFiles(file -> file.getName().endsWith(".json"));
				for( File file : files ) {
					try {
						SportSessionAlbums mysessionalbum = parser.parseSportSessionAlbumsData(file);
						if (mysessionalbum.getPhotosIds().contains(photoid)) {
							// Sport session id found within sport session albums
							sessionid = mysessionalbum.getId();
						}
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}

		return getSportSession(path, sessionid);
	}

	public void convertSportSession(File path, String id, File dest, String format) throws FileNotFoundException, IOException {
		SportSession session = parser.parseSportSession(new File(normalizeExportPath(path, SPORT_SESSIONS_DIR), id + ".json"), true);
		if (dest.isDirectory()) {
			dest = new File(dest, buildFileName(session, format));
		}
		mapper.mapSportSession(session, format, dest);
	}

	public int convertSportSessions(File path, File dest, String format) throws FileNotFoundException, IOException {
		if (dest.exists() && !dest.isDirectory()) {
			throw new IllegalArgumentException("Destination '" + dest + "' is not a valid directory");
		}
		dest.mkdirs();
		File[] files = normalizeExportPath(path, SPORT_SESSIONS_DIR).listFiles(file -> file.getName().endsWith(".json"));
		Arrays.asList(files).parallelStream().forEach(file -> {
			try {
				SportSession session = parser.parseSportSession(file, true);
				if (session.getGpsData() != null || session.getHeartRateData() != null || session.getGpx() != null) {
					File destFile = new File(dest, buildFileName(session, format));
					mapper.mapSportSession(session, format, destFile);
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return files.length;
	}

	public List<SportSession> convertSportSessions(File path, String format) throws FileNotFoundException, IOException {
		File[] files = normalizeExportPath(path, SPORT_SESSIONS_DIR).listFiles(file -> file.getName().endsWith(".json"));
		List<SportSession> sessionlist = new ArrayList<>();
		Arrays.asList(files).parallelStream().forEach(file -> {
			try {
				SportSession session = parser.parseSportSession(file, true);
				if (session.getGpsData() != null || session.getHeartRateData() != null || session.getGpx() != null) {
					GpxType gpx = (GpxType) mapper.mapSportSession(session, format);
					session.setGpx(gpx);
				}
				sessionlist.add(session);
				System.out.print(".");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		System.out.println(" ... finished!");
		return sessionlist;
	}


	protected File normalizeExportPath(File path, String subpath) {
		// check if "Sport Session" sub-directory is provided ...
		if (SPORT_SESSIONS_DIR.equals(path.getName())) {
			// if yes, remove them.
			path = path.getParentFile();
		}
		// check if already path including sub-path is provided ...
		if (!subpath.equals(path.getName())) {
			// if not, add sub-path to path.
			path = new File(path, subpath);
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Export path '" + path + "' is not a valid directory");
		}
		return path;
	}


	protected String buildFileName(SportSession session, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
		return new StringBuilder("runtastic_")
			.append(sdf.format(session.getStartTime()))
			.append('_')
			.append(session.getId())
			.append('.')
			.append(format != null ? format : DEFAULT_FORMAT)
			.toString();
	}

}
