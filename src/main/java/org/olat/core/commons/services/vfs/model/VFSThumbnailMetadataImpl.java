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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSThumbnailMetadata;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 14 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="vfsthumbnail")
@Table(name="o_vfs_thumbnail")
@NamedQuery(name="loadThumbnailByKey", query="select thumb from vfsthumbnail thumb where thumb.key=:thumbnailKey")
public class VFSThumbnailMetadataImpl implements Persistable, VFSThumbnailMetadata {

	private static final long serialVersionUID = -6149766723435220075L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;

	@Column(name="f_size", nullable=false, insertable=true, updatable=true)
	private long fileSize;  
	@Column(name="f_max_width", nullable=false, insertable=true, updatable=true)
	private int maxWidth;
	@Column(name="f_max_height", nullable=false, insertable=true, updatable=true)
	private int maxHeight;
	@Column(name="f_final_width", nullable=false, insertable=true, updatable=true)
	private int finalWidth;
	@Column(name="f_final_height", nullable=false, insertable=true, updatable=true)
	private int finalHeight;
	@Column(name="f_fill", nullable=false, insertable=true, updatable=true)
	private boolean fill;
	@Column(name="f_filename", nullable=false, insertable=true, updatable=true)
	private String filename;
	
	@ManyToOne(targetEntity=VFSMetadataImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_metadata", nullable=false, insertable=true, updatable=false)
	private VFSMetadata owner;
	
	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public int getMaxWidth() {
		return maxWidth;
	}
	
	public void setMaxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
	}

	@Override
	public int getMaxHeight() {
		return maxHeight;
	}
	
	public void setMaxHeight(int maxHeight) {
		this.maxHeight = maxHeight;
	}

	@Override
	public int getFinalWidth() {
		return finalWidth;
	}
	
	public void setFinalWidth(int finalWidth) {
		this.finalWidth = finalWidth;
	}

	@Override
	public int getFinalHeight() {
		return finalHeight;
	}
	
	public void setFinalHeight(int finalHeight) {
		this.finalHeight = finalHeight;
	}

	@Override
	public boolean isFill() {
		return fill;
	}
	
	public void setFill(boolean fill) {
		this.fill = fill;
	}

	@Override
	public String getFilename() {
		return filename;
	}
	
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public VFSMetadata getOwner() {
		return owner;
	}

	public void setOwner(VFSMetadata owner) {
		this.owner = owner;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 7386459 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof VFSThumbnailMetadataImpl) {
			VFSThumbnailMetadataImpl meta = (VFSThumbnailMetadataImpl)obj;
			return getKey().equals(meta.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
