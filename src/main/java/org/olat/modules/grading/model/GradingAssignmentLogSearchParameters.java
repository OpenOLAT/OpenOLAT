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
package org.olat.modules.grading.model;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 18 mai 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class GradingAssignmentLogSearchParameters {
	
	private IdentityRef grader;
	private RepositoryEntryRef entry;
	private RepositoryEntryRef referenceEntry;
	
	private Date closedToDate;
	private Date closedFromDate;

	public IdentityRef getGrader() {
		return grader;
	}

	public void setGrader(IdentityRef grader) {
		this.grader = grader;
	}

	public RepositoryEntryRef getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntryRef entry) {
		this.entry = entry;
	}

	public RepositoryEntryRef getReferenceEntry() {
		return referenceEntry;
	}

	public void setReferenceEntry(RepositoryEntryRef referenceEntry) {
		this.referenceEntry = referenceEntry;
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
}
