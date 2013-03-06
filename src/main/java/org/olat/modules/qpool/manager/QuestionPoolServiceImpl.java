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
package org.olat.modules.qpool.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionPoolSPI;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.PoolImpl;
import org.olat.modules.qpool.model.QuestionItemDocument;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.resource.OLATResource;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.search.service.searcher.SearchClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolService")
public class QuestionPoolServiceImpl implements QuestionPoolService {
	
	private static final OLog log = Tracing.createLoggerFor(QuestionPoolServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private CollectionDAO collectionDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private SearchClient searchClient;
	@Autowired
	private LifeFullIndexer lifeIndexer;
	

	@Override
	public String getTaxonomicPath(QuestionItem item) {
		QuestionItemImpl reloadedItem = questionItemDao.loadById(item.getKey());
		if(reloadedItem.getTaxonomyLevel() == null) {
			return "";
		}
		return reloadedItem.getTaxonomyLevel().getMaterializedPathNames();
	}

	@Override
	public void deleteItems(List<QuestionItem> items) {
		if(items == null || items.isEmpty()) {
			return; //nothing to do
		}
		
		poolDao.deleteFromPools(items);
		questionItemDao.deleteFromShares(items);
		collectionDao.deleteItemFromCollections(items);
		//TODO unmark
		questionItemDao.delete(items);
	}

	@Override
	public void addAuthors(List<Identity> authors, List<QuestionItem> items) {
		if(authors == null || authors.isEmpty() || items == null || items.isEmpty()) {
			return;//nothing to do
		}
		
		for(QuestionItem item:items) {
			questionItemDao.addAuthors(authors, item);
		}
	}
	
	@Override
	public List<Identity> getAuthors(QuestionItem item) {
		QuestionItemImpl itemImpl;
		if(item instanceof QuestionItemImpl) {
			itemImpl = (QuestionItemImpl)item;
		} else {
			itemImpl = questionItemDao.loadById(item.getKey());
		}
		return securityManager.getIdentitiesOfSecurityGroup(itemImpl.getOwnerGroup());
	}
	
	public QuestionItem getItem() {
		return null;
	}
	
	public QuestionItem updateItem(QuestionItem item) {
		QuestionItem mergedItem = questionItemDao.merge(item);
		dbInstance.commit();//
		lifeIndexer.indexDocument(QuestionItemDocument.TYPE, mergedItem.getKey());
		return mergedItem;
	}

	@Override
	public QuestionItem importItem(Identity owner, String filename, File file) {
		QuestionItem importedItem = null;
		List<QuestionPoolSPI> providers = qpoolModule.getQuestionPoolProviders();
		for(QuestionPoolSPI provider:providers) {
			if(provider.isCompatible(filename, file)) {
				importedItem = importItem(owner, filename, file, provider);
			}	
		}
		return importedItem;
	}
	
	@Override
	public VFSLeaf getRootFile(QuestionItem item) {
		QuestionItemImpl reloadedItem = questionItemDao.loadById(item.getKey());
		VFSContainer root = qpoolModule.getRootContainer();
		VFSItem dir = root.resolve(reloadedItem.getDirectory());
		if(dir instanceof VFSContainer) {
			VFSContainer itemContainer = (VFSContainer)dir;
			VFSItem rootLeaf = itemContainer.resolve(reloadedItem.getRootFilename());
			if(rootLeaf instanceof VFSLeaf) {
				return (VFSLeaf)rootLeaf;
			}
		}
		return null;
	}

	@Override
	public VFSContainer getRootDirectory(QuestionItem item) {
		QuestionItemImpl reloadedItem = questionItemDao.loadById(item.getKey());
		VFSContainer root = qpoolModule.getRootContainer();
		VFSItem dir = root.resolve(reloadedItem.getDirectory());
		if(dir instanceof VFSContainer) {
			return (VFSContainer)dir;
		}
		return null;
	}

	private QuestionItem importItem(Identity owner, String filename, File file, QuestionPoolSPI provider) {
		String uuid = UUID.randomUUID().toString();
		VFSContainer root = qpoolModule.getRootContainer();
		VFSContainer itemDir = getDirectory(root, uuid);

		String rootFilename = filename;
		if(filename.toLowerCase().endsWith(".zip")) {
			ZipUtil.unzipStrict(file, itemDir);
			rootFilename = searchRootFilename("", itemDir, provider);
		} else {
			//copy
			VFSLeaf leaf = itemDir.createChildLeaf(filename);
			OutputStream out = leaf.getOutputStream(false);
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				IOUtils.copy(in, out);
			} catch (FileNotFoundException e) {
				log.error("", e);
			} catch (IOException e) {
				log.error("", e);
			} finally {
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
		
		return questionItemDao.create(owner, filename, provider.getFormat(), "de", null, uuid, rootFilename, null);
	}
	
	private String searchRootFilename(String path, VFSContainer dir, QuestionPoolSPI provider) {
		for(VFSItem item:dir.getItems()) {
			if(item instanceof VFSContainer) {
				String root = searchRootFilename(path + "/" + item.getName(), (VFSContainer)item, provider);
				if(root != null) {
					return root;
				}
			} else if(item instanceof VFSLeaf) {
				if(provider.isCompatible(item.getName(), (VFSLeaf)item)) {
					return path + item.getName();
				}
			}
		}
		return null;
	}
	
	private VFSContainer getDirectory(VFSContainer rootContainer, String uuid) {
		String cleanUuid = uuid.replace("-", "");
		String firstToken = cleanUuid.substring(0, 2);
		VFSContainer firstContainer = getNextDirectory(rootContainer, firstToken);
		String secondToken = cleanUuid.substring(2, 4);
		VFSContainer secondContainer = getNextDirectory(firstContainer, secondToken);
		String thirdToken = cleanUuid.substring(4, 6);
		VFSContainer thridContainer = getNextDirectory(secondContainer, thirdToken);
		String forthToken = cleanUuid.substring(6, 8);
		return getNextDirectory(thridContainer, forthToken);
	}
	
	private VFSContainer getNextDirectory(VFSContainer container, String token) {
		VFSItem nextContainer = container.resolve(token);
		if(nextContainer instanceof VFSContainer) {
			return (VFSContainer)nextContainer;
		} else if (nextContainer instanceof VFSLeaf) {
			log.error("");
			return null;
		}
		return container.createChildContainer(token);
	}

	@Override
	public int countItems(Identity author) {
		return questionItemDao.countItems(author);
	}

	@Override
	public ResultInfos<QuestionItem> getItems(Identity author, SearchQuestionItemParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(QuestionItemDocument.OWNER_FIELD + ":" + author.getKey());
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, 10000, orderBy);

				int initialResultsSize = results.size();
				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItem>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItem> items = questionItemDao.getItems(author, results, firstResult, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItem>();
		} else {
			List<QuestionItem> items = questionItemDao.getItems(author, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public List<QuestionItem> getAllItems(int firstResult, int maxResults) {
		return questionItemDao.getAllItems(firstResult, maxResults);
	}

	@Override
	public List<Pool> getPools(Identity identity) {
		return poolDao.getPools(0, -1);
	}

	@Override
	public int getNumOfItemsInPool(Pool pool) {
		return poolDao.getNumOfItemsInPool(pool);
	}

	@Override
	public ResultInfos<QuestionItem> getItemsOfPool(Pool pool, SearchQuestionItemParams searchParams,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add("pool:" + pool.getKey());
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, 10000, orderBy);

				int initialResultsSize = results.size();
				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItem>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItem> items = poolDao.getItemsOfPool(pool, results, 0, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItem>();
		} else {
			List<QuestionItem> items = poolDao.getItemsOfPool(pool, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public void addItemToPool(QuestionItem item, Pool pool) {
		poolDao.addItemToPool(item, pool);
	}

	@Override
	public int getNumOfFavoritItems(Identity identity) {
		return questionItemDao.getNumOfFavoritItems(identity);
	}

	@Override
	public ResultInfos<QuestionItem> getFavoritItems(Identity identity, SearchQuestionItemParams searchParams,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				//filter with all favorits
				List<Long> favoritKeys = questionItemDao.getFavoritKeys(identity);

				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(getDbKeyConditionalQuery(favoritKeys));
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, maxResults * 5, orderBy);

				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItem>();
				}
				List<QuestionItem> items = questionItemDao.getFavoritItems(identity, results, firstResult, maxResults);
				return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), firstResult + results.size(), items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItem>();
		} else {
			List<QuestionItem> items = questionItemDao.getFavoritItems(identity, null, firstResult, maxResults);
			return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), -1, items);
		}
	}
	
