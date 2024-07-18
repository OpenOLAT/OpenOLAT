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
package org.olat.modules.topicbroker.model;

import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 29 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="topicbrokertopic")
@Table(name="o_tb_topic")
public class TBTopicImpl implements Persistable, TBTopic {
	
	private static final long serialVersionUID = 1586978565503910309L;

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
	
	@Column(name="t_identifier", nullable=true, insertable=true, updatable=true)
	private String identifier;
	@Column(name="t_title", nullable=true, insertable=true, updatable=true)
	private String title;
	@Column(name="t_description", nullable=true, insertable=true, updatable=true)
	private String description;
	@Column(name="t_min_participants", nullable=true, insertable=true, updatable=true)
	private Integer minParticipants;
	@Column(name="t_max_participants", nullable=true, insertable=true, updatable=true)
	private Integer maxParticipants;
	@Column(name="t_group_restrictions", nullable=true, insertable=true, updatable=true)
	private String groupRestrictions;
	private transient Set<Long> groupRestrictionKeys;
	@Column(name="t_sort_order", nullable=false, insertable=true, updatable=true)
	private int sortOrder;

	@Column(name="t_deleted_date", nullable=true, insertable=true, updatable=true)
	private Date deletedDate;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_deleted_by", nullable=false, insertable=true, updatable=true)
	private Identity deletedBy;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
	private Identity creator;
	
	@ManyToOne(targetEntity=TBBrokerImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_broker", nullable=false, insertable=true, updatable=false)
	private TBBroker broker;
	
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
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
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
	public Integer getMinParticipants() {
		return minParticipants;
	}

	@Override
	public void setMinParticipants(Integer minParticipants) {
		this.minParticipants = minParticipants;
	}

	@Override
	public Integer getMaxParticipants() {
		return maxParticipants;
	}

	@Override
	public void setMaxParticipants(Integer maxParticipants) {
		this.maxParticipants = maxParticipants;
	}

	@Override
	public Set<Long> getGroupRestrictionKeys() {
		if (groupRestrictionKeys == null && StringHelper.containsNonWhitespace(groupRestrictions)) {
			groupRestrictionKeys = Arrays.stream(groupRestrictions.split(",")).map(Long::valueOf).collect(Collectors.toSet());
		}
		return groupRestrictionKeys;
	}

	@Override
	public void setGroupRestrictionKeys(Set<Long> groupRestrictionKeys) {
		this.groupRestrictionKeys = groupRestrictionKeys;
		this.groupRestrictions = groupRestrictionKeys != null && !groupRestrictionKeys.isEmpty()
				? groupRestrictionKeys.stream().map(String::valueOf).collect(Collectors.joining(","))
				: null;
	}

	@Override
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public Date getDeletedDate() {
		return deletedDate;
	}

	public void setDeletedDate(Date deletedDate) {
		this.deletedDate = deletedDate;
	}

	@Override
	public Identity getDeletedBy() {
		return deletedBy;
	}

	public void setDeletedBy(Identity deletedBy) {
		this.deletedBy = deletedBy;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public TBBroker getBroker() {
		return broker;
	}

	public void setBroker(TBBroker broker) {
		this.broker = broker;
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
		TBTopicImpl other = (TBTopicImpl) obj;
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
