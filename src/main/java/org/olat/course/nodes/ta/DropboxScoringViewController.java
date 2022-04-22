/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.ta;

import java.io.File;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.admin.quota.QuotaConstants;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.modules.bc.commands.FolderCommand;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.ReadOnlyCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.properties.Property;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  02.09.2004
 * @author Mike Stock
 */

public class DropboxScoringViewController extends BasicController {

	private static final Logger log = Tracing.createLoggerFor(DropboxScoringViewController.class);
	
	protected CourseNode node;
	protected UserCourseEnvironment userCourseEnv;	
	private VelocityContainer myContent;
	private Link taskLaunchButton;
	private Link cancelTaskButton;
	private FolderRunController dropboxFolderRunController;
	private FolderRunController returnboxFolderRunController;
	private String assignedTask;
	private StatusForm statusForm;
	private CloseableModalController cmc;
	private IFrameDisplayController iFrameCtr;
	private DialogBoxController dialogBoxController;
	private boolean hasNotification = false;
	private final AssessmentConfig assessmentConfig;
	
	@Autowired
	protected UserManager userManager;
	@Autowired
	private QuotaManager quotaManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	
	/**
	 * Scoring view of the dropbox.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param node
	 * @param userCourseEnv
	 */
	public DropboxScoringViewController(UserRequest ureq, WindowControl wControl, CourseNode node, UserCourseEnvironment userCourseEnv) { 
		this(ureq, wControl, node, userCourseEnv, true);
	}

	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param node
	 * @param userCourseEnv
	 * @param doInit        When true call init-method in constructor.
	 */
	protected DropboxScoringViewController(UserRequest ureq, WindowControl wControl, CourseNode node, UserCourseEnvironment userCourseEnv, boolean doInit) { 
		super(ureq, wControl);
		
		this.node = node;
		this.userCourseEnv = userCourseEnv;
		assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(userCourseEnv), node);
		if (doInit) {
			init(ureq);
		}
	}
	
	protected void init(UserRequest ureq, boolean hasNotification){
		this.hasNotification = hasNotification;
		init(ureq);
	}

	protected void init(UserRequest ureq) {
		myContent = createVelocityContainer("dropboxscoring");
		taskLaunchButton = LinkFactory.createButton("task.launch", myContent, this);
		cancelTaskButton = LinkFactory.createButton("task.cancel", myContent, this);
		cancelTaskButton.setVisible(!userCourseEnv.isCourseReadOnly());
		putInitialPanel(myContent);		

		ModuleConfiguration modConfig = node.getModuleConfiguration();
		Boolean bValue = (Boolean)modConfig.get(TACourseNode.CONF_TASK_ENABLED);
		myContent.contextPut("hasTask", (bValue != null) ? bValue : Boolean.valueOf(false));
		Boolean hasDropbox = (Boolean)modConfig.get(TACourseNode.CONF_DROPBOX_ENABLED); //configured value
		Boolean hasDropboxValue = (hasDropbox != null) ? hasDropbox : Boolean.valueOf(true);
		myContent.contextPut("hasDropbox", hasDropboxValue);
		
		Boolean hasReturnbox = (Boolean)modConfig.get(TACourseNode.CONF_RETURNBOX_ENABLED);
		myContent.contextPut("hasReturnbox", (hasReturnbox != null) ? hasReturnbox : hasDropboxValue);

		// dropbox display
		Identity assessee = userCourseEnv.getIdentityEnvironment().getIdentity();

		// notification
		if (hasNotification) {
			SubscriptionContext subsContext = DropboxFileUploadNotificationHandler.getSubscriptionContext(userCourseEnv.getCourseEnvironment(), node);
			if (subsContext != null) {
				String path = DropboxController.getDropboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node);
				ContextualSubscriptionController contextualSubscriptionCtr = AbstractTaskNotificationHandler
						.createContextualSubscriptionController(ureq, getWindowControl(), path, subsContext, DropboxController.class);
				myContent.put("subscription", contextualSubscriptionCtr.getInitialComponent());
				myContent.contextPut("hasNotification", Boolean.TRUE);
			}
		} else {
			myContent.contextPut("hasNotification", Boolean.FALSE);
		}
		
		VFSContainer namedDropbox = getDropboxFilePath(assessee);
		dropboxFolderRunController = new FolderRunController(namedDropbox, false, ureq, getWindowControl());
		listenTo(dropboxFolderRunController);
		
		myContent.put("dropbox", dropboxFolderRunController.getInitialComponent());

		// returnbox display
		VFSContainer namedReturnbox = getReturnboxFilePath(assessee);
		returnboxFolderRunController = new FolderRunController(namedReturnbox, false, ureq, getWindowControl());
		returnboxFolderRunController.disableSubscriptionController();
		listenTo(returnboxFolderRunController);
		
		myContent.put("returnbox", returnboxFolderRunController.getInitialComponent());

		// insert Status Pull-Down Menu depending on user role == author
		boolean isAuthor = ureq.getUserSession().getRoles().isAuthor();
		boolean isTutor  = userCourseEnv.isCoach();
		if (assessmentConfig.hasStatus() && (isAuthor || isTutor)) {
			myContent.contextPut("hasStatusPullDown", Boolean.TRUE);
			statusForm = new StatusForm(ureq, getWindowControl(), userCourseEnv.isCourseReadOnly());
			listenTo(statusForm);

			// get identity not from request (this would be an author)
			StatusManager.getInstance().loadStatusFormData(statusForm,node,userCourseEnv);
			myContent.put("statusForm",statusForm.getInitialComponent());
		}
		
		assignedTask = TaskController.getAssignedTask(assessee, userCourseEnv.getCourseEnvironment(), node);
		if (assignedTask != null) {
			myContent.contextPut("assignedtask", assignedTask);
			myContent.contextPut("taskIcon", CSSHelper.createFiletypeIconCssClassFor(assignedTask));
			if (!(assignedTask.toLowerCase().endsWith(".html") || assignedTask.toLowerCase().endsWith(".htm") || assignedTask.toLowerCase().endsWith(".txt"))){
				taskLaunchButton.setTarget("_blank");
			}
		}
	}
	
	protected VFSSecurityCallback getDropboxVfsSecurityCallback() {
		if(userCourseEnv.isCourseReadOnly()) return new ReadOnlyCallback();
		
		return new ReadOnlyAndDeleteCallback();
	}

	protected VFSSecurityCallback getReturnboxVfsSecurityCallback(String returnboxRelPath, Identity assessedIdentity) {
		if(userCourseEnv.isCourseReadOnly()) return new ReadOnlyCallback();
		
		SubscriptionContext subscriptionContext = ReturnboxFileUploadNotificationHandler
				.getSubscriptionContext(userCourseEnv.getCourseEnvironment(), node, assessedIdentity);
		return new ReturnboxFullAccessCallback(returnboxRelPath, subscriptionContext, quotaManager);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == taskLaunchButton) {
			File fTaskfolder = new File(FolderConfig.getCanonicalRoot()
				+ TACourseNode.getTaskFolderPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node));
			if (assignedTask.toLowerCase().endsWith(".html") || assignedTask.toLowerCase().endsWith(".htm") || assignedTask.toLowerCase().endsWith(".txt")) {
				// render content for other users always in iframe
				removeAsListenerAndDispose(iFrameCtr);
				iFrameCtr = new IFrameDisplayController(ureq, getWindowControl(), fTaskfolder);
				listenTo(iFrameCtr);
				iFrameCtr.setCurrentURI(assignedTask);				
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), iFrameCtr.getInitialComponent());
				listenTo(cmc);
				cmc.activate();
			} else {
				ureq.getDispatchResult().setResultingMediaResource(new FileMediaResource(new File(fTaskfolder, assignedTask)));
			}
		} else if (source == cancelTaskButton) {
			//confirm cancel task assignment
			dialogBoxController = this.activateYesNoDialog(ureq, "", translate("task.cancel.reassign"), dialogBoxController);
		}

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dropboxFolderRunController) {
			if (event instanceof FolderEvent) {
				FolderEvent folderEvent = (FolderEvent) event;
				if (folderEvent.getCommand().equals(FolderEvent.DELETE_EVENT)) {
					UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
					// log entry for this file
					Identity coach = ureq.getIdentity();
					Identity student = userCourseEnv.getIdentityEnvironment().getIdentity();
					am.appendToUserNodeLog(node, coach, student, "FILE DELETED: " + folderEvent.getFilename(), null);
				}
			}
		} else if (source == returnboxFolderRunController) {
			if (event instanceof FolderEvent) {
				FolderEvent folderEvent = (FolderEvent) event;
				if (   folderEvent.getCommand().equals(FolderEvent.UPLOAD_EVENT)
						|| folderEvent.getCommand().equals(FolderEvent.NEW_FILE_EVENT) ) {
					UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
					// log entry for this file
					Identity coach = ureq.getIdentity();
					Identity student = userCourseEnv.getIdentityEnvironment().getIdentity();
					
					if(assessmentConfig.isAssessable()) {
						AssessmentEvaluation eval = courseAssessmentService.getAssessmentEvaluation(node, userCourseEnv);
						if(eval.getAssessmentStatus() == null || eval.getAssessmentStatus() == AssessmentEntryStatus.notStarted) {
							eval = new AssessmentEvaluation(eval, AssessmentEntryStatus.inProgress);
							courseAssessmentService.updateScoreEvaluation(node, eval, userCourseEnv, coach, false, Role.coach);
						}
					}

					am.appendToUserNodeLog(node, coach, student, "FILE UPLOADED: " + folderEvent.getFilename(), null);
					String toMail = UserManager.getInstance().getUserDisplayEmail(student, ureq.getLocale());
					
					OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseNode.class, Long.valueOf(node.getIdent()));
					ContextEntry ce =		BusinessControlFactory.getInstance().createContextEntry(ores);
					BusinessControl bc = BusinessControlFactory.getInstance().createBusinessControl(ce, getWindowControl().getBusinessControl());
					String link = BusinessControlFactory.getInstance().getAsURIString(bc, true);
					
					log.debug("Returnbox notification email with link={}", link);
					String subject = translate("returnbox.email.subject");
					String body = translate("returnbox.email.body", new String[] { userCourseEnv.getCourseEnvironment().getCourseTitle(), node.getShortTitle(),
									folderEvent.getFilename(), link });

					MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
					MailBundle bundle = new MailBundle();
					bundle.setContext(context);
					bundle.setToId(student);
					bundle.setContent(subject, body);
					MailerResult result = CoreSpringFactory.getImpl(MailManager.class).sendMessage(bundle);
					if(result.getReturnCode() > 0) {
						am.appendToUserNodeLog(node, coach, student, "MAIL SEND FAILED TO:" + toMail + "; MailReturnCode: " + result.getReturnCode(), null);
						log.warn("Could not send email 'returnbox notification' to " + student + "with email=" + toMail);
					} else {
						log.info("Send email 'returnbox notification' to " + student + "with email=" + toMail);
					}
				}
			} else if(FolderCommand.FOLDERCOMMAND_FINISHED == event) {
				if(assessmentConfig.isAssessable()) {
					AssessmentEvaluation eval = courseAssessmentService.getAssessmentEvaluation(node, userCourseEnv);
					if (eval == null) {
						eval = AssessmentEvaluation.EMPTY_EVAL;
					}
					if(eval.getAssessmentStatus() == null || eval.getAssessmentStatus() == AssessmentEntryStatus.notStarted) {
						eval = new AssessmentEvaluation(eval, AssessmentEntryStatus.inProgress);
						courseAssessmentService.updateScoreEvaluation(node, eval, userCourseEnv, getIdentity(), false, Role.coach);
						fireEvent(ureq, Event.CHANGED_EVENT);
					}
				}
			}
		} else if (source == statusForm) {
			if (event == Event.DONE_EVENT) {
				// get identity not from request (this would be an author)
				StatusManager.getInstance().saveStatusFormData(statusForm,node,userCourseEnv);
			}
		} else if (source == dialogBoxController) {
			if (DialogBoxUIFactory.isYesEvent(event) && assignedTask!=null) {
				//cancel task assignment, and show "no task assigned to user"				
				removeAssignedTask(userCourseEnv, userCourseEnv.getIdentityEnvironment().getIdentity());			
				//update UI
				myContent.contextPut("assignedtask", null);
			}
		}
	}
	
	/**
	 * Cancel the task assignment.
	 * @param identity
	 * @param task
	 */
	private void removeAssignedTask(UserCourseEnvironment courseEnv, Identity identity) {
		CoursePropertyManager cpm = courseEnv.getCourseEnvironment().getCoursePropertyManager();
		List<Property> properties = cpm.findCourseNodeProperties(node, identity, null, TaskController.PROP_ASSIGNED);
		if(properties!=null && !properties.isEmpty()) {
		  Property propety = properties.get(0);
		  cpm.deleteProperty(propety);
		  assignedTask = null;
		}
	  //removed sampled  				
		properties = cpm.findCourseNodeProperties(node, null, null, TaskController.PROP_SAMPLED);
		if(properties!=null && !properties.isEmpty()) {
		  Property propety = properties.get(0);
		  cpm.deleteProperty(propety);		  
		}		
	}

	protected VFSContainer getDropboxFilePath(Identity assesseeIdentity) {
		String assesseeFullName = StringHelper.escapeHtml(userManager.getUserDisplayName(assesseeIdentity));
		String path = DropboxController.getDropboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node)
				+ "/" + assesseeIdentity.getName();
		VFSContainer rootDropbox = VFSManager.olatRootContainer(path, null);
		rootDropbox.setLocalSecurityCallback( getDropboxVfsSecurityCallback());
		VFSContainer namedDropbox = new NamedContainerImpl(assesseeFullName, rootDropbox);
		namedDropbox.setLocalSecurityCallback(getDropboxVfsSecurityCallback());
		return namedDropbox;
	}

	protected VFSContainer getReturnboxFilePath(Identity assesseeIdentity) {
		String assesseeFullName = StringHelper.escapeHtml(userManager.getUserDisplayName(assesseeIdentity));
		String path = ReturnboxController.getReturnboxPathRelToFolderRoot(userCourseEnv.getCourseEnvironment(), node)
				+ "/" + assesseeIdentity.getName();
		VFSContainer rootReturnbox = VFSManager.olatRootContainer(path, null);
		VFSSecurityCallback secCallback = getReturnboxVfsSecurityCallback(rootReturnbox.getRelPath(), assesseeIdentity);
		rootReturnbox.setLocalSecurityCallback(secCallback);
		VFSContainer namedReturnbox = new NamedContainerImpl(assesseeFullName, rootReturnbox);
		namedReturnbox.setLocalSecurityCallback(secCallback);
		return namedReturnbox;
	}
}

