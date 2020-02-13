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

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.grading.model.ReferenceEntryWithStatistics;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 6 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignedReferenceEntryRow implements RepositoryEntryRef {
	
	private final ReferenceEntryWithStatistics statistics;

	private FormLink toolsLink;
	
	public AssignedReferenceEntryRow(ReferenceEntryWithStatistics statistics) {
		this.statistics = statistics;
	}
	
	@Override
	public Long getKey() {
		return statistics.getEntry().getKey();
	}
	
	public RepositoryEntry getReferenceEntry() {
		return statistics.getEntry();
	}
	
	public String getDisplayname() {
		return statistics.getEntry().getDisplayname();
	}
	
	public String getExternalRef() {
		return statistics.getEntry().getExternalRef();
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

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
