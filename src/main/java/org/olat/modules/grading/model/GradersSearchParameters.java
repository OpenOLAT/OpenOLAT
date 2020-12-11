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

import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.grading.GraderStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradersSearchParameters {
	
	private Date gradingTo;
	private Date gradingFrom;
	private Date closedToDate;
	private Date closedFromDate;
	private Identity grader;
	private Identity manager;
	private List<GraderStatus> status;
	private RepositoryEntry referenceEntry;

	public Date getGradingTo() {
		return gradingTo;
	}

	public void setGradingTo(Date gradingTo) {
		this.gradingTo = gradingTo;
	}

	public Date getGradingFrom() {
		return gradingFrom;
	}

	public void setGradingFrom(Date gradingFrom) {
		this.gradingFrom = gradingFrom;
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

	public List<GraderStatus> getStatus() {
		return status;
	}

	public void setStatus(List<GraderStatus> status) {
		this.status = status;
	}

	public Identity getGrader() {
		return grader;
	}

	public void setGrader(Identity grader) {
		this.grader = grader;
	}

	public RepositoryEntry getReferenceEntry() {
		return referenceEntry;
	}

	public void setReferenceEntry(RepositoryEntry referenceEntry) {
		this.referenceEntry = referenceEntry;
	}

	public Identity getManager() {
		return manager;
	}

	public void setManager(Identity manager) {
		this.manager = manager;
	}


}
