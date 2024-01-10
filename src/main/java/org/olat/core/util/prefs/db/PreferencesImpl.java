/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.util.prefs.db;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.gui.GuiPreference;
import org.olat.core.util.prefs.gui.GuiPreferenceService;
import org.olat.core.util.xml.XStreamHelper;

/**
 * Initial date: Dez 15, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class PreferencesImpl implements Preferences {

	private static final Logger log = Tracing.createLoggerFor(PreferencesImpl.class);

	private static final XStream xstream = XStreamHelper.createXStreamInstance();

	static {
		XStreamHelper.allowDefaultPackage(xstream);
		xstream.ignoreUnknownElements();
	}

	private final GuiPreferenceService guiPreferenceService;
	private final IdentityRef identity;

	public PreferencesImpl(GuiPreferenceService guiPreferenceService, IdentityRef identity) {
		this.guiPreferenceService = guiPreferenceService;
		this.identity = identity;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <U> List<U> getList(Class<?> attributedClass, String key, Class<U> type) {
		List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass.getName(), key);
		String prefValue =  !guiPreferences.isEmpty() ? guiPreferences.get(0).getPrefValue() : null;

		return (List<U>) retrieveObjectValue(prefValue, null);
	}

	@Override
	public Object get(Class<?> attributedClass, String key) {
		return get(attributedClass.getName(), key);
	}

	@Override
	public Object get(String attributedClass, String key) {
		List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, key);
		// get(0) because only one entry can be found if all three parameters are set for loadGuiPrefsByUniqueProperties
		String prefValue = !guiPreferences.isEmpty() ? guiPreferences.get(0).getPrefValue() : null;

		return retrieveObjectValue(prefValue, null);
	}

	@Override
	public Object get(Class<?> attributedClass, String key, Object defaultValue) {
		List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass.getName(), key);
		// get(0) because only one entry can be found if all three parameters are set for loadGuiPrefsByUniqueProperties
		String prefValue = !guiPreferences.isEmpty() ? guiPreferences.get(0).getPrefValue() : null;

		return retrieveObjectValue(prefValue, defaultValue);
	}

	@Override
	public void putAndSave(Class<?> attributedClass, String key, Object value) {
		putAndSave(attributedClass.getName(), key, value);
	}

	@Override
	public void putAndSave(String attributedClass, String key, Object value) {
		String newPrefValue = xstream.toXML(value);
		List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, attributedClass, key);
		// get(0) because only one or 0 entry can be found if all three parameters are set for loadGuiPrefsByUniqueProperties
		GuiPreference guiPreference = !guiPreferences.isEmpty() ? guiPreferences.get(0) : null;

		// update if not null, otherwise create new entry
		if (guiPreference != null) {
			guiPreference.setPrefValue(newPrefValue);
			guiPreferenceService.updateGuiPreferences(guiPreference);
			DBFactory.getInstance().commit();
		} else {
			GuiPreference newGuiPref = guiPreferenceService.createGuiPreferenceEntry(identity, attributedClass, key, newPrefValue);
			guiPreferenceService.persistOrLoad(newGuiPref);
		}
	}

	@Override
	public Object findPrefByKey(String key) {
		List<GuiPreference> guiPreferences = guiPreferenceService.loadGuiPrefsByUniqueProperties(identity, null, key);
		// use case to find specific entry, where identity and key are unique, so only one result should be available, thus get(0)
		String prefValue = !guiPreferences.isEmpty() ? guiPreferences.get(0).getPrefValue() : null;

		return retrieveObjectValue(prefValue, null);
	}

	private Object retrieveObjectValue(String prefValue, Object defaultValue) {
		Object objectValue = defaultValue;
		if (prefValue != null) {
			// try/catch to prevent invalid prefValue to go through xstream (e.g. invalid XML Tags)
			try {
				objectValue = xstream.fromXML(prefValue);
			} catch (Exception e) {
				log.error("xStream not did not work properly on value: {}", prefValue);
			}
		}

		return objectValue;
	}
}
