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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.core.commons.services.vfs.VFSStatistics;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 10 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="vfsstatistics")
@Table(name="o_vfs_statistics")
public class VFSStatisticsImpl implements Persistable, VFSStatistics {

	private static final long serialVersionUID = -7124435947598523998L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="f_files_amount", nullable=false, insertable=true, updatable=false)
	private long filesAmount;
	@Column(name="f_files_size", nullable=false, insertable=true, updatable=false)
	private long filesSize;

	@Column(name="f_trash_amount", nullable=false, insertable=true, updatable=false)
	private long trashAmount;
	@Column(name="f_trash_size", nullable=false, insertable=true, updatable=false)
	private long trashSize;

	@Column(name="f_revisions_amount", nullable=false, insertable=true, updatable=false)
	private long revisionsAmount;
	@Column(name="f_revisions_size", nullable=false, insertable=true, updatable=false)
	private long revisionsSize;

	@Column(name="f_thumbnails_amount", nullable=false, insertable=true, updatable=false)
	private long thumbnailsAmount;
	@Column(name="f_thumbnails_size", nullable=false, insertable=true, updatable=false)
	private long thumbnailsSize;
	
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
	public long getFilesAmount() {
		return filesAmount;
	}

	public void setFilesAmount(long filesAmount) {
		this.filesAmount = filesAmount;
	}

	@Override
	public long getFilesSize() {
		return filesSize;
	}

	public void setFilesSize(long filesSize) {
		this.filesSize = filesSize;
	}

	@Override
	public long getTrashAmount() {
		return trashAmount;
	}

	public void setTrashAmount(long trashAmount) {
		this.trashAmount = trashAmount;
	}

	@Override
	public long getTrashSize() {
		return trashSize;
	}

	public void setTrashSize(long trashSize) {
		this.trashSize = trashSize;
	}

	@Override
	public long getRevisionsAmount() {
		return revisionsAmount;
	}

	public void setRevisionsAmount(long revisionsAmount) {
		this.revisionsAmount = revisionsAmount;
	}

	@Override
	public long getRevisionsSize() {
		return revisionsSize;
	}

	public void setRevisionsSize(long revisionsSize) {
		this.revisionsSize = revisionsSize;
	}

	@Override
	public long getThumbnailsAmount() {
		return thumbnailsAmount;
	}

	public void setThumbnailsAmount(long thumbnailsAmount) {
		this.thumbnailsAmount = thumbnailsAmount;
	}

	@Override
	public long getThumbnailsSize() {
		return thumbnailsSize;
	}

	public void setThumbnailsSize(long thumbnailsSize) {
		this.thumbnailsSize = thumbnailsSize;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -767822189 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof VFSStatisticsImpl) {
			VFSStatisticsImpl stats = (VFSStatisticsImpl)obj;
			return getKey() != null && getKey().equals(stats.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
