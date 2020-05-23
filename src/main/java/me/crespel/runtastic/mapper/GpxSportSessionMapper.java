package me.crespel.runtastic.mapper;

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.topografix.gpx._1._1.BoundsType;
import com.topografix.gpx._1._1.EmailType;
import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.MetadataType;
import com.topografix.gpx._1._1.ObjectFactory;
import com.topografix.gpx._1._1.PersonType;
import com.topografix.gpx._1._1.RteType;
import com.topografix.gpx._1._1.TrkType;
import com.topografix.gpx._1._1.TrksegType;
import com.topografix.gpx._1._1.WptType;

import me.crespel.runtastic.model.GpsData;
import me.crespel.runtastic.model.ImagesMetaData;
import me.crespel.runtastic.model.SportSession;

/**
 * GPX sport session mapper.
 * 
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

		if (session.getImages() != null) {
			// Add the photos as "way points"
			for (ImagesMetaData image : session.getImages()) {
				WptType wpt = factory.createWptType();
				wpt.setLat(image.getLatitude());
				wpt.setLon(image.getLongitude());
				wpt.setName("Photo: " + image.getId() + ".jpg");
				wpt.setDesc(image.getDescription());
				wpt.setType("photo");
				wpt.setTime(mapDate(image.getCreatedAt()));
				gpx.getWpt().add(wpt);
			}
		}

		if (session.getGpsData() != null) {
			TrkType trk = factory.createTrkType();
			trk.setName(session.getNotes());
			trk.setType(mapSport(session.getSportTypeId()));
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
			gpx.getTrk().add(trk);
		}

		if (session.getGpx() != null) {
			TrkType trk = factory.createTrkType();
			trk.setName(session.getNotes());
			trk.setType(mapSport(session.getSportTypeId()));
			// handling GPX GPS data
			trk.getTrkseg().addAll(session.getGpx().getTrk().get(0).getTrkseg());
			gpx.getTrk().add(trk);
		}

		gpx.setMetadata(mapMetadata(session));

		// Calculate bounds and set them in meta data
		gpx.getMetadata().setBounds(calculateBounds(gpx));

		// Add bounds as waypoints
		gpx.getWpt().addAll(getBoundsAsWpt(gpx.getMetadata().getBounds(),"Session bounds"));

		// Add bounds as "rte" and "rtept"
		gpx.getRte().add(getBoundsAsRte(gpx.getMetadata().getBounds(),"Session bounds"));

		if( session.getInnerBound() != null ) {
			// Add "inner bounds" as "rte" and "rtept"
			gpx.getRte().add(getBoundsAsRte(session.getInnerBound(),"Inner bounds of all overlapping sessions"));
		}
		if( session.getOuterBound() != null ) {
			// Add "outer bounds" as "rte" and "rtept"
			gpx.getRte().add(getBoundsAsRte(session.getOuterBound(),"Outer bounds of all overlapping sessions"));
		}

		return gpx;
	}

	private MetadataType mapMetadata(SportSession session) {
		MetadataType meta = factory.createMetadataType();
		meta.setTime(mapDate(session.getCreatedAt()));
		meta.setDesc(session.getNotes());
		if (session.getUser() != null) {
			PersonType author = factory.createPersonType();
			EmailType email = factory.createEmailType();
			author.setName(session.getUser().getFirstName() + " " + session.getUser().getLastName());
			email.setId(session.getUser().getEmail());
			author.setEmail(email);
			meta.setAuthor(author);
		}
		meta.setKeywords("runtastic"); // add comma separated keywords
		return meta;
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
		case "4":
			return "4"; // Hiking
		case "7":
			return "Hiking"; // Hiking
		case "19":
			return "19"; // ??
		case "22":
			return "22"; // ??
		default:
			return ""; // Other
		}
	}

	protected BoundsType calculateBounds( GpxType gpx ) {
		BoundsType bounds = factory.createBoundsType();

		// search through waypoints (=photos)
		for( WptType wpt : gpx.getWpt() ) {
			if( bounds.getMaxlat() == null || (bounds.getMaxlat().compareTo(wpt.getLat()) == -1)) {
					bounds.setMaxlat(wpt.getLat());
			}
			if( bounds.getMinlat() == null || (bounds.getMinlat().compareTo(wpt.getLat()) == 1)) {
				bounds.setMinlat(wpt.getLat());
			}
			if( bounds.getMaxlon() == null || (bounds.getMaxlon().compareTo(wpt.getLon()) == -1)) {
				bounds.setMaxlon(wpt.getLon());
			}
			if( bounds.getMinlon() == null || (bounds.getMinlon().compareTo(wpt.getLon()) == 1)) {
				bounds.setMinlon(wpt.getLon());
			}
		}

		// search through sport session tracks
		for( TrkType trk : gpx.getTrk()) {
			for( TrksegType trkseg : trk.getTrkseg() ) {
				for( WptType wpt : trkseg.getTrkpt() ) {
					if( bounds.getMaxlat() == null || (bounds.getMaxlat().compareTo(wpt.getLat()) == -1 )) {
						bounds.setMaxlat(wpt.getLat());
					}
					if( bounds.getMinlat() == null || (bounds.getMinlat().compareTo(wpt.getLat()) == 1)) {
						bounds.setMinlat(wpt.getLat());
					}
					if( bounds.getMaxlon() == null || (bounds.getMaxlon().compareTo(wpt.getLon()) == -1)) {
						bounds.setMaxlon(wpt.getLon());
					}
					if( bounds.getMinlon() == null || (bounds.getMinlon().compareTo(wpt.getLon()) == 1)) {
						bounds.setMinlon(wpt.getLon());
					}
				}
			}
		}

		return bounds;
	}

	private Collection<? extends WptType> getBoundsAsWpt(BoundsType bounds, String boundName) {
		List<WptType> wptlist = new ArrayList<>();

		// Add bounds as waypoints
		WptType wpt1 = factory.createWptType();
		wpt1.setLat(bounds.getMaxlat());
		wpt1.setLon(bounds.getMaxlon());
		wpt1.setName(boundName + ": top-right corner");
		wpt1.setType("bounds");
		wptlist.add(wpt1);
		WptType wpt2 = factory.createWptType();
		wpt2.setLat(bounds.getMinlat());
		wpt2.setLon(bounds.getMaxlon());
		wpt2.setName(boundName + ": down-right corner");
		wpt2.setType("bounds");
		wptlist.add(wpt2);
		WptType wpt3 = factory.createWptType();
		wpt3.setLat(bounds.getMaxlat());
		wpt3.setLon(bounds.getMinlon());
		wpt3.setName(boundName + ": top-left corner");
		wpt3.setType("bounds");
		wptlist.add(wpt3);
		WptType wpt4 = factory.createWptType();
		wpt4.setLat(bounds.getMinlat());
		wpt4.setLon(bounds.getMinlon());
		wpt4.setName(boundName + ": down-left corner");
		wpt4.setType("bounds");
		wptlist.add(wpt4);

		return wptlist;
	}

	private RteType getBoundsAsRte(BoundsType bounds, String boundName) {
		RteType rte = factory.createRteType();
		rte.setName(boundName);
		rte.setDesc(boundName + ": Bounds of this sport session.");

		// Add bounds as waypoints
		WptType wpt1 = factory.createWptType();
		wpt1.setLat(bounds.getMaxlat());
		wpt1.setLon(bounds.getMaxlon());
		wpt1.setName(boundName + ": top-right corner");
		wpt1.setType("bounds");
		rte.getRtept().add(wpt1);
		WptType wpt2 = factory.createWptType();
		wpt2.setLat(bounds.getMinlat());
		wpt2.setLon(bounds.getMaxlon());
		wpt2.setName(boundName + ": down-right corner");
		wpt2.setType("bounds");
		rte.getRtept().add(wpt2);
		WptType wpt4 = factory.createWptType();
		wpt4.setLat(bounds.getMinlat());
		wpt4.setLon(bounds.getMinlon());
		wpt4.setName(boundName + ": down-left corner");
		wpt4.setType("bounds");
		rte.getRtept().add(wpt4);
		WptType wpt3 = factory.createWptType();
		wpt3.setLat(bounds.getMaxlat());
		wpt3.setLon(bounds.getMinlon());
		wpt3.setName(boundName + ": top-left corner");
		wpt3.setType("bounds");
		rte.getRtept().add(wpt3);
		rte.getRtept().add(wpt1);

		return rte;
	}

}
