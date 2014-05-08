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

package org.olat.course.config.ui;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.StringResourceableType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.fileresource.types.GlossaryResource;
import org.olat.modules.glossary.GlossaryManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.resource.references.ReferenceImpl;
import org.olat.resource.references.ReferenceManager;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * Description: <br>
 * Course config controller to modify the course glossary. The controller allows
 * the user to enable / disable the course glossary by setting a
 * <p>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class CourseConfigGlossaryController extends FormBasicController implements ControllerEventListener {

	private static final OLog log = Tracing.createLoggerFor(CourseConfigGlossaryController.class);
	public static final String VALUE_EMPTY_GLOSSARY_FILEREF = "gf.notconfigured";
	private static final String COMMAND_REMOVE = "command.glossary.remove";
	private static final String COMMAND_ADD = "command.glossary.add";

	private FormLink addCommand, removeCommand;
	private StaticTextElement reNameEl;

	private CloseableModalController cmc;
	private ReferencableEntriesSearchController repoSearchCtr;
	
	private CourseConfig courseConfig;
	private final OLATResourceable courseOres;
	private final RepositoryManager repositoryService;
	private final ReferenceManager refM;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 * @param courseOres
	 * @param courseConfig
	 * @param editable
	 */
	public CourseConfigGlossaryController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		this.courseConfig = courseConfig;
		this.courseOres = courseOres;
		refM = ReferenceManager.getInstance();
		repositoryService = CoreSpringFactory.getImpl(RepositoryManager.class);
		initForm(ureq);
		
		if (courseConfig.hasGlossary()) {
			RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntryBySoftkey(courseConfig.getGlossarySoftKey(), false);
			if (repoEntry == null) {
				// Something is wrong here, maybe the glossary has been deleted. Try to
				// remove glossary from configuration
				doRemoveGlossary(ureq);
				log.warn("Course with ID::" + courseOres + " had a config for a glossary softkey::"
						+ courseConfig.getGlossarySoftKey() + " but no such glossary was found");				
			} else if(editable) {
				removeCommand.setEnabled(true);
			}
		} else if(editable) {
			addCommand.setVisible(true);
		}
		
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.glossary.title");
		setFormContextHelp("org.olat.course.config.ui","course-glossary.html","help.hover.course-gloss");
		
		String text = translate("glossary.no.glossary");
		reNameEl = uifactory.addStaticTextElement("repoName", "glossary.isconfigured", text, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);

		removeCommand = uifactory.addFormLink(COMMAND_REMOVE, buttonsCont, Link.BUTTON);
		addCommand = uifactory.addFormLink(COMMAND_ADD, buttonsCont, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == addCommand) {
			repoSearchCtr = new ReferencableEntriesSearchController(getWindowControl(), ureq, GlossaryResource.TYPE_NAME, translate("select"));			
			listenTo(repoSearchCtr);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), repoSearchCtr.getInitialComponent());
			listenTo(cmc);
			cmc.activate();
		} else if (source == removeCommand) {
			doRemoveGlossary(ureq);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == repoSearchCtr) {
			cmc.deactivate();
			if (event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				RepositoryEntry repoEntry = repoSearchCtr.getSelectedEntry();
				doSelectGlossary(repoEntry, ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(repoSearchCtr);
		removeAsListenerAndDispose(cmc);
		repoSearchCtr = null;
		cmc = null;
	}

	/**
	 * Updates config with selected glossary
	 * 
	 * @param repoEntry
	 * @param ureq
	 */
	private void doSelectGlossary(RepositoryEntry repoEntry, UserRequest ureq) {
		reNameEl.setValue(StringHelper.escapeHtml(repoEntry.getDisplayname()));
		removeCommand.setEnabled(true);
		saveConfig(ureq, repoEntry.getSoftkey());
	}

	/**
	 * Removes the current glossary from the configuration
	 * 
	 * @param ureq
	 */
	private void doRemoveGlossary(UserRequest ureq) {			
		reNameEl.setValue(translate("glossary.no.glossary"));
		removeCommand.setEnabled(false);
		saveConfig(ureq, null);
	}
	
	private void saveConfig(UserRequest ureq, String softKey) {
		final String deleteGlossarySoftKey = courseConfig.getGlossarySoftKey();
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setGlossarySoftKey(softKey);
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(),true);
		
		ILoggingAction loggingAction = (softKey == null) ?
				LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_DISABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_GLOSSARY_ENABLED;
		
		

		LoggingResourceable lri = null;
		if(softKey != null) {
			lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, softKey, softKey);
		} else if (deleteGlossarySoftKey != null) {
			lri = LoggingResourceable.wrapNonOlatResource(StringResourceableType.glossarySoftKey, deleteGlossarySoftKey, deleteGlossarySoftKey);
		}
		if (lri != null) {
			ThreadLocalUserActivityLogger.log(loggingAction, getClass(), lri);
		}
		if(softKey == null) {
			// remove references
			List<ReferenceImpl> repoRefs = refM.getReferences(course);
			for (ReferenceImpl ref:repoRefs) {
				if (ref.getUserdata().equals(GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER)) {
					refM.delete(ref);
					continue;
				}
			}
		} else {
			// update references
			RepositoryEntry repoEntry = repositoryService.lookupRepositoryEntryBySoftkey(softKey, false);
			refM.addReference(course, repoEntry.getOlatResource(), GlossaryManager.GLOSSARY_REPO_REF_IDENTIFYER); 
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}