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

import org.apache.commons.io.FilenameUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import com.topografix.gpx._1._1.GpxType;

import me.crespel.runtastic.model.ElevationData;
import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.HeartRateData;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.SportSessionAlbums;

/**
 * Sport session parser.
 * This class reads sport sessions and related data exported as JSON.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class SportSessionParser {

	public static final String ELEVATION_DATA_DIR = "Elevation-data";
	public static final String GPS_DATA_DIR = "GPS-data";
	public static final String HEARTRATE_DATA_DIR = "Heart-rate-data";
	public static final String PHOTOS_DATA_DIR = "Photos\\Images-meta-data\\Sport-session-albums";

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
				// read GPS data from JSON file
				File gpsDataFileJSON = new File(new File(file.getParentFile(), GPS_DATA_DIR), file.getName());
				if (gpsDataFileJSON.exists()) {
					sportSession.setGpsData(parseGpsData(gpsDataFileJSON));
				}
				// read GPS data from GPX file (the runtastic export, starting from April-2020 contain GPS data as GPX files)
				File gpsDataFileGPX = new File(new File(file.getParentFile(), GPS_DATA_DIR), FilenameUtils.getBaseName(file.getName()) + ".gpx");
				if (gpsDataFileGPX.exists()) {
					// Load GPX file
					try {
						JAXBContext ctx = JAXBContext.newInstance(GpxType.class);
						Unmarshaller um = ctx.createUnmarshaller();
						JAXBElement<GpxType> root = (JAXBElement<GpxType>)um.unmarshal(gpsDataFileGPX);
						GpxType gpx = root.getValue();
						sportSession.setGpx(gpx);
					} catch (JAXBException e) {
						throw new RuntimeException(e);
					}
				}
				File heartRateDataFile = new File(new File(file.getParentFile(), HEARTRATE_DATA_DIR), file.getName());
				if (heartRateDataFile.exists()) {
					sportSession.setHeartRateData(parseHeartRateData(heartRateDataFile));
				}
				File photoSessionDataFile = new File(new File(file.getParentFile().getParentFile(), PHOTOS_DATA_DIR), file.getName());
				if (photoSessionDataFile.exists()) {
					sportSession.setPhotos(parseSportSessionAlbumsData(photoSessionDataFile));
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

	public SportSessionAlbums parseSportSessionAlbumsData(File file) throws FileNotFoundException, IOException {
		try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
			return parseSportSessionAlbumsData(is);
		}
	}

	public SportSessionAlbums parseSportSessionAlbumsData(InputStream is) throws FileNotFoundException, IOException {
		return mapper.readValue(is, new TypeReference<SportSessionAlbums>() {});
	}

}
