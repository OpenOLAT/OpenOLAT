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
package org.olat.modules.catalog.site;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.AbstractSiteDefinition;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinition;
import org.olat.core.gui.control.navigation.SiteInstance;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.repository.RepositoryModule;

/**
 * 
 * Initial date: 16.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CatalogSiteDef extends AbstractSiteDefinition implements SiteDefinition {

	@Override
	public SiteInstance createSite(UserRequest ureq, WindowControl wControl, SiteConfiguration config) {
		// default is to show catalog to anybody, even guests and invitee
		return new CatalogSite(this, ureq.getLocale());
	}
	
	@Override
	public boolean isEnabled() {
		return isFeatureEnabled() && super.isEnabled();
	}
	
	@Override
	public boolean isFeatureEnabled() {
		CatalogV2Module catalogV2Module = CoreSpringFactory.getImpl(CatalogV2Module.class);
		if (catalogV2Module.isEnabled()) {
			return true;
		}
		
		RepositoryModule repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		return repositoryModule.isCatalogEnabled() && repositoryModule.isCatalogSiteEnabled();
	}
}
