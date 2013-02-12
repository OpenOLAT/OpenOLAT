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
import org.olat.core.id.Identity;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolService")
public class QuestionPoolServiceImpl implements QuestionPoolService, ApplicationListener<ContextRefreshedEvent> {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private QuestionItemDAO questionItemDao;
	
	
	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		int numOfPools = poolDao.getNumOfPools();
		if(numOfPools == 0) {
			//create a pool if there isn't any
			poolDao.createPool("Catalog");
			dbInstance.intermediateCommit();
		}
		
		int numOfQuestions = questionItemDao.getNumOfQuestions();
		if(numOfQuestions < 3) {
			List<Pool> pools = poolDao.getPools();
			for(int i=0; i<200; i++) {
				QuestionItem item = questionItemDao.create("NGC " + i);
				poolDao.addItemToPool(item, pools.get(0));
				if(i % 20 == 0) {
					dbInstance.intermediateCommit();
				}
			}
		}
		dbInstance.intermediateCommit();
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
	public List<QuestionItem> getItemsOfPool(Pool pool, int firstResult, int maxResults) {
		return poolDao.getItemsOfPool(pool, firstResult, maxResults);
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
	public List<QuestionItem> getItems(Identity identity, int firstResult, int maxResults) {
		return new ArrayList<QuestionItem>();
	}
}
