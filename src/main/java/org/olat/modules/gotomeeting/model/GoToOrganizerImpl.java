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
package org.olat.modules.gotomeeting.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.gotomeeting.GoToOrganizer;

/**
 * 
 * Initial date: 21.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="gotoorganizer")
@Table(name="o_goto_organizer")
@NamedQuery(name="loadOrganizerByKey", query="select organizer from gotoorganizer organizer where organizer.key=:key")
@NamedQuery(name="getSystemOrganizers", query="select organizer from gotoorganizer organizer where organizer.owner is null")
@NamedQuery(name="getSystemOrganizersAndMy", query="select organizer from gotoorganizer organizer where organizer.owner is null or organizer.owner.key=:identityKey")
public class GoToOrganizerImpl implements GoToOrganizer, Persistable, ModifiedInfo {
	
	private static final long serialVersionUID = -1633399884431270798L;

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
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="g_name", nullable=true, insertable=true, updatable=true)
	private String name;

	@Column(name="g_account_key", nullable=true, insertable=true, updatable=true)
	private String accountKey;
	
	@Column(name="g_access_token", nullable=false, insertable=true, updatable=true)
	private String accessToken;
	@Column(name="g_refresh_token", nullable=false, insertable=true, updatable=true)
	private String refreshToken;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_renew_date", nullable=true, insertable=true, updatable=true)
	private Date renewDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="g_renew_refresh_date", nullable=true, insertable=true, updatable=true)
	private Date renewRefreshDate;
	
	@Column(name="g_organizer_key", nullable=false, insertable=true, updatable=true)
	private String organizerKey;
	@Column(name="g_username", nullable=false, insertable=true, updatable=false)
	private String username;
	
	@Column(name="g_firstname", nullable=true, insertable=true, updatable=true)
	private String firstName;
	@Column(name="g_lastname", nullable=true, insertable=true, updatable=true)
	private String lastName;
	@Column(name="g_email", nullable=true, insertable=true, updatable=true)
	private String email;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_identity", nullable=true, insertable=true, updatable=false)
	private Identity owner;

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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	@Override
	public Date getRenewDate() {
		return renewDate;
	}

	public void setRenewDate(Date renewDate) {
		this.renewDate = renewDate;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	public Date getRenewRefreshDate() {
		return renewRefreshDate;
	}

	public void setRenewRefreshDate(Date renewRefreshDate) {
		this.renewRefreshDate = renewRefreshDate;
	}

	public String getAccountKey() {
		return accountKey;
	}

	public void setAccountKey(String accountKey) {
		this.accountKey = accountKey;
	}

	@Override
	public String getOrganizerKey() {
		return organizerKey;
	}

	public void setOrganizerKey(String organizerKey) {
		this.organizerKey = organizerKey;
	}

	@Override
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public Identity getOwner() {
		return owner;
	}

	public void setOwner(Identity owner) {
		this.owner = owner;
	}

	@Override
	public int hashCode() {
		return key == null ? 4839 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof GoToOrganizerImpl) {
			GoToOrganizerImpl organizer = (GoToOrganizerImpl)obj;
			return key != null && key.equals(organizer.getKey());
		}
		return super.equals(obj);
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GoToOrganizer[key=").append(getKey() == null ? "null" : getKey())
		  .append(":organizerKey=").append(organizerKey == null ? "null" : organizerKey)
		  .append(":accountKey=").append(accountKey == null ? "null" : accountKey)
		  .append("]");
		return sb.toString();
	}
}
