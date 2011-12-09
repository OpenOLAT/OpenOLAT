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
import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.ModifiedInfo;

/**
 * Description:<br>
 * CheckpointResults object, stored in "o_checkpoint_results" via Hibernate
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class CheckpointResult extends PersistentObject implements ModifiedInfo, Serializable {
	
	private Date lastModified;
	private boolean result;
	private Long identityId;
	private Checkpoint checkpoint;

	public CheckpointResult() {
		//
	}
	
	public CheckpointResult(Checkpoint checkpoint, Long identityId, boolean result) {
		this.checkpoint = checkpoint;
		this.identityId = identityId;
		this.result = result;
		this.lastModified = new Date();
	}
	
	public Checkpoint getCheckpoint() {
		return checkpoint;
	}
	
	public void setCheckpoint(Checkpoint checkpoint) {
		this.checkpoint = checkpoint;
	}

	/**
	 * @return Returns the lastModified.
	 */
	public Date getLastModified() {
		return lastModified;
	}

	/**
	 * @return Returns the result.
	 */
	public boolean getResult() {
		return result;
	}

	/**
	 * @return Returns the identityId.
	 */
	public Long getIdentityId() {
		return identityId;
	}

	/**
	 * @param lastModified The lastModified to set.
	 */
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * @param result The result to set.
	 */
	public void setResult(boolean result) {
		this.result = result;
	}

	/**
	 * @param identityId The identityId to set.
	 */
	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}
	
}
