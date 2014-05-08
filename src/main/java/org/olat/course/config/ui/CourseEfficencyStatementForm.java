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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.EfficiencyStatementManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
/**
 * 
 * Description:<br>
 * TODO: guido Class Description for CourseEfficencyStatementForm
 * 
 */
public class CourseEfficencyStatementForm extends FormBasicController {

	private SelectionElement isOn;
	private final boolean enabled;
	private final boolean editable;
	private CourseConfig courseConfig;
	private final OLATResourceable courseOres;
	
	private DialogBoxController enableEfficiencyDC, disableEfficiencyDC;

	/**
	 * @param name
	 * @param chatEnabled
	 */
	public CourseEfficencyStatementForm(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		this.courseConfig = courseConfig;
		this.courseOres = OresHelper.clone(courseOres);
		enabled = courseConfig.isEfficencyStatementEnabled();
		this.editable = editable;
		initForm (ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("org.olat.course.config.ui","course-efficiency.html","help.hover.course-eff");
		
		isOn = uifactory.addCheckboxesVertical("isOn", "chkbx.efficency.onoff", formLayout, new String[] {"xx"}, new String[] {""}, null, 1);
		isOn.select("xx", enabled);
		isOn.setEnabled(editable);
		
		if(editable) {
			uifactory.addFormSubmitButton("save", "save", formLayout);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == disableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == enableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {				
				doChangeConfig();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (courseConfig.isEfficencyStatementEnabled()) {
			// a change from enabled Efficiency to disabled
			disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
		} else {
			// a change from disabled Efficiency
			enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
		}
	}
	
	private void doChangeConfig() {
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		boolean enable = isOn.isSelected(0);
		
		ILoggingAction loggingAction = enable ?
				LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED :
				LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;

		courseConfig.setEfficencyStatementIsEnabled(enable);
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if(enable) {
            // first create the efficiencies, send event to agency (all courses add link)
			List<Identity> identitiesWithData = course.getCourseEnvironment().getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(null);
			EfficiencyStatementManager.getInstance().updateEfficiencyStatements(course, identitiesWithData);							
		} else {
            // delete really the efficiencies of the users.
			RepositoryEntry courseRepoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
			EfficiencyStatementManager.getInstance().deleteEfficiencyStatementsFromCourse(courseRepoEntry.getKey());						
		}
		//inform everybody else		
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigEvent.EFFICIENCY_STATEMENT_TYPE, course.getResourceableId());
		eventBus.fireEventToListenersOf(courseConfigEvent, course);
		ThreadLocalUserActivityLogger.log(loggingAction, getClass());
	}
}