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
package org.olat.course.nodeaccess.ui;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.assessment.STLearningPathConfigs;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Aug 24, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MigrationSelectionController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(MigrationSelectionController.class);

	private SingleSelection designEl;

	private final RepositoryEntry repositoryEntry;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private LearningPathService learningPathService;

	public MigrationSelectionController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl);
		this.repositoryEntry = repositoryEntry;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues designKV = new SelectionValues();
		designKV.add(new SelectionValues.SelectionValue(CourseModule.COURSE_TYPE_PATH, translate("course.design.path"), translate("course.design.path.desc"), "o_course_design_path_icon", null, true));
		designKV.add(new SelectionValues.SelectionValue(CourseModule.COURSE_TYPE_PROGRESS, translate("course.design.progress"), translate("course.design.progress.desc"), "o_course_design_progress_icon", null, true));
		designEl = uifactory.addCardSingleSelectHorizontal("course.design", "course.design", formLayout, designKV);
		designEl.setElementCssClass("o_course_design");
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!StringHelper.containsNonWhitespace(defaultCourseType)) {
			defaultCourseType = CourseModule.COURSE_TYPE_PATH;
		}
		designEl.select(defaultCourseType, true);

		// buttons
		FormLayoutContainer buttonLayoutCont = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayoutCont);
		uifactory.addFormSubmitButton("apply", buttonLayoutCont);
		uifactory.addFormCancelButton("cancel", buttonLayoutCont, ureq, getWindowControl());
	}

	private void doMigrate(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		List<CourseNode> unsupportedCourseNodes = learningPathService.getUnsupportedCourseNodes(course);
		if (!unsupportedCourseNodes.isEmpty()) {
			showUnsupportedMessage(ureq, unsupportedCourseNodes);
			return;
		}

		RepositoryEntry lpEntry = learningPathService.migrate(repositoryEntry, getIdentity());
		String bPath = "[RepositoryEntry:" + lpEntry.getKey() + "]";
		if (CourseModule.COURSE_TYPE_PROGRESS.equals(designEl.getSelectedKey())) {
			initProgressCourseConfig(lpEntry);
		}
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void showUnsupportedMessage(UserRequest ureq, List<CourseNode> unsupportedCourseNodes) {
		UnsupportedCourseNodesController unsupportedCourseNodesCtrl =
				new UnsupportedCourseNodesController(ureq, getWindowControl(), unsupportedCourseNodes);
		listenTo(unsupportedCourseNodesCtrl);

		CloseableModalController cmc = new CloseableModalController(getWindowControl(), translate("close"),
				unsupportedCourseNodesCtrl.getInitialComponent(), true, translate("unsupported.course.nodes.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void initProgressCourseConfig(RepositoryEntry repositoryEntry) {
		OLATResourceable courseOres = repositoryEntry.getOlatResource();
		if (CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			log.warn("Not able to set the course node access type: Edit session is already open!");
			return;
		}

		ICourse course = CourseFactory.openCourseEditSession(courseOres.getResourceableId());
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		courseConfig.setMenuPathEnabled(false);
		courseConfig.setMenuNodeIconsEnabled(true);

		ModuleConfiguration runConfig = course.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration();
		runConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);

		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode) course.getEditorTreeModel().getRootNode();
		ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
		editorConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);

		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(repositoryEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doMigrate(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
