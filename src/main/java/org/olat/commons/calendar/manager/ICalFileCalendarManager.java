 /**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.commons.calendar.manager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.commons.calendar.model.CalendarKey;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventKey;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIModifiedEvent;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.user.UserManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.TimeZone;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Contact;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtStart;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

@Service
public class ICalFileCalendarManager implements CalendarManager, InitializingBean {

	private static final Logger log = Tracing.createLoggerFor(ICalFileCalendarManager.class);

	private File fStorageBase;
	// o_clusterOK by:cg 
	private CacheWrapper<String, Kalendar> calendarCache;

	private static final Clazz ICAL_CLASS_PRIVATE = new Clazz("PRIVATE");
	private static final Clazz ICAL_CLASS_PUBLIC = new Clazz("PUBLIC");
	private static final Clazz ICAL_CLASS_X_FREEBUSY = new Clazz("X-FREEBUSY");
	
	/** rule for recurring events */
	private static final String ICAL_RRULE = "RRULE";
	/** property to exclude events from recurrence */
	private static final String ICAL_EXDATE = "EXDATE";
	
	private TimeZone tz;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarUserConfigurationDAO calendarUserConfigDao;

	@Override
	public void afterPropertiesSet() {
		fStorageBase = new File(WebappHelper.getUserDataRoot(), "calendars");
		if (!fStorageBase.exists()) {
			if (!fStorageBase.mkdirs())
				throw new OLATRuntimeException("Error creating calendar base directory at: " + fStorageBase.getAbsolutePath(), null);
		}
		
		//create the directories
		String[] dirs = new String[]{ TYPE_USER, TYPE_GROUP, TYPE_COURSE };
		for(String dir:dirs) {
			File fDirectory = new File(fStorageBase, dir);
			if(!fDirectory.exists()) {
				fDirectory.mkdirs();
			}
		}
		// set parser to relax (needed for allday events
		// see http://sourceforge.net/forum/forum.php?thread_id=1253735&forum_id=368291
		// made in module System.setProperty("ical4j.unfolding.relaxed", "true");
		// initialize timezone
		tz = calendarModule.getDefaultTimeZone();
		calendarCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(CalendarManager.class.getSimpleName(), "calendar");
	}
	
	@Override
	public URLConnection getURLConnection(String url) {
		try {
			URLConnection conn = new URL(url).openConnection();
			conn.setConnectTimeout(15000);
			return conn;
		} catch (IOException e) {
			log.error("Cannot open URL connection for: {}", url, e);
			return null;
		}
	}
	
	/**
	 * Check if a calendar already exists for the given id.
	 * @param calendarID
	 * @param type
	 * @return
	 */
	@Override
	public boolean calendarExists(String calendarType, String calendarID) {
		return getCalendarFile(calendarType, calendarID).exists();
	}
	
	/**
	 * 
	 * @see org.olat.calendar.CalendarManager#createClaendar(java.lang.String)
	 */
	@Override
	public Kalendar createCalendar(String type, String calendarID) {
		return new Kalendar(calendarID, type);
	}

	@Override
	public Kalendar getCalendar(final String type, final String calendarID) {
		String key = getKeyFor(type, calendarID);
		Kalendar cal = calendarCache.get(key);
		if(cal == null) {
			cal = getCalendarFromCache(type, calendarID);
		}
		return cal;
	}

	private Kalendar getCalendarFromCache(final String callType, final String callCalendarID) {
		String calKey = getKeyFor(callType,callCalendarID);	
		Kalendar cal = calendarCache.get(calKey);
		if (cal == null) {
			cal = loadOrCreateCalendar(callType, callCalendarID);
			Kalendar cacheCal = calendarCache.putIfAbsent(calKey, cal);
			if(cacheCal != null) {
				cal = cacheCal;
			}
		}
		return cal;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public OLATResourceable getOresHelperFor(Kalendar cal) {
		return OresHelper.createOLATResourceableType(getKeyFor(cal.getType(), cal.getCalendarID()));
	}
	
	private String getKeyFor(String type, String calendarID) {
	    return type + "_" + calendarID;
    }
	
	/**
	 * Internal load calendar file from filesystem.
	 */
	// o_clusterOK by:cg This must not be synchronized because the caller already synchronized 
	private Kalendar loadCalendarFromFile(String type, String calendarID) {
		Calendar calendar = readCalendar(type, calendarID);
		return createKalendar(type, calendarID, calendar);
	}

	protected Kalendar createKalendar(String type, String calendarID, Calendar calendar) {
		Kalendar cal = new Kalendar(calendarID, type);
		for (Iterator<?> iter = calendar.getComponents().iterator(); iter.hasNext();) {
			Object comp = iter.next();
			if (comp instanceof VEvent) {
				VEvent vevent = (VEvent)comp;
				KalendarEvent calEvent = getKalendarEvent(vevent);
				cal.addEvent(calEvent);
			} else if (comp instanceof VTimeZone) {
				log.info("createKalendar: VTimeZone Component is not supported and will not be added to calender");
				log.debug("createKalendar: VTimeZone={}", comp);
			} else {
				log.warn("createKalendar: unknown Component={}", comp);
			}
		}
		return cal;
	}

	/**
	 * Internal read calendar file from filesystem. It doesn't
	 * use the cache and return a not shared calendar.
	 */
	@Override
	public Calendar readCalendar(String type, String calendarID) {
		if(log.isDebugEnabled()) {
			log.debug("readCalendar from file, type={} calendarID={}", type, calendarID);
		}
		
		File calendarFile = getCalendarFile(type, calendarID);
		return readCalendar(calendarFile);
	}
	
	@Override
	public Calendar readCalendar(File calendarFile) {
		try(InputStream fIn = new FileInputStream(calendarFile);
				InputStream	in = new BufferedInputStream(fIn))  {
			
			CalendarBuilder builder = new CalendarBuilder();
			return builder.build(in);
		} catch (FileNotFoundException fne) {
			throw new OLATRuntimeException("Not found: " + calendarFile, fne);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file: " + calendarFile, e);
		}
	}

	@Override
	public Kalendar buildKalendarFrom(InputStream in, String calType, String calId) {
		Kalendar kalendar = null;
		
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			CalendarBuilder builder = new CalendarBuilder();
			Calendar calendar = builder.build(reader);
			kalendar = createKalendar(calType, calId, calendar);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file.", e);
		}
		return kalendar;
	}
	
	@Override
	public boolean synchronizeCalendarFrom(InputStream in, String source, Kalendar targetCalendar) {
		try(BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
			Calendar inCalendar = new CalendarBuilder().build(reader);
			Kalendar inTmpKalendar = createKalendar("TEMP", UUID.randomUUID().toString(), inCalendar);
			
			String targetId = "-" + targetCalendar.getType() + "-" + targetCalendar.getCalendarID() + "-";
			
			OLATResourceable calOres = getOresHelperFor(targetCalendar);
			Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> {
				//remove event in target calendar which doesn't exist in stream
				Collection<KalendarEvent> currentEvents = targetCalendar.getEvents();
				for(KalendarEvent currentEvent:currentEvents) {
					if(currentEvent.getExternalSource() != null && source.equals(currentEvent.getExternalSource())) {
						String eventId = currentEvent.getID();
						String recurrenceId = currentEvent.getRecurrenceID();
						if(inTmpKalendar.getEvent(eventId, recurrenceId) == null) {
							targetCalendar.removeEvent(currentEvent);
						} else if(eventId.contains(targetId)) {
							targetCalendar.removeEvent(currentEvent);//don't import myself;
						}
					}
				}

				Collection<KalendarEvent> inEvents = inTmpKalendar.getEvents();
				for(KalendarEvent inEvent:inEvents) {
					if(inEvent.getID().contains(targetId)) {
						continue;
					}

					inEvent.setManagedFlags(new CalendarManagedFlag[]{ CalendarManagedFlag.all } );
					inEvent.setExternalSource(source);
					
					KalendarEvent currentEvent = targetCalendar.getEvent(inEvent.getID(), inEvent.getRecurrenceID());
					if(currentEvent == null) {
						targetCalendar.addEvent(inEvent);
					} else {
						//need perhaps more refined synchronization per event
						targetCalendar.addEvent(inEvent);
					}
				}
				
				boolean successfullyPersist = persistCalendar(targetCalendar);
				// inform all controller about calendar change for reload
				CoordinatorManager.getInstance().getCoordinator().getEventBus()
					.fireEventToListenersOf(new CalendarGUIModifiedEvent(targetCalendar), OresHelper.lookupType(CalendarManager.class));
				return Boolean.valueOf(successfullyPersist);
			});
			
			return updatedSuccessful.booleanValue();
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}

	/**
	 * Save a calendar.
	 * This method is not thread-safe. Must be called from a synchronized block.
	 * Be sure to have the newest calendar (reload calendar in synchronized block before safe it).
	 * @param calendar
	 */
	// o_clusterOK by:cg only called by Junit-test  
	@Override
	public boolean persistCalendar(Kalendar kalendar) {
		Calendar calendar = buildCalendar(kalendar);
		boolean success = writeCalendarFile(calendar, kalendar.getType(), kalendar.getCalendarID());
		calendarCache.update(getKeyFor(kalendar.getType(), kalendar.getCalendarID()), kalendar);
		return success;
	}
	
	private boolean writeCalendarFile(Calendar calendar, String calType, String calId) {
		File fKalendarFile = getCalendarFile(calType, calId);

		try(OutputStream os = new BufferedOutputStream(new FileOutputStream(fKalendarFile, false))) {
			CalendarOutputter calOut = new CalendarOutputter(false);
			calOut.output(calendar, os);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * Delete calendar by type and id.
	 */
	@Override
	public boolean deleteCalendar(String type, String calendarID) {
		calendarCache.remove( getKeyFor(type,calendarID) );
		File fKalendarFile = getCalendarFile(type, calendarID);
		return fKalendarFile.delete();
	}

	@Override
	public File getCalendarICalFile(String type, String calendarID) {
		File fCalendarICalFile = getCalendarFile(type, calendarID);
		if (fCalendarICalFile.exists()) return fCalendarICalFile;
		else return null;
	}

	@Override
	public CalendarUserConfiguration findCalendarConfigForIdentity(Kalendar calendar, IdentityRef identity) {
		return calendarUserConfigDao.getCalendarUserConfiguration(identity, calendar.getCalendarID(), calendar.getType());
	}

	@Override
	public void saveCalendarConfigForIdentity(KalendarRenderWrapper wrapper, Identity identity) {
		Kalendar calendar = wrapper.getKalendar();
		CalendarUserConfiguration configuration = calendarUserConfigDao
				.getCalendarUserConfiguration(identity, calendar.getCalendarID(), calendar.getType());
		
		if(configuration == null) {
			configuration = calendarUserConfigDao.createCalendarUserConfiguration(wrapper.getKalendar(), identity,
					wrapper.getToken(), wrapper.isInAggregatedFeed(), wrapper.isVisible());
		} else {
			configuration.setVisible(wrapper.isVisible());
			configuration.setCssClass(wrapper.getCssClass());
			configuration.setToken(wrapper.getToken());
			configuration.setInAggregatedFeed(wrapper.isInAggregatedFeed());
			configuration = calendarUserConfigDao.update(configuration);
		}
	}
	
	@Override
	public CalendarUserConfiguration createAggregatedCalendarConfig(String calendarType, Long calendarId, Identity identity) {
		String token = RandomStringUtils.randomAlphanumeric(6);
		Kalendar calendar = new Kalendar(calendarId.toString(), calendarType);
		return calendarUserConfigDao.createCalendarUserConfiguration(calendar, identity,
				token, true, true);
	}

	@Override
	public CalendarUserConfiguration createCalendarConfig(Identity identity, Kalendar calendar) {
		String token = RandomStringUtils.randomAlphanumeric(6);
		return calendarUserConfigDao.createCalendarUserConfiguration(calendar, identity,
				token, true, true);
	}

	@Override
	public CalendarUserConfiguration saveCalendarConfig(CalendarUserConfiguration configuration) {
		return calendarUserConfigDao.update(configuration);
	}

	@Override
	public CalendarUserConfiguration getCalendarUserConfiguration(Long key) {
		return calendarUserConfigDao.getCalendarUserConfiguration(key);
	}

	@Override
	public List<CalendarUserConfiguration> getCalendarUserConfigurationsList(IdentityRef identity, String type, String calendarId) {
		return calendarUserConfigDao.getCalendarUserConfigurations(identity, type, calendarId);
	}
	
	@Override
	public CalendarUserConfiguration getCalendarUserConfiguration(IdentityRef identity, Kalendar calendar) {
		return calendarUserConfigDao.getCalendarUserConfiguration(identity, calendar.getCalendarID(), calendar.getType());
	}

	@Override
	public Map<CalendarKey,CalendarUserConfiguration> getCalendarUserConfigurationsMap(IdentityRef identity, String... types) {
		List<CalendarUserConfiguration> list = calendarUserConfigDao.getCalendarUserConfigurations(identity, types);
		Map<CalendarKey,CalendarUserConfiguration> map = new HashMap<>();
		for(CalendarUserConfiguration config:list) {
			map.put(new CalendarKey(config.getCalendarId(), config.getType()), config);
		}
		return map;
	}

	@Override
	public String getCalendarToken(String calendarType, String calendarID, String userName) {
		return calendarUserConfigDao.getCalendarToken(calendarType, calendarID, userName);
	}

	protected Calendar buildCalendar(Kalendar kalendar) {
		Calendar calendar = new Calendar();
		// add standard propeties
		calendar.getProperties().add(new ProdId("-//Ben Fortuna//iCal4j 1.0//EN"));
		calendar.getProperties().add(Version.VERSION_2_0);
		calendar.getProperties().add(CalScale.GREGORIAN);
		for (Iterator<KalendarEvent> iter = kalendar.getEvents().iterator(); iter.hasNext();) {
			KalendarEvent kEvent = iter.next();
			VEvent vEvent = getVEvent(kEvent);
			calendar.getComponents().add(vEvent);
		}
		return calendar;
	}
	
	@Override
	public KalendarEvent createKalendarEventRecurringOccurence(KalendarRecurEvent recurEvent) {
		KalendarEvent rootEvent = recurEvent.getCalendar().getEvent(recurEvent.getID(), null);
		VEvent vEvent = getVEvent(recurEvent);
		
		PropertyList vEventProperties = vEvent.getProperties();
		for(Iterator<?> objIt=vEventProperties.iterator(); objIt.hasNext(); ) {
			Object property = objIt.next();
			if(property instanceof RRule || property instanceof ExDate) {
				objIt.remove();
			}
		}

		try {
			Kalendar calendar = recurEvent.getCalendar();
			Date startDate = recurEvent.getOccurenceDate();
			String startString = CalendarUtils.formatRecurrenceDate(startDate, rootEvent.isAllDayEvent());
			RecurrenceId recurId;
			if(rootEvent.isAllDayEvent()) {
				recurId = new RecurrenceId(tz);
				recurId.setDate(CalendarUtils.createDate(startDate));
			} else {
				recurId = new RecurrenceId(startString, tz);
			}
			vEventProperties.add(recurId);
			KalendarEvent kEvent = getKalendarEvent(vEvent);
			kEvent.setKalendar(calendar);
			return kEvent;
		} catch (ParseException e) {
			log.error("", e);
			return null;
		}
	}
	
	private VEvent getVEvent(KalendarEvent kEvent) {
		VEvent vEvent;
		if (!kEvent.isAllDayEvent()) {
			// regular VEvent
			DateTime dtBegin = new DateTime(kEvent.getBegin());
			if(tz != null) {
				dtBegin.setTimeZone(tz);
			}

			Date kEventEnd = kEvent.getEnd();
			if(kEventEnd == null) {
				vEvent = new VEvent(dtBegin, kEvent.getSubject());
			} else {
				DateTime dtEnd = new DateTime(kEventEnd);
				if(tz != null) {
					dtEnd.setTimeZone(tz);
				}
				vEvent = new VEvent(dtBegin, dtEnd, kEvent.getSubject());
			}
		} else {
			// AllDay VEvent
			net.fortuna.ical4j.model.Date dtBegin = CalendarUtils.createDate(kEvent.getBegin());
			// adjust end date: ICal end dates for all day events are on the next day
			Date adjustedEndDate = new Date(kEvent.getEnd().getTime() + (1000 * 60 * 60 * 24));
			net.fortuna.ical4j.model.Date dtEnd = CalendarUtils.createDate(adjustedEndDate);
			vEvent = new VEvent(dtBegin, dtEnd, kEvent.getSubject());
		}
		
		if(kEvent.getCreated() > 0) {
			Created created = new Created(new DateTime(kEvent.getCreated()));
			vEvent.getProperties().add(created);
		}

		if( (kEvent.getCreatedBy() != null) && !kEvent.getCreatedBy().trim().isEmpty()) {
			Contact contact = new Contact();
			contact.setValue(kEvent.getCreatedBy());
			vEvent.getProperties().add(contact);
		}

		if(kEvent.getLastModified() > 0) {
			LastModified lastMod = new LastModified(new DateTime(kEvent.getLastModified()));
			vEvent.getProperties().add(lastMod);
		}

		// Uid
		PropertyList vEventProperties = vEvent.getProperties();
		vEventProperties.add(new Uid(kEvent.getID()));
		
		// clazz
		switch (kEvent.getClassification()) {
			case KalendarEvent.CLASS_PRIVATE: vEventProperties.add(ICAL_CLASS_PRIVATE); break;
			case KalendarEvent.CLASS_PUBLIC: vEventProperties.add(ICAL_CLASS_PUBLIC); break;
			case KalendarEvent.CLASS_X_FREEBUSY: vEventProperties.add(ICAL_CLASS_X_FREEBUSY); break;
			default: vEventProperties.add(ICAL_CLASS_PRIVATE); break;
		}

		// location
		if (kEvent.getLocation() != null) {
			vEventProperties.add(new Location(kEvent.getLocation()));
		}
		
		if(kEvent.getDescription() != null) {
			vEventProperties.add(new Description(kEvent.getDescription()));
		}
		
		// event links
		Url urlOnce = null;
		List<KalendarEventLink> kalendarEventLinks = kEvent.getKalendarEventLinks();
		if ((kalendarEventLinks != null) && !kalendarEventLinks.isEmpty()) {
			for (Iterator<KalendarEventLink> iter = kalendarEventLinks.iterator(); iter.hasNext();) {
				KalendarEventLink link = iter.next();
				StringBuilder linkEncoded = new StringBuilder(200);
				linkEncoded.append(link.getProvider());
				linkEncoded.append("§");
				linkEncoded.append(link.getId());
				linkEncoded.append("§");
				linkEncoded.append(link.getDisplayName());
				linkEncoded.append("§");
				linkEncoded.append(link.getURI());
				linkEncoded.append("§");
				linkEncoded.append(link.getIconCssClass());
				XProperty linkProperty = new XProperty(ICAL_X_OLAT_LINK, linkEncoded.toString());
				vEventProperties.add(linkProperty);
				if(urlOnce == null) {
					try {
						Url url = new Url();
						url.setValue(link.getURI());
						urlOnce = url;
					} catch (Exception e) {
						log.error("Invalid URL: {}", link.getURI());
					}
				}
			}
		}
		if(urlOnce != null) {
			vEventProperties.add(urlOnce);
		}
		
		if (kEvent.getComment() != null) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_COMMENT, kEvent.getComment()));
		}
		if (kEvent.getNumParticipants() != null) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_NUMPARTICIPANTS, Integer.toString(kEvent.getNumParticipants())));
		}
		if (kEvent.getParticipants() != null) {
			StringBuilder strBuf = new StringBuilder();
			String[] participants = kEvent.getParticipants();
			for ( String participant : participants ) {
				strBuf.append(participant);
				strBuf.append("§");
			}
			vEventProperties.add(new XProperty(ICAL_X_OLAT_PARTICIPANTS, strBuf.toString()));
		}
		if (kEvent.getSourceNodeId() != null) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_SOURCENODEID, kEvent.getSourceNodeId()));
		}
		
		if(kEvent.getManagedFlags() != null) {
			String val = CalendarManagedFlag.toString(kEvent.getManagedFlags());
			vEventProperties.add(new XProperty(ICAL_X_OLAT_MANAGED, val));
		}
		
		if(StringHelper.containsNonWhitespace(kEvent.getExternalId())) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_EXTERNAL_ID, kEvent.getExternalId()));
		}
		
		if(StringHelper.containsNonWhitespace(kEvent.getExternalSource())) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_EXTERNAL_SOURCE, kEvent.getExternalSource()));
		}
		
		String recurenceId = kEvent.getRecurrenceID();
		if(StringHelper.containsNonWhitespace(recurenceId)) {
			try {
				RecurrenceId recurId;
				// VALUE=DATE recurrence id need to be specially saved
				if(recurenceId.length() < 9) {
					recurId = new RecurrenceId(tz);
					recurId.setDate(CalendarUtils.createDate(new net.fortuna.ical4j.model.Date(recurenceId)));
				} else {
					recurId = new RecurrenceId(recurenceId, tz);
				}
				
				vEventProperties.add(recurId);
			} catch (ParseException e) {
				log.error("cannot create recurrence ID: {}", recurenceId, e);
			}
		}
		
		// recurrence
		String recurrence = kEvent.getRecurrenceRule();
		if(recurrence != null && !recurrence.equals("")) {
			try {
				Recur recur = new Recur(recurrence);
				RRule rrule = new RRule(recur);
				vEventProperties.add(rrule);
			} catch (ParseException e) {
				log.error("cannot create recurrence rule: {}", recurrence, e);
			}
		}
		
		// recurrence exclusions
		String recurrenceExc = kEvent.getRecurrenceExc();
		if(recurrenceExc != null && !recurrenceExc.equals("")) {
			ExDate exdate = new ExDate();
			try {
				exdate.setValue(recurrenceExc);
				vEventProperties.add(exdate);
			} catch (ParseException e) {
				log.error("", e);
			}
		}
		
		// video stream
		if(StringHelper.containsNonWhitespace(kEvent.getLiveStreamUrl())) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_VIDEO_STREAM_URL, kEvent.getLiveStreamUrl()));
		}
		if (kEvent.getLiveStreamUrlTemplateKey() != null) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_VIDEO_STREAM_URL_TEMPLATE_KEY, kEvent.getLiveStreamUrlTemplateKey().toString()));
		}
		
		return vEvent;
	}
	
	/**
	 * Build a KalendarEvent out of a source VEvent.
	 * @param event
	 * @return
	 */
	private KalendarEvent getKalendarEvent(VEvent event) {
		// subject
		Summary eventsummary = event.getSummary();
		String subject = "";
		if (eventsummary != null)
			subject = eventsummary.getValue();
		// start
		DtStart dtStart = event.getStartDate();
		Date start = dtStart.getDate();
		Duration dur = event.getDuration();
		// end
		Date end = null;
		if (dur != null) {
			end = dur.getDuration().getTime(start);
		} else if(event.getEndDate() != null) { 
			end = event.getEndDate().getDate();
		}

		// check all day event first
		boolean isAllDay = false;
		Parameter dateParameter = event.getProperties().getProperty(Property.DTSTART)
				.getParameters().getParameter(Value.DATE.getName());
		if (dateParameter != null) {
			isAllDay = true;
			
			//Make sure the time of the dates are 00:00 localtime because DATE fields in iCal are GMT 00:00 
			//Note that start date and end date can have different offset because of daylight saving switch
			java.util.TimeZone timezone = java.util.GregorianCalendar.getInstance().getTimeZone();
			start = new Date(start.getTime() - timezone.getOffset(start.getTime()));
			end   = new Date(end.getTime() - timezone.getOffset(end.getTime()));
			
			// adjust end date: ICal sets end dates to the next day
			end = new Date(end.getTime() - (1000 * 60 * 60 * 24));
		} else if(start != null && end != null && (end.getTime() - start.getTime()) == (24 * 60 * 60 * 1000)) {
			//check that start has no hour, no minute and no second
			java.util.Calendar cal = java.util.Calendar.getInstance();
			cal.setTime(start);
			isAllDay = cal.get(java.util.Calendar.HOUR_OF_DAY) == 0 && cal.get(java.util.Calendar.MINUTE) == 0
					&& cal.get(java.util.Calendar.SECOND) == 0 && cal.get(java.util.Calendar.MILLISECOND) == 0;
			// adjust end date: ICal sets end dates to the next day
			end = new Date(end.getTime() - (1000 * 60 * 60 * 24));
		}
		
		Uid eventuid = event.getUid();
		String uid;
		if (eventuid != null) {
			uid = eventuid.getValue();
		} else {
			uid = CodeHelper.getGlobalForeverUniqueID();
		}
		RecurrenceId eventRecurenceId = event.getRecurrenceId();
		String recurrenceId = null;
		if(eventRecurenceId != null) {
			recurrenceId = eventRecurenceId.getValue();
		}

		KalendarEvent calEvent = new KalendarEvent(uid, recurrenceId, subject, start, end);
		calEvent.setAllDayEvent(isAllDay);
		
		// classification
		Clazz classification = event.getClassification();
		if (classification != null) {
			String sClass = classification.getValue();
			int iClassification = KalendarEvent.CLASS_PRIVATE;
			if (sClass.equals(ICAL_CLASS_PRIVATE.getValue())) iClassification = KalendarEvent.CLASS_PRIVATE;
			else if (sClass.equals(ICAL_CLASS_X_FREEBUSY.getValue())) iClassification = KalendarEvent.CLASS_X_FREEBUSY;
			else if (sClass.equals(ICAL_CLASS_PUBLIC.getValue())) iClassification = KalendarEvent.CLASS_PUBLIC;
			calEvent.setClassification(iClassification);
		}
		// created/last modified
		Created created = event.getCreated();
		if (created != null) {
			calEvent.setCreated(created.getDate().getTime());
		}
		// created/last modified
		Contact contact = (Contact)event.getProperty(Property.CONTACT);
		if (contact != null) {
			calEvent.setCreatedBy(contact.getValue());
		}
		
		LastModified lastModified = event.getLastModified();
		if (lastModified != null) {
			calEvent.setLastModified(lastModified.getDate().getTime());
		}
		
		Description description = event.getDescription();
		if(description != null) {
			calEvent.setDescription(description.getValue());
		}
		
		// location
		Location location = event.getLocation();
		if (location != null) {
			calEvent.setLocation(location.getValue());
		}
		
		// links if any
		PropertyList linkProperties = event.getProperties(ICAL_X_OLAT_LINK);
		List<KalendarEventLink> kalendarEventLinks = new ArrayList<>();
		for (Iterator<?> iter = linkProperties.iterator(); iter.hasNext();) {
			XProperty linkProperty = (XProperty) iter.next();
			if (linkProperty != null) {
				String encodedLink = linkProperty.getValue();
				StringTokenizer st = new StringTokenizer(encodedLink, "§", false);
				if (st.countTokens() >= 4) {
					String provider = st.nextToken();
					String id = st.nextToken();
					String displayName = st.nextToken();
					String uri = st.nextToken();
					String iconCss = "";
					// migration: iconCss has been added later, check if available first
					if (st.hasMoreElements()) { 
						iconCss = st.nextToken();
					}
					KalendarEventLink eventLink = new KalendarEventLink(provider, id, displayName, uri, iconCss);
					kalendarEventLinks.add(eventLink);
				}
			}
		}
		calEvent.setKalendarEventLinks(kalendarEventLinks);
		
		Property comment = event.getProperty(ICAL_X_OLAT_COMMENT);
		if (comment != null)
			calEvent.setComment(comment.getValue());
		
		Property numParticipants = event.getProperty(ICAL_X_OLAT_NUMPARTICIPANTS);
		if (numParticipants != null)
			calEvent.setNumParticipants(Integer.parseInt(numParticipants.getValue()));
		
		Property participants = event.getProperty(ICAL_X_OLAT_PARTICIPANTS);
		if (participants != null) {
			StringTokenizer strTok = new StringTokenizer(participants.getValue(), "§", false);
			String[] parts = new String[strTok.countTokens()];
			for ( int i = 0; strTok.hasMoreTokens(); i++ ) {
				parts[i] = strTok.nextToken();
			}
			calEvent.setParticipants(parts);
		}
		
		Property sourceNodId = event.getProperty(ICAL_X_OLAT_SOURCENODEID);
		if (sourceNodId != null) {
			calEvent.setSourceNodeId(sourceNodId.getValue());
		}
		
		//managed properties
		Property managed = event.getProperty(ICAL_X_OLAT_MANAGED);
		if(managed != null) {
			String value = managed.getValue();
			if("true".equals(value)) {
				value = "all";
			}
			CalendarManagedFlag[] values = CalendarManagedFlag.toEnum(value);
			calEvent.setManagedFlags(values);
		}
		Property externalId = event.getProperty(ICAL_X_OLAT_EXTERNAL_ID);
		if(externalId != null) {
			calEvent.setExternalId(externalId.getValue());
		}
		Property externalSource = event.getProperty(ICAL_X_OLAT_EXTERNAL_SOURCE);
		if(externalSource != null) {
			calEvent.setExternalSource(externalSource.getValue());
		}
		
		// recurrence
		if (event.getProperty(ICAL_RRULE) != null) {
			calEvent.setRecurrenceRule(event.getProperty(ICAL_RRULE).getValue());
		}

		// recurrence exclusions
		if (event.getProperty(ICAL_EXDATE) != null) {
			calEvent.setRecurrenceExc(event.getProperty(ICAL_EXDATE).getValue());
		}
		
		// video stream
		Property liveStreamUrl = event.getProperty(ICAL_X_OLAT_VIDEO_STREAM_URL);
		if(liveStreamUrl != null) {
			calEvent.setLiveStreamUrl(liveStreamUrl.getValue());
		}
		
		Property liveStreamUrlTemplateKey = event.getProperty(ICAL_X_OLAT_VIDEO_STREAM_URL_TEMPLATE_KEY);
		if (liveStreamUrlTemplateKey != null)
			calEvent.setLiveStreamUrlTemplateKey(Long.parseLong(liveStreamUrlTemplateKey.getValue()));
		
		return calEvent;
	}

	@Override
	public KalendarEvent getRecurringInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		boolean isRecurring= isRecurringInPeriod(periodStart, periodEnd, kEvent);
		KalendarEvent recurEvent = null;

		if(isRecurring) {
			java.util.Calendar periodStartCal = java.util.Calendar.getInstance();
			java.util.Calendar eventBeginCal = java.util.Calendar.getInstance();
			
			periodStartCal.setTime(periodStart);
			eventBeginCal.setTime(kEvent.getBegin());
			
			Long duration = kEvent.getEnd().getTime() - kEvent.getBegin().getTime();

			java.util.Calendar beginCal = java.util.Calendar.getInstance();
			beginCal.setTime(kEvent.getBegin());
			beginCal.set(java.util.Calendar.YEAR, periodStartCal.get(java.util.Calendar.YEAR));
			beginCal.set(java.util.Calendar.MONTH, periodStartCal.get(java.util.Calendar.MONTH));
			beginCal.set(java.util.Calendar.DAY_OF_MONTH, periodStartCal.get(java.util.Calendar.DAY_OF_MONTH));

			recurEvent = kEvent.clone();
			recurEvent.setBegin(beginCal.getTime());
			recurEvent.setEnd(new Date(beginCal.getTime().getTime() + duration));
		}

		return recurEvent;
	}

	@Override
	public boolean isRecurringInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		DateList recurDates = getRecurringsInPeriod(periodStart, periodEnd, kEvent);
		return (recurDates != null && !recurDates.isEmpty());
	}

	@Override
	public File getCalendarFile(String type, String calendarID) {
		return new File(fStorageBase, "/" + type + "/" + calendarID + ".ics");
	}

	@Override
	public KalendarRenderWrapper getPersonalCalendar(Identity identity) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_USER, identity.getName());
		String fullName = userManager.getUserDisplayName(identity);
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal, fullName, null);
		calendarWrapper.setCssClass(KalendarRenderWrapper.CALENDAR_COLOR_BLUE);
		calendarWrapper.setVisible(true);
		return calendarWrapper;
	}

	@Override
	public KalendarRenderWrapper getImportedCalendar(Identity identity, String calendarId) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_USER, calendarId);
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal, calendarId, null);
		calendarWrapper.setCssClass(KalendarRenderWrapper.CALENDAR_COLOR_BLUE);
		calendarWrapper.setVisible(true);
		calendarWrapper.setImported(true);
		return calendarWrapper;
	}

	@Override
	public KalendarRenderWrapper getGroupCalendar(BusinessGroup businessGroup) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_GROUP, businessGroup.getResourceableId().toString());
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal, businessGroup.getName(), null);
		calendarWrapper.setCssClass(KalendarRenderWrapper.CALENDAR_COLOR_ORANGE);
		calendarWrapper.setVisible(true);
		return calendarWrapper;
	}

	@Override
	public KalendarRenderWrapper getCourseCalendar(ICourse course) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
		String externalRef = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getExternalRef();
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal, course.getCourseTitle(), externalRef);
		calendarWrapper.setCssClass(KalendarRenderWrapper.CALENDAR_COLOR_GREEN);
		calendarWrapper.setVisible(true);
		return calendarWrapper;
	}
	
	@Override
	public KalendarRenderWrapper getCalendarForDeletion(OLATResourceable resource) {
		String type;
		if("CourseModule".equals(resource.getResourceableTypeName())) {
			type = CalendarManager.TYPE_COURSE;
		} else {
			type = CalendarManager.TYPE_GROUP;
		}
		Kalendar cal = getCalendar(type, resource.getResourceableId().toString());
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal, "To delete", null);
		calendarWrapper.setCssClass(KalendarRenderWrapper.CALENDAR_COLOR_GREEN);
		calendarWrapper.setVisible(true);
		return calendarWrapper;
	}

	@Override
	public void deletePersonalCalendar(Identity identity) {
		deleteCalendar(CalendarManager.TYPE_USER, identity.getName());
	}

	@Override
	public void deleteGroupCalendar(BusinessGroup businessGroup) {
		deleteCalendar(CalendarManager.TYPE_GROUP, businessGroup.getResourceableId().toString());
	}

	@Override
	public void deleteCourseCalendar(ICourse course) {
		deleteCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
	}

	@Override
	public void deleteCourseCalendar(OLATResourceable course) {
		deleteCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
	}

	@Override
	public boolean addEventTo(final Kalendar cal, final KalendarEvent kalendarEvent) {
		return addEventTo(cal, Collections.singletonList(kalendarEvent));
  }
	
	@Override
	public boolean addEventTo(final Kalendar cal, final List<KalendarEvent> kalendarEvents) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean persistSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> {
			Kalendar loadedCal = getCalendarFromCache(cal.getType(),cal.getCalendarID());
			for(KalendarEvent kalendarEvent:kalendarEvents) {
				loadedCal.addEvent(kalendarEvent);
				kalendarEvent.resetImmutableDates();
			}
			boolean successfullyPersist = persistCalendar(loadedCal);
			return Boolean.valueOf(successfullyPersist);
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return persistSuccessful.booleanValue();
	}
	
	@Override
	public boolean removeEventsFrom(final Kalendar cal, final List<KalendarEvent> kalendarEvents) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean removeSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> {
			Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
			for(KalendarEvent kalendarEvent:kalendarEvents) {
				String uid = kalendarEvent.getID();
				String recurrenceId = kalendarEvent.getRecurrenceID();
				if(StringHelper.containsNonWhitespace(recurrenceId)) {
					loadedCal.removeEvent(kalendarEvent);
					KalendarEvent rootEvent = loadedCal.getEvent(kalendarEvent.getID(), null);
					if(rootEvent != null && kalendarEvent instanceof KalendarRecurEvent) {
						Date recurrenceDate = ((KalendarRecurEvent)kalendarEvent).getOccurenceDate();
						rootEvent.addRecurrenceExc(recurrenceDate);
					}
				} else {
					for(KalendarEvent kEvent:loadedCal.getEvents()) {
						if(uid.equals(kEvent.getID())) {
							loadedCal.removeEvent(kEvent);
						}
					}
				}
			}
			boolean successfullyPersist = persistCalendar(loadedCal);
			return Boolean.valueOf(successfullyPersist);
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return removeSuccessful.booleanValue();
    }

	@Override
	public boolean removeEventFrom(final Kalendar cal, final KalendarEvent kalendarEvent) {
		return removeEventsFrom(cal, Collections.singletonList(kalendarEvent));
    }
	
	@Override
	public boolean removeOccurenceOfEvent(final Kalendar cal, final KalendarRecurEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean removeSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(calOres, () -> {
			String uid = kalendarEvent.getID();
			Date occurenceDate = kalendarEvent.getBegin();
	
			Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
			KalendarEvent rootEvent = loadedCal.getEvent(kalendarEvent.getID(), null);
			rootEvent.addRecurrenceExc(kalendarEvent.getBegin());
			
			for(KalendarEvent kEvent:loadedCal.getEvents()) {
				if(uid.equals(kEvent.getID())
						&& kEvent.getOccurenceDate() != null
						&& occurenceDate.equals(kEvent.getOccurenceDate())) {
					loadedCal.removeEvent(kEvent);
				}
			}
			boolean successfullyPersist = persistCalendar(loadedCal);
			return Boolean.valueOf(successfullyPersist);
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return removeSuccessful.booleanValue();
		
	}

	@Override
	public boolean removeFutureOfEvent(Kalendar cal, KalendarRecurEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean removeSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(calOres, () -> {
			boolean successfullyPersist = false;
			try {
				String uid = kalendarEvent.getID();
				Date occurenceDate = kalendarEvent.getOccurenceDate();

				Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
				KalendarEvent rootEvent = loadedCal.getEvent(kalendarEvent.getID(), null);
				String rRule = rootEvent.getRecurrenceRule();
				
				Recur recur = new Recur(rRule);
				recur.setUntil(CalendarUtils.createDate(occurenceDate));
				RRule rrule = new RRule(recur);
				rootEvent.setRecurrenceRule(rrule.getValue());
				
				for(KalendarEvent kEvent:loadedCal.getEvents()) {
					if(uid.equals(kEvent.getID())
							&& StringHelper.containsNonWhitespace(kEvent.getRecurrenceID())
							&& occurenceDate.before(kEvent.getBegin())) {
						loadedCal.removeEvent(kEvent);
					}
				}
				
				successfullyPersist = persistCalendar(loadedCal);
			} catch (ParseException e) {
				log.error("", e);
			}
			return Boolean.valueOf(successfullyPersist);
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return removeSuccessful.booleanValue();
	}

	@Override
	public boolean updateEventFrom(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> updateEventAlreadyInSync(cal, kalendarEvent));
		return updatedSuccessful.booleanValue();
    }

	@Override
	public boolean updateEventsFrom(Kalendar cal, List<KalendarEvent> kalendarEvents) {
		final OLATResourceable calOres = getOresHelperFor(cal);
		Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> {
			Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
			for(KalendarEvent kalendarEvent:kalendarEvents) {
				loadedCal.removeEvent(kalendarEvent); // remove old event
				loadedCal.addEvent(kalendarEvent); // add changed event
			}
			boolean successfullyPersist = persistCalendar(loadedCal);
			// inform all controller about calendar change for reload
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
			return successfullyPersist;
		});
		return updatedSuccessful.booleanValue();
	}

	/**
	 * @see org.olat.commons.calendar.CalendarManager#updateEventFrom(org.olat.commons.calendar.model.Kalendar, org.olat.commons.calendar.model.KalendarEvent)
	 */
	@Override
	public boolean updateEventAlreadyInSync(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(calOres);
		Kalendar reloadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
		
		if(StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceRule())) {
			Date oldBegin = kalendarEvent.getImmutableBegin();
			Date oldEnd = kalendarEvent.getImmutableEnd();
			
			KalendarEvent originalEvent = reloadedCal.getEvent(kalendarEvent.getID(), null);

			Date newBegin = kalendarEvent.getBegin();
			Date newEnd = kalendarEvent.getEnd();
			long beginDiff = newBegin.getTime() - oldBegin.getTime();
			long endDiff = newEnd.getTime() - oldEnd.getTime();

			java.util.Calendar cl = java.util.Calendar.getInstance();
			cl.setTime(originalEvent.getBegin());
			cl.add(java.util.Calendar.MILLISECOND, (int)beginDiff);
			kalendarEvent.setBegin(cl.getTime());
			cl.setTime(originalEvent.getEnd());
			cl.add(java.util.Calendar.MILLISECOND, (int)endDiff);
			kalendarEvent.setEnd(cl.getTime());

			List<KalendarEvent> exEvents = new ArrayList<>();
			List<KalendarEvent> allEvents = reloadedCal.getEvents();
			for(KalendarEvent event:allEvents) {
				if(event.getID().equals(kalendarEvent.getID()) && StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
					exEvents.add(event);
				}
			}
			
			if(exEvents.size() > 0) {
				for(KalendarEvent exEvent:exEvents) {
					try {
						reloadedCal.removeEvent(exEvent);
						String recurrenceId = exEvent.getRecurrenceID();
						
						RecurrenceId recurId = new RecurrenceId(recurrenceId, tz);
						Date currentRecurrence = recurId.getDate();
						java.util.Calendar calc = java.util.Calendar.getInstance();
						calc.clear();
						calc.setTime(currentRecurrence);
						if(beginDiff > 0) {
							calc.add(java.util.Calendar.MILLISECOND, (int)beginDiff);
						}
						
						Date newRecurrenceDate = calc.getTime();
						
						boolean allDay = kalendarEvent.isAllDayEvent();
						RecurrenceId newRecurId;
						if(allDay) {
							newRecurId = new RecurrenceId(tz);
							newRecurId.setDate(CalendarUtils.createDate(newRecurrenceDate));
						} else {
							String startString = CalendarUtils.formatRecurrenceDate(newRecurrenceDate, false);
							newRecurId = new RecurrenceId(startString, tz);
						}
						exEvent.setRecurrenceID(newRecurId.getValue());
						reloadedCal.addEvent(exEvent);
					} catch (ParseException e) {
						log.error("", e);
					}
				}
			}
		}

		reloadedCal.removeEvent(kalendarEvent); // remove old event
		kalendarEvent.resetImmutableDates();
		reloadedCal.addEvent(kalendarEvent); // add changed event

		boolean successfullyPersist = persistCalendar(reloadedCal);
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return successfullyPersist;
	}
	
	@Override
	public boolean updateCalendar(final Kalendar cal, final Kalendar importedCal) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, () -> {
			Map<KalendarEventKey,KalendarEvent> uidToEvent = new HashMap<>();
			for(KalendarEvent event:cal.getEvents()) {
				if(StringHelper.containsNonWhitespace(event.getID())) {
					uidToEvent.put(new KalendarEventKey(event), event);
				}
			}
			
			Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
			for(KalendarEvent importedEvent:importedCal.getEvents()) {
				KalendarEventKey uid = new KalendarEventKey(importedEvent);
				if(uidToEvent.containsKey(uid)) {
					loadedCal.removeEvent(importedEvent); // remove old event
					loadedCal.addEvent(importedEvent); // add changed event
				} else {
					loadedCal.addEvent(importedEvent);
				}
			}
			
			boolean successfullyPersist = persistCalendar(cal);
			// inform all controller about calendar change for reload
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new CalendarGUIModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
			return Boolean.valueOf(successfullyPersist);
		});
		return updatedSuccessful.booleanValue();
	}
	
	/**
	 * Load a calendar when a calendar exists or create a new one.
	 * This method is not thread-safe. Must be called from synchronized block!
	 * @param callType
	 * @param callCalendarID
	 * @return
	 */
	protected Kalendar loadOrCreateCalendar(final String callType, final String callCalendarID) {
		if (!calendarExists(callType, callCalendarID)) {
			return createCalendar(callType, callCalendarID);
		} else {
			return loadCalendarFromFile(callType, callCalendarID);
		}
	}
	
	@Override
	public List<KalendarEvent> getEvents(Kalendar calendar, Date from, Date to, boolean privateEventsVisible) {
		List<KalendarEvent> allEvents = calendar.getEvents();
		List<KalendarEvent> events = new ArrayList<>(128);
		
		Map<String, List<KalendarRecurEvent>> idToRecurringEvents = new HashMap<>();
		//first pass, ignore events with recurrenceId
		for(KalendarEvent event:allEvents) {
			if(!privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_PRIVATE) {
				continue;
			}
			if(StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
				continue;
			}

			if (StringHelper.containsNonWhitespace(event.getRecurrenceRule())) {
				List<KalendarRecurEvent> recurringEvents = getRecurringEventsInPeriod(event, from, to, tz);
				if(recurringEvents.size() > 0) {
					idToRecurringEvents.put(event.getID(), recurringEvents);
					for (KalendarRecurEvent recurEvent:recurringEvents) {
						events.add(recurEvent);
					}
				}
			} else if(isInRange(from, to, event)) {
				events.add(event);
			}
		}
		
		//process events with recurrenceId
		for(KalendarEvent event:allEvents) {
			if(!StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
				continue;
			}

			String id = event.getID();
			if(idToRecurringEvents.containsKey(id)) {
				VEvent vEvent = getVEvent(event);
				RecurrenceId recurrenceId = vEvent.getRecurrenceId();
				net.fortuna.ical4j.model.Date startDate = recurrenceId.getDate();
				if(startDate instanceof net.fortuna.ical4j.model.DateTime) {
					List<KalendarRecurEvent> recurringEvents = idToRecurringEvents.get(id);
					for(KalendarRecurEvent recurEvent:recurringEvents) {
						Date beginDate = recurEvent.getBegin();
						if(beginDate.equals(startDate)) {
							recurEvent.setRecurrenceEvent(event);
						}
					}
				} else {
					List<KalendarRecurEvent> recurringEvents = idToRecurringEvents.get(id);
					for(KalendarRecurEvent recurEvent:recurringEvents) {
						Date beginDate = recurEvent.getBegin();
						net.fortuna.ical4j.model.Date occDate = CalendarUtils.createDate(beginDate);
						if(occDate.equals(startDate)) {
							recurEvent.setRecurrenceEvent(event);
						}
					}
				}
			}
		}
		
		return events;
	}
	
	private final boolean isInRange(Date from, Date to, KalendarEvent event) {
		Date begin = event.getBegin();
		Date end = CalendarUtils.endOf(event);

		if(begin != null && end != null) {
			if(from.compareTo(begin) <= 0 && to.compareTo(end) >= 0) {
				return true;
			} else if(begin.compareTo(from) <= 0 && end.compareTo(to) >= 0) {
				return true;
			} else if(from.compareTo(begin) <= 0 && to.compareTo(begin) >= 0) {
				return true;
			} else if(from.compareTo(end) <= 0 && to.compareTo(end) >= 0) {
				return true;
			}
		} else if(begin != null) {
			if(from.compareTo(begin) <= 0 && to.compareTo(begin) >= 0) {
				return false;
			}
		} else if(end != null) {
			if(from.compareTo(end) <= 0 && to.compareTo(end) >= 0) {
				return true;
			}
		}
		return false;
	}
	
	private final List<KalendarRecurEvent> getRecurringEventsInPeriod(KalendarEvent kEvent, Date periodStart, Date periodEnd, TimeZone userTz) {
		VEvent vEvent = getVEvent(kEvent);
		if(vEvent.getEndDate() == null || vEvent.getStartDate().getDate().after(vEvent.getEndDate().getDate())) {
			return Collections.emptyList();
		}

		//calculate the events in the specified period
        Period recurringPeriod = new Period(new DateTime(periodStart), new DateTime(periodEnd));
		PeriodList periodList = vEvent.calculateRecurrenceSet(recurringPeriod);
		List<KalendarRecurEvent> recurringEvents = new ArrayList<>(periodList.size());
		
		for(Object obj : periodList) {
			Period period = (Period)obj;
			Date date = period.getStart();

			java.util.Calendar eventStartCal = java.util.Calendar.getInstance();
			eventStartCal.clear();
			eventStartCal.setTime(kEvent.getBegin());
			
			java.util.Calendar eventEndCal = java.util.Calendar.getInstance();
			eventEndCal.clear();
			eventEndCal.setTime(kEvent.getEnd());
			
			java.util.Calendar recurStartCal = java.util.Calendar.getInstance();
			recurStartCal.clear();
			if(userTz == null) {
				recurStartCal.setTimeInMillis(date.getTime());
			} else {
				recurStartCal.setTimeInMillis(date.getTime() - userTz.getOffset(date.getTime()));
			}
			long duration = kEvent.getEnd().getTime() - kEvent.getBegin().getTime();

			java.util.Calendar beginCal = java.util.Calendar.getInstance();
			beginCal.clear();
			beginCal.set(recurStartCal.get(java.util.Calendar.YEAR), recurStartCal.get(java.util.Calendar.MONTH), recurStartCal.get(java.util.Calendar.DATE), 
				eventStartCal.get(java.util.Calendar.HOUR_OF_DAY), eventStartCal.get(java.util.Calendar.MINUTE), eventStartCal.get(java.util.Calendar.SECOND));
			
			java.util.Calendar endCal = java.util.Calendar.getInstance();
			endCal.clear();
			endCal.setTimeInMillis(beginCal.getTimeInMillis() + duration);

			boolean original = false;
			if(kEvent.getBegin().compareTo(beginCal.getTime()) == 0) {
				original = true; //prevent doubled events
			}

			Date recurrenceEnd = getRecurrenceEndDate(kEvent.getRecurrenceRule());
			if(kEvent.isAllDayEvent() && recurrenceEnd != null && recurStartCal.getTime().after(recurrenceEnd)) {
				continue; //workaround for ical4j-bug in all day events
			}
		
			KalendarRecurEvent recurEvent = new KalendarRecurEvent(kEvent.getID(), original, kEvent.getSubject(), beginCal.getTime(), endCal.getTime());
			recurEvent.setOccurenceDate(beginCal.getTime());
			recurEvent.setSourceEvent(kEvent);
			recurringEvents.add(recurEvent);
		}
		return recurringEvents;
	}
	
	private final DateList getRecurringsInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		DateList recurDates = null;
		String recurrenceRule = kEvent.getRecurrenceRule();
		if(StringHelper.containsNonWhitespace(recurrenceRule)) {
			try {
				Recur recur = new Recur(recurrenceRule);
				net.fortuna.ical4j.model.Date periodStartDate = CalendarUtils.createDate(periodStart);
				net.fortuna.ical4j.model.Date periodEndDate = CalendarUtils.createDate(periodEnd);
				net.fortuna.ical4j.model.Date eventStartDate = CalendarUtils.createDate(kEvent.getBegin());
				recurDates = recur.getDates(eventStartDate, periodStartDate, periodEndDate, Value.DATE);
			} catch (ParseException e) {
				log.error("cannot restore recurrence rule: " + recurrenceRule, e);
			}
			
			String recurrenceExc = kEvent.getRecurrenceExc();
			if(recurrenceExc != null && !recurrenceExc.equals("")) {
				try {
					ExDate exdate = new ExDate();
					// expected date+time format: 
					// 20100730T100000
					// unexpected all-day format:
					// 20100730
					// see OLAT-5645
					if (recurrenceExc.length() > 8) {
						exdate.setValue(recurrenceExc);
					} else {
						exdate.getParameters().replace(Value.DATE);
						exdate.setValue(recurrenceExc);
					}
					for( Object date : exdate.getDates() ) {
						if(recurDates.contains(date)) recurDates.remove(date);
					}
				} catch (ParseException e) {
					log.error("cannot restore excluded dates for this recurrence: " + recurrenceExc, e);
				}
			}
		}
		
		return recurDates;
	}
	
	/**
	 * 
	 * @param rule
	 * @return date of recurrence end
	 */
	@Override
	public Date getRecurrenceEndDate(String rule) {
		if (rule != null) {
			try {
				TimeZone ltz = calendarModule.getDefaultTimeZone();
				Recur recur = new Recur(rule);
				Date dUntil = recur.getUntil();
				DateTime dtUntil = dUntil == null ? null : new DateTime(dUntil.getTime());
				if(dtUntil != null) {
					if(ltz != null) {
						dtUntil.setTimeZone(ltz);
					}
					return dtUntil;
				}
			} catch (ParseException e) {
				log.error("cannot restore recurrence rule", e);
			}
		}
		
		return null;
	}
	
	/**
	 * Build iCalendar-compliant recurrence rule
	 * @param recurrence
	 * @param recurrenceEnd
	 * @return rrule
	 */
	@Override
	public String getRecurrenceRule(String recurrence, Date recurrenceEnd) {
		if (recurrence != null) { // recurrence available
			// create recurrence rule
			StringBuilder sb = new StringBuilder();
			sb.append("FREQ=");
			if(recurrence.equals(KalendarEvent.WORKDAILY)) {
				// build rule for monday to friday
				sb.append(KalendarEvent.DAILY).append(";").append("BYDAY=MO,TU,WE,TH,FR");
			} else if(recurrence.equals(KalendarEvent.BIWEEKLY)) {
				// build rule for biweekly
				sb.append(KalendarEvent.WEEKLY).append(";").append("INTERVAL=2");
			} else {
				// normal supported recurrence
				sb.append(recurrence);
			}
			
			if(recurrenceEnd != null) {
				java.util.Calendar recurEndCal = java.util.Calendar.getInstance();
				recurEndCal.setTimeZone(tz);
				recurEndCal.setTime(recurrenceEnd);
				recurEndCal = CalendarUtils.getEndOfDay(recurEndCal);
				
				long recTime = recurEndCal.getTimeInMillis() - tz.getOffset(recurEndCal.getTimeInMillis());
				DateTime recurEndDT = new DateTime(recTime);
				if(tz != null) {
					recurEndDT.setTimeZone(tz);
				}
				sb.append(";").append(KalendarEvent.UNTIL).append("=").append(recurEndDT.toString());
			}
			
			try {
				Recur recur = new Recur(sb.toString());
				RRule rrule = new RRule(recur);
				return rrule.getValue();
			} catch (ParseException e) {
				log.error("cannot create recurrence rule: {}", recurrence, e);
			}
		}
		
		return null;
	}
}
