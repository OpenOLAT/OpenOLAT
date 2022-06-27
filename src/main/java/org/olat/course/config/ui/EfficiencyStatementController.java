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
package org.olat.course.config.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.run.RunMainController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EfficiencyStatementController extends FormBasicController {

	private MultipleSelectionElement efficencyEl;

	private DialogBoxController enableEfficiencyDC, disableEfficiencyDC;
	
	private final RepositoryEntry entry;
	private CourseConfig courseConfig;
	private final boolean editable;

	@Autowired
	private EfficiencyStatementManager efficiencyStatementManager;

	public EfficiencyStatementController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			CourseConfig courseConfig, boolean editable) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RunMainController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.courseConfig = courseConfig;
		this.editable = editable;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("options.efficency.title");
		setFormContextHelp("manual_user/course_create/Course_Settings/#assessment");
		formLayout.setElementCssClass("o_sel_course_efficiency_statements");
		
		boolean effEnabled = courseConfig.isEfficencyStatementEnabled();
		boolean managedEff = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.efficencystatement);
		efficencyEl = uifactory.addCheckboxesHorizontal("effIsOn", "chkbx.efficency.onoff", formLayout, new String[] {"xx"}, new String[] {""});
		efficencyEl.addActionListener(FormEvent.ONCHANGE);
		efficencyEl.select("xx", effEnabled);
		efficencyEl.setEnabled(editable && !managedEff);
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == disableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig(ureq);
			} else {
				efficencyEl.select("xx", true);
			}
		} else if (source == enableEfficiencyDC) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doChangeConfig(ureq);
			} else {
				efficencyEl.select("xx", false);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}
	
	private void doSave(UserRequest ureq) {
		boolean confirmUpdateStatement = courseConfig.isEfficencyStatementEnabled() != efficencyEl.isSelected(0);
		if (confirmUpdateStatement) {
			if (courseConfig.isEfficencyStatementEnabled()) {
				// a change from enabled Efficiency to disabled
				disableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.todisabled"), disableEfficiencyDC);
			} else {
				// a change from disabled Efficiency
				enableEfficiencyDC = activateYesNoDialog(ureq, null, translate("warning.change.toenable"), enableEfficiencyDC);
			}
		} else {
			doChangeConfig(ureq);
		}
	}
	
	private void doChangeConfig(UserRequest ureq) {
		OLATResourceable courseOres = entry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		boolean enableEfficiencyStatment = efficencyEl.isSelected(0);
		boolean updateStatement = courseConfig.isEfficencyStatementEnabled() != enableEfficiencyStatment;
		courseConfig.setEfficencyStatementIsEnabled(enableEfficiencyStatment);

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		if(updateStatement) {
			if(enableEfficiencyStatment) {
				// first create the efficiencies, send event to agency (all courses add link)
				RepositoryEntry courseRe = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				List<Identity> identitiesWithData = course.getCourseEnvironment().getCoursePropertyManager().getAllIdentitiesWithCourseAssessmentData(null);
				efficiencyStatementManager.updateEfficiencyStatements(courseRe, identitiesWithData);							
			} else {
				// delete really the efficiencies of the users.
				RepositoryEntry courseRepoEntry = RepositoryManager.getInstance().lookupRepositoryEntry(course, true);
				efficiencyStatementManager.deleteEfficiencyStatementsFromCourse(courseRepoEntry.getKey());						
			}
			
			//inform everybody else		
			EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
			CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigType.efficiencyStatement, course.getResourceableId());
			eventBus.fireEventToListenersOf(courseConfigEvent, course);
			
			ILoggingAction loggingAction = enableEfficiencyStatment ?
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_ENABLED :
					LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_EFFICIENCY_STATEMENT_DISABLED;
			ThreadLocalUserActivityLogger.log(loggingAction, getClass());
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
