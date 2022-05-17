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
package org.olat.course.nodes.practice.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.olat.core.id.Persistable;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.model.ItemCollectionImpl;
import org.olat.modules.qpool.model.PoolImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="practiceresource")
@Table(name="o_practice_resource")
public class PracticeResourceImpl implements Persistable, PracticeResource {
	
	private static final long serialVersionUID = 2761536606554819349L;

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
	
	@ManyToOne(targetEntity=RepositoryEntry.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_entry", nullable=false, insertable=true, updatable=false)
	private RepositoryEntry repositoryEntry;
	@Column(name="p_subident", nullable=false, insertable=true, updatable=false)
	private String subIdent;
	
	@ManyToOne(targetEntity=RepositoryEntry.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_test_entry", nullable=true, insertable=true, updatable=false)
	private RepositoryEntry testEntry;
	
	@ManyToOne(targetEntity=ItemCollectionImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_item_collection", nullable=true, insertable=true, updatable=false)
	private QuestionItemCollection itemCollection;
	
	@ManyToOne(targetEntity=PoolImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_pool", nullable=true, insertable=true, updatable=false)
	private Pool pool;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class, fetch=FetchType.LAZY, optional=true)
	@JoinColumn(name="fk_resource_share", nullable=true, insertable=true, updatable=false)
	private OLATResource resourceShare;
	
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
	@Transient
	public String getName() {
		String name = null;
		if(getTestEntry() != null) {
			name = getTestEntry().getDisplayname();
		} else if(getPool() != null) {
			name = getPool().getName();
		} else if(getItemCollection() != null) {
			name = getItemCollection().getName();
		} else if(getResourceShare() != null) {
			name = getResourceShare().getResourceableId().toString();
		}
		return name;
	}

	@Override
	public RepositoryEntry getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntry repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	@Override
	public String getSubIdent() {
		return subIdent;
	}

	public void setSubIdent(String subIdent) {
		this.subIdent = subIdent;
	}

	@Override
	public RepositoryEntry getTestEntry() {
		return testEntry;
	}

	public void setTestEntry(RepositoryEntry testEntry) {
		this.testEntry = testEntry;
	}

	@Override
	public QuestionItemCollection getItemCollection() {
		return itemCollection;
	}

	public void setItemCollection(QuestionItemCollection itemCollection) {
		this.itemCollection = itemCollection;
	}

	@Override
	public Pool getPool() {
		return pool;
	}

	public void setPool(Pool pool) {
		this.pool = pool;
	}
	
	@Override
	public OLATResource getResourceShare() {
		return resourceShare;
	}

	public void setResourceShare(OLATResource resourceShare) {
		this.resourceShare = resourceShare;
	}

	@Override
	public int hashCode() {
		return key == null ? -2596781 : key.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof PracticeResourceImpl) {
			PracticeResourceImpl rsrc = (PracticeResourceImpl)obj;
			return getKey() != null && getKey().equals(rsrc.getKey());
		}
		return false;
	}

	@Override
	public boolean equalsByPersistableKey(Persistable persistable) {
		return equals(persistable);
	}
}
