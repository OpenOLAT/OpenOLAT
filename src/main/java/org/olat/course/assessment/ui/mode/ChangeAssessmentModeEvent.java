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
package org.olat.course.assessment.ui.mode;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12 juil. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ChangeAssessmentModeEvent extends MultiUserEvent {

	public static final OLATResourceable ASSESSMENT_MODE_ORES = OresHelper.createOLATResourceableType(AssessmentMode.class);

	private static final long serialVersionUID = -5428487290530968740L;

	public static final String CHANGED = "assessment-mode-changed";
	
	private Long entryKey;
	private Long assessmentModeKey;
	private Status status;
	private EndStatus endStatus;
	
	public ChangeAssessmentModeEvent(AssessmentMode assessmentMode, RepositoryEntry entry) {
		super(CHANGED);
		entryKey = entry.getKey();
		assessmentModeKey = assessmentMode.getKey();
		status = assessmentMode.getStatus();
		endStatus = assessmentMode.getEndStatus();
	}
	
	public Long getEntryKey() {
		return entryKey;
	}
	
	public Long getAssessmentModeKey() {
		return assessmentModeKey;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public EndStatus getEndStatus() {
		return endStatus;
	}

	public void setEndStatus(EndStatus endStatus) {
		this.endStatus = endStatus;
	}
}
