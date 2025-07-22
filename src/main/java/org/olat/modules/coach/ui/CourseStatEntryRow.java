/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.coach.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.coach.model.CourseStatEntry;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.Certificates;
import org.olat.modules.coach.model.ParticipantStatisticsEntry.SuccessStatus;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 30 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseStatEntryRow {
	
	private boolean marked;
	private final CourseStatEntry entry;
	
	private FormLink markLink;
	
	public CourseStatEntryRow(CourseStatEntry entry) {
		this.entry = entry;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public Long getRepoKey() {
		return entry.getRepoKey();
	}
	
	public String getRepoDisplayName() {
		return entry.getRepoDisplayName();
	}
	
	public String getRepoTechnicalType() {
		return entry.getRepoTechnicalType();
	}
	
	public String getRepoExternalId() {
		return entry.getRepoExternalId();
	}

	public String getRepoExternalRef() {
		return entry.getRepoExternalRef();
	}

	public RepositoryEntryStatusEnum getRepoStatus() {
		return entry.getRepoStatus();
	}

	public Date getLifecycleStartDate() {
		return entry.getLifecycleStartDate();
	}

	public Date getLifecycleEndDate() {
		return entry.getLifecycleEndDate();
	}

	public int getParticipants() {
		return entry.getParticipants();
	}

	public int getParticipantsVisited() {
		return entry.getParticipantsVisited();
	}

	public int getParticipantsNotVisited() {
		return entry.getParticipantsNotVisited();
	}

	public Date getLastVisit() {
		return entry.getLastVisit();
	}

	public SuccessStatus getSuccessStatus() {
		return entry.getSuccessStatus();
	}
	
	public Long getStatusPassed() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numPassed();
	}
	
	public Long getStatusNotPassed() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numFailed();
	}
	
	public Long getStatusUndefined() {
		return entry.getSuccessStatus() == null ? null : entry.getSuccessStatus().numUndefined();
	}

	public Double getAverageScore() {
		return entry.getAverageScore();
	}

	public Double getAverageCompletion() {
		return entry.getAverageCompletion();
	}

	public Certificates getCertificates() {
		return entry.getCertificates();
	}
	
	public CourseStatEntry getEntry() {
		return entry;
	}

	public FormLink getMarkLink() {
		return markLink;
	}

	public void setMarkLink(FormLink markLink) {
		this.markLink = markLink;
	}
}
