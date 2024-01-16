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
package org.olat.course.nodes.survey.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Util;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.ui.NodeRightsController;
import org.olat.course.nodes.survey.SurveyModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Jan 15, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SurveyDefaultsEditController extends FormBasicController {

	private FormLink resetDefaultsButton;
	private FormLink backLink;
	private final List<MultipleSelectionElement> rolesElList = new ArrayList<>();

	private DialogBoxController confirmReset;

	@Autowired
	private SurveyModule surveyModule;


	public SurveyDefaultsEditController(UserRequest ureq, WindowControl wControl, String title) {
		super(ureq, wControl, "survey_def_conf", Util.createPackageTranslator(NodeRightsController.class, ureq.getLocale()));
		flc.contextPut("title", title);
		initForm(ureq);
		loadDefaultConfigValues();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		resetDefaultsButton = uifactory.addFormLink("reset", "course.node.reset.defaults", null, formLayout, Link.BUTTON);
		resetDefaultsButton.setElementCssClass("o_sel_cal_delete pull-right");
		backLink = uifactory.addFormLink("back", flc);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		FormLayoutContainer surveyCont = FormLayoutContainer.createDefaultFormLayout("surveyCont", getTranslator());
		surveyCont.setFormTitle(translate("form.title"));
		surveyCont.setRootForm(mainForm);
		formLayout.add(surveyCont);

		initNodeRights(surveyModule.getExecutionNodeRightType(), surveyCont);
		initNodeRights(surveyModule.getReportNodeRightType(), surveyCont);

		// Create submit button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		surveyCont.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout)
				.setElementCssClass("o_sel_node_editor_submit");
	}

	private void initNodeRights(NodeRightType nodeRightType, FormLayoutContainer surveyCont) {
		Collection<NodeRightRole> roles = nodeRightType.getRoles();

		SelectionValues rolesKV = new SelectionValues();
		addRole(rolesKV, roles, NodeRightRole.owner);
		addRole(rolesKV, roles, NodeRightRole.coach);
		addRole(rolesKV, roles, NodeRightRole.participant);
		addRole(rolesKV, roles, NodeRightRole.guest);

		String name = "nr_role_" + CodeHelper.getRAMUniqueID();
		MultipleSelectionElement rolesEl = uifactory.addCheckboxesVertical(name, nodeRightType.getI18nKey(), surveyCont, rolesKV.keys(), rolesKV.values(), null, 1);
		rolesEl.setUserObject(nodeRightType);

		rolesElList.add(rolesEl);
	}

	private void loadDefaultConfigValues() {
		for (MultipleSelectionElement rolesEl : rolesElList) {
			NodeRightType nodeRightType = (NodeRightType) rolesEl.getUserObject();
			if (nodeRightType.getIdentifier().equals(surveyModule.getExecutionNodeRightType().getIdentifier())) {
				rolesEl.select(NodeRightRole.owner.name(), surveyModule.isExecutionOwner());
				rolesEl.select(NodeRightRole.coach.name(), surveyModule.isExecutionCoach());
				rolesEl.select(NodeRightRole.participant.name(), surveyModule.isExecutionParticipant());
				rolesEl.select(NodeRightRole.guest.name(), surveyModule.isExecutionGuest());
			} else if (nodeRightType.getIdentifier().equals(surveyModule.getReportNodeRightType().getIdentifier())) {
				rolesEl.select(NodeRightRole.owner.name(), surveyModule.isReportOwner());
				rolesEl.select(NodeRightRole.coach.name(), surveyModule.isReportCoach());
				rolesEl.select(NodeRightRole.participant.name(), surveyModule.isReportParticipant());
				rolesEl.select(NodeRightRole.guest.name(), surveyModule.isReportGuest());
			}
		}
	}

	private void addRole(SelectionValues rolesKV, Collection<NodeRightRole> roles, NodeRightRole role) {
		if (roles.contains(role)) {
			rolesKV.add(SelectionValues.entry(role.name(), translateRole(role)));
		}
	}

	private String translateRole(NodeRightRole role) {
		return translate("role." + role.name().toLowerCase());
	}

	private void updateDefaultConfigValues() {
		for (MultipleSelectionElement element : rolesElList) {
			NodeRightType nodeRightType = (NodeRightType) element.getUserObject();

			// set values in module config
			if (nodeRightType.getIdentifier().equals(surveyModule.getExecutionNodeRightType().getIdentifier())) {
				surveyModule.setExecutionOwner(element.isKeySelected(NodeRightRole.owner.name()));
				surveyModule.setExecutionCoach(element.isKeySelected(NodeRightRole.coach.name()));
				surveyModule.setExecutionParticipant(element.isKeySelected(NodeRightRole.participant.name()));
				surveyModule.setExecutionGuest(element.isKeySelected(NodeRightRole.guest.name()));
			} else if (nodeRightType.getIdentifier().equals(surveyModule.getReportNodeRightType().getIdentifier())) {
				surveyModule.setReportOwner(element.isKeySelected(NodeRightRole.owner.name()));
				surveyModule.setReportCoach(element.isKeySelected(NodeRightRole.coach.name()));
				surveyModule.setReportParticipant(element.isKeySelected(NodeRightRole.participant.name()));
				surveyModule.setReportGuest(element.isKeySelected(NodeRightRole.guest.name()));
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == resetDefaultsButton) {
			confirmReset = activateYesNoDialog(ureq, null, translate("course.node.confirm.reset"), confirmReset);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updateDefaultConfigValues();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmReset) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				surveyModule.resetProperties();
				loadDefaultConfigValues();
			}
			// Fire this event regardless of yes, no or close
			// Little hack to prevent a dirty form after pressing reset button
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}
