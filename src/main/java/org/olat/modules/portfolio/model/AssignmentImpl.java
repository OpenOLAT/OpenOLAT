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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.Section;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 12.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfassignment")
@Table(name="o_pf_assignment")
public class AssignmentImpl implements Persistable, ModifiedInfo, CreateInfo, Assignment {

	private static final long serialVersionUID = -8045342700649500903L;

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
	
	@Column(name="p_status", nullable=false, insertable=true, updatable=true)
	private String status;
	@Column(name="p_type", nullable=false, insertable=true, updatable=true)
	private String type;
	@Column(name="p_version", nullable=false, insertable=true, updatable=true)
	private int version;
	@Column(name="p_template", nullable=false, insertable=true, updatable=true)
	private boolean template;
	
	@Column(name="p_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="p_summary", nullable=true, insertable=true, updatable=true)
	private String summary;
	@Column(name="p_content", nullable=true, insertable=true, updatable=true)
	private String content;
	
	@Column(name="p_storage", nullable=true, insertable=true, updatable=true)
	private String storage;
	
	@ManyToOne(targetEntity=SectionImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_section_id", nullable=true, insertable=true, updatable=true)
	private Section section;
	
	@ManyToOne(targetEntity=BinderImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_binder_id", nullable=true, insertable=true, updatable=true)
	private Binder binder;
	
	@ManyToOne(targetEntity=AssignmentImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_template_reference_id", nullable=true, insertable=true, updatable=false)
	private Assignment templateReference;
	
	@ManyToOne(targetEntity=PageImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_page_id", nullable=true, insertable=true, updatable=true)
	private Page page;

	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_assignee_id", nullable=true, insertable=true, updatable=true)
	private Identity assignee;
	
	@Column(name="p_only_auto_eva", nullable=true, insertable=true, updatable=true)
	private boolean onlyAutoEvaluation;
	@Column(name="p_reviewer_see_auto_eva", nullable=true, insertable=true, updatable=true)
	private boolean reviewerSeeAutoEvaluation;
	@Column(name="p_anon_extern_eva", nullable=true, insertable=true, updatable=true)
	private boolean anonymousExternalEvaluation;
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_form_entry_id", nullable=true, insertable=true, updatable=true)
	private RepositoryEntry formEntry;
	
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
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	@Override
	@Transient
	public AssignmentStatus getAssignmentStatus() {
		return StringHelper.containsNonWhitespace(status) ? AssignmentStatus.valueOf(status) : null;
	}

	@Override
	public void setAssignmentStatus(AssignmentStatus status) {
		if(status == null) {
			this.status = null;
		} else {
			this.status = status.name();
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public AssignmentType getAssignmentType() {
		return StringHelper.containsNonWhitespace(type) ? AssignmentType.valueOf(type) : null;
	}

	@Override
	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	@Override
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public String getStorage() {
		return storage;
	}

	public void setStorage(String storage) {
		this.storage = storage;
	}

	@Override
	public Section getSection() {
		return section;
	}

	public void setSection(Section section) {
		this.section = section;
	}

	public Binder getBinder() {
		return binder;
	}

	public void setBinder(Binder binder) {
		this.binder = binder;
	}

	@Override
	public Assignment getTemplateReference() {
		return templateReference;
	}

	public void setTemplateReference(Assignment templateReference) {
		this.templateReference = templateReference;
	}

	@Override
	public Page getPage() {
		return page;
	}

	public void setPage(Page page) {
		this.page = page;
	}

	@Override
	public Identity getAssignee() {
		return assignee;
	}

	public void setAssignee(Identity assignee) {
		this.assignee = assignee;
	}

	@Override
	public boolean isOnlyAutoEvaluation() {
		return onlyAutoEvaluation;
	}

	@Override
	public void setOnlyAutoEvaluation(boolean onlyAutoEvaluation) {
		this.onlyAutoEvaluation = onlyAutoEvaluation;
	}

	@Override
	public boolean isReviewerSeeAutoEvaluation() {
		return reviewerSeeAutoEvaluation;
	}

	@Override
	public void setReviewerSeeAutoEvaluation(boolean reviewerSeeAutoEvaluation) {
		this.reviewerSeeAutoEvaluation = reviewerSeeAutoEvaluation;
	}

	@Override
	public boolean isAnonymousExternalEvaluation() {
		return anonymousExternalEvaluation;
	}

	@Override
	public void setAnonymousExternalEvaluation(boolean anonymousExternalEvaluation) {
		this.anonymousExternalEvaluation = anonymousExternalEvaluation;
	}

	@Override
	public RepositoryEntry getFormEntry() {
		return formEntry;
	}

	public void setFormEntry(RepositoryEntry formEntry) {
		this.formEntry = formEntry;
	}

	@Override
	public int hashCode() {
		return key == null ? 3645362 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssignmentImpl) {
			AssignmentImpl assignment = (AssignmentImpl)obj;
			return key != null && key.equals(assignment.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
