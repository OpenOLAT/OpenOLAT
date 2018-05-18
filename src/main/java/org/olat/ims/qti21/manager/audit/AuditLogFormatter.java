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
import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentResponse;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.audit.CandidateEvent;
import org.olat.repository.RepositoryEntryRef;

import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 12.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AuditLogFormatter {

	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	
	protected static void logDate(Writer writer) throws IOException {
		String date; 
		synchronized(dateFormat) {
			date = dateFormat.format(new Date());
		}
		writer.append(date);
		writer.append(" ");
	}
	
	protected static void log(CandidateEvent candidateEvent, Map<Identifier, AssessmentResponse> candidateResponseMap, Writer writer) throws IOException {
		writer.append("QTI21 audit [");
		
		AssessmentTestSession testSession = candidateEvent.getCandidateSession();
		if(testSession != null) {
			RepositoryEntryRef testEntry = candidateEvent.getTestEntry();
			RepositoryEntryRef courseEntry = candidateEvent.getRepositoryEntry();
			if(courseEntry != null && !testEntry.getKey().equals(courseEntry.getKey())) {
				writer.write(courseEntry.getKey().toString());
				writer.write(":");
				if(testSession.getSubIdent() == null) {
					writer.write("NULL:");
				} else {
					writer.write(testSession.getSubIdent());
					writer.write(":");
				}
			}
			
			if(testEntry != null) {
				writer.write(testEntry.getKey().toString());
			}
		}
		
		writer.write("] ");
		
		if(candidateEvent.getTestEventType() != null) {
			writer.append("TestEvent:");
			writer.append(candidateEvent.getTestEventType().toString());
			writer.write(" ");
		}
		if(candidateEvent.getItemEventType() != null) {
			writer.append("ItemEvent:");
			writer.append(candidateEvent.getItemEventType().toString());
			writer.write(" ");
		}

		if(candidateEvent.getTestItemKey() != null) {
			writer.append("TestItemKey[");
			writer.append(candidateEvent.getTestItemKey());
			writer.write("] ");
		}

		if(candidateResponseMap != null) {
			writer.write("params=");
			for (Map.Entry<Identifier, AssessmentResponse> responseEntry:candidateResponseMap.entrySet()) {
				Identifier identifier = responseEntry.getKey();
				AssessmentResponse response = responseEntry.getValue();
				
				writer.append("|");
				writer.append(identifier.toString());
				writer.append("=");
				String stringuifiedResponse = response.getStringuifiedResponse();
				if(stringuifiedResponse == null) {
					writer.append("NULL");
				} else {
					writer.append(stringuifiedResponse);
				}
			}
		}
	}
	
	protected static void logOutcomes(Map<Identifier, String> outcomes, Writer writer) throws IOException {
		writer.append("QTI21 audit outcomes=");
		
		if(outcomes == null || outcomes.isEmpty()) {
			writer.write("EMPTY");
		} else {
			writer.write("params=");
			for (Map.Entry<Identifier, String> responseEntry:outcomes.entrySet()) {
				Identifier identifier = responseEntry.getKey();
				String stringuifiedValue = responseEntry.getValue();
				
				writer.append("|");
				writer.append(identifier.toString());
				writer.append("=");
				if(stringuifiedValue == null) {
					writer.append("NULL");
				} else {
					writer.append(stringuifiedValue);
				}
			}
		}
	}
	
	protected static void logCorrection(AssessmentItemSession itemSession, Identity coach, Writer writer) throws IOException {
		writer.append("Manual correction itemSession identifier=");
		writer.append(itemSession.getAssessmentItemIdentifier());
		writer.append(" key=");
		writer.append(itemSession.getKey().toString());
		writer.append(" manualScore=");
		if(itemSession.getManualScore() == null) {
			writer.append("NULL");
		} else {
			writer.append(AssessmentHelper.getRoundedScore(itemSession.getManualScore()));
		}
		writer.append(" by ");
		writer.append(coach.getKey().toString());
	}
}
