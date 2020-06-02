package me.crespel.runtastic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;

import com.topografix.gpx._1._1.BoundsType;

import me.crespel.runtastic.converter.ExportConverter;
import me.crespel.runtastic.mapper.SportSessionMapper;
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
				if (args.length < 2) {
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
				if (args.length < 2) {
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
				if (args.length < 3) {
					throw new IllegalArgumentException("Missing argument for action 'overlap'");
				}
				doOverlap(new File(args[2]), args[1], args.length > 3 ? new File(args[3]) : null, args.length > 4 ? args[4] : "gpx");
				break;
			case "compound":
				if (args.length < 3) {
					throw new IllegalArgumentException("Missing argument for action 'compound'");
				}
				doCompound(new File(args[2]), args[1], args.length > 3 ? new File(args[3]) : null, args.length > 4 ? args[4] : "gpx");
				break;
			case "help":
			default:
				printUsage();
				break;
		}
	}

	protected void printUsage() {
		System.out.println("Expected arguments:");
		System.out.println("  check    <export path>");
		System.out.println("  list     <export path> <filter>");
		System.out.println("  user     <export path>");
		System.out.println("  info     <export path> <activity id>");
		System.out.println("  photo    <export path> <photo id>");
		System.out.println("  convert  <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']");
		System.out.println("  overlap  <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']");
		System.out.println("  compound <export path> <activity id | 'all'> <destination path> ['gpx' | 'tcx']");
		System.out.println("  help");
	}

	private void doCheck(File path) throws FileNotFoundException, IOException {
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

		// Calculate overlapping sessions
		converter.doOverlap(fullsessions);
		displaySummary(fullsessions, false);

		System.out.println("Session statistics ...");
		System.out.println("      " + fullsessions.size() + " Sport Sessions found.");
		System.out.println("      " + gpxSessionCount + " Sport Sessions found with GPX data assigned. ");
		System.out.println("      " + heartRateDataCount + " Sport Sessions found with heart rate data assigned.");
		System.out.println("      " + imageSessionCount + " Sport Sessions found with totally " + imageCount + " photo(s) assigned.");
		System.out.println("      Total Distance: " + totDistance / 1000.0 + " [km],  Minimum distance: " + minDistance / 1000.0 + " [km],  Maximum distance: " + maxDistance / 1000.0 + " [km]");

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
			System.out.println("      Elevation: (+) " + session.getElevationGain() + " m , (-) " + session.getElevationLoss() + " m  /  " + ( session.getLatitude() != null ? "Latitude: " + session.getLatitude() + ", Longitude: " + session.getLongitude() + "  ( http://maps.google.com/maps?q=" + session.getLatitude() + "," + session.getLongitude() + " )" : "No GPS information available.") );
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
			int count = converter.exportSportSessions(path, dest, format);
			long endTime = System.currentTimeMillis();
			System.out.println(count + " activities successfully written to '" + dest + "' in " + (endTime - startTime) / 1000 + " seconds");
		} else {
			converter.exportSportSession(path, id, dest, format);
			System.out.println("Activity successfully written to '" + dest + "'");
		}
	}

	private void doOverlap(File path, String id, File dest, String format) throws FileNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		System.out.println("Load full list of sport session (inclusive all sub-data), this requires some time ...");
		List<SportSession> sessions = converter.convertSportSessions(path, format);
		converter.doOverlap(sessions);
		displaySummary(sessions, false);

		if(dest!=null) {
			System.out.println("Export '" + id + "' overlap sport session(s) ...");
			for( SportSession session : sessions) {
				List<SportSession> overlapSessions = session.getOverlapSessions();
				if((overlapSessions!=null) && (overlapSessions.size() > 0)) {
					if ("all".equalsIgnoreCase(id) || (id.equalsIgnoreCase(session.getId()))) {
						converter.exportSportSession(session, dest, format);
					}
				}
			}
		}

		long endTime = System.currentTimeMillis();
		System.out.println(sessions.size() + " activities successfully processed, in " + (endTime - startTime) / 1000 + " seconds");
	}

	private void doCompound(File path, String id, File dest, String format) throws FileNotFoundException, IOException {
		long startTime = System.currentTimeMillis();
		System.out.println("Load full list of sport session (inclusive all sub-data), this requires some time ...");
		List<SportSession> sessions = converter.convertSportSessions(path, format);
		converter.doCompound(sessions);
		displaySummary(sessions, false);

		if(dest!=null){
			System.out.println("Export '" + id + "' compound sport session(S) ...");
			for( SportSession session : sessions) {
				List<SportSession> compoundSessions = session.getCompoundSessions();
				if((compoundSessions!=null) && (compoundSessions.size() > 0)) {
					if ("all".equalsIgnoreCase(id) || (id.equalsIgnoreCase(session.getId()))) {
						converter.exportSportSession(session, dest, format);
					}
				}
			}
		}

		long endTime = System.currentTimeMillis();
		System.out.println(sessions.size() + " activities successfully processed, in " + (endTime - startTime) / 1000 + " seconds");
	}


	// display summary of sport sessions
	public void displaySummary(List<SportSession> sessions, boolean full) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Integer singleSessionCount = 0;
		Integer multiSessionCount = 0;
		Integer totMultiSessionCount = 0;
		Integer minNumOverlapSessions = 99999;
		Integer maxNumOverlapSessions = 0;
		Integer compoundSessionCount = 0;

		System.out.println("Sessions with 'empty' GPX track(s) ...");
		Integer emptyGPXTrackSessionCount = 0;
		for (SportSession session : sessions) {
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
		for (SportSession session : sessions) {
			if(session.getDistance()!=null) {
				if(session.getDistance() == 0) {
					System.out.println("      " + sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration() / 60000 + " min), Notes: '" + session.getNotes() + "'");
					zeroDistanceSessionCount += 1;
				}
			}
		}
		if (zeroDistanceSessionCount == 0) {
			System.out.println("      none");
		}

		System.out.println("Single sport session found ..." );
		for( SportSession session : sessions) {
			List<SportSession> overlapsessions = session.getOverlapSessions();
			if( (overlapsessions==null) || (overlapsessions.size() == 0) ) {
				singleSessionCount+=1;
				if( full ) System.out.println("      " + sdf.format(session.getStartTime())  + "["+singleSessionCount+"] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
			}
		}
		if (singleSessionCount == 0) {
			System.out.println("      none");
		} else {
			System.out.println("      " + singleSessionCount + " single sessions found." );
		}

		System.out.println("Multiple sport session found ..." );
		for( SportSession session : sessions) {
			List<SportSession> overlapsessions = session.getOverlapSessions();
			if( (overlapsessions!=null) && (overlapsessions.size() > 0) ) {
				multiSessionCount+=1;
				totMultiSessionCount+=overlapsessions.size();
				minNumOverlapSessions = Integer.min(minNumOverlapSessions, overlapsessions.size());
				maxNumOverlapSessions = Integer.max(maxNumOverlapSessions, overlapsessions.size());
				if( full ) System.out.println("      " + sdf.format(session.getStartTime())  + "["+multiSessionCount+"] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
				for( SportSession overlapsession : overlapsessions ) {
					if( full ) System.out.println("            " + sdf.format(overlapsession.getStartTime()) + " - ID: " + overlapsession.getId() + ", Sport Type: " + overlapsession.getSportTypeId() + ", Notes: '" + overlapsession.getNotes() + "'");
					if( overlapsessions.size() != overlapsession.getOverlapSessions().size() ) {
						// this "error" occured before normalizing the overlapping sessions
						System.out.println("            ----> Diff. count of overlapping sessions: " + overlapsession.getOverlapSessions().size() + " vs. " + overlapsessions.size() + ", ID=" + overlapsession.getId() + " with " + overlapsession.getOverlapSessions().size() + " sessions vs. ID=" + session.getId() + " with " + overlapsessions.size() + " sessions." );
					}
				}

				// check bounds ..
				if((session.getGpx()!=null) && (session.getGpx().getMetadata()!=null) && (session.getGpx().getMetadata().getBounds()!=null)) {
					BoundsType sessionBound = session.getGpx().getMetadata().getBounds();
					if ((session.getInnerBound() != null) && (session.getOuterBound() != null) ) {
						if(sessionBound.getMinlat().compareTo(session.getInnerBound().getMinlat()) == 1 ) {
							System.out.println("            ----> Inner bound mismatch, getMinlat: ID=" + session.getId() + "  with session bound: " + sessionBound.getMinlat() + " vs. overlap bound: " + session.getInnerBound().getMinlat() + "." );
						}
						if(sessionBound.getMaxlat().compareTo(session.getInnerBound().getMaxlat()) == -1 ) {
							System.out.println("            ----> Inner bound mismatch, getMaxlat: ID=" + session.getId() + "  with session bound: " + sessionBound.getMaxlat() + " vs. overlap bound: " + session.getInnerBound().getMaxlat() + "." );
						}
						if(sessionBound.getMinlon().compareTo(session.getInnerBound().getMinlon()) == 1 ) {
							System.out.println("            ----> Inner bound mismatch, getMinlon: ID=" + session.getId() + "  with session bound: " + sessionBound.getMinlon() + " vs. overlap bound: " + session.getInnerBound().getMinlon() + "." );
						}
						if(sessionBound.getMaxlon().compareTo(session.getInnerBound().getMaxlon()) == -1 ) {
							System.out.println("            ----> Inner bound mismatch, getMaxlon: ID=" + session.getId() + "  with session bound: " + sessionBound.getMaxlon() + " vs. overlap bound: " + session.getInnerBound().getMaxlon() + "." );
						}
						if(sessionBound.getMinlat().compareTo(session.getOuterBound().getMinlat()) == -1 ) {
							System.out.println("            ----> Outer bound mismatch, getMinlat: ID=" + session.getId() + "  with session bound: " + sessionBound.getMinlat() + " vs. overlap bound: " + session.getInnerBound().getMinlat() + "." );
						}
						if(sessionBound.getMaxlat().compareTo(session.getOuterBound().getMaxlat()) == 1 ) {
							System.out.println("            ----> Outer bound mismatch, getMaxlat: ID=" + session.getId() + "  with session bound: " + sessionBound.getMaxlat() + " vs. overlap bound: " + session.getInnerBound().getMaxlat() + "." );
						}
						if(sessionBound.getMinlon().compareTo(session.getOuterBound().getMinlon()) == -1 ) {
							System.out.println("            ----> Outer bound mismatch, getMinlon: ID=" + session.getId() + "  with session bound: " + sessionBound.getMinlon() + " vs. overlap bound: " + session.getInnerBound().getMinlon() + "." );
						}
						if(sessionBound.getMaxlon().compareTo(session.getOuterBound().getMaxlon()) == 1 ) {
							System.out.println("            ----> Outer bound mismatch, getMaxlon: ID=" + session.getId() + "  with session bound: " + sessionBound.getMaxlon() + " vs. overlap bound: " + session.getInnerBound().getMaxlon() + "." );
						}
					} else {
						System.out.println("            ----> Inner and/or outer bounds not available: ID=" + session.getId() + "  with " + overlapsessions.size() + " sessions." );
					}
				} else {
					System.out.println("            ----> Session bounds not available: ID=" + session.getId() + "  with " + overlapsessions.size() + " sessions." );
				}
			}
		}
		if (multiSessionCount == 0) {
			System.out.println("      none");
		} else {
			System.out.println("      " + multiSessionCount  + " multi sesions found.  Minimum " + minNumOverlapSessions + " and maximum " + maxNumOverlapSessions + " number of overlapping sessions.");
		}

		System.out.println("Compound sport session found ..." );
		for( SportSession session : sessions) {
			List<SportSession> compoundSessions = session.getCompoundSessions();
			if( (compoundSessions!=null) && (compoundSessions.size() > 0) ) {
				compoundSessionCount+=1;
				if( full ) System.out.println("      " + sdf.format(session.getStartTime())  + "["+compoundSessionCount+"] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() 
				+ "', Bounds[MinLat="+session.getGpx().getMetadata().getBounds().getMinlat()
				+ ", MaxLat="+session.getGpx().getMetadata().getBounds().getMaxlat()
				+ ", MinLon="+session.getGpx().getMetadata().getBounds().getMinlon()
				+ ", MaxLon="+session.getGpx().getMetadata().getBounds().getMaxlon()+"]");
			if( full ) {
				for( SportSession compoundSession : compoundSessions ) {
					System.out.println("            ID: " + compoundSession.getId() + ", Sport Type: " + compoundSession.getSportTypeId() 
					+ ", Notes: '" + compoundSession.getNotes() 
					+ "', Bounds[MinLat="+compoundSession.getGpx().getMetadata().getBounds().getMinlat()
					+ ", MaxLat="+compoundSession.getGpx().getMetadata().getBounds().getMaxlat()
					+ ", MinLon="+compoundSession.getGpx().getMetadata().getBounds().getMinlon()
					+ ", MaxLon="+compoundSession.getGpx().getMetadata().getBounds().getMaxlon()+"]");
				}
			}
		}
		}
		if (compoundSessionCount == 0) {
			System.out.println("      none");
		} else {
			System.out.println("      " + compoundSessionCount + " compound sessions found." );
		}

		System.out.println(sessions.size() + " activities successfully processed with max. deviation of " + distance(0, 0, 0, converter.diff.doubleValue(), "K") + " km.");
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
