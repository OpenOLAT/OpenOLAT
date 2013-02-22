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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.resource.OLATResource;
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
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private StudyFieldDAO studyFieldDao;
	@Autowired
	private QuestionItemDAO questionItemDao;

	@Override
	public String getMateriliazedPathOfStudyFields(QuestionItem item) {
		QuestionItemImpl reloadedItem = (QuestionItemImpl)questionItemDao.loadById(item.getKey());
		return studyFieldDao.getMaterializedPath(reloadedItem.getStudyField());
	}

	@Override
	public void deleteItems(List<QuestionItem> items) {
		if(items == null || items.isEmpty()) {
			return; //nothing to do
		}
		
		poolDao.deleteFromPools(items);
		questionItemDao.deleteFromShares(items);
		//TODO unmark
		questionItemDao.delete(items);
	}

	@Override
	public List<Pool> getPools(Identity identity) {
		return poolDao.getPools();
	}

	@Override
	public int getNumOfItemsInPool(Pool pool) {
		return poolDao.getNumOfItemsInPool(pool);
	}

	@Override
	public List<QuestionItem> getItemsOfPool(Pool pool, int firstResult, int maxResults, SortKey... orderBy) {
		return poolDao.getItemsOfPool(pool, firstResult, maxResults, orderBy);
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
	public List<QuestionItem> getFavoritItems(Identity identity, int firstResult, int maxResults) {
		return questionItemDao.getFavoritItems(identity, firstResult, maxResults);
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
	public List<QuestionItem> getSharedItemByResource(OLATResource resource,
			int firstResult, int maxResults, SortKey... orderBy) {
		return questionItemDao.getSharedItemByResource(resource, firstResult, maxResults, orderBy);
	}

	@Override
	public List<QuestionItem> getItems(Identity identity, int firstResult, int maxResults) {
		return new ArrayList<QuestionItem>();
	}
}
