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
package org.olat.modules.portfolio.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PageStatus;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfpagebody")
@Table(name="o_pf_page_body")
public class PageBodyImpl implements Persistable, ModifiedInfo, CreateInfo, PageBody {

	private static final long serialVersionUID = -2441388713989345529L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	
	@Column(name="p_usage", nullable=false, insertable=true, updatable=true)
	private int usage;
	@Column(name="p_synthetic_status", nullable=true, insertable=true, updatable=true)
	private String syntheticStatus;
	
	@OneToMany(targetEntity=AbstractPart.class, fetch=FetchType.LAZY, mappedBy="body",
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<PagePart> parts;
	
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
	
	public void setCreationDate(Date date) {
		this.creationDate = date;
	}
	
	@Override
	public Date getLastModified() {
		return lastModified;
	}
	
	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}

	@Override
	public int getUsage() {
		return usage;
	}

	public void setUsage(int usage) {
		this.usage = usage;
	}

	public String getSyntheticStatus() {
		return syntheticStatus;
	}

	public void setSyntheticStatus(String syntheticStatus) {
		this.syntheticStatus = syntheticStatus;
	}

	@Override
	public PageStatus getSyntheticStatusEnum() {
		if(StringHelper.containsNonWhitespace(syntheticStatus)) {
			return PageStatus.valueOf(syntheticStatus);
		}
		return null;
	}
	
	public void setSyntheticStatusEnum(PageStatus status) {
		syntheticStatus = status == null ? null : status.name();
	}

	@Override
	public List<PagePart> getParts() {
		if(parts == null) {
			parts = new ArrayList<>();
		}
		return parts;
	}

	public void setParts(List<PagePart> parts) {
		this.parts = parts;
	}

	@Override
	public int hashCode() {
		return key == null ? 154589656 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PageBodyImpl) {
			PageBodyImpl body = (PageBodyImpl)obj;
			return key != null && key.equals(body.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
