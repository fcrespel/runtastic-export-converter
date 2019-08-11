package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.garmin.xmlschemas.trainingcenterdatabase.v2.ActivityLapT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.ActivityListT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.ActivityT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.HeartRateInBeatsPerMinuteT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.ObjectFactory;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.PositionT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.SportT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrackT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrackpointT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TrainingCenterDatabaseT;
import com.garmin.xmlschemas.trainingcenterdatabase.v2.TriggerMethodT;

import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.HeartRateData;
import me.crespel.runtastic.model.SportSession;

/**
 * TCX sport session mapper.
 * @author Fabien CRESPEL (fabien@crespel.net)
 */
public class TcxSportSessionMapper implements SportSessionMapper<TrainingCenterDatabaseT> {

	protected final DatatypeFactory dtf;
	protected final ObjectFactory factory = new ObjectFactory();

	public TcxSportSessionMapper() {
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public TrainingCenterDatabaseT mapSportSession(SportSession session) {
		List<TrackpointT> trackpoints = new ArrayList<>();
		trackpoints.addAll(mapGpsData(session.getGpsData()));
		trackpoints.addAll(mapHeartRateData(session.getHeartRateData()));
		TrackT track = new TrackT();
		track.getTrackpoint().addAll(mergeTrackpoints(trackpoints));

		ActivityLapT lap = new ActivityLapT();
		lap.setStartTime(mapDate(session.getStartTime()));
		lap.setTotalTimeSeconds(session.getDuration());
		lap.setDistanceMeters(session.getDistance());
		lap.setCalories(session.getCalories());
		lap.setAverageHeartRateBpm(mapHeartRate(session.getPulseAvg()));
		lap.setMaximumHeartRateBpm(mapHeartRate(session.getPulseMax()));
		lap.setMaximumSpeed(session.getMaxSpeed().doubleValue());
		lap.setTriggerMethod(TriggerMethodT.MANUAL);
		lap.getTrack().add(track);

		ActivityT activity = new ActivityT();
		activity.setSport(mapSport(session.getSportTypeId()));
		activity.setId(mapDate(session.getStartTime()));
		activity.getLap().add(lap);

		ActivityListT activities = new ActivityListT();
		activities.getActivity().add(activity);

		TrainingCenterDatabaseT tcx = new TrainingCenterDatabaseT();
		tcx.setActivities(activities);
		return tcx;
	}

	@Override
	public TrainingCenterDatabaseT mapSportSession(SportSession session, File dest) {
		TrainingCenterDatabaseT tcx = mapSportSession(session);
		try {
			JAXBContext ctx = JAXBContext.newInstance(TrainingCenterDatabaseT.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(factory.createTrainingCenterDatabase(tcx), dest);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return tcx;
	}

	@Override
	public TrainingCenterDatabaseT mapSportSession(SportSession session, OutputStream dest) {
		TrainingCenterDatabaseT tcx = mapSportSession(session);
		try {
			JAXBContext ctx = JAXBContext.newInstance(TrainingCenterDatabaseT.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(factory.createTrainingCenterDatabase(tcx), dest);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return tcx;
	}

	protected XMLGregorianCalendar mapDate(Date date) {
		if (date != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return dtf.newXMLGregorianCalendar(cal);
		}
		return null;
	}

	protected HeartRateInBeatsPerMinuteT mapHeartRate(Integer value) {
		if (value != null) {
			HeartRateInBeatsPerMinuteT hr = new HeartRateInBeatsPerMinuteT();
			hr.setValue(value.shortValue());
			return hr;
		}
		return null;
	}

	protected SportT mapSport(String sportTypeId) {
		switch (sportTypeId) {
		case "1":
			return SportT.RUNNING;
		case "3":
			return SportT.BIKING;
		default:
			return SportT.OTHER;	
		}
	}

	protected List<TrackpointT> mapGpsData(List<GpsData> gpsData) {
		List<TrackpointT> trackpoints = new ArrayList<>();
		if (gpsData != null) {
			for (GpsData gps : gpsData) {
				PositionT pos = new PositionT();
				pos.setLatitudeDegrees(gps.getLatitude().doubleValue());
				pos.setLongitudeDegrees(gps.getLongitude().doubleValue());
				TrackpointT trackpoint = new TrackpointT();
				trackpoint.setTime(mapDate(gps.getTimestamp()));
				trackpoint.setDistanceMeters(gps.getDistance().doubleValue());
				trackpoint.setAltitudeMeters(gps.getAltitude().doubleValue());
				trackpoint.setPosition(pos);
				trackpoints.add(trackpoint);
			}
		}
		return trackpoints;
	}

	protected List<TrackpointT> mapHeartRateData(List<HeartRateData> heartRateData) {
		List<TrackpointT> trackpoints = new ArrayList<>();
		if (heartRateData != null) {
			for (HeartRateData hr : heartRateData) {
				TrackpointT trackpoint = new TrackpointT();
				trackpoint.setTime(mapDate(hr.getTimestamp()));
				trackpoint.setDistanceMeters(hr.getDistance().doubleValue());
				trackpoint.setHeartRateBpm(mapHeartRate(hr.getHeartRate()));
				trackpoints.add(trackpoint);
			}
		}
		return trackpoints;
	}

	protected List<TrackpointT> mergeTrackpoints(List<TrackpointT> trackpoints) {
		List<TrackpointT> merged = new ArrayList<>();
		Collections.sort(trackpoints, (a, b) -> a.getTime().compare(b.getTime()));
		TrackpointT previous = null;
		for (TrackpointT current : trackpoints) {
			if (previous != null && current.getTime().equals(previous.getTime())) {
				if (previous.getDistanceMeters() == null) {
					previous.setDistanceMeters(current.getDistanceMeters());
				}
				if (previous.getAltitudeMeters() == null) {
					previous.setAltitudeMeters(current.getAltitudeMeters());
				}
				if (previous.getPosition() == null) {
					previous.setPosition(current.getPosition());
				}
				if (previous.getHeartRateBpm() == null) {
					previous.setHeartRateBpm(current.getHeartRateBpm());
				}
			} else {
				merged.add(current);
			}
			previous = current;
		}
		return merged;
	}

}
