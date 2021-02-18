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
package org.olat.core.commons.services.vfs.ui.version;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSMetadataRef;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 21 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DeletedFileRow implements VFSMetadataRef {
	
	private final VFSMetadata metadata;
	private VFSRevision lastRevision;
	private final List<VFSRevision> revisions = new ArrayList<>();
	
	private DownloadLink downloadLink;
	
	public DeletedFileRow(VFSMetadata metadata) {
		this.metadata = metadata;
	}
	
	@Override
	public Long getKey() {
		return metadata.getKey();
	}
	
	public String getFilename() {
		return metadata.getFilename();
	}
	
	public Identity getDeletedBy() {
		return lastRevision == null ? null : lastRevision.getFileInitializedBy();
	}
	
	public Date getDate() {
		return lastRevision == null ? null : lastRevision.getCreationDate();
	}
	
	public VFSMetadata getMetadata() {
		return metadata;
	}
	
	public DownloadLink getDownloadLink() {
		return downloadLink;
	}
	
	public void setDownloadLink(DownloadLink link) {
		downloadLink = link;
	}
	
	public VFSRevision getLastRevision() {
		return lastRevision;
	}
	
	public List<VFSRevision> getRevisions() {
		return revisions;
	}
	
	public void addRevision(VFSRevision revision) {
		if(revision == null) return;
		
		if(lastRevision == null || lastRevision.getRevisionNr() < revision.getRevisionNr()) {
			lastRevision = revision;
		}
		revisions.add(revision);
	}
}
