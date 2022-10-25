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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageBody;
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.Section;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfpage")
@Table(name="o_pf_page")
public class PageImpl implements Persistable, ModifiedInfo, CreateInfo, Page {
	
	private static final long serialVersionUID = -3846753221071930063L;

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

	/** Only used for order by */
	@Column(name="pos", insertable=false, updatable=false)
	private Integer pos;
	
	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_summary", nullable=true, insertable=true, updatable=true)
	private String summary;
	@Column(name="p_status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Column(name="p_image_path", nullable=true, insertable=true, updatable=true)
	private String imagePath;
	@Column(name="p_image_align", nullable=true, insertable=true, updatable=true)
	private String imageAlign;
	
	@Column(name="p_editable", nullable=false, insertable=true, updatable=true)
	private boolean editable;
	
	@Column(name="p_version", nullable=true, insertable=true, updatable=true)
	private int version;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_initial_publish_date", nullable=true, insertable=true, updatable=true)
	private Date initialPublicationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_last_publish_date", nullable=true, insertable=true, updatable=true)
	private Date lastPublicationDate;
	
	@ManyToOne(targetEntity=PageBodyImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_body_id", nullable=false, insertable=true, updatable=true)
	private PageBody body;
	
	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	@ManyToOne(targetEntity=SectionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_section_id", nullable=true, insertable=true, updatable=true)
	private Section section;
	
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
	public Date getLastModified() {
		return lastModified;
	}

	@Override
	public void setLastModified(Date date) {
		this.lastModified = date;
	}
	
	@Override
	@Transient
	public PortfolioElementType getType() {
		return PortfolioElementType.page;
	}

	@Override
	@Transient
	public PageStatus getPageStatus() {
		return StringHelper.containsNonWhitespace(status) ? PageStatus.valueOf(status) : null;
	}
	
	public void setPageStatus(PageStatus pageStatus) {
		if(pageStatus == null) {
			status = null;
		} else {
			status = pageStatus.name();
		}
	}
		
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Override
	public boolean isEditable() {
		return editable;
	}
	
	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	@Override
	public Date getInitialPublicationDate() {
		return initialPublicationDate;
	}

	public void setInitialPublicationDate(Date initialPublicationDate) {
		this.initialPublicationDate = initialPublicationDate;
	}

	@Override
	public Date getLastPublicationDate() {
		return lastPublicationDate;
	}

	public void setLastPublicationDate(Date lastPublicationDate) {
		this.lastPublicationDate = lastPublicationDate;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	@Override
	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public String getImagePath() {
		return imagePath;
	}

	@Override
	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getImageAlign() {
		return imageAlign;
	}

	public void setImageAlign(String imageAlign) {
		this.imageAlign = imageAlign;
	}

	@Override
	@Transient
	public PageImageAlign getImageAlignment() {
		return StringHelper.containsNonWhitespace(imageAlign) ? PageImageAlign.valueOf(imageAlign) : null;
	}

	@Override
	public void setImageAlignment(PageImageAlign align) {
		if(align == null) {
			imageAlign = null;
		} else {
			imageAlign = align.name();
		}
	}

	@Override
	public PageBody getBody() {
		return body;
	}

	public void setBody(PageBody body) {
		this.body = body;
	}

	@Override
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	@Override
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	@Override
	public int hashCode() {
		return key == null ? 8965446 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof PageImpl) {
			PageImpl page = (PageImpl)obj;
			return key != null && key.equals(page.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
