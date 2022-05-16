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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.CatalogEntry;
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
	private Long catEntryKey;
	private Integer position;
	private String name;
	private String authors;
	private String shortenedDescription;
	
	private boolean publicVisible;
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
	
	private FormLink positionLink;
	
	private List<PriceMethod> accessTypes;

	private OLATResourceable olatResource;

	
	public CatalogEntryRow(RepositoryEntry view) {
		key = view.getKey();
		name = view.getDisplayname();
		authors = view.getAuthors();
		shortenedDescription = StringHelper.truncateText(view.getDescription());
		creationDate = view.getCreationDate();
		
		externalId = view.getExternalId();
		externalRef = view.getExternalRef();
		managed = view.getManagedFlags() != null && view.getManagedFlags().length > 0;
		managedFlags = view.getManagedFlags();
		
		status = view.getEntryStatus();
		publicVisible = view.isPublicVisible();
		
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
	
	public CatalogEntryRow(RepositoryEntry view, CatalogEntry cat) {
		this(view);
		
		position = cat.getPosition() == null ? 0 : cat.getPosition();
		catEntryKey = cat.getKey();
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
	public boolean isPublicVisible() {
		return publicVisible;
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
	
	public Integer getPosition() {
		return position;
	}
	
	public Long getCatEntryKey() {
		return catEntryKey;
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
	
	public FormLink getPositionLink() {
		return positionLink;
	}
	
	public void setPositionLink(FormLink positionLink) {
		this.positionLink = positionLink;
	}
}