package me.crespel.runtastic;

import org.junit.Test;

import me.crespel.runtastic.mapper.DelegatingSportSessionMapper;
import me.crespel.runtastic.mapper.SportSessionMapper;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * TcxSportSessionMapper tests.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class TestSportSessionMapper {

	private final SportSessionParser parser = new SportSessionParser();
	private final SportSessionMapper<?> mapper = new DelegatingSportSessionMapper();

	@Test
	public void testMapSportSession() throws Exception {
		SportSession sportSession = parser.parseSportSession(getClass().getResourceAsStream("SportSession.json"));
		sportSession.setGpsData(parser.parseGpsData(getClass().getResourceAsStream("GpsData.json")));
		sportSession.setHeartRateData(parser.parseHeartRateData(getClass().getResourceAsStream("HeartRateData.json")));
		mapper.mapSportSession(sportSession, "tcx", System.out);
		mapper.mapSportSession(sportSession, "gpx", System.out);
	}

}
