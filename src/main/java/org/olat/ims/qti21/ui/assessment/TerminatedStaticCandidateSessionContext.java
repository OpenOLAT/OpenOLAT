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
package org.olat.ims.qti21.ui.assessment;

import java.util.Date;

import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.ui.CandidateSessionContext;

import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TerminatedStaticCandidateSessionContext implements CandidateSessionContext {
	
	private final AssessmentTestSession testSession;
	
	public TerminatedStaticCandidateSessionContext(AssessmentTestSession testSession) {
		this.testSession = testSession;
	}

	@Override
	public boolean isTerminated() {
		return true;
	}

	@Override
	public AssessmentTestSession getCandidateSession() {
		return testSession;
	}

	@Override
	public CandidateEvent getLastEvent() {
		return null;
	}

	@Override
	public Date getCurrentRequestTimestamp() {
		return null;
	}

	@Override
	public boolean isMarked(String itemKey) {
		return false;
	}

	@Override
	public boolean isRubricHidden(Identifier sectionKey) {
		return false;
	}

	@Override
	public int getNumber(TestPlanNode node) {
		return 0;
	}
}
