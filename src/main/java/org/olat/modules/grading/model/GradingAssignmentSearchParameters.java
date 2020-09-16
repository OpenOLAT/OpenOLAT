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
package org.olat.modules.grading.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingAssignmentSearchParameters {
	
	private Identity grader;
	private RepositoryEntry entry;
	private RepositoryEntry referenceEntry;
	
	private BigDecimal scoreFrom;
	private BigDecimal scoreTo;
	private Boolean passed;
	
	private Date gradingToDate;
	private Date gradingFromDate;
	private Date closedToDate;
	private Date closedFromDate;
	private List<TaxonomyLevel> taxonomyLevels;
	private List<SearchStatus> assignmentStatus;
	
	private IdentityRef manager;

	public Identity getGrader() {
		return grader;
	}

	public void setGrader(Identity grader) {
		this.grader = grader;
	}

	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}

	public void setReferenceEntry(RepositoryEntry referenceEntry) {
		this.referenceEntry = referenceEntry;
	}
	
	public List<SearchStatus> getAssignmentStatus() {
		return assignmentStatus;
	}

	public void setAssignmentStatus(List<SearchStatus> assignmentStatus) {
		this.assignmentStatus = assignmentStatus;
	}

	public List<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}

	public void setTaxonomyLevels(List<TaxonomyLevel> taxonomyLevels) {
		this.taxonomyLevels = taxonomyLevels;
	}

	public Date getGradingToDate() {
		return gradingToDate;
	}

	public void setGradingToDate(Date gradingToDate) {
		this.gradingToDate = gradingToDate;
	}

	public Date getGradingFromDate() {
		return gradingFromDate;
	}

	public void setGradingFromDate(Date gradingFromDate) {
		this.gradingFromDate = gradingFromDate;
	}

	public Date getClosedToDate() {
		return closedToDate;
	}

	public void setClosedToDate(Date closedToDate) {
		this.closedToDate = closedToDate;
	}

	public Date getClosedFromDate() {
		return closedFromDate;
	}

	public void setClosedFromDate(Date closedFromDate) {
		this.closedFromDate = closedFromDate;
	}

	public BigDecimal getScoreFrom() {
		return scoreFrom;
	}

	public void setScoreFrom(BigDecimal scoreFrom) {
		this.scoreFrom = scoreFrom;
	}

	public BigDecimal getScoreTo() {
		return scoreTo;
	}

	public void setScoreTo(BigDecimal scoreTo) {
		this.scoreTo = scoreTo;
	}

	public Boolean getPassed() {
		return passed;
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}

	public IdentityRef getManager() {
		return manager;
	}

	public void setManager(IdentityRef manager) {
		this.manager = manager;
	}




	public enum SearchStatus {
		unassigned,
		open,
		reminder1,
		reminder2,
		deadlineMissed,
		closed
	}
}
