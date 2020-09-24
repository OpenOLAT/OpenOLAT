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

import java.io.Closeable;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * This interface is base on the CandidateAuditLogger from
 * QtiWorks
 * 
 * Initial date: 12.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssessmentSessionAuditLogger extends Closeable {

	public void logCandidateEvent(CandidateEvent candidateEvent);
	
	public void logCandidateEvent(CandidateEvent candidateEvent, Map<Identifier, AssessmentResponse> candidateResponseMap);
	
	public void logCandidateOutcomes(AssessmentTestSession candidateSession, Map<Identifier, String> outcomes);
	
	public void logAndThrowCandidateException(AssessmentTestSession session, CandidateExceptionReason reason, Exception ex);
	
	public void logCorrection(AssessmentTestSession candidateSession, AssessmentItemSession itemSession, Identity coach);
	
	public void logTestRetrieved(AssessmentTestSession candidateSession, Identity coach);
	
	public void logTestReopen(AssessmentTestSession candidateSession, Identity coach);
	
	public void logTestExtend(AssessmentTestSession candidateSession, int extraTime, boolean compensation, Identity coach);

}
