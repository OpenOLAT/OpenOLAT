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

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumAutomationConfig;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Segment content for the per-element automation configuration (Adopt from type / Override).
 * Embeds CurriculumAutomationController as a nested child.
 *
 * Initial date: 2026-06-30<br>
 * @author uhensler, https://www.frentix.com
 */
public class EditCurriculumElementAutomationController extends FormBasicController {

	private static final String CONFIG_ADOPT = "adopt";
	private static final String CONFIG_OVERRIDE = "override";

	private SingleSelection configEl;
	private CurriculumAutomationController automationCtrl;

	private CurriculumElement element;
	private List<CurriculumAutomationConfig> elementConfigs;
	private List<CurriculumAutomationConfig> typeConfigs;

	@Autowired
	private CurriculumAutomationService automationService;
	@Autowired
	private CurriculumService curriculumService;

	public EditCurriculumElementAutomationController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.element = element;
		initForm(ureq);
	}

	public CurriculumElement getCurriculumElement() {
		return element;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer wrapperCont = FormLayoutContainer.createDefaultFormLayout("automationWrapper", getTranslator());
		wrapperCont.setRootForm(mainForm);
		wrapperCont.setFormTitle(translate("curriculum.element.automation"));
		Date nextExecution = automationService.getNextExecutionTime();
		String time = nextExecution != null
				? Formatter.getInstance(getLocale()).formatTimeShort(nextExecution)
				: "-";
		wrapperCont.setFormInfo(translate("automation.config.inherited.info")
				+ " " + translate("automation.config.run.time.info", time));
		formLayout.add(wrapperCont);

		CurriculumElementType type = element != null ? element.getType() : null;
		typeConfigs = type != null ? automationService.getConfigs(type) : null;
		SelectionValues configSV = new SelectionValues();
		String adoptLabel = buildAdoptLabel(type);
		configSV.add(SelectionValues.entry(CONFIG_ADOPT, adoptLabel));
		configSV.add(SelectionValues.entry(CONFIG_OVERRIDE, translate("automation.config.override")));
		configEl = uifactory.addRadiosHorizontal("automation.config", wrapperCont, configSV.keys(), configSV.values());
		configEl.addActionListener(FormEvent.ONCHANGE);
		elementConfigs = element != null ? automationService.getConfigs(element) : null;
		boolean hasOverride = hasRules(elementConfigs);
		configEl.select(hasOverride ? CONFIG_OVERRIDE : CONFIG_ADOPT, true);

		List<CurriculumAutomationConfig> initialConfig = resolveInitialConfig(hasOverride);
		AutomationFormConfig cfg = new AutomationFormConfig(
				resolveForUseAs(type),
				false,
				true,
				initialConfig,
				true,
				null,
				element);
		automationCtrl = new CurriculumAutomationController(ureq, getWindowControl(), mainForm, cfg);
		listenTo(automationCtrl);
		formLayout.add(automationCtrl.getInitialFormItem());

		updateAutomationMode(hasOverride);
	}

	private String buildAdoptLabel(CurriculumElementType type) {
		if (type == null) {
			return "-";
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(StringHelper.escapeHtml(type.getDisplayName()));
		if (StringHelper.containsNonWhitespace(type.getIdentifier())) {
			sb.append(" · <span class=\"text-muted o_small\">").append(StringHelper.escapeHtml(type.getIdentifier()));
			sb.append("</span>");
		}
		
		return translate("option.adopt", sb.toString(),
				hasRules(typeConfigs) ? translate("on"): translate("off"));
	}

	private List<CurriculumAutomationConfig> resolveInitialConfig(boolean hasOverride) {
		if (hasOverride) {
			return elementConfigs;
		}
		return typeConfigs;
	}

	private void updateAutomationMode(boolean override) {
		if (override) {
			List<CurriculumAutomationConfig> config = elementConfigs;
			if (!hasRules(config)) {
				config = typeConfigs;
			}
			if (!hasRules(config)) {
				config = defaultAutomationConfig();
			}
			automationCtrl.setAutomationConfig(config, true);
		} else {
			automationCtrl.setAutomationConfig(typeConfigs, true);
		}
		automationCtrl.setReadOnly(!override);
	}

	private String resolveForUseAs(CurriculumElementType type) {
		if (type == null || type.isImplOnly()) {
			return EditCurriculumElementTypeController.FOR_USE_AS_IMPL;
		}
		if (!type.isAllowedAsRootElement()) {
			return EditCurriculumElementTypeController.FOR_USE_AS_ELEM;
		}
		return EditCurriculumElementTypeController.FOR_USE_AS_IMPL_OR_ELEM;
	}

	private boolean hasRules(List<CurriculumAutomationConfig> config) {
		return config != null && !config.isEmpty();
	}

	private List<CurriculumAutomationConfig> defaultAutomationConfig() {
		int maxRelations = element != null && element.getType() != null
				? element.getType().getMaxRepositoryEntryRelations() : -1;
		return automationService.getDefaultConfig(true, maxRelations);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == automationCtrl && event == FormEvent.CHANGED_EVENT) {
			doSave(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (configEl == source) {
			boolean override = CONFIG_OVERRIDE.equals(configEl.getSelectedKey());
			updateAutomationMode(override);
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSave(UserRequest ureq) {
		boolean override = CONFIG_OVERRIDE.equals(configEl.getSelectedKey());
		element = curriculumService.getCurriculumElement(element);
		List<CurriculumAutomationConfig> configs = override ? automationCtrl.getAutomationConfig() : List.of();
		elementConfigs = automationService.updateConfigs(element, configs);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
