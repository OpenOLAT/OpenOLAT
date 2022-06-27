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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.repository.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntryStatusEnum;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchMyRepositoryEntryViewParams {
	private Identity identity;
	private Roles roles;
	
	private Boolean marked;
	private boolean membershipMandatory = false;
	private RepositoryEntryStatusEnum[] entryStatus;
	
	private OrderBy orderBy;
	private boolean asc;
	private List<Filter> filters;
	private CatalogEntry parentEntry;
	private List<String> resourceTypes;
	private List<? extends OrganisationRef> offerOrganisations;
	private Date offerValidAt;
	private Collection<Long> educationalTypeKeys;
	private List<? extends CurriculumRef> curriculums;

	private String idAndRefs;
	private String idRefsAndTitle;
	private String author;
	private String text;
	
	public SearchMyRepositoryEntryViewParams(Identity identity, Roles roles, String... resourceTypes) {
		this.identity = identity;
		this.roles = roles;
		addResourceTypes(resourceTypes);
	}
	
	public CatalogEntry getParentEntry() {
		return parentEntry;
	}

	public void setParentEntry(CatalogEntry parentEntry) {
		this.parentEntry = parentEntry;
	}

	public boolean isMembershipMandatory() {
		return membershipMandatory;
	}

	public void setMembershipMandatory(boolean membershipMandatory) {
		this.membershipMandatory = membershipMandatory;
	}
	
	public List<? extends OrganisationRef> getOfferOrganisations() {
		return offerOrganisations;
	}

	public void setOfferOrganisations(List<? extends OrganisationRef> offerOrganisations) {
		this.offerOrganisations = offerOrganisations;
	}

	public Date getOfferValidAt() {
		return offerValidAt;
	}

	public void setOfferValidAt(Date offerValidAt) {
		this.offerValidAt = offerValidAt;
	}

	public List<? extends CurriculumRef> getCurriculums() {
		return curriculums;
	}

	public void setCurriculums(List<? extends CurriculumRef> curriculums) {
		this.curriculums = curriculums;
	}

	public String getIdAndRefs() {
		return idAndRefs;
	}

	public void setIdAndRefs(String idAndRefs) {
		this.idAndRefs = idAndRefs;
	}

	public String getIdRefsAndTitle() {
		return idRefsAndTitle;
	}

	public void setIdRefsAndTitle(String idRefsAndTitle) {
		this.idRefsAndTitle = idRefsAndTitle;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public OrderBy getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(OrderBy orderBy) {
		this.orderBy = orderBy;
	}

	public boolean isOrderByAsc() {
		return asc;
	}

	public void setOrderByAsc(boolean asc) {
		this.asc = asc;
	}

	public List<Filter> getFilters() {
		return filters;
	}

	public void setFilters(List<Filter> filters) {
		this.filters = filters;
	}
	
	public void addFilter(Filter filter) {
		if(filters == null) {
			filters = new ArrayList<>(4);
		}
		if(!filters.contains(filter)) {
			filters.add(filter);
		}
	}
	
	public boolean isPassedFiltered() {
		return filters != null && (filters.contains(Filter.notPassed)
				|| filters.contains(Filter.passed)
				|| filters.contains(Filter.withoutPassedInfos));
	}
	
	public boolean isLifecycleFilterDefined() {
		return filters != null && (filters.contains(Filter.upcomingCourses)
				|| filters.contains(Filter.currentCourses)
				|| filters.contains(Filter.oldCourses));
	}
	
	public boolean isResourceTypesDefined() {
		return resourceTypes != null && !resourceTypes.isEmpty();
	}

	public List<String> getResourceTypes() {
		return resourceTypes;
	}
	
	public void setResourceTypes(List<String> resourceTypes) {
		this.resourceTypes = resourceTypes;
	}
	
	public void addResourceTypes(String... types) {
		if(this.resourceTypes == null) {
			this.resourceTypes = new ArrayList<>();
		}
		if(types != null) {
			for(String resourceType:types) {
				this.resourceTypes.add(resourceType);
			}
		}
	}
	
	public boolean isEducationalTypeDefined() {
		return educationalTypeKeys != null && !educationalTypeKeys.isEmpty();
	}
	
	public Collection<Long> getEducationalTypeKeys() {
		return educationalTypeKeys;
	}

	public void setEducationalTypeKeys(Collection<Long> educationalTypeKeys) {
		this.educationalTypeKeys = educationalTypeKeys;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public Roles getRoles() {
		return roles;
	}
	
	public RepositoryEntryStatusEnum[] getEntryStatus() {
		return entryStatus;
	}

	public void setEntryStatus(RepositoryEntryStatusEnum[] entryStatus) {
		this.entryStatus = entryStatus;
	}

	public Boolean getMarked() {
		return marked;
	}

	public void setMarked(Boolean marked) {
		this.marked = marked;
	}
	
	public enum OrderBy {
		automatic,
		favorit,
		lastVisited,
		passed,
		score,
		completion,
		title,
		lifecycle,
		author,
		location,
		creationDate,
		lastModified,
		rating,
		launchCounter,
		key,
		displayname,
		externalRef,
		externalId,
		lifecycleLabel,
		lifecycleSoftkey,
		lifecycleStart,
		lifecycleEnd,
		type,
		custom
	}
	
	public enum Filter {
		showAll,
		onlyCourses,
		currentCourses,
		oldCourses,
		upcomingCourses,
		asParticipant,
		asCoach,
		asAuthor,
		notBooked,
		passed,
		notPassed,
		withoutPassedInfos
	}
}
