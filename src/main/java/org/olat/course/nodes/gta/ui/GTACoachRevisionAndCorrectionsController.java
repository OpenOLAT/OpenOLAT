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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.io.SystemFilenameFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.nodes.gta.TaskHelper.FilesLocked;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.ui.events.SubmitEvent;
import org.olat.course.nodes.gta.ui.events.TaskMultiUserEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachRevisionAndCorrectionsController extends BasicController implements Activateable2 {
	
	private final VelocityContainer mainVC;
	private Link collectButton;
	private Link closeRevisionsButton;
	private Link returnToRevisionsButton;
	
	private CloseableModalController cmc;
	private Map<Integer,DirectoryController> loopToRevisionCtrl = new HashMap<>();
	private SubmitDocumentsController uploadCorrectionsCtrl;
	private ConfirmRevisionsController confirmReturnToRevisionsCtrl;
	private DialogBoxController confirmCollectCtrl;
	private DialogBoxController confirmCloseRevisionProcessCtrl;
	
	private Task assignedTask;
	private final int currentIteration;
	private final GTACourseNode gtaNode;
	private final boolean businessGroupTask;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;
	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment coachCourseEnv;
	private final OLATResourceable taskListEventResource;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	
	public GTACoachRevisionAndCorrectionsController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			Task assignedTask, GTACourseNode gtaNode, UserCourseEnvironment coachCourseEnv, BusinessGroup assessedGroup,
			Identity assessedIdentity, OLATResourceable taskListEventResource) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.assignedTask = assignedTask;
		this.assessedGroup = assessedGroup;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedIdentity = assessedIdentity;
		this.taskListEventResource = taskListEventResource;
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
		if(collectButton != null) {
			mainVC.remove(collectButton);//reset collect
		}
		
		if(assignedTask.getRevisionLoop() > 1) {
			for(int i=1 ; i<assignedTask.getRevisionLoop(); i++ ) {
				setRevisionIteration(ureq, i, revisionStepNames);
			}
		}

		TaskProcess status = assignedTask.getTaskStatus();
		if(status == TaskProcess.revision) {
			//assessed user can return some revised documents
			setRevisions(ureq, "revisions", currentIteration);
			setCollectRevisions();
		} else if(status == TaskProcess.correction) {
			//coach can return some corrections
			setRevisions(ureq, "revisions", currentIteration);
			setUploadCorrections(ureq, assignedTask, assignedTask.getRevisionLoop());
		} else {
			int lastIteration = assignedTask.getRevisionLoop();
			setRevisionIteration(ureq, lastIteration, revisionStepNames);
		}
	}
	
	private void setCollectRevisions() {
		collectButton = LinkFactory.createButton("coach.collect.revisions", mainVC, this);
		collectButton.setUserObject(assignedTask);
		collectButton.setVisible(!coachCourseEnv.isCourseReadOnly());
	}
	
	private void setRevisionIteration(UserRequest ureq, int iteration, List<String> revisionStepNames) {
		// revisions
		String revCmpName = "revisions-" + iteration;
		if(setRevisions(ureq, revCmpName, iteration)) {
			revisionStepNames.add(revCmpName);
		}
		// corrections
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
			documentsContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, iteration, assessedIdentity);
		}

		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			DirectoryController revisionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"coach.revisions.description", "bulk.revisions", "revisions.zip");
			listenTo(revisionsCtrl);
			mainVC.put(cmpName, revisionsCtrl.getInitialComponent());
			loopToRevisionCtrl.put(iteration, revisionsCtrl);
		} else if (assignedTask.getTaskStatus() == TaskProcess.revision) {
			String msg = "<i class='o_icon o_icon_error'> </i> " + translate("coach.corrections.rejected");
			TextFactory.createTextComponentFromString(cmpName, msg, null, true, mainVC);
		} else {
			TextFactory.createTextComponentFromI18nKey(cmpName, "coach.revisions.nofiles", getTranslator(), null, true, mainVC);			
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
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedIdentity);
		}
		
		boolean hasDocuments = TaskHelper.hasDocuments(documentsDir);
		if(hasDocuments) {
			DirectoryController correctionsCtrl = new DirectoryController(ureq, getWindowControl(), documentsDir, documentsContainer,
					"run.coach.corrections.description", "bulk.review", "review");
			listenTo(correctionsCtrl);
			mainVC.put(cmpName, correctionsCtrl.getInitialComponent());
		}
		
		String msg = null;
		if (assignedTask.getTaskStatus() == TaskProcess.revision) {
			// message already displayed in setRevisions method for this case
		} else if (assignedTask.getTaskStatus() == TaskProcess.correction) {
			msg = "<i class='o_icon o_icon_info'> </i> " + translate("coach.corrections.waiting");
		} else {				
			msg = "<i class='o_icon o_icon_ok'> </i> " + translate("coach.corrections.closed");				
		}
		mainVC.contextPut("revisionMessage", msg);

		
		return hasDocuments;
	}
	
	private void setUploadCorrections(UserRequest ureq, Task task, int iteration) {
		File documentsDir;
		VFSContainer documentsContainer;
		if(businessGroupTask) {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedGroup);
		} else {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
			documentsContainer = gtaManager.getRevisedDocumentsCorrectionsContainer(courseEnv, gtaNode, iteration, assessedIdentity);
		}
		
		uploadCorrectionsCtrl = new SubmitDocumentsController(ureq, getWindowControl(), task, documentsDir, documentsContainer,
				-1, -1, gtaNode, courseEnv, coachCourseEnv.isCourseReadOnly(), null, "coach.document");
		listenTo(uploadCorrectionsCtrl);
		mainVC.put("uploadCorrections", uploadCorrectionsCtrl.getInitialComponent());

		returnToRevisionsButton = LinkFactory.createCustomLink("coach.submit.corrections.to.revision.button", "submit", "coach.submit.corrections.to.revision.button", Link.BUTTON, mainVC, this);
		returnToRevisionsButton.setCustomEnabledLinkCSS("btn btn-primary");
		returnToRevisionsButton.setIconLeftCSS("o_icon o_icon o_icon_rejected");
		returnToRevisionsButton.setElementCssClass("o_sel_course_gta_return_revision");
		returnToRevisionsButton.setVisible(!coachCourseEnv.isCourseReadOnly());
		
		closeRevisionsButton = LinkFactory.createCustomLink("coach.close.revision.button", "close", "coach.close.revision.button", Link.BUTTON, mainVC, this);
		closeRevisionsButton.setCustomEnabledLinkCSS("btn btn-primary");
		closeRevisionsButton.setIconLeftCSS("o_icon o_icon o_icon_accepted");
		closeRevisionsButton.setElementCssClass("o_sel_course_gta_close_revision");
		closeRevisionsButton.setVisible(!coachCourseEnv.isCourseReadOnly());
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.size() <= 1) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Revision".equalsIgnoreCase(type)) {
			int revisionLoop = entries.get(0).getOLATResourceable().getResourceableId().intValue();
			if(loopToRevisionCtrl.containsKey(revisionLoop)) {
				List<ContextEntry> subEntriess = entries.subList(1, entries.size());
				loopToRevisionCtrl.get(revisionLoop).activate(ureq, subEntriess, null);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(uploadCorrectionsCtrl == source) {
			if(event instanceof SubmitEvent) {
				Task aTask = uploadCorrectionsCtrl.getAssignedTask();
				gtaManager.log("Corrections", (SubmitEvent)event, aTask,
						getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
			}
		} else if(confirmReturnToRevisionsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doReturnToRevisions(confirmReturnToRevisionsCtrl.getTask());
				fireEvent(ureq, Event.DONE_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCloseRevisionProcessCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doCloseRevisionProcess();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(confirmCollectCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doCollect();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(returnToRevisionsButton == source) {
			doConfirmReturnToRevisions(ureq);
		} else if(closeRevisionsButton == source) {
			doConfirmCloseRevisionProcess(ureq);
		} else if(collectButton == source) {
			doConfirmCollect(ureq);
		}
	}
	
	private void doConfirmReturnToRevisions(UserRequest ureq) {
		if(guardModalController(confirmReturnToRevisionsCtrl)) return;
		
		confirmReturnToRevisionsCtrl = new ConfirmRevisionsController(ureq, getWindowControl(), assignedTask,
				assessedIdentity, assessedGroup, gtaNode, courseEnv);
		listenTo(confirmReturnToRevisionsCtrl);
		
		String title = translate("coach.revisions.confirm.title"); // same title as link button
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmReturnToRevisionsCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmCollect(UserRequest ureq) {
		String toName = null;
		if (assessedGroup != null) {
			toName = assessedGroup.getName();
		} else if (assessedIdentity != null) {
			toName = userManager.getUserDisplayName(assessedIdentity);			
		}
		
		File[] submittedDocuments;
		VFSContainer documentsContainer;
		int iteration = assignedTask.getRevisionLoop();
		if(GTAType.group.name().equals(gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_TYPE))) {
			documentsContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, iteration, assessedGroup);
			File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		} else {
			documentsContainer = gtaManager.getRevisedDocumentsContainer(courseEnv, gtaNode, iteration, getIdentity());
			File documentsDir = gtaManager.getRevisedDocumentsDirectory(courseEnv, gtaNode, iteration, getIdentity());
			submittedDocuments = documentsDir.listFiles(new SystemFilenameFilter(true, false));
		}
		
		FilesLocked lockedBy = TaskHelper.getDocumentsLocked(documentsContainer, submittedDocuments);
		if(lockedBy != null) {
			showWarning("warning.submit.documents.edited", new String[]{ lockedBy.getLockedBy(), lockedBy.getLockedFiles() });
		} else {
			String title = translate("coach.collect.revisions.confirm.title");
			String text = translate("coach.collect.revisions.confirm.text", new String[]{ toName });
			text = "<div class='o_warning'>" + text + "</div>";
			confirmCollectCtrl = activateOkCancelDialog(ureq, title, text, confirmCollectCtrl);
			listenTo(confirmCollectCtrl);
		}
	}
	
	private void doCollect() {
		assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.correction, gtaNode, true, getIdentity(), Role.coach);
		gtaManager.log("Collect revision", "revision collected", assignedTask,
				getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);

		TaskMultiUserEvent event = new TaskMultiUserEvent(TaskMultiUserEvent.SUBMIT_REVISION,
				assessedGroup == null ? getIdentity() : null, assessedGroup, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, taskListEventResource);
	}
	
	private void doReturnToRevisions(Task task) {
		assignedTask = gtaManager.updateTask(task, TaskProcess.revision, currentIteration + 1, gtaNode, false, getIdentity(), Role.coach);
		gtaManager.log("Revision", "need another revision", assignedTask,
				getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
	}
	
	private void doConfirmCloseRevisionProcess(UserRequest ureq) {
		String title = translate("coach.reviewed.confirm.title");
		String text = translate("coach.reviewed.confirm.text");
		confirmCloseRevisionProcessCtrl = activateOkCancelDialog(ureq, title, text, confirmCloseRevisionProcessCtrl);	
		listenTo(confirmCloseRevisionProcessCtrl);
	}
	
	private void doCloseRevisionProcess() {
		assignedTask = gtaManager.reviewedTask(assignedTask, gtaNode, getIdentity(), Role.coach);
		gtaManager.log("Revision", "close revision", assignedTask,
				getIdentity(), assessedIdentity, assessedGroup, courseEnv, gtaNode, Role.coach);
	}
}
