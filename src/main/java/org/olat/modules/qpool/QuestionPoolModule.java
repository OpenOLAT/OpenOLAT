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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.qpool.manager.review.ProcesslessDecisionProvider;
import org.olat.modules.qpool.site.QuestionPoolSite;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.manager.TaxonomyDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolModule")
public class QuestionPoolModule extends AbstractSpringModule implements ConfigOnOff {

	private static final String DIRECTORY = "qpool";
	private static final String INFOS_PAGE_DIRECTORY = "infospage";
	private static final String COLLECTIONS_ENABLED = "collections.enabled";
	private static final String POOLS_ENABLED = "pools.enabled";
	private static final String SHARES_ENABLED = "shares.enabled";
	private static final String TAXONOMY_ENABLED = "taxonomy.enabled";
	private static final String EDUCATIONAL_CONTEXT_ENABLED = "educational.context.enabled";
	private static final String TAXONOMY_QPOOL_KEY = "taxonomy.qpool.key";
	public static final String DEFAULT_TAXONOMY_QPOOL_IDENTIFIER = "QPOOL";
	private static final String DELETE_QUESTIONS_WITHOUT_AUTHOR = "delete.questions.without.author";
	private static final String IGNORE_COMPETENCES = "ignore.taxonomies";
	private static final String IMPORT_CREATE_TAXONOMY_LEVEL = "import.create.taxomomy.level";
	
	private static final String REVIEW_PROCESS_ENABLED = "review.process.enabled";
	private static final String REVIEW_DECISION_TYPE = "review.decision.type";
	private static final String REVIEW_DECISION_NUMBER_OF_RATINGS = "review.decision.number.of.ratings";
	private static final String REVIEW_LOWER_LIMIT = "review.decision.lower.limit";
	private static final String FINAL_VISIBLE_TEACH = "final.visible.teach";
	
	private static final String POOL_ADMIN_METADATA = "pool.admin.metadata";
	private static final String POOL_ADMIN_STATUS = "pool.admin.status";
	private static final String POOL_ADMIN_REVIEW_PROCESS = "pool.admin.review.process";
	private static final String POOL_ADMIN_TAXONOMY = "pool.admin.taxonomy";
	private static final String POOL_ADMIN_POOLS = "pool.admin.pools";
	private static final String POOL_ADMIN_ITEM_TYPES = "pool.admin.item.types";
	private static final String POOL_ADMIN_EDUCATIONAL_CONTEXT = "pool.admin.educational.context";
	
	private boolean collectionsEnabled = true;
	private boolean poolsEnabled = true;
	private boolean sharesEnabled = true;
	private boolean taxonomyEnabled = true;
	private boolean educationalContextEnabled = true;
	private String taxonomyQPoolKey;
	private boolean deleteQuestionsWithoutAuthor = false;
	private boolean ignoreCompetences = true;
	private boolean importCreateTaxonomyLevel = true;

	private boolean reviewProcessEnabled = false;
	private String reviewDecisionType = ProcesslessDecisionProvider.TYPE;
	private boolean finalVisibleTeach = false;
	
	private boolean poolAdminAllowedToEditMetadata = false;
	private boolean poolAdminAllowedToEditStatus = false;
	private boolean poolAdminAllowedToConfigReviewProcess = false;
	private boolean poolAdminAllowedToConfigTaxonomy = true;
	private boolean poolAdminAllowedToConfigPools = true;
	private boolean poolAdminAllowedToConfigItemTypes = true;
	private boolean poolAdminAllowedToConfigEducationalContext = true;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private List<QPoolSPI> questionPoolProviders;

    private int reviewDecisionNumberOfRatings;
    private int reviewDecisionLowerLimit;

	private VFSContainer rootContainer;
	
	@Autowired
	public QuestionPoolModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		NewControllerFactory.getInstance().addContextEntryControllerCreator("QPool",
				new SiteContextEntryControllerCreator(QuestionPoolSite.class));

		updateProperties();
		initTaxonomy();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
    
