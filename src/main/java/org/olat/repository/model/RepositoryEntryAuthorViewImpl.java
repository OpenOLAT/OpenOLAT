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
package org.olat.repository.model;

import java.util.Date;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.GenericGenerator;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;

/**
 * 
 * Initial date: 28.04.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Cacheable(false)
@Entity(name="repositoryentryauthor")
@Table(name="o_repositoryentry_author_v")
public class RepositoryEntryAuthorViewImpl implements RepositoryEntryAuthorView {
	
	@Id
	@GeneratedValue(generator = "system-uuid")
	@GenericGenerator(name = "system-uuid", strategy = "hilo")
	@Column(name="re_id", nullable=false, unique=true, insertable=false, updatable=false)
	private Long key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="re_creationdate", nullable=false, insertable=false, updatable=false)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="re_lastmodified", nullable=false, insertable=false, updatable=false)
	private Date lastModified;
	
	@Column(name="re_displayname", nullable=false, insertable=false, updatable=false)
	private String displayname;
	@Column(name="re_description", nullable=false, insertable=false, updatable=false)
	private String description;
	@Column(name="re_author", nullable=false, insertable=false, updatable=false)
	private String author;
	@Column(name="re_authors", nullable=false, insertable=false, updatable=false)
	private String authors;
	
	@Column(name="re_softkey", nullable=false, insertable=false, updatable=false)
	private String softkey;
	@Column(name="re_external_id", nullable=false, insertable=false, updatable=false)
	private String externalId;
	@Column(name="re_external_ref", nullable=false, insertable=false, updatable=false)
	private String externalRef;
	
	@Column(name="re_membersonly", nullable=false, insertable=false, updatable=false)
	private boolean membersOnly;
	@Column(name="re_accesscode", nullable=false, insertable=false, updatable=false)
	private int access;
	@Column(name="re_statuscode", nullable=false, insertable=false, updatable=false)
	private int statusCode;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="re_lastusage", nullable=false, insertable=false, updatable=false)
	private Date lastUsage;
	
	@ManyToOne(targetEntity=OLATResourceImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_olatresource", nullable=false, insertable=false, updatable=false)
	private OLATResource olatResource;

	@ManyToOne(targetEntity=RepositoryEntryLifecycle.class,fetch=FetchType.LAZY,optional=true)
	@JoinColumn(name="fk_lifecycle", nullable=true, insertable=false, updatable=false)
	private RepositoryEntryLifecycle lifecycle;
	
	@Column(name="mark_id", nullable=true, insertable=false, updatable=false)
	private Long markKey;
	
	@Column(name="num_of_valid_offers", nullable=true, insertable=false, updatable=false)
	private long offersAvailable;
	@Column(name="num_of_offers", nullable=true, insertable=false, updatable=false)
	private long offers;
	
	@Column(name="member_id", nullable=true, insertable=false, updatable=false)
	private Long identityKey;

	@Override
	public Long getKey() {
		return key;
	}
	
	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(RepositoryEntry.class);
	}

	@Override
	public String getResourceType() {
		return olatResource.getResourceableTypeName();
	}

	@Override
	public int getStatusCode() {
		return statusCode;
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getDisplayname() {
		return displayname;
	}

	@Override
	public String getDescription() {
		return description;
	}

	public String getSoftkey() {
		return softkey;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}

	@Override
	public String getExternalRef() {
		return externalRef;
	}

	@Override
	public String getAuthor() {
		return author;
	}

	@Override
	public String getAuthors() {
		return authors;
	}

	@Override
	public boolean isMembersOnly() {
		return membersOnly;
	}

	@Override
	public int getAccess() {
		return access;
	}

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		return lifecycle;
	}

	@Override
	public boolean isMarked() {
		return markKey != null;
	}

	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	@Override
	public boolean isValidOfferAvailable() {
		return offersAvailable > 0;
	}

	@Override
	public boolean isOfferAvailable() {
		return offers > 0;
	}
	
	

}
