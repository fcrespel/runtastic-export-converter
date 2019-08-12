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
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * Export directory converter.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class ExportConverter {

	public static final String SPORT_SESSIONS_DIR = "Sport-sessions";
	public static final String DEFAULT_FORMAT = "tcx";

	protected final SportSessionParser parser = new SportSessionParser();
	protected final SportSessionMapper<?> mapper = new DelegatingSportSessionMapper();

	public List<SportSession> listSportSessions(File path) throws FileNotFoundException, IOException {
		path = normalizeSportSessionPath(path);
		List<SportSession> sessions = new ArrayList<>();
		File[] files = path.listFiles(file -> file.getName().endsWith(".json"));
		for (File file : files) {
			sessions.add(parser.parseSportSession(file));
		}

		Collections.sort(sessions);
		return sessions;
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
				if (session.getGpsData() != null || session.getHeartRateData() != null) {
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
