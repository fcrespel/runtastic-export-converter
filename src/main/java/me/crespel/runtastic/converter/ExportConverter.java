package me.crespel.runtastic.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import me.crespel.runtastic.mapper.DelegatingSportSessionMapper;
import me.crespel.runtastic.mapper.SportSessionMapper;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.SportSessionAlbums;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * Export directory converter.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
public class ExportConverter {

	public static final String SPORT_SESSIONS_DIR = "Sport-sessions";
	public static final String PHOTOS_DIR = "Photos";
	public static final String PHOTOS_META_DATA_DIR = "Photos\\Images-meta-data";
	public static final String PHOTOS_SPORT_SESSION_ALBUMS_DIR = "Photos\\Images-meta-data\\Sport-session-albums";
	public static final String DEFAULT_FORMAT = "tcx";

	protected final SportSessionParser parser = new SportSessionParser();
	protected final SportSessionMapper<?> mapper = new DelegatingSportSessionMapper();

	public List<SportSession> listSportSessions(File path, boolean full) throws FileNotFoundException, IOException {
		path = normalizeSportSessionPath(path);
		List<SportSession> sessions = new ArrayList<>();
		File[] files = path.listFiles(file -> file.getName().endsWith(".json"));
		for (File file : files) {
			sessions.add(parser.parseSportSession(file,full));
		}

		Collections.sort(sessions);
		return sessions;
	}

	public SportSession getSportSession(File path, String id) throws FileNotFoundException, IOException {
		path = normalizeSportSessionPath(path);
		return parser.parseSportSession(new File(path, id + ".json"), true);
	}

	public SportSession getSportSessionWithPhoto(File path, String photoid) throws FileNotFoundException, IOException {
		String sessionid = null;

		File photofile = new File(normalizePhotoPath(path), photoid + ".jpg");
		if( photofile.exists() ) {
			// photo file found ...

			ImagesMetaData image = parser.parseImagesMetaData(new File(normalizePhotoMetaDataPath(path), photoid + ".json"));
			if( image != null ) {
				// photo meta data file found ...

				// search trough sport session album data, to find sport session related to the photo
				File[] files = normalizePhotoSportSessionAlbumPath(path).listFiles(file -> file.getName().endsWith(".json"));
				for( File file : files ) {
					try {
						SportSessionAlbums mysessionalbum = parser.parseSportSessionAlbumsData(file);
						for( String myphotoid : mysessionalbum.getPhotosIds() ) {
							if( myphotoid.compareTo(photoid)==0) {
								// Sport session id found within sport session albums
								sessionid = mysessionalbum.getId();
								break;
							}
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
		path = normalizeSportSessionPath(path);
		SportSession session = parser.parseSportSession(new File(path, id + ".json"), true);
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
		path = normalizeSportSessionPath(path);
		File[] files = path.listFiles(file -> file.getName().endsWith(".json"));
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

	protected File normalizeSportSessionPath(File path) {
		if (!SPORT_SESSIONS_DIR.equals(path.getName())) {
			path = new File(path, SPORT_SESSIONS_DIR);
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Export path '" + path + "' is not a valid directory");
		}
		return path;
	}

	protected File normalizePhotoPath(File path) {
		if (!PHOTOS_DIR.equals(path.getName())) {
			path = new File(path, PHOTOS_DIR);
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Export path '" + path + "' is not a valid directory");
		}
		return path;
	}

	protected File normalizePhotoMetaDataPath(File path) {
		if (!PHOTOS_META_DATA_DIR.equals(path.getName())) {
			path = new File(path, PHOTOS_META_DATA_DIR);
		}
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Export path '" + path + "' is not a valid directory");
		}
		return path;
	}

	protected File normalizePhotoSportSessionAlbumPath(File path) {
		if (!PHOTOS_SPORT_SESSION_ALBUMS_DIR.equals(path.getName())) {
			path = new File(path, PHOTOS_SPORT_SESSION_ALBUMS_DIR);
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
