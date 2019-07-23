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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * Description:<br>
 * Checklist object, stored in "o_checklist" via Hibernate
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class Checklist extends PersistentObject implements ModifiedInfo, Serializable {

	private static final long serialVersionUID = -723324838609424892L;
	private String title;
	private String description;
	private Date lastMofified;
	private List<Checkpoint> checkpoints = new ArrayList<>();
	
	public Checklist() {
		this.title = "";
		this.description = "";
	}
	
	public Checklist(String title, String description, List<Checkpoint> checkpoints) {
		this.title = title;
		this.description = description;
		this.checkpoints = checkpoints;
		this.lastMofified = new Date();
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
	
	public Checkpoint getCheckpoint(Checkpoint cl) {
		if(checkpoints != null) {
			for(Checkpoint checkpoint:checkpoints) {
				if(checkpoint.equals(cl)) {
					return checkpoint;
				}
			}
		}
		return null;
	}
	
	/**
	 * @return Returns the checkpoints.
	 */
	public List<Checkpoint> getCheckpoints() {
		return checkpoints;
	}
	
	/**
	 * @return Returns the checkpoints sorted.
	 */
	public List<Checkpoint> getCheckpointsSorted(Comparator<Checkpoint> comparator) {
		Collections.sort(checkpoints, comparator);
		return checkpoints;
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
	 * @param checkpoints The checkpoints to set.
	 */
	public void setCheckpoints(List<Checkpoint> checkpoints) {
		this.checkpoints = checkpoints;
	}

	/**
	 * @see org.olat.core.id.ModifiedInfo#getLastModified()
	 */
	public Date getLastModified() {
		return this.lastMofified;
	}

	/**
	 * @see org.olat.core.id.ModifiedInfo#setLastModified(java.util.Date)
	 */
	public void setLastModified(Date date) {
		this.lastMofified = date;
	}

	/**
	 * Add checkpoint to this checklist
	 * @param index
	 * @param checkpoint
	 */
	public void addCheckpoint(int index, Checkpoint checkpoint) {
		if(index >= 0 && index < checkpoints.size()) {
			checkpoints.add(index, checkpoint);
		} else {
			checkpoints.add(checkpoint);
		}
	}
	
	/**
	 * Remove checkpoint from this checklist
	 * @param checkpoint
	 */
	public void removeCheckpoint(Checkpoint checkpoint) {
		checkpoints.remove(checkpoint);
	}
	
	/**
	 * @return <code>true</code> or <code>false</code>
	 */
	public boolean hasCheckpoints() {
		return (this.checkpoints != null) && (this.checkpoints.size() > 0);
	}
	
	/**
	 * Filter out unvisible checkpoints.
	 * @return List with all visible checkpoints
	 */
	public List<Checkpoint> getVisibleCheckpoints() {
		List<Checkpoint> visibleCheckpoints = new ArrayList<>();
		for(Checkpoint checkpoint : getCheckpoints()) {
			if(!checkpoint.getMode().equals(CheckpointMode.MODE_HIDDEN)) visibleCheckpoints.add(checkpoint);
		}
		return visibleCheckpoints;
	}
}
