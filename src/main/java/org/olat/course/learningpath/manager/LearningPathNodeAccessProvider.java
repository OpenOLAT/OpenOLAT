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
package org.olat.course.learningpath.manager;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.navigation.NodeVisitedListener;
import org.olat.course.run.userview.CourseTreeNodeBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LearningPathNodeAccessProvider implements NodeAccessProvider, NodeVisitedListener {

	public static final String TYPE = "learningpath";
	
	@Autowired
	private LearningPathRegistry registry;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	private LearningPathConfigs getConfigs(CourseNode courseNode) {
		return registry.getLearningPathNodeHandler(courseNode).getConfigs(courseNode);
	}
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public String getDisplayName(Locale locale) {
		Translator translator = Util.createPackageTranslator(LearningPathNodeConfigController.class, locale);
		return translator.translate("access.provider.name");
	}

	@Override
	public boolean isSupported(String courseNodeType) {
		return registry.getLearningPathNodeHandler(courseNodeType).isSupported();
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel) {
		Controller configCtrl = registry.getLearningPathNodeHandler(courseNode).createConfigEditController(ureq, wControl, courseNode);
		return new TabbableLeaningPathNodeConfigController(ureq, wControl, configCtrl);
	}

	@Override
	public CourseTreeNodeBuilder getNodeEvaluationBuilder(UserCourseEnvironment userCourseEnvironment) {
		return new LearningPathCourseTreeNodeBuilder(userCourseEnvironment);
	}

	@Override
	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		boolean doneOnNodeStarted = getConfigs(courseNode).isDoneOnNodeVisited();
		boolean participant = userCourseEnvironment.isParticipant();
		if (participant && doneOnNodeStarted) {
			courseAssessmentService.updateAssessmentStatus(courseNode, userCourseEnvironment,
					AssessmentEntryStatus.done, Role.user);
			return true;
		}
		return false;
	}

	@Override
	public void onCompletionUpdate(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			Double completion, AssessmentRunStatus runStatus, Role by) {
		boolean isDoneOnCompletion = getConfigs(courseNode).isDoneOnCompletion(completion);
		boolean isDoneOnRunStatus = getConfigs(courseNode).isDoneOnRunStatus(runStatus);;
		boolean participant = userCourseEnvironment.isParticipant();
		if (participant && (isDoneOnCompletion || isDoneOnRunStatus)) {
			courseAssessmentService.updateAssessmentStatus(courseNode, userCourseEnvironment,
					AssessmentEntryStatus.done, by);
		}
	}

}
