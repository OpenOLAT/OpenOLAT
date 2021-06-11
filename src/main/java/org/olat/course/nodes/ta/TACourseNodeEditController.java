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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.admin.quota.QuotaConstants;
import org.olat.core.commons.modules.bc.FolderEvent;
import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.callbacks.FullAccessWithQuotaCallback;
import org.olat.core.util.vfs.callbacks.VFSSecurityCallback;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.ms.MSCourseNodeEditController;
import org.olat.course.nodes.ms.MSEditFormController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  
 *   Initial Date:  30.08.2004
 *  
 *   @author Mike Stock
 *   
 *   Comment:  
 *   
 * </pre>
 */

public class TACourseNodeEditController extends ActivateableTabbableDefaultController implements ControllerEventListener {	
	
	private static final Logger log = Tracing.createLoggerFor(TACourseNodeEditController.class); 
	
	public static final String PANE_TAB_CONF_SCORING = "pane.tab.conf.scoring";

	public static final String PANE_TAB_CONF_DROPBOX = "pane.tab.conf.dropbox";

	public static final String PANE_TAB_CONF_TASK = "pane.tab.conf.task";

	public static final String PANE_TAB_CONF_MODULES = "pane.tab.conf.modules";

	public static final String PANE_TAB_ACCESSIBILITY = "pane.tab.accessibility";

	public static final String PANE_TAB_SOLUTION = "pane.tab.solution";
	
	private static final String[] paneKeys = { PANE_TAB_SOLUTION, PANE_TAB_CONF_SCORING, PANE_TAB_CONF_DROPBOX, PANE_TAB_CONF_TASK, PANE_TAB_CONF_MODULES,
			PANE_TAB_ACCESSIBILITY };

	private ICourse course;
	private TACourseNode node;
	private ModuleConfiguration config;

	private VelocityContainer accessabilityVC, solutionVC;
	private VelocityContainer editModules, editTask, editDropbox, editScoring;
	private TabbedPane myTabbedPane;
	private int taskTabPosition, dropboxTabPosition, scoringTabPosition, solutionTabPosition;
	private ModulesForm modulesForm;
	private TaskFormController taskController;
	private DropboxForm dropboxForm;
	private MSEditFormController scoringController;
	private FolderRunController frc;
	private ConditionEditController taskConditionC, dropConditionC, returnboxConditionC, scoringConditionC, solutionConditionC;
	private boolean hasLogEntries;	
	private DialogBoxController dialogBoxController;

	private Link btfButton;
	private Link editScoringConfigButton;
	private Link vfButton;
	
	private MailNotificationEditController mailCtr;
	private CloseableModalController cmc;
	private List<Identity> identitiesToBeNotified;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private QuotaManager quotaManager;

	/**
	 * @param ureq
	 * @param wControl
	 * @param course
	 * @param node
	 * @param groupMgr
	 */
	public TACourseNodeEditController(UserRequest ureq, WindowControl wControl, ICourse course, TACourseNode node,
			UserCourseEnvironment euce) {
		super(ureq, wControl);

		this.node = node;
		//o_clusterOk by guido: save to hold reference to course inside editor
		this.course = course;
		this.config = node.getModuleConfiguration();
		Translator newTranslator = Util.createPackageTranslator(TACourseNodeEditController.class, ureq.getLocale(),
				Util.createPackageTranslator(MSCourseNodeEditController.class, ureq.getLocale()));
		setTranslator(newTranslator);
		
		accessabilityVC = this.createVelocityContainer("edit");
		// Task precondition
		taskConditionC = new ConditionEditController(ureq, getWindowControl(), euce, node.getConditionTask(),
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node));		
		this.listenTo(taskConditionC);
		if (((Boolean) config.get(TACourseNode.CONF_TASK_ENABLED)).booleanValue()) accessabilityVC.put("taskCondition", taskConditionC
				.getInitialComponent());

