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
package org.olat.ims.qti21.ui.assessment.model;

import java.math.BigDecimal;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;

import uk.ac.ed.ph.jqtiplus.node.result.SessionStatus;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 17.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemCorrection {
	
	private final TestPlanNode itemNode;
	private final Identity assessedIdentity;
	private final AssessmentItemRef itemRef;
	private AssessmentItemSession itemSession;
	private final ItemSessionState itemSessionState;
	private AssessmentTestSession testSession;
	private final TestSessionState testSessionState;
	
	public AssessmentItemCorrection(Identity assessedIdentity, 
			AssessmentTestSession testSession, TestSessionState testSessionState,
			AssessmentItemSession itemSession, ItemSessionState itemSessionState,
			AssessmentItemRef itemRef, TestPlanNode itemNode) {
		this.itemNode = itemNode;
		this.itemSession = itemSession;
		this.testSession = testSession;
		this.itemRef = itemRef;
		this.assessedIdentity = assessedIdentity;
		this.testSessionState = testSessionState;
		this.itemSessionState = itemSessionState;
	}

	public TestPlanNode getItemNode() {
		return itemNode;
	}
	
	public AssessmentItemRef getItemRef() {
		return itemRef;
	}

	public AssessmentItemSession getItemSession() {
		return itemSession;
	}
	
	public void setItemSession(AssessmentItemSession itemSession) {
		this.itemSession = itemSession;
	}

	public ItemSessionState getItemSessionState() {
		return itemSessionState;
	}
	
	public String getItemSessionStatus() {
		return itemSessionState != null && itemSessionState.getSessionStatus() != null ? itemSessionState.getSessionStatus().name()
				: "";
	}
	
	public boolean isItemSessionStatusFinal() {
		return itemSessionState != null && itemSessionState.getSessionStatus() != null
				&& SessionStatus.FINAL.equals(itemSessionState.getSessionStatus());
	}

	public AssessmentTestSession getTestSession() {
		return testSession;
	}
	
	public void setTestSession(AssessmentTestSession testSession) {
		this.testSession = testSession;
	}

	public TestSessionState getTestSessionState() {
		return testSessionState;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public BigDecimal getManualScore() {
		return itemSession == null ? null : itemSession.getManualScore();
	}
	
	public boolean isResponded() {
		return itemSessionState != null && itemSessionState.isResponded();
	}
}
