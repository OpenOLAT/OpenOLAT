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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.olat.modules.cl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Description:<br>
 * Checkpoint object, stored in "o_checkpoint" via Hibernate
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class Checkpoint  {
	
	private Long key = null;
	private int version = 0;
	protected Date creationDate;

	private String title;
	private String description;
	private String mode;
	private Date lastModified;
	private Checklist checklist;
	private List<CheckpointResult> results = new ArrayList<>();

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getMode() {
		return mode;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public List<CheckpointResult> getResults() {
		return results;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public void setLastModified(Date lastmodified) {
		this.lastModified = lastmodified;
	}

	public void setResults(List<CheckpointResult> results) {
		this.results = results;
	}

	public void setChecklist(Checklist checklist) {
		this.checklist = checklist;
	}

	public Checklist getChecklist() {
		return checklist;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -34892 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof Checkpoint entry) {
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}
}
