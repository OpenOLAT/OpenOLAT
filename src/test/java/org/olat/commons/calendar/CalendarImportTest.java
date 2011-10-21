package org.olat.commons.calendar;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.TimeZoneRegistry;
import net.fortuna.ical4j.model.TimeZoneRegistryFactory;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.util.Dates;
import net.fortuna.ical4j.util.TimeZones;

import org.junit.Test;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  20 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CalendarImportTest {
	
	@Test
	public void testImportMonthFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_30.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}
	
	@Test
	public void testImportWeekFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_7.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}
	
	@Test
	public void testImportAllFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_Alles.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}
	
	@Test
	public void testImportOktoberFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("BB_Okt.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}
	
	@Test
	public void testImportFromOutlook() throws IOException, ParserException {
		InputStream in = CalendarImportTest.class.getResourceAsStream("Hoffstedde.ics");
		CalendarBuilder builder = new CalendarBuilder();
		Calendar calendar = builder.build(in);
        assertNotNull(calendar);
	}

}
