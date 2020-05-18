package me.crespel.runtastic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.topografix.gpx._1._1.BoundsType;

import me.crespel.runtastic.converter.ExportConverter;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.User;

/**
 * Runtastic export converter main class.
 * 
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
public class RuntasticExportConverter {

	private BigDecimal diff = new BigDecimal(0.007); // max. allowed "deviation" between bounds of sessions

	protected final ExportConverter converter = new ExportConverter();

	public static void main(String[] args) {
		RuntasticExportConverter converter = new RuntasticExportConverter();
		try {
			converter.run(args);
		} catch (Throwable t) {
			System.out.println(t);
			converter.printUsage();
			System.exit(1);
		}
	}

	public void run(String[] args) throws Exception {
		String action = args.length > 0 ? args[0] : "";
		switch (action) {
			case "check":
				if (args.length < 1) {
					throw new IllegalArgumentException("Missing argument for action 'check'");
				}
				doCheck(new File(args[1]));
				break;
			case "list":
				if (args.length < 2) {
					throw new IllegalArgumentException("Missing argument for action 'list'");
				}
				doListWithFilter(new File(args[1]), args.length > 2 ? args[2] : null);
				break;
			case "user":
				if (args.length < 1) {
					throw new IllegalArgumentException("Missing argument for action 'user'");
				}
				doUser(new File(args[1]));
				break;
			case "info":
				if (args.length < 3) {
					throw new IllegalArgumentException("Missing argument for action 'info'");
				}
				doInfo(new File(args[1]), args[2]);
				break;
			case "photo":
				if (args.length < 3) {
					throw new IllegalArgumentException("Missing argument for action 'photo'");
				}
				doPhoto(new File(args[1]), args[2]);
				break;
			case "convert":
				if (args.length < 4) {
					throw new IllegalArgumentException("Missing arguments for action 'convert'");
				}
				doConvert(new File(args[1]), args[2], new File(args[3]), args.length > 4 ? args[4] : null);
				break;
			case "overlap":
				if (args.length < 1) {
					throw new IllegalArgumentException("Missing argument for action 'overlap'");
				}
				doOverlap(new File(args[1]));
				break;
			case "help":
			default:
				printUsage();
				break;
		}
	}

	protected void printUsage() {
		System.out.println("Expected arguments:");
		System.out.println("  check <export path>");
		System.out.println("  list <export path> <filter>");
		System.out.println("  user <export path>");
		System.out.println("  info <export path> <activity id>");
		System.out.println("  photo <export path> <photo id>");
		System.out.println("  convert <export path> <activity id | 'all'> <destination> ['gpx' | 'tcx']");
		System.out.println("  overlap <export path> ");
		System.out.println("  help");
	}

	private void doCheck(File path) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("Check curent export and provide some statistics ...");
		List<SportSession> sessions = converter.listSportSessions(path, false);
		System.out.println("      " + sessions.size() + " Sport Sessions found.");

		System.out.println("Load full list of sport session (inclusive all sub-data), this requires some time ...");
		List<SportSession> fullsessions = converter.convertSportSessions(path, "gpx");

		// Calculate statistics ..
		Integer gpxSessionCount = 0;
		Integer heartRateDataCount = 0;
		Integer imageSessionCount = 0;
		Integer imageCount = 0;
		Integer minDistance = Integer.MAX_VALUE, maxDistance = 0, totDistance = 0;
		for (SportSession session : fullsessions) {
			if (session.getGpx() != null)
				gpxSessionCount += 1;
			if (session.getHeartRateData() != null)
				heartRateDataCount += 1;
			if (session.getImages() != null) {
				imageSessionCount += 1;
				imageCount += session.getImages().size();
			}
			minDistance = Integer.min(minDistance, session.getDistance());
			maxDistance = Integer.max(maxDistance, session.getDistance());
			totDistance += session.getDistance();
		}

		System.out.println("Sessions with 'empty' GPX track(s) ...");
		Integer emptyGPXTrackSessionCount = 0;
		for (SportSession session : fullsessions) {
			if (session.getGpx() == null) {
				System.out.println("      " + sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration() / 60000 + " min), Notes: '" + session.getNotes() + "'");
				emptyGPXTrackSessionCount += 1;
			}
		}
		if (emptyGPXTrackSessionCount == 0) {
			System.out.println("      none");
		}

		System.out.println("Sessions with 'zero' distance ...");
		Integer zeroDistanceSessionCount = 0;
		for (SportSession session : fullsessions) {
			if (session.getDistance() == 0) {
				System.out.println("      " + sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration() / 60000 + " min), Notes: '" + session.getNotes() + "'");
				zeroDistanceSessionCount += 1;
			}
		}
		if (zeroDistanceSessionCount == 0) {
			System.out.println("      none");
		}

		// Calculate overlapping sessions
		doOverlap(fullsessions);
		displayOverlapSummary(fullsessions, false);

		System.out.println("Session statistics ...");
		System.out.println("      " + fullsessions.size() + " Sport Sessions found.");
		System.out.println("      " + gpxSessionCount + " Sport Sessions found with GPX data assigned. " + emptyGPXTrackSessionCount + " sessions found with no GPX data");
		System.out.println("      " + heartRateDataCount + " Sport Sessions found with heart rate data assigned.");
		System.out.println("      " + imageSessionCount + " Sport Sessions found with totally " + imageCount + " photo(s) assigned.");
		System.out.println("      Total Distance: " + totDistance / 1000.0 + " [km],  Minimum distance: " + minDistance / 1000.0 + " [km],  Maximum distance: " + maxDistance / 1000.0 + " [km],  " + zeroDistanceSessionCount + " sessions found with distance 'zero' found.");

	}

	protected void doList(File path) throws FileNotFoundException, IOException {
		doListWithFilter(path, null);
	}

	protected void doListWithFilter(File path, String filter) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SportSession> sessions = converter.listSportSessions(path, false);
		for (SportSession session : sessions) {
			if (filter == null || session.contains(filter)) {
				System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration() / 60000 + " min), Notes: '" + session.getNotes() + "'");
			}
		}
	}

	protected void doUser(File path) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		User user = converter.getUser(path);
		System.out.println(sdf.format(user.getCreatedAt()) + " - ID: " + user.getLogin() );
		System.out.println("      Name: " + user.getFirstName() + " " + user.getLastName() + ",  Birthday: " + user.getBirthday() + ",  City: " + user.getCityName() );
		System.out.println("      Mail: " + user.getEmail() + " (" + user.getFbProxiedEMail() + ")");
		System.out.println("      Gender: " + user.getGender() + ", Height: " + user.getHeight() + ", Weight: " + user.getWeight() + ", Language: " + user.getLanguage());
		System.out.println("      Created At: " + sdf.format(user.getCreatedAt()) + ",  Confirmed At: " + sdf.format(user.getConfirmedAt()) + ",  Last Sign-in At: " + sdf.format(user.getLastSignInAt()) + ",  Updated At: " + sdf.format(user.getUpdatedAt()));
	}

	protected void doInfo(File path, String id) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SportSession session = converter.getSportSession(path, id);
		if (session != null) {
			System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId());
			System.out.println("      Sport Type: " + session.getSportTypeId() + ", Surface Type: "	+ session.getSurfaceId() + ", Feeling Id: " + session.getSubjectiveFeelingId());
			System.out.println("      Duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration() / 60000 + " min)");
			System.out.println("      Distance: " + (session.getDistance() != null ? session.getDistance() / 1000.0 : "n/a") + " km, Calories: " + session.getCalories());
			System.out.println("      Avg Pace: " + (session.getDurationPerKm() != null ? session.getDurationPerKm() / 60000.0 : "n/a") + " min/km");
			System.out.println("      Avg Speed: " + session.getAverageSpeed() + " km/h, Max Speed: " + session.getMaxSpeed() + " km/h");
			System.out.println("      Start: " + sdf.format(session.getStartTime()) + ", End: " + sdf.format(session.getEndTime()) + ", Created: " + sdf.format(session.getCreatedAt()) + ", Updated: " + sdf.format(session.getUpdatedAt()));
			System.out.println("      Elevation: (+) " + session.getElevationGain() + " m , (-) " + session.getElevationLoss() + " m  /  Latitude: " + session.getLatitude() + ", Longitude: " + session.getLongitude() + ( session.getLatitude() != null ? "  ( http://maps.google.com/maps?q=" + session.getLatitude() + "," + session.getLongitude() + " )" : "") );
			System.out.println("      Notes: " + session.getNotes());
			System.out.println("      Waypoints: " + ((session.getGpsData() == null) ? "0" : session.getGpsData().size()) + " JSON points, " + ((session.getGpx() == null) ? "0" : session.getGpx().getTrk().get(0).getTrkseg().get(0).getTrkpt().size()) + " GPX points.");
			System.out.println("      Photos:" + (session.getSessionAlbum() != null ? session.getSessionAlbum().getPhotosIds().toString() : "none"));
			if (session.getImages() != null) {
				for (ImagesMetaData image : session.getImages()) {
					System.out.println("             [" + image.getId() + ".jpg] " + sdf.format(image.getCreatedAt()) + ": " + image.getDescription() + ( image.getLatitude() != null ? " ( http://maps.google.com/maps?q=" + image.getLatitude() + "," + image.getLongitude() + " )" : "") );
				}
			}
			if (session.getUser() != null) {
				User user = session.getUser();
				System.out.println("      Name: " + user.getFirstName() + " " + user.getLastName() + ",  Birthday: " + user.getBirthday() + ",  City: " + user.getCityName());
				System.out.println("      Mail: " + user.getEmail() + " (" + user.getFbProxiedEMail() + ")");
				System.out.println("      Gender: " + user.getGender() + ", Height: " + user.getHeight() + ", Weight: " + user.getWeight() + ", Language: " + user.getLanguage());
				System.out.println("      Created At: " + sdf.format(user.getCreatedAt()) + ",  Confirmed At: " + sdf.format(user.getConfirmedAt()) + ",  Last Sign-in At: " + sdf.format(user.getLastSignInAt()) + ",  Updated At: " + sdf.format(user.getUpdatedAt()));
			}
		}
	}

	protected void doPhoto(File path, String id) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SportSession session = converter.getSportSessionWithPhoto(path, id);
		for (ImagesMetaData image : session.getImages()) {
			if (image != null) {
				if (image.getId() == Integer.parseInt(id)) {
					doInfo(path, session.getId());
					System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId());
					System.out.println("             [" + image.getId() + ".jpg] " + sdf.format(image.getCreatedAt()) + ": " + image.getDescription() );
					if( image.getLatitude() != null ) System.out.println("             ( http://maps.google.com/maps?q=" + image.getLatitude() + "," + image.getLongitude() + " )");
				}
			}
		}
	}

	protected void doConvert(File path, String id, File dest, String format) throws FileNotFoundException, IOException {
		if ("all".equalsIgnoreCase(id)) {
			long startTime = System.currentTimeMillis();
			int count = converter.convertSportSessions(path, dest, format);
			long endTime = System.currentTimeMillis();
			System.out.println(count + " activities successfully written to '" + dest + "' in " + (endTime - startTime) / 1000 + " seconds");
		} else {
			converter.convertSportSession(path, id, dest, format);
			System.out.println("Activity successfully written to '" + dest + "'");
		}
	}

	private void doOverlap(File path) throws FileNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		System.out.println("Load full list of sport session (inclusive all sub-data), this requires some time ...");
		List<SportSession> sessions = converter.convertSportSessions(path, "gpx");
		doOverlap(sessions);
		displayOverlapSummary(sessions, true);
		long endTime = System.currentTimeMillis();
		System.out.println(sessions.size() + " activities successfully processed, in " + (endTime - startTime) / 1000 + " seconds");
	}

	// Loop through all sport session and add "overlapping" session to each sport
	// session
	private void doOverlap(List<SportSession> sessions) throws FileNotFoundException, IOException {
		// (1) search per session for all overlapping sessions
		// NOTE: This can result in different results; e.g.
		// - Session A, overlaps with B and C, but
		// - Session D, overlaps only with B and C (this because B & C are in range of
		// D, but not of A)
		// but expected is that all mention sessions above are calculated as
		// "overlapping"
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
				session.setOverlapSessions(overlapSessions);
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
			if( session.getOverlapSessions() != null ) {
				BoundsType innerBounds = null;
				BoundsType outerBounds = null;
				for (SportSession overlapSession : session.getOverlapSessions()) {
					BoundsType sessionBounds = overlapSession.getGpx().getMetadata().getBounds();;
					if( (innerBounds == null) && (outerBounds == null) ) {
						// init bounds with "any" existing bounds from sessions
						innerBounds = sessionBounds;
						outerBounds = sessionBounds;
					} else {
						// calculate "left" side of outer bounds ...
						if( sessionBounds.getMinlon().compareTo(outerBounds.getMinlon()) < 0 ) {
							outerBounds.setMinlon(sessionBounds.getMinlon());
						}
						// calculate "left" side of inner bounds ...
						if( sessionBounds.getMinlon().compareTo(innerBounds.getMinlon()) > 0 ) {
							innerBounds.setMinlon(sessionBounds.getMinlon());
						}
						// calculate "right" side of inner bounds ...
						if( sessionBounds.getMaxlon().compareTo(innerBounds.getMaxlon()) < 0 ) {
							innerBounds.setMaxlon(sessionBounds.getMaxlon());
						}
						// caluclate "right" side of outer bounds ...
						if( sessionBounds.getMaxlon().compareTo(outerBounds.getMaxlon()) > 0 ) {
							outerBounds.setMaxlon(sessionBounds.getMaxlon());
						}
						// calculate "top" side of outer bounds ...
						if( sessionBounds.getMaxlat().compareTo(outerBounds.getMaxlat()) > 0 ) {
							outerBounds.setMaxlat(sessionBounds.getMaxlat());
						}
						// caluclate "top" side of inner bounds ...
						if( sessionBounds.getMaxlat().compareTo(innerBounds.getMaxlat()) < 0 ) {
							innerBounds.setMaxlat(sessionBounds.getMaxlat());
						}
						// calculate "lower" side of inner bounds ...
						if( sessionBounds.getMinlat().compareTo(innerBounds.getMinlat()) > 0 ) {
							innerBounds.setMinlat(sessionBounds.getMinlat());
						}
						// calculate "lower" side of outer bounds ...
						if( sessionBounds.getMinlat().compareTo(outerBounds.getMinlat()) < 0 ) {
							outerBounds.setMinlat(sessionBounds.getMinlat());
						}
					}
				}
				// Store inner and outer bounds in sport session
				session.setInnerBound(innerBounds);
				session.setOuterBound(outerBounds);
			}
		}
	}

	private void addOverlapSessions(List<SportSession> normalizedOverlapSessions, SportSession overlapSession) {
		if( overlapSession.getOverlapSessions() != null ) {
			for (SportSession innerOverlapSession : overlapSession.getOverlapSessions()) {
				if( (innerOverlapSession != null) && (normalizedOverlapSessions != null) && (!normalizedOverlapSessions.contains(innerOverlapSession)) ) {
					normalizedOverlapSessions.add(innerOverlapSession);
					addOverlapSessions(normalizedOverlapSessions, innerOverlapSession);
				}
			}
		}
	}

	// display summary of "overlapping" sport sessions
	private void displayOverlapSummary(List<SportSession> sessions, boolean full) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Integer singleSessions = 0;
		Integer multiSessions = 0;
		Integer totMultiSessions = 0;
		if( full ) System.out.println("Multiple sport session found ..." );
		for( SportSession session : sessions) {
			List<SportSession> overlapsessions = session.getOverlapSessions();
			if( (overlapsessions!=null) && (overlapsessions.size() > 0) ) {
				multiSessions+=1;
				totMultiSessions+=overlapsessions.size();
				if( full ) System.out.println("      " + sdf.format(session.getStartTime())  + "["+multiSessions+"] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
				for( SportSession overlapsession : overlapsessions ) {
					if( full ) System.out.println("            " + sdf.format(overlapsession.getStartTime()) + " - ID: " + overlapsession.getId() + ", Sport Type: " + overlapsession.getSportTypeId() + ", Notes: '" + overlapsession.getNotes() + "'");
					if( overlapsessions.size() != overlapsession.getOverlapSessions().size() ) {
						System.out.println("            ----> Diff. count of overlapping sessions: " + overlapsession.getOverlapSessions().size() + " vs. " + overlapsessions.size() + ", ID=" + overlapsession.getId() + " with " + overlapsession.getOverlapSessions().size() + " sessions vs. ID=" + session.getId() + " with " + overlapsessions.size() + " sessions." );
					}
				}
			}
		}
		if( full ) System.out.println("Single sport session found ..." );
		for( SportSession session : sessions) {
			List<SportSession> overlapsessions = session.getOverlapSessions();
			if( (overlapsessions==null) || (overlapsessions.size() == 0) ) {
				singleSessions+=1;
				if( full ) System.out.println("      " + sdf.format(session.getStartTime())  + "["+singleSessions+"] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
			}
		}
		System.out.println(sessions.size() + " activities successfully processed with max. deviation of " + distance(0, 0, 0, diff.doubleValue(), "K") + " km, " + singleSessions + " single sessions, " + multiSessions + " multi sesions of total " + totMultiSessions + " sessions found");
	}


	// See https://www.geodatasource.com/developers/java
	private static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
		if ((lat1 == lat2) && (lon1 == lon2)) {
			return 0;
		}
		else {
			double theta = lon1 - lon2;
			double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			if (unit.equals("K")) {
				dist = dist * 1.609344;
			} else if (unit.equals("N")) {
				dist = dist * 0.8684;
			}
			return (dist);
		}
	}

}
