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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentContentEditController extends FormBasicController {

	private ProjAppointmentContentEditForm appointmentContentEditForm;

	private final ProjProject project;
	private final boolean template;
	private ProjAppointment appointment;
	private final Date initialStartDate;
	
	@Autowired
	private ProjectService projectService;

	public ProjAppointmentContentEditController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjProject project,  boolean template, ProjAppointment appointment, Date initialStartDate) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.project = project;
		this.template = template;
		this.appointment = appointment;
		this.initialStartDate = initialStartDate;
		
		initForm(ureq);
	}
	
	public ProjAppointment getAppointment() {
		return appointment;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<TagInfo> tagInfos = projectService.getTagInfos(project, appointment != null? appointment.getArtefact(): null);
		
		Date startDate = appointment != null? appointment.getStartDate(): initialStartDate;
		appointmentContentEditForm = new ProjAppointmentContentEditForm(ureq, getWindowControl(), mainForm, appointment, template, tagInfos, startDate);
		listenTo(appointmentContentEditForm);
		formLayout.add(appointmentContentEditForm.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (appointment == null) {
			appointment = projectService.createAppointment(getIdentity(), project, appointmentContentEditForm.getStartDate());
		}
		
		projectService.updateAppointment(getIdentity(), appointment, 
				appointmentContentEditForm.getStartDate(),
				appointmentContentEditForm.getEndDate(),
				appointmentContentEditForm.getSubject(),
				appointmentContentEditForm.getDescription(),
				appointmentContentEditForm.getLocation(),
				appointmentContentEditForm.getColor(),
				appointmentContentEditForm.isAllDay(),
				appointmentContentEditForm.getRecurrenceRule()
				);
		
		projectService.updateTags(getIdentity(), appointment.getArtefact(), appointmentContentEditForm.getTagDisplayValues());
	}

}
