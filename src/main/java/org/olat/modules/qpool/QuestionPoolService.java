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
package org.olat.modules.qpool;

import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionPoolService {
	
	public String getMateriliazedPathOfStudyFields(QuestionItem item);
	
	public void deleteItems(List<QuestionItem> items);
	
	public void addAuthors(List<Identity> authors, List<QuestionItem> items);
	

	public int countItems(Identity author);

	public List<QuestionItem> getItems(Identity author, int firstResult, int maxResults, SortKey... orderBy);

	public List<QuestionItem> getAllItems(int firstResult, int maxResults);
	
	
	
	//pools
	public List<Pool> getPools(Identity identity);
	
	public int getNumOfItemsInPool(Pool pool);
	
	public List<QuestionItem> getItemsOfPool(Pool pool, int firstResult, int maxResults, SortKey... orderBy);
	
	public void addItemToPool(QuestionItem item, Pool pool);
	
	
	//favorit
	public int getNumOfFavoritItems(Identity identity);
	
	public List<QuestionItem> getFavoritItems(Identity identity, int firstResult, int maxResults, SortKey... orderBy);
	
	
	
	//share
	public void shareItems(List<QuestionItem> items, List<BusinessGroup> groups);
	
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity);
	
	public int countSharedItemByResource(OLATResource resource);
	
	public List<QuestionItem> getSharedItemByResource(OLATResource resource, int firstResult, int maxResults, SortKey... orderBy);
		
	
	//list
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItem> initialItems);
	
	public void addItemToCollection(QuestionItem item, QuestionItemCollection collection);
	
	public List<QuestionItemCollection> getCollections(Identity owner);
	
	public int countItemsOfCollection(QuestionItemCollection collection);
	
	public List<QuestionItem> getItemsOfCollection(QuestionItemCollection collection, int firstResult, int maxResults, SortKey... orderBy);

}
