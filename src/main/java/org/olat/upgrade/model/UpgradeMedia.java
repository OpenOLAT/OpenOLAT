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
package org.olat.upgrade.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * 
 * Initial date: 16 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="upgrademedia")
@Table(name="o_media")
public class UpgradeMedia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_collection_date", nullable=false, insertable=true, updatable=false)
	private Date collectionDate;
	@Column(name="p_type", nullable=false, insertable=true, updatable=false)
	private String type;
	
	@Column(name="p_storage_path", nullable=true, insertable=true, updatable=true)
	private String storagePath;
	@Column(name="p_root_filename", nullable=true, insertable=true, updatable=true)
	private String rootFilename;
	@Column(name="p_content", nullable=true, insertable=true, updatable=true)
	private String content;
	
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}
	
	public Date getCollectionDate() {
		return collectionDate;
	}
	
	public void setCollectionDate(Date collectionDate) {
		this.collectionDate = collectionDate;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public String getStoragePath() {
		return storagePath;
	}
	
	public void setStoragePath(String storagePath) {
		this.storagePath = storagePath;
	}
	
	public String getRootFilename() {
		return rootFilename;
	}
	
	public void setRootFilename(String rootFilename) {
		this.rootFilename = rootFilename;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	@Override
	public int hashCode() {
		return key == null ? 459537 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof UpgradeMedia media) {
			return key != null && key.equals(media.getKey());
		}
		return false;
	}
}
