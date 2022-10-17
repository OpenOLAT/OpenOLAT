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

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.olat.basesecurity.OrganisationType;
import org.olat.basesecurity.OrganisationTypeToType;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="organisationtypetotype")
@Table(name="o_org_type_to_type")
public class OrganisationTypeToTypeImpl implements Persistable, OrganisationTypeToType {

	private static final long serialVersionUID = -7454973947275371839L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@ManyToOne(targetEntity=OrganisationTypeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_type", nullable=false, insertable=true, updatable=false)
	private OrganisationType organisationType;
	@ManyToOne(targetEntity=OrganisationTypeImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_allowed_sub_type", nullable=false, insertable=true, updatable=false)
	private OrganisationType allowedSubOrganisationType;
	

	@Override
	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	@Override
	public OrganisationType getOrganisationType() {
		return organisationType;
	}

	public void setOrganisationType(OrganisationType organisationType) {
		this.organisationType = organisationType;
	}

	@Override
	public OrganisationType getAllowedSubOrganisationType() {
		return allowedSubOrganisationType;
	}

	public void setAllowedSubOrganisationType(OrganisationType allowedSubOrganisationType) {
		this.allowedSubOrganisationType = allowedSubOrganisationType;
	}


	@Override
	public int hashCode() {
		return getKey() == null ? 541318 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof OrganisationTypeToTypeImpl) {
			OrganisationTypeToTypeImpl type = (OrganisationTypeToTypeImpl)obj;
			return getKey() != null && getKey().equals(type.getKey());
		}
		return false	;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
