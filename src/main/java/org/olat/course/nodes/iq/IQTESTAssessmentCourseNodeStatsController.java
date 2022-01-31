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
package org.olat.course.nodes.iq;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.Group;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeStatsController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.group.BusinessGroup;
import org.olat.ims.qti21.ui.statistics.QTI21AssessmentTestStatisticsController;
import org.olat.ims.qti21.ui.statistics.QTI21StatisticResourceResult;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTAssessmentCourseNodeStatsController extends AssessmentCourseNodeStatsController {
	
	@Autowired
	private RepositoryService repositoryService;

	public IQTESTAssessmentCourseNodeStatsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNode courseNode, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, userCourseEnv, courseNode, assessmentCallback);
	}

	@Override
	protected boolean hasDetails() {
		return courseNode.isStatisticNodeResultAvailable(userCourseEnv, StatisticType.TEST);
	}

	@Override
	protected Controller createDetailsController(UserRequest ureq, WindowControl wControl) {
		StatisticResourceOption options = new StatisticResourceOption();
		if (!userCourseEnv.isAdmin()) {
			options.setParticipantsGroups(getCoachedGroups());
		}
		StatisticResourceResult statisticNodeResult = courseNode.createStatisticNodeResult(ureq, wControl, userCourseEnv, options, StatisticType.TEST);
		if (statisticNodeResult instanceof QTI21StatisticResourceResult) {
			QTI21StatisticResourceResult qti21StatisticResourceResult = (QTI21StatisticResourceResult)statisticNodeResult;
			qti21StatisticResourceResult.getTreeModel();
			return new QTI21AssessmentTestStatisticsController(ureq, wControl, null, qti21StatisticResourceResult, false, false, false);
		}
		return null;
	}
	
	private List<Group> getCoachedGroups() {
		List<Group> groups = new ArrayList<>();
		
		if(assessmentCallback.canAssessRepositoryEntryMembers()) {
			Group bGroup = repositoryService.getDefaultGroup(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry());
			groups.add(bGroup);
		}
		
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<Group> bgBaseGroups = assessmentCallback.getCoachedGroups().stream()
					.map(BusinessGroup::getBaseGroup)
					.collect(Collectors.toList());
			groups.addAll(bgBaseGroups);
		}
		
		if(assessmentCallback.canAssessCurriculumMembers()) {
			List<Group> ceBaseGroups = userCourseEnv.getCoachedCurriculumElements().stream()
					.map(CurriculumElement::getGroup)
					.collect(Collectors.toList());
			groups.addAll(ceBaseGroups);
		}
		
		return groups;
	}

}
