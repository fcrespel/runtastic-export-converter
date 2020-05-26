package me.crespel.runtastic.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.topografix.gpx._1._1.BoundsType;

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

	public BigDecimal diff = new BigDecimal(0.0005); // max. allowed "deviation" between bounds of sessions

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

		if( sessionid == null) {
			throw new FileNotFoundException("Sport Session file not found for photo id = '" + photoid + "'");
		}

		return getSportSession(path, sessionid);
	}

	public List<SportSession> convertSportSessions(File path, String format) throws FileNotFoundException, IOException {
		File[] files = normalizeExportPath(path, SPORT_SESSIONS_DIR).listFiles(file -> file.getName().endsWith(".json"));
		List<SportSession> sessionlist = new ArrayList<>();
		Arrays.asList(files).parallelStream().forEach(file -> {
			try {
				SportSession session = parser.parseSportSession(file, true);
				if (session.getGpsData() != null || session.getHeartRateData() != null || session.getGpx() != null) {
					mapper.mapSportSession(session, format);
				}
				sessionlist.add(session);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return sessionlist;
	}

	public void exportSportSession(SportSession session, File dest, String format) throws FileNotFoundException, IOException {
		if (dest.isDirectory()) {
			dest = new File(dest, buildFileName(session, format));
		}
		mapper.mapSportSession(session, format, dest);
	}

	public void exportSportSession(File path, String id, File dest, String format) throws FileNotFoundException, IOException {
		SportSession session = parser.parseSportSession(new File(normalizeExportPath(path, SPORT_SESSIONS_DIR), id + ".json"), true);
		exportSportSession(session, dest, format);
	}

	public int exportSportSessions(File path, File dest, String format) throws FileNotFoundException, IOException {
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


	// Loop through all sport session and add "overlapping" session to each sport session
	public void doOverlap(List<SportSession> sessions) {
		// (1) search per session for all overlapping sessions
		// NOTE: This can result in different results; e.g.
		// - Session A, overlaps with B and C, but
		// - Session D, overlaps only with B and C (this because B & C are in range of D, but not of A)
		// but expected is that all mention sessions above are calculated as "overlapping"
		// This circumstance will be "normalized" in a second step.
		for (SportSession session : sessions) {
			if (session.getGpx() != null && session.getGpx().getMetadata() != null
					&& session.getGpx().getMetadata().getBounds() != null) {
				List<SportSession> overlapSessions = new ArrayList<>();
				for (SportSession session2 : sessions) {
					if (!session.getId().equals(session2.getId())) {
						if ((session2.getGpx() != null && session2.getGpx().getMetadata() != null
								&& session2.getGpx().getMetadata().getBounds() != null)) {
							BoundsType bounds = session.getGpx().getMetadata().getBounds();
							BoundsType bounds2 = session2.getGpx().getMetadata().getBounds();
							BigDecimal diffMaxlat = bounds.getMaxlat().subtract(bounds2.getMaxlat()).abs();
							BigDecimal diffMaxlon = bounds.getMaxlon().subtract(bounds2.getMaxlon()).abs();
							BigDecimal diffMinlat = bounds.getMinlat().subtract(bounds2.getMinlat()).abs();
							BigDecimal diffMinlon = bounds.getMinlon().subtract(bounds2.getMinlon()).abs();
							if ((diffMaxlat.compareTo(diff) < 0) && (diffMaxlon.compareTo(diff) < 0)
									&& (diffMinlat.compareTo(diff) < 0) && (diffMinlon.compareTo(diff) < 0)) {
								// overlapping sport session found
								overlapSessions.add(session2);
							}
						}
					}
				}
				if( overlapSessions.size()>0) {
					session.setOverlapSessions(overlapSessions);
				}
			}
		}
		// (2) Normalize overlapping sport sessions
		for (SportSession session : sessions) {
			if( session.getOverlapSessions() != null ) {
				List<SportSession> normalizedOverlapSessions = new ArrayList<>();
				for (SportSession overlapSession : session.getOverlapSessions()) {
					addOverlapSessions(normalizedOverlapSessions, overlapSession);
				}
				session.setOverlapSessions(normalizedOverlapSessions);
			}
		}
		// (3) Calculate inner and outer bound (of normalized overlapping sessions)
		for (SportSession session : sessions) {
			calculateInnerAndOuterBound(session);
		}
	}

	private void addOverlapSessions(List<SportSession> normalizedOverlapSessions, SportSession overlapSession) {
		if((normalizedOverlapSessions != null) && (overlapSession.getOverlapSessions() != null)) {
			for (SportSession innerOverlapSession : overlapSession.getOverlapSessions()) {
				if( (innerOverlapSession != null) && (!normalizedOverlapSessions.contains(innerOverlapSession)) ) {
					normalizedOverlapSessions.add(innerOverlapSession);
					addOverlapSessions(normalizedOverlapSessions, innerOverlapSession);
				}
			}
		}
	}

	public void calculateInnerAndOuterBound(SportSession session) {
		if( session.getOverlapSessions() != null ) {
			BoundsType innerBounds = null;
			BoundsType outerBounds = null;
			for (SportSession overlapSession : session.getOverlapSessions()) {
				BoundsType sessionBounds = overlapSession.getGpx().getMetadata().getBounds();;
				if( (innerBounds == null) && (outerBounds == null) ) {
					// init bounds with "any" existing bounds from sessions
					innerBounds = new BoundsType();
					innerBounds.setMaxlat(sessionBounds.getMaxlat());
					innerBounds.setMinlat(sessionBounds.getMinlat());
					innerBounds.setMaxlon(sessionBounds.getMaxlon());
					innerBounds.setMinlon(sessionBounds.getMinlon());
					outerBounds = new BoundsType();
					outerBounds.setMaxlat(sessionBounds.getMaxlat());
					outerBounds.setMinlat(sessionBounds.getMinlat());
					outerBounds.setMaxlon(sessionBounds.getMaxlon());
					outerBounds.setMinlon(sessionBounds.getMinlon());
				} else {
					// calculate "left" side of inner bounds ...
					innerBounds.setMinlon(sessionBounds.getMinlon().max(innerBounds.getMinlon()));
					// calculate "right" side of inner bounds ...
					innerBounds.setMaxlon(sessionBounds.getMaxlon().min(innerBounds.getMaxlon()));
					// caluclate "top" side of inner bounds ...
					innerBounds.setMaxlat(sessionBounds.getMaxlat().min(innerBounds.getMaxlat()));
					// calculate "lower" side of inner bounds ...
					innerBounds.setMinlat(sessionBounds.getMinlat().max(innerBounds.getMinlat()));
					// calculate "left" side of outer bounds ...
					outerBounds.setMinlon(sessionBounds.getMinlon().min(outerBounds.getMinlon()));
					// caluclate "right" side of outer bounds ...
					outerBounds.setMaxlon(sessionBounds.getMaxlon().max(outerBounds.getMaxlon()));
					// calculate "top" side of outer bounds ...
					outerBounds.setMaxlat(sessionBounds.getMaxlat().max(outerBounds.getMaxlat()));
					// calculate "lower" side of outer bounds ...
					outerBounds.setMinlat(sessionBounds.getMinlat().min(outerBounds.getMinlat()));
				}
			}
			// Store inner and outer bounds in sport session
			session.setInnerBound(innerBounds);
			session.setOuterBound(outerBounds);
		}
	}

	// Loop through all sport session and search for "adjuncted" sessions
	public void doCompound(List<SportSession> sessions) {
		// calculate overlapping sessions, as those are not considered as "compound" session
		doOverlap(sessions);

		// (1) search per session for all "adjuncted sessions
		for (SportSession session : sessions) {
			if (session.getGpx() != null && session.getGpx().getMetadata() != null && session.getGpx().getMetadata().getBounds() != null) {
				List<SportSession> compoundSessions = new ArrayList<>();
				for (SportSession session2 : sessions) {
					if (!session.getId().equals(session2.getId())) {
						if( (session.getOverlapSessions()==null) ||
						    ((session.getOverlapSessions()!=null) && (!session.getOverlapSessions().contains(session2))) ) {
							// process session only if it isn't an "overlapping" session
							if(isCompound(session,session2))
							{
								// compound sport session found
								compoundSessions.add(session2);
							}
						}
					}
				}
				if( compoundSessions.size() > 0 ) {
					session.setCompoundSessions(compoundSessions);
				}
			}
		}
		// (2) Normalize compound sport sessions (add all compound sessions to one "chain")
		for (SportSession session : sessions) {
			if( session.getCompoundSessions() != null ) {
				List<SportSession> normalizedCompoundSessions = new ArrayList<>();
				for (SportSession compoundSession : session.getCompoundSessions()) {
					addCompoundSessions(normalizedCompoundSessions, compoundSession);
				}
				session.setCompoundSessions(normalizedCompoundSessions);
			}
		}
	}

	public boolean isCompound(SportSession session, SportSession session2) {
		if ((session2.getGpx() != null && session2.getGpx().getMetadata() != null && session2.getGpx().getMetadata().getBounds() != null)) {
			BoundsType bounds = session.getGpx().getMetadata().getBounds();
			BoundsType bounds2 = session2.getGpx().getMetadata().getBounds();
			BigDecimal diffTop = bounds.getMaxlat().subtract(bounds2.getMinlat()).abs();
			BigDecimal diffRight = bounds.getMaxlon().subtract(bounds2.getMinlon()).abs();
			BigDecimal diffDown = bounds.getMinlat().subtract(bounds2.getMaxlat()).abs();
			BigDecimal diffLeft = bounds.getMinlon().subtract(bounds2.getMaxlon()).abs();
			if (((diffTop.compareTo(diff)  < 0) && (bounds.getMinlon().compareTo(bounds2.getMaxlon())<=0) && (bounds.getMaxlon().compareTo(bounds2.getMinlon())>=0)) 
			||  ((diffRight.compareTo(diff)< 0) && (bounds.getMinlat().compareTo(bounds2.getMaxlat())<=0) && (bounds.getMaxlat().compareTo(bounds2.getMinlat())>=0)) 
			||  ((diffDown.compareTo(diff) < 0) && (bounds.getMinlon().compareTo(bounds2.getMaxlon())<=0) && (bounds.getMaxlon().compareTo(bounds2.getMinlon())>=0)) 
			||  ((diffLeft.compareTo(diff) < 0) && (bounds.getMinlat().compareTo(bounds2.getMaxlat())<=0) && (bounds.getMaxlat().compareTo(bounds2.getMinlat())>=0)) ) {
				// compound sport session found
				return true;
			}
		}
		return false;
	}

	private void addCompoundSessions(List<SportSession> normalizedCompoundSessions, SportSession compoundSession) {
		if((normalizedCompoundSessions != null) && (compoundSession.getCompoundSessions() != null)) {
			for (SportSession innerCompoundSession : compoundSession.getCompoundSessions()) {
				if( (innerCompoundSession != null) && (!normalizedCompoundSessions.contains(innerCompoundSession)) ) {
					normalizedCompoundSessions.add(innerCompoundSession);
					addCompoundSessions(normalizedCompoundSessions, innerCompoundSession);
				}
			}
		}
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
