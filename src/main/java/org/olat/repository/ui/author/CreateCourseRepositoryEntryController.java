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
package org.olat.repository.ui.author;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.controllers.accordion.AssistanceAccordionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.st.assessment.STLearningPathConfigs;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.handlers.RepositoryHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateCourseRepositoryEntryController extends CreateRepositoryEntryController {

	private static final Logger log = Tracing.createLoggerFor(CreateCourseRepositoryEntryController.class);
	
	private SingleSelection designEl;
	
	private CourseAssistanceController courseAssistanceCtrl;
	private AssistanceAccordionController assistanceCtrl;

	@Autowired
	private CourseModule courseModule;

	public CreateCourseRepositoryEntryController(UserRequest ureq, WindowControl wControl, RepositoryHandler handler,
			boolean wizardsEnabled) {
		super(ureq, wControl, handler, wizardsEnabled);
	}
	
	@Override
	protected boolean hasLifecycle() {
		return true;
	}
	
	@Override
	protected boolean hasEducationalType() {
		return true;
	}

	@Override
	protected void initAdditionalFormElements(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues designKV = new SelectionValues();
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_PATH, translate("course.design.path"), translate("course.design.path.desc"),"o_course_design_path_icon", null, true));
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_PROGRESS, translate("course.design.progress"), translate("course.design.progress.desc"),"o_course_design_progress_icon", null, true));
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_CLASSIC, translate("course.design.classic"), translate("course.design.classic.desc"),"o_course_design_classic_icon", null, true));
		designEl = uifactory.addCardSingleSelectHorizontal("course.design", "course.design", formLayout, designKV);
		designEl.setElementCssClass("o_course_design");
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!StringHelper.containsNonWhitespace(defaultCourseType)) {
			defaultCourseType = CourseModule.COURSE_TYPE_PATH;
		}
		designEl.select(defaultCourseType, true);
		
		FormLayoutContainer assistanceCont = FormLayoutContainer.createCustomFormLayout("assistanceCont", getTranslator(), velocity_root + "/course_assistance_container.html");
		assistanceCont.setRootForm(mainForm);
		formLayout.add(assistanceCont);
		
		courseAssistanceCtrl = new CourseAssistanceController(ureq, getWindowControl());
		listenTo(courseAssistanceCtrl);
		
		assistanceCtrl = new AssistanceAccordionController(ureq, getWindowControl(), getTranslator(), "assistance");
		listenTo(assistanceCtrl);
		assistanceCont.put("assistance", assistanceCtrl.getInitialComponent());
		assistanceCtrl.setHelpLink("help.additional.informations", "manual_user/course_create/General_Information/");
		assistanceCtrl.addQuestionAnswer("course.design.help.compare", null, new Component[] {courseAssistanceCtrl.getInitialComponent()});
	}

	@Override
	protected void afterEntryCreated() {
		String type = CourseModule.COURSE_TYPE_CLASSIC.equals(designEl.getSelectedKey())
				? ConditionNodeAccessProvider.TYPE
				: LearningPathNodeAccessProvider.TYPE;
		CourseFactory.initNodeAccessType(repositoryEntry, NodeAccessType.of(type));
		repositoryEntry = repositoryManager.setTechnicalType(repositoryEntry, type);
		
		if (CourseModule.COURSE_TYPE_PROGRESS.equals(designEl.getSelectedKey())) {
			initProgressCourseConfig();
		}
	}

	private void initProgressCourseConfig() {
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
		
		CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode)course.getEditorTreeModel().getRootNode();
		ModuleConfiguration editorConfig = courseEditorTreeNode.getCourseNode().getModuleConfiguration();
		editorConfig.setStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.saveCourse(repositoryEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
	}
	
}
