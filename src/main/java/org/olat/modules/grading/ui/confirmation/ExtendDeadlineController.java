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
package org.olat.modules.grading.ui.confirmation;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.RepositoryEntryGradingConfiguration;
import org.olat.modules.grading.ui.GradingRepositoryOverviewController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ExtendDeadlineController extends FormBasicController {
	
	private DateChooser deadlineEl;
	
	private List<GradingAssignment> assignments;
	
	@Autowired
	private GradingService gradingService;
	
	public ExtendDeadlineController(UserRequest ureq, WindowControl wControl, List<GradingAssignment> assignments) {
		super(ureq, wControl, Util.createPackageTranslator(GradingRepositoryOverviewController.class, ureq.getLocale()));
		this.assignments = assignments;
		initForm(ureq);
	}
	
	public List<GradingAssignment> getAssignments() {
		return assignments;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		Date extendedDeadline = getLastExtendedDeadline();
		if(extendedDeadline == null) {
			extendedDeadline = getLastAssignmentDate();
		}
		deadlineEl = uifactory.addDateChooser("extend.deadline", extendedDeadline, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		buttonsCont.setRootForm(mainForm);
		
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("tool.extend.deadline", buttonsCont);
	}
	
	private Date getLastExtendedDeadline() {
		Date lastDate = null;
		for(GradingAssignment assignment:assignments) {
			if(assignment.getDeadline() != null
					&& (lastDate == null || assignment.getDeadline().after(lastDate))) {
				lastDate = assignment.getDeadline();
			}
			if(assignment.getExtendedDeadline() != null
					&& (lastDate == null || assignment.getExtendedDeadline().after(lastDate))) {
				lastDate = assignment.getExtendedDeadline();
			}
		}
		return lastDate;
	}
	
	private Date getLastDeadline() {
		Date lastDate = null;
		for(GradingAssignment assignment:assignments) {
			if(assignment.getDeadline() != null
					&& (lastDate == null || assignment.getDeadline().after(lastDate))) {
				lastDate = assignment.getDeadline();
			}
		}
		return lastDate;
	}
	
	private Date getLastAssignmentDate() {
		GradingAssignment lastAssignment = null;
		for(GradingAssignment assignment:assignments) {
			if(assignment.getAssignmentDate() != null
					&& (lastAssignment == null || assignment.getAssignmentDate().after(lastAssignment.getAssignmentDate()))) {
				lastAssignment = assignment;
			}
		}
		
		if(lastAssignment == null) {
			return null;
		}
		
		RepositoryEntryGradingConfiguration config = gradingService.getOrCreateConfiguration(lastAssignment.getReferenceEntry());
		int period = 10;
		if(config != null && config.getGradingPeriod() != null) {
			period = config.getGradingPeriod().intValue();
		}
		return CalendarUtils.addWorkingDays(lastAssignment.getAssignmentDate(), period);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		deadlineEl.clearError();	
		if(deadlineEl.getDate() == null) {
			deadlineEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(deadlineEl.getDate().compareTo(new Date()) <= 0) {
			deadlineEl.setErrorKey("error.date.future", null);
			allOk &= false;
		} else {
			Date lastDeadline = getLastDeadline();
			if(lastDeadline != null && lastDeadline.after(deadlineEl.getDate())) {
				deadlineEl.setErrorKey("error.before.deadline", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(GradingAssignment assignment:assignments) {
			gradingService.extendAssignmentDeadline(assignment, deadlineEl.getDate());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
