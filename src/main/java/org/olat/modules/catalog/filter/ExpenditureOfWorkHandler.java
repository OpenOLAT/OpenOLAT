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
public class ExpenditureOfWorkHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "expenditure.of.work";
	
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
		return 155;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.expenditure.of.work.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.expenditure.of.work.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.expenditure.of.work.edit";
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
		
		List<String> expendituresOfWork = catalogService.getExpendituresOfWork(searchParams);
		if (expendituresOfWork == null || expendituresOfWork.isEmpty()) {
			return null;
		}
		
		SelectionValues filterKV = new SelectionValues();
		expendituresOfWork.forEach(expenditureOfWork -> filterKV.add(new SelectionValue(expenditureOfWork, expenditureOfWork)));
		filterKV.sort(SelectionValues.VALUE_ASC);
		
		return new FlexiTableMultiSelectionFilter(repositoryTranslator.translate("cif.expenditureOfWork"), TYPE, filterKV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void enrichSearchParams(CatalogRepositoryEntrySearchParams searchParams, FlexiTableFilter flexiTableFilter) {
		List<String> expenditureOfWork = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		searchParams.setExpendituresOfWork(expenditureOfWork);
	}
	
	/**
	 * We remove (but keep) the lady loading supplier until performance problems occur.
	 */
	@SuppressWarnings("unused")
	private static final class ExpenditureOfWorkSupplier implements SelectionValuesSupplier {
		
		private final CatalogRepositoryEntrySearchParams searchParams;
		private SelectionValues selectionValues;
		
		private ExpenditureOfWorkSupplier(CatalogRepositoryEntrySearchParams searchParams) {
			this.searchParams = searchParams;
		}

		private SelectionValues getSelectionValues() {
			if (selectionValues == null) {
				selectionValues = new SelectionValues();
				CoreSpringFactory.getImpl(CatalogV2Service.class)
						.getExpendituresOfWork(searchParams)
						.forEach(expenditureOfWork -> selectionValues.add(new SelectionValue(expenditureOfWork, expenditureOfWork)));
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
