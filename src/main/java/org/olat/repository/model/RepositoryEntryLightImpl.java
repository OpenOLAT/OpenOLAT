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
package org.olat.repository.model;

import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.repository.RepositoryEntryLight;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 15.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="repoentrylight")
@Table(name="o_repositoryentry")
@NamedQueries({
	@NamedQuery(name="loadLightReByKey", query="select v from repoentrylight v where v.key=:repoKey")
})
public class RepositoryEntryLightImpl implements RepositoryEntryLight, Persistable {

	private static final long serialVersionUID = 5427458190644827372L;

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
	@Column(name="repositoryentry_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Column(name="displayname", nullable=false, insertable=false, updatable=false)
	private String displayname;
	@Column(name="description", nullable=false, insertable=false, updatable=false)
	private String description;

	@Column(name="status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="allusers", nullable=false, insertable=true, updatable=true)
	private boolean allUsers;
	@Column(name="guests", nullable=false, insertable=true, updatable=true)
	private boolean guests;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_olatresource", nullable=false, insertable=true, updatable=false)
	private OLATResource olatResource;
	
	@OneToMany(targetEntity=RepositoryEntryToGroupRelation.class, fetch=FetchType.LAZY)
	@JoinColumn(name="fk_entry_id")
	private Set<RepositoryEntryToGroupRelation> groups;
	
	public Long getKey() {
		return key;
	}
	
	@Override
	public String getDisplayname() {
		return displayname;
	}
	
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getResourceType() {
		return olatResource.getResourceableTypeName();
	}

	public OLATResource getOlatResource() {
		return olatResource;
	}

	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	public Set<RepositoryEntryToGroupRelation> getGroups() {
		return groups;
	}

	public void setGroups(Set<RepositoryEntryToGroupRelation> groups) {
		this.groups = groups;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return RepositoryEntryStatusEnum.valueOf(status);
	}

	@Override
	public boolean isAllUsers() {
		return allUsers;
	}

	public void setAllUsers(boolean allUsers) {
		this.allUsers = allUsers;
	}
	@Override
	public boolean isGuests() {
		return guests;
	}

	public void setGuests(boolean guests) {
		this.guests = guests;
	}

	@Override
	public int hashCode() {
		return key == null ? 891265 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof RepositoryEntryLightImpl) {
			RepositoryEntryLightImpl other = (RepositoryEntryLightImpl) obj;
			return getKey().equals(other.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {	
		return equals(persistable);
	}
}