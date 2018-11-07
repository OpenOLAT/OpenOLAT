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
package org.olat.repository.ui.catalog;

import java.util.Date;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryLight;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.PriceMethod;

/**
 * 
 * Initial date: 04.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CatalogEntryRow implements RepositoryEntryRef, RepositoryEntryLight {

	private Long key;
	private String name;
	private String authors;
	private String shortenedDescription;
	
	private boolean allUsers;
	private boolean guests;
	private boolean bookable;
	private RepositoryEntryStatusEnum status;

	private Date creationDate;
	
	private String externalId;
	private String externalRef;
	private boolean managed;
	private RepositoryEntryManagedFlag[] managedFlags;
	
	private String lifecycleLabel;
	private String lifecycleSoftKey;
	private Date lifecycleStart;
	private Date lifecycleEnd;
	
	private List<PriceMethod> accessTypes;

	private OLATResourceable olatResource;

	
	public CatalogEntryRow(RepositoryEntry view) {
		key = view.getKey();
		name = view.getDisplayname();
		authors = view.getAuthors();
		if(view.getDescription() != null) {
			String shortDesc = FilterFactory.getHtmlTagsFilter().filter(view.getDescription());
			if(shortDesc.length() > 255) {
				shortenedDescription = shortDesc.substring(0, 255);
			} else {
				shortenedDescription = shortDesc;
			}
		} else {
			shortenedDescription = "";
		}

		creationDate = view.getCreationDate();
		
		externalId = view.getExternalId();
		externalRef = view.getExternalRef();
		managed = view.getManagedFlags() != null && view.getManagedFlags().length > 0;
		managedFlags = view.getManagedFlags();
		
		status = view.getEntryStatus();
		allUsers = view.isAllUsers();
		guests = view.isGuests();
		
		olatResource = OresHelper.clone(view.getOlatResource());
		
		RepositoryEntryLifecycle lifecycle = view.getLifecycle();
		if(lifecycle != null) {
			lifecycleStart = lifecycle.getValidFrom();
			lifecycleEnd = lifecycle.getValidTo();
			if(!lifecycle.isPrivateCycle()) {
				lifecycleLabel = lifecycle.getLabel();
				lifecycleSoftKey = lifecycle.getSoftKey();
			}
		}
	}
	
	public String getCssClass() {
		return "o_CourseModule_icon";
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public Date getCreationDate() {
		return creationDate;
	}
	
	@Override
	public RepositoryEntryStatusEnum getEntryStatus() {
		return status;
	}

	@Override
	public boolean isAllUsers() {
		return allUsers;
	}

	@Override
	public boolean isGuests() {
		return guests;
	}
	
	@Override
	public boolean isBookable() {
		return bookable;
	}

	@Override
	public String getDisplayname() {
		return name;
	}

	@Override
	public String getDescription() {
		return getShortenedDescription();
	}
	
	public String getShortenedDescription() {
		return shortenedDescription;
	}
	
	public String getExternalId() {
		return externalId;
	}

	public String getExternalRef() {
		return externalRef;
	}
	
	public boolean isManaged() {
		return managed;
	}
	
	public RepositoryEntryManagedFlag[] getManagedFlags() {
		return managedFlags;
	}

	public String getLifecycleLabel() {
		return lifecycleLabel;
	}

	public String getLifecycleSoftKey() {
		return lifecycleSoftKey;
	}

	public Date getLifecycleStart() {
		return lifecycleStart;
	}

	public Date getLifecycleEnd() {
		return lifecycleEnd;
	}
	
	public List<PriceMethod> getAccessTypes() {
		return accessTypes;
	}

	public void setAccessTypes(List<PriceMethod> accessTypes) {
		this.accessTypes = accessTypes;
	}

	public OLATResourceable getRepositoryEntryResourceable() {
		return OresHelper.createOLATResourceableInstance("RepositoryEntry", getKey());
	}
	
	@Override
	public String getResourceType() {
		return olatResource.getResourceableTypeName();
	}

	/**
	 * This is a clone of the repositoryEntry.getOLATResource();
	 * @return
	 */
	public OLATResourceable getOLATResourceable() {
		return olatResource;
	}

	public String getAuthors() {
		return authors;
	}
}