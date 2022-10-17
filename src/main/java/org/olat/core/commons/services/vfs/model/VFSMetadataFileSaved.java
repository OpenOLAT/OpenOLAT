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
package org.olat.core.commons.services.vfs.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 15 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="vfsmetadatafilesaved")
@Table(name="o_vfs_metadata")
public class VFSMetadataFileSaved implements Persistable {

	private static final long serialVersionUID = -7289462568737206096L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Column(name="f_lastmodified", nullable=false, insertable=true, updatable=true)
	private Date fileLastModified;
	@Column(name="f_size", nullable=false, insertable=true, updatable=true)
	private long fileSize;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_initialized_by", nullable=true, insertable=true, updatable=true)
	private Identity fileInitializedBy;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lastmodified_by", nullable=true, insertable=true, updatable=true)
	private Identity fileLastModifiedBy;
	@Column(name="f_deleted", nullable=false, insertable=true, updatable=true)
	private boolean deleted;
	@Column(name="f_filename", nullable=false, insertable=false, updatable=false)
	private String filename;
	@Column(name="f_relative_path", nullable=false, insertable=false, updatable=false)
	private String relativePath;
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Date getFileLastModified() {
		return fileLastModified;
	}

	public void setFileLastModified(Date fileLastModified) {
		this.fileLastModified = fileLastModified;
	}

	public Identity getFileInitializedBy() {
		return fileInitializedBy;
	}

	public void setFileInitializedBy(Identity fileInitializedBy) {
		this.fileInitializedBy = fileInitializedBy;
	}

	public Identity getFileLastModifiedBy() {
		return fileLastModifiedBy;
	}

	public void setFileLastModifiedBy(Identity fileLastModifiedBy) {
		this.fileLastModifiedBy = fileLastModifiedBy;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public String getRelativePath() {
		return relativePath;
	}
	
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@Override
	public int hashCode() {
		return getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof VFSMetadataFileSaved) {
			VFSMetadataFileSaved count = (VFSMetadataFileSaved)obj;
			return getKey().equals(count.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
