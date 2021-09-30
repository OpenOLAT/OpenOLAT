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
package org.olat.course.learningpath.obligation;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.learningpath.obligation.PassedExceptionalObligationHandler.Status;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.rule.PassedRuleSPI;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PassedExceptionalObligationController extends FormBasicController
		implements ExceptionalObligationController {
	
	private SingleSelection courseNodeEl;
	private MultipleSelectionElement statusEl;
	
	private final List<CourseNode> passeableCourseNodes;
	
	public PassedExceptionalObligationController(UserRequest ureq, WindowControl wControl, List<CourseNode> passeableCourseNodes) {
		super(ureq, wControl, "passed_add");
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.passeableCourseNodes = passeableCourseNodes;
		
		initForm(ureq);
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		String courseNodeIdent = courseNodeEl.getSelectedKey();
		String status = statusEl.getSelectedKeys().stream().collect(Collectors.joining(Status.DELIMITER));
		
		PassedExceptionalObligation exceptionalObligation = new PassedExceptionalObligation();
		exceptionalObligation.setType(PassedExceptionalObligationHandler.TYPE);
		exceptionalObligation.setCourseNodeIdent(courseNodeIdent);
		exceptionalObligation.setStatus(status);
		return Collections.singletonList(exceptionalObligation);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (passeableCourseNodes.isEmpty()) {
			EmptyState emptyState = EmptyStateFactory.create("empty.state", flc.getFormItemComponent(), this);
			emptyState.setIconCss("o_icon o_icon-fw o_st_icon");
			emptyState.setMessageI18nKey("error.no.passeable");
		} else {
			FormLayoutContainer configCont = FormLayoutContainer.createDefaultFormLayout("configs", getTranslator());
			configCont.setRootForm(mainForm);
			formLayout.add(configCont);
			
			SelectionValues courseNodeKV = new SelectionValues();
			passeableCourseNodes.forEach(courseNode ->
					courseNodeKV.add(new SelectionValue(
							courseNode.getIdent(),
							courseNode.getShortTitle() + " (" + courseNode.getIdent() + ")"
						)));
			courseNodeEl = uifactory.addDropdownSingleselect("config.exceptional.obligation.course.node", configCont, 
					courseNodeKV.keys(), courseNodeKV.values());
			courseNodeEl.setMandatory(true);
			
			SelectionValues statusSV = new SelectionValues();
			statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.gradedPassed.name(), translate("exceptional.obligation.passed")));
			statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.gradedFailed.name(), translate("exceptional.obligation.failed")));
			statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.notGraded.name(), translate("exceptional.obligation.not.graded")));
			statusEl = uifactory.addCheckboxesHorizontal("status", "config.exceptional.obligation.passed.status",
					configCont, statusSV.keys(), statusSV.values());
			statusEl.setMandatory(true);
		}
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group_right o_block_top");
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if (!passeableCourseNodes.isEmpty()) {
			uifactory.addFormSubmitButton("config.exceptional.obligation.add.button", buttonCont);
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk =  super.validateFormLogic(ureq);
		
		if (courseNodeEl != null) {
			courseNodeEl.clearError();
			if (!courseNodeEl.isOneSelected()) {
				courseNodeEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		if (statusEl != null) {
			statusEl.clearError();
			if (!statusEl.isAtLeastSelected(1)) {
				statusEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if (statusEl.isAtLeastSelected(3)) {
				statusEl.setErrorKey("error.passed.status.selction", null);
				allOk &= false;
			}
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

	@Override
	protected void doDispose() {
		//
	}

}
