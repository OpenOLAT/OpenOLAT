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

import java.util.Date;

import org.olat.core.util.event.MultiUserEvent;
import org.olat.modules.assessment.model.AssessmentRunStatus;

/**
 * 
 * Initial date: 22 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CompletionEvent extends MultiUserEvent {

	private static final long serialVersionUID = 61311103785495742L;
	public static final String PROGRESS = "completion-progess";
	
	private final Date start;
	private final Double completion;
	private final AssessmentRunStatus status;
	private final String subIdent; 
	private final Long identityKey;
	
	public CompletionEvent(String name, String subIdent, Date start, Double completion, AssessmentRunStatus status, Long identityKey) {
		super(name);
		this.status = status;
		this.subIdent = subIdent;
		this.start = start;
		this.completion = completion;
		this.identityKey = identityKey;
	}

	public String getSubIdent() {
		return subIdent;
	}
	
	public Date getStart() {
		return start;
	}

	public Double getCompletion() {
		return completion;
	}
	
	public AssessmentRunStatus getStatus() {
		return status;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}
}
