/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.topicbroker.model;

import java.util.Date;

import org.olat.modules.topicbroker.TBBroker;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBTransientBroker implements TBBroker {
	
	private Long key;
	private Date creationDate;
	private Date lastModified;
	private Integer maxSelections;
	private Date selectionStartDate;
	private Date selectionEndDate;
	private Integer requiredEnrollments;
	private boolean participantCanEditRequiredEnrollments;
	private boolean autoEnrollment;
	private Date enrollmentStartDate;
	private Date enrollmentDoneDate;
	private boolean participantCanWithdraw;
	private Date withdrawEndDate;
	private RepositoryEntry repositoryEntry;
	private String subIdent;

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return "tb-broker";
	}

	@Override
	public Long getResourceableId() {
		return key;
	}
	
	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	

	@Override
	public Integer getMaxSelections() {
		return maxSelections;
	}

	@Override
	public void setMaxSelections(Integer maxSelections) {
		this.maxSelections = maxSelections;
	}

	@Override
	public Date getSelectionStartDate() {
		return selectionStartDate;
	}

	@Override
	public void setSelectionStartDate(Date selectionStartDate) {
		this.selectionStartDate = selectionStartDate;
	}

	@Override
	public Date getSelectionEndDate() {
		return selectionEndDate;
	}

	@Override
	public void setSelectionEndDate(Date selectionEndDate) {
		this.selectionEndDate = selectionEndDate;
	}

	@Override
	public Integer getRequiredEnrollments() {
		return requiredEnrollments;
	}

	@Override
	public void setRequiredEnrollments(Integer requiredEnrollments) {
		this.requiredEnrollments = requiredEnrollments;
	}

	@Override
	public boolean isParticipantCanEditRequiredEnrollments() {
		return participantCanEditRequiredEnrollments;
	}

	@Override
	public void setParticipantCanEditRequiredEnrollments(boolean participantCanEditRequiredEnrollments) {
		this.participantCanEditRequiredEnrollments = participantCanEditRequiredEnrollments;
	}

	@Override
	public boolean isAutoEnrollment() {
		return autoEnrollment;
	}

	@Override
	public void setAutoEnrollment(boolean autoEnrollment) {
		this.autoEnrollment = autoEnrollment;
	}

	@Override
	public Date getEnrollmentStartDate() {
		return enrollmentStartDate;
	}

	public void setEnrollmentStartDate(Date enrollmentStartDate) {
		this.enrollmentStartDate = enrollmentStartDate;
	}

	@Override
	public Date getEnrollmentDoneDate() {
		return enrollmentDoneDate;
	}

	public void setEnrollmentDoneDate(Date enrollmentDoneDate) {
		this.enrollmentDoneDate = enrollmentDoneDate;
	}

	@Override
	public boolean isParticipantCanWithdraw() {
		return participantCanWithdraw;
	}

	@Override
	public void setParticipantCanWithdraw(boolean participantCanWithdraw) {
		this.participantCanWithdraw = participantCanWithdraw;
	}

	@Override
	public Date getWithdrawEndDate() {
		return withdrawEndDate;
	}

	@Override
	public void setWithdrawEndDate(Date withdrawEndDate) {
		this.withdrawEndDate = withdrawEndDate;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

}
