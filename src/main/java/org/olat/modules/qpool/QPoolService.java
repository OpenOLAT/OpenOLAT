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
import java.util.Locale;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 22.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface QPoolService {
	
	
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
	public QuestionItem createAndPersistItem(Identity owner, String subject, String format, String language, TaxonomyLevel taxonLevel,
			String dir, String rootFilename, QItemType type);
	
	public QuestionItem loadItemById(Long key);
	
	public List<QuestionItemFull> getAllItems(int firstResult, int maxResults);
	
	public QuestionItem updateItem(QuestionItem item);
	
	public void deleteItems(List<QuestionItemShort> items);
	
	public int countItems(SearchQuestionItemParams params);
	
	public ResultInfos<QuestionItemView> getItems(SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	
	
	//manage authors
	public boolean isAuthor(QuestionItem item, Identity identity);
	
	public List<Identity> getAuthors(QuestionItem item);
	
	public void addAuthors(List<Identity> authors, List<QuestionItemShort> items);
	
	public void removeAuthors(List<Identity> authors, List<QuestionItemShort> items);
	
	//import / export
	public MediaResource export(List<QuestionItemShort> items, ExportFormatOptions format);
	
	/**
	 * 
	 * @param item
	 * @param zout
	 * @param names Collection of the names used in the ZIP dd
	 */
	public void exportItem(QuestionItemShort item, ZipOutputStream zout, Set<String> names);
	
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file);
	
	public VFSLeaf getRootFile(QuestionItem item);
	
	public VFSContainer getRootDirectory(QuestionItem item);
	
	public List<QuestionItem> copyItems(Identity cloner, List<QuestionItemShort> itemsToCopy);

	//pools
	public List<Pool> getPools(Identity identity, Roles roles);
	
	public boolean isMemberOfPrivatePools(IdentityRef identity);
	
	public boolean isOwner(Identity owner, Pool pool);
	
	public void addOwners(List<Identity> owners, List<Pool> pools);
	
	public void removeOwners(List<Identity> owners, List<Pool> pools);
	
	public void addItemsInPools(List<? extends QuestionItemShort> items, List<Pool> pools, boolean editable);
	
	public void removeItemsInPool(List<QuestionItemShort> items, Pool pool);
	
	public List<QuestionItem2Pool> getPoolInfosByItem(QuestionItemShort item);
	
	//share
	public void shareItemsWithGroups(List<? extends QuestionItemShort> items, List<BusinessGroup> groups, boolean editable);
	
	public void removeItemsFromResource(List<QuestionItemShort> items, OLATResource resource);
	
	public List<BusinessGroup> getResourcesWithSharedItems(Identity identity);
	
	public int countSharedItemByResource(OLATResource resource);
	
	public ResultInfos<QuestionItemView> getSharedItemByResource(OLATResource resource, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	public List<QuestionItem2Resource> getSharedResourceInfosByItem(QuestionItem item);
	
	//list
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItemShort> initialItems);
	
	public void addItemToCollection(List<? extends QuestionItemShort> items, List<QuestionItemCollection> collections);
	
	public void removeItemsFromCollection(List<QuestionItemShort> items, QuestionItemCollection collection);

	public QuestionItemCollection renameCollection(QuestionItemCollection coll, String name);
	
	public void deleteCollection(QuestionItemCollection coll);
	
	public List<QuestionItemCollection> getCollections(Identity owner);
	
	public int countItemsOfCollection(QuestionItemCollection collection);
	
	public ResultInfos<QuestionItemView> getItemsOfCollection(QuestionItemCollection collection, SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);

	
	//study field admin
	public List<TaxonomyLevel> getTaxonomyLevels();
	
	public TaxonomyLevel createTaxonomyLevel(TaxonomyLevel parent, String field);
	
	public TaxonomyLevel updateTaxonomyLevel(String newField, TaxonomyLevel level);
	
	public boolean delete(TaxonomyLevel level);
	
	
	//pool administration
	public void createPool(Identity identity, String name, boolean publicPool);
	
	public Pool updatePool(Pool pool);
	
	public void deletePool(Pool pool);
	
	public int countPools();

	public ResultInfos<Pool> getPools(int firstResult, int maxResults, SortKey... orderBy);
	
	//item types administration
	public QItemType createItemType(String type, boolean deletable);

	public List<QItemType> getAllItemTypes();
	
	public QItemType getItemType(String type);
	
	public boolean delete(QItemType itemType);
	
	//item levels administration
	public QEducationalContext createEducationalContext(String level);

	public List<QEducationalContext> getAllEducationlContexts();
	
	public QEducationalContext getEducationlContextByLevel(String level);
	
	public boolean deleteEducationalContext(QEducationalContext context);
	
	//licenses administration
	public QLicense createLicense(String licenseKey, String text);
	
	public List<QLicense> getAllLicenses();
	
	public QLicense getLicense(String licenseKey);
	
	public QLicense updateLicense(QLicense license);
	
	public boolean deleteLicense(QLicense license);
	
	
}
