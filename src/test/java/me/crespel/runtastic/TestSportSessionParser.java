package me.crespel.runtastic;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.HeartRateData;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.Shoe;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.model.SportSessionAlbums;
import me.crespel.runtastic.model.User;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * SportSessionParser tests.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @author Christian IMFELD (imfeldc@gmail.com)
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

	@Test
	public void testParseImagesMetaData() throws JsonParseException, JsonMappingException, IOException {
		ImagesMetaData data = parser.parseImagesMetaData(getClass().getResourceAsStream("ImagesMetaData.json"));
		System.out.println(data);
	}

	@Test
	public void testParseImagesMetaData2() throws JsonParseException, JsonMappingException, IOException {
		ImagesMetaData data = parser.parseImagesMetaData(getClass().getResourceAsStream("ImagesMetaData2.json"));
		System.out.println(data);
	}

	@Test
	public void testParseShoe() throws JsonParseException, JsonMappingException, IOException {
		Shoe data = parser.parseShoe(getClass().getResourceAsStream("Shoe.json"));
		System.out.println(data);
	}

	@Test
	public void testParseUser() throws JsonParseException, JsonMappingException, IOException {
		User data = parser.parseUser(getClass().getResourceAsStream("User.json"));
		System.out.println(data);
	}

}
