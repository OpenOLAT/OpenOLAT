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
import org.olat.modules.topicbroker.TBParticipant;
import org.olat.modules.topicbroker.TBSelection;
import org.olat.modules.topicbroker.TBTopic;

/**
 * 
 * Initial date: 4 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Entity(name="topicbrokerselection")
@Table(name="o_tb_selection")
public class TBSelectionImpl implements Persistable, TBSelection {
	
	private static final long serialVersionUID = -4779377112002909464L;

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
	
	@Column(name="t_enrolled", nullable=false, insertable=true, updatable=true)
	private boolean enrolled;
	@Column(name="t_sort_order", nullable=false, insertable=true, updatable=true)
	private int sortOrder;
	
	@ManyToOne(targetEntity=IdentityImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_creator", nullable=false, insertable=true, updatable=false)
	private Identity creator;
	@ManyToOne(targetEntity=TBParticipantImpl.class, fetch=FetchType.LAZY, optional=false)
	@JoinColumn(name="fk_participant", nullable=false, insertable=true, updatable=false)
	private TBParticipant participant;
	
	@ManyToOne(targetEntity=TBTopicImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_topic", nullable=false, insertable=true, updatable=false)
	private TBTopic topic;
	
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
	public boolean isEnrolled() {
		return enrolled;
	}

	public void setEnrolled(boolean enrolled) {
		this.enrolled = enrolled;
	}

	@Override
	public int getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	@Override
	public Identity getCreator() {
		return creator;
	}

	public void setCreator(Identity creator) {
		this.creator = creator;
	}

	@Override
	public TBParticipant getParticipant() {
		return participant;
	}

	public void setParticipant(TBParticipant participant) {
		this.participant = participant;
	}

	@Override
	public TBTopic getTopic() {
		return topic;
	}

	public void setTopic(TBTopic topic) {
		this.topic = topic;
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
		TBSelectionImpl other = (TBSelectionImpl) obj;
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
