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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathNodeConfigController extends FormBasicController {	

	public static final String CONFIG_KEY_DURATION = "duration";
	public static final String CONFIG_KEY_OBLIGATION = "obligation";
	public static final String CONFIG_DEFAULT_OBLIGATION = AssessmentObligation.mandatory.name();
	public static final String CONFIG_KEY_TRIGGER = "fully.assessed.trigger";
	public static final String CONFIG_VALUE_TRIGGER_NONE = "none";
	public static final String CONFIG_VALUE_TRIGGER_NODE_VISITED = "nodeVisited";
	public static final String CONFIG_VALUE_TRIGGER_CONFIRMED = "confirmed";
	public static final String CONFIG_VALUE_TRIGGER_STATUS_DONE = "statusDone";
	public static final String CONFIG_DEFAULT_TRIGGER = CONFIG_VALUE_TRIGGER_NONE;
	
	private TextElement durationEl;
	private SingleSelection obligationEl;
	private SingleSelection triggerEl;

	private final CourseConfig courseConfig;
	private final ModuleConfiguration moduleConfigs;
	private final LearningPathControllerConfig ctrlConfig;

	public LearningPathNodeConfigController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			ModuleConfiguration moduleConfig, LearningPathControllerConfig ctrlConfig) {
		super(ureq, wControl);
		this.courseConfig = CourseFactory.loadCourse(courseEntry).getCourseConfig();
		this.moduleConfigs = moduleConfig;
		this.ctrlConfig = ctrlConfig;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String estimatedTime = moduleConfigs.getStringValue(CONFIG_KEY_DURATION);
		durationEl = uifactory.addTextElement("config.duration", 128, estimatedTime , formLayout);
		
		KeyValues obligationKV = new KeyValues();
		obligationKV.add(entry(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		obligationKV.add(entry(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", formLayout, obligationKV.keys(), obligationKV.values());
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		String obligationKey = moduleConfigs.getStringValue(CONFIG_KEY_OBLIGATION, CONFIG_DEFAULT_OBLIGATION);
		if (Arrays.asList(obligationEl.getKeys()).contains(obligationKey)) {
			obligationEl.select(obligationKey, true);
		}
		
		KeyValues triggerKV = getTriggerKV();
		triggerEl = uifactory.addRadiosVertical("config.trigger", formLayout,
				triggerKV.keys(), triggerKV.values());
		triggerEl.addActionListener(FormEvent.ONCHANGE);
		String triggerKey = moduleConfigs.getStringValue(CONFIG_KEY_TRIGGER, CONFIG_DEFAULT_TRIGGER);
		if (Arrays.asList(triggerEl.getKeys()).contains(triggerKey)) {
			triggerEl.select(triggerKey, true);
		}
		
		uifactory.addFormSubmitButton("save", formLayout);
		
		updateUI();
	}

	private KeyValues getTriggerKV() {
		KeyValues triggerKV = new KeyValues();
		triggerKV.add(entry(CONFIG_VALUE_TRIGGER_NONE, translate("config.trigger.none")));
		if (ctrlConfig.isTriggerNodeVisited()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_NODE_VISITED, translate("config.trigger.visited")));
		}
		if (ctrlConfig.isTriggerConfirmed()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_CONFIRMED, translate("config.trigger.confirmed")));
		}
		TranslateableBoolean triggerStatusDone = ctrlConfig.getTriggerStatusDone();
		if (triggerStatusDone.isTrue()) {
			triggerKV.add(entry(CONFIG_VALUE_TRIGGER_STATUS_DONE,
					getTranslationOrDefault(triggerStatusDone, "config.trigger.status.done")));
		}
		return triggerKV;
	}
	
	private String getTranslationOrDefault(TranslateableBoolean trans, String defaulI18nKey) {
		return trans.isTranslated()
				? trans.getMessage()
				: translate(defaulI18nKey);
	}
	
	private void updateUI() {
		durationEl.setMandatory(isDurationMandatory());
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == obligationEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allOk = validateInteger(durationEl, 1, 10000, isDurationMandatory(), "error.positiv.int");
		
		return allOk & super.validateFormLogic(ureq);
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
		String estimatedTime = durationEl.getValue();
		moduleConfigs.setStringValue(CONFIG_KEY_DURATION, estimatedTime);
		
		String obligation = obligationEl.isOneSelected()
				? obligationEl.getSelectedKey()
				: CONFIG_DEFAULT_OBLIGATION;
		moduleConfigs.setStringValue(CONFIG_KEY_OBLIGATION, obligation);
		
		String trigger = triggerEl.isOneSelected()
				? triggerEl.getSelectedKey()
				: CONFIG_DEFAULT_TRIGGER;
		moduleConfigs.setStringValue(CONFIG_KEY_TRIGGER, trigger);
		
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
	
	public interface LearningPathControllerConfig {
		
		public boolean isTriggerNodeVisited();
		
		public boolean isTriggerConfirmed();
		
		public TranslateableBoolean getTriggerStatusDone();
		
	}
	
	public static ControllerConfigBuilder builder() {
		return new ControllerConfigBuilder();
	}
	
	public static class ControllerConfigBuilder {
		
		private boolean triggerNodeVisited;
		private boolean triggerConfirmed;
		private TranslateableBoolean triggerStatusDone;
		
		private ControllerConfigBuilder() {
		}
		
		public ControllerConfigBuilder enableNodeVisited() {
			triggerNodeVisited = true;
			return this;
		}
		
		public ControllerConfigBuilder enableStatusDone() {
			triggerStatusDone = TranslateableBoolean.untranslatedTrue();
			return this;
		}
		
		public ControllerConfigBuilder enableStatusDone(String message) {
			triggerStatusDone = TranslateableBoolean.translatedTrue(message);
			return this;
		}
		
		public LearningPathControllerConfig build() {
			return new ControllerConfigImpl(this);
		}
		
		private final static class ControllerConfigImpl implements LearningPathControllerConfig {
			
			public final boolean triggerNodeVisited;
			public final boolean triggerConfirmed;
			public final TranslateableBoolean triggerStatusDone;

			public ControllerConfigImpl(ControllerConfigBuilder builder) {
				this.triggerNodeVisited = builder.triggerNodeVisited;
				this.triggerConfirmed = builder.triggerConfirmed;
				this.triggerStatusDone = falseIfNull(builder.triggerStatusDone);
			}
			
			private TranslateableBoolean falseIfNull(TranslateableBoolean translateableBoolean) {
				return translateableBoolean != null
						? translateableBoolean
						: TranslateableBoolean.untranslatedFalse();
			}

			@Override
			public boolean isTriggerNodeVisited() {
				return triggerNodeVisited;
			}

			@Override
			public boolean isTriggerConfirmed() {
				return triggerConfirmed;
			}

			@Override
			public TranslateableBoolean getTriggerStatusDone() {
				return triggerStatusDone;
			}
			
		}
		
	}
	
}

