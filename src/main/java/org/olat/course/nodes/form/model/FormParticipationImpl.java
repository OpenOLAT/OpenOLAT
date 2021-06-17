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
package org.olat.course.nodes.form.model;

import java.util.Date;

import org.olat.core.id.Identity;
import org.olat.course.nodes.form.FormParticipation;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormParticipationRef;
import org.olat.modules.forms.EvaluationFormParticipationStatus;

/**
 * 
 * Initial date: 17 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormParticipationImpl implements FormParticipation {
	
	private Identity identity;
	private EvaluationFormParticipationRef evaluationFormParticipationRef;
	private EvaluationFormParticipationStatus participationStatus;
	private Date submissionDate;
	
	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	@Override
	public EvaluationFormParticipationRef getEvaluationFormParticipationRef() {
		return evaluationFormParticipationRef;
	}

	@Override
	public EvaluationFormParticipationStatus getParticipationStatus() {
		return participationStatus;
	}
	
	public void setEvaluationFormParticipation(EvaluationFormParticipation evaluationFormParticipation) {
		this.evaluationFormParticipationRef = evaluationFormParticipation;
		this.participationStatus = evaluationFormParticipation.getStatus();
	}
	
	@Override
	public Date getSubmissionDate() {
		return submissionDate;
	}
	
	public void setSubmissionDate(Date submissionDate) {
		this.submissionDate = submissionDate;
	}
	
	
}
