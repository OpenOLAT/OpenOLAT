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

	private static final String TAXONOMY_QPOOL_KEY = "taxonomy.qpool.key";
	public static final String DEFAULT_TAXONOMY_QPOOL_IDENTIFIER = "QPOOL";
	
	private String taxonomyQPoolKey;
	private boolean reviewProcessEnabled = false;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private List<QPoolSPI> questionPoolProviders;

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
	
	public void addQuestionPoolProvider(QPoolSPI provider) {
		int currentIndex = -1;
		for(int i=questionPoolProviders.size(); i-->0; ) {
			QPoolSPI currentProvider = questionPoolProviders.get(i);
			if(provider.getFormat() != null &&
					provider.getFormat().equals(currentProvider.getFormat())) {
				currentIndex = i;
			}
		}
		
		if(currentIndex >= 0) {
			questionPoolProviders.set(currentIndex, provider);
		} else {
			questionPoolProviders.add(provider);
		}
	}
	
	public String getTaxonomyQPoolKey() {
		return taxonomyQPoolKey;
	}

	public void setTaxonomyQPoolKey(String taxonomyQPoolKey) {
		this.taxonomyQPoolKey = taxonomyQPoolKey;
		setStringProperty(TAXONOMY_QPOOL_KEY, taxonomyQPoolKey, true);
	}

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
