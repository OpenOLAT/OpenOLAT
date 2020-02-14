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
package org.olat.modules.grading.ui;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.grading.model.ReferenceEntryStatistics;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 6 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignedReferenceEntryRow implements RepositoryEntryRef {
	
	private final RepositoryEntry entry;
	private final ReferenceEntryStatistics statistics;
	private final List<AbsenceLeave> absenceLeaves;

	private FormLink toolsLink;
	
	public AssignedReferenceEntryRow(ReferenceEntryWithStatistics statistics) {
		entry = statistics.getEntry();
		this.statistics = statistics.getStatistics();
		if(statistics.getAbsenceLeaves() != null && !statistics.getAbsenceLeaves().isEmpty()) {
			absenceLeaves = statistics.getAbsenceLeaves();
		} else {
			absenceLeaves = Collections.emptyList();
		}
	}
	
	@Override
	public Long getKey() {
		return entry.getKey();
	}
	
	public RepositoryEntry getReferenceEntry() {
		return entry;
	}
	
	public String getDisplayname() {
		return entry.getDisplayname();
	}
	
	public String getExternalRef() {
		return entry.getExternalRef();
	}

	public long getRecordedTimeInSeconds() {
		return statistics.getRecordedTimeInSeconds();
	}
	
	public long getTotalAssignments() {
		return statistics.getTotalAssignments();
	}
	
	public long getNumOfDoneAssignments() {
		return statistics.getNumOfDoneAssignments();
	}
	
	public long getNumOfOpenAssignments() {
		return statistics.getNumOfOpenAssignments();
	}
	
	public long getNumOfOverdueAssignments() {
		return statistics.getNumOfOverdueAssignments();
	}
	
	public Date getOldestOpenAssignment() {
		return statistics.getOldestOpenAssignment();
	}
	
	public List<AbsenceLeave> getAbsenceLeaves() {
		return absenceLeaves;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
