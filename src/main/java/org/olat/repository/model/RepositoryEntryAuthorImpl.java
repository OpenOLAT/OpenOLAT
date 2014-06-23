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

import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 04.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryAuthorImpl implements RepositoryEntryAuthorView {

	private Long key;
	
	private Date creationDate;
	
	private String displayname;
	private String description;
	private String author;
	private String authors;
	
	private String softkey;
	private String externalId;
	private String externalRef;
	private RepositoryEntryManagedFlag[] managedFlags;
	
	private boolean membersOnly;
	private int access;
	private int statusCode;
	
	private Date lastUsage;
	
	private OLATResource olatResource;
	private RepositoryEntryLifecycle lifecycle;
	
	private boolean marked;
	
	private long offers;
	
	public RepositoryEntryAuthorImpl(RepositoryEntry re, boolean marked, long offers) {
		key = re.getKey();
		creationDate = re.getCreationDate();
		
		displayname = re.getDisplayname();
		description = re.getDescription();
		author = re.getInitialAuthor();
		authors = re.getAuthors();
		
		softkey = re.getSoftkey();
		externalId = re.getExternalId();
		externalRef = re.getExternalRef();
		managedFlags = re.getManagedFlags();
		
		membersOnly = re.isMembersOnly();
		access = re.getAccess();
		statusCode = re.getStatusCode();
		
		lastUsage = re.getStatistics().getLastUsage();
		
		olatResource = re.getOlatResource();
		lifecycle = re.getLifecycle();
		this.marked = marked;
		this.offers = offers;
	}

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
	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return managedFlags;
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
		return marked;
	}

	@Override
	public Date getLastUsage() {
		return lastUsage;
	}

	@Override
	public boolean isOfferAvailable() {
		return offers > 0;
	}
}