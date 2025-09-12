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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.UUID;

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
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.crypto.PasswordGenerator;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.user.UserManager;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.CalendarDateFormat;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.ParameterList;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.PropertyList;
import net.fortuna.ical4j.model.Recur;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.component.VTimeZone;
import net.fortuna.ical4j.model.parameter.Value;
import net.fortuna.ical4j.model.property.Clazz;
import net.fortuna.ical4j.model.property.Color;
import net.fortuna.ical4j.model.property.Contact;
import net.fortuna.ical4j.model.property.Created;
import net.fortuna.ical4j.model.property.DateListProperty;
import net.fortuna.ical4j.model.property.Description;
import net.fortuna.ical4j.model.property.DtEnd;
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.RecurrenceId;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.XProperty;
import net.fortuna.ical4j.model.property.immutable.ImmutableCalScale;
import net.fortuna.ical4j.model.property.immutable.ImmutableVersion;

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
		for (Iterator<CalendarComponent> iter = calendar.getComponents().iterator(); iter.hasNext();) {
			CalendarComponent comp = iter.next();
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
		try(InputStream fIn = new FileInputStream(calendarFile))  {
			return CalendarUtils.buildCalendar(fIn);
		} catch (FileNotFoundException fne) {
			throw new OLATRuntimeException("Not found: " + calendarFile, fne);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file: " + calendarFile, e);
		}
	}

	@Override
	public Kalendar buildKalendarFrom(InputStream in, String calType, String calId) {
		Kalendar kalendar = null;
		
		try {
			Calendar calendar = CalendarUtils.buildCalendar(in);
			kalendar = createKalendar(calType, calId, calendar);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file.", e);
		}
		return kalendar;
	}
	
	@Override
	public boolean synchronizeCalendarFrom(InputStream in, String source, Kalendar targetCalendar) {
		try {
			Calendar inCalendar = CalendarUtils.buildCalendar(in);
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
		long start = System.nanoTime();
		String token = PasswordGenerator.generateAlphaNumericToken(8);
		log.info("Aggregated calendar random token takes {} ms", CodeHelper.nanoToMilliTime(start));
		Kalendar calendar = new Kalendar(calendarId.toString(), calendarType);
		return calendarUserConfigDao.createCalendarUserConfiguration(calendar, identity,
				token, true, true);
	}

	@Override
	public CalendarUserConfiguration createCalendarConfig(Identity identity, Kalendar calendar) {
		long start = System.nanoTime();
		String token = PasswordGenerator.generateAlphaNumericToken(8);
		log.info("Calendar random token takes {} ms", CodeHelper.nanoToMilliTime(start));
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
		Calendar calendar = new Calendar().withDefaults()
			.withProdId("-//Ben Fortuna//iCal4j 4.1//EN")
			.withProperty(ImmutableVersion.VERSION_2_0)
			.withProperty(ImmutableCalScale.GREGORIAN)
			.getFluentTarget();
		
		List<VEvent> vEvents =  kalendar.getEvents().stream()
				.map(this::getVEvent).toList();
		calendar.setComponentList(new ComponentList<>(vEvents));
		return calendar;
	}
	
	@Override
	public KalendarEvent createKalendarEventRecurringOccurence(KalendarRecurEvent recurEvent) {
		KalendarEvent rootEvent = recurEvent.getCalendar().getEvent(recurEvent.getID(), null);
		VEvent vEvent = getVEvent(recurEvent);
		
		List<Property> vEventProperties = new ArrayList<>(vEvent.getProperties());
		for(Iterator<Property> objIt=vEventProperties.iterator(); objIt.hasNext(); ) {
			Property property = objIt.next();
			if(property instanceof RRule || property instanceof ExDate) {
				objIt.remove();
			}
		}
		
		
		try {
			Kalendar calendar = recurEvent.getCalendar();
			ZonedDateTime startDate = recurEvent.getOccurenceDate();
			String startString = CalendarUtils.formatRecurrenceDate(startDate, rootEvent.isAllDayEvent());
			RecurrenceId<Temporal> recurId;
			if(rootEvent.isAllDayEvent()) {
				recurId = new RecurrenceId<>();
				recurId.setDate(startDate);
			} else {
				recurId = new RecurrenceId<>(startString);
			}
			vEventProperties.add(recurId);
			vEvent.setPropertyList(new PropertyList(vEventProperties));
			KalendarEvent kEvent = getKalendarEvent(vEvent);
			kEvent.setKalendar(calendar);
			return kEvent;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private VEvent getVEvent(KalendarEvent kEvent) {
		VEvent vEvent;
		if (!kEvent.isAllDayEvent()) {
			// regular VEvent
			ZonedDateTime dtBegin = kEvent.getBegin();
			ZoneId tz = calendarModule.getDefaultZoneId();
			if(tz != null) {
				dtBegin = dtBegin.withZoneSameInstant(tz);
			}

			ZonedDateTime kEventEnd = kEvent.getEnd();
			if(kEventEnd == null) {
				vEvent = new VEvent(dtBegin, kEvent.getSubject());
			} else {
				ZonedDateTime dtEnd = kEventEnd;
				if(tz != null) {
					dtEnd = dtEnd.withZoneSameInstant(tz);
				}
				vEvent = new VEvent(dtBegin, dtEnd, kEvent.getSubject());
			}
		} else {
			// AllDay VEvent
			ZonedDateTime dtBegin = kEvent.getBegin();
			// adjust end date: ICal end dates for all day events are on the next day
			ZonedDateTime adjustedEndDate = kEvent.getEnd().plus((1000 * 60 * 60 * 24), ChronoUnit.MILLIS);// .getTime() + (1000 * 60 * 60 * 24));
			vEvent = new VEvent(dtBegin, adjustedEndDate, kEvent.getSubject());
		}

		List<Property> vEventProperties = new ArrayList<>(vEvent.getProperties());
		if(kEvent.getCreated() > 0) {
			Created created = new Created(new Date(kEvent.getCreated()).toInstant());
			vEventProperties.add(created);
		}

		if( (kEvent.getCreatedBy() != null) && !kEvent.getCreatedBy().trim().isEmpty()) {
			Contact contact = new Contact();
			contact.setValue(kEvent.getCreatedBy());
			vEventProperties.add(contact);
		}

		if(kEvent.getLastModified() > 0) {
			LastModified lastMod = new LastModified(new Date(kEvent.getLastModified()).toInstant());
			vEventProperties.add(lastMod);
		}

		// Uid
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
		
		if (kEvent.getColor() != null) {
			Color color = new Color();
			color.setValue(kEvent.getColor());
			vEventProperties.add(color);
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
				RecurrenceId<Temporal> recurId;
				// VALUE=DATE recurrence id need to be specially saved
				if(recurenceId.length() < 9) {
					ParameterList pList = new ParameterList(List.of(new Value("DATE")));
					recurId = new RecurrenceId<>(pList, recurenceId);
				} else {
					recurId = new RecurrenceId<>(recurenceId);
				}
				
				vEventProperties.add(recurId);
			} catch (Exception e) {
				log.error("cannot create recurrence ID: {}", recurenceId, e);
			}
		}
		
		// recurrence
		String recurrence = kEvent.getRecurrenceRule();
		if(recurrence != null && !recurrence.equals("")) {
			try {
				Recur<Temporal> recur = new Recur<>(recurrence, true);
				Temporal until = recur.getUntil();
				if(until instanceof LocalDate date) {
					recur.setUntil(LocalDateTime.of(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59));
				}
				RRule<Temporal> rrule = new RRule<>(recur);
				vEventProperties.add(rrule);
			} catch (Exception e) {
				log.error("cannot create recurrence rule: {}", recurrence, e);
			}
		}
		
		// recurrence exclusions
		String recurrenceExc = kEvent.getRecurrenceExc();
		if(recurrenceExc != null && !recurrenceExc.equals("")) {
			try {
				vEventProperties.add(new ExDate<>(recurrenceExc));
			} catch (Exception e) {
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
		
		PropertyList propertyList = new PropertyList(vEventProperties);
		return new VEvent(propertyList);
	}
	
	@Override
	public KalendarEvent cloneKalendarEvent(KalendarEvent event) {
		VEvent ve = getVEvent(event);
		return getKalendarEvent(ve);
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
		ZonedDateTime start = CalendarUtils.convertDateProperty(event.getDateTimeStart(), calendarModule.getDefaultZoneId());
		Duration dur = event.getDuration();
		// end
		DtEnd<Temporal> dtEnd = null;
		if (dur != null) {
			dtEnd = event.getDateTimeEnd();
		} else if(event.getEndDate(false).isPresent()) { 
			dtEnd = event.getEndDate(false).orElse(null);
		}
		ZonedDateTime end = dtEnd == null ? null : CalendarUtils.convertDateProperty(dtEnd, calendarModule.getDefaultZoneId());

		// check all day event first
		boolean isAllDay = false;
		
		Property allDayProperty = event.getProperty(Property.DTSTART).orElse(null);
		boolean dateParameter = allDayProperty != null && allDayProperty.getParameter(Value.DATE.getName()).isPresent();
		if (dateParameter) {
			isAllDay = true;
			
			//Make sure the time of the dates are 00:00 localtime because DATE fields in iCal are GMT 00:00 
			//Note that start date and end date can have different offset because of daylight saving switch
			//java.util.TimeZone timezone = java.util.GregorianCalendar.getInstance().getTimeZone();
			//start = new Date(start.getTime() - timezone.getOffset(start.getTime()));
			//end   = new Date(end.getTime() - timezone.getOffset(end.getTime()));
			
			// adjust end date: ICal sets end dates to the next day
			end = end.minus((1000 * 60 * 60 * 24), ChronoUnit.MILLIS);
		} else if(start != null && end != null && ChronoUnit.MILLIS.between(start, end) == (24 * 60 * 60 * 1000)) {
			//check that start has no hour, no minute and no second
			isAllDay = start.getHour() == 0 && start.getMinute() == 0
					&& start.getSecond() == 0 && start.get(ChronoField.MILLI_OF_SECOND) == 0;
			// adjust end date: ICal sets end dates to the next day
			end = end.minus((1000 * 60 * 60 * 24), ChronoUnit.MILLIS);
		}
		
		Uid eventuid = event.getUid().orElse(null);
		String uid;
		if (eventuid != null) {
			uid = eventuid.getValue();
		} else {
			uid = CodeHelper.getGlobalForeverUniqueID();
		}
		RecurrenceId<Temporal> eventRecurenceId = event.getRecurrenceId();
		String recurrenceId = null;
		if(eventRecurenceId != null) {
			try {
				recurrenceId = eventRecurenceId.getValue();
			} catch (Exception e) {
				log.warn("Non readable recurence ID", e);
			}
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
			calEvent.setCreated(created.getDate().toEpochMilli());
		}
		// created/last modified
		Contact contact = event.getContact();
		if (contact != null) {
			calEvent.setCreatedBy(contact.getValue());
		}
		
		LastModified lastModified = event.getLastModified();
		if (lastModified != null) {
			calEvent.setLastModified(lastModified.getDate().getEpochSecond());
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
		
		Property colorProperty = event.getProperty(Color.PROPERTY_NAME).orElse(null);
		if (colorProperty != null) {
			calEvent.setColor(colorProperty.getValue());
		}
		
		// links if any
		List<Property> linkProperties = event.getProperties(ICAL_X_OLAT_LINK);
		List<KalendarEventLink> kalendarEventLinks = new ArrayList<>();
		for (Iterator<Property> iter = linkProperties.iterator(); iter.hasNext();) {
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
		
		Property comment = event.getProperty(ICAL_X_OLAT_COMMENT).orElse(null);
		if (comment != null)
			calEvent.setComment(comment.getValue());
		
		Property numParticipants = event.getProperty(ICAL_X_OLAT_NUMPARTICIPANTS).orElse(null);
		if (numParticipants != null)
			calEvent.setNumParticipants(Integer.parseInt(numParticipants.getValue()));
		
		Property participants = event.getProperty(ICAL_X_OLAT_PARTICIPANTS).orElse(null);
		if (participants != null) {
			StringTokenizer strTok = new StringTokenizer(participants.getValue(), "§", false);
			String[] parts = new String[strTok.countTokens()];
			for ( int i = 0; strTok.hasMoreTokens(); i++ ) {
				parts[i] = strTok.nextToken();
			}
			calEvent.setParticipants(parts);
		}
		
		Property sourceNodId = event.getProperty(ICAL_X_OLAT_SOURCENODEID).orElse(null);
		if (sourceNodId != null) {
			calEvent.setSourceNodeId(sourceNodId.getValue());
		}
		
		//managed properties
		Property managed = event.getProperty(ICAL_X_OLAT_MANAGED).orElse(null);
		if(managed != null) {
			String value = managed.getValue();
			if("true".equals(value)) {
				value = "all";
			}
			CalendarManagedFlag[] values = CalendarManagedFlag.toEnum(value);
			calEvent.setManagedFlags(values);
		}
		Property externalId = event.getProperty(ICAL_X_OLAT_EXTERNAL_ID).orElse(null);
		if(externalId != null) {
			calEvent.setExternalId(externalId.getValue());
		}
		Property externalSource = event.getProperty(ICAL_X_OLAT_EXTERNAL_SOURCE).orElse(null);
		if(externalSource != null) {
			calEvent.setExternalSource(externalSource.getValue());
		}
		
		// recurrence
		Property recurence = event.getProperty(ICAL_RRULE).orElse(null);
		if (recurence instanceof RRule<?> rrule) {
			String recurenceValue = recurence.getValue();
			if(rrule.getRecur().getUntil() instanceof LocalDate localDate
					&& rrule.getRecur().getCount() == -1) {
				ZonedDateTime occurenceDate = CalendarUtils.convertTemporal(localDate, calendarModule.getDefaultZoneId());
				Recur<Temporal> recur = new Recur.Builder<>(new Recur<>(recurenceValue))
						.until(occurenceDate)
						.count(-1)
						.build();
				calEvent.setRecurrenceRule(recur.toString());
			} else {
				calEvent.setRecurrenceRule(recurence.getValue());
			}
		}

		// recurrence exclusions
		Property exDate = event.getProperty(ICAL_EXDATE).orElse(null);
		if (exDate != null) {
			calEvent.setRecurrenceExc(exDate.getValue());
		}
		
		// video stream
		Property liveStreamUrl = event.getProperty(ICAL_X_OLAT_VIDEO_STREAM_URL).orElse(null);
		if(liveStreamUrl != null) {
			calEvent.setLiveStreamUrl(liveStreamUrl.getValue());
		}
		
		Property liveStreamUrlTemplateKey = event.getProperty(ICAL_X_OLAT_VIDEO_STREAM_URL_TEMPLATE_KEY).orElse(null);
		if (liveStreamUrlTemplateKey != null)
			calEvent.setLiveStreamUrlTemplateKey(Long.parseLong(liveStreamUrlTemplateKey.getValue()));
		
		return calEvent;
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
					if(rootEvent != null && kalendarEvent instanceof KalendarRecurEvent krEvent) {
						ZonedDateTime recurrenceDate = krEvent.getOccurenceDate();
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
			ZonedDateTime occurenceDate = kalendarEvent.getBegin();
	
			Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
			KalendarEvent rootEvent = loadedCal.getEvent(kalendarEvent.getID(), null);
			rootEvent.addRecurrenceExc(kalendarEvent.getBegin());
			
			for(KalendarEvent kEvent:loadedCal.getEvents()) {
				if(uid.equals(kEvent.getID())
						&& kEvent.getOccurenceDate() != null
						&& occurenceDate.isEqual(kEvent.getOccurenceDate())) {
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
				ZonedDateTime occurenceDateTime = kalendarEvent.getOccurenceDate();

				Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
				KalendarEvent rootEvent = loadedCal.getEvent(kalendarEvent.getID(), null);
				String rRule = rootEvent.getRecurrenceRule();
				
				//iCal4j 3: RRULE:FREQ=DAILY;UNTIL=20250306;COUNT=-1
				ZonedDateTime occurenceDate = occurenceDateTime.truncatedTo(ChronoUnit.DAYS);
				Recur<Temporal> recur = new Recur.Builder<>(new Recur<>(rRule))
					.until(occurenceDate)
					.count(-1)
					.build();
				RRule<Temporal> rrule = new RRule<>(recur);
				rootEvent.setRecurrenceRule(rrule.getValue());
				
				for(KalendarEvent kEvent:loadedCal.getEvents()) {
					if(uid.equals(kEvent.getID())
							&& StringHelper.containsNonWhitespace(kEvent.getRecurrenceID())
							&& occurenceDateTime.isBefore(kEvent.getBegin())) {
						loadedCal.removeEvent(kEvent);
					}
				}
				
				successfullyPersist = persistCalendar(loadedCal);
			} catch (Exception e) {
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
			ZonedDateTime oldBegin = kalendarEvent.getImmutableBegin();
			ZonedDateTime oldEnd = kalendarEvent.getImmutableEnd();
			
			KalendarEvent originalEvent = reloadedCal.getEvent(kalendarEvent.getID(), null);

			ZonedDateTime newBegin = kalendarEvent.getBegin();
			ZonedDateTime newEnd = kalendarEvent.getEnd();
			long beginDiff = ChronoUnit.MILLIS.between(oldBegin, newBegin);
			long endDiff = ChronoUnit.MILLIS.between(oldEnd, newEnd);

			kalendarEvent.setBegin(originalEvent.getBegin().plus(beginDiff, ChronoUnit.MILLIS));
			kalendarEvent.setEnd(originalEvent.getEnd().plus(endDiff, ChronoUnit.MILLIS));

			List<KalendarEvent> exEvents = new ArrayList<>();
			List<KalendarEvent> allEvents = reloadedCal.getEvents();
			for(KalendarEvent event:allEvents) {
				if(event.getID().equals(kalendarEvent.getID()) && StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
					exEvents.add(event);
				}
			}
			
			if(!exEvents.isEmpty()) {
				for(KalendarEvent exEvent:exEvents) {
					try {
						reloadedCal.removeEvent(exEvent);
						String recurrenceId = exEvent.getRecurrenceID();
						
						RecurrenceId<Temporal> recurId = new RecurrenceId<>(recurrenceId);
						ZonedDateTime currentRecurrence = CalendarUtils
								.convertTemporal(recurId.getDate(), calendarModule.getDefaultZoneId());
						ZonedDateTime newRecurrenceDate = currentRecurrence.plus(beginDiff, ChronoUnit.MILLIS);
						
						boolean allDay = kalendarEvent.isAllDayEvent();
						RecurrenceId<Temporal> newRecurId;
						if(allDay) {
							newRecurId = new RecurrenceId<>();
							newRecurId.setDate(newRecurrenceDate);
						} else {
							String startString = CalendarUtils.formatRecurrenceDate(newRecurrenceDate, false);
							newRecurId = new RecurrenceId<>(startString);
						}
						exEvent.setRecurrenceID(newRecurId.getValue());
						reloadedCal.addEvent(exEvent);
					} catch (Exception e) {
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
	public List<KalendarEvent> getEvents(Kalendar calendar, ZonedDateTime from, ZonedDateTime to, boolean privateEventsVisible) {
		List<KalendarEvent> allEvents = calendar.getEvents();
		List<KalendarEvent> events = new ArrayList<>(128);
		
		// first pass, collect edited recurring events
		Map<String,List<ZonedDateTime>> idToRecurringStartDateEvents = new HashMap<>();
		for(KalendarEvent event:allEvents) {
			if(StringHelper.containsNonWhitespace(event.getRecurrenceID()) && isInRange(from, to, event)) {
				VEvent vEvent = getVEvent(event);
				RecurrenceId<Temporal> recurrenceId = vEvent.getRecurrenceId();
				ZonedDateTime recurenceIdDate = CalendarUtils.convertTemporal(recurrenceId.getDate(), calendarModule.getDefaultZoneId());
				idToRecurringStartDateEvents
					.computeIfAbsent(event.getID(), id -> new ArrayList<>())
					.add(recurenceIdDate);
			}
		}
		
		Map<String, List<KalendarRecurEvent>> idToRecurringEvents = new HashMap<>();
		// second pass, ignore events with recurrenceId
		for(KalendarEvent event:allEvents) {
			if(!privateEventsVisible && event.getClassification() == KalendarEvent.CLASS_PRIVATE) {
				continue;
			}
			if(StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
				continue;
			}

			if (StringHelper.containsNonWhitespace(event.getRecurrenceRule())) {
				ZoneId tz = calendarModule.getDefaultZoneId();
				List<ZonedDateTime> recurringStartDateEvents = idToRecurringStartDateEvents.get(event.getID());
				List<KalendarRecurEvent> recurringEvents = getRecurringEventsInPeriod(event, from, to, recurringStartDateEvents, tz);
				if(!recurringEvents.isEmpty()) {
					idToRecurringEvents.put(event.getID(), recurringEvents);
					for (KalendarRecurEvent recurEvent:recurringEvents) {
						events.add(recurEvent);
					}
				}
			} else if(isInRange(from, to, event)) {
				events.add(event);
			}
		}
		
		// third pass, process events with recurrenceId
		for(KalendarEvent event:allEvents) {
			if(!StringHelper.containsNonWhitespace(event.getRecurrenceID())) {
				continue;
			}

			String id = event.getID();
			if(idToRecurringEvents.containsKey(id)) {
				VEvent vEvent = getVEvent(event);
				RecurrenceId<Temporal> recurrenceId = vEvent.getRecurrenceId();
				ZonedDateTime startDate = CalendarUtils.convertTemporal(recurrenceId.getDate(), calendarModule.getDefaultZoneId());
				List<KalendarRecurEvent> recurringEvents = idToRecurringEvents.get(id);
				for(KalendarRecurEvent recurEvent:recurringEvents) {
					ZonedDateTime beginDate = recurEvent.getBegin();
					if(startDate.equals(beginDate)) {
						recurEvent.setRecurrenceEvent(event);
					}
				}
			}
		}
		
		return events;
	}

	private final boolean isInRange(ZonedDateTime from, ZonedDateTime to, KalendarEvent event) {
		ZonedDateTime begin = event.getBegin();
		ZonedDateTime end = CalendarUtils.endOf(event);

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
	
	private ZonedDateTime calculateRecurringStartPeriod(ZonedDateTime periodStart, List<ZonedDateTime> recurringIdDates) {
		if(recurringIdDates != null && !recurringIdDates.isEmpty()) {
			for(ZonedDateTime recurringIdDate:recurringIdDates) {
				if(recurringIdDate.isBefore(periodStart)) {
					// make sure the date is included
					periodStart = recurringIdDate.minusDays(2) ;
				}
			}
		}
		return periodStart;
	}
	
	private ZonedDateTime calculateRecurringEndPeriod(ZonedDateTime periodEnd, List<ZonedDateTime> recurringIdDates) {
		if(recurringIdDates != null && !recurringIdDates.isEmpty()) {
			for(ZonedDateTime recurringIdDate:recurringIdDates) {
				if(recurringIdDate.isAfter(periodEnd)) {
					// make sure the date is included
					periodEnd = recurringIdDate.plusDays(2);
				}
			}
		}
		return periodEnd;
	}
	
	private List<ZonedDateTime> getExDates(VEvent vEvent) {
		List<Property> exDateProps = vEvent.getProperties(Property.EXDATE);
		List<ZonedDateTime> exDates = new ArrayList<>();
		for(Property exDateProp:exDateProps) {
			if(exDateProp instanceof DateListProperty dateList) {
				@SuppressWarnings("unchecked")
				List<Temporal> dates = dateList.getDates();
				for(Temporal date:dates) {
					exDates.add(CalendarUtils.convertTemporal(date, calendarModule.getDefaultZoneId()));
				}
			}
		}
		return exDates;
	}
	
	private boolean isEx(Period<Temporal> period, List<ZonedDateTime> exDates) {
		ZonedDateTime pStart = CalendarUtils.convertTemporal(period.getStart(), calendarModule.getDefaultZoneId());
		for(ZonedDateTime exDate:exDates) {
			//ical4j 3 checks for DATE too
			if(period.includes(exDate) || DateUtils.isSameDay(pStart, exDate)) {
				return true;
			}
		}
		return false;
	}
	
	private final List<KalendarRecurEvent> getRecurringEventsInPeriod(KalendarEvent kEvent,
			ZonedDateTime periodStart, ZonedDateTime periodEnd, List<ZonedDateTime> recurringIdDates, ZoneId userTz) {
		VEvent vEvent = getVEvent(kEvent);
		if(vEvent.getEndDate(false).isEmpty()) {
			return Collections.emptyList();
		}
		
		periodStart = calculateRecurringStartPeriod(periodStart, recurringIdDates);
		periodEnd = calculateRecurringEndPeriod(periodEnd, recurringIdDates);

		//calculate the events in the specified period
        Period<ZonedDateTime> recurringPeriod = new Period<>(periodStart, periodEnd);
		Set<Period<Temporal>> periodList = vEvent.calculateRecurrenceSet(recurringPeriod);
		List<KalendarRecurEvent> recurringEvents = new ArrayList<>();
		List<ZonedDateTime> exDates = getExDates(vEvent);
		
		for(Period<Temporal> period : periodList) {
			ZonedDateTime date = ZonedDateTime.from(period.getStart());
			if(isEx(period, exDates)) {
				continue;
			}
			
			ZonedDateTime recurStartCal;
			if(userTz == null) {
				recurStartCal = date;
			} else {
				recurStartCal = date.withZoneSameInstant(userTz);
			}
			long duration = ChronoUnit.MILLIS.between(kEvent.getBegin(), kEvent.getEnd());

			ZonedDateTime beginCal = recurStartCal;
			ZonedDateTime endCal = recurStartCal.plus(duration, ChronoUnit.MILLIS);

			boolean original = false;
			if(kEvent.getBegin().isEqual(beginCal)) {
				original = true; //prevent doubled events
			}

			ZonedDateTime recurrenceEnd = getRecurrenceEndDate(kEvent.getRecurrenceRule());
			if(kEvent.isAllDayEvent() && recurrenceEnd != null && recurStartCal.isAfter(recurrenceEnd)) {
				continue; //workaround for ical4j-bug in all day events
			}
		
			KalendarRecurEvent recurEvent = new KalendarRecurEvent(kEvent.getID(), original, kEvent.getSubject(), beginCal, endCal);
			recurEvent.setOccurenceDate(beginCal);
			recurEvent.setSourceEvent(kEvent);
			recurringEvents.add(recurEvent);
		}
		return recurringEvents;
	}
	
	/**
	 * 
	 * @param rule
	 * @return date of recurrence end
	 */
	@Override
	public ZonedDateTime getRecurrenceEndDate(String rule) {
		if (StringHelper.containsNonWhitespace(rule)) {
			try {
				Recur<Temporal> recur = new Recur<>(rule);
				return CalendarUtils.convertTemporal(recur.getUntil(), calendarModule.getDefaultZoneId());
			} catch (Exception e) {
				log.error("Cannot restore recurrence rule: {}", rule, e);
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
				ZonedDateTime recurrenceEndDateTime = DateUtils.toZonedDateTime(recurrenceEnd, calendarModule.getDefaultZoneId());
				recurrenceEndDateTime = DateUtils.getEndOfDay(recurrenceEndDateTime)
						.withSecond(0)
						.withZoneSameInstant(ZoneId.of("UTC"));
				String recurrenceEndStr = CalendarDateFormat.FLOATING_DATE_TIME_FORMAT.format(recurrenceEndDateTime);
				sb.append(";").append(KalendarEvent.UNTIL).append("=").append(recurrenceEndStr).append("Z");
			}
			
			try {
				Recur<Temporal> recur = new Recur<>(sb.toString());
				RRule<Temporal> rrule = new RRule<>(recur);
				return rrule.getValue();
			} catch (Exception e) {
				log.error("cannot create recurrence rule: {}", recurrence, e);
			}
		}
		
		return null;
	}
}
