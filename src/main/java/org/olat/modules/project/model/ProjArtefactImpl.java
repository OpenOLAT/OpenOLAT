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
package org.olat.modules.project.model;

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

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectStatus;

/**
 * 
 * Initial date: 22 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="projartefact")
@Table(name="o_proj_artefact")
public class ProjArtefactImpl implements ProjArtefact, Persistable  {
	
	private static final long serialVersionUID = 3396702613158819838L;

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
	
	@Column(name="p_type", nullable=false, insertable=true, updatable=false)
	private String type;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_content_modified_date", nullable=false, insertable=true, updatable=true)
	private Date contentModifiedDate;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_content_modified_by", nullable=false, insertable=true, updatable=true)
	private Identity contentModifiedBy;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_deleted_date", nullable=true, insertable=true, updatable=true)
	private Date deletedDate;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_deleted_by", nullable=false, insertable=true, updatable=true)
	private Identity deletedBy;
	@Enumerated(EnumType.STRING)
	@Column(name="p_status", nullable=false, insertable=true, updatable=true)
	private ProjectStatus status;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
	private Identity creator;
	@ManyToOne(targetEntity=ProjProjectImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_project", nullable=false, insertable=true, updatable=false)
	private ProjProject project;
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_group", nullable=true, insertable=true, updatable=true)
	private Group baseGroup;
	
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
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public Date getContentModifiedDate() {
		return contentModifiedDate;
	}

	@Override
	public void setContentModifiedDate(Date contentModifiedDate) {
		this.contentModifiedDate = contentModifiedDate;
	}

	@Override
	public Identity getContentModifiedBy() {
		return contentModifiedBy;
	}

	@Override
	public void setContentModifiedBy(Identity contentModifiedBy) {
		this.contentModifiedBy = contentModifiedBy;
	}

	@Override
	public Date getDeletedDate() {
		return deletedDate;
	}

	@Override
	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	@Override
	public Identity getDeletedBy() {
		return deletedBy;
	}

	@Override
	public void setDeletedBy(Identity deletedBy) {
		this.deletedBy = deletedBy;
	}

	@Override
	public ProjectStatus getStatus() {
		return status;
	}

	@Override
	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public ProjProject getProject() {
		return project;
	}

	public void setProject(ProjProject project) {
		this.project = project;
	}
	
	@Override
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjArtefactImpl other = (ProjArtefactImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
