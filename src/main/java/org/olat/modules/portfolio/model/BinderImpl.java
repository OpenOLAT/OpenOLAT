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
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
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
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.BinderStatus;
import org.olat.modules.portfolio.PortfolioElementType;
import org.olat.modules.portfolio.Section;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 06.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfbinder")
@Table(name="o_pf_binder")
@NamedQuery(name="loadBinderByKey", query="select binder from pfbinder as binder" +
											" inner join fetch binder.baseGroup as baseGroup" +
											" left join fetch binder.entry as v" +
											" left join fetch v.olatResource as res" +
											" where binder.key=:portfolioKey")

public class BinderImpl implements Persistable, ModifiedInfo, CreateInfo, Binder {

	private static final long serialVersionUID = -2607615295380443760L;

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
	
	@Column(name="p_copy_date", nullable=true, insertable=true, updatable=true)
	private Date copyDate;
	@Column(name="p_return_date", nullable=true, insertable=true, updatable=true)
	private Date returnDate;
	@Column(name="p_deadline", nullable=true, insertable=true, updatable=true)
	private Date deadLine;

	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_summary", nullable=true, insertable=true, updatable=true)
	private String summary;
	@Column(name="p_status", nullable=true, insertable=true, updatable=true)
	private String status;
	@Column(name="p_image_path", nullable=true, insertable=true, updatable=true)
	private String imagePath;

	@ManyToOne(targetEntity=GroupImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_group_id", nullable=false, insertable=true, updatable=false)
	private Group baseGroup;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_olatresource_id", nullable=true, insertable=true, updatable=false)
	private OLATResource olatResource;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_entry_id", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry entry;
    @Column(name="p_subident", nullable=true, insertable=true, updatable=false)
	private String subIdent;
	
	@ManyToOne(targetEntity=BinderImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template_id", nullable=true, insertable=true, updatable=false)
	private Binder template;
	
	@OneToMany(targetEntity=SectionImpl.class, mappedBy="binder", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<Section> sections;
	
	@OneToMany(targetEntity=AssignmentImpl.class, mappedBy="binder", fetch=FetchType.LAZY,
			orphanRemoval=true, cascade={CascadeType.REMOVE})
	@OrderColumn(name="pos")
	private List<Assignment> assignments;
	
	
	@Override
	public Long getKey() {
		return key;
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
	public Date getCopyDate() {
		return copyDate;
	}

	public void setCopyDate(Date copyDate) {
		this.copyDate = copyDate;
	}

	@Override
	public Date getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}

	@Override
	public Date getDeadLine() {
		return deadLine;
	}

	public void setDeadLine(Date deadLine) {
		this.deadLine = deadLine;
	}

	@Override
	public String getResourceableTypeName() {
		return olatResource == null ? "Binder" : olatResource.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return olatResource == null ? getKey() : olatResource.getResourceableId();
	}

	@Override
	@Transient
	public PortfolioElementType getType() {
		return PortfolioElementType.binder;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	@Transient
	@Override
	public BinderStatus getBinderStatus() {
		return StringHelper.containsNonWhitespace(status) ? BinderStatus.valueOf(status) : BinderStatus.open;
	}

	@Override
	public void setBinderStatus(BinderStatus binderStatus) {
		if(binderStatus == null) {
			status = null;
		} else {
			status = binderStatus.name();
		}
	}

	@Override
	public String getSummary() {
		return summary;
	}

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

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}

	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	@Override
	public RepositoryEntry getEntry() {
		return entry;
	}

	public void setEntry(RepositoryEntry entry) {
		this.entry = entry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public Binder getTemplate() {
		return template;
	}

	public void setTemplate(Binder template) {
		this.template = template;
	}

	public List<Section> getSections() {
		if(sections == null) {
			sections = new ArrayList<>();
		}
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections;
	}

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
	public Group getBaseGroup() {
		return baseGroup;
	}

	public void setBaseGroup(Group baseGroup) {
		this.baseGroup = baseGroup;
	}

	@Override
	public int hashCode() {
		return key == null ? 836578 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof BinderImpl) {
			BinderImpl binder = (BinderImpl)obj;
			return key != null && key.equals(binder.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
