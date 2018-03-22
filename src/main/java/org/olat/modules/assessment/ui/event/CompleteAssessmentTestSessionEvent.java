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
package org.olat.modules.assessment.ui.event;


import java.util.List;

import org.olat.core.gui.control.Event;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;

/**
 * 
 * Initial date: 18.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompleteAssessmentTestSessionEvent extends Event {

	private static final long serialVersionUID = 795226774030848980L;

	public static final String COMPLETE_EVENT = "complete-assessments";
	
	private final AssessmentEntryStatus status;
	private final AssessmentTest assessmentTest;
	private final List<AssessmentTestSession> testSessions;
	
	public CompleteAssessmentTestSessionEvent(List<AssessmentTestSession> testSessions, AssessmentTest assessmentTest, AssessmentEntryStatus status) {
		super(COMPLETE_EVENT);
		this.status = status;
		this.testSessions = testSessions;
		this.assessmentTest = assessmentTest;
	}
	
	public AssessmentEntryStatus getStatus() {
		return status;
	}

	public List<AssessmentTestSession> getTestSessions() {
		return testSessions;
	}

	public AssessmentTest getAssessmentTest() {
		return assessmentTest;
	}
}
