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
package org.olat.core.util.prefs.gui;

import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.prefs.PreferencesStorage;

/**
 * Initial date: Dez 05, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public interface GuiPreferenceService extends PreferencesStorage {

	/**
	 * Creates a new GuiPreference object, but does not persist it
	 *
	 * @param identity
	 * @param attributedClass
	 * @param prefKey
	 * @param prefValue
	 * @return new created object GuiPreference
	 */
	GuiPreference createGuiPreferenceEntry(IdentityRef identity, String attributedClass, String prefKey, String prefValue);

	/**
	 * Updates given guiPreference, which has to exist already
	 *
	 * @param guiPreference
	 * @return updated object GuiPreference
	 */
	GuiPreference updateGuiPreferences(GuiPreference guiPreference);

	/**
	 * either persists given object or loads it
	 *
	 * @param guiPreference
	 * @return given object if persisting or loading was successful otherwise null
	 */
	GuiPreference persistOrLoad(GuiPreference guiPreference);

	/**
	 * Primarily used in admin area to show how many entries of each attributedClass are there
	 *
	 * @return Map: attributedClass to the respective count of entries
	 */
	Map<String, Long> countDistinctAttrClass();

	/**
	 * loads guiPreferences by given parameters, if one or all params are null then those null values
	 * won't be taken into consideration for loading (e.g. all null equals getting all guiPrefs)
	 *
	 * @param identity        can be null
	 * @param attributedClass can be null
	 * @param prefKey         can be null
	 * @return list of GuiPreferences or emptyList
	 */
	List<GuiPreference> loadGuiPrefsByUniqueProperties(IdentityRef identity, String attributedClass, String prefKey);

	/**
	 * uses loadGuiPrefsByUniqueProperties(...) for retrieving desired guiPrefs with given params
	 * deletes all retrieved entries
	 *
	 * @param identity        can be null
	 * @param attributedClass can be null
	 * @param prefKey         can be null
	 */
	void deleteGuiPrefsByUniqueProperties(IdentityRef identity, String attributedClass, String prefKey);

	/**
	 * deletes one specific GuiPreference
	 *
	 * @param guiPreference
	 */
	void deleteGuiPreference(GuiPreference guiPreference);
}
