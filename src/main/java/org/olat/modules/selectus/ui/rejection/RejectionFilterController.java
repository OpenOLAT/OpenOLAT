/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.MailCenterExcludeOption;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 12 janv. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RejectionFilterController extends StepFormBasicController {

	private MultipleSelectionElement statusEl;
	private MultipleSelectionElement includeEl;
	private MultipleSelectionElement excludeEl;
	
	private boolean changed = true;
	private final EmailVariables emailVar;
	private final MailCenterExcludeOption excludeOptions;

	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public RejectionFilterController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form, EmailVariables emailVar) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		excludeOptions = recruitingModule.getMailCenterExclusionOption();
		this.emailVar = emailVar;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormInfo("rejection.include.info");
		
		String[] includeKeys = new String[]{ "3", "2", "1", "no" };
		String[] includeValues = new String[] {
				translate("rejection.3"), translate("rejection.2"), translate("rejection.1"), translate("rejection.no.decision")
			};
		includeEl = uifactory.addCheckboxesVertical("rejection.include", "rejection.include", formLayout, includeKeys, includeValues, 1);
		includeEl.setElementCssClass("o_sel_rejection_include_filter");
		includeEl.addActionListener(FormEvent.ONCHANGE);
		
		uifactory.addSpacerElement("status.spacer", formLayout, false);
		
		SelectionValues statusKeysValues = new SelectionValues();
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		for(ApplicationStatus appStatus:availableStatus) {
			statusKeysValues.add(SelectionValues.entry(appStatus.name(), translate("application.status.".concat(appStatus.name()))));
		}
		statusEl = uifactory.addCheckboxesVertical("status", null, formLayout, statusKeysValues.keys(), statusKeysValues.values(), 1);
		statusEl.setElementCssClass("o_sel_rejection_status_filter");
		statusEl.addActionListener(FormEvent.ONCHANGE);
		statusEl.select(ApplicationStatus.active.name(), true);
		
		SelectionValues excludeKeyValues = new SelectionValues();
		if(excludeOptions == MailCenterExcludeOption.alreadySent) {
			excludeKeyValues.add(SelectionValues.entry("x-send-x", translate("rejection.send")));
		} else if(excludeOptions == MailCenterExcludeOption.templates) {
			SelectionValues templatesKeys = emailVar.getTemplatesKeyValues();
			templatesKeys.remove("-");
			String label = translate("exclusion.filter.template", new String[] { translate("filter.no.template") });
			excludeKeyValues.add(SelectionValues.entry("-", label));
			for(SelectionValue keyValue:templatesKeys.keyValues()) {
				String val = translate("exclusion.filter.template", new String[] { keyValue.getValue() });
				excludeKeyValues.add(SelectionValues.entry(keyValue.getKey(), val));
			}
		}
	
		if(!excludeKeyValues.isEmpty()) {
			uifactory.addSpacerElement("exclude.spacer", formLayout, false);
		}
		
		excludeEl = uifactory.addCheckboxesVertical("rejection.exclude", "rejection.exclude", formLayout,
				excludeKeyValues.keys(), excludeKeyValues.values(), 1);
		excludeEl.setElementCssClass("o_sel_rejection_exclude_filter");
		excludeEl.setVisible(!excludeKeyValues.isEmpty());
		excludeEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(includeEl == source || excludeEl == source || statusEl == source) {
			changed = true;
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(changed) {
			Position position = emailVar.getPosition();
			Collection<String> selectedKeys = includeEl.getSelectedKeys();
			boolean noDecision = false;
			List<Integer> decisions = new ArrayList<>();
			List<String> applicantsGroups = new ArrayList<>();
			for(String selectedKey:selectedKeys) {
				if("no".equals(selectedKey)) {
					noDecision = true;
					applicantsGroups.add(translate("rejection.no.decision"));
				} else {
					decisions.add(Integer.valueOf(selectedKey));
					applicantsGroups.add(translate("rejection." + selectedKey));
				}
			}
			
			Collection<String> selectedStatus = statusEl.getSelectedKeys();
			List<ApplicationStatus> status = selectedStatus.stream()
					.map(ApplicationStatus::valueOf).collect(Collectors.toList());
			
			List<String> exclusions;
			boolean excludeSendRejection;
			Collection<String> selectedExclusions = excludeEl.getSelectedKeys();
			if(excludeOptions == MailCenterExcludeOption.alreadySent) {
				excludeSendRejection = selectedExclusions.contains("x-send-x");
				exclusions = Collections.emptyList();
			} else if(excludeOptions == MailCenterExcludeOption.templates) {
				excludeSendRejection = false;
				exclusions = new ArrayList<>(selectedExclusions);
			} else {
				excludeSendRejection = false;
				exclusions = Collections.emptyList();
			}
			
			List<ApplicationLight> apps = recruitingService.getApplicationsWithDecisions(position, decisions, status, noDecision, exclusions, excludeSendRejection);
			emailVar.setRows(apps);
			emailVar.setShowDecisions(true);
			emailVar.setApplicationsGroups(applicantsGroups);
			changed = false;
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}