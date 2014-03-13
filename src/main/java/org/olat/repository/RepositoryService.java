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
package org.olat.repository;

import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.id.Identity;
import org.olat.resource.OLATResource;



/**
 * To replace the repository manager
 * 
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryService {
	
	
	public RepositoryEntry create(Identity initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource);
	
	public RepositoryEntry create(String initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource);
	
	public RepositoryEntry loadByKey(Long key);
	
	public RepositoryEntry update(RepositoryEntry re);
	

	public void incrementLaunchCounter(RepositoryEntry re);
	
	public void incrementDownloadCounter(RepositoryEntry re);
	
	public void setLastUsageNowFor(RepositoryEntry re);

	public Group getDefaultGroup(RepositoryEntryRef ref);
	
	public int countMembers(RepositoryEntryRef re, String... roles);
	
	public List<Identity> getMembers(RepositoryEntryRef re, String... roles);
	
	public boolean hasRole(Identity identity, RepositoryEntryRef re, String... roles);
	
	public void addRole(Identity identity, RepositoryEntry re, String role);
	
	public void removeRole(Identity identity, RepositoryEntry re, String role);
	
	public void removeMembers(RepositoryEntry re);
	
	public int countMyView(SearchMyRepositoryEntryViewParams params);
	
	/**
	 * The identity is mandatory for the search.
	 * @param params
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<RepositoryEntryMyView> searchMyView(SearchMyRepositoryEntryViewParams params, int firstResult, int maxResults);
	

}
