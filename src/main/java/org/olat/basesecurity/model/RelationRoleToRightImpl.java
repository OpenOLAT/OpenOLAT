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
package org.olat.basesecurity.model;

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

import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationRole;
import org.olat.basesecurity.RelationRoleToRight;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 28 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="relationroletoright")
@Table(name="o_bs_relation_role_to_right")
public class RelationRoleToRightImpl implements RelationRoleToRight, Persistable {

	private static final long serialVersionUID = 6149090168353809087L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@ManyToOne(targetEntity=RelationRoleImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_role_id", nullable=false, insertable=true, updatable=false)
	private RelationRole role;
	
	@ManyToOne(targetEntity=RelationRightImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_right_id", nullable=false, insertable=true, updatable=false)
	private RelationRight right;

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
	public RelationRole getRelationRole() {
		return role;
	}

	public void setRole(RelationRole role) {
		this.role = role;
	}

	@Override
	public RelationRight getRelationRight() {
		return right;
	}

	public void setRight(RelationRight right) {
		this.right = right;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? -381746 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof RelationRoleToRightImpl) {
			RelationRoleToRightImpl relation = (RelationRoleToRightImpl)obj;
			return getKey() != null && getKey().equals(relation.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
