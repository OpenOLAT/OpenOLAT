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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.ui.CatalogEntryRow;
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
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogFilter catalogFilter,
			List<CatalogEntry> catalogEntries, TaxonomyLevel launcherTaxonomyLevel) {
		
		// This filter is only useful in microsites
		if (launcherTaxonomyLevel == null) {
			return null;
		}
		
		// In no single entry is present, the filter is not displayed
		Set<Long> taxonomyLevelKeys = getTaxonomyLevelsWithEntries(catalogEntries, launcherTaxonomyLevel.getMaterializedPathKeys());
		if (taxonomyLevelKeys == null || taxonomyLevelKeys.isEmpty()) {
			return null;
		}
		
		List<TaxonomyLevel> descendants = taxonomyLevelDao.getDescendants(launcherTaxonomyLevel, launcherTaxonomyLevel.getTaxonomy());
		descendants.removeIf(level -> !taxonomyLevelKeys.contains(level.getKey()));
		descendants.add(launcherTaxonomyLevel);
		
		SelectionValues taxonomyValues = getTaxonomyLevelsSV(translator, launcherTaxonomyLevel, descendants);
		FlexiTableMultiSelectionFilter flexiTableFilter = new FlexiTableMultiSelectionFilter(
				translator.translate("filter.taxonomy.children.label"), TYPE, taxonomyValues, catalogFilter.isDefaultVisible());
		if (KEY_HIDE.equals(catalogFilter.getConfig())) {
			flexiTableFilter.setValues(List.of(launcherTaxonomyLevel.getKey().toString()));
		}
		return flexiTableFilter;
	}
	
	private Set<Long> getTaxonomyLevelsWithEntries(List<CatalogEntry> catalogEntries, String taxonomyLevelPathKey) {
		Set<Long> taxonomyLevelsWithEntries = new HashSet<>();
		
		for (CatalogEntry catalogEntry : catalogEntries) {
			if (catalogEntry.getTaxonomyLevels() != null && !catalogEntry.getTaxonomyLevels().isEmpty()) {
				for (TaxonomyLevel taxonomyLevel : catalogEntry.getTaxonomyLevels()) {
					if (taxonomyLevel.getMaterializedPathKeys().startsWith(taxonomyLevelPathKey) && !taxonomyLevel.getMaterializedPathKeys().equals(taxonomyLevelPathKey) ) {
						taxonomyLevelsWithEntries.add(taxonomyLevel.getKey());
					}
				}
			}
		}
		
		return taxonomyLevelsWithEntries;
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
	public void filter(FlexiTableFilter flexiTableFilter, List<CatalogEntryRow> rows) {
		List<String> taxonomyLevelKeyStrs = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		if (taxonomyLevelKeyStrs != null && !taxonomyLevelKeyStrs.isEmpty()) {
			Set<Long> taxonomyLevelKeys = taxonomyLevelKeyStrs.stream().map(Long::valueOf).collect(Collectors.toSet());
			rows.removeIf(row -> !isMatch(row, taxonomyLevelKeys));
		}
	}
	
	// Show entry if any taxonomy level of the entry is exactly one of the selected levels (no sublevels)
	private boolean isMatch(CatalogEntryRow row, Set<Long> taxonomyLevelKeys) {
		if (row.getTaxonomyLevels() != null && !row.getTaxonomyLevels().isEmpty()) {
			for (TaxonomyLevel taxonomyLevel : row.getTaxonomyLevels()) {
				for (Long taxonomyLevelKey : taxonomyLevelKeys) {
					if (taxonomyLevel.getKey().equals(taxonomyLevelKey)) {
						return true;
					}
				}
				
			}
		}
		return false;
	}
}
