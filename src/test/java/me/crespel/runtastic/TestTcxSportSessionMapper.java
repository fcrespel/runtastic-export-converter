package me.crespel.runtastic;

import org.junit.Test;

import me.crespel.runtastic.mapper.TcxSportSessionMapper;
import me.crespel.runtastic.model.SportSession;
import me.crespel.runtastic.parser.SportSessionParser;

/**
 * TcxSportSessionMapper tests.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class TestTcxSportSessionMapper {

	private final SportSessionParser parser = new SportSessionParser();
	private final TcxSportSessionMapper tcxMapper = new TcxSportSessionMapper();

	@Test
	public void testMapSportSession() throws Exception {
		SportSession sportSession = parser.parseSportSession(getClass().getResourceAsStream("SportSession.json"));
		sportSession.setGpsData(parser.parseGpsData(getClass().getResourceAsStream("GpsData.json")));
		sportSession.setHeartRateData(parser.parseHeartRateData(getClass().getResourceAsStream("HeartRateData.json")));
		tcxMapper.mapSportSession(sportSession, System.out);
	}

}
