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

import java.util.Date;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRevision;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 19 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RevisionRow {
	
	private boolean current;
	
	private final long revisionNr;
	private final String formatedRevisionNr;
	private long revisionSize;
	private Identity author;
	private String comment;
	
	private VFSRevision revision;
	private VFSMetadata metadata;
	private final DownloadLink downloadLink;
	
	public RevisionRow(VFSMetadata metadata, String formatedRevisionNr, DownloadLink downloadLink) {
		current = true;
		this.metadata = metadata;
		this.revisionNr = metadata.getRevisionNr();
		this.formatedRevisionNr = formatedRevisionNr;
		comment = metadata.getRevisionComment();
		author = metadata.getFileInitializedBy();
		this.downloadLink = downloadLink;
	}
	
	public RevisionRow(VFSRevision revision, String formatedRevisionNr, DownloadLink downloadLink) {
		current = false;
		this.revisionNr = revision.getRevisionNr();
		this.formatedRevisionNr = formatedRevisionNr;
		author = revision.getFileInitializedBy();
		comment = revision.getRevisionComment();
		revisionSize = revision.getSize();
		this.revision = revision;
		this.downloadLink = downloadLink;
	}

	public boolean isCurrent() {
		return current;
	}
	
	public void setCurrent(boolean current) {
		this.current = current;
	}
	
	public Long getKey() {
		if(isCurrent()) {
			return metadata.getKey();
		}
		return revision instanceof Persistable ? ((Persistable)revision).getKey() : null;
	}
	
	public long getRevisionNr() {
		return revisionNr;
	}

	public String getFormatedRevisionNr() {
		return formatedRevisionNr;
	}
	
	public long getSize() {
		return current ? metadata.getFileSize() : revisionSize;
	}
	
	public Identity getAuthor() {
		return author;
	}
	
	public String getRevisionComment() {
		return comment;
	}
	
	public Date getRevisionDate() {
		return current ? metadata.getFileLastModified() : revision.getFileLastModified();
	}
	
	public VFSRevision getRevision() {
		return revision;
	}

	public DownloadLink getDownloadLink() {
		return downloadLink;
	}
}
