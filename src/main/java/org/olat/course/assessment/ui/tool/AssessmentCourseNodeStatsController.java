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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessmentStatsController;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.Stat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseNodeStatsController extends BasicController implements AssessmentCourseNodeOverviewController {

	private final AssessmentStatsController assessmentStatsCtrl;
	private Controller detailsCtrl;

	protected final UserCourseEnvironment userCourseEnv;
	protected final CourseNode courseNode;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public AssessmentCourseNodeStatsController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode, AssessmentToolSecurityCallback assessmentCallback, boolean courseInfoLaunch, boolean readOnly) {
		super(ureq, wControl);
		this.userCourseEnv = userCourseEnv;
		this.courseNode = courseNode;
		this.assessmentCallback = assessmentCallback;
		
		// Reset the velocity root, so that the children find the template
		setVelocityRoot(Util.getPackageVelocityRoot(AssessmentCourseNodeStatsController.class));
		VelocityContainer mainVC = createVelocityContainer("course_node_stats");
		
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(
				userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent(),
				courseNode.getReferencedRepositoryEntry(), assessmentCallback);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		List<Stat> stats = new ArrayList<>(2);
		if (Mode.none != assessmentConfig.getPassedMode()) {
			stats.add(Stat.passed);
		} else if (assessmentConfig.hasStatus() || LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(userCourseEnv).getType())) {
			stats.add(Stat.status);
		}
		if (Mode.none != assessmentConfig.getScoreMode()) {
			stats.add(Stat.score);
		}
		
		assessmentStatsCtrl = new AssessmentStatsController(ureq, wControl, assessmentCallback, params, stats, courseInfoLaunch, readOnly);
		assessmentStatsCtrl.setExpanded(true);
		listenTo(assessmentStatsCtrl);
		mainVC.put("stats", assessmentStatsCtrl.getInitialComponent());
		
		if (hasDetails()) {
			detailsCtrl = createDetailsController(ureq, wControl);
			listenTo(detailsCtrl);
			mainVC.put("details", detailsCtrl.getInitialComponent());
		}
		
		putInitialPanel(mainVC);
	}

	protected boolean hasDetails() {
		return false;
	}

	@SuppressWarnings("unused")
	protected Controller createDetailsController(UserRequest ureq, WindowControl wControl) {
		return null;
	}

	@Override
	public void reload() {
		assessmentStatsCtrl.reload();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == assessmentStatsCtrl) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
