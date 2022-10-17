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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Persistable;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;

/**
 * 
 * Initial date: 07.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfsection")
@Table(name="o_pf_section")
public class SectionImpl implements Persistable, CreateInfo, Section {

	private static final long serialVersionUID = -8520480114785588566L;

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
	@GeneratedValue
	@Column(name="pos", insertable=false, updatable=false)
	private long pos;

	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="p_status", nullable=false, insertable=true, updatable=true)
	private String status;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_begin", nullable=true, insertable=true, updatable=true)
	private Date beginDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="p_end", nullable=true, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="p_override_begin_end", nullable=true, insertable=true, updatable=true)
	private boolean overrideBeginEndDates;

	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	@ManyToOne(targetEntity=BinderImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_binder_id", nullable=false, insertable=true, updatable=false)
	private Binder binder;
	
	@OneToMany(targetEntity=PageImpl.class, mappedBy="section", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<Page> pages;
	
	@OneToMany(targetEntity=AssignmentImpl.class, mappedBy="section", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<Assignment> assignments;
	

	@ManyToOne(targetEntity=SectionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template_reference_id", nullable=true, insertable=true, updatable=false)
	private Section templateReference;
	
	public SectionImpl() {
		//
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

	public long getPos() {
		return pos;
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
		lastModified = date;
	}
	
	@Override
	@Transient
	public PortfolioElementType getType() {
		return PortfolioElementType.section;
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
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	@Transient
	public SectionStatus getSectionStatus() {
		return SectionStatus.secureValueOf(status);
	}
	
	public void setSectionStatus(SectionStatus sectionStatus) {
		if(sectionStatus == null) {
			status = null;
		} else {
			status = sectionStatus.name();
		}
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	public Date getBeginDate() {
		return beginDate;
	}

	@Override
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	@Override
	public boolean isOverrideBeginEndDates() {
		return overrideBeginEndDates;
	}

	@Override
	public void setOverrideBeginEndDates(boolean overrideBeginEndDates) {
		this.overrideBeginEndDates = overrideBeginEndDates;
	}

	@Override
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	@Override
	public Binder getBinder() {
		return binder;
	}

	public void setBinder(Binder binder) {
		this.binder = binder;
	}

	@Override
	public Section getTemplateReference() {
		return templateReference;
	}

	public void setTemplateReference(Section templateReference) {
		this.templateReference = templateReference;
	}

	@Override
	public List<Page> getPages() {
		if(pages == null) {
			pages = new ArrayList<>();
		}
		return pages;
	}

	public void setPages(List<Page> pages) {
		this.pages = pages;
	}

	@Override
	public List<Assignment> getAssignments() {
		if(assignments == null) {
			assignments = new ArrayList<>();
		}
		return assignments;
	}

	public void setAssignments(List<Assignment> assignments) {
		this.assignments = assignments;
	}

	@Override
	public int hashCode() {
		return key == null ? -7659236 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof SectionImpl) {
			SectionImpl section = (SectionImpl)obj;
			return key != null && key.equals(section.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
