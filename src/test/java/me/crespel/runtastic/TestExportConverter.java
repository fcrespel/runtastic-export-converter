package me.crespel.runtastic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.topografix.gpx._1._1.BoundsType;
import com.topografix.gpx._1._1.GpxType;
import com.topografix.gpx._1._1.MetadataType;

import org.junit.Test;

import me.crespel.runtastic.converter.ExportConverter;
import me.crespel.runtastic.model.SportSession;

/**
 * RuntasticExportConverter tests.
 * @author Christian IMFELD (imfeldc@gmail.com)
 */

public class TestExportConverter {
    
	private final ExportConverter converter = new ExportConverter();

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

        assertEquals("Outer bound 'Maxlat' is wrong", new BigDecimal(52.0), session.getOuterBound().getMaxlat());
        assertEquals("Outer bound 'Minlat' is wrong", new BigDecimal(18.0), session.getOuterBound().getMinlat());
        assertEquals("Outer bound 'Maxlon' is wrong", new BigDecimal(42.0), session.getOuterBound().getMaxlon());
        assertEquals("Outer bound 'Minlon' is wrong", new BigDecimal(28.0), session.getOuterBound().getMinlon());

        assertEquals("Inner bound 'Maxlat' is wrong", new BigDecimal(48.0), session.getInnerBound().getMaxlat());
        assertEquals("Inner bound 'Minlat' is wrong", new BigDecimal(22.0), session.getInnerBound().getMinlat());
        assertEquals("Inner bound 'Maxlon' is wrong", new BigDecimal(38.0), session.getInnerBound().getMaxlon());
        assertEquals("Inner bound 'Minlon' is wrong", new BigDecimal(32.0), session.getInnerBound().getMinlon());
    }


    @Test
    public void testIsCompoundInsideOnRight() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "right" side and "smaller" than initial session
        bound1.setMaxlat(new BigDecimal(48.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(22.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(1) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "bigger" than initial session
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(2) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "one corner match top"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(3) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "one corner match below"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(4) Not detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundOutsideOnRight() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "right" side and "outside above"
        bound1.setMaxlat(new BigDecimal(66.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(55.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(1) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "outside below"
        bound1.setMaxlat(new BigDecimal(11.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(5.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(2) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "one corner NOT match top"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(50.1));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(3) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "right" side and "one corner NOT match below"
        bound1.setMaxlat(new BigDecimal(19.9));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(4) Detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundInsideOnLeft() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "left" side and "smaller" than initial session
        bound1.setMaxlat(new BigDecimal(48.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(22.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(1) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "bigger" than initial session
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(2) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "one corner match top"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(3) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "one corner match below"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(4) Not detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundOutsideOnLeft() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "left" side and "outside above"
        bound1.setMaxlat(new BigDecimal(66.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(55.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(1) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "outside below"
        bound1.setMaxlat(new BigDecimal(11.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(5.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(2) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "one corner NOT match top"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(20.0));
        bound1.setMinlat(new BigDecimal(50.1));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(3) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "left" side and "one corner NOT match below"
        bound1.setMaxlat(new BigDecimal(19.9));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(4) Detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundInsideOnTop() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "top" side and "smaller" than initial session
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(38.0));
        bound1.setMinlon(new BigDecimal(32.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(1) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "bigger" than initial session
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(2) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "one corner match on left"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(3) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "one corner match on right"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(4) Not detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundOutsideOnTop() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "top" side and "outside on right"
        bound1.setMaxlat(new BigDecimal(66.0));
        bound1.setMaxlon(new BigDecimal(55.0));
        bound1.setMinlon(new BigDecimal(44.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(1) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "outside on left"
        bound1.setMaxlat(new BigDecimal(60.0));
        bound1.setMaxlon(new BigDecimal(22.0));
        bound1.setMinlon(new BigDecimal(11.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(2) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "one corner NOT match on right"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.1));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(3) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "top" side and "one corner NOT match on left"
        bound1.setMaxlat(new BigDecimal(55.0));
        bound1.setMaxlon(new BigDecimal(29.9));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(50.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(4) Detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundInsideOnBottom() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "bottom" side and "smaller" than initial session
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(38.0));
        bound1.setMinlon(new BigDecimal(32.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(1) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "bigger" than initial session
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(2) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "one corner match on left"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(30.0));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(3) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "one corner match on right"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(4) Not detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundOutsideOnBottom() throws Exception {
    
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

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, at "bottom" side and "outside on right"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(55.0));
        bound1.setMinlon(new BigDecimal(44.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(1) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "outside on left"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(22.0));
        bound1.setMinlon(new BigDecimal(11.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(2) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "one corner NOT match on right"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(44.0));
        bound1.setMinlon(new BigDecimal(40.1));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(3) Detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, at "bottom" side and "one corner NOT match on left"
        bound1.setMaxlat(new BigDecimal(20.0));
        bound1.setMaxlon(new BigDecimal(29.9));
        bound1.setMinlon(new BigDecimal(22.0));
        bound1.setMinlat(new BigDecimal(11.0));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertFalse("(4) Detected as compound sessions!", converter.isCompound(session, session1));
    }

    @Test
    public void testIsCompoundWithRealData() throws Exception {
        // 2017-05-09 17:48:11[141] - ID: 5911f98508cd1247485d41cd, Sport Type: 1, Notes: 'Zuger Trophy - Etappe Baar, lang', 
        // --> Bounds[MinLat=47.154605865478516, MaxLat=47.19623947143555, MinLon=8.540229797363281, MaxLon=8.561245918273926]
        // ID: 55c2424b7d8d351e9909fb2f, Sport Type: 3, Notes: 'null', Bounds[MinLat=47.19075012207031, MaxLat=47.20124053955078, MinLon=8.490988731384277, MaxLon=8.54030704498291]
        // ID: 7bb2b147-35b9-4cc7-a707-5750bc5096cc, Sport Type: 1, Notes: 'null', Bounds[MinLat=47.196255, MaxLat=47.26670455932617, MinLon=8.485222, MaxLon=8.577558517456055]
        // ID: 55de05107f280eea8e006ccb, Sport Type: 3, Notes: 'Zuger Trophy, Velo Zugerberh', Bounds[MinLat=47.141380310058594, MaxLat=47.154335021972656, MinLon=8.53863525390625, MaxLon=8.561858177185059]


        // Session
        SportSession session = new SportSession();
        GpxType gpx = new GpxType();
        MetadataType meta = new MetadataType();
        BoundsType bound = new BoundsType();
        bound.setMaxlat(new BigDecimal(47.19623947143555));
        bound.setMaxlon(new BigDecimal(8.561245918273926));
        bound.setMinlon(new BigDecimal(8.540229797363281));
        bound.setMinlat(new BigDecimal(47.154605865478516));
        meta.setBounds(bound);
        gpx.setMetadata(meta);
        session.setGpx(gpx);

        SportSession session1 = new SportSession();
        GpxType gpx1 = new GpxType();
        MetadataType meta1 = new MetadataType();
        BoundsType bound1 = new BoundsType();

        // Compound session, Bounds[MinLat=47.19075012207031, MaxLat=47.20124053955078, MinLon=8.490988731384277, MaxLon=8.54030704498291]
        bound1.setMaxlat(new BigDecimal(47.20124053955078));
        bound1.setMaxlon(new BigDecimal(8.54030704498291));
        bound1.setMinlon(new BigDecimal(8.490988731384277));
        bound1.setMinlat(new BigDecimal(47.19075012207031));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(1) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, Bounds[MinLat=47.196255, MaxLat=47.26670455932617, MinLon=8.485222, MaxLon=8.577558517456055]
        bound1.setMaxlat(new BigDecimal(47.26670455932617));
        bound1.setMaxlon(new BigDecimal(8.577558517456055));
        bound1.setMinlon(new BigDecimal(8.485222));
        bound1.setMinlat(new BigDecimal(47.196255));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(2) Not detected as compound sessions!", converter.isCompound(session, session1));

        // Compound session, Bounds[MinLat=47.141380310058594, MaxLat=47.154335021972656, MinLon=8.53863525390625, MaxLon=8.561858177185059]
        bound1.setMaxlat(new BigDecimal(47.154335021972656));
        bound1.setMaxlon(new BigDecimal(8.561858177185059));
        bound1.setMinlon(new BigDecimal(8.53863525390625));
        bound1.setMinlat(new BigDecimal(47.141380310058594));
        meta1.setBounds(bound1);
        gpx1.setMetadata(meta1);
        session1.setGpx(gpx1);
        assertTrue("(3) Not detected as compound sessions!", converter.isCompound(session, session1));
    }



}
