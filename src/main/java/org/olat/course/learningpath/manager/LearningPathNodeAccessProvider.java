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
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.navigation.NodeVisitedListener;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
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
		RepositoryEntry courseEntry = userCourseEnvironment.getCourseEditorEnv().getCourseGroupManager().getCourseEntry();
		Controller configCtrl = registry.getLearningPathNodeHandler(courseNode).createConfigEditController(ureq, wControl, courseEntry, courseNode);
		return new TabbableLeaningPathNodeConfigController(ureq, wControl, configCtrl);
	}

	@Override
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		return new LearningPathCourseTreeModelBuilder(userCourseEnv);
	}

	@Override
	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnNodeVisited();
		boolean participant = userCourseEnv.isParticipant();
		if (participant && result.isFullyAssessed()) {
			AssessmentEntryStatus status = getStatus(courseNode, userCourseEnv, result.isDone());
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv, Boolean.TRUE,
					status, Role.user);
			return true;
		}
		return false;
	}

	@Override
	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnConfirmation();
		boolean participant = userCourseEnv.isParticipant();
		boolean confirmationEnabled = participant && result.isFullyAssessed();
		return confirmationEnabled;
	}

	@Override
	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnConfirmation();
		boolean participant = userCourseEnv.isParticipant();
		if (participant && result.isFullyAssessed()) {
			AssessmentEntryStatus status = getStatus(courseNode, userCourseEnv, result.isDone());
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv, Boolean.TRUE,
					status, Role.user);
		}
	}

	@Override
	public void onCompletionUpdate(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			Double completion, AssessmentEntryStatus status, Role by) {
		FullyAssessedResult onCompletion = getConfigs(courseNode).isFullyAssessedOnCompletion(completion);
		FullyAssessedResult onRunStatus = getConfigs(courseNode).isFullyAssessedOnStatus(status);
		boolean isFullyAssessed = onCompletion.isFullyAssessed() || onRunStatus.isFullyAssessed();
		boolean participant = userCourseEnv.isParticipant();
		if (participant && isFullyAssessed) {
			boolean isDone = onCompletion.isDone() || onRunStatus.isDone();
			AssessmentEntryStatus newStatus = getStatus(courseNode, userCourseEnv, isDone);
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv, Boolean.TRUE,
					newStatus, by);
		}
	}

	private AssessmentEntryStatus getStatus(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			boolean isDone) {
		return isDone
				? AssessmentEntryStatus.done
				:courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnvironment).getAssessmentStatus();
	}

}
