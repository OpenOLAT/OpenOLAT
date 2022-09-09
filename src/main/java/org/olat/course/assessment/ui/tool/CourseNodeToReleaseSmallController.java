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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.model.SearchAssessedIdentityParams.Particpant;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeToReleaseSmallController extends CourseNodeToReviewAbstractSmallController {
	
	private static final Supplier<AssessedIdentityListState> IDENTITY_FILTER = 
			() -> new AssessedIdentityListState(null, null, null, null, null, null, IdentityListCourseNodeController.TO_RELEASE_TAB_ID, true);
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public CourseNodeToReleaseSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, courseEntry, assessmentCallback);
	}
	
	@Override
	protected String getIconCssClass() {
		return "o_icon_results_hidden";
	}

	@Override
	protected String getTitleI18nKey() {
		return "user.visibility.hidden.title";
	}

	@Override
	protected String getTitleNumberI18nKey() {
		return "user.visibility.hidden.title.number";
	}

	@Override
	protected String getTableEmptyI18nKey() {
		return "user.visibility.hidden.empty";
	}

	@Override
	protected Map<String, List<AssessmentEntry>> loadNodeIdentToEntries(Set<Particpant> participants) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		params.setParticipants(participants);
		params.setUserVisibility(Boolean.FALSE);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		return assessmentToolManager.getAssessmentEntries(getIdentity(), params, AssessmentEntryStatus.done).stream()
				.collect(Collectors.groupingBy(AssessmentEntry::getSubIdent));
	}

	@Override
	protected Supplier<AssessedIdentityListState> getIdentityFilter() {
		return IDENTITY_FILTER;
	}
	
}