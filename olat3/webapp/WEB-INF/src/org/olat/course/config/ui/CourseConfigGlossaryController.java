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
 * <p>
 */

package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.course.config.CourseConfig;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;

/**
 * 
 * Description: <br>
 * Course config controller to modify the course glossary. The controller allows
 * the user to enable / disable the course glossary by setting a
 * <p>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CourseConfigGlossaryController extends BasicController implements ControllerEventListener {

	private OLog log = Tracing.createLoggerFor(this.getClass());
	public static final String VALUE_EMPTY_GLOSSARY_FILEREF = "gf.notconfigured";
	private static final String COMMAND_REMOVE = "command.glossary.remove";
	private static final String COMMAND_ADD = "command.glossary.add";

	private VelocityContainer myContent;
	private ReferencableEntriesSearchController repoSearchCtr;
	private Link addCommand, removeCommand;

	private CloseableModalController cmc;
	private CourseConfig courseConfig;
	private Long courseResourceableId;
	private ILoggingAction loggingAction;
	

	/**
	 * Constructor
	 * 
	 * @param ureq
	 * @param wControl
	 * @param course
	 */
	public CourseConfigGlossaryController(UserRequest ureq, WindowControl wControl, CourseConfig courseConfig, Long courseResourceableId) {
		super(ureq, wControl);
		this.courseConfig = courseConfig;
		this.courseResourceableId = courseResourceableId;
		
		myContent = createVelocityContainer("CourseGlossary");
		
		if (courseConfig.hasGlossary()) {
			RepositoryEntry repoEntry = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(), false);
			if (repoEntry == null) {
				// Something is wrong here, maybe the glossary has been deleted. Try to
				// remove glossary from configuration
				doRemoveGlossary(ureq);
				log.warn("Course with ID::" + courseResourceableId + " had a config for a glossary softkey::"
						+ courseConfig.getGlossarySoftKey() + " but no such glossary was found");				
			} else {
				removeCommand = LinkFactory.createButton(COMMAND_REMOVE, myContent, this);
				myContent.contextPut("repoEntry", repoEntry);
			}
		} else {
			addCommand = LinkFactory.createButton(COMMAND_ADD, myContent, this);
		}		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == addCommand) {
			repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, translate("select"));			
			listenTo(repoSearchCtr);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent());
			cmc.activate();
		} else if (source == removeCommand) {
			doRemoveGlossary(ureq);
			fireEvent(ureq, Event.CHANGED_EVENT);// FIXME:pb:send event to agency
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == repoSearchCtr) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = repoSearchCtr.getSelectedEntry();
				doSelectGlossary(repoEntry, ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);// FIXME:pb:send event to agency
			}
		}
	}

	/**
	 * Updates config with selected glossary
	 * 
	 * @param repoEntry
	 * @param ureq
	 */
	private void doSelectGlossary(RepositoryEntry repoEntry, UserRequest ureq) {
		
		String softkey = repoEntry.getSoftkey();
		courseConfig.setGlossarySoftKey(softkey);		
		loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_ENABLED;
		
		// update view
		myContent.contextPut("repoEntry", repoEntry);
		myContent.remove(addCommand);
		removeCommand = LinkFactory.createButton(COMMAND_REMOVE, myContent, this);
		if (log.isDebug()) {
		  log.debug("Set new glossary softkey::" + courseConfig.getGlossarySoftKey() + " for course with ID::" + courseResourceableId);
		}	
		this.fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * Removes the current glossary from the configuration
	 * 
	 * @param ureq
	 */
	private void doRemoveGlossary(UserRequest ureq) {
		
		courseConfig.setGlossarySoftKey(null);				
		loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_DISABLED;
		
		// update view
		myContent.contextRemove("repoEntry");
		myContent.remove(removeCommand);
		addCommand = LinkFactory.createButton(COMMAND_ADD, myContent, this);
		
		if (log.isDebug()) {
			log.debug("Removed glossary softkey for course with ID::" + courseResourceableId);
		}		
		this.fireEvent(ureq, Event.CHANGED_EVENT);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
    //auto dispose by basic controller
	}

	public ILoggingAction getLoggingAction() {
		return loggingAction;
	}
}