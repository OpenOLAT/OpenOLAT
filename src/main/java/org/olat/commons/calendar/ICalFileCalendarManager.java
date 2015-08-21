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

package org.olat.commons.calendar;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarModifiedEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Parameter;
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
import net.fortuna.ical4j.model.property.Duration;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.LastModified;
import net.fortuna.ical4j.model.property.Location;
import net.fortuna.ical4j.model.property.ProdId;
import net.fortuna.ical4j.model.property.RRule;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.model.property.Uid;
import net.fortuna.ical4j.model.property.Url;
import net.fortuna.ical4j.model.property.Version;
import net.fortuna.ical4j.model.property.XProperty;

public class ICalFileCalendarManager implements CalendarManager {

	private static final OLog log = Tracing.createLoggerFor(ICalFileCalendarManager.class);

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

	protected ICalFileCalendarManager(File fStorageBase) {
		this.fStorageBase = fStorageBase;
		if (!fStorageBase.exists()) {
			if (!fStorageBase.mkdirs())
				throw new OLATRuntimeException("Error creating calendar base directory at: " + fStorageBase.getAbsolutePath(), null);
		}
		createCalendarFileDirectories();
		// set parser to relax (needed for allday events
		// see http://sourceforge.net/forum/forum.php?thread_id=1253735&forum_id=368291
		//made in module System.setProperty("ical4j.unfolding.relaxed", "true");
		// initialize tiemzone
		tz = CoreSpringFactory.getImpl(CalendarModule.class).getDefaultTimeZone();
		calendarCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getCache(CalendarManager.class.getSimpleName(), "calendar");
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
		Kalendar kalendar = createKalendar(type, calendarID, calendar);
		return kalendar;
	}

	private Kalendar createKalendar(String type, String calendarID, Calendar calendar) {
		Kalendar cal = new Kalendar(calendarID, type);
		for (Iterator<?> iter = calendar.getComponents().iterator(); iter.hasNext();) {
			Object comp = iter.next();
			if (comp instanceof VEvent) {
				VEvent vevent = (VEvent)comp;
				KalendarEvent calEvent = getKalendarEvent(vevent);
				cal.addEvent(calEvent);
			} else if (comp instanceof VTimeZone) {
				log.info("createKalendar: VTimeZone Component is not supported and will not be added to calender");
				log.debug("createKalendar: VTimeZone=" + comp);
			} else {
				log.warn("createKalendar: unknown Component=" + comp);
			}
		}
		return cal;
	}

  /**
   * Internal read calendar file from filesystem
   */
	@Override
	public Calendar readCalendar(String type, String calendarID) {
		if(log.isDebug()) {
			log.debug("readCalendar from file, type=" + type + "  calendarID=" + calendarID);
		}
		
		File calendarFile = getCalendarFile(type, calendarID);
    
		Calendar calendar;
		try(InputStream fIn = new FileInputStream(calendarFile);
				InputStream	in = new BufferedInputStream(fIn))  {
			
			CalendarBuilder builder = new CalendarBuilder();
			calendar = builder.build(in);
		} catch (FileNotFoundException fne) {
			throw new OLATRuntimeException("Not found: " + calendarFile, fne);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file.", e);
		}
		return calendar;
	}

	@Override
  public Kalendar buildKalendarFrom(String calendarContent, String calType, String calId) {
  	Kalendar kalendar = null;
  	BufferedReader reader = new BufferedReader(new StringReader(calendarContent));
		CalendarBuilder builder = new CalendarBuilder();
		try {
			Calendar calendar = builder.build(reader);
			kalendar = createKalendar(calType, calId, calendar);
		} catch (Exception e) {
			throw new OLATRuntimeException("Error parsing calendar file.", e);
		} finally {
			if (reader != null) {
				try {
		            reader.close();
	            } catch (IOException e) {
	            	throw new OLATRuntimeException("Could not close reader after build calendar file.", e);
	            }
			}
		}
	  return kalendar;
  }

	/**
	 * Save a calendar.
	 * This method is not thread-safe. Must be called from a synchronized block.
	 * Be sure to have the newest calendar (reload calendar in synchronized block before safe it).
	 * @param calendar
	 */
	// o_clusterOK by:cg only called by Junit-test  
	public boolean persistCalendar(Kalendar kalendar) {
		Calendar calendar = buildCalendar(kalendar);
		boolean success = writeCalendarFile(calendar,kalendar.getType(), kalendar.getCalendarID());
		calendarCache.update(getKeyFor(kalendar.getType(), kalendar.getCalendarID()), kalendar);
		return success;
	}
	
