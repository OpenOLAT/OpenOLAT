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
package org.olat.course.nodes.livestream.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.course.nodes.livestream.ui.LiveStreamUIFactory.validateInteger;

import java.util.Arrays;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.LiveStreamCourseNode;
import org.olat.course.nodes.livestream.LiveStreamModule;
import org.olat.course.nodes.livestream.paella.PlayerProfile;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23.05.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamConfigController extends FormBasicController {
	
	private static final String[] ENABLED_KEYS = new String[]{"on"};
	
	private final ModuleConfiguration config;
	
	private TextElement bufferBeforeMinEl;
	private TextElement bufferAfterMinEl;
	private MultipleSelectionElement coachCanEditEl;
	private SingleSelection playerProfileEl;
	
	@Autowired
	private LiveStreamModule liveStreamModule;

	public LiveStreamConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);
		this.config = moduleConfiguration;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		setFormContextHelp("Knowledge Transfer#_livestream");
		
		int bufferBeforeMin = config.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, 0);
		bufferBeforeMinEl = uifactory.addTextElement("config.buffer.before.min", 4, String.valueOf(bufferBeforeMin),
				formLayout);
		bufferBeforeMinEl.setMandatory(true);

		int bufferAfterMin = config.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_AFTER_MIN, 0);
		bufferAfterMinEl = uifactory.addTextElement("config.buffer.after.min", 4, String.valueOf(bufferAfterMin),
				formLayout);
		bufferAfterMinEl.setMandatory(true);
		
		coachCanEditEl = uifactory.addCheckboxesVertical("config.coach.edit", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS), 1);
		boolean coachCanEdit = config.getBooleanSafe(LiveStreamCourseNode.CONFIG_COACH_CAN_EDIT);
		coachCanEditEl.select(ENABLED_KEYS[0], coachCanEdit);
		
		if (liveStreamModule.isMultiStreamEnabled()) {
			SelectionValues playerProfileKV = new SelectionValues();
			for (PlayerProfile playerProfile : PlayerProfile.values()) {
				playerProfileKV.add(entry(playerProfile.name(), translate(playerProfile.getI18nKey())));
			}
			playerProfileEl = uifactory.addDropdownSingleselect("config.player.profile", formLayout, playerProfileKV.keys(),
					playerProfileKV.values());
			String playerProfile = config.getStringValue(LiveStreamCourseNode.CONFIG_PLAYER_PROFILE,
					liveStreamModule.getPlayerProfile());
			if (Arrays.asList(playerProfileEl.getKeys()).contains(playerProfile)) {
				playerProfileEl.select(playerProfile, true);
			}
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		allOk &= validateInteger(bufferBeforeMinEl, true);
		allOk &= validateInteger(bufferAfterMinEl, true);

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		int bufferBeforeMin = Integer.parseInt(bufferBeforeMinEl.getValue());
		config.setIntValue(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, bufferBeforeMin);
		
		int bufferAfterMin = Integer.parseInt(bufferAfterMinEl.getValue());
		config.setIntValue(LiveStreamCourseNode.CONFIG_BUFFER_AFTER_MIN, bufferAfterMin);
		
		boolean coachCanEdit = coachCanEditEl.isAtLeastSelected(1);
		config.setBooleanEntry(LiveStreamCourseNode.CONFIG_COACH_CAN_EDIT, coachCanEdit);
		
		if (playerProfileEl != null) {
			String playerProfile = playerProfileEl.getSelectedKey();
			config.setStringValue(LiveStreamCourseNode.CONFIG_PLAYER_PROFILE, playerProfile);
		}
		
		return config;
	}

	@Override
	protected void doDispose() {
		//
	}
	
}
