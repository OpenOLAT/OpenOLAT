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

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Description:<br>
 * This class describes a file's revision. The attributes container and
 * file are set by the VersionsFileManager.
 * 
 * <P>
 * Initial Date: 21 sept. 2009 <br>
 * 
 * @author srosse
 */
public class RevisionFileImpl implements VFSRevision {

	private String author;
	private String comment;
	private String name;
	private String uuid;
	private long lastModified;

	private VFSLeaf file;
	private VFSContainer container;
	private String revisionNr;
	private String filename;
	private MetaInfo metadata;

	/**
	 * Only for the VersionsFileManager or XStream
	 */
	public RevisionFileImpl() {
	//
	}
	
	/**
	 * UUID of the revision
	 * @return
	 */
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the file of this revision
	 */
	public VFSLeaf getFile() {
		if (file == null) {
			file = (VFSLeaf) container.resolve(filename);
		}
		return file;
	}

	public void setContainer(VFSContainer container) {
		this.container = container;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public long getSize() {
		VFSLeaf f = getFile();
		return f == null ? -1l : f.getSize();
	}

	@Override
	public InputStream getInputStream() {
		VFSLeaf f = getFile();
		return f == null ? null : f.getInputStream();
	}

	@Override
	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getName() {
		if (name == null || name.length() == 0) { return getFilename(); }
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MetaInfo getMetadata() {
		return metadata;
	}

	public void setMetadata(MetaInfo metadata) {
		this.metadata = metadata;
	}

	@Override
	public String getRevisionNr() {
		return revisionNr;
	}

	public void setRevisionNr(String revisionNr) {
		this.revisionNr = revisionNr;
	}

	@Override
	public int hashCode() {
		return filename == null ? 26592 : filename.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj instanceof RevisionFileImpl) {
			RevisionFileImpl impl = (RevisionFileImpl) obj;
			return filename != null && filename.equals(impl.filename);
		}
		return false;
	}
}