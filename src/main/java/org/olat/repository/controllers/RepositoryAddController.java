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

package org.olat.repository.controllers;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.OlatResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.Util;
import org.olat.ims.qti.fileresource.SurveyFileResource;
import org.olat.ims.qti.fileresource.TestFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.BlogHandler;
import org.olat.repository.handlers.CourseHandler;
import org.olat.repository.handlers.GlossaryHandler;
import org.olat.repository.handlers.ImsCPHandler;
import org.olat.repository.handlers.PodcastHandler;
import org.olat.repository.handlers.PortfolioHandler;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.handlers.SCORMCPHandler;
import org.olat.repository.handlers.SharedFolderHandler;
import org.olat.repository.handlers.WebDocumentHandler;
import org.olat.repository.handlers.WikiHandler;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
*  Description:<br>
*
* @author Felix Jost
*/
public class RepositoryAddController extends BasicController {
		
	public static final String PROCESS_NEW = "new";
	public static final String PROCESS_ADD = "add";
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(RepositoryManager.class);

	static final String ACTION_ADD_PREFIX = "a.";
	static final String ACTION_ADD_COURSE = ACTION_ADD_PREFIX + "co";
	static final String ACTION_ADD_CP = ACTION_ADD_PREFIX + "cp";
	static final String ACTION_ADD_SCORM = ACTION_ADD_PREFIX + "scorm";
	static final String ACTION_ADD_TEST = ACTION_ADD_PREFIX + "te";
	static final String ACTION_ADD_SURVEY = ACTION_ADD_PREFIX + "sv";
	static final String ACTION_ADD_WIKI = ACTION_ADD_PREFIX + "wiki";
	static final String ACTION_ADD_PODCAST = ACTION_ADD_PREFIX + "podcast";
	static final String ACTION_ADD_BLOG = ACTION_ADD_PREFIX + "blog";
	static final String ACTION_ADD_GLOSSARY = ACTION_ADD_PREFIX + "glossary";
	static final String ACTION_ADD_DOC = ACTION_ADD_PREFIX + "dc";
	static final String ACTION_NEW_COURSE = ACTION_ADD_PREFIX + "nco";
	static final String ACTION_NEW_CP = ACTION_ADD_PREFIX + "ncp";
	static final String ACTION_NEW_TEST = ACTION_ADD_PREFIX + "nte";
	static final String ACTION_NEW_SURVEY = ACTION_ADD_PREFIX + "nsu";
	static final String ACTION_NEW_SHAREDFOLDER = ACTION_ADD_PREFIX + "nsf";
	static final String ACTION_NEW_WIKI = ACTION_ADD_PREFIX + "nwiki";
	static final String ACTION_NEW_PODCAST = ACTION_ADD_PREFIX + "npodcast";
	static final String ACTION_NEW_BLOG = ACTION_ADD_PREFIX + "nblog";
	static final String ACTION_NEW_GLOSSARY = ACTION_ADD_PREFIX + "nglossary";
	static final String ACTION_NEW_PORTFOLIO = ACTION_ADD_PREFIX + "nportfolio";
	static final String ACTION_CANCEL = "cancel";
	static final String ACTION_FORWARD = "forward";

	private VelocityContainer repositoryadd;

	private RepositoryEditDescriptionController detailsController;
	private IAddController addController;
	private RepositoryHandler typeToAdd;
	private RepositoryAddCallback addCallback;
	private BaseSecurity securityManager;
	private RepositoryEntry addedEntry;
	
	// flag is true when workflow has been finished successfully, 
	// otherwhise when disposing the controller or in a case of 
	// user abort / cancel the system will delete temporary data
	private boolean workflowSuccessful = false;
	private Link cancelButton;
	private Link forwardButton;
	private Panel panel;
	private String actionAddCommand, actionProcess;
	
