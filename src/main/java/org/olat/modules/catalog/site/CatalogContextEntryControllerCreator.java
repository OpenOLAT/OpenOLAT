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

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.control.navigation.SiteSecurityCallback;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.id.context.TabContext;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.repository.RepositoryModule;
import org.olat.repository.site.MyCoursesSite;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  11 fev. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class CatalogContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
	
	private static final Logger log = Tracing.createLoggerFor(CatalogContextEntryControllerCreator.class);
	
	private CatalogV2Module catalogV2Module;
	private RepositoryModule repositoryModule;

	@Override
	public ContextEntryControllerCreator clone() {
		return this;
	}

	@Override
	public String getSiteClassName(List<ContextEntry> ces, UserRequest ureq) {
		if (isCatalogSiteVisible(ureq)) {
			return CatalogSite.class.getName();
		}
		return MyCoursesSite.class.getName();
	}
	
	private boolean isCatalogSiteVisible(UserRequest ureq) {
		if (getCatalogV2Module().isEnabled() 
				|| (getRepositoryModule().isCatalogEnabled() && getRepositoryModule().isCatalogSiteEnabled())) {
			try {
				CatalogSiteDef siteDef = CoreSpringFactory.getImpl(CatalogSiteDef.class);
				SiteDefinitions siteDefinitions = CoreSpringFactory.getImpl(SiteDefinitions.class);
				SiteConfiguration config = siteDefinitions.getConfigurationSite(siteDef);
				if(config != null && StringHelper.containsNonWhitespace(config.getSecurityCallbackBeanId())) {
					String secCallbackBeanId = config.getSecurityCallbackBeanId();
					Object siteSecCallback = CoreSpringFactory.getBean(secCallbackBeanId);
					if (siteSecCallback instanceof SiteSecurityCallback	&& !((SiteSecurityCallback)siteSecCallback).isAllowedToLaunchSite(ureq)) {
						return false;
					}
				}
			} catch (Exception e) {
				log.error("Cannot find if the user is allowed to launch the catalog.", e);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
		return getCatalogV2Module().isEnabled() || getRepositoryModule().isCatalogEnabled();
	}

	@Override
	public TabContext getTabContext(UserRequest ureq, OLATResourceable ores, ContextEntry mainEntry, List<ContextEntry> entries) {
		//My courses need the catalog entry
		List<ContextEntry> redirectEntries = new ArrayList<>();
		if(entries == null || entries.isEmpty() || !entries.get(0).equals(mainEntry)) {
			redirectEntries.add(mainEntry);
		}
		if(entries != null && entries.size() > 0) {
			redirectEntries.addAll(entries);
		}
		return new TabContext(getTabName(mainEntry, ureq), ores, redirectEntries);
	}
	
	private CatalogV2Module getCatalogV2Module() {
		if (catalogV2Module == null) {
			catalogV2Module = CoreSpringFactory.getImpl(CatalogV2Module.class);
		}
		return catalogV2Module;
	}
	
	private RepositoryModule getRepositoryModule() {
		if (repositoryModule == null) {
			repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		}
		return repositoryModule;
	}
}