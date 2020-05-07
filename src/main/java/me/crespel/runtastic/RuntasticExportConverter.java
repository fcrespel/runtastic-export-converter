package me.crespel.runtastic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.List;

import me.crespel.runtastic.converter.ExportConverter;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;

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
			doList(new File(args[1]));
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
		case "help":
		default:
			printUsage();
			break;
		}
	}

	protected void printUsage() {
		System.out.println("Expected arguments:");
		System.out.println("  list <export path>");
		System.out.println("  info <export path> <activity id>");
		System.out.println("  photo <exort path> <photo id>");
		System.out.println("  convert <export path> <activity id | 'all'> <destination> ['gpx' | 'tcx']");
		System.out.println("  help");
	}

	protected void doList(File path) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SportSession> sessions = converter.listSportSessions(path,false);
		for (SportSession session : sessions) {
			System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", Sport Type: " + session.getSportTypeId() + ", duration: " + Duration.ofMillis(session.getDuration()).toString() + " (" + session.getDuration()/60000 + " min)");
		}
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
			System.out.println("      Elevation: (+) " + session.getElevationGain() + " m , (-) " + session.getElevationLoss() + " m  /  Latitude: " + session.getLatitude() + ", Longitude: " + session.getLongitude());
			System.out.println("      Notes: " + session.getNotes());
			System.out.println("      Waypoints: " + ((session.getGpsData()==null) ? "0" : session.getGpsData().size()) + " JSON points, " + ((session.getGpx()==null) ? "0" : session.getGpx().getTrk().get(0).getTrkseg().get(0).getTrkpt().size()) + " GPX points.");
			System.out.println("      Photos:" + (session.getSessionAlbum()!=null ? session.getSessionAlbum().getPhotosIds().toString() : "none"));
			if ( session.getImages() != null ) {
				for ( ImagesMetaData image : session.getImages() ) {
					System.out.println("             [" + image.getId() + ".jpg] " + image.toString() );
				}
			}
		}
	}

	protected void doPhoto(File path, String id) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SportSession> sessions = converter.listSportSessions(path,true);
		for (SportSession session : sessions) {
			if( session.getImages() != null ) {
				for ( ImagesMetaData image : session.getImages()) {
					if( image != null ) {
						if( image.getId() == Integer.parseInt(id) ) {
							System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() );
							System.out.println("             [" + image.getId() + ".jpg] " + image.toString() );
							doInfo(path, session.getId());
						}
					}
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

}
