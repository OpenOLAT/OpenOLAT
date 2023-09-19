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
package org.olat.modules.cemedia.model;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 1 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchMediaParameters {
	
	private String searchString;
	private String checksum;
	private List<String> types;
	private List<Long> tags;
	private List<TaxonomyLevelRef> taxonomyLevelsRefs;
	
	private IdentityRef identity;
	private RepositoryEntryRef repositoryEntry;
	private Scope scope;
	private List<UsedIn> usedIn;
	private List<MediaToGroupRelationType> sharedWith;
	private Access access;
	
	public String getSearchString() {
		return searchString;
	}
	
	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	public String getChecksum() {
		return checksum;
	}

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	public IdentityRef getIdentity() {
		return identity;
	}

	public void setIdentity(IdentityRef identity) {
		this.identity = identity;
	}

	public RepositoryEntryRef getRepositoryEntry() {
		return repositoryEntry;
	}

	public void setRepositoryEntry(RepositoryEntryRef repositoryEntry) {
		this.repositoryEntry = repositoryEntry;
	}

	public Scope getScope() {
		return scope;
	}

	public void setScope(Scope scope) {
		this.scope = scope;
	}

	public List<UsedIn> getUsedIn() {
		return usedIn;
	}

	public void setUsedIn(List<UsedIn> usedIn) {
		this.usedIn = usedIn;
	}

	public List<MediaToGroupRelationType> getSharedWith() {
		return sharedWith;
	}

	public void setSharedWith(List<MediaToGroupRelationType> sharedWith) {
		this.sharedWith = sharedWith;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<Long> getTags() {
		return tags;
	}
	
	public void setTags(List<Long> tags) {
		this.tags = tags;
	}
	
	public List<TaxonomyLevelRef> getTaxonomyLevelsRefs() {
		return taxonomyLevelsRefs;
	}
	
	public void setTaxonomyLevelsRefs(List<TaxonomyLevelRef> taxonomyLevelsRefs) {
		this.taxonomyLevelsRefs = taxonomyLevelsRefs;
	}
	
	public enum Access {
		/**
		 * For administrator and learn resource managers, access media directly shared
		 */
		DIRECT,
		/**
		 * For administrator and learn resource managers, access media shared with groups
		 * courses and organizations these users manage
		 */
		INDIRECT,
	}
	
	public enum UsedIn {
		PAGE,
		PORTFOLIO,
		NOT_USED
	}
	
	public enum Scope {
		MY,
		SHARED_BY_ME,
		SHARED_WITH_ME,
		SHARED_WITH_ENTRY,
		ALL	
	}
}
