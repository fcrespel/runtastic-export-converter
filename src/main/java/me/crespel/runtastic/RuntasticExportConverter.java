package me.crespel.runtastic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.crespel.runtastic.mapper.TcxSportSessionMapper;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * Runtastic export converter main class.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class RuntasticExportConverter {

	protected final SportSessionParser parser = new SportSessionParser();
	protected final TcxSportSessionMapper tcxMapper = new TcxSportSessionMapper();

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
			doConvert(new File(args[1]), args[2], new File(args[3]));
			break;
		case "help":
		default:
			printUsage();
			break;
		}
	}

	protected void printUsage() {
		System.out.println("Expected arguments:");
		System.out.println("  list <sport sessions path>");
		System.out.println("  convert <sport sessions path> <activity id> <dest file.tcx>");
		System.out.println("  help");
	}

	protected void doList(File path) throws FileNotFoundException, IOException {
		List<SportSession> sessions = new ArrayList<>();
		File[] files = path.listFiles(file -> file.getName().endsWith(".json"));
		for (File file : files) {
			sessions.add(parser.parseSportSession(file));
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Collections.sort(sessions);
		for (SportSession session : sessions) {
			System.out.println(sdf.format(session.getStartTime()) + " - ID: " + session.getId() + ", type: " + session.getSportTypeId() + ", duration: " + session.getDuration() + "s");
		}
	}

	protected void doConvert(File path, String id, File dest) throws FileNotFoundException, IOException {
		SportSession session = parser.parseSportSession(new File(path, id + ".json"), true);
		tcxMapper.mapSportSession(session, dest);
		System.out.println("TCX file written to " + dest);
	}

}
