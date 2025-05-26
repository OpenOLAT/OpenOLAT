/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.catalog.ui.admin;

import java.util.Date;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.CoreSpringFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.TaxonomyService;

/**
 * 
 * Initial date: 1 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogTaxonomySecurityCallback implements TaxonomySecurityCallback {
	
	private final TaxonomyRef taxonomy;
	private final IdentityRef identity;
	private Set<Long> editableLevelKeys;

	private final TaxonomyService taxonomyService;

	public CatalogTaxonomySecurityCallback(TaxonomyRef taxonomy, IdentityRef identity) {
		this.taxonomy = taxonomy;
		this.identity = identity;
		
		taxonomyService = CoreSpringFactory.getImpl(TaxonomyService.class);
		
		loadEditableLevels();
	}

	@Override
	public void refresh() {
		loadEditableLevels();
	}
	
	private void loadEditableLevels() {
		editableLevelKeys = taxonomyService.getManagedTaxonomyLevelKeys(taxonomy, identity, new Date());
	}
	
	@Override
	public boolean canEditTaxonomyMetadata() {
		return false;
	}

	@Override
	public boolean canImportExport() {
		return false;
	}

	@Override
	public boolean canCreateChild(TaxonomyLevel level) {
		if (level == null) {
			return false;
		}
		
		return editableLevelKeys.contains(level.getKey());
	}

	@Override
	public boolean canDelete(TaxonomyLevel level) {
		return false;
	}
	
	@Override
	public boolean canMove(TaxonomyLevel level) {
		return false;
	}

	@Override
	public boolean canEditMetadata(TaxonomyLevel level) {
		return editableLevelKeys.contains(level.getKey());
	}
	
	@Override
	public boolean canViewLevelTypes() {
		return false;
	}

	@Override
	public boolean canViewManagement(TaxonomyLevel level) {
		return editableLevelKeys.contains(level.getKey());
	}
	
	@Override
	public boolean canViewCompetences() {
		return false;
	}
	
	@Override
	public boolean canViewLostFound() {
		return false;
	}
	
}