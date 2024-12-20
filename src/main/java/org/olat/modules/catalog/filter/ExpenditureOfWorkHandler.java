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
	public FlexiTableExtendedFilter createFlexiTableFilter(Translator translator, CatalogFilter catalogFilter,
			List<CatalogEntry> catalogEntries, TaxonomyLevel launcherTaxonomyLevel) {
		SelectionValues filterSV = new SelectionValues();
		catalogEntries.stream()
				.map(CatalogEntry::getExpenditureOfWork)
				.filter(StringHelper::containsNonWhitespace)
				.map(StringHelper::escapeHtml)
				.distinct()
				.sorted()
				.forEach(expenditureOfWork -> filterSV.add(new SelectionValue(
						expenditureOfWork,
						expenditureOfWork)));
		
		if (filterSV.isEmpty()) {
			return null;
		}
		
		return new FlexiTableMultiSelectionFilter(translator.translate("cif.expenditureOfWork"), TYPE, filterSV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void filter(FlexiTableFilter flexiTableFilter, List<CatalogEntryRow> rows) {
		List<String> expenditureOfWork = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		if (expenditureOfWork != null && !expenditureOfWork.isEmpty()) {
			rows.removeIf(row -> row.getExpenditureOfWork() == null || !expenditureOfWork.contains(row.getExpenditureOfWork()));
		}
	}
	
}
