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
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.ui.admin.CatalogFilterTaxonomyLevelController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyModule;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyLevelDAO;
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
public class TaxonomyLevelFilterHandler implements CatalogFilterHandler {
	
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
	public boolean isEnabled(boolean isGuestOnly) {
		return taxonomyModule.isEnabled() && StringHelper.isLong(repositoryModule.getTaxonomyTreeKey());
	}

	@Override
	public int getSortOrder() {
		return 300;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.taxonomy.level.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.taxonomy.level.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.taxonomy.level.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogFilter catalogFilter) {
		String config = catalogFilter.getConfig();
		if (StringHelper.isLong(config)) {
			TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> Long.valueOf(config));
			if (taxonomyLevel != null) {
				return taxonomyLevel.getDisplayName();
			}
		}
		return "-";
	}

	@Override
	public boolean isMultiInstance() {
		return true;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogFilter catalogFilter) {
		return new CatalogFilterTaxonomyLevelController(ureq, wControl, this, catalogFilter);
	}

	@Override
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogFilter catalogFilter) {
		if (!StringHelper.isLong(catalogFilter.getConfig())) {
			return null;
		}
		TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(() -> Long.valueOf(catalogFilter.getConfig()));
		if (taxonomyLevel == null) {
			return null;
		}
		
		List<TaxonomyLevel> descendants = taxonomyLevelDao.getDescendants(taxonomyLevel, taxonomyLevel.getTaxonomy());
		TaxonomyLevelUserObject taxonomyLevelUserObject = new TaxonomyLevelUserObject(catalogFilter.getKey().toString(), descendants);
		
		SelectionValues taxonomyValues = getTaxonomyLevelsSV(taxonomyLevel, descendants);
		FlexiTableMultiSelectionFilter flexiTableFilter = new FlexiTableMultiSelectionFilter(
				taxonomyLevel.getDisplayName(), TYPE, taxonomyValues, catalogFilter.isDefaultVisible());
		flexiTableFilter.setUserObject(taxonomyLevelUserObject);
		return flexiTableFilter;
	}
	
	private SelectionValues getTaxonomyLevelsSV(TaxonomyLevel taxonomyLevel, List<TaxonomyLevel> descendants) {
		SelectionValues keyValues = new SelectionValues();
		for (TaxonomyLevel level: descendants) {
			List<String> names = new ArrayList<>();
			addParentNames(names, level, taxonomyLevel);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(level.getKey().toString(), value));
		}
		keyValues.sort(VALUE_ASC);
		return keyValues;
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level, TaxonomyLevel topLevel) {
		names.add(level.getDisplayName());
		TaxonomyLevel parent = level.getParent();
		if (parent != null && !parent.equals(topLevel)) {
			addParentNames(names, parent, topLevel);
		}
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> taxonomyLevelKeys = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		TaxonomyLevelUserObject taxonomyLevelUserObject = (TaxonomyLevelUserObject)flexiTableFilter.getUserObject();
		if (taxonomyLevelKeys != null) {
			List<TaxonomyLevel> taxonomyLevels = taxonomyLevelUserObject.getTaxonomyLevels().stream()
					.filter(level -> taxonomyLevelKeys.contains(level.getKey().toString()))
					.collect(Collectors.toList());
			searchParams.getIdentToTaxonomyLevels().put(taxonomyLevelUserObject.getIdent(), taxonomyLevels);
		} else {
			searchParams.getIdentToTaxonomyLevels().remove(taxonomyLevelUserObject.getIdent());
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
