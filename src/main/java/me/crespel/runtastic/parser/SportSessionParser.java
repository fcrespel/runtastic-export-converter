package me.crespel.runtastic.parser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import me.crespel.runtastic.model.ElevationData;
import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.HeartRateData;
import me.crespel.runtastic.model.SportSession;

/**
 * Sport session parser.
 * This class reads sport sessions and related data exported as JSON.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class SportSessionParser {

	public static final String ELEVATION_DATA_DIR = "Elevation-data";
	public static final String GPS_DATA_DIR = "GPS-data";
	public static final String HEARTRATE_DATA_DIR = "Heart-rate-data";

	protected final ObjectMapper mapper = new ObjectMapper();

	public SportSession parseSportSession(File file) throws FileNotFoundException, IOException {
		return parseSportSession(file, false);
	}

	public SportSession parseSportSession(File file, boolean full) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			SportSession sportSession = parseSportSession(is);
			if (full) {
				File elevationDataFile = new File(new File(file.getParentFile(), ELEVATION_DATA_DIR), file.getName());
				if (elevationDataFile.exists()) {
					sportSession.setElevationData(parseElevationData(elevationDataFile));
				}
				File gpsDataFile = new File(new File(file.getParentFile(), GPS_DATA_DIR), file.getName());
				if (gpsDataFile.exists()) {
					sportSession.setGpsData(parseGpsData(gpsDataFile));
				}
				File heartRateDataFile = new File(new File(file.getParentFile(), HEARTRATE_DATA_DIR), file.getName());
				if (heartRateDataFile.exists()) {
					sportSession.setHeartRateData(parseHeartRateData(heartRateDataFile));
				}
			}
			return sportSession;
		}
	}

	public SportSession parseSportSession(InputStream is) throws FileNotFoundException, IOException {
		return mapper.readValue(is, SportSession.class);
	}

	public List<ElevationData> parseElevationData(File file) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return parseElevationData(is);
		}
	}

	public List<ElevationData> parseElevationData(InputStream is) throws FileNotFoundException, IOException {
		return mapper.readValue(is, new TypeReference<List<ElevationData>>() {});
	}

	public List<GpsData> parseGpsData(File file) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return parseGpsData(is);
		}
	}

	public List<GpsData> parseGpsData(InputStream is) throws FileNotFoundException, IOException {
		return mapper.readValue(is, new TypeReference<List<GpsData>>() {});
	}

	public List<HeartRateData> parseHeartRateData(File file) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return parseHeartRateData(is);
		}
	}

	public List<HeartRateData> parseHeartRateData(InputStream is) throws FileNotFoundException, IOException {
		return mapper.readValue(is, new TypeReference<List<HeartRateData>>() {});
	}

}
