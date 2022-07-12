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

import java.util.List;

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
import org.olat.modules.catalog.ui.admin.CatalogFilterTaxonomyChildrenController;
import org.olat.modules.taxonomy.TaxonomyModule;
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
	private static final String TYPE = "taxonomy.level.children";
	
	@Autowired
	private TaxonomyModule taxonomyModule;
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
		SelectionValues childrenSV = new SelectionValues();
		childrenSV.add(SelectionValues.entry(KEY_SHOW, translator.translate("filter.taxonomy.children.show")));
		FlexiTableMultiSelectionFilter flexiTableFilter = new FlexiTableMultiSelectionFilter(
				translator.translate("filter.taxonomy.children.label"), TYPE, childrenSV, catalogFilter.isDefaultVisible());
		if (KEY_SHOW.equals(catalogFilter.getConfig())) {
			flexiTableFilter.setValues(List.of(KEY_SHOW));
		}
		return flexiTableFilter;
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> taxonomyLevelKeys = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		searchParams.setTaxonomyLevelChildren(taxonomyLevelKeys != null && taxonomyLevelKeys.contains(KEY_SHOW));
	}
}
