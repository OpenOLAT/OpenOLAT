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
package org.olat.modules.forms.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.CoachCandidates.Role;
import org.olat.modules.forms.model.xml.CoachInformations;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Dec 19, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class CoachInfoInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	
	private SingleSelection roleEl;
	private SingleSelection obligationEl;
	
	private final CoachInformations coachInfo;
	private final boolean restrictedEdit;

	@Autowired
	private ColorService colorService;

	public CoachInfoInspectorController(UserRequest ureq, WindowControl wControl, CoachInformations coachInfo, boolean restrictedEdit) {
		super(ureq, wControl, "coach_info_inspector");
		this.coachInfo = coachInfo;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.formcoachdetails");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);
		
		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}
	
	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);
		
		SelectionValues roleSV = new SelectionValues();
		roleSV.add(SelectionValues.entry(Role.coach.name(), translate("coach.information.role.coaches")));
		roleSV.add(SelectionValues.entry(Role.owner.name(), translate("coach.information.role.coaches.owner")));
		roleEl = uifactory.addRadiosVertical("gi_r_" + CodeHelper.getRAMUniqueID(), "coach.information.role", layoutCont,
				roleSV.keys(), roleSV.values());
		String selectedRole = coachInfo.getRoles() != null && coachInfo.getRoles().contains(Role.owner)
				? Role.owner.name()
				: Role.coach.name();
		roleEl.select(selectedRole, true);
		roleEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues obligationSV = new SelectionValues();
		obligationSV.add(SelectionValues.entry(Obligation.optional.name(), translate("obligation.optional.empty")));
		obligationSV.add(SelectionValues.entry(Obligation.mandatory.name(), translate("obligation.mandatory.editable")));
		obligationSV.add(SelectionValues.entry(Obligation.autofill.name(), translate("obligation.mandatory.not.editable")));
		obligationEl = uifactory.addRadiosVertical("gi_m_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
				obligationSV.keys(), obligationSV.values());
		String selectedObligation = coachInfo.getObligation() != null
				? coachInfo.getObligation().name()
				: Obligation.optional.name();
		obligationEl.select(selectedObligation, true);
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, getLocale());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, getLayoutSettings(), velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings() {
		if (coachInfo.getLayoutSettings() != null) {
			return coachInfo.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (coachInfo.getAlertBoxSettings() != null) {
			return coachInfo.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == roleEl || source == obligationEl) {
			doSave(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
		fireEvent(ureq, new ClosePartEvent(coachInfo));
	}

	private void doSave(UserRequest ureq) {
		List<Role> roles = new ArrayList<>(2);
		roles.add(Role.coach);
		if (roleEl.isOneSelected() && roleEl.isKeySelected(Role.owner.name())) {
			roles.add(Role.owner);
		}
		coachInfo.setRoles(roles);
		
		Obligation sselectedObligation = obligationEl.isOneSelected()
				? Obligation.valueOf(obligationEl.getSelectedKey())
				: Obligation.optional;
		coachInfo.setObligation(sselectedObligation);
		
		fireEvent(ureq, new ChangePartEvent(coachInfo));
	}

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		coachInfo.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(coachInfo));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		coachInfo.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(coachInfo));

		getInitialComponent().setDirty(true);
	}
}