	/**
	 * Controller implementing "Add Repository Entry"-workflow.
	 * @param ureq
	 * @param wControl
	 * @param actionAddCommand
	 */
	public RepositoryAddController(UserRequest ureq, WindowControl wControl, String actionAddCommand) {
		super(ureq, wControl);
		
		setBasePackage(RepositoryManager.class);
		
		this.actionAddCommand = actionAddCommand;
		securityManager = BaseSecurityManager.getInstance();

		/*
		 * FIXME:pb: review: during constructor call -> /addDelegate.html is active first, then typeToAdd.getAddController() with
		 * this as addCallback may/should/must? call protected finished(..); which in turn replaces /addDelegate.html by /addDetails.html....
		 * what are the concepts here?
		 */
		
		repositoryadd = createVelocityContainer("addDelegate");
		cancelButton = LinkFactory.createButton("cmd.cancel", repositoryadd, this);
		cancelButton.setElementCssClass("o_sel_repo_add_cancel");
		forwardButton = LinkFactory.createButton("cmd.forward", repositoryadd, this);
		forwardButton.setElementCssClass("o_sel_repo_add_forward");
		
		String translatedTypeName = null;
		String typeIntro = null;
		addCallback = new RepositoryAddCallback(this);
		if (actionAddCommand.equals(ACTION_ADD_COURSE)) {
			typeToAdd = new CourseHandler();
			addController = typeToAdd.createAddController(addCallback, CourseHandler.PROCESS_IMPORT, ureq, getWindowControl());
			translatedTypeName = translate("add.course");
			typeIntro = translate("add.course.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_DOC)) {
			typeToAdd = new WebDocumentHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.webdoc");
			typeIntro = translate("add.webdoc.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_TEST)) {
			//get registered Handler instead of using new with a concrete Handler Class -> introduced during onxy review
			typeToAdd = RepositoryHandlerFactory.getInstance().getRepositoryHandler(TestFileResource.TYPE_NAME);
			addController = typeToAdd.createAddController(addCallback, PROCESS_ADD, ureq, getWindowControl());
			translatedTypeName = translate("add.test");
			typeIntro = translate("add.test.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_SURVEY)) {
			//get registered Handler instead of using new with a concrete Handler Class -> introduced during onxy review
			typeToAdd = RepositoryHandlerFactory.getInstance().getRepositoryHandler(SurveyFileResource.TYPE_NAME);
			addController = typeToAdd.createAddController(addCallback, PROCESS_ADD, ureq, getWindowControl());
			translatedTypeName = translate("add.survey");
			typeIntro = translate("add.survey.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_CP)) {
			typeToAdd = new ImsCPHandler();
			addController = typeToAdd.createAddController(addCallback, ImsCPHandler.PROCESS_IMPORT, ureq, getWindowControl());
			translatedTypeName = translate("add.cp");
			typeIntro = translate("add.cp.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_SCORM)) {
			typeToAdd = new SCORMCPHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.scorm");
			typeIntro = translate("add.scorm.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_WIKI)) {
			typeToAdd = new WikiHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.wiki");
			typeIntro = translate("add.wiki.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_PODCAST)) {
			typeToAdd = new PodcastHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.podcast");
			typeIntro = translate("add.podcast.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_BLOG)) {
			typeToAdd = new BlogHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.blog");
			typeIntro = translate("add.blog.intro");
		}
		else if (actionAddCommand.equals(ACTION_ADD_GLOSSARY)) {
			typeToAdd = new GlossaryHandler();
			addController = typeToAdd.createAddController(addCallback, null, ureq, getWindowControl());
			translatedTypeName = translate("add.glossary");
			typeIntro = translate("add.glossary.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_COURSE)) {
			typeToAdd = new CourseHandler();
			this.actionProcess = RepositoryAddController.PROCESS_NEW;
			addController = typeToAdd.createAddController(addCallback, CourseHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.course");
			typeIntro = translate("new.course.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_TEST)) {
			typeToAdd = RepositoryHandlerFactory.getInstance().getRepositoryHandler(TestFileResource.TYPE_NAME);
			addController = typeToAdd.createAddController(addCallback, PROCESS_NEW, ureq, getWindowControl());
			translatedTypeName = translate("new.test");
			typeIntro = translate("new.test.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_SURVEY)) {
			typeToAdd = RepositoryHandlerFactory.getInstance().getRepositoryHandler(SurveyFileResource.TYPE_NAME);
			addController = typeToAdd.createAddController(addCallback, PROCESS_NEW, ureq, getWindowControl());
			translatedTypeName = translate("new.survey");
			typeIntro = translate("new.survey.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_SHAREDFOLDER)) {
			typeToAdd = new SharedFolderHandler();
			addController = typeToAdd.createAddController(addCallback, SharedFolderHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.sharedfolder");
			typeIntro = translate("new.sharedfolder.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_WIKI)) {
			typeToAdd = new WikiHandler();
			addController = typeToAdd.createAddController(addCallback, WikiHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.wiki");
			typeIntro = translate("new.wiki.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_PODCAST)) {
			typeToAdd = new PodcastHandler();
			addController = typeToAdd.createAddController(addCallback, PodcastHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.podcast");
			typeIntro = translate("new.podcast.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_BLOG)) {
			typeToAdd = new BlogHandler();
			addController = typeToAdd.createAddController(addCallback, BlogHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.blog");
			typeIntro = translate("new.blog.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_GLOSSARY)) {
			typeToAdd = new GlossaryHandler();
			addController = typeToAdd.createAddController(addCallback, GlossaryHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.glossary");
			typeIntro = translate("new.glossary.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_PORTFOLIO)) {
			typeToAdd = new PortfolioHandler();
			addController = typeToAdd.createAddController(addCallback, PortfolioHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("new.portfolio");
			typeIntro = translate("new.portfolio.intro");
		}
		else if (actionAddCommand.equals(ACTION_NEW_CP)) {
			typeToAdd = new ImsCPHandler();
			addController = typeToAdd.createAddController(addCallback, ImsCPHandler.PROCESS_CREATENEW, ureq, getWindowControl());
			translatedTypeName = translate("tools.add.cp");
			typeIntro = translate("new.cp.intro");
		}		
		else throw new AssertException("Unsuported Repository Type.");

		// AddControllers may not need a GUI-based workflow.
		// In such cases, they do not have to return a transactional component,
		// but they must call addCallback.finished().
		Component transactionComponent = addController.getTransactionComponent();
		if (transactionComponent != null)	repositoryadd.put("subcomp", transactionComponent);
		repositoryadd.contextPut("typeHeader", (translatedTypeName == null) ?
				translate("add.header") :
				translate("add.header.specific", new String[] {translatedTypeName}));
		repositoryadd.contextPut("typeIntro", typeIntro);
		forwardButton.setEnabled(false);
		forwardButton.setTextReasonForDisabling(translate("disabledforwardreason"));
		panel = putInitialPanel(repositoryadd);
		return;
	}

	/**
	 * Used by RepositoryMainController to identify which command was executed.
	 */
	protected String getActionAddCommand() {
		return actionAddCommand;
	}
	
	/**
	 * Used by RepositoryMainController to identify which process was executed.
	 */
	protected String getActionProcess() {
		return actionProcess != null ? actionProcess : "";
	}

	/**
	 * Used by RepositoryMainController to identify which resource has been added.
	 */
	RepositoryEntry getAddedEntry() { return addedEntry; }
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == forwardButton){
			
			//FIXME: this code belongs to the repo manager and not here!
			// finish transaction and add repository entry
			if (!addController.transactionFinishBeforeCreate()) return;
			//save current name and description from create from
			String displayName = addedEntry.getDisplayname();
			String description = addedEntry.getDescription();
			// Do set access for owner at the end, because unfinished course should be invisible
			addedEntry = (RepositoryEntry) DBFactory.getInstance().loadObject(addedEntry); // need a reload from hibernate because create a new cp load a repository-entry (OLAT-5631) TODO: 7.1 Refactor in method getRepositoryEntry()
			addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
			addedEntry.setDisplayname(displayName);
			addedEntry.setDescription(description);
			RepositoryManager.getInstance().updateRepositoryEntry(addedEntry);
			addController.repositoryEntryCreated(addedEntry);
			
			workflowSuccessful = true;
			
			// do logging
			ThreadLocalUserActivityLogger.log(LearningResourceLoggingAction.LEARNING_RESOURCE_CREATE, getClass(),
					LoggingResourceable.wrap(addedEntry, OlatResourceableType.genRepoEntry));
			
			fireEvent(ureq, Event.DONE_EVENT);
			fireEvent(ureq, new EntryChangedEvent(addedEntry, EntryChangedEvent.ADDED));
			return;
		} else if (source == cancelButton){
			// FIXME:pb: review is it really as intended to pass here from /addDelegate.html or /addDetails.html
			// clean up temporary data and abort transaction
			cleanup();
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}
	}
	
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == detailsController) {
			if (event == Event.CANCELLED_EVENT) {
				// clean up temporary data and abort transaction
				cleanup();
				fireEvent(ureq, Event.CANCELLED_EVENT);
				return;
			} else if (event == Event.DONE_EVENT) {
				forwardButton.setEnabled(true);
				addedEntry = detailsController.getRepositoryEntry();
			}
		}
	}

	protected void addFinished(UserRequest ureq) {
		try {
			DBFactory.getInstance().commitAndCloseSession();
		} catch (Exception e) {
			logError("", e);
		}
		
		addedEntry = RepositoryManager.getInstance()
			.createRepositoryEntryInstance(ureq.getIdentity().getName());

		addedEntry.setCanDownload(false);
		addedEntry.setCanLaunch(typeToAdd.supportsLaunch(addedEntry));
		String dispName = addCallback.getDisplayName();
		if (dispName == null) dispName = "";
		addedEntry.setDisplayname(dispName);
		String resName = addCallback.getResourceName();
		if (resName == null) resName = "";
		addedEntry.setResourcename(resName);
		
		String resDescription = addCallback.getDescription();
		if(resDescription == null){
			resDescription = "";
		}
		addedEntry.setDescription(resDescription);
		
    // Do set access for owner at the end, because unfinished course should be invisible
		// addedEntry.setAccess(RepositoryEntry.ACC_OWNERS);
		addedEntry.setAccess(0);//Access for nobody
		
		// Set the resource on the repository entry and save the entry.
		RepositoryManager rm = RepositoryManager.getInstance();
		OLATResource ores = OLATResourceManager.getInstance().findOrPersistResourceable(addCallback.getResourceable());
		addedEntry.setOlatResource(ores);
		
		// create security groups
		//security group for owners / authors
		SecurityGroup newGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_ACCESS, newGroup);
		// members of this group are always authors also
		securityManager.createAndPersistPolicy(newGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_AUTHOR);
		
		securityManager.addIdentityToSecurityGroup(ureq.getIdentity(), newGroup);
		addedEntry.setOwnerGroup(newGroup);
		
		//fxdiff VCRP-1,2: access control of resources
		// security group for tutors / coaches
		SecurityGroup tutorGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_ACCESS, addedEntry.getOlatResource());
		// members of this group are always tutors also
		securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_TUTOR);
		addedEntry.setTutorGroup(tutorGroup);
		
		// security group for participants
		SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
		// member of this group may modify member's membership
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_ACCESS, addedEntry.getOlatResource());
		// members of this group are always participants also
		securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_PARTICIPANT);
		addedEntry.setParticipantGroup(participantGroup);
		
		rm.saveRepositoryEntry(addedEntry);

		removeAsListenerAndDispose(detailsController);
		detailsController = new RepositoryEditDescriptionController(ureq, getWindowControl(), addedEntry, true);
		listenTo(detailsController);
		
		repositoryadd.put("details", detailsController.getInitialComponent());
		// try to get type description based on handlertype
		repositoryadd.contextPut("header",
				translate("add.header.specific", new String[] {translate(ores.getResourceableTypeName())}));
		repositoryadd.setPage(VELOCITY_ROOT + "/addDetails.html");
		
		DBFactory.getInstance().commitAndCloseSession();
	}

	protected void addCanceled(UserRequest ureq) {
		cleanup();
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	protected void addFailed(UserRequest ureq) {
		cleanup();
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	private void cleanup() {
		//FIXME: this belongs to manager code!
		if (detailsController != null) {
			addedEntry = detailsController.getRepositoryEntry();
			if (addedEntry != null) {
				RepositoryManager.getInstance().deleteRepositoryEntryAndBasesecurity(addedEntry);
			}
			if (detailsController != null) {
				detailsController.dispose();
				detailsController = null;
			}
		}
		// tell add controller about this
		if (addController != null){
			addController.transactionAborted();
		}
		getLogger().debug("cleanup : finished");
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		if (!workflowSuccessful) {
			cleanup();
		} 
		// OLAT-4619 In any case execute controller dispose chain (e.g. clean tmp upload files)
		if (addController != null) {
			addController.dispose();
			addController = null;
		}
	}
}
