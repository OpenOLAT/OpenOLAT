/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.config.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: Jun 11, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CoachRightsController extends FormBasicController {
	
	private MultipleSelectionElement coachesCanEl;
	
	private final RepositoryEntry courseEntry;
	private final boolean editable;

	public CoachRightsController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, boolean editable) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.editable = editable;
		
		initForm(ureq);
		updateUI();
		update();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("user.rights");
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		
		SelectionValues coachesCanSV = new SelectionValues();
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_PASSED_MANUALLY, translate("options.passed.manually")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_RESET_DATA, translate("options.reset.data")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_GRADE_APPLY, translate("options.grade.apply")));
		coachesCanSV.add(SelectionValues.entry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, translate("options.user.visibility")));
		coachesCanEl = uifactory.addCheckboxesVertical("options.coaches.can", formLayout, coachesCanSV.keys(), coachesCanSV.values(), 1);
		coachesCanEl.addActionListener(FormEvent.ONCHANGE);
		coachesCanEl.setEnabled(editable);
		coachesCanEl.select(STCourseNode.CONFIG_COACH_GRADE_APPLY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY));
		coachesCanEl.select(STCourseNode.CONFIG_COACH_USER_VISIBILITY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY));
		coachesCanEl.select(STCourseNode.CONFIG_PASSED_MANUALLY, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_MANUALLY));
		coachesCanEl.select(STCourseNode.CONFIG_COACH_RESET_DATA, moduleConfig.getBooleanSafe(STCourseNode.CONFIG_COACH_RESET_DATA));
		
		if (editable) {
			FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonCont.setRootForm(mainForm);
			formLayout.add(buttonCont);
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	private void updateUI() {
		boolean userVisibility = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		if (userVisibility) {
			coachesCanEl.setEnabled(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			coachesCanEl.setEnabled(STCourseNode.CONFIG_PASSED_MANUALLY, false);
			coachesCanEl.select(STCourseNode.CONFIG_PASSED_MANUALLY, false);
		}
	}
	
	public void update() {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		ModuleConfiguration moduleConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		boolean passedEnabled = moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_PROGRESS)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_ALL)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_NUMBER)
				|| moduleConfig.getBooleanSafe(STCourseNode.CONFIG_PASSED_POINTS);
		
		if (passedEnabled) {
			coachesCanEl.setVisible(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			coachesCanEl.setVisible(STCourseNode.CONFIG_PASSED_MANUALLY, false);
			coachesCanEl.select(STCourseNode.CONFIG_PASSED_MANUALLY, false);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == coachesCanEl) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		OLATResourceable courseOres = courseEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		ModuleConfiguration runConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
		
		boolean gradeApply = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_GRADE_APPLY);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_GRADE_APPLY, gradeApply);
		
		boolean userVisibility = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_USER_VISIBILITY, userVisibility);
		
		boolean passedManually = coachesCanEl.isKeySelected(STCourseNode.CONFIG_PASSED_MANUALLY);
		if (passedManually) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_PASSED_MANUALLY, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
			editorConfig.remove(STCourseNode.CONFIG_PASSED_MANUALLY);
		}

		boolean resetData = coachesCanEl.isKeySelected(STCourseNode.CONFIG_COACH_RESET_DATA);
		if (resetData) {
			runConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_RESET_DATA, true);
			editorConfig.setBooleanEntry(STCourseNode.CONFIG_COACH_RESET_DATA, true);
		} else {
			runConfig.remove(STCourseNode.CONFIG_COACH_RESET_DATA);
			editorConfig.remove(STCourseNode.CONFIG_COACH_RESET_DATA);
		}
		
		CourseFactory.saveCourse(courseEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

}
