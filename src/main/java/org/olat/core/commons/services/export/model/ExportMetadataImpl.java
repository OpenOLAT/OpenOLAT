/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.export.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.export.ArchiveType;
import org.olat.core.commons.services.export.ExportMetadata;
import org.olat.core.commons.services.taskexecutor.model.PersistentTask;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.model.VFSMetadataImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 21 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="exportmetadata")
@Table(name="o_ex_export_metadata")
public class ExportMetadataImpl implements ExportMetadata, Persistable {
	
	private static final long serialVersionUID = -3813832030475683717L;

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
	
	@Enumerated(EnumType.STRING)
	@Column(name="e_archive_type", nullable=true, insertable=true, updatable=true)
	private ArchiveType archiveType;

	@Column(name="e_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="e_description", nullable=true, insertable=true, updatable=true)
	private String description;
	
	@Column(name="e_file_name", nullable=true, insertable=true, updatable=true)
	private String filename;
	@Column(name="e_file_path", nullable=true, insertable=true, updatable=true)
	private String filePath;

	
	@Column(name="e_only_administrators", nullable=true, insertable=true, updatable=true)
	private boolean onlyAdministrators;
	@Column(name="e_expiration_date", nullable=true, insertable=true, updatable=true)
	private Date expirationDate;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
	@Column(name="e_sub_ident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
	@ManyToOne(targetEntity=PersistentTask.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_task", nullable=true, insertable=true, updatable=false)
	private PersistentTask task;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_creator", nullable=true, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=VFSMetadataImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_metadata", nullable=true, insertable=true, updatable=true)
	private VFSMetadata metadata;
	
	public ExportMetadataImpl() {
		//
	}

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
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public ArchiveType getArchiveType() {
		return archiveType;
	}

	@Override
	public void setArchiveType(ArchiveType archiveType) {
		this.archiveType = archiveType;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getFilename() {
		return filename;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilePath() {
		return filePath;
	}

	@Override
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	@Override
	public boolean isOnlyAdministrators() {
		return onlyAdministrators;
	}

	@Override
	public void setOnlyAdministrators(boolean onlyAdministrators) {
		this.onlyAdministrators = onlyAdministrators;
	}

	@Override
	public Date getExpirationDate() {
		return expirationDate;
	}

	@Override
	public void setExpirationDate(Date expirationDate) {
		this.expirationDate = expirationDate;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public PersistentTask getTask() {
		return task;
	}

	public void setTask(PersistentTask task) {
		this.task = task;
	}
	
	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public VFSMetadata getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(VFSMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 497510 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ExportMetadataImpl exp) {
			return getKey() != null && getKey().equals(exp.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
