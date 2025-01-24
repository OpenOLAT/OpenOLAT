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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 4, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EditCurriculumElementOptionsController extends FormBasicController {
	
	private static final String KEY_ADOPT = "adopt";
	private static final String KEY_OVERRIDE = "override";
	private static final String[] CONFIG_KEYS = new String[] { KEY_ADOPT, KEY_OVERRIDE };
	
	private SingleSelection calendarConfigEl;
	private FormToggle calendarEl;
	private SingleSelection lectureConfigEl;
	private FormToggle lectureEl;
	private SingleSelection learningProgressConfigEl;
	private FormToggle learningProgressEl;
	
	private CurriculumElement element;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;

	public EditCurriculumElementOptionsController(UserRequest ureq, WindowControl wControl, CurriculumElement element,
			CurriculumSecurityCallback secCallback) {
		super(ureq, wControl);
		this.element = element;
		this.secCallback = secCallback;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("curriculum.element.options");
		
		boolean canEdit = secCallback.canEditCurriculumElement(element);
		
		String[] configValues = new String[] {
				translate("option.adopt", StringHelper.escapeHtml(element.getType().getDisplayName()),
						element.getType().getCalendars() == CurriculumCalendars.enabled? translate("on"): translate("off")),
				translate("option.override")
		};
		calendarConfigEl = uifactory.addRadiosHorizontal("type.calendars.config", formLayout, CONFIG_KEYS, configValues);
		calendarConfigEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.calendars));
		calendarConfigEl.addActionListener(FormEvent.ONCHANGE);
		if (element.getCalendars() == CurriculumCalendars.inherited) {
			calendarConfigEl.select(KEY_ADOPT, true);
		} else {
			calendarConfigEl.select(KEY_OVERRIDE, true);
		}
		
		calendarEl = uifactory.addToggleButton("calendars", "type.calendars.enabled", translate("on"), translate("off"), formLayout);
		calendarEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.calendars));
		calendarEl.toggle(element.getCalendars() == CurriculumCalendars.enabled || element.getCalendars() == CurriculumCalendars.inherited);
		
		configValues = new String[] {
				translate("option.adopt", StringHelper.escapeHtml(element.getType().getDisplayName()),
						element.getType().getLectures() == CurriculumLectures.enabled? translate("on"): translate("off")),
				translate("option.override")
		};
		lectureConfigEl = uifactory.addRadiosHorizontal("type.lectures.config", formLayout, CONFIG_KEYS, configValues);
		lectureConfigEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.lectures));
		lectureConfigEl.addActionListener(FormEvent.ONCHANGE);
		if (element.getLectures() == CurriculumLectures.inherited) {
			lectureConfigEl.select(KEY_ADOPT, true);
		} else {
			lectureConfigEl.select(KEY_OVERRIDE, true);
		}
		
		lectureEl = uifactory.addToggleButton("lectures", "type.lectures.enabled", translate("on"), translate("off"), formLayout);
		lectureEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.lectures));
		lectureEl.toggle(element.getLectures() == CurriculumLectures.enabled || element.getLectures() == CurriculumLectures.inherited);
		
		configValues = new String[] {
				translate("option.adopt", StringHelper.escapeHtml(element.getType().getDisplayName()),
						element.getType().getLearningProgress() == CurriculumLearningProgress.enabled? translate("on"): translate("off")),
				translate("option.override")
		};
		learningProgressConfigEl = uifactory.addRadiosHorizontal("type.learning.progress.config", formLayout, CONFIG_KEYS, configValues);
		learningProgressConfigEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.learningProgress));
		learningProgressConfigEl.addActionListener(FormEvent.ONCHANGE);
		if (element.getLearningProgress() == CurriculumLearningProgress.inherited) {
			learningProgressConfigEl.select(KEY_ADOPT, true);
		} else {
			learningProgressConfigEl.select(KEY_OVERRIDE, true);
		}
		
		learningProgressEl = uifactory.addToggleButton("learningProgress", "type.learning.progress.enabled", translate("on"), translate("off"), formLayout);
		learningProgressEl.setEnabled(canEdit && !CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.learningProgress));
		learningProgressEl.toggle(element.getLearningProgress() == CurriculumLearningProgress.enabled || element.getLearningProgress() == CurriculumLearningProgress.inherited);
		
		if (canEdit) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}
	
	private void updateUI() {
		boolean calendarOverride = false;
		if (calendarConfigEl.isOneSelected() && calendarConfigEl.isKeySelected(KEY_OVERRIDE)) {
			calendarOverride = true;
		}
		calendarEl.setVisible(calendarOverride);
		
		boolean lecturesOverride = false;
		if (lectureConfigEl.isOneSelected() && lectureConfigEl.isKeySelected(KEY_OVERRIDE)) {
			lecturesOverride = true;
		}
		lectureEl.setVisible(lecturesOverride);
		
		boolean learningProgresOverride = false;
		if (learningProgressConfigEl.isOneSelected() && learningProgressConfigEl.isKeySelected(KEY_OVERRIDE)) {
			learningProgresOverride = true;
		}
		learningProgressEl.setVisible(learningProgresOverride);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == calendarConfigEl) {
			updateUI();
		} else if (source == lectureConfigEl) {
			updateUI();
		} else if (source == learningProgressConfigEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		element = curriculumService.getCurriculumElement(element);
		
		CurriculumCalendars calendars;
		if (calendarEl.isVisible()) {
			calendars = calendarEl.isOn()? CurriculumCalendars.enabled: CurriculumCalendars.disabled;
		} else {
			calendars = CurriculumCalendars.inherited;
		}
		element.setCalendars(calendars);
		
		CurriculumLectures lectures;
		if (lectureEl.isVisible()) {
			lectures = lectureEl.isOn()? CurriculumLectures.enabled: CurriculumLectures.disabled;
		} else {
			lectures = CurriculumLectures.inherited;
		}
		element.setLectures(lectures);
		
		CurriculumLearningProgress learningProgress;
		if (learningProgressEl.isVisible()) {
			learningProgress = learningProgressEl.isOn()? CurriculumLearningProgress.enabled: CurriculumLearningProgress.disabled;
		} else {
			learningProgress = CurriculumLearningProgress.inherited;
		}
		element.setLearningProgress(learningProgress);
		
		element = curriculumService.updateCurriculumElement(element);
	}

}
