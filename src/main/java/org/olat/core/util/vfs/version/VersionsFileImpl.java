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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
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
public class VersionsFileImpl implements Versions {

	private boolean versioned;
	private String author;
	private String creator;
	private String revisionNr;
	private String comment;
	private Versionable currentVersion;
	private VFSLeaf versionFile;
	private List<VFSRevision> revisions;

	public VersionsFileImpl() {
	//
	}

	@Override
	public String getRevisionNr() {
		return revisionNr;
	}

	public void setRevisionNr(String revisionNr) {
		this.revisionNr = revisionNr;
	}

	public VFSLeaf getVersionFile() {
		return versionFile;
	}

	public void setVersionFile(VFSLeaf versionFile) {
		this.versionFile = versionFile;
	}

	public Versionable getCurrentVersion() {
		return currentVersion;
	}

	public void setCurrentVersion(Versionable currentVersion) {
		this.currentVersion = currentVersion;
	}

	@Override
	public boolean addVersion(Identity identity, String comments, InputStream newVersion) {
		return CoreSpringFactory.getImpl(VersionsManager.class).addVersion(getCurrentVersion(), identity, comments, newVersion);
	}

	@Override
	public boolean move(VFSContainer container) {
		return CoreSpringFactory.getImpl(VersionsManager.class).move(getCurrentVersion(), container);
	}

	@Override
	public boolean copy(VFSContainer container) {
		return CoreSpringFactory.getImpl(VersionsManager.class).copy(getCurrentVersion(), container);
	}

	@Override
	public boolean restore(Identity identity, VFSRevision version, String comments) {
		return CoreSpringFactory.getImpl(VersionsManager.class).restore(getCurrentVersion(), version, comments);
	}

	@Override
	public boolean delete(Identity identity, List<VFSRevision> revisionsToDelete) {
		return CoreSpringFactory.getImpl(VersionsManager.class).deleteRevisions(getCurrentVersion(), revisionsToDelete);
	}

	@Override
	public List<VFSRevision> getRevisions() {
		if (revisions == null) {
			revisions = new ArrayList<>();
		}
		return revisions;
	}

	public void setRevisions(List<VFSRevision> revisions) {
		this.revisions = revisions;
	}

	@Override
	public boolean isVersioned() {
		return versioned;
	}

	protected void setVersioned(boolean versioned) {
		this.versioned = versioned;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	@Override
	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	protected void update(Versions newVersions) {
		if (newVersions instanceof VersionsFileImpl) {
			VersionsFileImpl newVersionsImpl = (VersionsFileImpl) newVersions;
			author = newVersionsImpl.getAuthor();
			creator = newVersionsImpl.getCreator();
			currentVersion = newVersionsImpl.getCurrentVersion();
			revisionNr = newVersionsImpl.getRevisionNr();
			comment = newVersionsImpl.getComment();
		}

		revisions = new ArrayList<>();
		revisions.addAll(newVersions.getRevisions());
	}
}
