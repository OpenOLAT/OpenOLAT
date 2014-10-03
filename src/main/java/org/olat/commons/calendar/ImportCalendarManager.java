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
* <p>
*/ 

package org.olat.commons.calendar;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarComparator;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;


/**
 * Description:<BR>
 * Constants and helper methods for the OLAT iCal feeds
 * 
 * <P>
 * Initial Date:  July 22, 2008
 *
 * @author Udit Sajjanhar
 */
public class ImportCalendarManager extends BasicManager {
	private static final OLog log = Tracing.createLoggerFor(ImportCalendarManager.class);

	public static String PROP_CATEGORY	 = "Imported-Calendar";
	public static String PROP_CATEGORY_IMP	 = "Imported-Calendar-To";
	
	
	public static boolean importCalendarIn(Kalendar cal, String importUrl) {
		try {
			String calendarContent = getContentFromUrl(importUrl);
			CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
			Kalendar importedCal = calManager.buildKalendarFrom(calendarContent, cal.getType(), cal.getCalendarID());
			boolean imported = calManager.updateCalendar(cal, importedCal);
			if(imported) {
				PropertyManager pm = PropertyManager.getInstance();
				List<Property> p = pm.findProperties(null, null, cal.getType(), 0l, PROP_CATEGORY_IMP, cal.getCalendarID());
				if(p.isEmpty()) {
					long timestamp = System.currentTimeMillis();
					OLATResourceable ores = OresHelper.createOLATResourceableInstance(cal.getType(), 0l);
					Property prop = pm.createPropertyInstance(null, null, ores, PROP_CATEGORY_IMP, cal.getCalendarID(), null, timestamp, null, importUrl);
					pm.saveProperty(prop);
				} else {
					Property prop = p.get(0);
					String textVal = prop.getTextValue();
					if(!textVal.contains(importUrl)) {
						prop.setTextValue(textVal + "|" + importUrl);
					}
				}
			}
			return imported;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	/**
	 * Method used by the cron job
	 * @return
	 */
	public static boolean updateCalendarIn() {
		List<Property> properties = loadPropertiesByImportCategory();
		log.audit("Begin to update " + properties.size() + " calendars.");
		
		int count = 0;
		for(Property property:properties) {
			String type = property.getResourceTypeName();
			String id = property.getName();
			String importUrls = property.getTextValue();
			if(!StringHelper.containsNonWhitespace(importUrls) || !StringHelper.containsNonWhitespace(type) || !StringHelper.containsNonWhitespace(id)) {
				continue;
			}
			
			for(StringTokenizer tokenizer = new StringTokenizer(importUrls, "|"); tokenizer.hasMoreTokens(); ) {
				String importUrl = tokenizer.nextToken();
				CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
				Kalendar cal = calManager.getCalendar(type + "dru", id);
				if(importCalendarIn(cal, importUrl)) {
					log.audit("Updated successfully calendar: " + type + " / " + id);
				} else {
					log.audit("Failed to update calendar: " + type + " / " + id);
				}
				
				if(count++ % 20 == 0) {
					DBFactory.getInstance().intermediateCommit();
				}
			}
		}
		return false;
	}
	
	private static List<Property> loadPropertiesByImportCategory() {
		StringBuilder sb = new StringBuilder();
		sb.append("select p from ").append(Property.class.getName()).append(" as p where ")
		  .append(" p.category = :category and p.resourceTypeId = :restypeid");

		return DBFactory.getInstance().getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setParameter("category", PROP_CATEGORY_IMP)
				.setParameter("restypeid", new Long(0))
				.getResultList();		
	}
	
	public static boolean importCalendarIn(KalendarRenderWrapper calenderWrapper, InputStream in) {
		try {
			String calendarContent = getContentFromStream(in);
			Kalendar cal = calenderWrapper.getKalendar();
			CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
			Kalendar importedCal = calManager.buildKalendarFrom(calendarContent, cal.getType(), cal.getCalendarID());
			return calManager.updateCalendar(cal, importedCal);
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	/**
	 * Save the imported calendar
	 * 	1. make an entry in the database
	 *  2. save the calendar file  
	 * @param calendarName
	 * @param ureq
	 * @param importUrl can be null when import from file
	 * @return
	 */
	public static void persistCalendar(String calendarName, UserRequest ureq, String importUrl) {
		// move the temporary file to the permanent file
		String tempCalendarID = getTempCalendarIDForUpload(ureq);
		String importedCalendarID = getImportedCalendarID(ureq, calendarName);
		String importedCalendarType = getImportedCalendarType();
		
		CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
		File oldCalendarFile = calManager.getCalendarFile(importedCalendarType, tempCalendarID);
		oldCalendarFile.renameTo(calManager.getCalendarFile(importedCalendarType, importedCalendarID));
		
		// make the entry in the database
		long timestamp = System.currentTimeMillis();
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.createUserPropertyInstance(ureq.getIdentity(), PROP_CATEGORY, sanitize(calendarName), null, timestamp, importUrl, null);
		pm.saveProperty(p);
	}
	
	/**
	 * Delete an imported calendar
	 *  1. remove the entry from the database
	 *  2. delete the calendar file
	 * @param calendarID
	 * @param ureq
	 * @return
	 */
	public static void deleteCalendar(String calendarID, UserRequest ureq) {
		String calendarName = getImportedCalendarNameFromID(ureq.getIdentity(), calendarID);
		// remove the entry from the database
		PropertyManager pm = PropertyManager.getInstance();
		Property p = pm.findUserProperty(ureq.getIdentity(), PROP_CATEGORY, calendarName);
		pm.deleteProperty(p);

		// delete the calendar file
		CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
		String importedCalendarID = getImportedCalendarID(ureq, calendarName);
		String importedCalendarType = getImportedCalendarType();
		calManager.deleteCalendar(importedCalendarType, importedCalendarID);
	}
	
	/**
	 * Get imported calendars for a user.
	 * @param ureq
	 * @return
	 */
	public static List<KalendarRenderWrapper> getImportedCalendarsForIdentity(UserRequest ureq) {
		// initialize the calendars list
		List<KalendarRenderWrapper> calendars = new ArrayList<KalendarRenderWrapper>();
		CalendarModule calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		if(calendarModule.isEnabled() && calendarModule.isEnablePersonalCalendar()) {
			// read all the entries from the database
			PropertyManager pm = PropertyManager.getInstance();
			List<Property> properties = pm.listProperties(ureq.getIdentity(), null, null, PROP_CATEGORY, null);
			
			// return the list of calendar objects
			Iterator<Property> propertyIter = properties.iterator();
			CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
			while (propertyIter.hasNext()) {
				Property calendarProperty = propertyIter.next();
				String calendarName = calendarProperty.getName();
				KalendarRenderWrapper calendarWrapper = calManager.getImportedCalendar(ureq.getIdentity(), calendarName);
				calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
				calendarWrapper.setImported(true);
				KalendarConfig importedKalendarConfig = calManager.findKalendarConfigForIdentity(
						calendarWrapper.getKalendar(), ureq);
				if (importedKalendarConfig != null) {
					calendarWrapper.getKalendarConfig().setCss(importedKalendarConfig.getCss());
					calendarWrapper.getKalendarConfig().setVis(importedKalendarConfig.isVis());
				}
				calendars.add(calendarWrapper);
			}
			Collections.sort(calendars, KalendarComparator.getInstance());
		}
		return calendars;
	}

	/**
	 * Reload imported calendars from URL and persist calendars.
	 * @param ureq
	 */
	public static void reloadUrlImportedCalendars(UserRequest ureq) {
		// read all the entries from the database
		List<Property> properties = PropertyManager.getInstance().listProperties(ureq.getIdentity(), null, null, PROP_CATEGORY, null);
		// return the list of calendar objects
		Iterator<Property> propertyIter = properties.iterator();
		while (propertyIter.hasNext()) {
			Property calendarProperty = propertyIter.next();
			String calendarName = calendarProperty.getName();
			String calendarUrl = calendarProperty.getStringValue();
			long timestampLastupdate = calendarProperty.getLongValue();
			long timestamp = System.currentTimeMillis();
			// reload only if string-property with importUrl exist and last update is older than 1 hour
			if ( (calendarProperty.getStringValue() != null) && (timestamp-timestampLastupdate > 3600000) ) {
				reloadCalendarFromUrl(calendarUrl, getImportedCalendarType(), getImportedCalendarID(ureq, calendarName));
				log.info("Calendar reload started from url=" + calendarUrl);
				calendarProperty.setLongValue(timestamp);
				PropertyManager.getInstance().updateProperty(calendarProperty);
				log.info("Calendar reloaded from url=" + calendarUrl);
			}
		}
	}   

	/**
	 * Reload calendar from url and store calendar file locally.
	 * @param importUrl
	 * @param calType
	 * @param calId
	 */
	private static void reloadCalendarFromUrl(String importUrl, String calType, String calId) {
    try {
    	String calendarContent = getContentFromUrl(importUrl);
    	CalendarManager calManager = CalendarManagerFactory.getInstance().getCalendarManager();
    	Kalendar kalendar = calManager.buildKalendarFrom(calendarContent, calType, calId);
    	calManager.persistCalendar(kalendar);
    } catch (Exception e) {
      	log.error("Could not reload calendar from url=" + importUrl, e);
    }
	}

	/**
	 * Get a temporary calendarID for upload
	 * @param ureq
	 * @return
	 */
	public static String getTempCalendarIDForUpload(UserRequest ureq) {
		return ureq.getIdentity().getName() + "_import_tmp";
	}
	
	/**
	 * Get ID of a imported calendar 
	 * @param ureq
	 * @param calendarName
	 * @return
	 */
	public static String getImportedCalendarID(UserRequest ureq, String calendarName) {
		return getImportedCalendarID(ureq.getIdentity(), calendarName);
	}
	
	/**
	 * Get ID of a imported calendar 
	 * @param identity
	 * @param calendarName
	 * @return
	 */
	public static String getImportedCalendarID(Identity identity, String calendarName) {
		return identity.getName() + "_" + sanitize(calendarName);
	}
	
	private static String getImportedCalendarNameFromID(Identity identity, String calendarID) {
		int idLength = calendarID.length();
		return calendarID.substring(identity.getName().length() + 1, idLength );
	}
	
	private static String getImportedCalendarType() {
		return CalendarManager.TYPE_USER;
	}
	
	
	private static String sanitize(String name) {
		// delete the preceding and trailing whitespaces
		name = name.trim();
		
		// replace every other character other than alphabets and numbers by underscore
		Pattern specialChars = Pattern.compile("([^a-zA-z0-9])");
		return specialChars.matcher(name).replaceAll("_").toLowerCase();
	}

	public static String getContentFromUrl(String url) throws IOException {
		InputStream in = new URL(url).openStream();
		return getContentFromStream(in);
	}
	
	public static String getContentFromStream(InputStream in) throws IOException {
		BufferedReader dis = new BufferedReader(new InputStreamReader(in));
		StringBuffer fBuf = new StringBuffer() ;
		String line;
		while ( (line = dis.readLine()) != null) {
			fBuf.append (line + "\n");
		}
		in.close ();
		return fBuf.toString();
	}
}
