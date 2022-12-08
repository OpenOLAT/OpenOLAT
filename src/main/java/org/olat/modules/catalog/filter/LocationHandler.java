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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 16 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LocationHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "location";
	
	@Autowired
	private CatalogV2Service catalogService;
	
	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(boolean isGuestOnly) {
		return true;
	}

	@Override
	public int getSortOrder() {
		return 180;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.location.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.location.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.location.edit";
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
		
		List<String> locations = catalogService.getLocations(searchParams);
		if (locations == null || locations.isEmpty()) {
			return null;
		}
		
		SelectionValues filterKV = new SelectionValues();
		locations.forEach(location -> filterKV.add(new SelectionValue(location, location)));
		filterKV.sort(SelectionValues.VALUE_ASC);
		
		return new FlexiTableMultiSelectionFilter(repositoryTranslator.translate("cif.location"), TYPE, filterKV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> locations = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		searchParams.setLocations(locations);
	}
	
	/**
	 * We remove (but keep) the lady loading supplier until performance problems occur.
	 */
	@SuppressWarnings("unused")
	private static final class LocationSupplier implements SelectionValuesSupplier {
		
		private final CatalogRepositoryEntrySearchParams searchParams;
		private SelectionValues selectionValues;
		
		private LocationSupplier(CatalogRepositoryEntrySearchParams searchParams) {
			this.searchParams = searchParams;
		}

		private SelectionValues getSelectionValues() {
			if (selectionValues == null) {
				selectionValues = new SelectionValues();
				CoreSpringFactory.getImpl(CatalogV2Service.class)
						.getLocations(searchParams)
						.forEach(location -> selectionValues.add(new SelectionValue(location, location)));
				selectionValues.sort(SelectionValues.VALUE_ASC);
			}
			return selectionValues;
		}

		@Override
		public String getValue(String key) {
			return getSelectionValues().getValue(key);
		}

		@Override
		public String[] keys() {
			return getSelectionValues().keys();
		}

		@Override
		public String[] values() {
			return getSelectionValues().values();
		}
		
	}
}
