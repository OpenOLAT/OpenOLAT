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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.id.context.SiteContextEntryControllerCreator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.vfs.VFSContainer;
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

	private static final String COLLECTIONS_ENABLED = "collections.enabled";
	private static final String POOLS_ENABLED = "pools.enabled";
	private static final String SHARES_ENABLED = "shares.enabled";
	private static final String REVIEW_PROCESS_ENABLED = "review.process.enabled";
	private static final String REVIEW_DECISION_TYPE = "review.decision.type";
	private static final String REVIEW_DECISION_NUMBER_OF_RATINGS = "review.decision.number.of.ratings";
	private static final String REVIEW_LOWER_LIMIT = "review.decision.lower.limit";
	private static final String TAXONOMY_QPOOL_KEY = "taxonomy.qpool.key";
	public static final String DEFAULT_TAXONOMY_QPOOL_IDENTIFIER = "QPOOL";
	
	private boolean collectionsEnabled = true;
	private boolean poolsEnabled = true;
	private boolean sharesEnabled = true;
	private boolean reviewProcessEnabled = false;
	private String reviewDecisionType = ProcesslessDecisionProvider.TYPE;
	private String taxonomyQPoolKey;
	
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
		
		String taxonomyQPoolKeyObj = getStringPropertyValue(TAXONOMY_QPOOL_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyQPoolKeyObj)) {
			taxonomyQPoolKey = taxonomyQPoolKeyObj;
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public VFSContainer getRootContainer() {
		if(rootContainer == null) {
			rootContainer = new OlatRootFolderImpl(File.separator + "qpool", null);
		}
		return rootContainer;
	}

	public List<QPoolSPI> getQuestionPoolProviders() {
		List<QPoolSPI> providers = new ArrayList<>(questionPoolProviders);
		Collections.sort(providers, new QuestionPoolSPIComparator());
		return providers;
	}
	
	public QPoolSPI getQuestionPoolProvider(String format) {
		for(QPoolSPI provider:questionPoolProviders) {
			if(format.equals(provider.getFormat())) {
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

	public String getTaxonomyQPoolKey() {
		return taxonomyQPoolKey;
	}

	public void setTaxonomyQPoolKey(String taxonomyQPoolKey) {
		this.taxonomyQPoolKey = taxonomyQPoolKey;
		setStringProperty(TAXONOMY_QPOOL_KEY, taxonomyQPoolKey, true);
	}

	//TODO uh delete
	public Collection<QuestionStatus> getEditableQuestionStates() {
		Collection<QuestionStatus> editableQuestionStates;
		if (reviewProcessEnabled) {
			editableQuestionStates = Arrays.asList(QuestionStatus.draft, QuestionStatus.revised);
		} else {
			editableQuestionStates = Arrays.asList(QuestionStatus.values());	
		}
		return editableQuestionStates; 
	}

	public Collection<QuestionStatus> getReviewableQuestionStates() {
		Collection<QuestionStatus> revieweableStates;
		if (reviewProcessEnabled) {
			revieweableStates = Arrays.asList(QuestionStatus.review);
		} else {
			revieweableStates = Collections.emptyList();
		}
		return revieweableStates;
	}
}
