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
package org.olat.core.util.vfs.version;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * This class implements the @see org.olat.core.util.vfs.version.Versions
 * for a file which is versioned. The attributes versionFile and currentVersion
 * are set by the VersionsFilemanager.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class VersionsFileImpl {

	private boolean versioned;
	private String author;
	private String creator;
	private int revisionNr;
	private String comment;
	private Object currentVersion;
	private VFSLeaf versionFile;
	private List<RevisionFileImpl> revisions;

	public VersionsFileImpl() {
	//
	}

	public int getRevisionNr() {
		return revisionNr;
	}

	public void setRevisionNr(int revisionNr) {
		this.revisionNr = revisionNr;
	}

	public VFSLeaf getVersionFile() {
		return versionFile;
	}

	public void setVersionFile(VFSLeaf versionFile) {
		this.versionFile = versionFile;
	}

	public Object getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Object currentVersion) {
		this.currentVersion = currentVersion;
	}

	public List<RevisionFileImpl> getRevisions() {
		if (revisions == null) {
			revisions = new ArrayList<>();
		}
		return revisions;
	}

	public void setRevisions(List<RevisionFileImpl> revisions) {
		this.revisions = revisions;
	}

	public boolean isVersioned() {
		return versioned;
	}

	protected void setVersioned(boolean versioned) {
		this.versioned = versioned;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
