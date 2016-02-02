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
package org.olat.ims.qti21.model.jpa;

import java.util.Date;

import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.model.CandidateItemEventType;
import org.olat.ims.qti21.model.CandidateTestEventType;

/**
 * 
 * Initial date: 19.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CandidateEvent {
	
	private Date timestamp;
    private String testItemKey;

	private AssessmentTestSession candidateSession;
	
	private CandidateTestEventType testEventType;
	private CandidateItemEventType itemEventType;

    public CandidateEvent() {
    	//
    }

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public CandidateTestEventType getTestEventType() {
		return testEventType;
	}

	public void setTestEventType(CandidateTestEventType testEventType) {
		this.testEventType = testEventType;
	}

	public CandidateItemEventType getItemEventType() {
		return itemEventType;
	}

	public void setItemEventType(CandidateItemEventType itemEventType) {
		this.itemEventType = itemEventType;
	}

	public String getTestItemKey() {
		return testItemKey;
	}

	public void setTestItemKey(String testItemKey) {
		this.testItemKey = testItemKey;
	}

	public AssessmentTestSession getCandidateSession() {
		return candidateSession;
	}

	public void setCandidateSession(AssessmentTestSession candidateSession) {
		this.candidateSession = candidateSession;
	}
	
}
