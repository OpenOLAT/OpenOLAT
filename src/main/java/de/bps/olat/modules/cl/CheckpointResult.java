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

import java.util.Date;

/**
 * Description:<br>
 * CheckpointResults object, stored in "o_checkpoint_results" via Hibernate
 * 
 * <P>
 * Initial Date:  23.07.2009 <br>
 * @author bja <bja@bps-system.de>
 */
public class CheckpointResult {
	
	private Long key = null;
	private int version = 0;
	protected Date creationDate;
	
	private Date lastModified;
	private boolean result;
	private Long identityId;
	private Checkpoint checkpoint;
	
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

	public Checkpoint getCheckpoint() {
		return checkpoint;
	}
	
	public void setCheckpoint(Checkpoint checkpoint) {
		this.checkpoint = checkpoint;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public boolean getResult() {
		return result;
	}

	public Long getIdentityId() {
		return identityId;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public void setIdentityId(Long identityId) {
		this.identityId = identityId;
	}
}
