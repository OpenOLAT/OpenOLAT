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

import java.util.ArrayList;
import java.util.List;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 6 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceEntryWithStatistics implements RepositoryEntryRef {
	
	private final RepositoryEntry entry;
	private final ReferenceEntryStatistics statistics;
	private long recordedTimeInSeconds = 0l;
	private long recordedMetadataTimeInSeconds = 0l;
	private final List<AbsenceLeave> absenceLeaves = new ArrayList<>(4);

	public ReferenceEntryWithStatistics(RepositoryEntry entry) {
		this.entry = entry;
		statistics = ReferenceEntryStatistics.empty(entry); 
	}
	
	public ReferenceEntryWithStatistics(ReferenceEntryStatistics statistics) {
		this.entry = statistics.getEntry();
		this.statistics = statistics;
	}
	
	@Override
	public Long getKey() {
		return entry.getKey();
	}
	
	public RepositoryEntry getEntry() {
		return entry;
	}

	public ReferenceEntryStatistics getStatistics() {
		return statistics;
	}
	
	public List<AbsenceLeave> getAbsenceLeaves() {
		return absenceLeaves;
	}
	
	public void addAbsenceLeave(AbsenceLeave absenceLeave) {
		absenceLeaves.add(absenceLeave);
	}

	public long getRecordedTimeInSeconds() {
		return recordedTimeInSeconds;
	}

	public void addRecordedTimeInSeconds(long seconds) {
		recordedTimeInSeconds += seconds;
	}
	
	public long getRecordedMetadataTimeInSeconds() {
		return recordedMetadataTimeInSeconds;
	}

	public void addRecordedMetadataTimeInSeconds(long seconds) {
		recordedMetadataTimeInSeconds += seconds;
	}
}
