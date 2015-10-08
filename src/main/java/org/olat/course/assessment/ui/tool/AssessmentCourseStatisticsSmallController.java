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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.CourseStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentCourseStatisticsSmallController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentCourseStatisticsSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		mainVC = createVelocityContainer("course_stats_small");
		putInitialPanel(mainVC);
		updateStatistics();
	}
	
	public void updateStatistics() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		CourseStatistics stats = assessmentToolManager.getStatistics(getIdentity(), params);
		
		mainVC.contextPut("numOfAssessedIdentities", stats.getNumOfAssessedIdentities());
		mainVC.contextPut("scoreAverage", AssessmentHelper.getRoundedScore(stats.getAverageScore()));
		mainVC.contextPut("numOfPassed", stats.getCountPassed());
		int percentPassed = Math.round(100.0f * ((float)stats.getCountPassed() / (float)stats.getNumOfAssessedIdentities()));
		mainVC.contextPut("percentPassed", percentPassed);
		mainVC.contextPut("numOfFailed", stats.getCountFailed());
		int percentFailed = Math.round(100.0f * ((float)stats.getCountFailed() / (float)stats.getNumOfAssessedIdentities()));
		mainVC.contextPut("percentFailed", percentFailed);
		
		mainVC.contextPut("numOfInitialLaunch", stats.getInitialLaunch());
		
		
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}


}
