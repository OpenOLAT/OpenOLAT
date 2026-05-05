/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AssignmentMethods;
import org.olat.modules.selectus.AssignmentService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.committee.assignment.AddAssignment1CommitteeStep;
import org.olat.modules.selectus.ui.committee.assignment.AddAssignmentStepCallback;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData;
import org.olat.modules.selectus.ui.committee.assignment.RemoveAssignment1CommitteeStep;
import org.olat.modules.selectus.ui.committee.assignment.RemoveAssignmentStepCallback;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationAssignmentsController extends FormBasicController {
	
	private FormLink addAssignmentButton;
	private FormLink removeAssignmentButton;

	private Position position;
	private Application application;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private StepsMainRunController addAssignmentWizardController;
	private StepsMainRunController removeAssignmentWizardController;
	
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private AssignmentService assignmentService;
	@Autowired
	private RecruitingService recruitingService;

	public ApplicationAssignmentsController(UserRequest ureq, WindowControl wControl, Position position,
			Application application, RecruitingPositionSecurityCallback secCallback, Form rootForm) {
		super(ureq, wControl, "assignments", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		this.secCallback = secCallback;
		this.application = application;
		this.position = position;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		loadModel();
		
		if(recruitingModule.isApplicationAssignmentsEnabled() && secCallback.canEditAssignments()) {
			addAssignmentButton = uifactory.addFormLink("add.assignments", formLayout, Link.LINK);
			addAssignmentButton.setElementCssClass("o_sel_add_assignments");
			removeAssignmentButton = uifactory.addFormLink("remove.assignments", formLayout, Link.LINK);
			removeAssignmentButton.setElementCssClass("o_sel_remove_assignments");

			DropdownItem dropdown = new DropdownItem("assignments", "assignments.mgmt", getTranslator());
			formLayout.add("assignments", dropdown);
			dropdown.addElement(addAssignmentButton);
			dropdown.addElement(removeAssignmentButton);
			dropdown.setButton(true);
			dropdown.setEmbbeded(true);
		}
	}
	
	private void loadModel() {
		List<Identity> assignees = assignmentService.getAssignees(application);
		List<String> assigneesList = new ArrayList<>(assignees.size());
		for(Identity assignee:assignees) {
			StringBuilder fullName = new StringBuilder(64);
			String title = assignee.getUser().getProperty(UserConstants.TITLE, getLocale());
			if(StringHelper.containsNonWhitespace(title) && !"-".equals(title)) {
				fullName.append(title);
			}
			String firstName = assignee.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(firstName)) {
				if(fullName.length() > 0) fullName.append(" ");
				fullName.append(firstName);
			}
			String lastName = assignee.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			if(StringHelper.containsNonWhitespace(lastName)) {
				if(fullName.length() > 0) fullName.append(" ");
				fullName.append(lastName);
			}
			assigneesList.add(StringHelper.escapeHtml(fullName.toString()));
		}
		flc.contextPut("assignees", assigneesList);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addAssignmentWizardController == source || removeAssignmentWizardController == source) {
			if (event == Event.CANCELLED_EVENT) {
				getWindowControl().pop();
			} else if (event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				getWindowControl().pop();
				//reload the list
				loadModel();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(removeAssignmentWizardController);
		removeAsListenerAndDispose(addAssignmentWizardController);
		removeAssignmentWizardController = null;
		addAssignmentWizardController = null;
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addAssignmentButton == source) {
			doAddAssignments(ureq);
		} else if(removeAssignmentButton == source) {
			doRemoveAssignments(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddAssignments(UserRequest ureq) {
		removeAsListenerAndDispose(addAssignmentWizardController);
		
		ApplicationLight app = recruitingService.getApplicationLight(position, application);
		List<ApplicationLight> apps = Collections.singletonList(app);
		AssignmentsData data = new AssignmentsData(position, apps, AssignmentMethods.manual);
		Step start = new AddAssignment1CommitteeStep(ureq, data);
		AddAssignmentStepCallback finish = new AddAssignmentStepCallback(apps, data, getTranslator());
		
		String title = translate("add.assignment.wizard.title");		
		addAssignmentWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
		listenTo(addAssignmentWizardController);
		
		getWindowControl().pushAsModalDialog(addAssignmentWizardController.getInitialComponent());
	}
	
	private void doRemoveAssignments(UserRequest ureq) {
		removeAsListenerAndDispose(removeAssignmentWizardController);

		ApplicationLight app = recruitingService.getApplicationLight(position, application);
		List<ApplicationLight> apps = Collections.singletonList(app);
		AssignmentsData data = new AssignmentsData(position, apps, AssignmentMethods.manual);
		Step start = new RemoveAssignment1CommitteeStep(ureq, data);
		RemoveAssignmentStepCallback finish = new RemoveAssignmentStepCallback(apps, data, getTranslator());
	
		String title = translate("remove.assignment.wizard.title");
		removeAssignmentWizardController = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, title, null);
		listenTo(removeAssignmentWizardController);

		getWindowControl().pushAsModalDialog(removeAssignmentWizardController.getInitialComponent());
		
	}
}
