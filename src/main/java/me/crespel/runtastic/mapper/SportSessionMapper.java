package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;

import me.crespel.runtastic.model.SportSession;

/**
 * Sport session mapper.
 * This interface maps a sport session to a specific type.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @param <T> target mapping type
 */
public interface SportSessionMapper<T> {

	T mapSportSession(SportSession session);

	T mapSportSession(SportSession session, File dest);

	T mapSportSession(SportSession session, OutputStream dest);

}