	private String getDbKeyConditionalQuery(List<Long> keys) {
		StringBuilder sb = new StringBuilder();
		sb.append(AbstractOlatDocument.DB_ID_NAME).append(":(");
		for(Long key:keys) {
			if(sb.length() > 9) sb.append(" ");
			sb.append(key);
		}
		return sb.append(')').toString();
	}

	@Override
	public void shareItems(List<QuestionItem> items, List<BusinessGroup> groups) {
		if(items == null || items.isEmpty() || groups == null || groups.isEmpty()) {
			return;//nothing to do
		}
		
		List<OLATResource> resources = new ArrayList<OLATResource>(groups.size());
		for(BusinessGroup group:groups) {
			resources.add(group.getResource());
		}
		
		for(QuestionItem item:items) {
			questionItemDao.share(item, resources);
		}
	}

	@Override
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity) {
		return questionItemDao.getResourcesWithSharedItems(identity);
	}

	@Override
	public int countSharedItemByResource(OLATResource resource) {
		return questionItemDao.countSharedItemByResource(resource);
	}

	@Override
	public ResultInfos<QuestionItem> getSharedItemByResource(OLATResource resource, SearchQuestionItemParams searchParams,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(QuestionItemDocument.SHARE_FIELD + ":" + resource.getKey());
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, maxResults * 5, orderBy);

				int initialResultsSize = results.size();
				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItem>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItem> items = questionItemDao.getSharedItemByResource(resource, results, firstResult, maxResults);
				return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItem>();
		} else {
			List<QuestionItem> items = questionItemDao.getSharedItemByResource(resource, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItem> initialItems) {
		QuestionItemCollection coll = collectionDao.createCollection(collectionName, owner);
		for(QuestionItem item:initialItems) {
			collectionDao.addItemToCollection(item, coll);
		}
		return coll;
	}

	@Override
	public void addItemToCollection(QuestionItem item, QuestionItemCollection coll) {
		collectionDao.addItemToCollection(item, coll);
	}

	@Override
	public List<QuestionItemCollection> getCollections(Identity owner) {
		return collectionDao.getCollections(owner);
	}

	@Override
	public int countItemsOfCollection(QuestionItemCollection collection) {
		return collectionDao.countItemsOfCollection(collection);
	}

	@Override
	public ResultInfos<QuestionItem> getItemsOfCollection(QuestionItemCollection collection, SearchQuestionItemParams searchParams,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				List<Long> content = collectionDao.getItemKeysOfCollection(collection);

				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(getDbKeyConditionalQuery(content));
				List<Long> results = searchClient.doSearch(queryString, condQueries, searchParams.getIdentity(), searchParams.getRoles(),
						firstResult, maxResults * 5, orderBy);

				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItem>();
				}
				List<QuestionItem> items = collectionDao.getItemsOfCollection(collection, results, firstResult, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), firstResult + results.size(), items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItem>();
		} else {
			List<QuestionItem> items = collectionDao.getItemsOfCollection(collection, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItem>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public void createPool(Identity identity, String name) {
		PoolImpl pool = poolDao.createPool(name);
		securityManager.addIdentityToSecurityGroup(identity, pool.getOwnerGroup());
	}

	@Override
	public Pool updatePool(Pool pool) {
		return poolDao.updatePool(pool);
	}

	@Override
	public void deletePool(Pool pool) {
		poolDao.deletePool(pool);
	}

	@Override
	public int countPools() {
		return poolDao.countPools();
	}

	@Override
	public ResultInfos<Pool> getPools(int firstResult, int maxResults, SortKey... orderBy) {
		List<Pool> pools = poolDao.getPools(firstResult, maxResults);
		return new DefaultResultInfos<Pool>(firstResult + pools.size(), -1, pools);
	}

	@Override
	public List<TaxonomyLevel> getStudyFields() {
		return taxonomyLevelDao.loadAllLevels();
	}
	
	
	
	
}