	private boolean writeCalendarFile(Calendar calendar, String calType, String calId) {
		File fKalendarFile = getCalendarFile(calType, calId);
		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(fKalendarFile, false));
			CalendarOutputter calOut = new CalendarOutputter(false);
			calOut.output(calendar, os);
		} catch (Exception e) {
			return false;
		} finally {
			FileUtils.closeSafely(os);
		}
		return true;
	}

	/**
	 * Delete calendar by type and id.
	 */
	public boolean deleteCalendar(String type, String calendarID) {
		calendarCache.remove( getKeyFor(type,calendarID) );
		File fKalendarFile = getCalendarFile(type, calendarID);
		return fKalendarFile.delete();
	}

	public File getCalendarICalFile(String type, String calendarID) {
		File fCalendarICalFile = getCalendarFile(type, calendarID);
		if (fCalendarICalFile.exists()) return fCalendarICalFile;
		else return null;
	}
	
	/**
	 * @see org.olat.calendar.CalendarManager#findKalendarConfigForIdentity(org.olat.calendar.model.Kalendar, org.olat.core.gui.UserRequest)
	 */
	public KalendarConfig findKalendarConfigForIdentity(Kalendar kalendar, UserRequest ureq) {
		Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
		if(guiPreferences == null) {
			return null;
		}
		return (KalendarConfig)guiPreferences.get(KalendarConfig.class, kalendar.getCalendarID());
	}

	/**
	 * @see org.olat.calendar.CalendarManager#saveKalendarConfigForIdentity(org.olat.calendar.model.KalendarConfig, org.olat.calendar.model.Kalendar, org.olat.core.gui.UserRequest)
	 */
	public void saveKalendarConfigForIdentity(KalendarConfig config, Kalendar kalendar, UserRequest ureq) {
		Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
		guiPreferences.putAndSave(KalendarConfig.class, kalendar.getCalendarID(), config);
	}
	
	private Calendar buildCalendar(Kalendar kalendar) {
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
	
	private VEvent getVEvent(KalendarEvent kEvent) {
		VEvent vEvent = new VEvent();
		if (!kEvent.isAllDayEvent()) {
			// regular VEvent
			DateTime dtBegin = new DateTime(kEvent.getBegin());
			if(tz != null) {
				dtBegin.setTimeZone(tz);
			}
			DateTime dtEnd = null;
			Date kEventEnd = kEvent.getEnd();
			if(kEventEnd != null) {
				dtEnd = new DateTime(kEventEnd);
				if(tz != null) {
					dtEnd.setTimeZone(tz);
				}
			}
			vEvent = new VEvent(dtBegin, dtEnd, kEvent.getSubject());
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
					} catch (URISyntaxException e) {
						log.error("Invalid URL:" + link.getURI());
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
			StringBuffer strBuf = new StringBuffer();
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
		
		if(kEvent.isManaged()) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_MANAGED, "true"));
		}
		
		if(StringHelper.containsNonWhitespace(kEvent.getExternalId())) {
			vEventProperties.add(new XProperty(ICAL_X_OLAT_EXTERNAL_ID, kEvent.getExternalId()));
		}
		
		// recurrence
		String recurrence = kEvent.getRecurrenceRule();
		if(recurrence != null && !recurrence.equals("")) {
			try {
				Recur recur = new Recur(recurrence);
				RRule rrule = new RRule(recur);
				vEventProperties.add(rrule);
			} catch (ParseException e) {
				log.error("cannot create recurrence rule: " + recurrence.toString(), e);
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
		Date start = event.getStartDate().getDate();
		Duration dur = event.getDuration();
		// end
		Date end = null;
		if (dur != null) {
			end = dur.getDuration().getTime(event.getStartDate().getDate());
		} else if(event.getEndDate() != null) { 
			end = event.getEndDate().getDate();
		}

		// check all day event first
		boolean isAllDay = false;
		Parameter dateParameter = event.getProperties().getProperty(Property.DTSTART).getParameters().getParameter(Value.DATE.getName());
		if (dateParameter != null) isAllDay = true;

		if (isAllDay) {
			//Make sure the time of the dates are 00:00 localtime because DATE fields in iCal are GMT 00:00 
			//Note that start date and end date can have different offset because of daylight saving switch
			java.util.TimeZone timezone = java.util.GregorianCalendar.getInstance().getTimeZone();
			start = new Date(start.getTime() - timezone.getOffset(start.getTime()));
			end   = new Date(end.getTime()   - timezone.getOffset(end.getTime()));
			
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
		KalendarEvent calEvent = new KalendarEvent(uid, subject, start, end);
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
		
		// location
		Location location = event.getLocation();
		if (location != null) {
			calEvent.setLocation(location.getValue());
		}
		
		// links if any
		PropertyList linkProperties = event.getProperties(ICAL_X_OLAT_LINK);
		List<KalendarEventLink> kalendarEventLinks = new ArrayList<KalendarEventLink>();
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
		
		Property managed = event.getProperty(ICAL_X_OLAT_MANAGED);
		if(managed != null) {
			calEvent.setManaged("true".equals(managed.getValue()));
		}
		
		Property externalId = event.getProperty(ICAL_X_OLAT_EXTERNAL_ID);
		if(externalId != null) {
			calEvent.setExternalId(externalId.getValue());
		}
		
		// recurrence
		if (event.getProperty(ICAL_RRULE) != null) {
			calEvent.setRecurrenceRule(event.getProperty(ICAL_RRULE).getValue());
		}

		// recurrence exclusions
		if (event.getProperty(ICAL_EXDATE) != null) {
			calEvent.setRecurrenceExc(event.getProperty(ICAL_EXDATE).getValue());
		}
		
		return calEvent;
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public boolean isRecurringInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		DateList recurDates = CalendarUtils.getRecurringsInPeriod(periodStart, periodEnd, kEvent);
		return (recurDates != null && !recurDates.isEmpty());
	}

	@Override
	public List<KalendarRecurEvent> getRecurringDatesInPeriod(Date periodStart, Date periodEnd, KalendarEvent kEvent) {
		return CalendarUtils.getRecurringDatesInPeriod(periodStart, periodEnd, kEvent, tz);
	}

	public File getCalendarFile(String type, String calendarID) {
		return new File(fStorageBase, "/" + type + "/" + calendarID + ".ics");
	}

	private void createCalendarFileDirectories() {
		File fDirectory = new File(fStorageBase, "/" + TYPE_USER);
		fDirectory.mkdirs();
		fDirectory =  new File(fStorageBase, "/" + TYPE_GROUP);
		fDirectory.mkdirs();
		fDirectory = new File(fStorageBase, "/" + TYPE_COURSE);
		fDirectory.mkdirs();
	}

	@Override
	public KalendarRenderWrapper getPersonalCalendar(Identity identity) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_USER, identity.getName());
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal);
		KalendarConfig config = new KalendarConfig(identity.getName(), KalendarRenderWrapper.CALENDAR_COLOR_BLUE, true);
		calendarWrapper.setKalendarConfig(config);
		return calendarWrapper;
	}

	@Override
	public KalendarRenderWrapper getImportedCalendar(Identity identity, String calendarName) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_USER, ImportCalendarManager.getImportedCalendarID(identity, calendarName));
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal);
		KalendarConfig config = new KalendarConfig(calendarName, KalendarRenderWrapper.CALENDAR_COLOR_BLUE, true);
		calendarWrapper.setKalendarConfig(config);
		return calendarWrapper;
	}

	@Override
	public KalendarRenderWrapper getGroupCalendar(BusinessGroup businessGroup) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_GROUP, businessGroup.getResourceableId().toString());
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal);
		KalendarConfig config = new KalendarConfig(businessGroup.getName(), KalendarRenderWrapper.CALENDAR_COLOR_ORANGE, true);
		calendarWrapper.setKalendarConfig(config);
		return calendarWrapper;
	}
	
	public KalendarRenderWrapper getCourseCalendar(ICourse course) {
		Kalendar cal = getCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal);
		KalendarConfig config = new KalendarConfig(course.getCourseTitle(), KalendarRenderWrapper.CALENDAR_COLOR_GREEN, true);
		calendarWrapper.setKalendarConfig(config);
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
		KalendarRenderWrapper calendarWrapper = new KalendarRenderWrapper(cal);
		KalendarConfig config = new KalendarConfig("To delete", KalendarRenderWrapper.CALENDAR_COLOR_GREEN, true);
		calendarWrapper.setKalendarConfig(config);
		return calendarWrapper;
	}

	public void deletePersonalCalendar(Identity identity) {
		deleteCalendar(CalendarManager.TYPE_USER, identity.getName());
	}
	
	public void deleteGroupCalendar(BusinessGroup businessGroup) {
		deleteCalendar(CalendarManager.TYPE_GROUP, businessGroup.getResourceableId().toString());
	}
	
	public void deleteCourseCalendar(ICourse course) {
		deleteCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
	}
	
	public void deleteCourseCalendar(OLATResourceable course) {
		deleteCalendar(CalendarManager.TYPE_COURSE, course.getResourceableId().toString());
	}

	/**
	 * @see org.olat.commons.calendar.CalendarManager#addEventTo(org.olat.commons.calendar.model.Kalendar, org.olat.commons.calendar.model.KalendarEvent)
	 */
	public boolean addEventTo(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean persistSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, new SyncerCallback<Boolean>() {
			public Boolean execute() {
				Kalendar loadedCal = getCalendarFromCache(cal.getType(),cal.getCalendarID());
				loadedCal.addEvent(kalendarEvent);
				boolean successfullyPersist = persistCalendar(loadedCal);
				return new Boolean(successfullyPersist);
			}
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return persistSuccessful.booleanValue();
  }

	/**
	 * @see org.olat.commons.calendar.CalendarManager#removeEventFrom(org.olat.commons.calendar.model.Kalendar, org.olat.commons.calendar.model.KalendarEvent)
	 */
	public boolean removeEventFrom(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean removeSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, new SyncerCallback<Boolean>() {
			public Boolean execute() {
				Kalendar loadedCal = getCalendarFromCache(cal.getType(),cal.getCalendarID());
				loadedCal.removeEvent(kalendarEvent);
				boolean successfullyPersist = persistCalendar(loadedCal);
				return new Boolean(successfullyPersist);
			}
		});
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return removeSuccessful.booleanValue();
    }
	
	/**
	 * @see org.olat.commons.calendar.CalendarManager#updateEventFrom(org.olat.commons.calendar.model.Kalendar, org.olat.commons.calendar.model.KalendarEvent)
	 */
	public boolean updateEventFrom(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, new SyncerCallback<Boolean>() {
			public Boolean execute() {
				return updateEventAlreadyInSync(cal, kalendarEvent);
			}
		});
		return updatedSuccessful.booleanValue();
    }
    
    /**
	 * @see org.olat.commons.calendar.CalendarManager#updateEventFrom(org.olat.commons.calendar.model.Kalendar, org.olat.commons.calendar.model.KalendarEvent)
	 */
	public boolean updateEventAlreadyInSync(final Kalendar cal, final KalendarEvent kalendarEvent) {
		OLATResourceable calOres = getOresHelperFor(cal);
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(calOres);
		Kalendar loadedCal = getCalendarFromCache(cal.getType(),cal.getCalendarID());
		loadedCal.removeEvent(kalendarEvent); // remove old event
		loadedCal.addEvent(kalendarEvent); // add changed event
		boolean successfullyPersist = persistCalendar(loadedCal);
		// inform all controller about calendar change for reload
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
		return successfullyPersist;
	}
	
	public boolean updateCalendar(final Kalendar cal, final Kalendar importedCal) {
		OLATResourceable calOres = getOresHelperFor(cal);
		Boolean updatedSuccessful = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync( calOres, new SyncerCallback<Boolean>() {
			public Boolean execute() {
				Map<String,KalendarEvent> uidToEvent = new HashMap<String,KalendarEvent>();
				for(KalendarEvent event:cal.getEvents()) {
					if(StringHelper.containsNonWhitespace(event.getID())) {
						uidToEvent.put(event.getID(), event);
					}
				}
				
				Kalendar loadedCal = getCalendarFromCache(cal.getType(), cal.getCalendarID());
				for(KalendarEvent importedEvent:importedCal.getEvents()) {
					String uid = importedEvent.getID();
					if(uidToEvent.containsKey(uid)) {
						loadedCal.removeEvent(importedEvent); // remove old event
						loadedCal.addEvent(importedEvent); // add changed event
					} else {
						loadedCal.addEvent(importedEvent);
					}
				}
				
				boolean successfullyPersist = persistCalendar(cal);
				// inform all controller about calendar change for reload
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(new KalendarModifiedEvent(cal), OresHelper.lookupType(CalendarManager.class));
				return new Boolean(successfullyPersist);
			}
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

}
