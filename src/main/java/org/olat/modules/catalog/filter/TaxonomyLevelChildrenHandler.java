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
package org.olat.modules.catalog.filter;

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.admin.CatalogFilterTaxonomyChildrenController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyLevelChildrenHandler implements CatalogFilterHandler {
	
	public static final String KEY_SHOW = "show";
	public static final String KEY_HIDE = "hide";
	public static final String TYPE = "taxonomy.level.children";
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private TaxonomyModule taxonomyModule;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private RepositoryModule repositoryModule;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(boolean isGuestOnly) {
		return taxonomyModule.isEnabled() && !repositoryModule.getTaxonomyRefs().isEmpty();
	}

	@Override
	public int getSortOrder() {
		return 300;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.taxonomy.children.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.taxonomy.children.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.taxonomy.children.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogFilter catalogFilter) {
		return KEY_SHOW.equals(catalogFilter.getConfig())
				? translator.translate("filter.taxonomy.children.default.show")
				: translator.translate("filter.taxonomy.children.default.hide");
	}

	@Override
	public boolean isMultiInstance() {
		return false;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogFilter catalogFilter) {
		return new CatalogFilterTaxonomyChildrenController(ureq, wControl, this, catalogFilter);
	}

	@Override
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogRepositoryEntrySearchParams searchParams, CatalogFilter catalogFilter) {
		CatalogRepositoryEntrySearchParams searchParamsCopy = searchParams.copy();
		searchParamsCopy.setTaxonomyLevelChildren(true);
		
		// This filter is only useful in microsites
		List<TaxonomyLevel> launcherTaxonomyLevels = searchParams.getIdentToTaxonomyLevels().get(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER);
		if (launcherTaxonomyLevels == null || launcherTaxonomyLevels.isEmpty()) {
			return null;
		}

		List<Long> taxonomyLevelKeys = catalogService.getTaxonomyLevelsWithOffers(searchParamsCopy);
		if (taxonomyLevelKeys == null || taxonomyLevelKeys.isEmpty()) {
			return null;
		}
		
		TaxonomyLevel launcherTaxonomyLevel = launcherTaxonomyLevels.get(0);
		List<TaxonomyLevel> descendants = taxonomyLevelDao.getDescendants(launcherTaxonomyLevel, launcherTaxonomyLevel.getTaxonomy());
		descendants.removeIf(level -> !taxonomyLevelKeys.contains(level.getKey()));
		descendants.add(launcherTaxonomyLevel);
		
		SelectionValues taxonomyValues = getTaxonomyLevelsSV(translator, launcherTaxonomyLevel, descendants);
		FlexiTableMultiSelectionFilter flexiTableFilter = new FlexiTableMultiSelectionFilter(
				translator.translate("filter.taxonomy.children.label"), TYPE, taxonomyValues, catalogFilter.isDefaultVisible());
		flexiTableFilter.setUserObject(new TaxonomyLevelUserObject(catalogFilter.getKey().toString(), descendants));
		if (KEY_HIDE.equals(catalogFilter.getConfig())) {
			flexiTableFilter.setValues(List.of(launcherTaxonomyLevel.getKey().toString()));
		}
		return flexiTableFilter;
	}
	
	private SelectionValues getTaxonomyLevelsSV(Translator translator, TaxonomyLevel topTaxonomyLevel, List<TaxonomyLevel> taxonomyLevels) {
		SelectionValues keyValues = new SelectionValues();
		for (TaxonomyLevel level: taxonomyLevels) {
			List<String> names = new ArrayList<>();
			addParentNames(translator, names, level, topTaxonomyLevel);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(level.getKey().toString(), value));
		}
		keyValues.sort(VALUE_ASC);
		return keyValues;
	}
	
	private void addParentNames(Translator translator, List<String> names, TaxonomyLevel level, TaxonomyLevel topLevel) {
		names.add(TaxonomyUIFactory.translateDisplayName(translator, level));
		if (!level.equals(topLevel)) {
			TaxonomyLevel parent = level.getParent();
			if (parent != null) {
				addParentNames(translator, names, parent, topLevel);
			}
		}
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> taxonomyLevelKeys = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		TaxonomyLevelUserObject taxonomyLevelUserObject = (TaxonomyLevelUserObject)flexiTableFilter.getUserObject();
		if (taxonomyLevelKeys != null && !taxonomyLevelKeys.isEmpty()) {
			List<TaxonomyLevel> taxonomyLevels = taxonomyLevelUserObject.getTaxonomyLevels().stream()
					.filter(level -> taxonomyLevelKeys.contains(level.getKey().toString()))
					.collect(Collectors.toList());
			searchParams.getIdentToTaxonomyLevels().put(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER_OVERRIDE, taxonomyLevels);
			searchParams.setTaxonomyLevelChildren(false);
		} else {
			searchParams.getIdentToTaxonomyLevels().remove(CatalogRepositoryEntrySearchParams.KEY_LAUNCHER_OVERRIDE);
			searchParams.setTaxonomyLevelChildren(true);
		}
	}
	
	public static final class TaxonomyLevelUserObject {
		
		private final String ident;
		private final List<TaxonomyLevel> taxonomyLevels;
		
		public TaxonomyLevelUserObject(String ident, List<TaxonomyLevel> taxonomyLevels) {
			this.ident = ident;
			this.taxonomyLevels = taxonomyLevels;
		}
		
		public String getIdent() {
			return ident;
		}
		
		public List<TaxonomyLevel> getTaxonomyLevels() {
			return taxonomyLevels;
		}
		
	}
}
