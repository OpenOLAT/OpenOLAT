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
package org.olat.group.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroupLazy;
import org.olat.group.BusinessGroupManagedFlag;

/**
 * 
 * Initial date: 15.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="lazybusinessgroup")
@Table(name="o_gp_member_v")
public class BusinessGroupLazyImpl implements BusinessGroupLazy, CreateInfo, Persistable {

	private static final long serialVersionUID = 5125563005863650603L;

	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="bg_id", nullable=false, insertable=true, updatable=false)
	private Long key;
	
	@Column(name="member_id", nullable=false, insertable=true, updatable=true)
	private Long memberId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="bg_creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="bg_name", nullable=false, insertable=true, updatable=true)
	private String name;
	
	@Column(name="bg_desc", nullable=false, insertable=true, updatable=true)
	private String description;

	@Column(name="bg_managed_flags", nullable=false, insertable=true, updatable=true)
	private String managedFlagsString;

	@Override
	public Long getKey() {
		return key;
	}
	
	public Long getMemberId() {
		return memberId;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String getResourceableTypeName() {
		return "BusinessGroup";
	}

	public String getManagedFlagsString() {
		return managedFlagsString;
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return BusinessGroupManagedFlag.toEnum(managedFlagsString);
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	@Override
	public int hashCode() {
		return key == null ? 925867 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BusinessGroupLazyImpl) {
			BusinessGroupLazyImpl msg = (BusinessGroupLazyImpl)obj;
			return key != null && key.equals(msg.key);
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
