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
import org.olat.core.id.Identity;
import org.olat.modules.grading.GraderStatus;
import org.olat.modules.grading.model.GraderStatistics;
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 17 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GraderRow {
	
	private final Identity grader;
	private final List<GraderStatus> graderStatus;
	private final GraderStatistics statistics;
	private final List<AbsenceLeave> absenceLeaves;
	
	private final long recordedTimeInSeconds;
	private final long recordedMetadataTimeInSeconds;
	
	private FormLink toolsLink;
	
	public GraderRow(Identity grader, GraderStatistics statistics,
			long recordedTimeInSeconds, long recordedMetadataTimeInSeconds,
			List<AbsenceLeave> absenceLeaves, List<GraderStatus> status) {
		this.grader = grader;
		this.graderStatus = status;
		this.statistics = statistics;
		this.recordedTimeInSeconds = recordedTimeInSeconds;
		this.recordedMetadataTimeInSeconds = recordedMetadataTimeInSeconds;
		if(absenceLeaves != null && !absenceLeaves.isEmpty()) {
			this.absenceLeaves = absenceLeaves;
		} else {
			this.absenceLeaves = Collections.emptyList();
		}
	}
	
	public Identity getGrader() {
		return grader;
	}
	
	public List<GraderStatus> getGraderStatus() {
		return graderStatus;
	}
	
	public List<AbsenceLeave> getAbsenceLeaves() {
		return absenceLeaves;
	}
	
	public boolean hasGraderStatus(GraderStatus status) {
		return graderStatus != null && graderStatus.contains(status);
	}
	
	public boolean hasOnlyGraderStatus(GraderStatus status) {
		return graderStatus != null && graderStatus.contains(status) && graderStatus.size() == 1;
	}
	
	public Long getTotalAssignments() {
		return statistics == null || statistics.getTotalAssignments() == 0l ? null : Long.valueOf(statistics.getTotalAssignments());
	}
	
	public Long getNumOfDoneAssignments() {
		return statistics == null || statistics.getNumOfDoneAssignments() == 0l ? null : Long.valueOf(statistics.getNumOfDoneAssignments());
	}
	
	public Long getNumOfOpenAssignments() {
		return statistics == null || statistics.getNumOfOpenAssignments() == 0l ? null : Long.valueOf(statistics.getNumOfOpenAssignments());
	}
	
	public Long getNumOfOverdueAssignments() {
		return statistics == null || statistics.getNumOfOverdueAssignments() == 0l ? null : Long.valueOf(statistics.getNumOfOverdueAssignments());
	}
	
	public Date getOldestOpenAssignment() {
		return statistics == null ? null : statistics.getOldestOpenAssignment();
	}
	
	public Long getRecordedTimeInSeconds() {
		return recordedTimeInSeconds;
	}
	
	public Long getRecordedMetadataTimeInSeconds() {
		return recordedMetadataTimeInSeconds;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
