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
package org.olat.course.nodes.edubase;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.EdubaseCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 21.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseConfigController extends FormBasicController {
	
	private static final String[] enabledKeys = new String[] { "no" };
	
	private final ModuleConfiguration config;
	
	private MultipleSelectionElement descriptionEnabledEl;

	public EdubaseConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);

		this.config = moduleConfiguration;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		setFormContextHelp("manual_user/course_elements/Knowledge_Transfer/#edubase");
		
		boolean descriptionEnabled = config.getBooleanSafe(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED);
		String[] enableValues = new String[] { translate("on") };
		descriptionEnabledEl = uifactory.addCheckboxesHorizontal("edubase.with.description.enabled", formLayout,
				enabledKeys, enableValues);
		if (descriptionEnabled) {
			descriptionEnabledEl.select(enabledKeys[0], true);
		}

		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	protected ModuleConfiguration getUpdatedConfig() {
		config.set(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED, 
				descriptionEnabledEl.isAtLeastSelected(1)? Boolean.toString(true): Boolean.toString(false));
		return config;
	}
}
