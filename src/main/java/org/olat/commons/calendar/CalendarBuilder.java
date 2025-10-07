/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.commons.calendar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import net.fortuna.ical4j.data.CalendarParser;
import net.fortuna.ical4j.data.CalendarParserFactory;
import net.fortuna.ical4j.data.ContentHandler;
import net.fortuna.ical4j.data.DefaultContentHandler;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.data.UnfoldingReader;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.TimeZoneRegistry;

/**
 * This a reduced copy of the CalendarBuilder of ical4j
 * 
 * 
 * Initial date: 7 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CalendarBuilder {

    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private final CalendarParser parser;

    private final ContentHandler contentHandler;

    private final TimeZoneRegistry tzRegistry;

    /**
     * The calendar instance created by the builder.
     */
    private Calendar calendar;

    /**
     * Constructs a new calendar builder using the specified timezone registry.
     *
     * @param tzRegistry a timezone registry to populate with discovered timezones
     */
    public CalendarBuilder(final TimeZoneRegistry tzRegistry) {
        this.parser = CalendarParserFactory.getInstance().get();
        this.tzRegistry = tzRegistry;
        this.contentHandler = new CalendarContentHandler(calendar -> {this.calendar = calendar;}, tzRegistry);
    }

    /**
     * Builds an iCalendar model from the specified input stream.
     *
     * @param in an input stream to read calendar data from
     * @return a calendar parsed from the specified input stream
     * @throws IOException     where an error occurs reading data from the specified stream
     * @throws ParserException where an error occurs parsing data from the stream
     */
    public Calendar build(final InputStream in) throws IOException, ParserException {
        return build(new InputStreamReader(in, DEFAULT_CHARSET));
    }

    /**
     * Builds an iCalendar model from the specified reader. An <code>UnfoldingReader</code> is applied to the
     * specified reader to ensure the data stream is correctly unfolded where appropriate.
     *
     * @param in a reader to read calendar data from
     * @return a calendar parsed from the specified reader
     * @throws IOException     where an error occurs reading data from the specified reader
     * @throws ParserException where an error occurs parsing data from the reader
     */
    public Calendar build(final Reader in) throws IOException, ParserException {
        return build(new UnfoldingReader(in));
    }

    /**
     * Build an iCalendar model by parsing data from the specified reader.
     *
     * @param uin an unfolding reader to read data from
     * @return a calendar parsed from the specified reader
     * @throws IOException     where an error occurs reading data from the specified reader
     * @throws ParserException where an error occurs parsing data from the reader
     */
    public Calendar build(final UnfoldingReader uin) throws IOException, ParserException {
        parser.parse(uin, contentHandler);
        return calendar;
    }

    /**
     * Returns the timezone registry used in the construction of calendars.
     *
     * @return a timezone registry
     */
    public final TimeZoneRegistry getRegistry() {
        return tzRegistry;
    }
    
    private static class CalendarContentHandler extends DefaultContentHandler {
    	
    	private final Consumer<Calendar> consumer;
        
    	public CalendarContentHandler(Consumer<Calendar> consumer, TimeZoneRegistry tzRegistry) {
            super(consumer, tzRegistry);
            this.consumer = consumer;
        }

		@Override
		public void endCalendar() {
	        consumer.accept(new Calendar(new PropertyList(calendarProperties),
	                new ComponentList<>(calendarComponents)));
		}
    }
}
