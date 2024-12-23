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
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.license.License;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.ui.CatalogEntryRow;
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.manager.RepositoryEntryLicenseHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LicenseHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "license";
	
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private RepositoryEntryLicenseHandler licenseHandler;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(boolean isGuestOnly) {
		return licenseModule.isEnabled(licenseHandler);
	}

	@Override
	public int getSortOrder() {
		return 160;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.license.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.license.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.license.edit";
	}

	@Override
	public String getDetails(Translator translator, CatalogFilter catalogFilter) {
		return null;
	}

	@Override
	public boolean isMultiInstance() {
		return false;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, CatalogFilter catalogFilter) {
		return new CatalogFilterBasicController(ureq, wControl, this, catalogFilter);
	}
	
	@Override
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogFilter catalogFilter,
			List<CatalogEntry> catalogEntries, TaxonomyLevel launcherTaxonomyLevel) {
		SelectionValues filterSV = new SelectionValues();
		catalogEntries.stream()
				.map(CatalogEntry::getLicense)
				.filter(Objects::nonNull)
				.map(License::getLicenseType)
				.filter(licenseType -> !licenseService.isNoLicense(licenseType))
				.distinct()
				.forEach(licenseType -> filterSV.add(new SelectionValue(
						licenseType.getKey().toString(),
						StringHelper.escapeHtml(LicenseUIFactory.translate(licenseType, translator.getLocale())))));
		
		if (filterSV.isEmpty()) {
			return null;
		}
		
		filterSV.sort(SelectionValues.VALUE_ASC);
		
		return new FlexiTableMultiSelectionFilter(translator.translate("cif.license"), TYPE, filterSV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void filter(FlexiTableFilter flexiTableFilter, List<CatalogEntryRow> rows) {
		List<String> licenseTypeKeyStr = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		if (licenseTypeKeyStr != null && !licenseTypeKeyStr.isEmpty()) {
			Set<Long> licenseTypeKeys = licenseTypeKeyStr.stream().map(Long::valueOf).collect(Collectors.toSet());
			rows.removeIf(row -> row.getLicense() == null 
					|| row.getLicense().getLicenseType() == null
					|| !licenseTypeKeys.contains(row.getLicense().getLicenseType().getKey()));
		}
	}
	
}