	private void initTaxonomy() {
		if(!StringHelper.isLong(taxonomyQPoolKey)) {
			Taxonomy taxonomy = taxonomyDao.createTaxonomy(DEFAULT_TAXONOMY_QPOOL_IDENTIFIER, "Question pool", "taxonomy for the question pool", DEFAULT_TAXONOMY_QPOOL_IDENTIFIER);
			dbInstance.commitAndCloseSession();
			setTaxonomyQPoolKey(taxonomy.getKey().toString());
		}	
	}
	
	private void updateProperties() {
		String collectionsEnabledObj = getStringPropertyValue(COLLECTIONS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(collectionsEnabledObj)) {
			collectionsEnabled = "true".equals(collectionsEnabledObj);
		}
		
		String poolsEnabledObj = getStringPropertyValue(POOLS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(poolsEnabledObj)) {
			poolsEnabled = "true".equals(poolsEnabledObj);
		}
		
		String sharesEnabledObj = getStringPropertyValue(SHARES_ENABLED, true);
		if(StringHelper.containsNonWhitespace(sharesEnabledObj)) {
			sharesEnabled = "true".equals(sharesEnabledObj);
		}
		
		String reviewProcessEnabledObj = getStringPropertyValue(REVIEW_PROCESS_ENABLED, true);
		if(StringHelper.containsNonWhitespace(reviewProcessEnabledObj)) {
			reviewProcessEnabled = "true".equals(reviewProcessEnabledObj);
		}
		
		String taxonomyEnabledObj = getStringPropertyValue(TAXONOMY_ENABLED, true);
		if(StringHelper.containsNonWhitespace(taxonomyEnabledObj)) {
			taxonomyEnabled = "true".equals(taxonomyEnabledObj);
		}
		
		String educationalContextEnabledObj = getStringPropertyValue(EDUCATIONAL_CONTEXT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(educationalContextEnabledObj)) {
			educationalContextEnabled = "true".equals(educationalContextEnabledObj);
		}
		
		String deleteQuestionsWithoutAuthorObj = getStringPropertyValue(DELETE_QUESTIONS_WITHOUT_AUTHOR, true);
		if(StringHelper.containsNonWhitespace(deleteQuestionsWithoutAuthorObj)) {
			deleteQuestionsWithoutAuthor = "true".equals(deleteQuestionsWithoutAuthorObj);
		}
		
		String ignoreCompetencesObj = getStringPropertyValue(IGNORE_COMPETENCES, true);
		if(StringHelper.containsNonWhitespace(ignoreCompetencesObj)) {
			ignoreCompetences = "true".equals(ignoreCompetencesObj);
		}
		
		String importCreateTaxonomyLevelObj = getStringPropertyValue(IMPORT_CREATE_TAXONOMY_LEVEL, true);
		if(StringHelper.containsNonWhitespace(importCreateTaxonomyLevelObj)) {
			importCreateTaxonomyLevel = "true".equals(importCreateTaxonomyLevelObj);
		}
		
		String reviewDecisionTypeObj = getStringPropertyValue(REVIEW_DECISION_TYPE, true);
		if(StringHelper.containsNonWhitespace(reviewDecisionTypeObj)) {
			reviewDecisionType = reviewDecisionTypeObj;
		}
		
		String reviewDecisionNumberOfRatingsObj = getStringPropertyValue(REVIEW_DECISION_NUMBER_OF_RATINGS, true);
		if(StringHelper.containsNonWhitespace(reviewDecisionNumberOfRatingsObj)) {
			reviewDecisionNumberOfRatings = Integer.parseInt(reviewDecisionNumberOfRatingsObj);
		}
		
		String reviewDecisionLowerLimitObj = getStringPropertyValue(REVIEW_LOWER_LIMIT, true);
		if(StringHelper.containsNonWhitespace(reviewDecisionLowerLimitObj)) {
			reviewDecisionLowerLimit = Integer.parseInt(reviewDecisionLowerLimitObj);
		}
		
		String finalVisibleTeachObj = getStringPropertyValue(FINAL_VISIBLE_TEACH, true);
		if(StringHelper.containsNonWhitespace(finalVisibleTeachObj)) {
			finalVisibleTeach = "true".equals(finalVisibleTeachObj);
		}
		
		String taxonomyQPoolKeyObj = getStringPropertyValue(TAXONOMY_QPOOL_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyQPoolKeyObj)) {
			taxonomyQPoolKey = taxonomyQPoolKeyObj;
		}
		
