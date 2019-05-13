/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.CalendarUserConfigurationDAO;
import org.olat.commons.calendar.manager.ImportedCalendarDAO;
import org.olat.commons.calendar.manager.ImportedToCalendarDAO;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.model.ImportedCalendar;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.upgrade.model.KalendarConfig;
import org.olat.upgrade.model.UpgradePreferences;
import org.springframework.beans.factory.annotation.Autowired;

import com.thoughtworks.xstream.XStream;

/**
 * 
 * Initial date: 27.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OLATUpgrade_10_4_0 extends OLATUpgrade {

	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_10_4_0.class);
	
	private static final int BATCH_SIZE = 1000;
	private static final String CALENDAR_TOKENS = "Calendar tokens";
	private static final String CALENDAR_USER_CONFIGS = "Calendar user configurations";
	private static final String IMPORTED_CALENDARS = "Imported calendars";
	private static final String IMPORTED_CALENDARS_URL = "Imported calendars url";
	private static final String VERSION = "OLAT_10.4.0";
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		xstream.ignoreUnknownElements();
		xstream.alias("org.olat.preferences.DbPrefs", UpgradePreferences.class);
		xstream.alias("org.olat.core.util.prefs.db.DbPrefs", UpgradePreferences.class);
		xstream.alias("org.olat.commons.calendar.model.KalendarConfig", KalendarConfig.class);
	}

	private static final String KALENDAR_GUI_MARKER = "org.olat.commons.calendar.model.KalendarConfig::";

	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarManager calendarMgr;
	@Autowired
	private ImportedCalendarDAO importedCalendarDao;
	@Autowired
	private ImportedToCalendarDAO importedToCalendarDao;
	@Autowired
	private CalendarUserConfigurationDAO calendarUserConfigurationDao;
	
	public OLATUpgrade_10_4_0() {
		super();
	}

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}
		
		boolean allOk = true;
		allOk &= migrateImportedCalendars(upgradeManager, uhd);
		allOk &= migrateImportedCalendarsTo(upgradeManager, uhd);
		
		allOk &= migrateCalendarTokens(upgradeManager, uhd);
		allOk &= migrateCalendarConfigurations(upgradeManager, uhd);

		uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		if(allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_10_4_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_10_4_0 not finished, try to restart OpenOLAT!");
		}
		return allOk;
	}
	
	private boolean migrateCalendarTokens(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(CALENDAR_TOKENS)) {
			int counter = 0;
			List<Property> properties;
			do {
				properties = findAllUserProperties(counter, BATCH_SIZE);
				for(Property property:properties) {
					processCalendarProperty(property);
					if(counter % 20 == 0) {
						dbInstance.commit();
					}
				}
				dbInstance.commitAndCloseSession();
				counter += properties.size();
			} while(properties.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(CALENDAR_TOKENS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processCalendarProperty(Property property) {
		String calendarId;
		Identity identity = property.getIdentity();
		String resourceType = property.getResourceTypeName();
		if(StringHelper.containsNonWhitespace(resourceType) && property.getResourceTypeId() != null) {
			calendarId = property.getResourceTypeId().toString();
		} else {
			resourceType = CalendarManager.TYPE_USER;
			calendarId = identity.getName();
		}
		
		CalendarUserConfiguration config = calendarUserConfigurationDao
				.getCalendarUserConfiguration(identity, calendarId, resourceType);
		
		if(config == null) {
			String token = property.getStringValue();
			Kalendar mockCal = new Kalendar(calendarId, resourceType);
			calendarUserConfigurationDao.createCalendarUserConfiguration(mockCal, identity, token, true, true);
		}
	}
	
	private List<Property> findAllUserProperties(int firstResult, int maxResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join v.identity identity ")
		  .append(" where v.category='icalAuthToken' and v.name='authToken'")
		  .append(" order by v.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	private boolean migrateCalendarConfigurations(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(CALENDAR_USER_CONFIGS)) {
			int counter = 0;

			List<Property> properties;
			do {
				properties = getUserGUIProperties(counter, BATCH_SIZE);
				for(Property property:properties) {
					processCalendarGUIProperty(property);
					if(counter % 20 == 0) {
						dbInstance.commit();
					}
				}
				counter += properties.size();
				log.info(Tracing.M_AUDIT, "Calendar GUI properties processed: " + properties.size() + ", total processed (" + counter + ")");
				dbInstance.commitAndCloseSession();
			} while(properties.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(CALENDAR_USER_CONFIGS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private List<Property> getUserGUIProperties(int firstResult, int maxResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join v.identity identity ")
		  .append(" where v.name='v2guipreferences'")
		  .append(" order by v.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	private void processCalendarGUIProperty(Property property) {
		String text = property.getTextValue();
		if(StringHelper.containsNonWhitespace(text)) {
			try {
				UpgradePreferences prefs = (UpgradePreferences)xstream.fromXML(text);
				if(prefs != null) {
					Map<String,Object> preferenceMap = prefs.getPreferences();
					for(Map.Entry<String,Object> entry:preferenceMap.entrySet()) {
						String key = entry.getKey();
						if(key.startsWith(KALENDAR_GUI_MARKER)) {
							String calendarId = key.substring(KALENDAR_GUI_MARKER.length(), key.length());
							KalendarConfig config = (KalendarConfig)entry.getValue();
							processKalendarConfig(property.getIdentity(), calendarId, config);
						}
					}
				}
			} catch (Exception e) {
				log.warn("Cannot read the preferences", e);
			}
		}
	}

	private void processKalendarConfig(Identity identity, String calendarId, KalendarConfig config) {
		Kalendar cal;
		CalendarUserConfiguration userConfig;
		if(StringHelper.isLong(calendarId)) {
			//guess if it's a course or a group calendar
			if(calendarMgr.getCalendarFile(CalendarManager.TYPE_COURSE, calendarId) != null) {
				cal = new Kalendar(calendarId, CalendarManager.TYPE_COURSE);
			} else if(calendarMgr.getCalendarFile(CalendarManager.TYPE_GROUP, calendarId) != null) {
				cal = new Kalendar(calendarId, CalendarManager.TYPE_GROUP);
			} else {
				return;
			}
		} else {
			//personal calendar
			cal = new Kalendar(calendarId, CalendarManager.TYPE_USER);
		}
		
		userConfig = calendarUserConfigurationDao
				.getCalendarUserConfiguration(identity, cal.getCalendarID(), cal.getType());
		if(userConfig == null) {
			userConfig = calendarUserConfigurationDao.createCalendarUserConfiguration(cal, identity);
			userConfig.setCssClass(config.getCss());
			userConfig.setVisible(config.isVis());
			userConfig = calendarUserConfigurationDao.update(userConfig);
		}
	}
	
	private boolean migrateImportedCalendars(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(IMPORTED_CALENDARS)) {
			int counter = 0;
			List<Property> properties;
			do {
				properties = getImportCalendarProperties(counter, BATCH_SIZE);
				for(Property property:properties) {
					processImportedCalendars(property);
					if(counter % 20 == 0) {
						dbInstance.commit();
					}
				}
				dbInstance.commitAndCloseSession();
				counter += properties.size();
			} while(properties.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(IMPORTED_CALENDARS, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processImportedCalendars(Property property) {
		Identity identity = property.getIdentity();
		String calendarPartialId = property.getName();
		String calendarId = identity.getName() + "_" + calendarPartialId;
		String url = property.getStringValue();
		
		List<ImportedCalendar> currentCalendars = importedCalendarDao
				.getImportedCalendar(identity, calendarId, CalendarManager.TYPE_USER);
		
		if(currentCalendars.isEmpty()) {
			Date importDate;
			if(property.getLongValue() != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(property.getLongValue().longValue());
				importDate = calendar.getTime();
			} else {
				importDate = new Date();
			}
			importedCalendarDao.createImportedCalendar(identity, calendarPartialId,
					calendarId, CalendarManager.TYPE_USER, url, importDate);
		}
	}
	
	private List<Property> getImportCalendarProperties(int firstResult, int maxResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" inner join v.identity identity ")
		  .append(" where v.category='Imported-Calendar'")
		  .append(" order by v.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
	
	private boolean migrateImportedCalendarsTo(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(IMPORTED_CALENDARS_URL)) {
			int counter = 0;
			List<Property> properties;
			do {
				properties = getImportCalendarWithUrlProperties(counter, BATCH_SIZE);
				for(Property property:properties) {
					processImportedCalendarsTo(property);
					if(counter % 20 == 0) {
						dbInstance.commit();
					}
				}
				dbInstance.commitAndCloseSession();
				counter += properties.size();
			} while(properties.size() == BATCH_SIZE);
			uhd.setBooleanDataValue(IMPORTED_CALENDARS_URL, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return true;
	}
	
	private void processImportedCalendarsTo(Property property) {
		//the calendar are imported in an existent calendar
		//urls are | separated
		String calendarId = property.getName();
		String type = property.getResourceTypeName();
		if("user".equals(type)) {
			return;//don't convert this, they have there own synchronization mechanism
		}
		String importUrls = property.getTextValue();

		if(StringHelper.containsNonWhitespace(importUrls) && StringHelper.containsNonWhitespace(calendarId) && StringHelper.containsNonWhitespace(type)) {
			for(StringTokenizer tokenizer = new StringTokenizer(importUrls, "|"); tokenizer.hasMoreTokens(); ) {
				String importUrl = tokenizer.nextToken();
				
				List<ImportedToCalendar> currentCalendars = importedToCalendarDao.getImportedToCalendars(calendarId, type, importUrl);
				if(currentCalendars.isEmpty()) {
					Date importDate;
					if(property.getLongValue() != null) {
						Calendar calendar = Calendar.getInstance();
						calendar.setTimeInMillis(property.getLongValue().longValue());
						importDate = calendar.getTime();
					} else {
						importDate = new Date();
					}
					importedToCalendarDao.createImportedToCalendar(calendarId, type, importUrl, importDate);
				}
			}
		}
	}
	
	private List<Property> getImportCalendarWithUrlProperties(int firstResult, int maxResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(Property.class.getName()).append(" as v ")
		  .append(" where v.category='Imported-Calendar-To'")
		  .append(" order by v.key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Property.class)
				.setFirstResult(firstResult)
				.setMaxResults(maxResult)
				.getResultList();
	}
}
