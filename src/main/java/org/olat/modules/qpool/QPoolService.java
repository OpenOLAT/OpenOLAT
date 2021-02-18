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
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
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
	
	/**
	 * @param key The primary key
	 * @return The question item or null if not found
	 */
	public QuestionItem loadItemById(Long key);
	
	/**
	 * @param identifier The identifier as defined in metadata
	 * @return The question items with the corresponding identifier
	 */
	public List<QuestionItem> loadItemByIdentifier(String identifier);
	
	/**
	 * 
	 * @param identifiers A list of identifiers as defined in metadata
	 * @return The question items with the corresponding identifiers
	 */
	public List<QuestionItemShort> loadItemsByIdentifier(List<String> identifiers);
	
	public List<QuestionItemFull> getAllItems(int firstResult, int maxResults);
	
	public QuestionItem updateItem(QuestionItem item);
	
	public void backupQuestion(QuestionItem item, Identity identity);
	
	public void deleteItems(List<? extends QuestionItemShort> items);
	
	public int countItems(SearchQuestionItemParams params);
	
	public ResultInfos<QuestionItemView> getItems(SearchQuestionItemParams params, int firstResult, int maxResults, SortKey... orderBy);
	
	public QuestionItemView getItem(Long key, Identity identity, Long restrictToPoolKey, Long restrictToGroupKey);
	
	/**
	 * Search the question items using the specified taxonomy level
	 * 
	 * @param level The taxonomy level
	 * @return A list of the question items
	 */
	public List<QuestionItemShort> getItems(TaxonomyLevelRef level);
	
	public List<Identity> getAuthors(QuestionItem item);
	
	public void addAuthors(List<Identity> authors, List<QuestionItemShort> items);
	
	public void removeAuthors(List<Identity> authors, List<QuestionItemShort> items);
	
	//import / export
	public MediaResource export(List<QuestionItemShort> items, ExportFormatOptions format, Locale locale);
	
	public void exportItem(QuestionItemShort item, ZipOutputStream zout, Locale locale, Set<String> names);
	
	public Set<ExportFormatOptions> getExportFormatOptions(List<QuestionItemShort> items, ExportFormatOptions.Outcome outcome);
	
	public List<QuestionItem> importItems(Identity owner, Locale defaultLocale, String filename, File file);

	public File getRootFile(QuestionItem item);
	
	public File getRootDirectory(QuestionItem item);
	
	public VFSLeaf getRootLeaf(QuestionItemShort item);
	
	public VFSContainer getRootContainer(QuestionItemShort item);
	
	public List<QuestionItem> copyItems(Identity cloner, List<QuestionItemShort> itemsToCopy);

	public List<QuestionItem> convertItems(Identity cloner, List<QuestionItemShort> itemsToConvert, String format,
			Locale locale);

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
	
	public List<QuestionItem2Resource> getSharedResourceInfosByItem(QuestionItem item);

	//list
	public QuestionItemCollection createCollection(Identity owner, String collectionName, List<QuestionItemShort> initialItems);
	
	public void addItemToCollection(List<? extends QuestionItemShort> items, List<QuestionItemCollection> collections);
	
	public void removeItemsFromCollection(List<QuestionItemShort> items, QuestionItemCollection collection);

	public QuestionItemCollection renameCollection(QuestionItemCollection coll, String name);
	
	public void deleteCollection(QuestionItemCollection coll);
	
	public List<QuestionItemCollection> getCollections(Identity owner);
	
	/**
	 * Send the message to index this list of items.
	 * @param items
	 */
	public void index(List<? extends QuestionItemShort> items);

	// review process
	public List<TaxonomyLevel> getTaxonomyLevel(Identity identity, TaxonomyCompetenceTypes... competenceType);
	
	public void rateItemInReview(QuestionItem item, Identity identity, Float rating, String comment);

	/**
	 * Reset the status of all questions in the question bank to the status "draft".
	 * @param reseter identity who reseted the states (for logging)
	 */
	public void resetAllStatesToDraft(Identity reseter);
	
	/**
	 * @return The taxonomy configured for the question pool or null
	 */
	public Taxonomy getQPoolTaxonomy();
	
	/**
	 * @return The list of taxonomy levels defined in the taxonomy
	 *			of the question pool.
	 */
	public List<TaxonomyLevel> getTaxonomyLevels();
	
	/**
	 * 
	 * @param parent
	 * @param displayName
	 * @return
	 */
	public List<TaxonomyLevel> getTaxonomyLevelBy(TaxonomyLevel parent, String displayName);
	
	public TaxonomyLevel createTaxonomyLevel(TaxonomyLevel parent, String identifier, String displayName);
	
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
	
	// Audit log
	public QuestionItemAuditLogBuilder createAuditLogBuilder(Identity author, Action action);
	
	public void persist(QuestionItemAuditLog auditLog);
	
	public String toAuditXml(QuestionItem item);
	
	public QuestionItem toAuditQuestionItem(String xml);

	public List<QuestionItemAuditLog> getAuditLogByQuestionItem(QuestionItemShort item);

}
