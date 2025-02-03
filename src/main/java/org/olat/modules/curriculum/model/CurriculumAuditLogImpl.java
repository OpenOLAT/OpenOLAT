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
package org.olat.modules.curriculum.model;

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
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementAuditLog;

/**
 * 
 * Initial date: 3 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="curriculumauditlog")
@Table(name="o_cur_audit_log")
public class CurriculumAuditLogImpl implements CurriculumElementAuditLog, Persistable {
	
	private static final long serialVersionUID = -117956831890037751L;
	
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
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=CurriculumImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_curriculum", nullable=true, insertable=true, updatable=false)
	private Curriculum curriculum;
	@ManyToOne(targetEntity=CurriculumElementImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_curriculum_element", nullable=true, insertable=true, updatable=false)
	private CurriculumElement curriculumElement;

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
	
	

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

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
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Curriculum getCurriculum() {
		return curriculum;
	}

	public void setCurriculum(Curriculum curriculum) {
		this.curriculum = curriculum;
	}

	@Override
	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}

	public void setCurriculumElement(CurriculumElement curriculumElement) {
		this.curriculumElement = curriculumElement;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 56489 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof CurriculumAuditLogImpl audit) {
			return getKey() != null && getKey().equals(audit.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

}
