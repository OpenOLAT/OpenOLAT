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
package org.olat.mcp.model;

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

import org.olat.basesecurity.model.OrganisationImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Organisation;
import org.olat.core.id.Persistable;
import org.olat.mcp.McpProfile;

/**
 * The link of a profile to an organisation. A profile applies to its
 * organisations and to every organisation below them. The pair of profile
 * and organisation is unique. The rows are never updated.
 *
 * Initial date: 12 Oct 2026<br>
 * @author AI, ai@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="mcpprofiletoorganisation")
@Table(name="o_mcp_profile_to_organisation")
public class McpProfileToOrganisationImpl implements CreateInfo, Persistable {

	private static final long serialVersionUID = 2026101201L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@ManyToOne(targetEntity=McpProfileImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_profile", nullable=false, insertable=true, updatable=false)
	private McpProfile profile;
	@ManyToOne(targetEntity=OrganisationImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_organisation", nullable=false, insertable=true, updatable=false)
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

	public McpProfile getProfile() {
		return profile;
	}

	public void setProfile(McpProfile profile) {
		this.profile = profile;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	@Override
	public int hashCode() {
		return key == null ? 917246 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof McpProfileToOrganisationImpl rel) {
			return key != null && key.equals(rel.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
