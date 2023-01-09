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
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

/**
 * Description:<BR/> Initial Date: Aug 23, 2004
 * 
 * @author gnaegi
 */
@Entity
@Table(name="o_gp_bgarea")
public class BGAreaImpl implements Persistable, BGArea {

	private static final long serialVersionUID = 4452153442327716546L;
	
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
	@Column(name="area_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	
	@Column(name="name", nullable=false, insertable=true, updatable=true)
	private String name;
	@Column(name="descr", nullable=true, insertable=true, updatable=true)
	private String description;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_resource", nullable=false, insertable=true, updatable=false)
	private OLATResource resource;

	/**
	 * Constructor used for Hibernate instanciation.
	 */
	protected BGAreaImpl() {
	// nothing to do
	}
	
	BGAreaImpl(String name, String description, OLATResource resource) {
		setName(name);
		setResource(resource);
		setDescription(description);
		setCreationDate(new Date());
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
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getShortName() {
		return getName();
	}
	
	@Override
	public String toString() {
		return "name=" + name + "::" + super.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BGArea area) {
			return getKey() != null && getKey().equals(area.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}

	@Override
	public int hashCode() {
		return getKey() ==null ? 568301 : getKey().hashCode();
	}

}
