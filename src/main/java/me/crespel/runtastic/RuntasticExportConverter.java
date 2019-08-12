package me.crespel.runtastic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import me.crespel.runtastic.converter.ExportConverter;
import me.crespel.runtastic.model.SportSession;

/**
 * Runtastic export converter main class.
 * @author Fabien CRESPEL (fabien@crespel.net)
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
		System.out.println("  convert <export path> <activity id | 'all'> <destination> ['gpx' | 'tcx']");
		System.out.println("  help");
	}

	protected void doList(File path) throws FileNotFoundException, IOException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<SportSession> sessions = converter.listSportSessions(path);
		for (SportSession session : sessions) {
			System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", type: " + session.getSportTypeId() + ", duration: " + session.getDuration() + "s");
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
