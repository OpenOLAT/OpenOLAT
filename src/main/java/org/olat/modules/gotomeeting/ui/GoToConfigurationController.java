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
package org.olat.modules.gotomeeting.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.gotomeeting.GoToMeetingModule;
import org.olat.modules.gotomeeting.GoToTimezoneIDs;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Setup the configuration for GoTo
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToConfigurationController extends FormBasicController {
	
	private SingleSelection timeZoneEls;
	private TextElement trainingConsumerKeyEl;
	private TextElement trainingConsumerSecretEl;
	private MultipleSelectionElement enabledEl;

	private static final String[] enabledKeys = new String[]{"on"};
	
	@Autowired
	private GoToMeetingModule goToMeetingModule;
	
	public GoToConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] enabledValues = new String[]{ translate("enabled") };
		enabledEl = uifactory.addCheckboxesHorizontal("goto.module.enabled", formLayout, enabledKeys, enabledValues);
		enabledEl.select(enabledKeys[0], goToMeetingModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		//
		String consumerKey = goToMeetingModule.getTrainingConsumerKey();
		trainingConsumerKeyEl = uifactory.addTextElement("training.consumerkey", "training.consumerkey", 128, consumerKey, formLayout);
		String consumerSecret = goToMeetingModule.getTrainingConsumerSecret();
		trainingConsumerSecretEl = uifactory.addTextElement("training.consumersecret", "training.consumersecret", 128, consumerSecret, formLayout);
		
		String[] timezoneIds = orderedTimezoneIds();
		timeZoneEls = uifactory.addDropdownSingleselect("timezone.ids", "timezone.id", formLayout, timezoneIds, timezoneIds, null);
		String timeZoneId = goToMeetingModule.getGoToTimeZoneId();
		for(String key: GoToTimezoneIDs.TIMEZONE_IDS) {
			if(key.equals(timeZoneId)) {
				timeZoneEls.select(key, true);
			}
		}
		updateEnabled();
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	private String[] orderedTimezoneIds() {
		List<String> timezoneIdList = new ArrayList<>(GoToTimezoneIDs.TIMEZONE_IDS);
		Collections.sort(timezoneIdList);
		return timezoneIdList.toArray(new String[timezoneIdList.size()]);
	}
	
	private void updateEnabled() {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		trainingConsumerKeyEl.setVisible(enabled);
		trainingConsumerSecretEl.setVisible(enabled);
		timeZoneEls.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabledEl == source) {
			updateEnabled();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		goToMeetingModule.setEnabled(enabledEl.isAtLeastSelected(1));
		if(enabledEl.isAtLeastSelected(1)) {
			String trainingConsumerKey = trainingConsumerKeyEl.getValue();
			goToMeetingModule.setTrainingConsumerKey(trainingConsumerKey);
			String trainingConsumerSecret = trainingConsumerSecretEl.getValue();
			goToMeetingModule.setTrainingConsumerSecret(trainingConsumerSecret);
			String selectedTimeZoneId = timeZoneEls.getSelectedKey();
			goToMeetingModule.setGoToTimeZoneId(selectedTimeZoneId);
		} else {
			goToMeetingModule.setTrainingConsumerKey(null);
			goToMeetingModule.setTrainingConsumerSecret(null);
			goToMeetingModule.setGoToTimeZoneId(null);
		}
	}
}
