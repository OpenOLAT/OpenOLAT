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
package org.olat.course.assessment.bulk;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

/**
 *
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScheduleStepForm extends StepFormBasicController {

	private static final String[] typeKeys = new String[]{ "immediately", "delayed"};

	private DateChooser scheduleDateChooser;
	private SingleSelection scheduleTypeEl;

	public ScheduleStepForm(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form rootForm) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_bulk_assessment_schedule");
		
		setFormTitle("schedule.title");
		setFormDescription("schedule.description");
		setFormContextHelp("Using Course Tools#bulkassessment_schedule");

		String[] typeValues = new String[]{
				translate("schedule.immediately"), translate("schedule.delayed")
		};
		scheduleTypeEl = uifactory.addRadiosVertical("schedule.type", formLayout, typeKeys, typeValues);
		scheduleTypeEl.addActionListener(FormEvent.ONCHANGE);

		boolean delayed;
		Date scheduleDate;
		Task task = (Task)getFromRunContext("task");
		if(task != null && task.getScheduledDate() != null) {
			scheduleDate = task.getScheduledDate();
			delayed = true;
		} else {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			for(int i=Calendar.HOUR; i<Calendar.MILLISECOND; i++) {
				cal.set(i, 0);
			}
			scheduleDate = cal.getTime();
			delayed = false;
		}

		scheduleDateChooser = uifactory.addDateChooser("scheduleDateChooser", "schedule.date", scheduleDate, formLayout);
		scheduleDateChooser.setValidDateCheck("schedule.error.past");
		scheduleDateChooser.setDateChooserTimeEnabled(true);

		scheduleTypeEl.select(delayed ? typeKeys[1]: typeKeys[0], true);
		scheduleDateChooser.setVisible(delayed);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		scheduleTypeEl.clearError();
		if(!scheduleTypeEl.isOneSelected()) {
			scheduleTypeEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		} else if( scheduleTypeEl.isOneSelected() && scheduleTypeEl.isSelected(1)) {
			scheduleDateChooser.clearError();
			Date scheduleDate = scheduleDateChooser.getDate();
			if(scheduleDate == null) {
				scheduleDateChooser.setErrorKey("form.legende.mandatory", null);
				allOk = false;
			} else if(Calendar.getInstance().getTime().compareTo(scheduleDate) > 0) {
				scheduleDateChooser.setErrorKey("schedule.error.past", null);
				allOk = false;
			}
		}

		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(scheduleTypeEl == source) {
			boolean delayed = scheduleTypeEl.isOneSelected() && scheduleTypeEl.isSelected(1);
			scheduleDateChooser.setVisible(delayed);
			flc.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(scheduleTypeEl.isOneSelected() && scheduleTypeEl.isSelected(1)) {
			Date date = scheduleDateChooser.getDate();
			addToRunContext("scheduledDate", date);
		}
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
