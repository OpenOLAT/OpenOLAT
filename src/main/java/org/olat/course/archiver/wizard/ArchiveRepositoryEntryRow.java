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
package org.olat.course.archiver.wizard;

import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ArchiveRepositoryEntryRow {
	
	private int numOfArchives = 0;
	private int numOfCompleteArchives = 0;
	private boolean completeArchive;
	private boolean runningArchive;
	
	private final RepositoryEntry entry;
	private final SingleSelection archiveTypeEl;
	
	public ArchiveRepositoryEntryRow(RepositoryEntry entry, SingleSelection typeEl) {
		this.entry = entry;
		this.archiveTypeEl = typeEl;
	}
	
	public Long getKey() {
		return entry.getKey();
	}
	
	public String getDisplayName() {
		return entry.getDisplayname();
	}
	
	public String getExternalRef() {
		return entry.getExternalRef();
	}
	
	public RepositoryEntryStatusEnum getStatus() {
		return entry.getEntryStatus();
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}
	
	public long getNumOfArchives() {
		return numOfArchives;
	}
	
	public void incrementNumOfArchives() {
		numOfArchives++;
	}
	
	public long getNumOfCompleteArchives() {
		return numOfCompleteArchives;
	}
	
	public void incrementNumOfCompleteArchives() {
		numOfCompleteArchives++;
	}

	public boolean isCompleteArchive() {
		return completeArchive;
	}

	public void setCompleteArchive(boolean completeArchive) {
		this.completeArchive = completeArchive;
	}

	public boolean isRunningArchive() {
		return runningArchive;
	}

	public void setRunningArchive(boolean runningArchive) {
		this.runningArchive = runningArchive;
	}

	public SingleSelection getArchiveTypeEl() {
		return archiveTypeEl;
	}
}
