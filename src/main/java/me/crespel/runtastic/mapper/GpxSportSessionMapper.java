package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.MetadataType;
import com.topografix.gpx._1._1.ObjectFactory;
import com.topografix.gpx._1._1.TrkType;
import com.topografix.gpx._1._1.TrksegType;
import com.topografix.gpx._1._1.WptType;

import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;

/**
 * GPX sport session mapper.
 * @author Fabien CRESPEL (fabien@crespel.net)
 * @author Christian IMFELD (imfeldc@gmail.com)
 */
public class GpxSportSessionMapper implements SportSessionMapper<GpxType> {

	protected final DatatypeFactory dtf;
	protected final ObjectFactory factory = new ObjectFactory();

	public GpxSportSessionMapper() {
		try {
			dtf = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean supports(String format) {
		return format != null && format.toLowerCase().endsWith("gpx");
	}

	@Override
	public GpxType mapSportSession(SportSession session, String format) {

		GpxType gpx = factory.createGpxType();
		gpx.setVersion("1.1");
		gpx.setCreator("RuntasticExportConverter");

		MetadataType meta = factory.createMetadataType();
		meta.setTime(mapDate(session.getStartTime()));
		gpx.setMetadata(meta);

		if( session.getImages() != null ) {
			// Add the photos as "way points"
			for( ImagesMetaData image : session.getImages()) {
				WptType wpt = factory.createWptType();
				wpt.setLat(image.getLatitude());
				wpt.setLon(image.getLongitude());
				wpt.setName("Photo: " + image.getId() + ".jpg");
				wpt.setDesc(image.getDescription());
				wpt.setType("photo");
				gpx.getWpt().add(wpt);
			}
		}

		TrkType trk = factory.createTrkType();
		trk.setName(session.getNotes());
		trk.setType(mapSport(session.getSportTypeId()));
		if (session.getGpsData() != null) {
			// handling JSON GPS data
			TrksegType trkseg = factory.createTrksegType();
			for (GpsData gps : session.getGpsData()) {
				WptType wpt = factory.createWptType();
				wpt.setLat(gps.getLatitude());
				wpt.setLon(gps.getLongitude());
				wpt.setEle(gps.getAltitude());
				wpt.setTime(mapDate(gps.getTimestamp()));
				trkseg.getTrkpt().add(wpt);
			}
			trk.getTrkseg().add(trkseg);
		}
		gpx.getTrk().add(trk);

		if (session.getGpx() != null ) {
			// handling GPX GPS data
			//gpx.getTrk().addAll(session.getGpx().getTrk());
			gpx.getTrk().get(0).getTrkseg().addAll(session.getGpx().getTrk().get(0).getTrkseg());
		}

		return gpx;
	}

	@Override
	public GpxType mapSportSession(SportSession session, String format, File dest) {
		GpxType gpx = mapSportSession(session, format);
		try {
			JAXBContext ctx = JAXBContext.newInstance(GpxType.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(factory.createGpx(gpx), dest);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return gpx;
	}

	@Override
	public GpxType mapSportSession(SportSession session, String format, OutputStream dest) {
		GpxType gpx = mapSportSession(session, format);
		try {
			JAXBContext ctx = JAXBContext.newInstance(GpxType.class);
			Marshaller m = ctx.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(factory.createGpx(gpx), dest);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		return gpx;
	}

	protected XMLGregorianCalendar mapDate(Date date) {
		if (date != null) {
			GregorianCalendar cal = new GregorianCalendar();
			cal.setTime(date);
			return dtf.newXMLGregorianCalendar(cal);
		}
		return null;
	}

	protected String mapSport(String sportTypeId) {
		switch (sportTypeId) {
		case "1":
			return "9"; // Running
		case "3":
			return "1"; // Biking
		case "7":
			return "Hiking"; // Hiking
		default:
			return ""; // Other
		}
	}

}
