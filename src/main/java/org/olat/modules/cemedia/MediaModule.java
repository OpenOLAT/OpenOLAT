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
package org.olat.modules.cemedia;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaModule extends AbstractSpringModule {
	
	private static final String TAXONOMY_TREE_KEY = "taxonomy.tree.key";
	
	private String taxonomyTreeKey;
	private List<TaxonomyRef> taxonomyRefs;
	
	@Autowired
	private RepositoryModule repositoryModule;
	
	@Autowired
	public MediaModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}

	private void updateProperties() {
		String taxonomyTreeKeyObj = getStringPropertyValue(TAXONOMY_TREE_KEY, true);
		if(StringHelper.containsNonWhitespace(taxonomyTreeKeyObj)) {
			taxonomyTreeKey = taxonomyTreeKeyObj;
			taxonomyRefs = null;
		}
	}
	
	public List<TaxonomyRef> getTaxonomyRefs() {
		return getTaxonomyRefs(true);
	}
	
	public boolean isTaxonomyLinked(Long taxonomyKey, boolean fallback) {
		List<TaxonomyRef> taxonomies = getTaxonomyRefs(fallback);
		for (TaxonomyRef taxonomy : taxonomies) {
			if (taxonomy.getKey().equals(taxonomyKey)) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized List<TaxonomyRef> getTaxonomyRefs(boolean fallback) {
		if (taxonomyRefs == null && StringHelper.containsNonWhitespace(taxonomyTreeKey)) {
			taxonomyRefs = Arrays.stream(taxonomyTreeKey.split(","))
				.filter(StringHelper::isLong)
				.map(Long::valueOf)
				.map(TaxonomyRefImpl::new)
				.map(TaxonomyRef.class::cast)
				.toList();
		}
		if((taxonomyRefs == null || taxonomyRefs.isEmpty()) && fallback) {
			return repositoryModule.getTaxonomyRefs();		
		}
		return taxonomyRefs == null ? List.of() : taxonomyRefs;
	}
	
	public synchronized void setTaxonomyRefs(List<TaxonomyRef> taxonomyRefs) {
		this.taxonomyTreeKey = taxonomyRefs != null && !taxonomyRefs.isEmpty()
				? taxonomyRefs.stream().map(TaxonomyRef::getKey).map(String::valueOf).collect(Collectors.joining(","))
				: null;
		setStringProperty(TAXONOMY_TREE_KEY, taxonomyTreeKey, true);
		this.taxonomyRefs = null;
	}
}
