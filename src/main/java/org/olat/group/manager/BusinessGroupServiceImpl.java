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
package org.olat.group.manager;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.SecurityGroup;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.delete.service.GroupDeletionManager;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.ui.BGConfigFlags;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupService")
public class BusinessGroupServiceImpl implements BusinessGroupService {
	

	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;
	@Autowired
	private BusinessGroupImportExport businessGroupImportExport;
	@Autowired
	private BusinessGroupArchiver businessGroupArchiver;
	
	@Override
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description, String type,
			int minParticipants, int maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			OLATResource resource) {
		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description, type,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled);
		
		if(resource instanceof OLATResourceImpl) {
			businessGroupDAO.addRelationToResource(group, resource);
		}
		return group;
	}
	
	
	
	@Override
	public Set<BusinessGroup> createUniqueBusinessGroupsFor(Set<String> allNames, OLATResource resource, String bgDesc, Integer bgMin,
			Integer bgMax, Boolean enableWaitingList, Boolean enableAutoCloseRanks) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	@Transactional
	public BusinessGroup mergeBusinessGroup(BusinessGroup group) {
		return businessGroupDAO.merge(group);
	}

	@Override
	@Transactional
	public void updateBusinessGroup(BusinessGroup group) {
		businessGroupDAO.update(group);
	}
	
	@Override
	@Transactional
	public BusinessGroup setLastUsageFor(BusinessGroup group) {
		BusinessGroup reloadedGroup = businessGroupDAO.load(group.getKey());
		reloadedGroup.setLastUsage(new Date());
		LifeCycleManager.createInstanceFor(reloadedGroup).deleteTimestampFor(GroupDeletionManager.SEND_DELETE_EMAIL_ACTION);
		updateBusinessGroup(reloadedGroup);
		return reloadedGroup;
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(BusinessGroup group) {
		return businessGroupDAO.load(group.getKey());
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(Long key) {
		return businessGroupDAO.load(key);
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(OLATResource resource) {
		return businessGroupDAO.load(resource.getResourceableId());
	}

	@Override
	@Transactional
	public List<BusinessGroup> loadBusinessGroups(Collection<Long> keys) {
		return businessGroupDAO.load(keys);
	}
	
	@Override
	@Transactional
	public List<BusinessGroup> loadAllBusinessGroups() {
		return businessGroupDAO.loadAll();
	}
	
	@Override
	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> names, OLATResource resource) {
		return businessGroupDAO.checkIfOneOrMoreNameExistsInContext(names, resource);
	}

	@Override
	@Transactional
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
		return businessGroupDAO.findBusinessGroup(secGroup);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsOwnedBy(String type, Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(type);
		return businessGroupDAO.findBusinessGroups(params, identity, true, false, resource, 0, -1);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(type);
		return businessGroupDAO.findBusinessGroups(params, identity, false, true, resource, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(String type, Identity identity,  OLATResource resource) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	@Transactional(readOnly=true)
	public int countBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroups(params, identity, ownedById, attendedById, resource);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource, int firstResult, int maxResults) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroups(params, identity, ownedById, attendedById, resource, firstResult, maxResults);
	}




	@Override
	@Transactional(readOnly=true)
	public int countContacts(Identity identity) {
		return businessGroupDAO.countContacts(identity);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		return businessGroupDAO.findContacts(identity, firstResult, maxResults);
	}
	
	

	@Override
	public void deleteBusinessGroup(BusinessGroup group) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int countMembersOf(BusinessGroup group, boolean owner, boolean attendee) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Identity> getMembersOf(BusinessGroup group, boolean owner, boolean attendee) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int countMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<Identity> getMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		// TODO Auto-generated method stub
		return null;
	}



	@Override
	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findResources(groups, firstResult, maxResults);
	}

	@Override
	public List<RepositoryEntry> findRepositoryEntries(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findRepositoryEntries(groups, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup) {
		SecurityGroup participants = businessGroup.getPartipiciantGroup();
		if (participants != null && securityManager.isIdentityInSecurityGroup(identity, participants)) {
			return true;
		}
		SecurityGroup owners = businessGroup.getOwnerGroup();
		if (owners != null && securityManager.isIdentityInSecurityGroup(identity, owners)) {
			return true;
		}
		return false;
	}

	@Override
	@Transactional(readOnly=true)
	public boolean isIdentityInBusinessGroup(Identity identity, String groupName, String groupType,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		return businessGroupDAO.isIdentityInBusinessGroup(identity, groupName, groupType, resource);
	}
	

	@Override
	public void exportGroups(List<BusinessGroup> groups, File fExportFile) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void importGroups(OLATResource resource, File fGroupExportXML) {
		businessGroupImportExport.importGroups(resource, fGroupExportXML);
	}

	@Override
	public void archiveGroups(List<BusinessGroup> groups, File exportFile) {
		// TODO Auto-generated method stub
		businessGroupArchiver.archiveBGContext(null, exportFile);
	}

	@Override
	public File archiveAreaMembers(OLATResource resource, List<String> columnList, List<BGArea> areaList, String archiveType, Locale locale, String charset) {
		return businessGroupArchiver.archiveAreaMembers(resource, columnList, areaList, archiveType, locale, charset);
	}

	@Override
	public File archiveGroupMembers(OLATResource resource, List<String> columnList, List<BusinessGroup> groupList, String archiveType, Locale locale, String charset) {
		return businessGroupArchiver.archiveGroupMembers(resource, columnList, groupList, archiveType, locale, charset);
	}



		@Override
	public List<String> getDependingDeletablableListFor(BusinessGroup currentGroup, Locale locale) {
		// TODO Auto-generated method stub
		return null;
	}

	
	//memberships management
	


	@Override
	public void removeParticipantsAndFireEvent(Identity ureqIdentity, List<Identity> identities, BusinessGroup group, BGConfigFlags flags) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeOwnerAndFireEvent(Identity identity, Identity currentIdentity, BusinessGroup group, BGConfigFlags flags, boolean b) {
		// TODO Auto-generated method stub
		
	}
	

}
