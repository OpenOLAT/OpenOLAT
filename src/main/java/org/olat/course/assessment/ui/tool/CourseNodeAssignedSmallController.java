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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 févr. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseNodeAssignedSmallController extends CourseNodeToReviewAbstractSmallController {
	
	private static final Supplier<AssessedIdentityListState> IDENTITY_FILTER = 
			() -> new AssessedIdentityListState(null, null, null, null, null, null, IdentityListCourseNodeController.ASSIGNED_TO_ME_TAB_ID, true);
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public CourseNodeAssignedSmallController(UserRequest ureq, WindowControl wContrl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wContrl, courseEntry, assessmentCallback);
	}

	@Override
	protected String getIconCssClass() {
		return "o_icon_status_in_review";
	}

	@Override
	protected String getTitleI18nKey() {
		return "assigned.open";
	}
	
	@Override
	protected String getLinkNumberI18nKey() {
		return "assigned.open.number";
	}

	@Override
	protected String getTableEmptyI18nKey() {
		return "assigned.open.empty";
	}

	@Override
	protected Map<String, List<AssessmentEntry>> loadNodeIdentToEntries(Collection<ParticipantType> participants) {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		params.setParticipantTypes(participants);
		params.setAssignedCoachKeys(List.of(getIdentity().getKey()));
		return assessmentToolManager.getAssessmentEntries(getIdentity(), params, AssessmentEntryStatus.inReview).stream()
				.collect(Collectors.groupingBy(AssessmentEntry::getSubIdent));
	}

	@Override
	protected Supplier<AssessedIdentityListState> getIdentityFilter() {
		return IDENTITY_FILTER;
	}

}
