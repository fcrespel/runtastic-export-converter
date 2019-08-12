package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;

import me.crespel.runtastic.model.SportSession;

/**
 * Delegating sport session mapper.
 * This class delegates the actual mapping to a supported mapper.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class DelegatingSportSessionMapper implements SportSessionMapper<Object> {

	private final SportSessionMapper<?>[] mappers = { new GpxSportSessionMapper(), new TcxSportSessionMapper() };

	@Override
	public boolean supports(String format) {
		for (SportSessionMapper<?> mapper : mappers) {
			if (mapper.supports(format)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object mapSportSession(SportSession session, String format) {
		for (SportSessionMapper<?> mapper : mappers) {
			if (mapper.supports(format)) {
				return mapper.mapSportSession(session, format);
			}
		}
		throw new UnsupportedOperationException("Unsupported export format '" + format + "'");
	}

	@Override
	public Object mapSportSession(SportSession session, String format, File dest) {
		for (SportSessionMapper<?> mapper : mappers) {
			if (mapper.supports(format) || mapper.supports(dest.getName())) {
				return mapper.mapSportSession(session, format, dest);
			}
		}
		throw new UnsupportedOperationException("Unsupported export format '" + format + "'");
	}

	@Override
	public Object mapSportSession(SportSession session, String format, OutputStream dest) {
		for (SportSessionMapper<?> mapper : mappers) {
			if (mapper.supports(format)) {
				return mapper.mapSportSession(session, format, dest);
			}
		}
		throw new UnsupportedOperationException("Unsupported export format '" + format + "'");
	}

}
