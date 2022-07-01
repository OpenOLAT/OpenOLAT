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

import static org.olat.test.JunitTestHelper.random;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItem2Resource;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.QuestionType;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.model.SearchQuestionItemParams;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.resource.OLATResource;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionDAOTest extends OlatTestCase {

	@Autowired
	private DB dbInstance;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private QuestionItemDAO questionDao;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private QItemTypeDAO qItemTypeDao;
	@Autowired
	private QItemQueriesDAO qItemQueriesDao;
	@Autowired
	private QEducationalContextDAO qEduContextDao;
	
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	
	@Test
	public void createQuestion() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QuestionItem item = questionDao.createAndPersist(null, "Stars", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getKey());
		Assert.assertNotNull(item.getIdentifier());
		Assert.assertNotNull(item.getCreationDate());
		Assert.assertNotNull(item.getLastModified());
		Assert.assertNotNull(item.getType());
		Assert.assertNotNull(item.getQuestionStatus());
		Assert.assertNotNull(item.getQuestionStatusLastModified());
		Assert.assertEquals("Stars", item.getTitle());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void createQuestion_withOwner() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-1-" + UUID.randomUUID().toString());
		QuestionItem item = questionDao.createAndPersist(id, "My fav. stars", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		Assert.assertNotNull(item);
		Assert.assertNotNull(item.getKey());
		Assert.assertNotNull(item.getCreationDate());
		Assert.assertNotNull(item.getLastModified());
		Assert.assertNotNull(item.getType());
		Assert.assertNotNull(item.getQuestionStatus());
		Assert.assertEquals("My fav. stars", item.getTitle());
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void copyQuestion() {
		// create an item and fill it
		Taxonomy taxonomy = taxonomyDao.createTaxonomy("ID-QP", "QPool taxonomy", null, null);
		TaxonomyLevel taxonomyLevel = taxonomyLevelDao.createTaxonomyLevel("QP-L-1", random(), "QLevel 1", "For testing only", null, null, null, null, taxonomy);
		QEducationalContext eduContext = qEduContextDao.create("primary.school", true);
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QItemType essayType = qItemTypeDao.loadByType(QuestionType.ESSAY.name());
		
		Identity author = JunitTestHelper.createAndPersistIdentityAsUser("QClone-1-" + UUID.randomUUID().toString());
		Identity cloner = JunitTestHelper.createAndPersistIdentityAsUser("QClone-2-" + UUID.randomUUID().toString());
		QuestionItemImpl original = questionDao.createAndPersist(author, "To copy", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), taxonomyLevel, null, "root.xml", fibType);
		dbInstance.commit();
		Assert.assertNotNull(original);
		Assert.assertNotNull(original.getIdentifier());
		Assert.assertNull(original.getMasterIdentifier());
		
		//general
		original.setTitle("Original");
		original.setTopic("Topic");
		original.setDescription("Original description");
		original.setKeywords("original copy to");
		original.setCoverage("New coverage");
		original.setAdditionalInformations("Additional informations before copy");
		original.setLanguage("en");
		//educational
		original.setEducationalContext(eduContext);
		original.setEducationalLearningTime("PT1H30M");
		//question
		original.setType(essayType);
		original.setDifficulty(new BigDecimal("0.1"));
		original.setStdevDifficulty(new BigDecimal("0.2"));
		original.setDifferentiation(new BigDecimal("-0.5"));
		original.setNumOfAnswerAlternatives(4);
		original.setUsage(5);
		original.setAssessmentType("formative");
		original.setCorrectionTime(3);
		//lifecycle
		original.setItemVersion("1.0");
		original.setStatus(QuestionStatus.review.name());
		//technical
		original.setEditor("OpenOLAT");
		original.setEditorVersion("9.0a");
		
		original = questionDao.merge(original);
		dbInstance.commitAndCloseSession();
		
		//clone it
		QuestionItemImpl clone = questionDao.copy(original);
		questionDao.persist(cloner, clone);
		//compare
		Assert.assertEquals(1, questionDao.countItems(cloner));
		//general
		Assert.assertNotNull(clone.getIdentifier());
		Assert.assertFalse(clone.getIdentifier().equals(original.getIdentifier()));
		Assert.assertEquals(original.getIdentifier(), clone.getMasterIdentifier());
		Assert.assertNotNull(clone.getTitle());
		Assert.assertEquals("(Copy) " + original.getTitle(), clone.getTitle());
		Assert.assertEquals(original.getTopic(), clone.getTopic());
		Assert.assertEquals(original.getDescription(), clone.getDescription());
		Assert.assertEquals(original.getKeywords(), clone.getKeywords());
		Assert.assertEquals(original.getCoverage(), clone.getCoverage());
		Assert.assertEquals(original.getAdditionalInformations(), clone.getAdditionalInformations());
		Assert.assertEquals(original.getLanguage(), clone.getLanguage());
		//classification
		Assert.assertEquals(original.getTaxonomyLevel(), clone.getTaxonomyLevel());
		//educational
		Assert.assertEquals(original.getEducationalContext(), clone.getEducationalContext());
		Assert.assertEquals(original.getEducationalLearningTime(), clone.getEducationalLearningTime());
		//question
		Assert.assertEquals(original.getType(), clone.getType());
		Assert.assertNotNull(clone.getDifficulty());
		Assert.assertEquals(original.getDifficulty().doubleValue(), clone.getDifficulty().doubleValue(), 0.000001);
		Assert.assertNotNull(clone.getStdevDifficulty());
		Assert.assertEquals(original.getStdevDifficulty().doubleValue(), clone.getStdevDifficulty().doubleValue(), 0.000001);
		Assert.assertNotNull(clone.getDifferentiation());
		Assert.assertEquals(original.getDifferentiation().doubleValue(), clone.getDifferentiation().doubleValue(), 0.000001);
		Assert.assertEquals(original.getNumOfAnswerAlternatives(), clone.getNumOfAnswerAlternatives());
		Assert.assertEquals(0, clone.getUsage());
		Assert.assertEquals(original.getAssessmentType(), clone.getAssessmentType());
		Assert.assertEquals(Integer.valueOf(3), clone.getCorrectionTime());
		//lifecycle
		Assert.assertEquals(QuestionStatus.draft.name(), clone.getStatus());
		Assert.assertNotNull(clone.getQuestionStatusLastModified());
		Assert.assertEquals(original.getItemVersion(), clone.getItemVersion());
		//technical
		Assert.assertEquals(original.getEditor(), clone.getEditor());
		Assert.assertEquals(original.getEditorVersion(), clone.getEditorVersion());
		Assert.assertEquals(original.getFormat(), clone.getFormat());
		Assert.assertNotNull(clone.getCreationDate());
		Assert.assertNotNull(clone.getLastModified());
	}
	
	@Test
	public void getItems_all() {
		//create an author with 2 items
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-all-" + UUID.randomUUID().toString());
		QuestionItem item = questionDao.createAndPersist(id, "NGC all", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		//retrieve all items
		List<QuestionItemFull> items = questionDao.getAllItems(0, -1);
		Assert.assertNotNull(items);
		Assert.assertTrue(items.size() >= 1);
		Assert.assertTrue(items.contains(item));	
	}
	
	@Test
	public void getItemsWithOneAuthor() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-all-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC all", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC all", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		QuestionItem item3 = questionDao.createAndPersist(id, "NGC all", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		Identity id2 = JunitTestHelper.createAndPersistIdentityAsUser("QOwn-all-" + UUID.randomUUID().toString());
		questionDao.addAuthors(Collections.singletonList(id2), item3);
		dbInstance.commitAndCloseSession();
		
		List<QuestionItem> itemsWithOneAuthor = questionDao.getItemsWithOneAuthor(id);
		
		Assert.assertNotNull(itemsWithOneAuthor);
		Assert.assertTrue(itemsWithOneAuthor.size() == 2);
		Assert.assertTrue(itemsWithOneAuthor.contains(item1));	
		Assert.assertTrue(itemsWithOneAuthor.contains(item2));	
	}


	@Test
	public void getNumOfQuestions() {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItem item = questionDao.createAndPersist(null, "NGC 1277", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		Assert.assertNotNull(item);
		dbInstance.commitAndCloseSession();
		
		int numOfQuestions = questionDao.getNumOfQuestions();
		Assert.assertTrue(numOfQuestions >= 1);
		dbInstance.commitAndCloseSession();
	}
	
	@Test
	public void resetAllStatesToDraft() {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QuestionItemImpl item1 = questionDao.createAndPersist(null, "RES DRAFT 1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		item1.setQuestionStatus(QuestionStatus.endOfLife);
		questionDao.loadForUpdate(item1);
		QuestionItemImpl item2 = questionDao.createAndPersist(null, "RES DRAFT 2", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		item2.setQuestionStatus(QuestionStatus.review);
		questionDao.loadForUpdate(item2);
		QuestionItemImpl item3 = questionDao.createAndPersist(null, "RES DRAFT 3", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		item3.setQuestionStatus(QuestionStatus.revised);
		questionDao.loadForUpdate(item3);
		questionDao.createAndPersist(null, "RES DRAFT 4", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commitAndCloseSession();
		
		questionDao.resetAllStatesToDraft();
		dbInstance.commitAndCloseSession();
		
		List<QuestionItemFull> allItems = questionDao.getAllItems(0,  -1);
		for (QuestionItem item: allItems) {
			Assert.assertEquals(QuestionStatus.draft, item.getQuestionStatus());
		}
	}

	@Test
	public void getFavoritItemKeys() {
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("fav-item-" + UUID.randomUUID().toString());
		QuestionItem item1 = questionDao.createAndPersist(id, "NGC 331", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "NGC 332", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item3 = questionDao.createAndPersist(id, "NGC 333", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		markManager.setMark(item1, id, null, "[QuestionItem:" + item1.getKey() + "]");
		markManager.setMark(item2, id, null, "[QuestionItem:" + item2.getKey() + "]");
		dbInstance.commitAndCloseSession();
		
		List<Long> favoritKeys = questionDao.getFavoritKeys(id);
		Assert.assertNotNull(favoritKeys);
		Assert.assertEquals(2, favoritKeys.size());
		Assert.assertTrue(favoritKeys.contains(item1.getKey()));
		Assert.assertTrue(favoritKeys.contains(item2.getKey()));
		Assert.assertFalse(favoritKeys.contains(item3.getKey()));
	}
	
	@Test
	public void shareItems_countSharedItemByResource() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Share-item-");
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(null, "Count-shared-Item-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(null, "Count-shared-Item-2", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item3 = questionDao.createAndPersist(null, "Count-shared-Item-3", QTI21Constants.QTI_21_FORMAT, Locale.FRENCH.getLanguage(), null, null, null, fibType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item1, group.getResource());
		questionDao.share(item2, group.getResource());
		questionDao.share(item3, group.getResource());
		dbInstance.commitAndCloseSession();
		
		//retrieve them
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		params.setFormat(QTI21Constants.QTI_21_FORMAT);
		int sharedItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(3, sharedItems);
	}
	
	@Test
	public void shareItems_countSharedItemByResource_format() {
		//create a group to share 2 items
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Share-item-");
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		BusinessGroup group = businessGroupDao.createAndPersist(null, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(null, "Count-shared-Item-1", "IMS QTI 1.2", Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(null, "Count-shared-Item-2", "IMS QTI 1.2.1", Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item3 = questionDao.createAndPersist(null, "Count-shared-Item-3", QTI21Constants.QTI_21_FORMAT, Locale.FRENCH.getLanguage(), null, null, null, fibType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item1, group.getResource());
		questionDao.share(item2, group.getResource());
		questionDao.share(item3, group.getResource());
		dbInstance.commitAndCloseSession();
		
		//retrieve them
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		params.setFormat(QTI21Constants.QTI_21_FORMAT);
		int sharedItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(1, sharedItems);
	}
	
	@Test
	public void shareItems_avoidDuplicates() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("QShare-3-" + UUID.randomUUID());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		

		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		
		//retrieve them
		List<QuestionItemView> sharedItems = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(sharedItems);
		Assert.assertEquals(1, sharedItems.size());
		Assert.assertEquals(item.getKey(), sharedItems.get(0).getKey());
	}
	
	@Test
	public void shareItems_businessGroups() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-item-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		questionDao.share(item, group.getResource());
		
		//retrieve them
		List<BusinessGroup> shared = questionDao.getResourcesWithSharedItems(id);
		Assert.assertNotNull(shared);
		Assert.assertEquals(1, shared.size());
		Assert.assertTrue(shared.contains(group));
	}
	
	@Test
	public void shareInfos_byItems() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-item-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdap", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdaq", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		questionDao.share(item, group1.getResource());
		questionDao.share(item, group2.getResource());
		dbInstance.commit();

		//retrieve them
		List<QuestionItem2Resource> shared = questionDao.getSharedResourceInfos(item);
		Assert.assertNotNull(shared);
		Assert.assertEquals(2, shared.size());
	}
	
	@Test
	public void shareItems_removeFromBusinessGroups() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-item-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		questionDao.share(item, group.getResource());
		dbInstance.commit();
		
		//check them
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		List<QuestionItemView> shared = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertNotNull(shared);
		Assert.assertEquals(1, shared.size());
		
		//remove
		questionDao.removeFromShare(Collections.<QuestionItemShort>singletonList(item), group.getResource());
		dbInstance.commitAndCloseSession();
		//check
		int numOfStayingItems = qItemQueriesDao.countItems(params);
		Assert.assertEquals(0, numOfStayingItems);
	}
	
	@Test
	public void shareItems_removeFromBusinessGroups_paranoid() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Share-item-");
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdao-b", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdao-c", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item1 = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		QuestionItem item2 = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		questionDao.share(item1, group1.getResource());
		questionDao.share(item1, group2.getResource());
		questionDao.share(item2, group1.getResource());
		questionDao.share(item2, group2.getResource());
		dbInstance.commit();
		
		SearchQuestionItemParams params1 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params1.setResource(group1.getResource());
		SearchQuestionItemParams params2 = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params2.setResource(group2.getResource());
		
		//check them
		int numOfItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(2, numOfItems_1);
		int numOfItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(2, numOfItems_2);
		
		//remove
		questionDao.removeFromShare(Collections.<QuestionItemShort>singletonList(item2), group1.getResource());
		dbInstance.commitAndCloseSession();

		//check
		int numOfStayingItems_1 = qItemQueriesDao.countItems(params1);
		Assert.assertEquals(1, numOfStayingItems_1);
		int numOfStayingItems_2 = qItemQueriesDao.countItems(params2);
		Assert.assertEquals(2, numOfStayingItems_2);
		
		
		List<QuestionItemView> items_1 = qItemQueriesDao.getItems(params1, 0, -1);
		Assert.assertEquals(1, items_1.size());
		Assert.assertEquals(item1.getKey(), items_1.get(0).getKey());
	}
	
	@Test
	public void getSharedResources() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-item-" + UUID.randomUUID().toString());
		BusinessGroup group1 = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		BusinessGroup group2 = businessGroupDao.createAndPersist(id, "gdao", "gdao-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-Item-Dup-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		
		//share them
		questionDao.share(item, group1.getResource());
		questionDao.share(item, group2.getResource());
		dbInstance.commitAndCloseSession();
		
		//retrieve them
		List<OLATResource> shared = questionDao.getSharedResources(item);
		Assert.assertNotNull(shared);
		Assert.assertEquals(2, shared.size());
		Assert.assertTrue(shared.contains(group1.getResource()));
		Assert.assertTrue(shared.contains(group2.getResource()));
	}
	
	@Test
	public void removeFromShare() {
		//create a group to share 2 items
		QItemType mcType = qItemTypeDao.loadByType(QuestionType.MC.name());
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("Share-rm-" + UUID.randomUUID().toString());
		BusinessGroup group = businessGroupDao.createAndPersist(id, "gdrm", "gdrm-desc", BusinessGroup.BUSINESS_TYPE,
				-1, -1, false, false, false, false, false);
		QuestionItem item = questionDao.createAndPersist(id, "Share-item-rm-1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, mcType);
		dbInstance.commit();
		//share them
		questionDao.share(item, group.getResource());
		
		//retrieve them as a check
		SearchQuestionItemParams params = new SearchQuestionItemParams(id, null, Locale.ENGLISH);
		params.setResource(group.getResource());
		List<QuestionItemView> shared = qItemQueriesDao.getItems(params, 0, -1);
		Assert.assertEquals(1, shared.size());
		//and remove the items
		List<QuestionItemShort> toDelete = Collections.<QuestionItemShort>singletonList(shared.get(0));
		int count = questionDao.removeFromShares(toDelete);
		Assert.assertEquals(1, count);
		dbInstance.commit();//make sure that changes are committed
	}
	
	@Test
	public void deleteQuestion() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QuestionItem item = questionDao.createAndPersist(null, "To delete 1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		List<QuestionItem> itemsToDelete = Collections.singletonList(item);
		questionDao.delete(itemsToDelete);
		dbInstance.commitAndCloseSession();
		
		QuestionItem deletedItem = questionDao.loadById(item.getKey());
		Assert.assertNull(deletedItem);
	}
	
	@Test
	public void deleteQuestion_alreadyDeletedQuestions() {
		QItemType fibType = qItemTypeDao.loadByType(QuestionType.FIB.name());
		QuestionItem item1 = questionDao.createAndPersist(null, "To delete 1", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		QuestionItem item2 = questionDao.createAndPersist(null, "To delete 2", QTI21Constants.QTI_21_FORMAT, Locale.ENGLISH.getLanguage(), null, null, null, fibType);
		dbInstance.commitAndCloseSession();
		
		// delete item 1
		questionDao.delete(Collections.singletonList(item1));
		dbInstance.commitAndCloseSession();
		
		List<QuestionItem> itemsToDelete = new ArrayList<>();
		itemsToDelete.add(item1);
		itemsToDelete.add(item2);
		questionDao.delete(itemsToDelete);
		dbInstance.commitAndCloseSession();
		
		QuestionItem deletedItem1 = questionDao.loadById(item1.getKey());
		Assert.assertNull(deletedItem1);
		QuestionItem deletedItem2 = questionDao.loadById(item2.getKey());
		Assert.assertNull(deletedItem2);
	}
}