class ReadOnlyAndDeleteCallback implements VFSSecurityCallback {

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return false;
	
	}
	@Override
	public boolean canCreateFolder() {
		return false;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return false;
	}

	@Override
	public Quota getQuota() {
		return null;
	}

	@Override
	public void setQuota(Quota quota) {
		//
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}
}
	
class ReturnboxFullAccessCallback implements VFSSecurityCallback {

	private Quota quota;
	private final SubscriptionContext subscriptionContext;

	public ReturnboxFullAccessCallback(String relPath, SubscriptionContext subscriptionContext, QuotaManager quotaManager) {
		this.subscriptionContext = subscriptionContext;
		quota = quotaManager.getCustomQuota(relPath);
		if (quota == null) { // if no custom quota set, use the default quotas...
			Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
			quota = quotaManager.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
	}

	@Override
	public boolean canList() {
		return true;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return true;
	}
	
	@Override
	public boolean canCreateFolder() {
		return true;
	}

	@Override
	public boolean canDelete() {
		return true;
	}

	@Override
	public boolean canCopy() {
		return true;
	}

	@Override
	public boolean canDeleteRevisionsPermanently() {
		return false;
	}
	
	

	@Override
	public Quota getQuota() {
		return quota;
	}

	@Override
	public void setQuota(Quota quota) {
		this.quota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return subscriptionContext;
	} 
}
