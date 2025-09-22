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
package org.olat.repository.ui.author.copy;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.ui.author.copy.wizard.CopyCourseWizardController;

/**
 * Initial date: 17.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyRepositoryEntryWrapperController extends BasicController {
	
	private CopyCourseWizardController copyLearningPathCourseWizardController;
	private CopyRepositoryEntryController copyRepositoryEntryController;	
	private CloseableModalController cmc;
	
	private final RepositoryEntry repositoryEntry;

	/**
	 * @param ureq
	 * @param wControl
	 * @param repositoryEntry
	 * @param useCourseWizard
	 * @param saveAsTemplate
	 */
	public CopyRepositoryEntryWrapperController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, boolean useCourseWizard, boolean saveAsTemplate) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		this.repositoryEntry = repositoryEntry;
		
		if (useCourseWizard && repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.ORES_TYPE_COURSE)) {
			ICourse course = CourseFactory.loadCourse(repositoryEntry);
			
			if (course != null && LearningPathNodeAccessProvider.TYPE.equals(course.getCourseConfig().getNodeAccessType().getType())) {
				copyLearningPathCourseWizardController = new CopyCourseWizardController(ureq, wControl, repositoryEntry, course, saveAsTemplate);
				listenTo(copyLearningPathCourseWizardController);
				return;
			}
		}
		
		copyRepositoryEntryController = new CopyRepositoryEntryController(ureq, getWindowControl(), repositoryEntry, saveAsTemplate);
		String title = saveAsTemplate ? translate("details.save.as.template") : translate("details.copy");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), copyRepositoryEntryController.getInitialComponent(), true, title);
		
		listenTo(cmc);
		listenTo(copyRepositoryEntryController);
		
		cmc.activate();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		fireEvent(ureq, event);		
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(copyRepositoryEntryController == source) {
			if (event == Event.CLOSE_EVENT) {
				if(cmc != null) {
					cmc.deactivate();
				}
			} else {
				if(cmc != null) {
					cmc.deactivate();
				}
				if (event == Event.DONE_EVENT) {
					launchCopiedCourse(ureq, copyRepositoryEntryController.getCopiedEntry());
				}
				cleanUp();
			}
		} else if (copyLearningPathCourseWizardController == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				launchCopiedCourse(ureq, copyLearningPathCourseWizardController.getCopiedEntry());
			}
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
	}
	
	private void launchCopiedCourse(UserRequest ureq, RepositoryEntryRef copy) {
		String businessPath = "[RepositoryEntry:" + copy.getKey() + "][EditDescription:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		
		EntryChangedEvent e = new EntryChangedEvent(repositoryEntry, getIdentity(), Change.added, "runtime");
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(copyLearningPathCourseWizardController);
		removeAsListenerAndDispose(copyRepositoryEntryController);
		removeAsListenerAndDispose(cmc);
		
		copyLearningPathCourseWizardController = null;
		copyRepositoryEntryController = null;
		cmc = null;
	}
	
}
