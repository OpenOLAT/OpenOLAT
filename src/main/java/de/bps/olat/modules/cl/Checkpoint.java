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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;

/**
 * Description:<br>
 * Checkpoint object, stored in "o_checkpoint" via Hibernate
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class Checkpoint extends PersistentObject implements ModifiedInfo, Serializable {

	private static final long serialVersionUID = 6963103659557231124L;
	private String title;
	private String description;
	private String mode;
	private Date lastModified;
	private Checklist checklist;
	private List<CheckpointResult> results = new ArrayList<>();
	
	public Checkpoint() {
		//
	}

	/**
	 * @return Returns the title.
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return Returns the mode.
	 */
	public String getMode() {
		return mode;
	}

	/**
	 * @return Returns the lastmodified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return Returns the results.
	 */
	public List<CheckpointResult> getResults() {
		return results;
	}
	
	/**
	 * @param title The title to set.
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param mode The mode to set.
	 */
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	/**
	 * @param lastmodified The lastmodified to set.
	 */
	public void setLastModified(Date lastmodified) {
		this.lastModified = lastmodified;
	}

	/**
	 * @param results The results to set.
	 */
	public void setResults(List<CheckpointResult> results) {
		this.results = results;
	}

	/**
	 * @param checklist The checklist to set.
	 */
	public void setChecklist(Checklist checklist) {
		this.checklist = checklist;
	}

	/**
	 * @return Returns the checklist.
	 */
	public Checklist getChecklist() {
		return checklist;
	}

	/**
	 * Is this checkpoint selected for the given identity?
	 * @param identity
	 * @return <code>true</code> or <code>false</code>
	 */
	public Boolean getSelectionFor(Identity identity) {
		for(CheckpointResult result : getResults()) {
			if(result.getIdentityId().equals(identity.getKey())) {
				return result.getResult();
			}
		}
		return false;
	}
	
	/**
	 * Set the selection for the given identity.
	 * @param identity
	 * @param selected
	 */
	public void setSelectionFor(Identity identity, boolean selected) {
		boolean newResult = true;
		// iterate through the results to find associated result
		for(CheckpointResult result : getResults()) {
			if(result.getIdentityId().equals(identity.getKey())) {
				result.setResult(selected);
				result.setLastModified(new Date());
				newResult = false;
			}
		}
		// no result found, add new one
		if(newResult) {
			addResult(identity, selected);
		}
	}
	
	/**
	 * Internal helper to add a new result.
	 * @param identity
	 * @param selected
	 */
	private void addResult(Identity identity, boolean selected) {
		CheckpointResult result = new CheckpointResult();
		result.setIdentityId(identity.getKey());
		result.setResult(selected);
		result.setLastModified(new Date());
		result.setCheckpoint(this);
		getResults().add(result);
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
		if(obj instanceof Checkpoint) {
			Checkpoint entry = (Checkpoint)obj;
			return getKey() != null && getKey().equals(entry.getKey());
		}
		return false;
	}
}
