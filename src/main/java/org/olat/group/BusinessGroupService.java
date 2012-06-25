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
package org.olat.group;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.group.area.BGArea;
import org.olat.group.context.BGContext;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGConfigFlags;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface BusinessGroupService {
	
	
	
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description, String type,
			int minParticipants, int maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			OLATResource resource);
	
	public BusinessGroup mergeBusinessGroup(BusinessGroup group);
	
	public void updateBusinessGroup(BusinessGroup group);
	
	public void deleteBusinessGroup(BusinessGroup group);
	
	public BusinessGroup setLastUsageFor(BusinessGroup group);
		
	public BusinessGroup loadBusinessGroup(BusinessGroup group);
	
	public BusinessGroup loadBusinessGroup(Long key);
	
	public List<BusinessGroup> loadBusinessGroups(Collection<Long> keys);
	
	public List<BusinessGroup> loadAllBusinessGroups();
	
	public BusinessGroup loadBusinessGroup(OLATResource resource);
	
	
	
	//search methods
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup);

	public List<BusinessGroup> findBusinessGroupsOwnedBy(String type, Identity identity, OLATResource resource);
	
	public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, OLATResource resource);
	
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(String type, Identity identity,OLATResource resource);
	
	public int countBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource);
	
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource, int firstResult, int maxResults);
	
	//check
	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> names, OLATResource resource); 
	

	//retrieve repository entries
	public List<RepositoryEntry> findRepositoryEntries(Collection<BusinessGroup> groups, int firstResult, int maxResults);
	
	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults);
	
	
	//found identities
	public int countContacts(Identity identity);
	
	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults);
	
	public int countMembersOf(BusinessGroup group, boolean owner, boolean attendee);
	
	public List<Identity> getMembersOf(BusinessGroup group, boolean owner, boolean attendee);
	
	public int countMembersOf(OLATResource resource, boolean owner, boolean attendee);
	
	public List<Identity> getMembersOf(OLATResource resource, boolean owner, boolean attendee);
	
	

	
	//security
	public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup);
	/**
	 * 
	 * @param identity
	 * @param groupName
	 * @param resource
	 * @return
	 */
	public boolean isIdentityInBusinessGroup(Identity identity, String groupName, String groupType, boolean ownedById, boolean attendedById, OLATResource resource);

	
	
	//memberships

	public void removeParticipantsAndFireEvent(Identity ureqIdentity, List<Identity> identities, BusinessGroup group, BGConfigFlags flags);

	public void removeOwnerAndFireEvent(Identity identity, Identity currentIdentity, BusinessGroup group, BGConfigFlags flags, boolean b);

	public List<String> getDependingDeletablableListFor(BusinessGroup currentGroup, Locale locale);

	public Set<BusinessGroup> createUniqueBusinessGroupsFor(Set<String> allNames, OLATResource resource, String bgDesc, Integer bgMin,
			Integer bgMax, Boolean enableWaitingList, Boolean enableAutoCloseRanks);


	
	
	//export - import
	public void exportGroups(List<BusinessGroup> groups, File fExportFile);

	public void importGroups(OLATResource resource, File fGroupExportXML);
	
	public void archiveGroups(List<BusinessGroup> groups, File exportFile);
	
	//TODO move to area service
	public File archiveAreaMembers(OLATResource resource, List<String> columnList, List<BGArea> areaList, String archiveType,
			Locale locale, String charset);

	public File archiveGroupMembers(OLATResource resource, List<String> columnList, List<BusinessGroup> groupList, String archiveType,
			Locale locale, String charset);

	

}
