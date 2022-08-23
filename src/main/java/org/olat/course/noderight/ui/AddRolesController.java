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
package org.olat.course.noderight.ui;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightType;

/**
 * 
 * Initial date: 30 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddRolesController extends FormBasicController {

	private MultipleSelectionElement rolesEl;
	private DateChooser startEl;
	private DateChooser endEl;

	private final NodeRightType type;
	private final NodeRight nodeRight;


	public AddRolesController(UserRequest ureq, WindowControl wControl, NodeRightType type, NodeRight nodeRight) {
		super(ureq, wControl);
		this.type = type;
		this.nodeRight = nodeRight;
		
		initForm(ureq);
	}
	
	public NodeRight getNodeRight() {
		return nodeRight;
	}

	public List<NodeRightRole> getRoles() {
		return rolesEl.getSelectedKeys().stream()
				.map(NodeRightRole::valueOf)
				.collect(Collectors.toList());
	}
	
	public Date getStart() {
		return startEl.getDate();
	}
	
	public Date getEnd() {
		return endEl.getDate();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues rolesKV = new SelectionValues();
		addRole(rolesKV, NodeRightRole.owner);
		addRole(rolesKV, NodeRightRole.coach);
		addRole(rolesKV, NodeRightRole.participant);
		addRole(rolesKV, NodeRightRole.guest);
		rolesEl = uifactory.addCheckboxesVertical("role", formLayout, rolesKV.keys(), rolesKV.values(), null, 1);
		
		startEl = uifactory.addDateChooser("grant.start", null, formLayout);
		startEl.setDateChooserTimeEnabled(true);
		startEl.setValidDateCheck("form.error.date");
		
		endEl = uifactory.addDateChooser("grant.end", null, formLayout);
		endEl.setDateChooserTimeEnabled(true);
		endEl.setValidDateCheck("form.error.date");
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	private void addRole(SelectionValues rolesKV, NodeRightRole role) {
		if (type.getRoles().contains(role)) {
			rolesKV.add(SelectionValues.entry(role.name(), translateRole(role)));
		}
	}
	
	private String translateRole(NodeRightRole role) {
		return translate("role." + role.name().toLowerCase());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		endEl.clearError();
		Date start = startEl.getDate();
		Date to = endEl.getDate();
		if(!validateFormItem(startEl) || !validateFormItem(endEl)) {
			allOk &= false;
		} else if (start != null && to != null && start.after(to)) {
			endEl.setErrorKey("error.end.after.start", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
