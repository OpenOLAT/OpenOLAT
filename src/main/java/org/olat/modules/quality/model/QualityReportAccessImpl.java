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
package org.olat.modules.quality.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.model.GroupImpl;
import org.olat.core.id.Persistable;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualityReportAccess;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.model.QualityGeneratorImpl;

/**
 * 
 * Initial date: 29.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="qualityreportaccess")
@Table(name="o_qual_report_access")
public class QualityReportAccessImpl implements QualityReportAccess, Persistable {

	private static final long serialVersionUID = -8693827352805565462L;

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
	
	@Enumerated(EnumType.STRING)
	@Column(name="q_type", nullable=false, insertable=true, updatable=false)
	private QualityReportAccess.Type type;
	@Column(name="q_role", nullable=true, insertable=true, updatable=false)
	private String role;
	@Column(name="q_online", nullable=true, insertable=true, updatable=true)
	private boolean online;
	@Enumerated(EnumType.STRING)
	@Column(name="q_email_trigger", nullable=true, insertable=true, updatable=true)
	private QualityReportAccess.EmailTrigger emailTrigger;
	
	@ManyToOne(targetEntity=GroupImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_group", nullable=true, insertable=true, updatable=true)
	private Group group;
	
	@ManyToOne(targetEntity=QualityDataCollectionImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_data_collection", nullable=true, insertable=true, updatable=false)
	private QualityDataCollection dataCollection;
	@ManyToOne(targetEntity=QualityGeneratorImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_generator", nullable=true, insertable=true, updatable=false)
	private QualityGenerator generator;
	
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
	public QualityReportAccess.Type getType() {
		return type;
	}

	public void setType(QualityReportAccess.Type type) {
		this.type = type;
	}

	@Override
	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	@Override
	public boolean isOnline() {
		return online;
	}

	@Override
	public void setOnline(boolean online) {
		this.online = online;
	}

	@Override
	public QualityReportAccess.EmailTrigger getEmailTrigger() {
		return emailTrigger;
	}

	@Override
	public void setEmailTrigger(QualityReportAccess.EmailTrigger emailTrigger) {
		this.emailTrigger = emailTrigger;
	}

	@Override
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	public QualityDataCollection getDataCollection() {
		return dataCollection;
	}

	public void setDataCollection(QualityDataCollection dataCollection) {
		this.dataCollection = dataCollection;
	}

	public QualityGenerator getGenerator() {
		return generator;
	}
	
	public void setGenerator(QualityGenerator generator) {
		this.generator = generator;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		QualityReportAccessImpl other = (QualityReportAccessImpl) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		return true;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
