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

import java.io.File;
import java.util.List;

import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QuestionPoolService {
	
	public String getTaxonomicPath(QuestionItem item);
	
	public void deleteItems(List<QuestionItemShort> items);
	
	public List<Identity> getAuthors(QuestionItem item);
	
	public void addAuthors(List<Identity> authors, List<QuestionItemShort> items);
	
	public List<QuestionItem> importItems(Identity owner, String filename, File file);
	
	public VFSLeaf getRootFile(QuestionItem item);
	
	public VFSContainer getRootDirectory(QuestionItem item);
	
	/**
	 * Create a new item and persist it on the database
	 * @param owner
	 * @param subject
	 * @param format
	 * @param language
	 * @param taxonLevel
	 * @param dir
	 * @param rootFilename
	 * @param type
	 * @return
	 */
	public QuestionItem createAndPersistItem(Identity owner, String subject, String format, String language, TaxonomyLevel taxonLevel, String dir, String rootFilename, QuestionType type);
	
	public QuestionItem loadItemById(Long key);
	
	public QuestionItem updateItem(QuestionItem item);
	

	public int countItems(Identity author);

	public ResultInfos<QuestionItemShort> getItems(Identity author, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);

	public List<QuestionItem> getAllItems(int firstResult, int maxResults);
	
	
	
	//pools
	public List<Pool> getPools(Identity identity);
	
	public int getNumOfItemsInPool(Pool pool);
	
	public ResultInfos<QuestionItemShort> getItemsOfPool(Pool pool, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	public void addItemToPool(QuestionItemShort item, Pool pool);
	
	
	//favorit
	public int getNumOfFavoritItems(Identity identity);
	
	public ResultInfos<QuestionItemShort> getFavoritItems(Identity identity, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	
	
	//share
	public void shareItems(List<QuestionItemShort> items, List<BusinessGroup> groups);
	
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity);
	
	public int countSharedItemByResource(OLATResource resource);
	
	public ResultInfos<QuestionItemShort> getSharedItemByResource(OLATResource resource, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	//list
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItemShort> initialItems);
	
	public void addItemToCollection(QuestionItemShort item, QuestionItemCollection collection);
	
	public List<QuestionItemCollection> getCollections(Identity owner);
	
	public int countItemsOfCollection(QuestionItemCollection collection);
	
	public ResultInfos<QuestionItemShort> getItemsOfCollection(QuestionItemCollection collection, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);

	
	//study field admin
	public List<TaxonomyLevel> getStudyFields();
	
	
	//pool administration
	public void createPool(Identity identity, String name);
	
	public Pool updatePool(Pool pool);
	
	public void deletePool(Pool pool);
	
	public int countPools();

	public ResultInfos<Pool> getPools(int firstResult, int maxResults, SortKey... orderBy);
	
	
}
