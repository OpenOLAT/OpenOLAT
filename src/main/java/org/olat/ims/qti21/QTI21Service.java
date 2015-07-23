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
package org.olat.ims.qti21;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.impl.MultipartFileInfos;
import org.olat.core.id.Identity;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;
import org.olat.ims.qti21.model.jpa.CandidateEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.notification.NotificationRecorder;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentObject;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 12.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QTI21Service {
	
	public URI createAssessmentObjectUri(File resourceDirectory);
	
	public <E extends ResolvedAssessmentObject<?>> E loadAndResolveAssessmentObject(File resourceDirectory);
	
	
	public UserTestSession createTestSession(Identity identity, AssessmentEntry assessmentEntry,
			RepositoryEntry entry, String subIdent, RepositoryEntry testEntry,
			boolean authorMode);
	
	public UserTestSession getResumableTestSession(Identity identity, RepositoryEntry entry, String subIdent, RepositoryEntry testEntry);
	
	public UserTestSession updateTestSession(UserTestSession session);
	
	public TestSessionState loadTestSessionState(UserTestSession session);
	
	/**
	 * Retrieve the sessions of a user.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param identity
	 * @return
	 */
	public List<UserTestSession> getUserTestSessions(RepositoryEntryRef courseEntry, String subIdent, IdentityRef identity);
	
	public void recordTestAssessmentResult(UserTestSession candidateSession, AssessmentResult assessmentResult);
	
	public UserTestSession finishTestSession(UserTestSession candidateSession, AssessmentResult assessmentResul, Date timestamp);
	
	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			TestSessionState testSessionState, NotificationRecorder notificationRecorder);

	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestSessionState testSessionState, NotificationRecorder notificationRecorder);

	public CandidateEvent recordCandidateTestEvent(UserTestSession candidateSession, CandidateTestEventType textEventType,
			CandidateItemEventType itemEventType, TestPlanNodeKey itemKey, TestSessionState testSessionState, NotificationRecorder notificationRecorder);
	
	
	

	public UserTestSession finishItemSession(UserTestSession candidateSession, AssessmentResult assessmentResul, Date timestamp);
	

	public void recordItemAssessmentResult(UserTestSession candidateSession, AssessmentResult assessmentResult);
	
	public CandidateEvent recordCandidateItemEvent(UserTestSession candidateSession, CandidateItemEventType itemEventType,
			ItemSessionState itemSessionState, NotificationRecorder notificationRecorder);
	
	public CandidateEvent recordCandidateItemEvent(UserTestSession candidateSession,
            CandidateItemEventType itemEventType, ItemSessionState itemSessionState);
	
	public String importFileSubmission(UserTestSession candidateSession, MultipartFileInfos multipartFile);

}