		String poolAdminAllowedToEditMetadataObj = getStringPropertyValue(POOL_ADMIN_METADATA, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToEditMetadataObj)) {
			poolAdminAllowedToEditMetadata = "true".equals(poolAdminAllowedToEditMetadataObj);
		}
		
		String poolAdminAllowedToEditStatusObj = getStringPropertyValue(POOL_ADMIN_STATUS, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToEditStatusObj)) {
			poolAdminAllowedToEditStatus = "true".equals(poolAdminAllowedToEditStatusObj);
		}
		
		String poolAdminAllowedToConfigReviewProcessObj = getStringPropertyValue(POOL_ADMIN_REVIEW_PROCESS, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToConfigReviewProcessObj)) {
			poolAdminAllowedToConfigReviewProcess = "true".equals(poolAdminAllowedToConfigReviewProcessObj);
		}
		
		String poolAdminAllowedToConfigTaxonomyObj = getStringPropertyValue(POOL_ADMIN_TAXONOMY, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToConfigTaxonomyObj)) {
			poolAdminAllowedToConfigTaxonomy = "true".equals(poolAdminAllowedToConfigTaxonomyObj);
		}
		
		String poolAdminAllowedToConfigPoolsObj = getStringPropertyValue(POOL_ADMIN_POOLS, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToConfigPoolsObj)) {
			poolAdminAllowedToConfigPools = "true".equals(poolAdminAllowedToConfigPoolsObj);
		}
		
		String poolAdminAllowedToConfigItemTypesObj = getStringPropertyValue(POOL_ADMIN_ITEM_TYPES, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToConfigItemTypesObj)) {
			poolAdminAllowedToConfigItemTypes = "true".equals(poolAdminAllowedToConfigItemTypesObj);
		}
		
		String poolAdminAllowedToConfigEducationalContextObj = getStringPropertyValue(POOL_ADMIN_EDUCATIONAL_CONTEXT, true);
		if(StringHelper.containsNonWhitespace(poolAdminAllowedToConfigEducationalContextObj)) {
			poolAdminAllowedToConfigEducationalContext = "true".equals(poolAdminAllowedToConfigEducationalContextObj);
		}
		
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public VFSContainer getRootContainer() {
		if(rootContainer == null) {
			rootContainer = VFSManager.olatRootContainer(File.separator + DIRECTORY, null);
		}
		return rootContainer;
	}
	
	public VFSContainer getInfoPageContainer() {
		String path = "/" + DIRECTORY + "/" + INFOS_PAGE_DIRECTORY;
		return VFSManager.olatRootContainer(path, null);
	}

	public List<QPoolSPI> getQuestionPoolProviders() {
		List<QPoolSPI> providers = new ArrayList<>(questionPoolProviders);
		Collections.sort(providers, new QuestionPoolSPIComparator());
		return providers;
	}
	
	public QPoolSPI getQuestionPoolProvider(String format) {
		for(QPoolSPI provider:questionPoolProviders) {
			if(provider.getFormat().equals(format)) {
				return provider;
			}
		}
		return null;
	}

	public boolean isCollectionsEnabled() {
		return collectionsEnabled;
	}

	public void setCollectionsEnabled(boolean collectionsEnabled) {
		this.collectionsEnabled = collectionsEnabled;
		setStringProperty(COLLECTIONS_ENABLED, Boolean.toString(collectionsEnabled), true);
	}

	public boolean isPoolsEnabled() {
		return poolsEnabled;
	}

	public void setPoolsEnabled(boolean poolsEnabled) {
		this.poolsEnabled = poolsEnabled;
		setStringProperty(POOLS_ENABLED, Boolean.toString(poolsEnabled), true);
	}

	public boolean isSharesEnabled() {
		return sharesEnabled;
	}

	public void setSharesEnabled(boolean sharesEnabled) {
		this.sharesEnabled = sharesEnabled;
		setStringProperty(SHARES_ENABLED, Boolean.toString(sharesEnabled), true);
	}
	
	public boolean isTaxonomyEnabled() {
		return taxonomyEnabled;
	}

	public void setTaxonomyEnabled(boolean taxonomyEnabled) {
		this.taxonomyEnabled = taxonomyEnabled;
		setStringProperty(TAXONOMY_ENABLED, Boolean.toString(taxonomyEnabled), true);
	}

	public boolean isEducationalContextEnabled() {
		return educationalContextEnabled;
	}

	public void setEducationalContextEnabled(boolean educationalContextEnabled) {
		this.educationalContextEnabled = educationalContextEnabled;
		setStringProperty(EDUCATIONAL_CONTEXT_ENABLED, Boolean.toString(educationalContextEnabled), true);
	}

	public boolean isDeleteQuestionsWithoutAuthor() {
		return deleteQuestionsWithoutAuthor;
	}

	public void setDeleteQuestionsWithoutAuthor(boolean deleteQuestionsWithoutAuthor) {
		this.deleteQuestionsWithoutAuthor = deleteQuestionsWithoutAuthor;
		setStringProperty(DELETE_QUESTIONS_WITHOUT_AUTHOR, Boolean.toString(deleteQuestionsWithoutAuthor), true);
	}

	public boolean isIgnoreCompetences() {
		return ignoreCompetences;
	}

	public void setIgnoreCompetences(boolean ignoreCompetences) {
		this.ignoreCompetences = ignoreCompetences;
		setStringProperty(IGNORE_COMPETENCES, Boolean.toString(ignoreCompetences), true);
	}

	public boolean isImportCreateTaxonomyLevel() {
		return importCreateTaxonomyLevel;
	}

	public void setImportCreateTaxonomyLevel(boolean importCreateTaxonomyLevel) {
		this.importCreateTaxonomyLevel = importCreateTaxonomyLevel;
		setStringProperty(IMPORT_CREATE_TAXONOMY_LEVEL, Boolean.toString(importCreateTaxonomyLevel), true);
	}

	public boolean isReviewProcessEnabled() {
		return reviewProcessEnabled;
	}

	public void setReviewProcessEnabled(boolean reviewProcessEnabled) {
		this.reviewProcessEnabled = reviewProcessEnabled;
		setStringProperty(REVIEW_PROCESS_ENABLED, Boolean.toString(reviewProcessEnabled), true);
	}
	
	public String getReviewDecisionProviderType() {
		return reviewDecisionType;
	}

	public void setReviewDecisionType(String reviewDecisionType) {
		this.reviewDecisionType = reviewDecisionType;
		setStringProperty(REVIEW_DECISION_TYPE, reviewDecisionType, true);
	}

	public int getReviewDecisionNumberOfRatings() {
		return reviewDecisionNumberOfRatings;
	}

	public void setReviewDecisionNumberOfRatings(int reviewDecisionNumberOfRatings) {
		this.reviewDecisionNumberOfRatings = reviewDecisionNumberOfRatings;
		setIntProperty(REVIEW_DECISION_NUMBER_OF_RATINGS, reviewDecisionNumberOfRatings, true);
	}

	public int getReviewDecisionLowerLimit() {
		return reviewDecisionLowerLimit;
	}

	public void setReviewDecisionLowerLimit(int reviewDecisionLowerLimit) {
		this.reviewDecisionLowerLimit = reviewDecisionLowerLimit;
		setIntProperty(REVIEW_LOWER_LIMIT, reviewDecisionLowerLimit, true);
	}
	
	public boolean isFinalVisibleTeach() {
		return finalVisibleTeach;
	}

	public void setFinalVisibleTeach(boolean finalVisibleTeach) {
		this.finalVisibleTeach = finalVisibleTeach;
		setStringProperty(FINAL_VISIBLE_TEACH, Boolean.toString(finalVisibleTeach), true);
	}

	public String getTaxonomyQPoolKey() {
		return taxonomyQPoolKey;
	}

	public void setTaxonomyQPoolKey(String taxonomyQPoolKey) {
		this.taxonomyQPoolKey = taxonomyQPoolKey;
		setStringProperty(TAXONOMY_QPOOL_KEY, taxonomyQPoolKey, true);
	}

	public boolean isPoolAdminAllowedToEditMetadata() {
		return poolAdminAllowedToEditMetadata;
	}

	public void setPoolAdminAllowedToEditMetadata(boolean poolAdminAllowedToEditMetadata) {
		this.poolAdminAllowedToEditMetadata = poolAdminAllowedToEditMetadata;
		setStringProperty(POOL_ADMIN_METADATA, Boolean.toString(poolAdminAllowedToEditMetadata), true);
	}

	public boolean isPoolAdminAllowedToEditStatus() {
		return poolAdminAllowedToEditStatus;
	}

	public void setPoolAdminAllowedToEditStatus(boolean poolAdminAllowedToEditStatus) {
		this.poolAdminAllowedToEditStatus = poolAdminAllowedToEditStatus;
		setStringProperty(POOL_ADMIN_STATUS, Boolean.toString(poolAdminAllowedToEditStatus), true);
	}


	public boolean isPoolAdminAllowedToConfigReviewProcess() {
		return poolAdminAllowedToConfigReviewProcess;
	}

	public void setPoolAdminAllowedToConfigReviewProcess(boolean poolAdminAllowedToConfigReviewProcess) {
		this.poolAdminAllowedToConfigReviewProcess = poolAdminAllowedToConfigReviewProcess;
		setStringProperty(POOL_ADMIN_REVIEW_PROCESS, Boolean.toString(poolAdminAllowedToConfigReviewProcess), true);
	}

	public boolean isPoolAdminAllowedToConfigTaxonomy() {
		return poolAdminAllowedToConfigTaxonomy;
	}

	public void setPoolAdminAllowedToConfigTaxonomy(boolean poolAdminAllowedToConfigTaxonomy) {
		this.poolAdminAllowedToConfigTaxonomy = poolAdminAllowedToConfigTaxonomy;
		setStringProperty(POOL_ADMIN_TAXONOMY, Boolean.toString(poolAdminAllowedToConfigTaxonomy), true);
	}

	public boolean isPoolAdminAllowedToConfigPools() {
		return poolAdminAllowedToConfigPools;
	}

	public void setPoolAdminAllowedToConfigPools(boolean poolAdminAllowedToConfigPools) {
		this.poolAdminAllowedToConfigPools = poolAdminAllowedToConfigPools;
		setStringProperty(POOL_ADMIN_POOLS, Boolean.toString(poolAdminAllowedToConfigPools), true);
	}

	public boolean isPoolAdminAllowedToConfigItemTypes() {
		return poolAdminAllowedToConfigItemTypes;
	}

	public void setPoolAdminAllowedToConfigItemTypes(boolean poolAdminAllowedToConfigItemTypes) {
		this.poolAdminAllowedToConfigItemTypes = poolAdminAllowedToConfigItemTypes;
		setStringProperty(POOL_ADMIN_ITEM_TYPES, Boolean.toString(poolAdminAllowedToConfigItemTypes), true);
	}

	public boolean isPoolAdminAllowedToConfigEducationalContext() {
		return poolAdminAllowedToConfigEducationalContext;
	}

	public void setPoolAdminAllowedToConfigEducationalContext(boolean poolAdminAllowedToConfigEducationalContext) {
		this.poolAdminAllowedToConfigEducationalContext = poolAdminAllowedToConfigEducationalContext;
		setStringProperty(POOL_ADMIN_EDUCATIONAL_CONTEXT, Boolean.toString(poolAdminAllowedToConfigEducationalContext), true);
	}

}
