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

package org.olat.core.commons.services.tagging.model;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Persistable;

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
 * 
 * Description:<br>
 * Hibernate implementation of @ref{org.olat.core.commons.services.tagging.model.Tag} 
 * 
 * <P>
 * Initial Date:  19 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity
@Table(name="o_tag")
public class TagImpl implements Persistable, CreateInfo, Tag {

	private static final long serialVersionUID = 2272529253221047436L;
	
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
	@Column(name="tag_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Version
	private int version = 0;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;

	@Column(name="tag", nullable=true, insertable=true, updatable=true)
	private String tag;
	@Column(name="resname", nullable=true, insertable=true, updatable=true)
	private String resName;
	@Column(name="resid", nullable=true, insertable=true, updatable=true)
	private Long resId;
	@Column(name="ressubpath", nullable=true, insertable=true, updatable=true)
	private String resSubPath;
	@Column(name="businesspath", nullable=true, insertable=true, updatable=true)
	private String businessPath;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_author_id", nullable=false, insertable=true, updatable=false)
	private Identity author;
	
	public TagImpl() {
		//
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
	public String getTag() {
		return tag;
	}

	@Override
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	@Override
	public OLATResourceable getOLATResourceable() {
		final Long id = resId;
		final String name = resName;
		
		return new OLATResourceable() {
			@Override
			public Long getResourceableId() {
				return id;
			}

			@Override
			public String getResourceableTypeName() {
				return name;
			}
		};
	}
	
	public String getResName() {
		return resName;
	}
	
	public void setResName(String resName) {
		this.resName = resName;
	}
	
	public Long getResId() {
		return resId;
	}
	
	public void setResId(Long resId) {
		this.resId = resId;
	}
	
	@Override
	public String getResSubPath() {
		return resSubPath;
	}
	
	public void setResSubPath(String resSubPath) {
		this.resSubPath = resSubPath;
	}
	
	@Override
	public String getBusinessPath() {
		return businessPath;
	}
	
	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}
	
	@Override
	public Identity getAuthor() {
		return author;
	}
	
	public void setAuthor(Identity author) {
		this.author = author;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -92544 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj instanceof TagImpl tagImpl) {
			return getKey() != null && getKey().equals(tagImpl.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
