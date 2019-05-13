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
package org.olat.ims.qti21.manager.audit;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * Write the audit log on the standard OLog infrastructure
 * 
 * 
 * Initial date: 12.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentSessionAuditOLog implements AssessmentSessionAuditLogger {

	private static final Logger log = Tracing.createLoggerFor(AssessmentSessionAuditOLog.class);

	@Override
	public void logCandidateEvent(CandidateEvent candidateEvent) {
		try {
			StringOutput sb = new StringOutput(255);
			AuditLogFormatter.log(candidateEvent, null, sb);
			log.info(Tracing.M_AUDIT, sb.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void logCandidateEvent(CandidateEvent candidateEvent, Map<Identifier, AssessmentResponse> candidateResponseMap) {
		try {
			StringOutput sb = new StringOutput(255);
			AuditLogFormatter.log(candidateEvent, candidateResponseMap, sb);
			log.info(Tracing.M_AUDIT, sb.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void logCandidateOutcomes(AssessmentTestSession candidateSession, Map<Identifier, String> outcomes) {
		try {
			StringOutput sb = new StringOutput(255);
			AuditLogFormatter.logOutcomes(outcomes, sb);
			log.info(Tracing.M_AUDIT, sb.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logCorrection(AssessmentTestSession candidateSession, AssessmentItemSession itemSession, Identity coach) {
		try {
			StringOutput sb = new StringOutput(255);
			sb.append("Test session ").append(candidateSession.getKey()).append(" (assessed identity=");
			if(candidateSession.getIdentity() != null) {
				sb.append(candidateSession.getIdentity().getKey());
			} else {
				sb.append(candidateSession.getAnonymousIdentifier());
			}
			sb.append(" ");
			AuditLogFormatter.logCorrection(itemSession, coach, sb);
			log.info(Tracing.M_AUDIT, sb.toString());
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logTestRetrieved(AssessmentTestSession candidateSession, Identity coach) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("Test session ").append(candidateSession.getKey()).append(" (assessed identity=");
		if(candidateSession.getIdentity() != null) {
			sb.append(candidateSession.getIdentity().getKey());
		} else {
			sb.append(candidateSession.getAnonymousIdentifier());
		}
		sb.append(" ) retrieved by coach ").append(coach.getKey());
		log.info(Tracing.M_AUDIT, sb.toString());
	}
	
	
	
	@Override
	public void logTestReopen(AssessmentTestSession candidateSession, Identity coach) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("Test session ").append(candidateSession.getKey()).append(" (assessed identity=");
		if(candidateSession.getIdentity() != null) {
			sb.append(candidateSession.getIdentity().getKey());
		} else {
			sb.append(candidateSession.getAnonymousIdentifier());
		}
		sb.append(" ) reopened by coach ").append(coach.getKey());
		log.info(Tracing.M_AUDIT, sb.toString());
	}

	@Override
	public void logTestExtend(AssessmentTestSession candidateSession, int extraTime, Identity coach) {
		StringBuilder sb = new StringBuilder(255);
		sb.append("Test session ").append(candidateSession.getKey()).append(" (assessed identity=");
		if(candidateSession.getIdentity() != null) {
			sb.append(candidateSession.getIdentity().getKey());
		} else {
			sb.append(candidateSession.getAnonymousIdentifier());
		}
		sb.append(" ) extended of ").append(extraTime).append(" by coach ").append(coach.getKey());
		log.info(Tracing.M_AUDIT, sb.toString());
	}

	@Override
	public void logAndThrowCandidateException(AssessmentTestSession session, CandidateExceptionReason reason, Exception ex) {
		log.error(reason.name(), ex);
	}

	@Override
	public void close() throws IOException {
		//
	}
	
	
}
