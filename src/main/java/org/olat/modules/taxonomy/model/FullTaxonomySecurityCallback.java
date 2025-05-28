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
package org.olat.modules.taxonomy.model;

import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;

/**
 * 
 * Initial date: 23 May 2025<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FullTaxonomySecurityCallback implements TaxonomySecurityCallback {
	
	@Override
	public void refresh() {
		//
	}
	
	@Override
	public boolean canEditTaxonomyMetadata() {
		return true;
	}
	
	@Override
	public boolean canImportExport() {
		return true;
	}
	
	@Override
	public boolean canFilterRelevant() {
		return false;
	}
	
	@Override
	public boolean isRelevant(TaxonomyLevel level) {
		return false;
	}
	
	@Override
	public boolean canCreateChild(TaxonomyLevel level) {
		return true;
	}

	@Override
	public boolean canDelete(TaxonomyLevel level) {
		return true;
	}

	@Override
	public boolean canMove(TaxonomyLevel level) {
		return true;
	}

	@Override
	public boolean canEditMetadata(TaxonomyLevel level) {
		return true;
	}
	
	@Override
	public boolean canViewLevelTypes() {
		return true;
	}

	@Override
	public boolean canViewManagement(TaxonomyLevel level) {
		return true;
	}

	@Override
	public boolean canViewCompetences() {
		return true;
	}
	
	@Override
	public boolean canViewLostFound() {
		return true;
	}
	
}