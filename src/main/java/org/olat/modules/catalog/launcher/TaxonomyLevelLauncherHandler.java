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
package org.olat.modules.catalog.launcher;

import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.ui.CatalogLauncherTaxonomyController;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.catalog.ui.admin.CatalogLauncherTaxonomyEditController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelLauncherHandler implements CatalogLauncherHandler {
	
	private static final String TYPE = "taxonomy.level";
	
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryModule repositoryModule;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return taxonomyModule.isEnabled() && StringHelper.isLong(repositoryModule.getTaxonomyTreeKey());
	}

	@Override
	public int getSortOrder() {
		return 500;
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public String getTypeI18nKey() {
		return "launcher.taxonomy.level.type";
	}

	@Override
	public String getAddI18nKey() {
		return "launcher.taxonomy.level.add";
	}

	@Override
	public String getEditI18nKey() {
		return "launcher.taxonomy.level.edit";
	}

	@Override
	public String getDetails(CatalogLauncher catalogLauncher) {
		String config = catalogLauncher.getConfig();
		if (StringHelper.isLong(config)) {
			TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> Long.valueOf(config));
			if (taxonomyLevel != null) {
				return taxonomyLevel.getDisplayName();
			}
		}
		return "-";
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogLauncher catalogLauncher) {
		return new CatalogLauncherTaxonomyEditController(ureq, wControl, this, catalogLauncher);
	}

	@Override
	public Controller createRunController(UserRequest ureq, WindowControl wControl, Translator translator,
			CatalogLauncher catalogLauncher, CatalogRepositoryEntrySearchParams defaultSearchParams) {
		if (!StringHelper.isLong(catalogLauncher.getConfig())) {
			return null;
		}
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> Long.valueOf(catalogLauncher.getConfig()));
		if (taxonomyLevel == null) {
			return null;
		}
		
		List<TaxonomyLevel> taxonomyLevels = taxonomyLevelDao.getChildren(taxonomyLevel);
		Comparator<TaxonomyLevel> comparator = Comparator.comparing(TaxonomyLevel::getSortOrder, Comparator.nullsLast(Comparator.reverseOrder()))
				.thenComparing(TaxonomyLevel::getDisplayName);
		taxonomyLevels.sort(comparator);
		String launcherName = CatalogV2UIFactory.translateLauncherName(translator, this, catalogLauncher);
		
		return new CatalogLauncherTaxonomyController(ureq, wControl, taxonomyLevels, launcherName);
	}

}
