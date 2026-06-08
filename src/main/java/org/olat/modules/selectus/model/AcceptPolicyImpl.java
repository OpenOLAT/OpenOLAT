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
package org.olat.modules.selectus.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="racceptpolicy")
@Table(name="o_selectus_position_policy")
@NamedQueries({
	@NamedQuery(name="policyByIdentityAndPosition", query="select policy from racceptpolicy policy where policy.identity.key=:identityKey and policy.position.key=:positionKey")
})
public class AcceptPolicyImpl implements CreateInfo, Persistable, AcceptPolicy {

	private static final long serialVersionUID = -6291500537172391211L;
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "enhanced-sequence", parameters={
		@Parameter(name="sequence_name", value="hibernate_unique_key"),
		@Parameter(name="force_table_use", value="true"),
		@Parameter(name="optimizer", value="legacy-hilo"),
		@Parameter(name="value_column", value="next_hi"),
		@Parameter(name="increment_size", value="32767"),
		@Parameter(name="initial_value", value="32767")
	})
	@Column(name="pol_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity_id", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	
	@ManyToOne(targetEntity=PositionImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_position_id", nullable=false, insertable=true, updatable=false)
	private Position position;

	@Column(name="policytype", nullable=true, unique=false, insertable=true, updatable=true)
	private String name;
	@Column(name="dont_show_next_time", nullable=true, unique=false, insertable=true, updatable=true)
	private boolean dontShowNextTime = false;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}

	public boolean isDontShowNextTime() {
		return dontShowNextTime;
	}

	public void setDontShowNextTime(boolean dontShowNextTime) {
		this.dontShowNextTime = dontShowNextTime;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("acceptPolicy[")
		  .append(super.toString())
		  .append(";").append(identity.toString())
		  .append(";").append(position.toString());
		return sb.toString();
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 2978928 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AcceptPolicyImpl) {
			AcceptPolicyImpl policy = (AcceptPolicyImpl)obj;
			return getKey() != null && getKey().equals(policy.getKey());
		}
		return false;
	}
}