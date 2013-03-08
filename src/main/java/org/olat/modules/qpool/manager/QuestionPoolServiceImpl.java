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
import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DefaultResultInfos;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.TaxonomyLevel;
import org.olat.modules.qpool.model.PoolImpl;
import org.olat.modules.qpool.model.QItemDocument;
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
	public void deleteItems(List<QuestionItemShort> items) {
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
	public void addAuthors(List<Identity> authors, List<QuestionItemShort> items) {
		if(authors == null || authors.isEmpty() || items == null || items.isEmpty()) {
			return;//nothing to do
		}
		
		for(QuestionItemShort item:items) {
			questionItemDao.addAuthors(authors, item.getKey());
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
	
	@Override
	public QuestionItem loadItemById(Long key) {
		return questionItemDao.loadById(key);
	}

	public QuestionItem updateItem(QuestionItem item) {
		QuestionItem mergedItem = questionItemDao.merge(item);
		dbInstance.commit();//
		lifeIndexer.indexDocument(QItemDocument.TYPE, mergedItem.getKey());
		return mergedItem;
	}

	@Override
	public List<QuestionItem> importItems(Identity owner, String filename, File file) {
		List<QuestionItem> importedItem = null;
		List<QPoolSPI> providers = qpoolModule.getQuestionPoolProviders();
		for(QPoolSPI provider:providers) {
			if(provider.isCompatible(filename, file)) {
				importedItem = provider.importItems(owner, filename, file);
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

	@Override
	public QuestionItem createAndPersistItem(Identity owner, String subject, String format, String language,
			TaxonomyLevel taxonLevel, String dir, String rootFilename, QuestionType type) {
		return questionItemDao.createAndPersist(owner, subject, format, language, taxonLevel, dir, rootFilename, type);
	}

	/*private QuestionItem importItem(Identity owner, String filename, File file, QPoolSPI provider) {
		String uuid = UUID.randomUUID().toString();
		VFSContainer root = qpoolModule.getRootContainer();
		VFSContainer itemDir = FileStorage.getDirectory(root, uuid);

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
	
	private String searchRootFilename(String path, VFSContainer dir, QPoolSPI provider) {
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
	}*/
	


	@Override
	public int countItems(Identity author) {
		return questionItemDao.countItems(author);
	}

	@Override
	public ResultInfos<QuestionItemShort> getItems(Identity author, SearchQuestionItemParams searchParams, int firstResult, int maxResults, SortKey... orderBy) {
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(QItemDocument.OWNER_FIELD + ":" + author.getKey());
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, 10000, orderBy);

				int initialResultsSize = results.size();
				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItemShort>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItemShort> items = questionItemDao.getItems(author, results, firstResult, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItemShort>();
		} else {
			List<QuestionItemShort> items = questionItemDao.getItems(author, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), -1, items);
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
	public ResultInfos<QuestionItemShort> getItemsOfPool(Pool pool, SearchQuestionItemParams searchParams,
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
					return new DefaultResultInfos<QuestionItemShort>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItemShort> items = poolDao.getItemsOfPool(pool, results, 0, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItemShort>();
		} else {
			List<QuestionItemShort> items = poolDao.getItemsOfPool(pool, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public void addItemToPool(QuestionItemShort item, Pool pool) {
		poolDao.addItemToPool(item, pool);
	}

	@Override
	public int getNumOfFavoritItems(Identity identity) {
		return questionItemDao.getNumOfFavoritItems(identity);
	}

	@Override
	public ResultInfos<QuestionItemShort> getFavoritItems(Identity identity, SearchQuestionItemParams searchParams,
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
					return new DefaultResultInfos<QuestionItemShort>();
				}
				List<QuestionItemShort> items = questionItemDao.getFavoritItems(identity, results, firstResult, maxResults);
				return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), firstResult + results.size(), items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItemShort>();
		} else {
			List<QuestionItemShort> items = questionItemDao.getFavoritItems(identity, null, firstResult, maxResults);
			return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), -1, items);
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
	public void shareItems(List<QuestionItemShort> items, List<BusinessGroup> groups) {
		if(items == null || items.isEmpty() || groups == null || groups.isEmpty()) {
			return;//nothing to do
		}
		
		List<OLATResource> resources = new ArrayList<OLATResource>(groups.size());
		for(BusinessGroup group:groups) {
			resources.add(group.getResource());
		}
		
		for(QuestionItemShort item:items) {
			questionItemDao.share(item.getKey(), resources);
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
	public ResultInfos<QuestionItemShort> getSharedItemByResource(OLATResource resource, SearchQuestionItemParams searchParams,
			int firstResult, int maxResults, SortKey... orderBy) {
		
		if(searchParams != null && StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			try {
				String queryString = searchParams.getSearchString();
				List<String> condQueries = new ArrayList<String>();
				condQueries.add(QItemDocument.SHARE_FIELD + ":" + resource.getKey());
				List<Long> results = searchClient.doSearch(queryString, condQueries,
						searchParams.getIdentity(), searchParams.getRoles(), firstResult, maxResults * 5, orderBy);

				int initialResultsSize = results.size();
				if(results.isEmpty()) {
					return new DefaultResultInfos<QuestionItemShort>();
				} else if(results.size() > maxResults) {
					results = results.subList(0, Math.min(results.size(), maxResults * 2));
				}
				List<QuestionItemShort> items = questionItemDao.getSharedItemByResource(resource, results, firstResult, maxResults);
				return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), firstResult + initialResultsSize, items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItemShort>();
		} else {
			List<QuestionItemShort> items = questionItemDao.getSharedItemByResource(resource, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), -1, items);
		}
	}

	@Override
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItemShort> initialItems) {
		QuestionItemCollection coll = collectionDao.createCollection(collectionName, owner);
		for(QuestionItemShort item:initialItems) {
			collectionDao.addItemToCollection(item.getKey(), coll);
		}
		return coll;
	}

	@Override
	public void addItemToCollection(QuestionItemShort item, QuestionItemCollection coll) {
		collectionDao.addItemToCollection(item.getKey(), coll);
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
	public ResultInfos<QuestionItemShort> getItemsOfCollection(QuestionItemCollection collection, SearchQuestionItemParams searchParams,
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
					return new DefaultResultInfos<QuestionItemShort>();
				}
				List<QuestionItemShort> items = collectionDao.getItemsOfCollection(collection, results, firstResult, maxResults, orderBy);
				return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), firstResult + results.size(), items);
			} catch (Exception e) {
				log.error("", e);
			}
			return new DefaultResultInfos<QuestionItemShort>();
		} else {
			List<QuestionItemShort> items = collectionDao.getItemsOfCollection(collection, null, firstResult, maxResults, orderBy);
			return new DefaultResultInfos<QuestionItemShort>(firstResult + items.size(), -1, items);
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
