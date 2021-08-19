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
* Initial code contributed and copyrighted by<br>
* JGS goodsolutions GmbH, http://www.goodsolutions.ch
* <p>
*/
package org.olat.core.util.prefs.db;

import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesStorage;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

import com.thoughtworks.xstream.XStream;

/**
 * Description:<br>
 * <P>
 * Initial Date: 21.06.2006 <br>
 * 
 * @author Felix Jost
 */
public class DbStorage implements PreferencesStorage {
	
	private static final Logger log = Tracing.createLoggerFor(DbStorage.class);

	static final String USER_PROPERTY_KEY = "v2guipreferences";
	
	private static final XStream xstream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.ignoreUnknownElements();
	}

	@Override
	public Preferences getPreferencesFor(Identity identity, boolean useTransientPreferences) {
		if (useTransientPreferences) {
			return createEmptyDbPrefs(identity,true);
		} else {			
			try {
				return getPreferencesFor(identity);
			} catch (Exception e) {
				log.error("Retry after exception", e);
				return getPreferencesFor(identity);
			}
		}
	}

	@Override
	public void updatePreferencesFor(Preferences prefs, Identity identity) {
		String props = xstream.toXML(prefs);
		Property property = getPreferencesProperty(identity);
		if (property == null) {
			property = PropertyManager.getInstance().createPropertyInstance(identity, null, null, null, DbStorage.USER_PROPERTY_KEY, null, null,
					null, props);
			// also save the properties to db, here (strentini)
			// fixes the "non-present gui preferences" for new users, or where guiproperties were manually deleted
			PropertyManager.getInstance().saveProperty(property);
		} else {
			property.setTextValue(props);
			PropertyManager.getInstance().updateProperty(property);
		}
	}

	/**
	 * search x-stream serialization in properties table, create new if not found
	 * @param identity
	 * @return
	 */
	private DbPrefs getPreferencesFor(final Identity identity) {
		Property guiProperty = getPreferencesProperty(identity);
		if (guiProperty == null) {
			return createEmptyDbPrefs(identity,false);
		} else {
			return getPreferencesForProperty(identity, guiProperty);
		}
	}
	
	private Property getPreferencesProperty(Identity identity) {
		Property guiProperty = null; 
		try { 
			guiProperty = PropertyManager.getInstance().findProperty(identity, null, null, null, USER_PROPERTY_KEY); 
		} catch (Exception e) {
			// OLAT-6429 detect and delete multiple prefs objects, keep the first one only 
			List<Property> guiPropertyList = PropertyManager.getInstance().findProperties(identity, null, null, null, USER_PROPERTY_KEY); 
			if (guiPropertyList != null && !guiPropertyList.isEmpty()) {
				 log.warn("Found more than 1 entry for {} in o_property table for identity {}. Use first of them, deleting the others!",
						 USER_PROPERTY_KEY, identity.getKey(), e); 
				 Iterator<Property> iterator = guiPropertyList.iterator();
				 guiProperty = iterator.next();
				 while (iterator.hasNext()) { 
					 Property property = iterator.next(); 
					 PropertyManager.getInstance().deleteProperty(property); 				 
					 log.info("Will delete old property: {}", property.getTextValue()); 
				} 
			}
		}
		return guiProperty;
	}

	public DbPrefs getPreferencesForProperty(Identity identity, Property guiProperty) {
		DbPrefs prefs;
		try {
			prefs = createDbPrefsFrom(identity, guiProperty.getTextValue());
		} catch (Exception e) {
			log.error("", e);
			prefs = doGuiPrefsMigration( guiProperty, identity);
		}
		return prefs;
	}

	private DbPrefs createEmptyDbPrefs(Identity identity, boolean isTransient) {
		DbPrefs prefs = new DbPrefs();
		prefs.setIdentity(identity);
		prefs.setTransient(isTransient);
		return prefs;
	}

	private DbPrefs createDbPrefsFrom(Identity identity, String textValue) {
		DbPrefs prefs = (DbPrefs) xstream.fromXML(textValue);
		prefs.setIdentity(identity); // reset transient value
		return prefs;
	}

	private DbPrefs doGuiPrefsMigration(Property guiProperty, Identity identity) {
		String migratedTextValue = doCalendarRefactoringMigration(guiProperty.getTextValue());
		// add new migration method here 
		try {
			return createDbPrefsFrom(identity, migratedTextValue);
		} catch (Exception e) {
			// Migration failed => return empty db-prefs
			return createEmptyDbPrefs(identity,false);
		}
	}

	/**
	 * Migration for 5.1.x to 5.2.0 because the calendar package was changed. 
	 * Rename 'org.olat.core.commons.calendar.model.KalendarConfig' to 'org.olat.commons.calendar.model.KalendarConfig'.
	 * @param textValue
	 * @return Migrated textValue String
	 */
	private String doCalendarRefactoringMigration(String textValue) {
		return textValue.replaceAll("org.olat.core.commons.calendar.model.KalendarConfig", "org.olat.commons.calendar.model.KalendarConfig");
	}

}
