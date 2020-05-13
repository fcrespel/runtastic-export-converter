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
		System.out.println("  list <export path> <filter>");
		System.out.println("  user <export path>");
		System.out.println("  info <export path> <activity id>");
		System.out.println("  photo <exort path> <photo id>");
		System.out.println("  convert <export path> <activity id | 'all'> <destination> ['gpx' | 'tcx']");
		System.out.println("  overlap <export path> ");
		System.out.println("  help");
	}

	protected void doList(File path) throws FileNotFoundException, IOException {
		doListWithFilter(path, null);
	}

	protected void doListWithFilter(File path, String filter) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SportSession> sessions = converter.listSportSessions(path,false);
		for (SportSession session : sessions) {
			if( filter == null || session.contains(filter) ) {
				System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration()/60000 + " min), Notes: '" + session.getNotes() + "'");
			}
		}
	}

	protected void doUser(File path) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		User user = converter.getUser(path);
		System.out.println(sdf.format(user.getCreatedAt()) + " - ID: " + user.getFbUserId() );
		System.out.println("      Name: " + user.getFirstName() + " " + user.getLastName() + ",  Birthday: " + user.getBirthday() + ",  City: " + user.getCityName() );
		System.out.println("      Mail: " + user.getEmail() + " (" + user.getFbProxiedEMail() + ")");
		System.out.println("      Gender: " + user.getGender() + ", Height: " + user.getHeight() + ", Weight: " + user.getWeight() + ", Language: " + user.getLanguage() );
		System.out.println("      Created At: " + sdf.format(user.getCreatedAt()) + ",  Confirmed At: " + sdf.format(user.getConfirmedAt()) + ",  Last Sign-in At: " + sdf.format(user.getLastSignInAt()) + ",  Updated At: " + sdf.format(user.getUpdatedAt()) );
	}

	protected void doInfo(File path, String id) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SportSession session = converter.getSportSession(path, id);
		if ( session != null ) {
			System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() );
			System.out.println("      Sport Type: " + session.getSportTypeId() + ", Surface Type: " + session.getSurfaceId() + ", Feeling Id: " + session.getSubjectiveFeelingId());
			System.out.println("      Duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration()/60000 + " min)");
			System.out.println("      Distance: " + (session.getDistance()!=null ? session.getDistance()/1000.0 : "n/a") + " km, Calories: " + session.getCalories());
			System.out.println("      Avg Pace: " + (session.getDurationPerKm()!=null ? session.getDurationPerKm()/60000.0 : "n/a") + " min/km");
			System.out.println("      Avg Speed: " + session.getAverageSpeed() + " km/h, Max Speed: " + session.getMaxSpeed() + " km/h");
			System.out.println("      Start: " + sdf.format(session.getStartTime()) + ", End: " + sdf.format(session.getEndTime()) + ", Created: " + sdf.format(session.getCreatedAt())  + ", Updated: " + sdf.format(session.getUpdatedAt()));
			System.out.println("      Elevation: (+) " + session.getElevationGain() + " m , (-) " + session.getElevationLoss() + " m  /  Latitude: " + session.getLatitude() + ", Longitude: " + session.getLongitude() + "  ( http://maps.google.com/maps?q=" + session.getLatitude() + "," + session.getLongitude() + " )");
			System.out.println("      Notes: " + session.getNotes());
			System.out.println("      Waypoints: " + ((session.getGpsData()==null) ? "0" : session.getGpsData().size()) + " JSON points, " + ((session.getGpx()==null) ? "0" : session.getGpx().getTrk().get(0).getTrkseg().get(0).getTrkpt().size()) + " GPX points.");
			System.out.println("      Photos:" + (session.getSessionAlbum()!=null ? session.getSessionAlbum().getPhotosIds().toString() : "none"));
			if ( session.getImages() != null ) {
				for ( ImagesMetaData image : session.getImages() ) {
					System.out.println("             [" + image.getId() + ".jpg] " + sdf.format(image.getCreatedAt()) + ": " + image.getDescription() + " ( http://maps.google.com/maps?q=" + image.getLatitude() + "," + image.getLongitude() + " )" );
				}
			}
			if ( session.getUser() != null ) {
				User user = session.getUser();
				System.out.println("      Name: " + user.getFirstName() + " " + user.getLastName() + ",  Birthday: " + user.getBirthday() + ",  City: " + user.getCityName() );
				System.out.println("      Mail: " + user.getEmail() + " (" + user.getFbProxiedEMail() + ")");
				System.out.println("      Gender: " + user.getGender() + ", Height: " + user.getHeight() + ", Weight: " + user.getWeight() + ", Language: " + user.getLanguage() );
				System.out.println("      Created At: " + sdf.format(user.getCreatedAt()) + ",  Confirmed At: " + sdf.format(user.getConfirmedAt()) + ",  Last Sign-in At: " + sdf.format(user.getLastSignInAt()) + ",  Updated At: " + sdf.format(user.getUpdatedAt()) );
			}
		}
	}

	protected void doPhoto(File path, String id) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SportSession session = converter.getSportSessionWithPhoto(path,id);
		for ( ImagesMetaData image : session.getImages()) {
			if( image != null ) {
				if( image.getId() == Integer.parseInt(id) ) {
					doInfo(path, session.getId());
					System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() );
					System.out.println("             [" + image.getId() + ".jpg] " + sdf.format(image.getCreatedAt()) + ": " + image.getDescription() + " ( http://maps.google.com/maps?q=" + image.getLatitude() + "," + image.getLongitude() + " )" );
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
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		System.out.println("Load full list of sport session (inclusive all sub-data), this requires some time ..." );
		List<SportSession> processedsessions = new ArrayList<>();
		List<SportSession> sessions = converter.convertSportSessions(path, "gpx");
		for (SportSession session : sessions) {
			if( processedsessions.contains(session) == false) {
				if (session.getGpx() != null && session.getGpx().getMetadata() != null && session.getGpx().getMetadata().getBounds() != null) {
					List<SportSession> overlapsessions = new ArrayList<>();
					for( SportSession session2 : sessions ) {
						if( !session.getId().equals(session2.getId())) {
							if( (session2.getGpx() != null && session2.getGpx().getMetadata() != null && session2.getGpx().getMetadata().getBounds() != null) ) {
								BoundsType bounds = session.getGpx().getMetadata().getBounds();
								BoundsType bounds2 = session2.getGpx().getMetadata().getBounds();
								BigDecimal diffMaxlat = bounds.getMaxlat().subtract(bounds2.getMaxlat()).abs();
								BigDecimal diffMaxlon = bounds.getMaxlon().subtract(bounds2.getMaxlon()).abs();
								BigDecimal diffMinlat = bounds.getMinlat().subtract(bounds2.getMinlat()).abs();
								BigDecimal diffMinlon = bounds.getMinlon().subtract(bounds2.getMinlon()).abs();
								BigDecimal diff = new BigDecimal(0.005); // max. allowed "deviation" between bounds of sessions
								if( (diffMaxlat.compareTo(diff) < 0) &&
									(diffMaxlon.compareTo(diff) < 0) &&
									(diffMinlat.compareTo(diff) < 0) && 
									(diffMinlon.compareTo(diff) < 0) ) {
									// overlapping sport session found
									overlapsessions.add(session2);
								}
							}
						}
					}
					// check against all other sessions done ..
					if( overlapsessions.size() > 0) {
						System.out.println("Matching sport session found ..." );
						System.out.println(sdf.format(session.getStartTime())  + "[1] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
						for( SportSession overlapsession : overlapsessions ) {
							System.out.println(sdf.format(overlapsession.getStartTime()) + "[x] - ID: " + overlapsession.getId() + ", Sport Type: " + overlapsession.getSportTypeId() + ", Notes: '" + overlapsession.getNotes() + "'");
						}			
					} else {
						System.out.println("NO matching sport session found ..." );
						System.out.println(sdf.format(session.getStartTime())  + "[1] - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", Notes: '" + session.getNotes() + "'");
					}
					// mark all found sport session as "processed", means they don't need to be processed again
					processedsessions.addAll(overlapsessions);
				}
				processedsessions.add(session);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println(sessions.size() + " activities successfully processed in " + (endTime - startTime) / 1000 + " seconds");
	}
}
