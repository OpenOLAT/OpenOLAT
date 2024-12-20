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
import org.olat.modules.catalog.ui.admin.CatalogFilterBasicController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.model.AccessMethod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ACMethodHandler implements CatalogFilterHandler {
	
	private static final String TYPE = "acmethod";
	private static final String OPEN_ACCESS = "openAccess";
	
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private ACService acService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled(boolean isGuestOnly) {
		return acModule.isEnabled() && !isGuestOnly && acService.getAvailableMethods().stream().anyMatch(AccessMethod::isVisibleInGui);
	}

	@Override
	public int getSortOrder() {
		return 200;
	}

	@Override
	public String getTypeI18nKey() {
		return "filter.acmethod.type";
	}

	@Override
	public String getAddI18nKey() {
		return "filter.acmethod.add";
	}

	@Override
	public String getEditI18nKey() {
		return "filter.acmethod.edit";
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
		SelectionValues accessMethodKV = new SelectionValues();
			acService.getAvailableMethods().stream()
				.filter(AccessMethod::isVisibleInGui)
				.forEach(method -> accessMethodKV
				.add(entry(
						method.getType(),
						acModule.getAccessMethodHandler(method.getType()).getMethodName(translator.getLocale()),
						null,
						method.getMethodCssClass() + "_icon o_icon o_icon-fw",
						null,
						true)));
		accessMethodKV.sort(SelectionValues.VALUE_ASC);
		accessMethodKV.add(entry(OPEN_ACCESS, translator.translate("filter.acmethod.open.access"), null,
				"o_ac_openaccess_icon o_icon o_icon-fw", null, true));
		
		return new FlexiTableMultiSelectionFilter(translator.translate("filter.acmethod.type"), TYPE, accessMethodKV,
			catalogFilter.isDefaultVisible());
	}

	@Override
	public void filter(FlexiTableFilter flexiTableFilter, List<CatalogEntryRow> rows) {
		List<String> accessTypes = ((FlexiTableMultiSelectionFilter)flexiTableFilter).getValues();
		if (accessTypes != null && !accessTypes.isEmpty()) {
			boolean openAccess = accessTypes.contains(OPEN_ACCESS);
			rows.removeIf(row -> !isMatch(row, openAccess, accessTypes));
		}
	}

	private boolean isMatch(CatalogEntryRow row, boolean openAccess, List<String> accessTypes) {
		if (openAccess && row.isOpenAccess()) {
			return true;
		}
		return row.getAccessMethodTypes() != null && row.getAccessMethodTypes().stream().anyMatch(rowAccessType -> accessTypes.contains(rowAccessType));
	}
}
