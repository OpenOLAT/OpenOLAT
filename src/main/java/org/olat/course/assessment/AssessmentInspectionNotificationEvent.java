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
package org.olat.course.assessment;

import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.model.TransientAssessmentInspection;

/**
 * 
 * Initial date: 4 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionNotificationEvent extends MultiUserEvent  {

	public static final String BEFORE = "assessment-mode-none-notification";
	public static final String LEADTIME = "assessment-mode-leadtime-notification";
	public static final String START_ASSESSMENT = "assessment-mode-start-assessment-notification";
	public static final String STOP_WARNING = "assessment-mode-warning-stop-notification";
	public static final String STOP_ASSESSMENT = "assessment-mode-stop-assessment-notification";
	public static final String END = "assessment-mode-end-notification";
	
	public static final OLATResourceable ASSESSMENT_INSPECTION_NOTIFICATION = OresHelper.createOLATResourceableType("assessment-inspection-notification");

	private static final long serialVersionUID = 1539360689947584111L;

	private TransientAssessmentInspection inspection;
	private Long assessedIdentityKey;
	
	public AssessmentInspectionNotificationEvent(String cmd, TransientAssessmentInspection inspection, Long assessedIdentityKey) {
		super(cmd, 9);
		this.inspection = inspection;
		this.assessedIdentityKey = assessedIdentityKey;
	}

	public TransientAssessmentInspection getAssessementInspection() {
		return inspection;
	}

	public Long getAssessedIdentityKey() {
		return assessedIdentityKey;
	}

	public boolean isModeOf(LockRequest currentRequest, Identity identity) {
		// if an assessment is running, only relevant if they are the same
		if(currentRequest != null) {
			return currentRequest.getRequestKey().equals(inspection.getRequestKey());
		}
		return (assessedIdentityKey != null && identity != null && assessedIdentityKey.equals(identity.getKey()));
	}
}
