/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai;

import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;
import org.olat.core.util.prefs.ram.RamPreferences;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Stores the choice of a person for a user-controlled AI feature and resolves it
 * against the system default of the administrator.
 * <p>
 * The preference lives in the GUI preferences, attributed class
 * AiUserPreferenceService, key ai.optout.&lt;feature type&gt;, value default, on
 * or off. Callers inside a controller pass
 * ureq.getUserSession().getGuiPreferences(), so the session copy stays in sync
 * after a write.
 *
 * Initial date: Sep 13, 2026<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
@Service
public class AiUserPreferenceService {

	private static final String PREF_KEY_PREFIX = "ai.optout.";
	private static final String VALUE_ON = "on";
	private static final String VALUE_OFF = "off";

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiEssayGradingService aiEssayGradingService;
	@Autowired
	private AiImageDescriptionService aiImageDescriptionService;

	/**
	 * @param prefs the GUI preferences of the person
	 * @param feature the feature
	 * @return the stored choice, DEFAULT for an absent row and for an unreadable value
	 */
	public AiUserPreference get(Preferences prefs, AiFeature feature) {
		if (prefs == null || feature == null) {
			return AiUserPreference.DEFAULT;
		}
		Object value = prefs.get(AiUserPreferenceService.class, prefKey(feature));
		if (value instanceof String stored) {
			if (VALUE_ON.equalsIgnoreCase(stored)) {
				return AiUserPreference.ON;
			}
			if (VALUE_OFF.equalsIgnoreCase(stored)) {
				return AiUserPreference.OFF;
			}
		}
		return AiUserPreference.DEFAULT;
	}

	/**
	 * Stores the choice of the person. DEFAULT writes the string default, it does
	 * not delete the row. Never writes for a guest: a guest gets transient
	 * preferences and all guests of one language share one identity.
	 * <p>
	 * The guest check reads the implementation type, because the Preferences API
	 * carries no identity and no roles. That is legitimate here:
	 * {@link org.olat.core.util.prefs.gui.manager.GuiPreferenceServiceImpl#getPreferencesFor(Identity, boolean)}
	 * is the only place that creates a {@link RamPreferences}, and the only caller
	 * that asks for transient preferences is UserSession.reloadPreferences() with
	 * Roles.isGuestOnly().
	 *
	 * @param prefs the GUI preferences of the person
	 * @param feature the feature
	 * @param pref the choice
	 */
	public void set(Preferences prefs, AiFeature feature, AiUserPreference pref) {
		if (prefs == null || feature == null || pref == null || prefs instanceof RamPreferences) {
			return;
		}
		prefs.putAndSave(AiUserPreferenceService.class, prefKey(feature), pref.name().toLowerCase(Locale.ROOT));
	}

	/**
	 * Overload for callers without a user session. A caller inside a controller
	 * uses the Preferences variant instead. Fails closed in the sense of "no
	 * choice": a null Identity returns DEFAULT.
	 *
	 * @param identity the person
	 * @param feature the feature
	 * @return the stored choice, DEFAULT for an absent row and for an unreadable value
	 */
	public AiUserPreference get(Identity identity, AiFeature feature) {
		if (identity == null) {
			return AiUserPreference.DEFAULT;
		}
		return get(PreferencesFactory.getInstance().getPreferencesFor(identity, false), feature);
	}

	/**
	 * @param prefs the GUI preferences of the person
	 * @param feature the feature
	 * @return true: the person made a choice, that is a stored ON or OFF
	 */
	public boolean hasPreference(Preferences prefs, AiFeature feature) {
		return get(prefs, feature) != AiUserPreference.DEFAULT;
	}

	/**
	 * Only for a feature with {@link AiFeature#isUserControlled()} == true; every
	 * other constant returns false. Fails closed: a null Preferences returns false.
	 *
	 * @param prefs the GUI preferences of the person
	 * @param feature the feature
	 * @return true: the feature is available and runs for this person
	 */
	public boolean isActive(Preferences prefs, AiFeature feature) {
		if (prefs == null || !isFeatureAvailable(feature)) {
			return false;
		}
		return switch (get(prefs, feature)) {
			case ON -> true;
			case OFF -> false;
			case DEFAULT -> aiModule.isUserDefaultOn(feature);
		};
	}

	/**
	 * @param feature the feature
	 * @return true: the administrator switched the feature on and a provider is configured
	 */
	public boolean isFeatureAvailable(AiFeature feature) {
		if (feature == null) {
			return false;
		}
		return switch (feature) {
			case EssayGrading -> aiEssayGradingService.isEnabled();
			case ImageDescriptionGenerator -> aiImageDescriptionService.isEnabled();
			default -> false;
		};
	}

	private static String prefKey(AiFeature feature) {
		return PREF_KEY_PREFIX + feature.getType();
	}

}
