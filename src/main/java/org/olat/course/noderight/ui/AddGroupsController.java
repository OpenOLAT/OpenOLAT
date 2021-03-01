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

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.noderight.NodeRight;

/**
 * 
 * Initial date: 22 Feb 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddGroupsController extends FormBasicController {

	private DateChooser startEl;
	private DateChooser endEl;
	
	private GroupSelectionController groupSelectionController;
	
	private final CourseGroupManager courseGroupManager;
	private final NodeRight nodeRight;

	public AddGroupsController(UserRequest ureq, WindowControl wControl, CourseGroupManager courseGroupManager,
			NodeRight nodeRight) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.courseGroupManager = courseGroupManager;
		this.nodeRight = nodeRight;
		
		initForm(ureq);
	}
	
	public NodeRight getNodeRight() {
		return nodeRight;
	}
	
	public Date getStart() {
		return startEl.getDate();
	}
	
	public Date getEnd() {
		return endEl.getDate();
	}
	
	public Collection<Long> getGroupKeys() {
		return groupSelectionController.getSelectedKeys();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer dateCont = FormLayoutContainer.createDefaultFormLayout("dates", getTranslator());
		formLayout.add(dateCont);
		
		startEl = uifactory.addDateChooser("grant.start", null, dateCont);
		startEl.setDateChooserTimeEnabled(true);
		
		endEl = uifactory.addDateChooser("grant.end", null, dateCont);
		endEl.setDateChooserTimeEnabled(true);
		
		groupSelectionController = new GroupSelectionController(ureq, getWindowControl(), mainForm, false,
				courseGroupManager, Collections.emptyList());
		formLayout.add(groupSelectionController.getInitialFormItem());
		
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("buttonsWrapper", getTranslator());
		formLayout.add(buttonsWrapperCont);
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		endEl.clearError();
		Date start = startEl.getDate();
		Date to = endEl.getDate();
		if (start != null && to != null && start.after(to)) {
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

	@Override
	protected void doDispose() {
		//
	}

}
