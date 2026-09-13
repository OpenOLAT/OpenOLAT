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
package org.olat.core.commons.services.ai.ui;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.ai.AiFeature;
import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiUserPreference;
import org.olat.core.commons.services.ai.AiUserPreferenceService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.prefs.Preferences;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The segment "AI settings" of the user settings. It shows one radio group per
 * available user-controlled AI feature, with the three choices Default, On and
 * Off. The label of the Default choice names the system default of the
 * administrator.
 *
 * Initial date: Sep 13, 2026<br>
 * @author Florian Gnägi, gnaegi, https://www.frentix.com
 *
 */
public class AiUserSettingsController extends FormBasicController {

	private static final String KEY_DEFAULT = "default";
	private static final String KEY_ON = "on";
	private static final String KEY_OFF = "off";

	/** The rows in the order of the design, not in the order of the enum. */
	public static final List<AiFeature> CONTROLLED_FEATURES =
			List.of(AiFeature.EssayGrading, AiFeature.ImageDescriptionGenerator);

	private final Map<AiFeature, SingleSelection> featureEls = new LinkedHashMap<>();

	@Autowired
	private AiModule aiModule;
	@Autowired
	private AiUserPreferenceService aiUserPreferenceService;

	public AiUserSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.optout.title");
		setFormDescription("ai.optout.desc");

		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		for (AiFeature feature : CONTROLLED_FEATURES) {
			if (!aiUserPreferenceService.isFeatureAvailable(feature)) {
				continue;
			}

			String defaultValue = aiModule.isUserDefaultOn(feature)
					? translate("ai.optout.choice.default.on")
					: translate("ai.optout.choice.default.off");
			String[] keys = new String[] { KEY_DEFAULT, KEY_ON, KEY_OFF };
			String[] values = new String[] { defaultValue, translate("ai.optout.choice.on"),
					translate("ai.optout.choice.off") };
			SingleSelection featureEl = uifactory.addRadiosHorizontal(feature.getType(),
					feature.getI18nUserNameKey(), formLayout, keys, values);
			featureEl.setExampleKey(feature.getI18nDescriptionKey(), null);
			featureEl.select(toKey(aiUserPreferenceService.get(prefs, feature)), true);
			featureEls.put(feature, featureEl);

			if (feature == AiFeature.EssayGrading) {
				// Without a stored choice the quiz asks at its start, the warning says so
				featureEl.setWarningKey("ai.optout.essay.note");
			}
		}

		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		for (Map.Entry<AiFeature, SingleSelection> entry : featureEls.entrySet()) {
			AiUserPreference selected = toPreference(entry.getValue().getSelectedKey());
			if (selected != aiUserPreferenceService.get(prefs, entry.getKey())) {
				aiUserPreferenceService.set(prefs, entry.getKey(), selected);
			}
		}
		showInfo("ai.optout.saved");
	}

	private static String toKey(AiUserPreference pref) {
		return switch (pref) {
			case ON -> KEY_ON;
			case OFF -> KEY_OFF;
			case DEFAULT -> KEY_DEFAULT;
		};
	}

	private static AiUserPreference toPreference(String key) {
		return switch (key) {
			case KEY_ON -> AiUserPreference.ON;
			case KEY_OFF -> AiUserPreference.OFF;
			default -> AiUserPreference.DEFAULT;
		};
	}
}
