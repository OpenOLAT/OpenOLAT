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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
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
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogRepositoryEntrySearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.olat.repository.RepositoryService;
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
	@Autowired
	private CatalogV2Service catalogService;

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
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogRepositoryEntrySearchParams searchParams, CatalogFilter catalogFilter) {
		Translator repositoryTranslator = Util.createPackageTranslator(RepositoryService.class, translator.getLocale());
		
		List<Long> licenseTypeKeys = catalogService.getLicenseTypeKeys(searchParams);
		if (licenseTypeKeys == null || licenseTypeKeys.isEmpty()) {
			return null;
		}
		
		List<LicenseType> licenseTypes = licenseService.loadLicenseTypes().stream()
				.filter(licenseType -> licenseTypeKeys.contains(licenseType.getKey()))
				.collect(Collectors.toList());
		if (licenseTypes == null || licenseTypes.isEmpty()) {
			return null;
		}
		
		SelectionValues filterKV = new SelectionValues();
		licenseTypes.stream()
				.filter(licenseType -> !licenseService.isNoLicense(licenseType))
				.forEach(licenseType -> filterKV.add(new SelectionValue(
						licenseType.getKey().toString(),
						LicenseUIFactory.translate(licenseType, translator.getLocale()))));
		filterKV.sort(SelectionValues.VALUE_ASC);
		
		return new FlexiTableMultiSelectionFilter(repositoryTranslator.translate("cif.license"), TYPE, filterKV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> licenseTypeKeyStr = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		Collection<Long> licenseTypeKeys = licenseTypeKeyStr != null && !licenseTypeKeyStr.isEmpty()
				? licenseTypeKeyStr.stream().map(Long::valueOf).collect(Collectors.toList())
				: null;
		searchParams.setLicenseTypeKeys(licenseTypeKeys);
	}
}
