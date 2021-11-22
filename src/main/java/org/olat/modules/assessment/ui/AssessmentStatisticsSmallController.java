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
package org.olat.modules.assessment.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatisticsSmallController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private final RepositoryEntry testEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private int numOfPassed;
	private int numOfFailed;
	private int numOfAssessedIdentities;
	private AssessmentMembersStatistics membersStatistics;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentStatisticsSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry testEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.testEntry = testEntry;
		this.assessmentCallback = assessmentCallback;
		
		mainVC = createVelocityContainer("test_stats_small");
		putInitialPanel(mainVC);
		updateStatistics();
	}
	
	public int getNumOfPassed() {
		return numOfPassed;
	}
	
	public int getNumOfFailed() {
		return numOfFailed;
	}
	
	public AssessmentMembersStatistics getMemberStatistics() {
		return membersStatistics;
	}

	public void updateStatistics() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(testEntry, null, testEntry, assessmentCallback);
		numOfAssessedIdentities = assessmentToolManager.getNumberOfAssessedIdentities(getIdentity(), params);
		mainVC.contextPut("numOfAssessedIdentities", numOfAssessedIdentities);
		
		membersStatistics = assessmentToolManager.getNumberOfParticipants(getIdentity(), params);
		mainVC.contextPut("numOfParticipants", membersStatistics.getNumOfParticipants());
		if(assessmentCallback.canAssessNonMembers()) {
			mainVC.contextPut("numOfOtherUsers", membersStatistics.getNumOfOtherUsers());
		}
		
		AssessmentStatistics stats = assessmentToolManager.getStatistics(getIdentity(), params);
		mainVC.contextPut("scoreAverage", AssessmentHelper.getRoundedScore(stats.getAverageScore()));
		numOfPassed = stats.getCountPassed();
		mainVC.contextPut("numOfPassed", numOfPassed);
		
		float total = membersStatistics.getTotal();
		int percentPassed = total <= 0 ? 0 : Math.round(100.0f * (stats.getCountPassed() / total));
		mainVC.contextPut("percentPassed", percentPassed);
		numOfFailed = stats.getCountFailed();
		mainVC.contextPut("numOfFailed", numOfFailed);
		int percentFailed = total <= 0 ? 0 : Math.round(100.0f * (stats.getCountFailed() / total));
		mainVC.contextPut("percentFailed", percentFailed);
		
		int numOfParticipantLaunches = membersStatistics.getNumOfParticipantsLoggedIn();
		mainVC.contextPut("numOfParticipantLaunches", numOfParticipantLaunches);
		if(assessmentCallback.canAssessNonMembers()) {
			mainVC.contextPut("numOfOtherUserLaunches", membersStatistics.getOthersLoggedIn());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
