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
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactToArtefact;
import org.olat.modules.project.ProjProject;

/**
 * 
 * Initial date: 5 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="projartefacttoartefact")
@Table(name="o_proj_artefact_to_artefact")
public class ProjArtefactToArtefactImpl implements ProjArtefactToArtefact, Persistable {

	private static final long serialVersionUID = -841600238330785036L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=ProjArtefactImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_artefact1", nullable=false, insertable=true, updatable=false)
	private ProjArtefact artefact1;
	@ManyToOne(targetEntity=ProjArtefactImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_artefact2", nullable=false, insertable=true, updatable=false)
	private ProjArtefact artefact2;
	
	@ManyToOne(targetEntity=ProjProjectImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_project", nullable=false, insertable=true, updatable=false)
	private ProjProject project;
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
	private Identity creator;
	
	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}
	
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	@Override
	public ProjArtefact getArtefact1() {
		return artefact1;
	}
	
	public void setArtefact1(ProjArtefact artefact1) {
		this.artefact1 = artefact1;
	}
	
	@Override
	public ProjArtefact getArtefact2() {
		return artefact2;
	}
	
	public void setArtefact2(ProjArtefact artefact2) {
		this.artefact2 = artefact2;
	}
	
	@Override
	public ProjProject getProject() {
		return project;
	}
	
	public void setProject(ProjProject project) {
		this.project = project;
	}
	
	@Override
	public Identity getCreator() {
		return creator;
	}
	
	public void setCreator(Identity creator) {
		this.creator = creator;
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
		ProjArtefactToArtefactImpl other = (ProjArtefactToArtefactImpl) obj;
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