		// DropBox precondition
		dropConditionC = new ConditionEditController(ureq, getWindowControl(), euce, node.getConditionDrop(),
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node));		
		this.listenTo(dropConditionC);
		Boolean hasDropboxValue = ((Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED)!=null) ? (Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED) : false;
		if (hasDropboxValue) accessabilityVC.put("dropCondition", dropConditionC.getInitialComponent());
				
		//returnbox precondition - use dropbox condition if none defined for rnew Boolean(task.isSelected(0)));boolean returnBoxEnabled = (returnBoxConf !=null) ? ((Boolean) returneturnbox
		Condition dropboxCondition = node.getConditionDrop();		
		Condition returnboxCondition = node.getConditionReturnbox();
		if(dropboxCondition!=null && returnboxCondition!= null && returnboxCondition.getConditionExpression()==null) {
			//old courses: use ConditionExpression from dropbox if none defined for returnbox			
			returnboxCondition = dropboxCondition;
			returnboxCondition.setConditionId(TACourseNode.ACCESS_RETURNBOX);
			node.setConditionReturnbox(returnboxCondition);			
		}
		returnboxConditionC = new ConditionEditController(ureq, getWindowControl(), euce, returnboxCondition,
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node));
		listenTo(returnboxConditionC);
		Object returnBoxConf = config.get(TACourseNode.CONF_RETURNBOX_ENABLED);
		//use the dropbox config if none specified for the return box
		boolean returnBoxEnabled = (returnBoxConf !=null) ? ((Boolean) returnBoxConf).booleanValue() : hasDropboxValue;
		if (returnBoxEnabled) accessabilityVC.put("returnboxCondition", returnboxConditionC.getInitialComponent());

		// Scoring precondition
		scoringConditionC = new ConditionEditController(ureq, getWindowControl(), euce, node.getConditionScoring(),
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node));		
		listenTo(scoringConditionC);
		if (((Boolean) config.get(TACourseNode.CONF_SCORING_ENABLED)).booleanValue()) accessabilityVC.put("scoringCondition", scoringConditionC
				.getInitialComponent());

		// SolutionFolder precondition
		solutionConditionC = new ConditionEditController(ureq, getWindowControl(), euce, node.getConditionSolution(),
				AssessmentHelper.getAssessableNodes(course.getEditorTreeModel(), node));		
		listenTo(solutionConditionC);
    if (((Boolean) config.get(TACourseNode.CONF_SOLUTION_ENABLED)).booleanValue()) accessabilityVC.put("solutionCondition", solutionConditionC
    		.getInitialComponent());
		
		// Modules config		
    editModules = this.createVelocityContainer("editModules");
		modulesForm = new ModulesForm(ureq, wControl, config);
		listenTo (modulesForm);
		editModules.put("modulesform", modulesForm.getInitialComponent());

		// Task config		
		editTask = this.createVelocityContainer("editTask");
		btfButton = LinkFactory.createButton("taskfolder", editTask, this);
		
		taskController = new TaskFormController(ureq, wControl, config);
		listenTo(taskController);
		String taskFolderPath = (String) node.getModuleConfiguration().get(TACourseNode.CONF_TASK_FOLDER_REL_PATH);
		if (taskFolderPath == null) editTask.contextPut("taskfolder", translate("taskfolder.empty"));
		else editTask.contextPut("taskfolder", taskFolderPath);
		editTask.put("taskform", taskController.getInitialComponent());

		// DropBox config		
		editDropbox = this.createVelocityContainer("editDropbox");
		dropboxForm = new DropboxForm(ureq, wControl, config);
		listenTo(dropboxForm);
		editDropbox.put("dropboxform", dropboxForm.getInitialComponent());

		// Scoring config		
		editScoring = this.createVelocityContainer("editScoring");
		editScoringConfigButton = LinkFactory.createButtonSmall("scoring.config.enable.button", editScoring, this);

		scoringController = new MSEditFormController(ureq, wControl, config, NodeAccessType.of(course));
		listenTo(scoringController);
		editScoring.put("scoringController", scoringController.getInitialComponent());
		
		// if there is already user data available, make for read only
		UserNodeAuditManager am = course.getCourseEnvironment().getAuditManager();
		hasLogEntries = am.hasUserNodeLogs(node);
		editScoring.contextPut("hasLogEntries", new Boolean(hasLogEntries));
		if (hasLogEntries) {
			scoringController.setDisplayOnly(true);
		}
		//Initialstate
		editScoring.contextPut("isOverwriting", new Boolean(false));
		
		// Solution-Tab		
		solutionVC = createVelocityContainer("editSolutionFolder");
		vfButton = LinkFactory.createButton("link.solutionFolder", solutionVC, this);
				
	}
	
	private VFSSecurityCallback getTaskFolderSecCallback(String relPath) {
		Quota folderQuota = quotaManager.getCustomQuota(relPath);
		if (folderQuota == null) {
			Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_POWER);
			folderQuota = quotaManager.createQuota(relPath, defQuota.getQuotaKB(), defQuota.getUlLimitKB());
		}
		return new TaskFolderCallback(false, folderQuota); // do not look task folder
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {		
		if (log.isDebugEnabled()) log.debug("event source=" + source + " " + event.toString());		
		if (source == btfButton){
			// check if there are already assigned tasks
			CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
			List<Property> assignedProps = cpm.listCourseNodeProperties(node, null, null, TaskController.PROP_ASSIGNED);
			if (assignedProps.isEmpty()) {
				// no task assigned
				String relPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
				VFSContainer rootFolder = VFSManager.olatRootContainer(relPath, null);
				VFSContainer namedFolder = new NamedContainerImpl(translate("taskfolder"), rootFolder);
				namedFolder.setLocalSecurityCallback(getTaskFolderSecCallback(relPath));
				frc = new FolderRunController(namedFolder, false, ureq, getWindowControl());
				//listenTo(frc);
				frc.addControllerListener(this);
				CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("folder.close"), frc
						.getInitialComponent());
				cmc.activate();				
			} else {				
				// already assigned task => open dialog with warn
				String[] args = new String[] { new Integer(assignedProps.size()).toString() };				
				dialogBoxController = activateOkCancelDialog(ureq, "", getTranslator().translate("taskfolder.overwriting.confirm", args), dialogBoxController);
			}
		} else if (source == vfButton) {
			// switch to new dialog
			VFSContainer namedContainer = TACourseNode.getNodeFolderContainer(node, course.getCourseEnvironment());
			Quota quota = quotaManager.getCustomQuota(namedContainer.getRelPath());
			if (quota == null) {
				Quota defQuota = quotaManager.getDefaultQuota(QuotaConstants.IDENTIFIER_DEFAULT_NODES);
				quota = quotaManager.createQuota(namedContainer.getRelPath(), defQuota.getQuotaKB(), defQuota.getUlLimitKB());
			}
			SubscriptionContext subContext = SolutionFileUploadNotificationHandler.getSubscriptionContext(course.getCourseEnvironment(), node);
			VFSSecurityCallback secCallback = new FullAccessWithQuotaCallback(quota, subContext);
			namedContainer.setLocalSecurityCallback(secCallback);
			FolderRunController folderCtrl = new FolderRunController(namedContainer, false, ureq, getWindowControl());
			CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"), folderCtrl.getInitialComponent());
			cmc.activate();
		} else if (source == editScoringConfigButton){
			scoringController.setDisplayOnly(false);
			editScoring.contextPut("isOverwriting", Boolean.valueOf(true));
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest urequest, Controller source, Event event) {
		if (source == modulesForm) {
			boolean onoff = event.getCommand().endsWith("true");
			if (event.getCommand().startsWith("task")) {
				config.set(TACourseNode.CONF_TASK_ENABLED, new Boolean(onoff));
				myTabbedPane.setEnabled(taskTabPosition, onoff);
				if (onoff) {
					accessabilityVC.put("taskCondition", taskConditionC.getInitialComponent());
				} else {
					accessabilityVC.remove(taskConditionC.getInitialComponent());
				}
			} else if (event.getCommand().startsWith("dropbox")) {
				config.set(TACourseNode.CONF_DROPBOX_ENABLED, new Boolean(onoff));
				myTabbedPane.setEnabled(dropboxTabPosition, onoff);
				if (onoff) {
					accessabilityVC.put("dropCondition", dropConditionC.getInitialComponent());
				} else {
					accessabilityVC.remove(dropConditionC.getInitialComponent());
				}
			} else if (event.getCommand().startsWith("returnbox")) {
				config.set(TACourseNode.CONF_RETURNBOX_ENABLED, new Boolean(onoff));
				if (onoff) {
					accessabilityVC.put("returnboxCondition", returnboxConditionC.getInitialComponent());
				} else {
					accessabilityVC.remove(returnboxConditionC.getInitialComponent());
				}	
			} else if (event.getCommand().startsWith("scoring")) {
				config.set(TACourseNode.CONF_SCORING_ENABLED, new Boolean(onoff));
				myTabbedPane.setEnabled(scoringTabPosition, onoff);
				if (onoff) {
					accessabilityVC.put("scoringCondition", scoringConditionC.getInitialComponent());
				} else {
					accessabilityVC.remove(scoringConditionC.getInitialComponent());
				}
			} else if (event.getCommand().startsWith("solution")) {
				config.set(TACourseNode.CONF_SOLUTION_ENABLED, new Boolean(onoff));
				myTabbedPane.setEnabled(solutionTabPosition, onoff);
				if (onoff) {
					accessabilityVC.put("solutionCondition", solutionConditionC.getInitialComponent());
				} else {
					accessabilityVC.remove(solutionConditionC.getInitialComponent());
				}
			} 
		
			fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			return;
			
		} else if (source == taskConditionC) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionTask(taskConditionC.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == dropConditionC) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionDrop(dropConditionC.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == returnboxConditionC) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionReturnbox(returnboxConditionC.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == scoringConditionC) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionScoring(scoringConditionC.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == solutionConditionC) {
			if (event == Event.CHANGED_EVENT) {
				node.setConditionSolution(solutionConditionC.getCondition());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == dialogBoxController) {			
			if (DialogBoxUIFactory.isOkEvent(event)) {
				// ok: open task folder
				String relPath = TACourseNode.getTaskFolderPathRelToFolderRoot(course, node);
				VFSContainer rootFolder = VFSManager.olatRootContainer(relPath, null);
				VFSContainer namedFolder = new NamedContainerImpl(translate("taskfolder"), rootFolder);
				namedFolder.setLocalSecurityCallback(getTaskFolderSecCallback(relPath));
				frc = new FolderRunController(namedFolder, false, urequest, getWindowControl());
				listenTo(frc);
				CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("folder.close"), frc
						.getInitialComponent());
				cmc.activate();
				fireEvent(urequest, Event.CHANGED_EVENT);
			}
		} else if (source == taskController) {
			if (event == Event.CANCELLED_EVENT) {
				return;
			} else if (event == Event.DONE_EVENT) {
				config.set(TACourseNode.CONF_TASK_TYPE, taskController.getTaskType());
				config.set(TACourseNode.CONF_TASK_TEXT, taskController.getOptionalText());
				config.set(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT, new Boolean(taskController.getIsSamplingWithReplacement()));
				config.setBooleanEntry(TACourseNode.CONF_TASK_PREVIEW, taskController.isTaskPreviewMode());
				config.setBooleanEntry(TACourseNode.CONF_TASK_DESELECT, taskController.isTaskDeselectMode());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				return;
			}
		} else if (source == scoringController) {
			if (event == Event.CANCELLED_EVENT) {
				if (hasLogEntries) {
					scoringController.setDisplayOnly(true);}
				editScoring.contextPut("isOverwriting", new Boolean(false));
				return;				
			} else if (event == Event.DONE_EVENT){
				scoringController.updateModuleConfiguration(config);
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(urequest, NodeEditController.REMINDER_VISIBILITY_EVENT);
			}
		} else if (source == dropboxForm) {
			if (event == Event.CANCELLED_EVENT) {
				return;
			} else if (event == Event.DONE_EVENT) {
				config.set(TACourseNode.CONF_DROPBOX_ENABLEMAIL, new Boolean(dropboxForm.mailEnabled()));
				config.set(TACourseNode.CONF_DROPBOX_CONFIRMATION, dropboxForm.getConfirmation());
				fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
				return;
			}
		} else if (source == frc && (event instanceof FolderEvent) && event.getCommand().equals(FolderEvent.DELETE_EVENT)) {					
			String deletedTaskFile = getFileListAsComaSeparated(((FolderEvent)event).getFilename());
		  //cancel task assignment				
			identitiesToBeNotified = removeAssignedTask(course,deletedTaskFile);
			if(identitiesToBeNotified.size()>0) {
		    //prepare mailTemplate if they are any identities to be notified
			  removeAsListenerAndDispose(mailCtr);					
			  RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			  String courseURL = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + repositoryEntry.getKey();
			  MailTemplate mailTemplate = this.createTaskDeletedMailTemplate(urequest, course.getCourseTitle(), courseURL, deletedTaskFile);
			  mailCtr = new MailNotificationEditController(getWindowControl(), urequest, mailTemplate, true, false, true);
			  listenTo(mailCtr);
			  cmc = new CloseableModalController(getWindowControl(), translate("close"), mailCtr.getInitialComponent());
			  listenTo(cmc);			
			  cmc.activate();
			}
		} else if (source == mailCtr) {
			if (event == Event.DONE_EVENT) {				
				cmc.deactivate();
				if(identitiesToBeNotified!=null && identitiesToBeNotified.size()>0) {
				  // sent email to all identities that used to have the deleted task assigned	
				  sendNotificationEmail(urequest, mailCtr.getMailTemplate(), identitiesToBeNotified);
				}
			} else if(event == Event.CANCELLED_EVENT) {
				cmc.deactivate();		
			}
		} else {			
			log.warn("Can not handle event in TACourseNodeEditController source=" + source + " " + event.toString());
		}
	}
	
	/**
	 * Strips the html tags from the input string.
	 * @param fileListHtml
	 * @return
	 */
	private String getFileListAsComaSeparated(String fileListHtml) {
	  //strip html
		String filesString = "";
		String[] tokens = fileListHtml.split("<[^<>]+>");
		for(String token:tokens) {
			if(!token.equals("")) {
				if(filesString.length()>3) {
					filesString += ", ";
				}
				filesString += token;					
			}
		}
		return filesString;
	}
	
	/**
	 * Create MailTemplate for task deleted action.
	 * @param ureq
	 * @param courseName
	 * @param courseLink
	 * @param fileName
	 * @return
	 */
	private MailTemplate createTaskDeletedMailTemplate(UserRequest ureq, String courseName, String courseLink, String fileName) {
		String subjectTemplate = courseName + ": " + translate("task.deleted.subject");
		String bodyTemplate = getTaskDeletedMailBody(ureq, fileName, courseName, courseLink);
		MailTemplate mailTempl = new MailTemplate(subjectTemplate, bodyTemplate, null) {

			@Override
			public void putVariablesInMailContext(VelocityContext context, Identity recipient) {
				// nothing to do
			}
		};
		return mailTempl;
	}

	private String getTaskDeletedMailBody(UserRequest ureq, String fileName, String courseName, String courseLink) {
    // grab standard text
		String confirmation = translate("task.deleted.body");
				
		Context c = new VelocityContext();
		Identity identity = ureq.getIdentity();
		c.put("first", identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()));
		c.put("last", identity.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
		c.put("email", UserManager.getInstance().getUserDisplayEmail(identity, ureq.getLocale()));
		c.put("filename", fileName);		
		c.put("coursename", courseName);
		c.put("courselink", courseLink);
		
		return VelocityHelper.getInstance().evaluateVTL(confirmation, c);
	}
	
	private void sendNotificationEmail(UserRequest ureq, MailTemplate mailTemplate, List<Identity> recipients) {
	// send the notification mail
		if (mailTemplate != null) {
			Identity sender = ureq.getIdentity();
			MailContext context = new MailContextImpl(getWindowControl().getBusinessControl().getAsString());
			MailerResult result = new MailerResult();
			String metaId = UUID.randomUUID().toString().replace("-", "");
			MailBundle[] bundles = mailManager.makeMailBundles(context, recipients, mailTemplate, sender, metaId, result);
			result.append(mailManager.sendMessage(bundles));
			if(mailTemplate.getCpfrom()) {
				MailBundle ccBundle = mailManager.makeMailBundle(context, sender, mailTemplate, sender, metaId, result);
				result.append(mailManager.sendMessage(ccBundle));
			}
			
			Roles roles = ureq.getUserSession().getRoles();
			boolean detailedErrorOutput = roles.isAdministrator() || roles.isSystemAdmin();
			MailHelper.printErrorsAndWarnings(result, getWindowControl(), detailedErrorOutput, ureq.getLocale());
		}
	}
	
	/**
	 * Cancel the task assignment for this task and all Identities.
	 * @param course
	 * @param task
	 * @return Returns the Identities list that have had this task assigned.
	 */
	private List<Identity> removeAssignedTask(ICourse course, String task) {	
		//identities to be notified
		List<Identity> identityList = new ArrayList<>();
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();				
		List<Property> properties = cpm.listCourseNodeProperties(node, null, null, TaskController.PROP_ASSIGNED);
		if(properties!=null && properties.size()>0) {
		  for(Object propetyObj:properties) {
		    Property propety = (Property)propetyObj;		    
		    identityList.add(propety.getIdentity());
		    cpm.deleteProperty(propety);	
		  }
		}
		return identityList;
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	@Override
	public void addTabs(TabbedPane theTabbedPane) {
		this.myTabbedPane = theTabbedPane;
		myTabbedPane.addTab(translate(PANE_TAB_ACCESSIBILITY), accessabilityVC);
		myTabbedPane.addTab(translate(PANE_TAB_CONF_MODULES), editModules);
		taskTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_TASK), editTask);
		dropboxTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_DROPBOX), editDropbox);
		scoringTabPosition = myTabbedPane.addTab(translate(PANE_TAB_CONF_SCORING), editScoring);
		solutionTabPosition  = myTabbedPane.addTab(translate(PANE_TAB_SOLUTION), solutionVC);

		Boolean bool = (Boolean) config.get(TACourseNode.CONF_TASK_ENABLED);
		myTabbedPane.setEnabled(taskTabPosition, (bool != null) ? bool.booleanValue() : true);
		bool = (Boolean) config.get(TACourseNode.CONF_DROPBOX_ENABLED);
		myTabbedPane.setEnabled(dropboxTabPosition, (bool != null) ? bool.booleanValue() : true);
		bool = (Boolean) config.get(TACourseNode.CONF_SCORING_ENABLED);
		myTabbedPane.setEnabled(scoringTabPosition, (bool != null) ? bool.booleanValue() : true);
		
		bool = (Boolean) config.get(TACourseNode.CONF_SOLUTION_ENABLED);
		myTabbedPane.setEnabled(solutionTabPosition, (bool != null) ? bool.booleanValue() : true);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	@Override
	protected void doDispose() {
    //child controllers registered with listenTo() get disposed in BasicController
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

}

class TaskFolderCallback implements VFSSecurityCallback {

	private boolean folderLocked;
	private Quota folderQuota = null;

	/**
	 * @param folderLocked
	 */
	public TaskFolderCallback(boolean folderLocked, Quota folderQuota) {
		this.folderLocked = folderLocked;
		this.folderQuota = folderQuota;
	}

	@Override
	public boolean canRead() {
		return true;
	}

	@Override
	public boolean canWrite() {
		return !folderLocked;
	}

	@Override
	public boolean canCreateFolder() {
		return false;
	}

	@Override
	public boolean canDelete() {
		return !folderLocked;
	}

	@Override
	public boolean canList() {
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
		return folderQuota;
	}

	@Override
	public void setQuota(Quota quota) {
		folderQuota = quota;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}
}