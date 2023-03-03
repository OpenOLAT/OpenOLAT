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

import org.olat.basesecurity.IdentityImpl;
import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjProject;

/**
 * 
 * Initial date: 16 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="projactivity")
@Table(name="o_proj_activity")
public class ProjActivityImpl implements ProjActivity, Persistable {

	private static final long serialVersionUID = -3836019443267315948L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Enumerated(EnumType.STRING)
	@Column(name="p_action", nullable=false, insertable=true, updatable=false)
	private Action action;
	@Enumerated(EnumType.STRING)
	@Column(name="p_action_target", nullable=false, insertable=true, updatable=false)
	private ActionTarget actionTarget;

	@Column(name="p_before", nullable=true, insertable=true, updatable=false)
	private String before;	
	@Column(name="p_after", nullable=true, insertable=true, updatable=false)
	private String after;
	@Column(name="p_temp_identifier", nullable=true, insertable=true, updatable=false)
	private String tempIdentifier;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_doer", nullable=false, insertable=true, updatable=false)
	private Identity doer;
	@ManyToOne(targetEntity=ProjProjectImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_project", nullable=false, insertable=true, updatable=false)
	private ProjProject project;
	@ManyToOne(targetEntity=ProjArtefactImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_artefact", nullable=true, insertable=true, updatable=false)
	private ProjArtefact artefact;
	@ManyToOne(targetEntity=ProjArtefactImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_artefact_reference", nullable=true, insertable=true, updatable=false)
	private ProjArtefact artefactReference;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_member", nullable=true, insertable=true, updatable=false)
	private Identity member;
	@ManyToOne(targetEntity=OrganisationImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_organisation", nullable=true, insertable=true, updatable=false)
	private Organisation organisation;
	
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
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Override
	public ActionTarget getActionTarget() {
		return actionTarget;
	}

	public void setActionTarget(ActionTarget actionTarget) {
		this.actionTarget = actionTarget;
	}

	@Override
	public String getBefore() {
		return before;
	}

	public void setBefore(String before) {
		this.before = before;
	}

	@Override
	public String getAfter() {
		return after;
	}

	public void setAfter(String after) {
		this.after = after;
	}

	@Override
	public String getTempIdentifier() {
		return tempIdentifier;
	}

	public void setTempIdentifier(String tempIdentifier) {
		this.tempIdentifier = tempIdentifier;
	}

	@Override
	public Identity getDoer() {
		return doer;
	}

	public void setDoer(Identity doer) {
		this.doer = doer;
	}

	@Override
	public ProjProject getProject() {
		return project;
	}

	public void setProject(ProjProject project) {
		this.project = project;
	}

	@Override
	public ProjArtefact getArtefact() {
		return artefact;
	}

	public void setArtefact(ProjArtefact artefact) {
		this.artefact = artefact;
	}

	@Override
	public ProjArtefact getArtefactReference() {
		return artefactReference;
	}

	public void setArtefactReference(ProjArtefact artefactReference) {
		this.artefactReference = artefactReference;
	}

	@Override
	public Identity getMember() {
		return member;
	}

	public void setMember(Identity member) {
		this.member = member;
	}

	@Override
	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public int hashCode() {
		return key == null ? 236520 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof ProjActivityImpl) {
			ProjActivityImpl activity = (ProjActivityImpl)obj;
			return key != null && key.equals(activity.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
