package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;

import me.crespel.runtastic.model.SportSession;

/**
 * Sport session mapper.
 * This interface maps a sport session to a specific format.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @param <T> target mapping type
 */
public interface SportSessionMapper<T> {

	boolean supports(String format);

	T mapSportSession(SportSession session, String format);

	T mapSportSession(SportSession session, String format, File dest);

	T mapSportSession(SportSession session, String format, OutputStream dest);

}
