package me.crespel.runtastic;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.topografix.gpx._1._1.BoundsType;
import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.MetadataType;

import org.junit.Test;

import me.crespel.runtastic.model.SportSession;

/**
 * RuntasticExportConverter tests.
 * @author Christian IMFELD (imfeldc@gmail.com)
 */

public class TestRuntasticExportConverter {
    
	private final RuntasticExportConverter converter = new RuntasticExportConverter();

    @Test
    public void testCalculateInnerAndOuterBound() throws Exception {
    
        // Session
        SportSession session = new SportSession();
        GpxType gpx = new GpxType();
        MetadataType meta = new MetadataType();
        BoundsType bound = new BoundsType();
        bound.setMaxlat(new BigDecimal(50.0));
        bound.setMaxlon(new BigDecimal(40.0));
        bound.setMinlon(new BigDecimal(30.0));
        bound.setMinlat(new BigDecimal(20.0));
        meta.setBounds(bound);
        gpx.setMetadata(meta);
        session.setGpx(gpx);

        // Overlap session 1
        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();
        bound1.setMaxlat(new BigDecimal(52.0));
        bound1.setMaxlon(new BigDecimal(42.0));
        bound1.setMinlon(new BigDecimal(32.0));
        bound1.setMinlat(new BigDecimal(22.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);

        // Overlap session 2
        SportSession session2 = new SportSession();
        GpxType gpx2 = new GpxType();
        MetadataType meta2 = new MetadataType();
        BoundsType bound2 = new BoundsType();
        bound2.setMaxlat(new BigDecimal(48.0));
        bound2.setMaxlon(new BigDecimal(38.0));
        bound2.setMinlon(new BigDecimal(28.0));
        bound2.setMinlat(new BigDecimal(18.0));
        meta2.setBounds(bound2);
        gpx2.setMetadata(meta2);
        session2.setGpx(gpx2);

        List<SportSession> overlapSessions = new ArrayList<>();
        overlapSessions.add(session1);
        overlapSessions.add(session2);
        session.setOverlapSessions(overlapSessions);
        session1.setOverlapSessions(overlapSessions);
        session2.setOverlapSessions(overlapSessions);

        converter.calculateInnerAndOuterBound(session);

        List<SportSession> sessions = new ArrayList<>();
        sessions.add(session);
        converter.displayOverlapSummary(sessions, false);

        assertEquals("Outer bound 'Maxlat' is wrong", new BigDecimal(52.0), session.getOuterBound().getMaxlat());
        assertEquals("Outer bound 'Minlat' is wrong", new BigDecimal(18.0), session.getOuterBound().getMinlat());
        assertEquals("Outer bound 'Maxlon' is wrong", new BigDecimal(42.0), session.getOuterBound().getMaxlon());
        assertEquals("Outer bound 'Minlon' is wrong", new BigDecimal(28.0), session.getOuterBound().getMinlon());

        assertEquals("Inner bound 'Maxlat' is wrong", new BigDecimal(48.0), session.getInnerBound().getMaxlat());
        assertEquals("Inner bound 'Minlat' is wrong", new BigDecimal(22.0), session.getInnerBound().getMinlat());
        assertEquals("Inner bound 'Maxlon' is wrong", new BigDecimal(38.0), session.getInnerBound().getMaxlon());
        assertEquals("Inner bound 'Minlon' is wrong", new BigDecimal(32.0), session.getInnerBound().getMinlon());
    }
}