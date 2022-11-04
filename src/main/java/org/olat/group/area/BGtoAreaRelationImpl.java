/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.area;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.id.Persistable;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

/**
 * Description:<BR/> Implementation of the business group to business group
 * area relation <P/> Initial Date: Aug 23, 2004
 * 
 * @author gnaegi
 */
@Entity
@Table(name="o_gp_bgtoarea_rel")
public class BGtoAreaRelationImpl implements Persistable, BGtoAreaRelation {

	private static final long serialVersionUID = 770758447044422197L;
	
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
	@Column(name="bgtoarea_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@OneToOne(targetEntity=BGAreaImpl.class)
	@JoinColumn(name="area_fk", nullable=false, insertable=true, updatable=false)
	private BGArea groupArea;
	@OneToOne(targetEntity=BusinessGroupImpl.class)
	@JoinColumn(name="group_fk", nullable=false, insertable=true, updatable=false)
	private BusinessGroup businessGroup;

	/**
	 * package local
	 */
	protected BGtoAreaRelationImpl() {
	  // for hibernate
	}

	BGtoAreaRelationImpl(BGArea groupArea, BusinessGroup businessGroup) {
		setBusinessGroup(businessGroup);
		setGroupArea(groupArea);
	}
	
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
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	@Override
	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	@Override
	public BGArea getGroupArea() {
		return groupArea;
	}

	@Override
	public void setGroupArea(BGArea groupArea) {
		this.groupArea = groupArea;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 6780945 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGtoAreaRelationImpl rel) {
			return getKey() != null && getKey().equals(rel.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
