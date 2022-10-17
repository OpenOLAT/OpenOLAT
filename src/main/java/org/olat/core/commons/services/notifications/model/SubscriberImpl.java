/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.core.commons.services.notifications.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.Persistable;

/**
 * Description: <br>
 * Relation between the publisher and the identity
 * <P>
 * 
 * Initial Date: 21.10.2004 <br>
 * @author Felix Jost
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Entity(name="notisub")
@Table(name="o_noti_sub")
@NamedQuery(name="subscribersByPublisher", query="select sub from notisub sub where sub.publisher=:publisher")
@NamedQuery(name="subscribersByPublisherAndIdentity", query="select sub from notisub as sub where sub.publisher.key=:publisherKey and sub.identity.key=:identityKey")
public class SubscriberImpl implements Subscriber, CreateInfo, Persistable  {
	private static final long serialVersionUID = 6165097156137862263L;
	
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
	@Column(name="publisher_id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	//for compatibility purpose
	@Column(name="version", nullable=false, insertable=true, updatable=false)
	private int version = 0;
	
	// reference to the subscribed publisher
	@ManyToOne(targetEntity=PublisherImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_publisher", nullable=false, updatable=false)
	private Publisher publisher;
	
	// the user this subscription belongs to
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, updatable=false)
	private Identity identity;
	
	// when the user latest received an email concerning this subscription; may be null if no email has been sent yet
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="latestemailed", nullable=false, insertable=true, updatable=true)
	private Date latestEmailed; 
	
	@Column(name="subenabled", nullable=false, insertable=true, updatable=true)
	private boolean enabled; 

	/**
	 * for hibernate only
	 */
	public SubscriberImpl() {
		//
	}
	/**
	 * @param persistedPublisher
	 * @param listener
	 */
	public SubscriberImpl(Publisher persistedPublisher, Identity listener) {
		publisher = persistedPublisher;
		identity = listener;
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
	
	/**
	 * @return the identity
	 */
	@Override
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * @param identity
	 */
	@Override
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	@Override
	public Date getLatestEmailed() {
		return latestEmailed;
	}

	@Override
	public void setLatestEmailed(Date latestEmailed) {
		this.latestEmailed = latestEmailed;
	}

	/**
	 * @return the publisher
	 */
	@Override
	public Publisher getPublisher() {
		return publisher;
	}

	/**
	 * @param publisher
	 */
	@Override
	public void setPublisher(Publisher publisher) {
		this.publisher = publisher;
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
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 813184 : getKey().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof SubscriberImpl) {
			SubscriberImpl s = (SubscriberImpl)obj;
			return getKey() != null && getKey().equals(s.getKey());
		}
		return false;
	}
	
	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}