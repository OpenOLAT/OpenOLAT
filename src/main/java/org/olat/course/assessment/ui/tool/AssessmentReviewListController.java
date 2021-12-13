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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.assessment.CoachingAssessmentEntry;
import org.olat.course.assessment.CoachingAssessmentSearchParams;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 22 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentReviewListController extends AssessmentCoachingListController {

	public AssessmentReviewListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, String translatedFormTitle) {
		super(ureq, wControl, stackPanel, translatedFormTitle);
	}

	@Override
	protected boolean canEditUserVisibility() {
		return false;
	}

	@Override
	protected boolean isShowLastUserModified() {
		return true;
	}

	@Override
	protected boolean isShowStatusDoneInfo() {
		return false;
	}

	@Override
	protected boolean canAssess() {
		return true;
	}

	@Override
	protected boolean canViewDetails() {
		return false;
	}

	@Override
	protected List<CoachingAssessmentEntry> loadModel() {
		CoachingAssessmentSearchParams  params = new CoachingAssessmentSearchParams();
		params.setSearchString(getQuickSearchString());
		params.setCoach(getIdentity());
		params.setStatus(AssessmentEntryStatus.inReview);
		
		return assessmentToolManager.getCoachingEntries(params);
	}

}
