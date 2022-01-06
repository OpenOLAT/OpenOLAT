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
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathConfigs.FullyAssessedResult;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NoAccessResolver;
import org.olat.course.nodeaccess.NodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.CoursePaginationController;
import org.olat.course.run.userview.CourseTreeModelBuilder;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
@Order(1)
public class LearningPathNodeAccessProvider implements NodeAccessProvider {

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
	public String getToolTipHelpText(Locale locale) {
		Translator translator = Util.createPackageTranslator(LearningPathNodeConfigController.class, locale);
		return translator.translate("access.provider.toolTip");
	}

	@Override
	public boolean isSupported(String courseNodeType) {
		return registry.getLearningPathNodeHandler(courseNodeType).isSupported();
	}

	@Override
	public boolean isGuestSupported() {
		return false;
	}

	@Override
	public boolean isConditionExpressionSupported() {
		return false;
	}

	@Override
	public boolean isScoreCalculatorSupported() {
		return false;
	}

	@Override
	public void updateConfigDefaults(CourseNode courseNode, boolean newNode, INode parent) {
		registry.getLearningPathNodeHandler(courseNode).updateDefaultConfigs(courseNode, newNode, parent);
	}
	
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment userCourseEnvironment, CourseEditorTreeModel editorModel) {
		RepositoryEntry courseEntry = userCourseEnvironment.getCourseEditorEnv().getCourseGroupManager().getCourseEntry();
		Controller configCtrl = registry.getLearningPathNodeHandler(courseNode).createConfigEditController(ureq, wControl, courseEntry, courseNode);
		return new TabbableLeaningPathNodeConfigController(ureq, wControl, configCtrl);
	}

	@Override
	public String getCourseTreeCssClass(CourseConfig courseConfig) {
		return courseConfig.isMenuPathEnabled()? "o_lp_tree o_path": "o_lp_tree";
	}

	@Override
	public CoursePaginationController getCoursePaginationController(UserRequest ureq, WindowControl wControl) {
		return new CoursePaginationController(ureq, wControl);
	}

	@Override
	public CourseTreeModelBuilder getCourseTreeModelBuilder(UserCourseEnvironment userCourseEnv) {
		return new LearningPathCourseTreeModelBuilder(userCourseEnv);
	}

	@Override
	public NoAccessResolver getNoAccessResolver(UserCourseEnvironment userCourseEnv) {
		return new LearningPathNoAccessResolver(userCourseEnv, getCourseTreeModelBuilder(userCourseEnv).build().getRootNode());
	}

	@Override
	public boolean onNodeVisited(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnNodeVisited();
		boolean participant = userCourseEnv.isParticipant();
		if (participant && result.isEnabled()) {
			AssessmentEntryStatus status = getStatus(courseNode, userCourseEnv, result.isDone(),
					result.isFullyAssessed());
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv,
					Boolean.valueOf(result.isFullyAssessed()), status);
			return true;
		}
		return false;
	}

	@Override
	public boolean isAssessmentConfirmationEnabled(CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnConfirmation(true);
		boolean participant = userCourseEnv.isParticipant();
		boolean confirmationEnabled = participant && result.isEnabled();
		return confirmationEnabled;
	}

	@Override
	public void onAssessmentConfirmed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, boolean confirmed) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnConfirmation(confirmed);
		updateFullyAssessed(courseNode, userCourseEnv, result);
	}

	@Override
	public void onScoreUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Float score,
			Boolean userVisibility) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnScore(score, userVisibility);
		updateFullyAssessed(courseNode, userCourseEnv, result);
	}

	@Override
	public void onPassedUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv, Boolean passed,
			Boolean userVisibility) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnPassed(passed, userVisibility);
		updateFullyAssessed(courseNode, userCourseEnv, result);
	}

	@Override
	public void onStatusUpdated(CourseNode courseNode, UserCourseEnvironment userCourseEnv,
			AssessmentEntryStatus status) {
		FullyAssessedResult result = getConfigs(courseNode).isFullyAssessedOnStatus(status);
		updateFullyAssessed(courseNode, userCourseEnv, result);
	}

	void updateFullyAssessed(CourseNode courseNode, UserCourseEnvironment userCourseEnv, FullyAssessedResult result) {
		boolean participant = userCourseEnv.isParticipant();
		if (participant && result.isEnabled()) {
			AssessmentEntryStatus status = getStatus(courseNode, userCourseEnv, result.isDone(),
					result.isFullyAssessed());
			courseAssessmentService.updateFullyAssessed(courseNode, userCourseEnv,
					Boolean.valueOf(result.isFullyAssessed()), status);
		}
	}

	private AssessmentEntryStatus getStatus(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment,
			boolean setDone, boolean fullyAssessed) {
		return setDone
				? fullyAssessed? AssessmentEntryStatus.done: AssessmentEntryStatus.notStarted
				: courseAssessmentService.getAssessmentEntry(courseNode, userCourseEnvironment).getAssessmentStatus();
	}

}
