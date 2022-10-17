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

package org.olat.core.commons.services.notifications.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;

/**
 * 
 * Initial Date: 21.10.2004 <br>
 * @author Felix Jost
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="notipublisher")
@Table(name="o_noti_pub")
public class PublisherImpl implements Publisher, CreateInfo, Persistable  {

	private static final long serialVersionUID = -7684628889607509977L;
	
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
	@Column(name="publisher_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	//for compatibility purpose
	@Column(name="version", nullable=false, insertable=true, updatable=false)
	private int version = 0;
	
	// e.g. Forum

	@Column(name="publishertype", nullable=false, insertable=true, updatable=false)
	private String type; 
	// e.g. CourseModule
	@Column(name="resname", nullable=false, insertable=true, updatable=false)
	private String resName; 
	// e.g. 2343284327
	@Column(name="resid", nullable=false, insertable=true, updatable=false)
	private Long resId; 
	// e.g. 69680861018558 (for a node in a course)
	@Column(name="subident", nullable=true, insertable=true, updatable=false)
	private String subidentifier;
	@Column(name="businesspath", nullable=true, insertable=true, updatable=false)
	private String businessPath;
	// any additional data (depending on type)
	@Column(name="data", nullable=true, insertable=true, updatable=true)
	private String data; 
	// 0 = ok
	@Column(name="state", nullable=false, insertable=true, updatable=false)
	private int state; 

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="latestnews", nullable=false, insertable=true, updatable=true)
	private Date latestNewsDate;


	/**
	 * for hibernate only
	 */
	public PublisherImpl() {
		//
	}

	/**
	 * @param resName
	 * @param resId
	 * @param subidentifier
	 * @param type
	 * @param data
	 * @param state
	 */
	public PublisherImpl(String resName, Long resId, String subidentifier, String type, String data, String businessPath, Date latestNewsDate, int state) {
		this.resId = resId;
		this.resName = resName;
		this.subidentifier = subidentifier;
		this.type = type;
		this.data = data;
		this.state = state;
		this.businessPath = businessPath;
		this.latestNewsDate = latestNewsDate;
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
	public Long getResId() {
		return resId;
	}

	@Override
	public void setResId(Long resId) {
		this.resId = resId;
	}

	@Override
	public String getResName() {
		return resName;
	}

	@Override
	public void setResName(String resName) {
		this.resName = resName;
	}

	@Override
	public String getSubidentifier() {
		return subidentifier;
	}

	/**
	 * @param subidentifier (max len 255)
	 */
	@Override
	public void setSubidentifier(String subidentifier) {
		this.subidentifier = subidentifier;
	}

	/**
	 * @return the type
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 */
	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String getData() {
		return data;
	}

	/**
	 * @param data
	 */
	@Override
	public void setData(String data) {
		this.data = data;
	}

	/**
	 * @return Returns the state.
	 */
	@Override
	public int getState() {
		return state;
	}

	/**
	 * @param state The state to set.
	 */
	@Override
	public void setState(int state) {
		this.state = state;
	}
	
	/**
	 * @return Returns the latestNewsDate.
	 */
	@Override
	public Date getLatestNewsDate() {
		return latestNewsDate;
	}
	
	/**
	 * @param latestNewsDate The latestNewsDate to set.
	 */
	@Override
	public void setLatestNewsDate(Date latestNewsDate) {
		this.latestNewsDate = latestNewsDate;
	}

	@Override
	public String getBusinessPath() {
		return businessPath;
	}

	@Override
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 54212 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PublisherImpl) {
			PublisherImpl p = (PublisherImpl)obj;
			return getKey() != null && getKey().equals(p.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}