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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.ims.qti21.model.audit.CandidateExceptionReason;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Write the logs in a file in the user storage for this assessment test
 * 
 * Initial date: 12.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentSessionAuditFileLog implements AssessmentSessionAuditLogger {

	private static final Logger log = Tracing.createLoggerFor(AssessmentSessionAuditFileLog.class);
	
	private Writer writer;
	private final OutputStream outputStream;
	private final AssessmentSessionAuditLogger debugLog;

	public AssessmentSessionAuditFileLog(OutputStream outputStream) throws IOException {
		this.outputStream = outputStream;
		debugLog = Settings.isDebuging() ? new AssessmentSessionAuditOLog() : new DefaultAssessmentSessionAuditLogger();
		writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
	}
	
	@Override
	public void logCandidateEvent(CandidateEvent candidateEvent) {
		try {
			AuditLogFormatter.logDate(writer);
			AuditLogFormatter.log(candidateEvent, null, writer);
			writer.write("\n");
			writer.flush();
			debugLog.logCandidateEvent(candidateEvent);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void logCandidateEvent(CandidateEvent candidateEvent, Map<Identifier, AssessmentResponse> candidateResponseMap) {
		try {
			AuditLogFormatter.logDate(writer);
			AuditLogFormatter.log(candidateEvent, candidateResponseMap, writer);
			writer.write("\n");
			writer.flush();
			debugLog.logCandidateEvent(candidateEvent, candidateResponseMap);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logCandidateOutcomes(AssessmentTestSession candidateSession, Map<Identifier, String> outcomes) {
		try {
			AuditLogFormatter.logDate(writer);
			AuditLogFormatter.logOutcomes(outcomes, writer);
			writer.write("\n");
			writer.flush();
			debugLog.logCandidateOutcomes(candidateSession, outcomes);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logCorrection(AssessmentTestSession candidateSession, AssessmentItemSession itemSession, Identity coach) {
		try {
			AuditLogFormatter.logDate(writer);
			AuditLogFormatter.logCorrection(itemSession, coach, writer);
			writer.write("\n");
			writer.flush();
			debugLog.logCorrection(candidateSession, itemSession, coach);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logTestRetrieved(AssessmentTestSession candidateSession, Identity coach) {
		try {
			AuditLogFormatter.logDate(writer);
			writer.write("Test session retrieved by " + coach.getKey());
			writer.write("\n");
			writer.flush();
			debugLog.logTestRetrieved(candidateSession, coach);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	

	@Override
	public void logTestReopen(AssessmentTestSession candidateSession, Identity coach) {
		try {
			AuditLogFormatter.logDate(writer);
			writer.write("Test session reopened by " + coach.getKey());
			writer.write("\n");
			writer.flush();
			debugLog.logTestRetrieved(candidateSession, coach);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logTestExtend(AssessmentTestSession candidateSession, int extraTime, boolean compensation, Identity coach) {
		try {
			AuditLogFormatter.logDate(writer);
			writer.write("Test session extened " + extraTime + " by " + coach.getKey());
			if(compensation) {
				writer.write(" (compensation for disadvantages)");
			}
			writer.write("\n");
			writer.flush();
			debugLog.logTestRetrieved(candidateSession, coach);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	@Override
	public void logAndThrowCandidateException(AssessmentTestSession session, CandidateExceptionReason reason, Exception ex) {
		log.error(reason.name(), ex);
	}

	@Override
	public void close() {
		FileUtils.closeSafely(outputStream);
	}
}
