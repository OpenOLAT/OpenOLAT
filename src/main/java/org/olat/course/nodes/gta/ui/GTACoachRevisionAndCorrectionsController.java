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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachRevisionAndCorrectionsController extends BasicController {
	
	private final VelocityContainer mainVC;
	private Link returnToRevisionsButton, closeRevisionsButton;
	private DirectoryController revisionsCtrl, correctionsCtrl;
	private SubmitDocumentsController uploadCorrectionsCtrl;
	
	private Task assignedTask;
	private final int currentIteration;
	private final GTACourseNode gtaNode;
	private final boolean businessGroupTask;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private GTAManager gtaManager;
	
	public GTACoachRevisionAndCorrectionsController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			Task assignedTask, GTACourseNode gtaNode, BusinessGroup assessedGroup, Identity assessedIdentity) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.assignedTask = assignedTask;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		this.businessGroupTask = GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE));
		currentIteration = assignedTask.getRevisionLoop();
		
		mainVC = createVelocityContainer("coach_revisions");
		putInitialPanel(mainVC);
		initRevisionProcess(ureq);
	}
	
	public Task getAssignedTask() {
		return assignedTask;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void initRevisionProcess(UserRequest ureq) {
		List<String> revisionStepNames = new ArrayList<>();
		mainVC.contextPut("previousRevisions", revisionStepNames);
		
		if(assignedTask.getRevisionLoop() > 1) {
			for(int i=1 ; i<assignedTask.getRevisionLoop(); i++ ) {
				setRevisionIteration(ureq, i, revisionStepNames);
			}
		}

		TaskProcess status = assignedTask.getTaskStatus();
		if(status == TaskProcess.revision) {
			//assessed user can return some revised documents
			setRevisions(ureq, "revisions", currentIteration);
		} else if(status == TaskProcess.correction) {
			//coach can return some corrections
			setRevisions(ureq, "revisions", currentIteration);
			setUploadCorrections(ureq, assignedTask, assignedTask.getRevisionLoop());
		} else {
			int lastIteration = assignedTask.getRevisionLoop();
			setRevisionIteration(ureq, lastIteration, revisionStepNames);
		}
	}
	
	private void setRevisionIteration(UserRequest ureq, int iteration, List<String> revisionStepNames) {
		//revisions
		String revCmpName = "revisions-" + iteration;
		if(setRevisions(ureq, revCmpName, iteration)) {
			revisionStepNames.add(revCmpName);
		}
		//corrections;
		String correctionCmpName = "corrections-" + iteration;
		if(setCorrections(ureq, correctionCmpName, iteration)) {
			revisionStepNames.add(correctionCmpName);
		}
	}
	
	private boolean setRevisions(UserRequest ureq, String cmpName, int iteration) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(businessGroupTask) {
			documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			documentsContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, iteration, assessedGroup);
		} else {
			documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
		}

		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			revisionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"coach.revisions.description");
			listenTo(revisionsCtrl);
			mainVC.put(cmpName, revisionsCtrl.getInitialComponent());
		}
		return hasDocuments;
	}
	
	private boolean setCorrections(UserRequest ureq, String cmpName, int iteration) {
		File documentsDir;
		VFSContainer documentsContainer = null;
		if(businessGroupTask) {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedGroup);
		} else {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
		}
		
		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			correctionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"run.coach.corrections.description", "bulk.review", "review");
			listenTo(correctionsCtrl);
			mainVC.put(cmpName, correctionsCtrl.getInitialComponent());
		}
		return hasDocuments;
	}
	
	private void setUploadCorrections(UserRequest ureq, Task task, int iteration) {
		File documentsDir;
		VFSContainer documentsContainer;
		ModuleConfiguration config = gtaNode.getModuleConfiguration();
		if(businessGroupTask) {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedGroup);
		} else {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedIdentity);
		}
		
		uploadCorrectionsCtrl = new SubmitDocumentsController(ureq, getWindowControl(), task, documentsDir, documentsContainer, -1, config, "coach.document");
		listenTo(uploadCorrectionsCtrl);
		mainVC.put("uploadCorrections", uploadCorrectionsCtrl.getInitialComponent());

		returnToRevisionsButton = LinkFactory.createCustomLink("coach.submit.corrections.to.revision.button", "submit", "coach.submit.corrections.to.revision.button", Link.BUTTON, mainVC, this);
		returnToRevisionsButton.setCustomEnabledLinkCSS("btn btn-primary");
		returnToRevisionsButton.setIconLeftCSS("o_icon o_icon o_icon_submit");
		returnToRevisionsButton.setElementCssClass("o_sel_course_gta_return_revision");
		returnToRevisionsButton.setVisible(uploadCorrectionsCtrl.hasUploadDocuments());
		
		closeRevisionsButton = LinkFactory.createCustomLink("coach.close.revision.button", "close", "coach.close.revision.button", Link.BUTTON, mainVC, this);
		closeRevisionsButton.setCustomEnabledLinkCSS("btn btn-primary");
		closeRevisionsButton.setIconLeftCSS("o_icon o_icon o_icon_submit");
		closeRevisionsButton.setElementCssClass("o_sel_course_gta_close_revision");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(uploadCorrectionsCtrl == source) {
			if(event instanceof SubmitEvent) {
				Task aTask = uploadCorrectionsCtrl.getAssignedTask();
				gtaManager.log("Corrections", (SubmitEvent)event, aTask, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
			}
			returnToRevisionsButton.setVisible(uploadCorrectionsCtrl.hasUploadDocuments());

		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(returnToRevisionsButton == source) {
			doReturnToRevisions();
			fireEvent(ureq, Event.DONE_EVENT);
		} else if(closeRevisionsButton == source) {
			doCloseRevisionProcess();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	private void doReturnToRevisions() {
		assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.revision, currentIteration + 1);
		gtaManager.log("Revision", "need another revision", assignedTask, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
	}
	
	private void doCloseRevisionProcess() {
		TaskProcess nextStep = gtaManager.nextStep(TaskProcess.correction, gtaNode);
		assignedTask = gtaManager.updateTask(assignedTask, nextStep);
		gtaManager.log("Revision", "close revision", assignedTask, getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode);
	}
}
