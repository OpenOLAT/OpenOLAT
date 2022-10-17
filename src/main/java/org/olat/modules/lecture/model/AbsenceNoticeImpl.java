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
package org.olat.modules.lecture.model;

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

import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.AbsenceCategory;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.AbsenceNoticeTarget;
import org.olat.modules.lecture.AbsenceNoticeType;

/**
 * 
 * Initial date: 22 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="absencenotice")
@Table(name="o_lecture_absence_notice")
public class AbsenceNoticeImpl implements AbsenceNotice, Persistable {

	private static final long serialVersionUID = -719113703378664586L;
	
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
	
	@Column(name="l_type", nullable=false, insertable=true, updatable=true)
	private String type;
	@Column(name="l_absence_reason", nullable=true, insertable=true, updatable=true)
	private String absenceReason;
	@Column(name="l_absence_authorized", nullable=true, insertable=true, updatable=true)
	private Boolean absenceAuthorized;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_start_date", nullable=false, insertable=true, updatable=true)
	private Date startDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="l_end_date", nullable=false, insertable=true, updatable=true)
	private Date endDate;
	@Column(name="l_target", nullable=false, insertable=true, updatable=true)
	private String target;
	
	@Column(name="l_attachments_dir", nullable=true, insertable=true, updatable=true)
	private String attachmentsDirectory;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, insertable=true, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_notifier", nullable=true, insertable=true, updatable=true)
	private Identity notifier;
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_authorizer", nullable=true, insertable=true, updatable=true)
	private Identity authorizer;
	@ManyToOne(targetEntity=AbsenceCategoryImpl.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_absence_category", nullable=true, insertable=true, updatable=true)
	private AbsenceCategory absenceCategory;
	
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

	@Override
	public AbsenceNoticeType getNoticeType() {
		return StringHelper.containsNonWhitespace(type) ? AbsenceNoticeType.valueOf(type) : null;
	}

	@Override
	public void setNoticeType(AbsenceNoticeType type) {
		this.type = (type == null ? null : type.name());
	}

	public String getType() {
		return type;
	}

	public void setType(String typeString) {
		this.type = typeString;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	@Override
	public AbsenceNoticeTarget getNoticeTarget() {
		return StringHelper.containsNonWhitespace(target) ? AbsenceNoticeTarget.valueOf(target) : null;
	}

	@Override
	public void setNoticeTarget(AbsenceNoticeTarget target) {
		this.target =  (target == null ? null : target.name());
	}

	@Override
	public String getAbsenceReason() {
		return absenceReason;
	}

	@Override
	public void setAbsenceReason(String absenceReason) {
		this.absenceReason = absenceReason;
	}

	@Override
	public Boolean getAbsenceAuthorized() {
		return absenceAuthorized;
	}

	@Override
	public void setAbsenceAuthorized(Boolean absenceAuthorized) {
		this.absenceAuthorized = absenceAuthorized;
	}

	@Override
	public Date getStartDate() {
		return startDate;
	}

	@Override
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	@Override
	public Date getEndDate() {
		return endDate;
	}

	@Override
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public String getAttachmentsDirectory() {
		return attachmentsDirectory;
	}

	public void setAttachmentsDirectory(String attachmentsDirectory) {
		this.attachmentsDirectory = attachmentsDirectory;
	}

	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public Identity getNotifier() {
		return notifier;
	}

	public void setNotifier(Identity notifier) {
		this.notifier = notifier;
	}

	public Identity getAuthorizer() {
		return authorizer;
	}

	public void setAuthorizer(Identity authorizer) {
		this.authorizer = authorizer;
	}

	public AbsenceCategory getAbsenceCategory() {
		return absenceCategory;
	}

	public void setAbsenceCategory(AbsenceCategory absenceCategory) {
		this.absenceCategory = absenceCategory;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 264782 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof AbsenceNoticeImpl) {
			AbsenceNoticeImpl notice = (AbsenceNoticeImpl)obj;
			return getKey() != null && getKey().equals(notice.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
