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
package org.olat.modules.catalog;

import java.util.Set;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Roles;
import org.olat.modules.taxonomy.TaxonomyModule;

/**
 * 
 * Initial date: 1 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSecurityCallbackFactory {
	
	public static CatalogSecurityCallback create(Roles roles) {
		boolean canEditCatalogAdministration = roles.isSystemAdmin();
		boolean canEditTaxonomy = false;
		if (CoreSpringFactory.getImpl(TaxonomyModule.class).isEnabled()) {
			if (roles.isSystemAdmin() || roles.isAdministrator()) {
				canEditTaxonomy = true;
			} else {
				Set<OrganisationRoles> taxonomyEditRoles = CoreSpringFactory.getImpl(CatalogV2Module.class).getTaxonomyEditRoles();
				for (OrganisationRoles role : taxonomyEditRoles) {
					if (roles.hasRole(role)) {
						canEditTaxonomy = true;
					}
				}
			}
		}
		
		return new CatalogSecurityCallbackImpl(canEditCatalogAdministration, canEditTaxonomy);
	}
	
	private static class CatalogSecurityCallbackImpl implements CatalogSecurityCallback {
		
		private final boolean canEditCatalogAdministration;
		private final boolean canEditTaxonomy;
		
		public CatalogSecurityCallbackImpl(boolean canEditCatalogAdministration, boolean canEditTaxonomy) {
			this.canEditCatalogAdministration = canEditCatalogAdministration;
			this.canEditTaxonomy = canEditTaxonomy;
		}

		@Override
		public boolean canEditCatalogAdministration() {
			return canEditCatalogAdministration;
		}

		@Override
		public boolean canEditTaxonomy() {
			return canEditTaxonomy;
		}
		
	}

}
