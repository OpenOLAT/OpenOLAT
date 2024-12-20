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

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.List;
import java.util.Objects;
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
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogEntry;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.ui.CatalogEntryRow;
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EducationalTypeHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "educationaltype";

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
		return 120;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.educational.type.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.educational.type.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.educational.type.edit";
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
		SelectionValues typesSV = new SelectionValues();
		catalogEntries.stream()
				.map(CatalogEntry::getEducationalType)
				.filter(Objects::nonNull)
				.forEach(type -> typesSV
				.add(entry(
						type.getKey().toString(),
						StringHelper.escapeHtml(translator.translate(RepositoyUIFactory.getI18nKey(type))))));
		if (typesSV.isEmpty() || typesSV.size() == 1) {
			return null;
		}
		
		typesSV.sort(SelectionValues.VALUE_ASC);
		
		return new FlexiTableMultiSelectionFilter(translator.translate("cif.educational.type"), TYPE, typesSV,
				catalogFilter.isDefaultVisible());
	}

	@Override
	public void filter(FlexiTableFilter flexiTableFilter, List<CatalogEntryRow> rows) {
		List<String> educationalTypeKeyStr = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		if (educationalTypeKeyStr != null && !educationalTypeKeyStr.isEmpty()) {
			Set<Long> educationalTypeKeys = educationalTypeKeyStr.stream().map(Long::valueOf).collect(Collectors.toSet());
			rows.removeIf(row -> row.getEducationalType() == null
					|| !educationalTypeKeys.contains(row.getEducationalType().getKey()));
		}
	}
	
}
