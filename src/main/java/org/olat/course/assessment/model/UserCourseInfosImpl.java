/**
$ * <a href="http://www.openolat.org">
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
package org.olat.course.assessment.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.olat.basesecurity.IdentityImpl;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

@Entity(name="usercourseinfos")
@Table(name="o_as_user_course_infos")
@NamedQueries({
	@NamedQuery(name="updateLaunchDates", query="update usercourseinfos set visit=visit+1, recentLaunch=:now, lastModified=:now where identity.key=:identityKey and resource.key=:resourceKey")
})
public class UserCourseInfosImpl implements UserCourseInformations, Persistable, ModifiedInfo {

	private static final long serialVersionUID = -6933599547069673655L;
	
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
	@Column(name="id", nullable=false, unique=true, insertable=true, updatable=false)
	private Long key;

	//de facto removing the optimistic locking
	@Column(name="version", nullable=false, insertable=true, updatable=false)
	private Integer version = 0;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="creationdate", nullable=false, insertable=true, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="lastmodified", nullable=false, insertable=true, updatable=true)
	private Date lastModified;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="initiallaunchdate", nullable=false, insertable=true, updatable=true)
	private Date initialLaunch;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="recentlaunchdate", nullable=false, insertable=true, updatable=true)
	private Date recentLaunch;

	@Column(name="visit", nullable=false, insertable=true, updatable=true)
	private int visit;
	@Column(name="timespend", nullable=false, insertable=true, updatable=true)
	private long timeSpend;
	
	@ManyToOne(targetEntity=IdentityImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_identity", nullable=false, updatable=false)
	private Identity identity;
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_resource_id", nullable=false, updatable=false)
	private OLATResource resource;

	@Override
	public Long getKey() {
		return key;
	}
	
	public void setKey(Long key) {
		this.key = key;
	}

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
	public Date getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(Date initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	@Override
	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}

	@Override
	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}

	@Override
	public long getTimeSpend() {
		return timeSpend;
	}

	public void setTimeSpend(long timeSpend) {
		this.timeSpend = timeSpend;
	}

	@Override
	public Identity getIdentity() {
		return identity;
	}

	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public OLATResource getResource() {
		return resource;
	}

	public void setResource(OLATResource resource) {
		this.resource = resource;
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 9271 : getKey().hashCode();
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof UserCourseInfosImpl) {
			UserCourseInfosImpl prop = (UserCourseInfosImpl)obj;
			return getKey() != null && getKey().equals(prop.getKey());	
		}
		return false;
	}
}
