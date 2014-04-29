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
package org.olat.repository.manager;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAuthorView;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.repository.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.SearchMyRepositoryEntryViewParams;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("repositoryService")
public class RepositoryServiceImpl implements RepositoryService {

	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDAO;
	@Autowired
	private RepositoryEntryRelationDAO reToGroupDao;
	@Autowired
	private RepositoryEntryStatisticsDAO repositoryEntryStatisticsDao;
	@Autowired
	private RepositoryEntryMyCourseViewQueries myCourseViewQueries;
	@Autowired
	private RepositoryEntryAuthorViewQueries authorViewQueries;
	@Autowired
	private OLATResourceManager resourceManager;

	@Autowired
	private LifeFullIndexer lifeIndexer;
	
	@Override
	public RepositoryEntry create(String initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource) {
		return create(initialAuthor, null, resourceName, displayname, description, resource);
	}

	@Override
	public RepositoryEntry create(Identity initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource) {
		return create(null, initialAuthor, resourceName, displayname, description, resource);
	}
	
	private RepositoryEntry create(String initialAuthorName, Identity initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource) { 
		Date now = new Date();
		
		RepositoryEntry re = new RepositoryEntry();
		if(StringHelper.containsNonWhitespace(initialAuthorName)) {
			re.setInitialAuthor(initialAuthorName);
		} else if(initialAuthor != null) {
			re.setInitialAuthor(initialAuthor.getName());
		}
		re.setCreationDate(now);
		re.setLastModified(now);
		re.setAccess(0);
		re.setCanDownload(false);
		re.setCanCopy(false);
		re.setCanReference(false);
		re.setDisplayname(displayname);
		re.setResourcename(resourceName == null ? "" : resourceName);
		re.setDescription(description == null ? "" : description);
		if(resource == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", CodeHelper.getForeverUniqueID());
			resource = resourceManager.createAndPersistOLATResourceInstance(ores);
		} else if(resource != null && resource.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(resource);
		}
		re.setOlatResource(resource);
		
		RepositoryEntryStatistics statistics = new RepositoryEntryStatistics();
		statistics.setLastUsage(now);
		statistics.setCreationDate(now);
		statistics.setLastModified(now);
		statistics.setDownloadCounter(0l);
		statistics.setLaunchCounter(0l);
		statistics.setNumOfRatings(0l);
		statistics.setNumOfComments(0l);
		dbInstance.getCurrentEntityManager().persist(statistics);
		
		re.setStatistics(statistics);
		
		Group group = groupDao.createGroup();
		RepositoryEntryToGroupRelation rel = new RepositoryEntryToGroupRelation();
		rel.setCreationDate(new Date());
		rel.setDefaultGroup(true);
		rel.setGroup(group);
		rel.setEntry(re);

		Set<RepositoryEntryToGroupRelation> rels = new HashSet<>(2);
		rels.add(rel);
		re.setGroups(rels);
		
		if(initialAuthor != null) {
			groupDao.addMembership(group, initialAuthor, GroupRoles.owner.name());
		}
		
		dbInstance.getCurrentEntityManager().persist(re);
		return re;	
	}

	@Override
	public RepositoryEntry update(RepositoryEntry re) {
		re.setLastModified(new Date());
		RepositoryEntry mergedRe = dbInstance.getCurrentEntityManager().merge(re);
		dbInstance.commit();
		lifeIndexer.indexDocument(RepositoryEntryDocument.TYPE, mergedRe.getKey());
		return mergedRe;
	}
	
	@Override
	public RepositoryEntry loadByKey(Long key) {
		return repositoryEntryDAO.loadByKey(key);
	}
	
	@Override
	public VFSLeaf getIntroductionImage(RepositoryEntry re) {
		VFSContainer repositoryHome = new LocalFolderImpl(new File(FolderConfig.getCanonicalRepositoryHome()));
		String imageName = re.getResourceableId() + ".jpg";
		VFSItem image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		imageName = re.getResourceableId() + ".png";
		image = repositoryHome.resolve(imageName);
		if(image instanceof VFSLeaf) {
			return (VFSLeaf)image;
		}
		return null;
	}

	@Override
	public VFSLeaf getIntroductionMovie(RepositoryEntry re) {
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
		VFSContainer mediaContainer = handler.getMediaContainer(re);
		if(mediaContainer != null) {
			List<VFSItem> items = mediaContainer.getItems();
			for(VFSItem item:items) {
				if(item instanceof VFSLeaf
						&& item.getName().startsWith(re.getKey().toString())
						&& (item.getName().endsWith(".mp4") || item.getName().endsWith(".m4v") || item.getName().endsWith(".flv")) ) {
					return (VFSLeaf)item;	
				}	
			}
		}
		return null;
	}

	@Override
	public void incrementLaunchCounter(RepositoryEntry re) {
		repositoryEntryStatisticsDao.incrementLaunchCounter(re);
	}

	@Override
	public void incrementDownloadCounter(RepositoryEntry re) {
		repositoryEntryStatisticsDao.incrementDownloadCounter(re);
	}

	@Override
	public void setLastUsageNowFor(RepositoryEntry re) {
		repositoryEntryStatisticsDao.setLastUsageNowFor(re);
	}

	@Override
	public Group getDefaultGroup(RepositoryEntryRef ref) {
		return reToGroupDao.getDefaultGroup(ref);
	}

	@Override
	public boolean hasRole(Identity identity, RepositoryEntryRef re, String... roles) {
		return reToGroupDao.hasRole(identity, re, roles);
	}

	@Override
	public boolean isMember(IdentityRef identity, RepositoryEntryRef entry) {
		return reToGroupDao.isMember(identity, entry);
	}

	@Override
	public void filterMembership(IdentityRef identity, List<Long> entries) {
		reToGroupDao.filterMembership(identity, entries);
	}

	@Override
	public int countMembers(RepositoryEntryRef re, String... roles) {
		return reToGroupDao.countMembers(re, roles);
	}

	@Override
	public List<Identity> getMembers(RepositoryEntryRef re, String... roles) {
		return reToGroupDao.getMembers(re, RepositoryEntryRelationType.defaultGroup, roles);
	}

	@Override
	public void addRole(Identity identity, RepositoryEntry re, String role) {
		reToGroupDao.addRole(identity, re, role);
	}

	@Override
	public void removeRole(Identity identity, RepositoryEntry re, String role) {
		reToGroupDao.removeRole(identity, re, role);
	}

	@Override
	public void removeMembers(RepositoryEntry re) {
		// TODO Auto-generated method stub
	}

	@Override
	public int countMyView(SearchMyRepositoryEntryViewParams params) {
		return myCourseViewQueries.countViews(params);
	}

	@Override
	public List<RepositoryEntryMyView> searchMyView(SearchMyRepositoryEntryViewParams params,
			int firstResult, int maxResults) {
		return myCourseViewQueries.searchViews(params, firstResult, maxResults);
	}

	@Override
	public int countAuthorView(SearchAuthorRepositoryEntryViewParams params) {
		return authorViewQueries.countViews(params);
	}

	@Override
	public List<RepositoryEntryAuthorView> searchAuthorView(SearchAuthorRepositoryEntryViewParams params,
			int firstResult, int maxResults) {
		return authorViewQueries.searchViews(params, firstResult, maxResults);
	}
}