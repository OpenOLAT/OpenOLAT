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
package org.olat.course.learningpath.ui;

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.Arrays;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.config.CompletionType;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathEditConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathTranslations;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNodeConfigController extends FormBasicController {	

	public static final String CONFIG_VALUE_TRIGGER_NODE_VISITED = FullyAssessedTrigger.nodeVisited.name();
	public static final String CONFIG_VALUE_TRIGGER_CONFIRMED = FullyAssessedTrigger.confirmed.name();
	public static final String CONFIG_VALUE_TRIGGER_STATUS_DONE = FullyAssessedTrigger.statusDone.name();
	public static final String CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW = FullyAssessedTrigger.statusInReview.name();
	public static final String CONFIG_VALUE_TRIGGER_SCORE = FullyAssessedTrigger.score.name();
	public static final String CONFIG_VALUE_TRIGGER_PASSED = FullyAssessedTrigger.passed.name();
	
	private SingleSelection obligationEl;
	private DateChooser startDateEl;
	private DateChooser endDateEl;
	private TextElement durationEl;
	private SingleSelection triggerEl;
	private TextElement scoreCutEl;

	private final CourseConfig courseConfig;
	private final LearningPathConfigs learningPathConfigs;
	private final LearningPathEditConfigs editConfigs;
	
	@Autowired
	private LearningPathService learningPathService;

	public LearningPathNodeConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			CourseNode courseNode, LearningPathEditConfigs editConfigs) {
		super(ureq, wControl);
		this.courseConfig = CourseFactory.loadCourse(courseEntry).getCourseConfig();
		this.learningPathConfigs = learningPathService.getConfigs(courseNode);
		this.editConfigs = editConfigs;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("config.title");
		setFormContextHelp("Learning path element");
		formLayout.setElementCssClass("o_sel_learnpath_element");
		KeyValues obligationKV = new KeyValues();
		obligationKV.add(entry(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		obligationKV.add(entry(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", formLayout, obligationKV.keys(), obligationKV.values());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		AssessmentObligation obligation = learningPathConfigs.getObligation() != null
				? learningPathConfigs.getObligation()
				: LearningPathConfigs.OBLIGATION_DEFAULT;
		String obligationKey = obligation.name();
		if (Arrays.asList(obligationEl.getKeys()).contains(obligationKey)) {
			obligationEl.select(obligationKey, true);
		}
		
		Date startDate = learningPathConfigs.getStartDate();
		startDateEl = uifactory.addDateChooser("config.start.date", startDate, formLayout);
		startDateEl.setDateChooserTimeEnabled(true);
		
		Date endDate = learningPathConfigs.getEndDate();
		endDateEl = uifactory.addDateChooser("config.end.date", endDate, formLayout);
		endDateEl.setDateChooserTimeEnabled(true);
		
		String duration = learningPathConfigs.getDuration() != null? learningPathConfigs.getDuration().toString(): null;
		durationEl = uifactory.addTextElement("config.duration", 128, duration , formLayout);
		
		KeyValues triggerKV = getTriggerKV();
		triggerEl = uifactory.addRadiosVertical("config.trigger", formLayout,
				triggerKV.keys(), triggerKV.values());
		triggerEl.addActionListener(FormEvent.ONCHANGE);
		FullyAssessedTrigger trigger = learningPathConfigs.getFullyAssessedTrigger() != null
				? learningPathConfigs.getFullyAssessedTrigger()
				: LearningPathConfigs.TRIGGER_DEFAULT;
		String triggerKey = trigger.name();
		if (Arrays.asList(triggerEl.getKeys()).contains(triggerKey)) {
			triggerEl.select(triggerKey, true);
		}
		
		String score = learningPathConfigs.getScoreTriggerValue() != null
				? learningPathConfigs.getScoreTriggerValue().toString()
				: null;
		scoreCutEl = uifactory.addTextElement("config.score.cut", 100, score, formLayout);
		scoreCutEl.setMandatory(true);
		
		uifactory.addFormSubmitButton("save", formLayout);
		
		updateUI();
	}

	private KeyValues getTriggerKV() {
		KeyValues triggerKV = new KeyValues();
		if (editConfigs.isTriggerNodeVisited()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_NODE_VISITED, translate("config.trigger.visited")));
		}
		if (editConfigs.isTriggerConfirmed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_CONFIRMED, translate("config.trigger.confirmed")));
		}
		if (editConfigs.isTriggerScore()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_SCORE, translate("config.trigger.score")));
		}
		if (editConfigs.isTriggerPassed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_PASSED, translate("config.trigger.passed")));
		}
		
		LearningPathTranslations translations = editConfigs.getTranslations();
		if (editConfigs.isTriggerStatusInReview()) {
			String translation = translations.getTriggerStatusInReview(getLocale()) != null
					? translations.getTriggerStatusInReview(getLocale())
					: translate("config.trigger.status.in.review");
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_IN_REVIEW, translation));
		}
		
		if (editConfigs.isTriggerStatusDone()) {
			String translation = translations.getTriggerStatusDone(getLocale()) != null
					? translations.getTriggerStatusDone(getLocale())
					: translate("config.trigger.status.done");
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_DONE, translation));
		}
		return triggerKV;
	}
	
	private void updateUI() {
		AssessmentObligation obligation = obligationEl.isOneSelected()
				? AssessmentObligation.valueOf(obligationEl.getSelectedKey())
				: LearningPathConfigs.OBLIGATION_DEFAULT;
		boolean obligationMandatory = AssessmentObligation.mandatory.equals(obligation);
		endDateEl.setVisible(obligationMandatory);
				
		durationEl.setMandatory(isDurationMandatory());
		
		boolean triggerScore = triggerEl.isOneSelected() && triggerEl.getSelectedKey().equals(CONFIG_VALUE_TRIGGER_SCORE);
		scoreCutEl.setVisible(triggerScore);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == obligationEl) {
			updateUI();
		} else if (source == triggerEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateInteger(durationEl, 1, 10000, isDurationMandatory(), "error.positiv.int");
		allOk &= validateInteger(scoreCutEl, 0, 10000, true, "error.positiv.int");
		
		if (startDateEl.getDate() != null && endDateEl.getDate() != null) {
			Date start = startDateEl.getDate();
			Date end = endDateEl.getDate();
			if(end.before(start)) {
				endDateEl.setErrorKey("error.start.after.end", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max, boolean mandatory, String i18nKey) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						allOk = false;
					} else if(max < value) {
						allOk = false;
					}
				} catch (NumberFormatException e) {
					allOk = false;
				}
			} else if (mandatory) {
				allOk = false;
			}
		}
		if (!allOk) {
			el.setErrorKey(i18nKey, null);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AssessmentObligation obligation = obligationEl.isOneSelected()
				? AssessmentObligation.valueOf(obligationEl.getSelectedKey())
				: LearningPathConfigs.OBLIGATION_DEFAULT;
		learningPathConfigs.setObligation(obligation);
		
		Date startDate = startDateEl.getDate();
		learningPathConfigs.setStartDate(startDate);
		
		if (!endDateEl.isVisible()) {
			endDateEl.setValue(null);
		}
		Date endDate = endDateEl.getDate();
		learningPathConfigs.setEndDate(endDate);
		
		Integer duration = StringHelper.containsNonWhitespace(durationEl.getValue())
				? Integer.valueOf(durationEl.getValue())
				: null;
		learningPathConfigs.setDuration(duration);
		
		FullyAssessedTrigger trigger = triggerEl.isOneSelected()
				? FullyAssessedTrigger.valueOf(triggerEl.getSelectedKey())
				: LearningPathConfigs.TRIGGER_DEFAULT;
		learningPathConfigs.setFullyAssessedTrigger(trigger);
		
		Integer score = scoreCutEl.isVisible()? Integer.valueOf(scoreCutEl.getValue()): null;
		learningPathConfigs.setScoreTriggerValue(score);
		
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private boolean isDurationMandatory() {
		return CompletionType.duration.equals(courseConfig.getCompletionType())
				&& obligationEl.isOneSelected()
				&& AssessmentObligation.mandatory.name().equals(obligationEl.getSelectedKey());
	}
	
}

