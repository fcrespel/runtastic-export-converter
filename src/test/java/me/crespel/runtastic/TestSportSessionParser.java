package me.crespel.runtastic;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.HeartRateData;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.SportSessionAlbums;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * SportSessionParser tests.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class TestSportSessionParser {

	private final SportSessionParser parser = new SportSessionParser();

	@Test
	public void testParseSportSession() throws JsonParseException, JsonMappingException, IOException {
		SportSession data = parser.parseSportSession(getClass().getResourceAsStream("SportSession.json"));
		System.out.println(data);
	}

	@Test
	public void testParseGpsData() throws JsonParseException, JsonMappingException, IOException {
		List<GpsData> data = parser.parseGpsData(getClass().getResourceAsStream("GpsData.json"));
		System.out.println(data);
	}

	@Test
	public void testParseHeartRateData() throws JsonParseException, JsonMappingException, IOException {
		List<HeartRateData> data = parser.parseHeartRateData(getClass().getResourceAsStream("HeartRateData.json"));
		System.out.println(data);
	}

	@Test
	public void testParseSportSessionAlbums() throws JsonParseException, JsonMappingException, IOException {
		SportSessionAlbums data = parser.parseSportSessionAlbumsData(getClass().getResourceAsStream("SportSessionAlbums.json"));
		System.out.println(data);
	}

}
